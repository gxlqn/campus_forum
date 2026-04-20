package com.campus.forum.service;

import com.campus.forum.common.PageResult;
import com.campus.forum.entity.SysNotice;

public interface NoticeService {

    PageResult<SysNotice> getNoticeList(Long userId, Long current, Long size);

    Long getUnreadCount(Long userId);

    void markAsRead(Long id, Long userId);

    void markAllAsRead(Long userId);

    void sendNotice(Long userId, Integer type, String title, String content, Long targetId, Integer targetType);

    void sendServiceAuditNotice(Long userId, String serviceType, String title, Integer auditStatus, Long targetId, Integer targetType);

    void sendActivitySignupNotice(Long userId, String activityTitle, Long activityId);

    void sendHelpOrderNotice(Long userId, String helpTitle, Long helpId, Integer type);
}