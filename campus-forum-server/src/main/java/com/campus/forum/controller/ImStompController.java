package com.campus.forum.controller;

import com.campus.forum.dto.im.ImAckRequest;
import com.campus.forum.dto.im.ImSendMessageRequest;
import com.campus.forum.dto.im.ImSyncRequest;
import com.campus.forum.im.service.ImRealtimeService;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class ImStompController {

    private final ImRealtimeService imRealtimeService;
    private final SimpMessagingTemplate messagingTemplate;

    public ImStompController(ImRealtimeService imRealtimeService,
            SimpMessagingTemplate messagingTemplate) {
        this.imRealtimeService = imRealtimeService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/im/send")
    public void send(@Payload ImSendMessageRequest request, Principal principal) {
        Long userId = userIdOf(principal);
        Map<String, Object> result = imRealtimeService.send(userId, request);
        messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/im-send-ack", result);
    }

    @MessageMapping("/im/ack")
    public void ack(@Payload ImAckRequest request, Principal principal) {
        Long userId = userIdOf(principal);
        Map<String, Object> result = imRealtimeService.ack(userId, request);
        messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/im-ack", result);
    }

    @MessageMapping("/im/sync")
    public void sync(@Payload ImSyncRequest request, Principal principal) {
        Long userId = userIdOf(principal);
        Map<String, Object> result = imRealtimeService.sync(userId, request);
        messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/im-sync", result);
    }

    @MessageExceptionHandler
    public void handleException(Throwable throwable, Principal principal) {
        Long userId = userIdOf(principal);
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("ok", false);
        error.put("code", "IM_ERROR");
        error.put("message", throwable.getMessage());
        error.put("time", LocalDateTime.now());
        messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/im-error", error);
    }

    private Long userIdOf(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new IllegalArgumentException("未认证的WebSocket连接");
        }
        try {
            return Long.parseLong(principal.getName());
        } catch (Exception ex) {
            throw new IllegalArgumentException("WebSocket用户信息无效");
        }
    }
}
