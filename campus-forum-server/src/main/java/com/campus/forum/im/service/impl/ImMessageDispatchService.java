package com.campus.forum.im.service.impl;

import com.campus.forum.im.service.ImPresenceService;
import com.campus.forum.mapper.MessageMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ImMessageDispatchService {

    private final MessageMapper messageMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ImPresenceService presenceService;

    public ImMessageDispatchService(MessageMapper messageMapper,
            SimpMessagingTemplate messagingTemplate,
            ImPresenceService presenceService) {
        this.messageMapper = messageMapper;
        this.messagingTemplate = messagingTemplate;
        this.presenceService = presenceService;
    }

    public boolean dispatchToReceiver(Long messageId, Long receiverId, String reason) {
        if (messageId == null || receiverId == null) {
            return false;
        }
        if (!presenceService.isOnline(receiverId)) {
            return false;
        }

        Map<String, Object> message = messageMapper.selectRealtimeMessageById(messageId);
        if (message == null) {
            return false;
        }

        Map<String, Object> payload = new LinkedHashMap<>(message);
        payload.put("dispatchReason", reason);
        payload.put("serverTime", LocalDateTime.now());
        messagingTemplate.convertAndSendToUser(String.valueOf(receiverId), "/queue/im-message", payload);
        return true;
    }
}
