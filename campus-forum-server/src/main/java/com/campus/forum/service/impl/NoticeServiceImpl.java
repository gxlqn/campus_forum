package com.campus.forum.service.impl;

import com.campus.forum.common.PageResult;
import com.campus.forum.entity.SysNotice;
import com.campus.forum.mapper.SysNoticeMapper;
import com.campus.forum.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private SysNoticeMapper noticeMapper;

    @Override
    public PageResult<SysNotice> getNoticeList(Long userId, Long current, Long size) {
        long pageNo = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 10 : size;
        long offset = (pageNo - 1) * pageSize;
        List<SysNotice> records = noticeMapper.selectByUserId(userId, offset, pageSize);
        Long total = noticeMapper.countByUserId(userId);
        return new PageResult<>(pageNo, pageSize, total == null ? 0L : total, records);
    }

    @Override
    public Long getUnreadCount(Long userId) {
        Long count = noticeMapper.countUnreadByUserId(userId);
        return count == null ? 0L : count;
    }

    @Override
    public void markAsRead(Long id, Long userId) {
        noticeMapper.markAsRead(id, userId);
    }

    @Override
    public void markAllAsRead(Long userId) {
        noticeMapper.markAllAsRead(userId);
    }

    @Override
    public void sendNotice(Long userId, Integer type, String title, String content, Long targetId, Integer targetType) {
        SysNotice notice = new SysNotice();
        notice.setUserId(userId);
        notice.setSenderId(null); // 系统通知，senderId为null
        notice.setType(type);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setTargetType(targetType);
        notice.setTargetId(targetId);
        noticeMapper.insert(notice);
    }

    @Override
    public void sendServiceAuditNotice(Long userId, String serviceType, String title, Integer auditStatus, Long targetId, Integer targetType) {
        String content = auditStatus == 1 ? 
            "您发布的" + serviceType + "《" + title + "》已审核通过，现在可以在平台上看到了。" :
            "您发布的" + serviceType + "《" + title + "》未通过审核，请检查内容是否符合平台规范。";
        sendNotice(userId, 6, serviceType + "审核通知", content, targetId, targetType);
    }

    @Override
    public void sendActivitySignupNotice(Long userId, String activityTitle, Long activityId) {
        String content = "您已成功报名活动《" + activityTitle + "》，请按时参加。";
        sendNotice(userId, 7, "活动报名通知", content, activityId, 4); // 7-活动通知, 4-活动
    }

    @Override
    public void sendHelpOrderNotice(Long userId, String helpTitle, Long helpId, Integer type) {
        String title = type == 1 ? "互助单被接单通知" : "互助单完成通知";
        String content = type == 1 ? 
            "您发布的互助单《" + helpTitle + "》已被接单，请及时联系对方。" :
            "您发布的互助单《" + helpTitle + "》已完成，感谢您的使用。";
        sendNotice(userId, 8, title, content, helpId, 5); // 8-互助通知, 5-互助
    }
}