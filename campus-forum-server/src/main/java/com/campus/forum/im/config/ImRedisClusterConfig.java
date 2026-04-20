package com.campus.forum.im.config;

import com.campus.forum.im.service.impl.RedisImClusterSubscriber;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@ConditionalOnProperty(prefix = "im.cluster", name = "mode", havingValue = "redis")
public class ImRedisClusterConfig {

    @Bean
    public RedisMessageListenerContainer imRedisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RedisImClusterSubscriber subscriber,
            ImProperties properties) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(subscriber, new PatternTopic(properties.getCluster().getChannel()));
        return container;
    }
}
