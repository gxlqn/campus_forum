package com.campus.forum.im.listener;

import com.campus.forum.im.service.ImPresenceService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;

@Component
public class ImSessionEventListener {

    private static final Logger log = LoggerFactory.getLogger(ImSessionEventListener.class);

    private final ImPresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;

    public ImSessionEventListener(ImPresenceService presenceService, SimpMessagingTemplate messagingTemplate) {
        this.presenceService = presenceService;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void onConnect(SessionConnectEvent event) {
        log.debug("IM SessionConnectEvent received");
        bindPresence(event.getMessage());
    }

    @EventListener
    public void onConnected(SessionConnectedEvent event) {
        log.info("IM WebSocket connected: sessionId={}", event.getMessage() != null ? StompHeaderAccessor.wrap(event.getMessage()).getSessionId() : null);
        bindPresence(event.getMessage());
    }

    private void bindPresence(org.springframework.messaging.Message<?> message) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        Principal principal = accessor.getUser();
        String sessionId = accessor.getSessionId();
        Long userId = parseUserId(principal);
        if (userId != null && sessionId != null) {
            presenceService.onConnected(userId, sessionId);
            broadcastPresence(userId, true);
            log.info("IM presence online: userId={}, sessionId={}", userId, sessionId);
        }
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        log.info("IM WebSocket disconnected: sessionId={}", event.getSessionId());
        Long userId = presenceService.onDisconnected(event.getSessionId());
        if (userId != null) {
            broadcastPresence(userId, false);
            log.info("IM presence offline: userId={}, sessionId={}", userId, event.getSessionId());
        }
    }

    private Long parseUserId(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return null;
        }
        try {
            return Long.parseLong(principal.getName());
        } catch (Exception ignore) {
            return null;
        }
    }

    private void broadcastPresence(Long userId, boolean online) {
        java.util.Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("userId", userId);
        payload.put("online", online);
        messagingTemplate.convertAndSend("/topic/im-presence", payload);
        messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/im-presence", payload);
        messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/im-online", payload);
    }
}
