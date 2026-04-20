package com.campus.forum.im.service.impl;

import com.campus.forum.im.config.ImProperties;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ImRateLimitService {

    private final ImProperties imProperties;
    private final Map<Long, Deque<Long>> userSendWindow = new ConcurrentHashMap<>();

    public ImRateLimitService(ImProperties imProperties) {
        this.imProperties = imProperties;
    }

    public boolean allow(Long userId) {
        if (userId == null) {
            return false;
        }
        int limit = imProperties.getRateLimitPerMinute() == null ? 120 : imProperties.getRateLimitPerMinute();
        long now = System.currentTimeMillis();
        long windowStart = now - 60_000L;
        Deque<Long> deque = userSendWindow.computeIfAbsent(userId, key -> new ArrayDeque<>());
        synchronized (deque) {
            while (!deque.isEmpty() && deque.peekFirst() < windowStart) {
                deque.pollFirst();
            }
            if (deque.size() >= limit) {
                return false;
            }
            deque.addLast(now);
            return true;
        }
    }
}
