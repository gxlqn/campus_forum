package com.campus.forum.im.service.impl;

import com.campus.forum.im.service.ImPresenceService;
import com.campus.forum.mapper.MessageMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ImMessageDispatchService {

    private static final Logger log = LoggerFactory.getLogger(ImMessageDispatchService.class);

    private final MessageMapper messageMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ImPresenceService presenceService;
    private final ImMetricsService imMetricsService;

    public ImMessageDispatchService(MessageMapper messageMapper,
            SimpMessagingTemplate messagingTemplate,
            ImPresenceService presenceService,
            ImMetricsService imMetricsService) {
        this.messageMapper = messageMapper;
        this.messagingTemplate = messagingTemplate;
        this.presenceService = presenceService;
        this.imMetricsService = imMetricsService;
    }

    public boolean dispatchToReceiver(Long messageId, Long receiverId, String reason) {
        imMetricsService.incrementWithReason("im.dispatch.attempt.total", reason);
        if (messageId == null || receiverId == null) {
            imMetricsService.incrementWithReason("im.dispatch.failed.total", "invalid_param");
            return false;
        }

        // 尝试性投递：在线表可能因连接事件时序问题短暂不一致，不能据此直接跳过推送。
        boolean online = presenceService.isOnline(receiverId);
        if (!online) {
            imMetricsService.incrementWithReason("im.dispatch.skipped.total", "presence_offline_maybe");
        }

        Map<String, Object> message = messageMapper.selectRealtimeMessageById(messageId);
        if (message == null) {
            imMetricsService.incrementWithReason("im.dispatch.failed.total", "message_not_found");
            return false;
        }

        Map<String, Object> payload = new LinkedHashMap<>(message);
        payload.put("dispatchReason", reason);
        payload.put("serverTime", LocalDateTime.now());
        messagingTemplate.convertAndSendToUser(String.valueOf(receiverId), "/queue/im-message", payload);
        log.info("IM dispatch SENT to user: receiverId={}, messageId={}, reason={}", receiverId, messageId, reason);
        imMetricsService.incrementWithReason("im.dispatch.success.total", reason);
        return true;
    }
}
