package com.campus.forum.service.impl;

import com.campus.forum.common.PageResult;
import com.campus.forum.common.ResultCode;
import com.campus.forum.dto.message.SendMessageRequest;
import com.campus.forum.entity.SysUser;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.MessageMapper;
import com.campus.forum.mapper.SysUserMapper;
import com.campus.forum.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Override
    public Map<String, Object> getNotifications(Long userId, Long current, Long size, Integer type, Integer isRead) {
        long pageNo = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 10 : size;
        long offset = (pageNo - 1) * pageSize;

        List<Map<String, Object>> records = messageMapper.selectNotificationPage(userId, type, isRead, offset, pageSize);
        Long total = messageMapper.countNotificationPage(userId, type, isRead);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("page", new PageResult<>(pageNo, pageSize, safeLong(total), records));
        data.put("unreadCount", safeLong(messageMapper.countUnreadNotifications(userId)));
        return data;
    }

    @Override
    public Map<String, Object> getNotificationDetail(Long userId, Long notificationId) {
        if (userId == null || notificationId == null) {
            throw new BusinessException(ResultCode.PARAM_MISSING);
        }
        Map<String, Object> notification = messageMapper.selectNotificationById(notificationId, userId);
        if (notification == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "通知不存在");
        }
        messageMapper.markNotificationRead(notificationId, userId);
        notification.put("isRead", 1);
        return notification;
    }

    @Override
    public Map<String, Object> getConversations(Long userId, Long current, Long size, String keyword) {
        long pageNo = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 10 : size;
        long offset = (pageNo - 1) * pageSize;

        List<Map<String, Object>> records = messageMapper.selectConversationPage(userId, keyword, offset, pageSize);
        Long total = messageMapper.countConversationPage(userId, keyword);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("page", new PageResult<>(pageNo, pageSize, safeLong(total), records));
        data.put("unreadCount", safeLong(messageMapper.countUnreadConversations(userId)));
        return data;
    }

    @Override
    public Map<String, Object> getConversationMessages(Long userId, String conversationId, Long current, Long size) {
        if (!StringUtils.hasText(conversationId)) {
            throw new BusinessException(ResultCode.PARAM_MISSING);
        }
        Map<String, Object> conversation = messageMapper.selectConversationById(conversationId, userId);
        if (conversation == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会话不存在");
        }

        long pageNo = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 20 : size;
        long offset = (pageNo - 1) * pageSize;
        List<Map<String, Object>> records = messageMapper.selectConversationMessages(conversationId, userId, offset, pageSize);
        Collections.reverse(records);
        Long total = messageMapper.countConversationMessages(conversationId, userId);
        Long targetUserId = parseLong(conversation.get("targetUserId"));
        Map<String, Object> sendPolicy = buildSendPolicy(conversationId, userId, targetUserId);

        messageMapper.markConversationMessagesRead(conversationId, userId);
        messageMapper.clearConversationUnread(conversationId, userId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("conversation", conversation);
        data.put("sendPolicy", sendPolicy);
        data.put("page", new PageResult<>(pageNo, pageSize, safeLong(total), records));
        return data;
    }

    @Override
    public Map<String, Object> sendMessage(Long senderId, SendMessageRequest request) {
        if (senderId == null || request == null || request.getReceiverId() == null || !StringUtils.hasText(request.getContent())) {
            throw new BusinessException(ResultCode.PARAM_MISSING);
        }
        if (senderId.equals(request.getReceiverId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不能给自己发消息");
        }

        SysUser receiver = userMapper.selectById(request.getReceiverId());
        if (receiver == null || (receiver.getDeleted() != null && receiver.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "接收用户不存在");
        }

        String conversationId = buildConversationId(senderId, request.getReceiverId());
        validateInitiatorSendRule(conversationId, senderId, request.getReceiverId());
        Integer contentType = request.getContentType() == null ? 1 : request.getContentType();
        String content = request.getContent().trim();

        messageMapper.insertPrivateMessage(conversationId, senderId, request.getReceiverId(), content, contentType);

        String preview = content.length() > 80 ? content.substring(0, 80) + "..." : content;
        messageMapper.upsertConversation(conversationId, senderId, request.getReceiverId(), preview, 0);
        messageMapper.upsertConversation(conversationId, request.getReceiverId(), senderId, preview, 1);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("conversationId", conversationId);
        data.put("senderId", senderId);
        data.put("receiverId", request.getReceiverId());
        data.put("content", content);
        data.put("contentType", contentType);
        data.put("createTime", LocalDateTime.now());
        data.put("sendPolicy", buildSendPolicy(conversationId, senderId, request.getReceiverId()));
        return data;
    }

    @Override
    public void markNotificationRead(Long userId, Long notificationId) {
        if (userId == null || notificationId == null) {
            throw new BusinessException(ResultCode.PARAM_MISSING);
        }
        int changed = messageMapper.markNotificationRead(notificationId, userId);
        if (changed == 0) {
            Map<String, Object> notification = messageMapper.selectNotificationById(notificationId, userId);
            if (notification == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "通知不存在");
            }
        }
    }

    @Override
    public void markAllNotificationsRead(Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.PARAM_MISSING);
        }
        messageMapper.markUserNotificationsRead(userId);
    }

    @Override
    public void clearSystemNotifications(Long userId, Boolean onlyRead) {
        if (userId == null) {
            throw new BusinessException(ResultCode.PARAM_MISSING);
        }
        messageMapper.deleteSystemNotifications(userId, onlyRead != null && onlyRead);
    }

    @Override
    public void markConversationRead(Long userId, String conversationId) {
        if (userId == null || !StringUtils.hasText(conversationId)) {
            throw new BusinessException(ResultCode.PARAM_MISSING);
        }
        Map<String, Object> conversation = messageMapper.selectConversationById(conversationId, userId);
        if (conversation == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会话不存在");
        }
        messageMapper.markConversationMessagesRead(conversationId, userId);
        messageMapper.clearConversationUnread(conversationId, userId);
    }

    private String buildConversationId(Long userA, Long userB) {
        long min = Math.min(userA, userB);
        long max = Math.max(userA, userB);
        return min + "_" + max;
    }

    private Long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private void validateInitiatorSendRule(String conversationId, Long senderId, Long receiverId) {
        Long firstSenderId = messageMapper.selectFirstMessageSenderId(conversationId);
        if (firstSenderId == null) {
            return;
        }

        // 首条消息发起者在对方回复前只能发送一条消息。
        if (!firstSenderId.equals(senderId)) {
            return;
        }

        Long receiverReplyCount = safeLong(messageMapper.countMessagesBySender(conversationId, receiverId));
        if (receiverReplyCount > 0) {
            return;
        }

        Long senderMessageCount = safeLong(messageMapper.countMessagesBySender(conversationId, senderId));
        if (senderMessageCount >= 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "对方未回复前，你只能发送一条私信");
        }
    }

    private Map<String, Object> buildSendPolicy(String conversationId, Long currentUserId, Long targetUserId) {
        Map<String, Object> policy = new LinkedHashMap<>();
        if (currentUserId == null || targetUserId == null || !StringUtils.hasText(conversationId)) {
            policy.put("restricted", false);
            policy.put("reason", "");
            return policy;
        }

        Long firstSenderId = messageMapper.selectFirstMessageSenderId(conversationId);
        if (firstSenderId == null || !firstSenderId.equals(currentUserId)) {
            policy.put("restricted", false);
            policy.put("reason", "");
            return policy;
        }

        Long receiverReplyCount = safeLong(messageMapper.countMessagesBySender(conversationId, targetUserId));
        Long senderMessageCount = safeLong(messageMapper.countMessagesBySender(conversationId, currentUserId));
        boolean restricted = receiverReplyCount == 0 && senderMessageCount >= 1;
        policy.put("restricted", restricted);
        if (restricted) {
            policy.put("reason", "你已发出首条私信，需等待对方回复后继续发送");
        } else if (receiverReplyCount > 0) {
            policy.put("reason", "对方已回复，当前会话已解除发送限制");
        } else {
            policy.put("reason", "");
        }
        return policy;
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception ignore) {
            return null;
        }
    }

    @Override
    public void sendNotification(Long userId, Long senderId, Integer type, String title, String content,
                                  Integer targetType, Long targetId) {
        if (userId == null || type == null) {
            return;
        }
        messageMapper.insertNotification(userId, senderId, type,
                StringUtils.hasText(title) ? title : "",
                StringUtils.hasText(content) ? content : "",
                targetType, targetId);
    }
}
