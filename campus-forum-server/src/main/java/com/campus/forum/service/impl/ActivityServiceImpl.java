package com.campus.forum.service.impl;

import com.campus.forum.common.PageResult;
import com.campus.forum.common.ResultCode;
import com.campus.forum.entity.ForumPost;
import com.campus.forum.entity.ForumSection;
import com.campus.forum.entity.ServiceActivity;
import com.campus.forum.entity.SysUser;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.ForumPostMapper;
import com.campus.forum.mapper.ForumSectionMapper;
import com.campus.forum.mapper.ServiceActivityMapper;
import com.campus.forum.mapper.SysUserMapper;
import com.campus.forum.service.ActivityService;
import com.campus.forum.service.NoticeService;
import com.campus.forum.service.SmartAuditService;
import com.campus.forum.search.SearchSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ActivityServiceImpl implements ActivityService {

    private static final Logger log = LoggerFactory.getLogger(ActivityServiceImpl.class);

    @Autowired
    private ServiceActivityMapper activityMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private ForumSectionMapper sectionMapper;

    @Autowired
    private ForumPostMapper forumPostMapper;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private SmartAuditService smartAuditService;

    @Autowired
    private SearchSyncService searchSyncService;

    @Override
    public PageResult<ServiceActivity> getActivityList(Long current, Long size, String type, String keyword) {
        long pageNo = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 10 : size;
        long offset = (pageNo - 1) * pageSize;
        List<ServiceActivity> records = activityMapper.selectPage(type, keyword, offset, pageSize);
        fillRelations(records, null);
        Long total = activityMapper.countPage(type, keyword);
        return new PageResult<>(pageNo, pageSize, total == null ? 0L : total, records);
    }

    @Override
    public ServiceActivity getActivityDetail(Long id, Long userId) {
        ServiceActivity activity = activityMapper.selectById(id);
        if (activity == null || (activity.getDeleted() != null && activity.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        }
        activityMapper.increaseViewCount(id);
        activity.setViewCount((activity.getViewCount() == null ? 0 : activity.getViewCount()) + 1);
        fillRelations(List.of(activity), userId);
        return activity;
    }

    @Override
    public ServiceActivity createActivity(ServiceActivity activity) {
        if (activity == null || activity.getUserId() == null || !StringUtils.hasText(activity.getTitle())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "活动标题不能为空");
        }
        if (activity.getAuditStatus() == null) {
            activity.setAuditStatus(0);
        }
        if (activity.getStatus() == null) {
            activity.setStatus(1);
        }

        // 先审核，再入库
        SmartAuditService.AuditResult auditResult = smartAuditService.auditActivity(
                activity.getUserId(), activity.getTitle(), activity.getDescription(), null);

        if (auditResult.getAuditStatus() == SmartAuditService.AuditStatus.AUTO_REJECT.getCode()) {
            log.warn("活动内容被拦截(含违规关键词): userId={}, reason={}", activity.getUserId(), auditResult.getReason());
            throw new BusinessException(ResultCode.CONTENT_AUDIT_BLOCKED, auditResult.getReason());
        }

        if (!auditResult.isPassed()) {
            log.warn("活动内容需人工审核: userId={}, reason={}", activity.getUserId(), auditResult.getReason());
        }
        if (auditResult.getAuditStatus() > 0) {
            activity.setAuditStatus(mapAuditStatusToContentStatus(auditResult.getAuditStatus()));
        }

        activityMapper.insert(activity);

        bindForumPost(activity);

        ServiceActivity saved = activityMapper.selectById(activity.getId());
        fillRelations(List.of(saved), activity.getUserId());
        searchSyncService.syncActivity(saved.getId());
        return saved;
    }

    @Override
    public void signupActivity(Long activityId, Long userId) {
        ServiceActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        }
        if (activity.getMaxParticipants() != null && activity.getCurrentParticipants() != null
                && activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
            throw new BusinessException(ResultCode.ACTIVITY_FULL);
        }
        int changed = activityMapper.insertSignup(activityId, userId);
        if (changed > 0) {
            activityMapper.updateParticipantCount(activityId, 1);
            // 发送报名通知
            noticeService.sendActivitySignupNotice(userId, activity.getTitle(), activityId);
        }
    }

    @Override
    public void cancelSignup(Long activityId, Long userId) {
        int changed = activityMapper.deleteSignup(activityId, userId);
        if (changed > 0) {
            activityMapper.updateParticipantCount(activityId, -1);
        }
    }

    @Override
    public PageResult<ServiceActivity> getAdminActivityList(Long current, Long size, String type, Integer status, Integer auditStatus, String keyword) {
        long pageNo = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 10 : size;
        long offset = (pageNo - 1) * pageSize;
        List<ServiceActivity> records = activityMapper.selectAdminPage(type, status, auditStatus, keyword, offset, pageSize);
        fillRelations(records, null);
        Long total = activityMapper.countAdminPage(type, status, auditStatus, keyword);
        return new PageResult<>(pageNo, pageSize, total == null ? 0L : total, records);
    }

    @Override
    public void auditActivity(Long id, Integer auditStatus) {
        ServiceActivity activity = activityMapper.selectById(id);
        if (activity == null || (activity.getDeleted() != null && activity.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        }
        int changed = activityMapper.updateAuditStatus(id, auditStatus);
        if (changed == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在或无权限操作");
        }
        searchSyncService.syncActivity(id);
        // 发送审核通知
        noticeService.sendServiceAuditNotice(activity.getUserId(), "活动", activity.getTitle(), auditStatus, activity.getId(), 4);
    }

    private void fillRelations(List<ServiceActivity> list, Long currentUserId) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (ServiceActivity item : list) {
            SysUser organizer = userMapper.selectById(item.getUserId());
            if (organizer != null) {
                organizer.setPassword(null);
            }
            item.setOrganizeUser(organizer);
            if (currentUserId != null) {
                item.setIsSignedUp(activityMapper.countSignup(item.getId(), currentUserId) > 0);
            } else {
                item.setIsSignedUp(false);
            }
        }
    }

    private void bindForumPost(ServiceActivity activity) {
        ForumSection section = sectionMapper.selectByCode("ACTIVITY");
        if (section == null) {
            return;
        }
        ForumPost post = new ForumPost();
        post.setUserId(activity.getUserId());
        post.setSectionId(section.getId());
        post.setTitle(activity.getTitle());
        post.setContent(activity.getDescription());
        post.setImages(activity.getImages());
        post.setAuditStatus(activity.getAuditStatus());
        post.setSourceType(3);
        post.setSourceId(activity.getId());
        post.setIsAnonymous(0);
        forumPostMapper.insert(post);
        activityMapper.updatePostId(activity.getId(), post.getId());
        activity.setPostId(post.getId());
    }

    private int mapAuditStatusToContentStatus(int auditStatus) {
        switch (auditStatus) {
            case 1: return 1;
            case 2: return 2;
            case 3: return 0;
            case 4: return 0;
            default: return 0;
        }
    }
}
