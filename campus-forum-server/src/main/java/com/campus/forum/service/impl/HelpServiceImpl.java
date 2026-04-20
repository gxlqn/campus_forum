package com.campus.forum.service.impl;

import com.campus.forum.common.PageResult;
import com.campus.forum.common.ResultCode;
import com.campus.forum.entity.ServiceHelpRequest;
import com.campus.forum.entity.ServiceHelpCandidate;
import com.campus.forum.entity.SysUser;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.ServiceHelpCandidateMapper;
import com.campus.forum.mapper.ServiceHelpRequestMapper;
import com.campus.forum.mapper.SysUserMapper;
import com.campus.forum.mapper.UserWalletRecordMapper;
import com.campus.forum.service.HelpService;
import com.campus.forum.service.NoticeService;
import com.campus.forum.service.SmartAuditService;
import com.campus.forum.search.SearchSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
public class HelpServiceImpl implements HelpService {

    private static final Logger log = LoggerFactory.getLogger(HelpServiceImpl.class);

    @Autowired
    private ServiceHelpRequestMapper helpMapper;

    @Autowired
    private ServiceHelpCandidateMapper helpCandidateMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private SmartAuditService smartAuditService;

    @Autowired
    private UserWalletRecordMapper userWalletRecordMapper;

    @Autowired
    private SearchSyncService searchSyncService;

    @Override
    public PageResult<ServiceHelpRequest> getHelpList(Long current, Long size, Integer type, String keyword) {
        long pageNo = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 10 : size;
        long offset = (pageNo - 1) * pageSize;
        List<ServiceHelpRequest> records = helpMapper.selectPage(type, keyword, offset, pageSize);
        fillPublisherHelper(records);
        Long total = helpMapper.countPage(type, keyword);
        return new PageResult<>(pageNo, pageSize, total == null ? 0L : total, records);
    }

    @Override
    public ServiceHelpRequest getHelpDetail(Long id) {
        ServiceHelpRequest req = helpMapper.selectById(id);
        if (req == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "互助单不存在");
        }
        helpMapper.increaseViewCount(id);
        req.setViewCount((req.getViewCount() == null ? 0 : req.getViewCount()) + 1);
        fillPublisherHelper(List.of(req));
        return req;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ServiceHelpRequest publishHelp(ServiceHelpRequest request) {
        if (request == null || request.getUserId() == null || !StringUtils.hasText(request.getTitle())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "互助信息不完整");
        }
        if (request.getType() == null) {
            request.setType(1); // 默认普通互助
        }
        if (request.getAuditStatus() == null) {
            request.setAuditStatus(0);
        }
        if (request.getStatus() == null) {
            request.setStatus(1); // 1-待接单
        }

