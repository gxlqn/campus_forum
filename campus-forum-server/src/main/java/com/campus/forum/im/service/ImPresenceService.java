package com.campus.forum.im.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ImPresenceService {

    private final com.campus.forum.im.service.impl.ImMetricsService imMetricsService;

    private final Map<Long, Set<String>> userSessionMap = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();

    public ImPresenceService(com.campus.forum.im.service.impl.ImMetricsService imMetricsService) {
        this.imMetricsService = imMetricsService;
        this.imMetricsService.registerOnlineUsersGauge(userSessionMap::size);
    }

    public void onConnected(Long userId, String sessionId) {
        if (userId == null || sessionId == null) {
            return;
        }
        userSessionMap.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(sessionId);
        sessionUserMap.put(sessionId, userId);
        imMetricsService.increment("im.connection.events", "type", "connect");
    }

    public Long onDisconnected(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        Long userId = sessionUserMap.remove(sessionId);
        if (userId == null) {
            return null;
        }
        Set<String> sessions = userSessionMap.get(userId);
        if (sessions == null) {
            return null;
        }
        sessions.remove(sessionId);
        if (sessions.isEmpty()) {
            userSessionMap.remove(userId);
            imMetricsService.increment("im.connection.events", "type", "disconnect");
            return userId;
        }
        return null;
    }

    public boolean isOnline(Long userId) {
        if (userId == null) {
            return false;
        }
        Set<String> sessions = userSessionMap.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    public int onlineUsers() {
        return userSessionMap.size();
    }
}
