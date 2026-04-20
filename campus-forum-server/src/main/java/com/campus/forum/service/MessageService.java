package com.campus.forum.service;

import com.campus.forum.dto.message.SendMessageRequest;

import java.util.Map;

public interface MessageService {

    Map<String, Object> getNotifications(Long userId, Long current, Long size, Integer type, Integer isRead);

    Map<String, Object> getNotificationDetail(Long userId, Long notificationId);

    Map<String, Object> getConversations(Long userId, Long current, Long size, String keyword);

    Map<String, Object> getConversationMessages(Long userId, String conversationId, Long current, Long size);

    Map<String, Object> sendMessage(Long senderId, SendMessageRequest request);

    void markNotificationRead(Long userId, Long notificationId);

    void markAllNotificationsRead(Long userId);

    void clearSystemNotifications(Long userId, Boolean onlyRead);

    void markConversationRead(Long userId, String conversationId);

    void sendNotification(Long userId, Long senderId, Integer type, String title, String content,
                          Integer targetType, Long targetId);
}
