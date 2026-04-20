package com.campus.forum.service.impl;

import com.campus.forum.common.PageResult;
import com.campus.forum.common.ResultCode;
import com.campus.forum.entity.ForumPost;
import com.campus.forum.entity.ForumSection;
import com.campus.forum.entity.ServiceLostFound;
import com.campus.forum.entity.SysUser;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.ForumPostMapper;
import com.campus.forum.mapper.ForumSectionMapper;
import com.campus.forum.mapper.ServiceLostFoundMapper;
import com.campus.forum.mapper.SysUserMapper;
import com.campus.forum.service.LostFoundService;
import com.campus.forum.service.NoticeService;
import com.campus.forum.service.SmartAuditService;
import com.campus.forum.mapper.ServiceLostFoundClaimMapper;
import com.campus.forum.entity.ServiceLostFoundClaim;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class LostFoundServiceImpl implements LostFoundService {

    private static final Logger log = LoggerFactory.getLogger(LostFoundServiceImpl.class);

    @Autowired
    private ServiceLostFoundMapper lostFoundMapper;

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
    private ServiceLostFoundClaimMapper claimMapper;

    @Override
    public PageResult<ServiceLostFound> getList(Long current, Long size, Integer type, String keyword) {
        long pageNo = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 10 : size;
        long offset = (pageNo - 1) * pageSize;
        List<ServiceLostFound> records = lostFoundMapper.selectPage(type, keyword, offset, pageSize);
        fillRelations(records);
        Long total = lostFoundMapper.countPage(type, keyword);
        return new PageResult<>(pageNo, pageSize, total == null ? 0L : total, records);
    }

    @Override
    public ServiceLostFound getDetail(Long id) {
        ServiceLostFound item = lostFoundMapper.selectById(id);
        if (item == null || (item.getDeleted() != null && item.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "失物招领信息不存在");
        }
        lostFoundMapper.increaseViewCount(id);
        item.setViewCount((item.getViewCount() == null ? 0 : item.getViewCount()) + 1);
        fillRelations(List.of(item));
        return item;
    }

    @Override
    public ServiceLostFound create(ServiceLostFound item) {
        if (item == null || item.getUserId() == null || !StringUtils.hasText(item.getTitle()) || item.getType() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "发布信息不完整");
        }
        if (item.getAuditStatus() == null) {
            item.setAuditStatus(0);
        }
        if (item.getStatus() == null) {
            item.setStatus(1);
        }

        // 先审核，再入库
        SmartAuditService.AuditResult auditResult = smartAuditService.auditLostFound(
                item.getUserId(), item.getTitle(), item.getDescription(), null);

        if (auditResult.getAuditStatus() == SmartAuditService.AuditStatus.AUTO_REJECT.getCode()) {
            log.warn("失物招领内容被拦截(含违规关键词): userId={}, reason={}", item.getUserId(), auditResult.getReason());
            throw new BusinessException(ResultCode.CONTENT_AUDIT_BLOCKED, auditResult.getReason());
        }

        if (!auditResult.isPassed()) {
            log.warn("失物招领内容需人工审核: userId={}, reason={}", item.getUserId(), auditResult.getReason());
        }
        if (auditResult.getAuditStatus() > 0) {
            item.setAuditStatus(mapAuditStatusToContentStatus(auditResult.getAuditStatus()));
        }

        lostFoundMapper.insert(item);

        bindForumPost(item);
        ServiceLostFound saved = lostFoundMapper.selectById(item.getId());
        fillRelations(List.of(saved));
        return saved;
    }

    @Override
    public ServiceLostFound update(ServiceLostFound item, Long userId) {
        if (item == null || item.getId() == null || userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        item.setUserId(userId);
        int changed = lostFoundMapper.updateByOwner(item);
        if (changed == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "信息不存在或无权限编辑");
        }
        ServiceLostFound saved = lostFoundMapper.selectById(item.getId());
        fillRelations(List.of(saved));
        return saved;
    }

    @Override
    public void delete(Long id, Long userId) {
        int changed = lostFoundMapper.deleteByOwner(id, userId);
        if (changed == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "信息不存在或无权限删除");
        }
    }

    @Override
    public void markComplete(Long id, Long userId) {
        int changed = lostFoundMapper.markComplete(id, userId);
        if (changed == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "信息不存在或无权限操作");
        }
    }

    @Override
    public PageResult<ServiceLostFound> getAdminList(Long current, Long size, Integer type, Integer status, Integer auditStatus, String keyword) {
        long pageNo = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 10 : size;
        long offset = (pageNo - 1) * pageSize;
        log.info("失物招领管理查询: type={}, status={}, auditStatus={}, keyword={}, page={}, size={}",
                type, status, auditStatus, keyword, pageNo, pageSize);
        List<ServiceLostFound> records = lostFoundMapper.selectAdminPage(type, status, auditStatus, keyword, offset, pageSize);
        fillRelations(records);
        Long total = lostFoundMapper.countAdminPage(type, status, auditStatus, keyword);
        log.info("失物招领管理查询结果: records={}, total={}", records.size(), total);
        return new PageResult<>(pageNo, pageSize, total == null ? 0L : total, records);
    }

    @Override
    public void auditItem(Long id, Integer auditStatus) {
        ServiceLostFound item = lostFoundMapper.selectById(id);
        if (item == null || (item.getDeleted() != null && item.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "失物招领信息不存在");
        }
        int changed = lostFoundMapper.updateAuditStatus(id, auditStatus);
        if (changed == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "信息不存在或无权限操作");
        }
        // 发送审核通知
        noticeService.sendServiceAuditNotice(item.getUserId(), "失物招领", item.getTitle(), auditStatus, item.getId(), 6);
    }

    private void fillRelations(List<ServiceLostFound> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (ServiceLostFound item : list) {
            SysUser publisher = userMapper.selectById(item.getUserId());
            if (publisher != null) {
                publisher.setPassword(null);
            }
            item.setPublisher(publisher);

            if (item.getStatus() != null && item.getStatus() == 2) {
                SysUser claimer = claimMapper.selectClaimerByLostFoundId(item.getId());
                if (claimer != null) {
                    claimer.setPassword(null);
                }
                item.setClaimer(claimer);
            }
        }
    }

    private void bindForumPost(ServiceLostFound item) {
        ForumSection section = sectionMapper.selectByCode("LOST_FOUND");
        if (section == null) {
            return;
        }
        ForumPost post = new ForumPost();
        post.setUserId(item.getUserId());
        post.setSectionId(section.getId());
        post.setTitle(item.getTitle());
        post.setContent(item.getDescription());
        post.setImages(item.getImages());
        post.setAuditStatus(item.getAuditStatus());
        post.setSourceType(2);
        post.setSourceId(item.getId());
        post.setIsAnonymous(0);
        forumPostMapper.insert(post);
        lostFoundMapper.updatePostId(item.getId(), post.getId());
        item.setPostId(post.getId());
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

    @Override
    public void submitClaim(Long lostFoundId, Long userId, String description, String images) {
        ServiceLostFound item = lostFoundMapper.selectById(lostFoundId);
        if (item == null || item.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "失物招领信息不存在");
        }
        if (item.getType() != 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "仅招领信息可发起认领申请");
        }
        if (item.getStatus() == 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该物品已被认领");
        }
        if (item.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不能认领自己发布的物品");
        }

        // 检查是否已有待审核的申请
        ServiceLostFoundClaim existing = claimMapper.checkDuplicateClaim(lostFoundId, userId);
        if (existing != null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "您已有申请正在处理中，请勿重复提交");
        }

        ServiceLostFoundClaim claim = new ServiceLostFoundClaim();
        claim.setLostFoundId(lostFoundId);
        claim.setUserId(userId);
        claim.setDescription(description);
        claim.setImages(images);
        claimMapper.insert(claim);
    }

    @Override
    public PageResult<Map<String, Object>> getClaimList(Long current, Long size, Integer status) {
        long pageNo = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 10 : size;
        long offset = (pageNo - 1) * pageSize;
        List<Map<String, Object>> records = claimMapper.selectClaimPage(status, offset, pageSize);
        Long total = claimMapper.countClaimPage(status);
        return new PageResult<>(pageNo, pageSize, total == null ? 0L : total, records);
    }

    @Override
    public void auditClaim(Long claimId, Long auditorId, Integer status, String remark) {
        ServiceLostFoundClaim claim = claimMapper.selectById(claimId);
        if (claim == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "申请记录不存在");
        }
        if (claim.getStatus() != 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该申请已处理过");
        }

        claimMapper.auditClaim(claimId, status, remark, auditorId);

        // 如果审核通过，且是招领物品，更新招领物品状态为已完成
        if (status == 1) {
            lostFoundMapper.updateStatus(claim.getLostFoundId(), 2); // 2-已认领
        }

        // 发送通知给申请人
        String content = status == 1 ? "您的认领申请已通过审核，请尽快联系发布者获取物品。" : "您的认领申请未通过，理由：" + (remark != null ? remark : "不符合要求");
            noticeService.sendNotice(claim.getUserId(), 1, "认领申请结果通知", content, claim.getLostFoundId(), 6);
    }
}
