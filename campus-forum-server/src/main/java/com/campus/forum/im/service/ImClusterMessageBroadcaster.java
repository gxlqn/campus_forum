package com.campus.forum.im.service;

public interface ImClusterMessageBroadcaster {

    void broadcastMessage(Long messageId, Long receiverId);
}
