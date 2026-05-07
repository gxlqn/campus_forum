package com.campus.forum.im.config;

import com.campus.forum.im.security.ImHandshakeHandler;
import com.campus.forum.im.security.JwtHandshakeInterceptor;
import com.campus.forum.im.security.StompAuthChannelInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class ImWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final ImProperties imProperties;
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    public ImWebSocketConfig(ImProperties imProperties,
            JwtHandshakeInterceptor jwtHandshakeInterceptor,
            StompAuthChannelInterceptor stompAuthChannelInterceptor) {
        this.imProperties = imProperties;
        this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
        this.stompAuthChannelInterceptor = stompAuthChannelInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(imProperties.getEndpoint())
                .addInterceptors(jwtHandshakeInterceptor)
                .setHandshakeHandler(new ImHandshakeHandler())
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue")
                .setTaskScheduler(imHeartbeatTaskScheduler())
                .setHeartbeatValue(new long[] { imProperties.getHeartbeatServerMs(), imProperties.getHeartbeatClientMs() });
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthChannelInterceptor);
    }

    @Bean
    public ThreadPoolTaskScheduler imHeartbeatTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("im-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }
}
