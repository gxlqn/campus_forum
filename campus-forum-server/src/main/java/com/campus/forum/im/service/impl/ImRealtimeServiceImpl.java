package com.campus.forum.im.service.impl;

import com.campus.forum.dto.im.ImAckRequest;
import com.campus.forum.dto.im.ImSendMessageRequest;
import com.campus.forum.dto.im.ImSyncRequest;
import com.campus.forum.dto.message.SendMessageRequest;
import com.campus.forum.im.config.ImProperties;
import com.campus.forum.im.service.ImClusterMessageBroadcaster;
import com.campus.forum.im.service.ImRealtimeService;
import com.campus.forum.mapper.ImDeliveryTaskMapper;
import com.campus.forum.mapper.MessageMapper;
import com.campus.forum.service.MessageService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImRealtimeServiceImpl implements ImRealtimeService {

    private final MessageService messageService;
    private final MessageMapper messageMapper;
    private final ImDeliveryTaskMapper taskMapper;
    private final ImClusterMessageBroadcaster broadcaster;
    private final ImRateLimitService rateLimitService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ImProperties imProperties;

    public ImRealtimeServiceImpl(MessageService messageService,
            MessageMapper messageMapper,
            ImDeliveryTaskMapper taskMapper,
            ImClusterMessageBroadcaster broadcaster,
            ImRateLimitService rateLimitService,
            SimpMessagingTemplate messagingTemplate,
            ImProperties imProperties) {
        this.messageService = messageService;
        this.messageMapper = messageMapper;
        this.taskMapper = taskMapper;
        this.broadcaster = broadcaster;
        this.rateLimitService = rateLimitService;
        this.messagingTemplate = messagingTemplate;
        this.imProperties = imProperties;
    }

    @Override
    public Map<String, Object> send(Long senderId, ImSendMessageRequest request) {
        if (senderId == null || request == null || request.getToUserId() == null || !StringUtils.hasText(request.getContent())) {
            return error("PARAM_MISSING", "参数缺失");
        }
        if (!rateLimitService.allow(senderId)) {
            return error("RATE_LIMIT", "发送过于频繁，请稍后再试");
        }

        SendMessageRequest sendMessageRequest = new SendMessageRequest();
        sendMessageRequest.setReceiverId(request.getToUserId());
        sendMessageRequest.setContent(request.getContent());
        sendMessageRequest.setContentType(request.getContentType());
        sendMessageRequest.setClientMessageId(request.getClientMessageId());

        Map<String, Object> sent = messageService.sendMessage(senderId, sendMessageRequest);
        Long messageId = toLong(sent.get("messageId"));
        Long receiverId = toLong(sent.get("receiverId"));
        String conversationId = sent.get("conversationId") == null ? null : String.valueOf(sent.get("conversationId"));
        String clientMessageId = sent.get("clientMessageId") == null ? null : String.valueOf(sent.get("clientMessageId"));

        if (messageId != null && receiverId != null) {
            LocalDateTime nextRetry = LocalDateTime.now().plusNanos((long) imProperties.getAckTimeoutMs() * 1_000_000L);
            taskMapper.upsertPendingTask(messageId, conversationId, senderId, receiverId, clientMessageId, nextRetry);
            broadcaster.broadcastMessage(messageId, receiverId);
        }

        Map<String, Object> ack = new LinkedHashMap<>();
        ack.put("ok", true);
        ack.put("type", "SEND_ACCEPTED");
        ack.put("data", sent);
        return ack;
    }

    @Override
    public Map<String, Object> ack(Long userId, ImAckRequest request) {
        if (userId == null || request == null || request.getMessageId() == null) {
            return error("PARAM_MISSING", "ACK参数缺失");
        }
        Map<String, Object> message = messageMapper.selectMessageSimpleById(request.getMessageId());
        if (message == null) {
            return error("NOT_FOUND", "消息不存在");
        }

        Long receiverId = toLong(message.get("receiverId"));
        if (!userId.equals(receiverId)) {
            return error("FORBIDDEN", "无权ACK该消息");
        }

        String receiptType = normalizeReceiptType(request.getReceiptType());
        if ("READ".equals(receiptType)) {
            messageMapper.markSingleMessageRead(request.getMessageId(), userId);
            String conversationId = String.valueOf(message.get("conversationId"));
            messageMapper.decreaseConversationUnread(conversationId, userId);
        }

        String clientMessageId = request.getClientMessageId();
        if (!StringUtils.hasText(clientMessageId) && message.get("clientMessageId") != null) {
            clientMessageId = String.valueOf(message.get("clientMessageId"));
        }

        messageMapper.insertReadReceipt(request.getMessageId(), userId, receiptType, clientMessageId);
        taskMapper.markAcked(request.getMessageId());

        Long senderId = toLong(message.get("senderId"));
        if (senderId != null) {
            Map<String, Object> notify = new LinkedHashMap<>();
            notify.put("messageId", request.getMessageId());
            notify.put("receiptType", receiptType);
            notify.put("fromUserId", userId);
            notify.put("time", LocalDateTime.now());
            messagingTemplate.convertAndSendToUser(String.valueOf(senderId), "/queue/im-delivery", notify);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ok", true);
        result.put("type", "ACK_ACCEPTED");
        result.put("messageId", request.getMessageId());
        result.put("receiptType", receiptType);
        return result;
    }

    @Override
    public Map<String, Object> sync(Long userId, ImSyncRequest request) {
        if (userId == null || request == null || !StringUtils.hasText(request.getConversationId())) {
            return error("PARAM_MISSING", "同步参数缺失");
        }

        Map<String, Object> conversation = messageMapper.selectConversationById(request.getConversationId(), userId);
        if (conversation == null) {
            return error("NOT_FOUND", "会话不存在");
        }

        int requestSize = request.getSize() == null ? imProperties.getSyncBatchSize() : request.getSize();
        int size = Math.max(1, Math.min(requestSize, 200));
        Long cursor = request.getCursorMessageId() == null ? 0L : Math.max(request.getCursorMessageId(), 0L);
        List<Map<String, Object>> messages = messageMapper.selectConversationMessagesAfterCursor(
                request.getConversationId(), userId, cursor, size);
        if (messages == null) {
            messages = Collections.emptyList();
        }

        Long nextCursor = cursor;
        if (!messages.isEmpty()) {
            Object lastId = messages.get(messages.size() - 1).get("id");
            Long parsed = toLong(lastId);
            if (parsed != null) {
                nextCursor = parsed;
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ok", true);
        result.put("type", "SYNC_RESULT");
        result.put("conversationId", request.getConversationId());
        result.put("cursor", cursor);
        result.put("nextCursor", nextCursor);
        result.put("hasMore", messages.size() >= size);
        result.put("records", messages);
        return result;
    }

    private Map<String, Object> error(String code, String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ok", false);
        result.put("code", code);
        result.put("message", message);
        return result;
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception ignore) {
            return null;
        }
    }

    private String normalizeReceiptType(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "DELIVERED";
        }
        String value = raw.trim().toUpperCase();
        if ("READ".equals(value)) {
            return "READ";
        }
        return "DELIVERED";
    }
}
