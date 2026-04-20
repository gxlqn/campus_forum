package com.campus.forum.im.security;

import com.campus.forum.utils.JwtUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;

    public StompAuthChannelInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (!StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        Long userId = readUserIdFromSession(accessor);
        if (userId == null) {
            userId = readUserIdFromNativeHeader(accessor);
        }
        if (userId == null) {
            throw new IllegalArgumentException("WebSocket鉴权失败");
        }

        accessor.setUser(new StompPrincipal(userId));
        return message;
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
