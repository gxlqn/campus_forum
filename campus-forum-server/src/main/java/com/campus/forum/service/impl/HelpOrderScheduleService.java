package com.campus.forum.service.impl;

import com.campus.forum.entity.ServiceHelpCandidate;
import com.campus.forum.entity.ServiceHelpRequest;
import com.campus.forum.mapper.ServiceHelpCandidateMapper;
import com.campus.forum.mapper.ServiceHelpRequestMapper;
import com.campus.forum.mapper.SysUserMapper;
import com.campus.forum.mapper.UserWalletRecordMapper;
import com.campus.forum.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class HelpOrderScheduleService {

    @Autowired
    private ServiceHelpRequestMapper helpRequestMapper;

    @Autowired
    private ServiceHelpCandidateMapper helpCandidateMapper;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private UserWalletRecordMapper userWalletRecordMapper;

    @Scheduled(fixedDelay = 30000)
    @Transactional(rollbackFor = Exception.class)
    public void processHelpOrderAllocateWindow() {
        int timeoutChanged = helpRequestMapper.timeoutUnassignedRequests();
        if (timeoutChanged > 0) {
            refundTimeoutFundsWithRecords();
        }
        helpRequestMapper.initAllocateWindows();

        List<ServiceHelpRequest> dueRequests = helpRequestMapper.selectDueAllocateRequests();
        if (dueRequests == null || dueRequests.isEmpty()) {
            return;
        }

        for (ServiceHelpRequest request : dueRequests) {
            ServiceHelpCandidate best = helpCandidateMapper.selectBestCandidate(request.getId());
            if (best != null) {
                int changed = helpRequestMapper.assignHelper(request.getId(), best.getUserId());
                if (changed > 0) {
                    helpCandidateMapper.markSelected(best.getId());
                    noticeService.sendHelpOrderNotice(request.getUserId(), request.getTitle(), request.getId(), 1);
                }
                continue;
            }

            LocalDateTime nextDeadline = LocalDateTime.now().plusMinutes(3);
            if (request.getExpectedTime() != null && !nextDeadline.isBefore(request.getExpectedTime())) {
                helpRequestMapper.timeoutUnassignedRequests();
                refundTimeoutFundsWithRecords();
            } else {
                helpRequestMapper.extendAllocateWindow(request.getId(), nextDeadline);
            }
        }
    }

    private void refundTimeoutFundsWithRecords() {
        List<ServiceHelpRequest> refundable = helpRequestMapper.selectTimeoutUnrefundedRequests();
        if (refundable == null || refundable.isEmpty()) {
            return;
        }

        for (ServiceHelpRequest request : refundable) {
            if (request.getUserId() == null || request.getReward() == null || request.getReward().signum() <= 0) {
                continue;
            }
            int marked = helpRequestMapper.markFundRefunded(request.getId());
            if (marked == 0) {
                continue;
            }
            int credited = userMapper.updateBalance(request.getUserId(), request.getReward());
            if (credited == 0) {
                throw new IllegalStateException("超时退款入账失败: helpId=" + request.getId());
            }
            com.campus.forum.entity.UserWalletRecord record = new com.campus.forum.entity.UserWalletRecord();
            record.setUserId(request.getUserId());
            record.setAmount(request.getReward());
            record.setType(5);
            record.setRelationId(request.getId());
            userWalletRecordMapper.insert(record);
        }
    }
}
