package com.campus.forum.im.security;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class ImHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        Object userId = attributes.get(JwtHandshakeInterceptor.ATTR_USER_ID);
        if (userId instanceof Number number) {
            return new StompPrincipal(number.longValue());
        }
        if (userId instanceof String str) {
            try {
                return new StompPrincipal(Long.parseLong(str));
            } catch (Exception ignore) {
                // fall through
            }
        }
        return super.determineUser(request, wsHandler, attributes);
    }
}
