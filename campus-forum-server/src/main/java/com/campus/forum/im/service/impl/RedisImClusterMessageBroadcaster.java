package com.campus.forum.im.service.impl;

import com.campus.forum.im.config.ImProperties;
import com.campus.forum.im.service.ImClusterMessageBroadcaster;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "im.cluster", name = "mode", havingValue = "redis")
public class RedisImClusterMessageBroadcaster implements ImClusterMessageBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(RedisImClusterMessageBroadcaster.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final ImProperties imProperties;
    private final ImMessageDispatchService dispatchService;

    public RedisImClusterMessageBroadcaster(StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            ImProperties imProperties,
            ImMessageDispatchService dispatchService) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.imProperties = imProperties;
        this.dispatchService = dispatchService;
    }

    @Override
    public void broadcastMessage(Long messageId, Long receiverId) {
        if (messageId == null || receiverId == null) {
            return;
        }
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("nodeId", imProperties.getCluster().getNodeId());
            payload.put("messageId", messageId);
            payload.put("receiverId", receiverId);
            String json = objectMapper.writeValueAsString(payload);
            redisTemplate.convertAndSend(imProperties.getCluster().getChannel(), json);
        } catch (Exception ex) {
            log.warn("Redis IM broadcast failed, fallback local dispatch. messageId={}, reason={}",
                    messageId, ex.getMessage());
            dispatchService.dispatchToReceiver(messageId, receiverId, "REDIS_FALLBACK_LOCAL");
        }
    }
}
