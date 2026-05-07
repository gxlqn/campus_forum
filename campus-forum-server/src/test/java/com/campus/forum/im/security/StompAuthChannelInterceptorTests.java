package com.campus.forum.im.security;

import com.campus.forum.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StompAuthChannelInterceptorTests {

    @Test
    void preSendShouldAttachPrincipalForConnectFrame() {
        JwtUtils jwtUtils = mock(JwtUtils.class);
        when(jwtUtils.validateToken("token-123")).thenReturn(true);
        when(jwtUtils.getUserIdFromToken("token-123")).thenReturn(42L);

        StompAuthChannelInterceptor interceptor = new StompAuthChannelInterceptor(jwtUtils);
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer token-123");
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, mock(org.springframework.messaging.MessageChannel.class));

        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertNotNull(resultAccessor.getUser());
        assertEquals("42", resultAccessor.getUser().getName());
    }

    @Test
    void preSendShouldAttachPrincipalForSendFrameFromSession() {
        JwtUtils jwtUtils = mock(JwtUtils.class);
        StompAuthChannelInterceptor interceptor = new StompAuthChannelInterceptor(jwtUtils);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setDestination("/app/im/sync");
        accessor.setSessionAttributes(new HashMap<>());
        accessor.getSessionAttributes().put(JwtHandshakeInterceptor.ATTR_USER_ID, 99L);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, mock(org.springframework.messaging.MessageChannel.class));

        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertNotNull(resultAccessor.getUser());
        assertEquals("99", resultAccessor.getUser().getName());
    }
}
