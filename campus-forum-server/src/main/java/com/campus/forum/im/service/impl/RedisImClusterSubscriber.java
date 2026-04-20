package com.campus.forum.im.service.impl;

import com.campus.forum.im.config.ImProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "im.cluster", name = "mode", havingValue = "redis")
public class RedisImClusterSubscriber implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(RedisImClusterSubscriber.class);

    private final ObjectMapper objectMapper;
    private final ImProperties imProperties;
    private final ImMessageDispatchService dispatchService;

    public RedisImClusterSubscriber(ObjectMapper objectMapper,
            ImProperties imProperties,
            ImMessageDispatchService dispatchService) {
        this.objectMapper = objectMapper;
        this.imProperties = imProperties;
        this.dispatchService = dispatchService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody());
            Map<String, Object> payload = objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {
            });
            String nodeId = String.valueOf(payload.get("nodeId"));
            if (imProperties.getCluster().getNodeId().equals(nodeId)) {
                return;
            }
            Long messageId = toLong(payload.get("messageId"));
            Long receiverId = toLong(payload.get("receiverId"));
            dispatchService.dispatchToReceiver(messageId, receiverId, "REDIS_SUB");
        } catch (Exception ex) {
            log.warn("Redis IM subscribe dispatch failed: {}", ex.getMessage());
        }
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception ignore) {
            return null;
        }
    }
}
