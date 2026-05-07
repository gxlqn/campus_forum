package com.campus.forum.im.security;

import com.campus.forum.utils.JwtUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(StompAuthChannelInterceptor.class);

    private final JwtUtils jwtUtils;

    public StompAuthChannelInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            return handleConnect(message, accessor);
        }

        if (accessor.getUser() == null) {
            Principal user = resolvePrincipalFromSession(accessor);
            if (user != null) {
                accessor.setUser(user);
                return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
            }
        }

        return message;
    }

    private Message<?> handleConnect(Message<?> message, StompHeaderAccessor accessor) {
        Long userId = readUserIdFromSession(accessor);
        if (userId == null) {
            userId = readUserIdFromNativeHeader(accessor);
        }
        if (userId == null) {
            log.warn("IM STOMP CONNECT rejected: auth failed, sessionId={}", accessor.getSessionId());
            throw new IllegalArgumentException("WebSocket鉴权失败");
        }

        StompPrincipal principal = new StompPrincipal(userId);
        accessor.setUser(principal);

        Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
        if (sessionAttrs != null) {
            sessionAttrs.put(JwtHandshakeInterceptor.ATTR_USER_ID, userId);
        }

        log.info("IM STOMP CONNECT accepted: userId={}, sessionId={}", userId, accessor.getSessionId());
        return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
    }

    private Principal resolvePrincipalFromSession(StompHeaderAccessor accessor) {
        Map<String, Object> attrs = accessor.getSessionAttributes();
        if (attrs == null) {
            return null;
        }
        Object value = attrs.get(JwtHandshakeInterceptor.ATTR_USER_ID);
        if (value instanceof Number number) {
            return new StompPrincipal(number.longValue());
        }
        if (value instanceof String str && StringUtils.hasText(str)) {
            try {
                return new StompPrincipal(Long.parseLong(str));
            } catch (Exception ignore) {
                return null;
            }
        }
        if (value instanceof StompPrincipal principal) {
            return principal;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Long readUserIdFromSession(StompHeaderAccessor accessor) {
        Map<String, Object> attrs = accessor.getSessionAttributes();
        if (attrs == null) {
            return null;
        }
        Object userId = attrs.get(JwtHandshakeInterceptor.ATTR_USER_ID);
        if (userId instanceof Number number) {
            return number.longValue();
        }
        if (userId instanceof String str && StringUtils.hasText(str)) {
            try {
                return Long.parseLong(str);
            } catch (Exception ignore) {
                return null;
            }
        }
        return null;
    }

    private Long readUserIdFromNativeHeader(StompHeaderAccessor accessor) {
        String token = readToken(accessor.getNativeHeader("Authorization"));
        if (!StringUtils.hasText(token)) {
            token = readToken(accessor.getNativeHeader("token"));
        }
        if (!StringUtils.hasText(token) || !jwtUtils.validateToken(token)) {
            return null;
        }
        return jwtUtils.getUserIdFromToken(token);
    }

    private String readToken(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        String raw = values.get(0);
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        if (raw.startsWith("Bearer ")) {
            return raw.substring("Bearer ".length());
        }
        return raw;
    }
}