        BigDecimal reward = request.getReward() == null ? BigDecimal.ZERO : request.getReward();
        if (reward.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "悬赏金额必须大于0");
        }

        int freezeChanged = userMapper.updateBalance(request.getUserId(), reward.negate());
        if (freezeChanged == 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "余额不足，无法冻结悬赏金额");
        }
        request.setFundStatus(1);
        request.setFundFreezeTime(java.time.LocalDateTime.now());

        // 先审核，再入库：通过则自动过审，命中违规关键词则直接拦截
        SmartAuditService.AuditResult auditResult = smartAuditService.auditPost(
                request.getUserId(), request.getTitle(), request.getDescription(), null);

        if (auditResult.getAuditStatus() == SmartAuditService.AuditStatus.AUTO_REJECT.getCode()) {
            log.warn("互助内容被拦截(含违规关键词): userId={}, reason={}", request.getUserId(), auditResult.getReason());
            throw new BusinessException(ResultCode.CONTENT_AUDIT_BLOCKED, auditResult.getReason());
        }

        if (!auditResult.isPassed()) {
            log.warn("互助内容需人工审核: userId={}, reason={}", request.getUserId(), auditResult.getReason());
        }

        if (auditResult.getAuditStatus() > 0) {
            request.setAuditStatus(mapAuditStatusToContentStatus(auditResult.getAuditStatus()));
        }

        helpMapper.insert(request);
        insertWalletRecord(request.getUserId(), reward.negate(), 2, request.getId());
        ServiceHelpRequest saved = helpMapper.selectById(request.getId());
        fillPublisherHelper(List.of(saved));
        searchSyncService.syncHelp(saved.getId());
        return saved;
    }

    @Override
    public void acceptHelp(Long id, Long helperId) {
        ServiceHelpRequest req = helpMapper.selectById(id);
        if (req == null || req.getDeleted() != null && req.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "互助单不存在");
        }
        if (req.getUserId() != null && req.getUserId().equals(helperId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "不能接自己发布的互助单");
        }
        if (req.getStatus() != null && req.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该互助单已被接单或已结束");
        }
        if (req.getExpectedTime() != null && !req.getExpectedTime().isAfter(java.time.LocalDateTime.now())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该互助单已超时，无法接单");
        }

        ServiceHelpCandidate exists = helpCandidateMapper.selectByHelpIdAndUserId(id, helperId);
        if (exists != null) {
            throw new BusinessException(ResultCode.REPEAT_OPERATION, "您已在当前抢单轮次中报名，请耐心等待分配");
        }

        SysUser helper = userMapper.selectById(helperId);
        int creditScore = helper == null || helper.getCreditScore() == null ? 0 : helper.getCreditScore();
        int changed = helpCandidateMapper.insertCandidate(id, helperId, creditScore);
        if (changed <= 0) {
            throw new BusinessException(ResultCode.ERROR, "抢单失败，请稍后重试");
        }
        searchSyncService.syncHelp(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long id, Long userId) {
        ServiceHelpRequest req = helpMapper.selectById(id);
        if (req == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "互助单不存在");
        }
        if (req.getStatus() == null || req.getStatus() != 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "仅进行中的订单可确认完成");
        }
        boolean isPublisher = req.getUserId() != null && req.getUserId().equals(userId);
        boolean isHelper = req.getHelperId() != null && req.getHelperId().equals(userId);
        if (!isPublisher && !isHelper) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅发布者和接单人可确认完成");
        }

        int changed;
        if (isPublisher) {
            changed = helpMapper.publisherConfirmComplete(id, userId);
            if (changed == 0 && req.getPublisherConfirmed() != null && req.getPublisherConfirmed() == 1) {
                throw new BusinessException(ResultCode.REPEAT_OPERATION, "您已确认完成，无需重复操作");
            }
        } else {
            changed = helpMapper.helperConfirmComplete(id, userId);
            if (changed == 0 && req.getHelperConfirmed() != null && req.getHelperConfirmed() == 1) {
                throw new BusinessException(ResultCode.REPEAT_OPERATION, "您已确认完成，无需重复操作");
            }
        }
        if (changed == 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "确认失败，请刷新后重试");
        }

        int completed = helpMapper.completeWhenBothConfirmed(id);
        if (completed > 0) {
            settleHelpRewardAndCredit(req);
            noticeService.sendHelpOrderNotice(req.getUserId(), req.getTitle(), id, 2);
        }
        searchSyncService.syncHelp(id);
    }

    @Override
    public PageResult<ServiceHelpRequest> getAdminHelpList(Long current, Long size, Integer type, Integer status, Integer auditStatus, String keyword) {
        long pageNo = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 10 : size;
        long offset = (pageNo - 1) * pageSize;
        List<ServiceHelpRequest> records = helpMapper.selectAdminPage(type, status, auditStatus, keyword, offset, pageSize);
        fillPublisherHelper(records);
        Long total = helpMapper.countAdminPage(type, status, auditStatus, keyword);
        return new PageResult<>(pageNo, pageSize, total == null ? 0L : total, records);
    }

    @Override
    public void auditHelpRequest(Long id, Integer auditStatus) {
        ServiceHelpRequest req = helpMapper.selectById(id);
        if (req == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "互助单不存在");
        }
        int changed = helpMapper.updateAuditStatus(id, auditStatus);
        if (changed == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "审核失败");
        }
        searchSyncService.syncHelp(id);
        if (auditStatus == 1) {
            noticeService.sendHelpOrderNotice(req.getUserId(), req.getTitle(), id, 1);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publisherCancelOrder(Long id, Long userId) {
        ServiceHelpRequest req = helpMapper.selectById(id);
        if (req == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "互助单不存在");
        }
        if (!req.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能取消自己发布的互助单");
        }
        if (req.getStatus() != null && req.getStatus() >= 3) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该互助单已完成或已取消");
        }
        helpMapper.cancelOrder(id);
        refundFrozenAmountIfNeeded(req);
        searchSyncService.syncHelp(id);
    }

    @Override
    public void assignHelperManually(Long requestId, Long helperId, Long userId) {
        ServiceHelpRequest req = helpMapper.selectById(requestId);
        if (req == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "互助单不存在");
        }
        if (req.getStatus() != null && req.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该互助单已被接单或已结束");
        }
        int changed = helpMapper.assignHelper(requestId, helperId);
        if (changed > 0) {
            noticeService.sendHelpOrderNotice(req.getUserId(), req.getTitle(), requestId, 1);
            searchSyncService.syncHelp(requestId);
        }
    }

    @Override
    public void publisherConfirm(Long id, Long userId, Integer isComplaint) {
        ServiceHelpRequest req = helpMapper.selectById(id);
        if (req == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "互助单不存在");
        }
        if (!req.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只有发布者可以确认完成");
        }
        if (isComplaint != null && isComplaint == 1) {
            if (req.getStatus() == null || req.getStatus() != 2) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "仅进行中的订单可发起投诉");
            }
            // 发布者投诉帮助者
            helpMapper.updateFreezeStatus(id, 1, 1);
        } else {
            completeOrder(id, userId);
        }
    }

    @Override
    public void helperAppeal(Long id, Long userId) {
        ServiceHelpRequest req = helpMapper.selectById(id);
        if (req == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "互助单不存在");
        }
        if (req.getHelperId() == null || !req.getHelperId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只有接单者可以申诉");
        }
        if (req.getIsFrozen() == null || req.getIsFrozen() != 1 || req.getComplaintStatus() == null || req.getComplaintStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "当前无可申诉的投诉订单");
        }
        helpMapper.updateFreezeStatus(id, 1, 2);
    }

    @Override
    public PageResult<ServiceHelpRequest> getArbitrationList(Integer pageNo, Integer size) {
        int page = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int pageSize = size == null || size < 1 ? 10 : size;
        long offset = (long) (page - 1) * pageSize;
        // 查询有争议的订单: 已冻结 或 有投诉状态
        List<ServiceHelpRequest> records = helpMapper.selectAdminPage(null, null, null, null, offset, (long) pageSize).stream()
                .filter(r -> r.getIsFrozen() != null && r.getIsFrozen() == 1)
                .toList();
        fillPublisherHelper(records);
        // 注意：仲裁列表简化处理，实际可添加专门的 count 查询
        return new PageResult<>((long) page, (long) pageSize, (long) records.size(), records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resolveArbitration(Long id, Integer winner) {
        ServiceHelpRequest req = helpMapper.selectById(id);
        if (req == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "互助单不存在");
        }
        if (req.getStatus() == null || req.getStatus() != 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "仅进行中的订单可仲裁处理");
        }
        // winner: 1-发布者胜, 2-帮助者胜
        if (winner == 1) {
            helpMapper.updateFreezeStatus(id, 0, 0);
            helpMapper.cancelOrder(id); // 取消订单，发布者退款
            refundFrozenAmountIfNeeded(req);
            searchSyncService.syncHelp(id);
        } else if (winner == 2) {
            helpMapper.updateFreezeStatus(id, 0, 0);
            int completed = helpMapper.completeOrderByArbitration(id); // 完成订单，帮助者获得报酬
            if (completed > 0) {
                settleHelpRewardAndCredit(req);
                noticeService.sendHelpOrderNotice(req.getUserId(), req.getTitle(), id, 2);
            }
            searchSyncService.syncHelp(id);
        } else {
            throw new BusinessException(ResultCode.PARAM_ERROR, "无效的仲裁结果");
        }
    }

    private void settleHelpRewardAndCredit(ServiceHelpRequest req) {
        if (req == null) {
            return;
        }

        Long publisherId = req.getUserId();
        Long helperId = req.getHelperId();
        if (publisherId == null || helperId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单角色信息不完整，无法结算");
        }

        BigDecimal reward = req.getReward() == null ? BigDecimal.ZERO : req.getReward();
        if (reward.compareTo(BigDecimal.ZERO) > 0) {
            int fundMarked = helpMapper.markFundSettled(req.getId());
            if (fundMarked == 0) {
                return;
            }

            // 发布互助单时服务端已冻结扣除悬赏金，完成时仅向接单人发放。
            int credit = userMapper.updateBalance(helperId, reward);
            if (credit == 0) {
                throw new BusinessException(ResultCode.ERROR, "奖励发放失败，请稍后重试");
            }
            insertWalletRecord(helperId, reward, 3, req.getId());
        }

        userMapper.updateCreditScore(helperId, 2);
        userMapper.updateCreditScore(publisherId, 1);
    }

    private void refundFrozenAmountIfNeeded(ServiceHelpRequest req) {
        if (req == null) {
            return;
        }
        BigDecimal reward = req.getReward() == null ? BigDecimal.ZERO : req.getReward();
        if (reward.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        int fundMarked = helpMapper.markFundRefunded(req.getId());
        if (fundMarked == 0) {
            return;
        }

        int credit = userMapper.updateBalance(req.getUserId(), reward);
        if (credit == 0) {
            throw new BusinessException(ResultCode.ERROR, "退款失败，请稍后重试");
        }
        insertWalletRecord(req.getUserId(), reward, 5, req.getId());
    }

    private void insertWalletRecord(Long userId, BigDecimal amount, Integer type, Long relationId) {
        if (userId == null || amount == null || type == null) {
            return;
        }
        com.campus.forum.entity.UserWalletRecord record = new com.campus.forum.entity.UserWalletRecord();
        record.setUserId(userId);
        record.setAmount(amount);
        record.setType(type);
        record.setRelationId(relationId);
        userWalletRecordMapper.insert(record);
    }

    private void fillPublisherHelper(List<ServiceHelpRequest> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (ServiceHelpRequest item : list) {
            if (item.getUserId() != null) {
                SysUser pub = userMapper.selectById(item.getUserId());
                if (pub != null) {
                    pub.setPassword(null);
                }
                item.setPublisher(pub);
            }
            if (item.getHelperId() != null) {
                SysUser helper = userMapper.selectById(item.getHelperId());
                if (helper != null) {
                    helper.setPassword(null);
                }
                item.setHelper(helper);
            }
        }
    }

    private int mapAuditStatusToContentStatus(int auditStatus) {
        switch (auditStatus) {
            case 1: return 1; // 自动通过
            case 2: return 2; // 自动拒绝（理论上这里不会入库）
            case 3: return 0; // AI不确定，待人工审核
            case 4: return 0; // 人工复核
            default: return 0;
        }
    }
}
