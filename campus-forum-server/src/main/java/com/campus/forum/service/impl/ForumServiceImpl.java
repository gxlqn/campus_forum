package com.campus.forum.service.impl;

import com.campus.forum.common.PageResult;
import com.campus.forum.common.ResultCode;
import com.campus.forum.entity.ForumComment;
import com.campus.forum.entity.ForumPost;
import com.campus.forum.entity.ForumSection;
import com.campus.forum.entity.SysUser;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.ForumCommentMapper;
import com.campus.forum.mapper.ForumPostMapper;
import com.campus.forum.mapper.ForumSectionMapper;
import com.campus.forum.mapper.SysUserMapper;
import com.campus.forum.service.ForumService;
import com.campus.forum.service.MessageService;
import com.campus.forum.service.SmartAuditService;
import com.campus.forum.search.SearchSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ForumServiceImpl implements ForumService {

    private static final Logger log = LoggerFactory.getLogger(ForumServiceImpl.class);

    @Autowired
    private ForumPostMapper postMapper;

    @Autowired
    private ForumSectionMapper sectionMapper;

    @Autowired
    private ForumCommentMapper commentMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private MessageService messageService;

    @Autowired
    private SmartAuditService smartAuditService;

    @Autowired
    private SearchSyncService searchSyncService;

    @Override
    public List<ForumSection> getAllSections() {
        return sectionMapper.selectEnabledSections();
    }

    @Override
    public PageResult<ForumPost> getPostList(Long current, Long size, Long sectionId, String keyword, String orderBy, Integer hotWindowHours) {
        long pageNo = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 10 : size;
        long offset = (pageNo - 1) * pageSize;
        String normalizedOrderBy = "hot".equalsIgnoreCase(orderBy) ? "hot" : "latest";
        int normalizedHotWindowHours = hotWindowHours == null || hotWindowHours < 1 ? 72 : Math.min(hotWindowHours, 24 * 30);
        List<ForumPost> records = postMapper.selectPostPage(sectionId, keyword, normalizedOrderBy, normalizedHotWindowHours, offset, pageSize);
        fillPostRelations(records, null);
        Long total = postMapper.countPostPage(sectionId, keyword, normalizedOrderBy, normalizedHotWindowHours);
        return new PageResult<>(pageNo, pageSize, total == null ? 0L : total, records);
    }

    @Override
    public ForumPost getPostDetail(Long id) {
        return getPostDetail(id, null, true);
    }

    @Override
    public ForumPost getPostDetail(Long id, Long userId) {
        return getPostDetail(id, userId, true);
    }

    @Override
    public ForumPost getPostDetail(Long id, Long userId, boolean incrementView) {
        ForumPost post = postMapper.selectById(id);
        if (post == null || (post.getDeleted() != null && post.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }
        
        // 仅在首次加载时增加浏览量，刷新时不增加
        if (incrementView) {
            postMapper.increaseViewCount(id);
            post.setViewCount((post.getViewCount() == null ? 0 : post.getViewCount()) + 1);
        }
        
        fillPostRelations(List.of(post), userId);
        return post;
    }

    @Override
    public ForumPost createPost(ForumPost post) {
        if (post == null || post.getUserId() == null || !StringUtils.hasText(post.getTitle())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "帖子标题不能为空");
        }
        if (post.getSectionId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "请选择板块");
        }

        SmartAuditService.AuditResult auditResult = smartAuditService.auditPost(
                post.getUserId(), post.getTitle(), post.getContent(), null);

        if (auditResult.getAuditStatus() == SmartAuditService.AuditStatus.AUTO_REJECT.getCode()) {
            log.warn("帖子内容被拦截(含违规关键词): userId={}, reason={}", post.getUserId(), auditResult.getReason());
            throw new BusinessException(ResultCode.CONTENT_AUDIT_BLOCKED, auditResult.getReason());
        }

        if (!auditResult.isPassed()) {
            log.warn("帖子内容需人工审核: userId={}, reason={}", post.getUserId(), auditResult.getReason());
        }

        if (post.getIsAnonymous() == null) {
            post.setIsAnonymous(0);
        }
        if (auditResult.getAuditStatus() > 0) {
            post.setAuditStatus(auditResult.getAuditStatus());
        } else if (post.getAuditStatus() == null) {
            post.setAuditStatus(0);
        }
        if (post.getSourceType() == null) {
            post.setSourceType(0);
        }
        postMapper.insert(post);
        ForumPost saved = postMapper.selectById(post.getId());
        fillPostRelations(List.of(saved), post.getUserId());
        searchSyncService.syncPost(saved.getId());
        return saved;
    }

    @Override
    public ForumPost updatePost(ForumPost post, Long userId) {
        if (post == null || post.getId() == null || userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        if (!StringUtils.hasText(post.getTitle())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "帖子标题不能为空");
        }
        post.setUserId(userId);
        int changed = postMapper.updateByOwner(post);
        if (changed == 0) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND, "帖子不存在或无权限编辑");
        }
        ForumPost saved = postMapper.selectById(post.getId());
        fillPostRelations(List.of(saved), userId);
        searchSyncService.syncPost(saved.getId());
        return saved;
    }

    @Override
    public void deletePost(Long id, Long userId) {
        if (id == null || userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        int changed = postMapper.deleteByOwner(id, userId);
        if (changed == 0) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND, "帖子不存在或无权限删除");
        }
        searchSyncService.deletePost(id);
        log.info("Delete post: {}", id);
    }

    @Override
    public void likePost(Long postId, Long userId) {
        if (postId == null || userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        int changed = postMapper.insertPostLike(userId, postId);
        if (changed > 0) {
            postMapper.updateLikeCount(postId, 1);

            ForumPost post = postMapper.selectById(postId);
            if (post != null && post.getUserId() != null && !userId.equals(post.getUserId())) {
                SysUser liker = userMapper.selectById(userId);
                String likerName = (liker != null && StringUtils.hasText(liker.getNickname()))
                        ? liker.getNickname()
                        : "用户";
                messageService.sendNotification(
                        post.getUserId(),
                        userId,
                        4,
                        likerName + " 赞了你的帖子",
                        StringUtils.hasText(post.getTitle()) ? post.getTitle() : "",
                        1,
                        postId
                );
            }
        }
    }

    @Override
    public void unlikePost(Long postId, Long userId) {
        if (postId == null || userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        int changed = postMapper.deletePostLike(userId, postId);
        if (changed > 0) {
            postMapper.updateLikeCount(postId, -1);
        }
    }

    @Override
    public void favoritePost(Long postId, Long userId) {
        if (postId == null || userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        int changed = postMapper.insertPostFavorite(userId, postId);
        if (changed > 0) {
            postMapper.updateFavoriteCount(postId, 1);
        }
    }

    @Override
    public void unfavoritePost(Long postId, Long userId) {
        if (postId == null || userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        int changed = postMapper.deletePostFavorite(userId, postId);
        if (changed > 0) {
            postMapper.updateFavoriteCount(postId, -1);
        }
    }

    @Override
    public PageResult<ForumPost> getMyPosts(Long userId, Long current, Long size, String keyword) {
        long pageNo = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 10 : size;
        long offset = (pageNo - 1) * pageSize;
        List<ForumPost> records = postMapper.selectMyPosts(userId, offset, pageSize, keyword);
        fillPostRelations(records, userId);
        Long total = postMapper.countMyPosts(userId, keyword);
        return new PageResult<>(pageNo, pageSize, total == null ? 0L : total, records);
    }

    @Override
    public PageResult<ForumPost> getMyFavoritePosts(Long userId, Long current, Long size) {
        long pageNo = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 10 : size;
        long offset = (pageNo - 1) * pageSize;
        List<ForumPost> records = postMapper.selectMyFavoritePosts(userId, offset, pageSize);
        fillPostRelations(records, userId);
        Long total = postMapper.countMyFavoritePosts(userId);
        return new PageResult<>(pageNo, pageSize, total == null ? 0L : total, records);
    }

    @Override
    public List<ForumComment> getCommentsByPostId(Long postId) {
        List<ForumComment> comments = commentMapper.selectByPostId(postId);
        for (ForumComment comment : comments) {
            SysUser author = userMapper.selectById(comment.getUserId());
            if (author != null) {
                author.setPassword(null);
            }
            comment.setAuthor(author);
        }
        return comments;
    }

    @Override
    public ForumComment addComment(ForumComment comment) {
        if (comment == null || comment.getPostId() == null || comment.getUserId() == null || !StringUtils.hasText(comment.getContent())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "评论内容不能为空");
        }
        if (comment.getParentId() == null) {
            comment.setParentId(0L);
        }
        if (comment.getIsAnonymous() == null) {
            comment.setIsAnonymous(0);
        }
        SmartAuditService.AuditResult auditResult = smartAuditService.auditComment(
                comment.getUserId(), comment.getContent(), null);

        if (auditResult.getAuditStatus() == SmartAuditService.AuditStatus.AUTO_REJECT.getCode()) {
            log.warn("评论内容被拦截(含违规关键词): userId={}, reason={}", comment.getUserId(), auditResult.getReason());
            throw new BusinessException(ResultCode.CONTENT_AUDIT_BLOCKED, auditResult.getReason());
        }

        if (!auditResult.isPassed()) {
            log.warn("评论内容需人工审核: userId={}, reason={}", comment.getUserId(), auditResult.getReason());
        }

        if (auditResult.getAuditStatus() > 0) {
            comment.setAuditStatus(mapAuditStatusToContentStatus(auditResult.getAuditStatus()));
        } else if (comment.getAuditStatus() == null) {
            comment.setAuditStatus(0);
        }
        commentMapper.insert(comment);
        commentMapper.increasePostCommentCount(comment.getPostId());
        
        ForumPost post = postMapper.selectById(comment.getPostId());
        if (post != null && !comment.getUserId().equals(post.getUserId())) {
            SysUser commenter = userMapper.selectById(comment.getUserId());
            String commenterName = (commenter != null && StringUtils.hasText(commenter.getNickname()))
                    ? commenter.getNickname()
                    : "匿名用户";
            String previewContent = comment.getContent();
            if (previewContent.length() > 50) {
                previewContent = previewContent.substring(0, 50) + "...";
            }
            messageService.sendNotification(
                    post.getUserId(),
                    comment.getUserId(),
                    3,
                    commenterName + " 评论了你的帖子",
                    previewContent,
                    1,
                    comment.getPostId()
            );
            
            log.info("已向用户 {} 发送评论通知，评论者: {}，帖子ID: {}", post.getUserId(), comment.getUserId(), comment.getPostId());
        }
        
        SysUser author = userMapper.selectById(comment.getUserId());
        if (author != null) {
            author.setPassword(null);
        }
        comment.setAuthor(author);
        return comment;
    }

    /**
     * 将审核服务的AuditStatus映射到内容表的audit_status字段
     * 审核服务: 0-待审核, 1-自动通过, 2-自动拒绝, 3-AI不确定, 4-待人工复核
     * 内容表: 0-待审核, 1-审核通过, 2-审核拒绝
     */
    private int mapAuditStatusToContentStatus(int auditStatus) {
        switch (auditStatus) {
            case 1: return 1;  // AUTO_PASS -> 通过
            case 2: return 2;  // AUTO_REJECT -> 拒绝
            case 3: return 0;  // AI_UNCERTAIN -> 待审核(人工复核)
            case 4: return 0;  // MANUAL_REVIEW -> 待审核(人工复核)
            default: return 0; // PENDING -> 待审核
        }
    }

    private void fillPostRelations(List<ForumPost> posts, Long currentUserId) {
        if (posts == null || posts.isEmpty()) {
            return;
        }
        for (ForumPost post : posts) {
            SysUser author = userMapper.selectById(post.getUserId());
            if (author != null) {
                author.setPassword(null);
            }
            post.setAuthor(author);
            post.setSection(sectionMapper.selectById(post.getSectionId()));
            if (currentUserId != null) {
                post.setIsLiked(postMapper.countUserPostLike(currentUserId, post.getId()) > 0);
                post.setIsFavorited(postMapper.countUserPostFavorite(currentUserId, post.getId()) > 0);
            } else {
                post.setIsLiked(false);
                post.setIsFavorited(false);
            }
        }
    }
}
