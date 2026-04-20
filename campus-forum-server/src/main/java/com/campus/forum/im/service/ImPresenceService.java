package com.campus.forum.im.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ImPresenceService {

    private final Map<Long, Set<String>> userSessionMap = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();

    public void onConnected(Long userId, String sessionId) {
        if (userId == null || sessionId == null) {
            return;
        }
        userSessionMap.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(sessionId);
        sessionUserMap.put(sessionId, userId);
    }

    public void onDisconnected(String sessionId) {
        if (sessionId == null) {
            return;
        }
        Long userId = sessionUserMap.remove(sessionId);
        if (userId == null) {
            return;
        }
        Set<String> sessions = userSessionMap.get(userId);
        if (sessions == null) {
            return;
        }
        sessions.remove(sessionId);
        if (sessions.isEmpty()) {
            userSessionMap.remove(userId);
        }
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
