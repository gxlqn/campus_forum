package com.campus.forum.im.service.impl;

import com.campus.forum.im.service.ImClusterMessageBroadcaster;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "im.cluster", name = "mode", havingValue = "local", matchIfMissing = true)
public class LocalImClusterMessageBroadcaster implements ImClusterMessageBroadcaster {

    private final ImMessageDispatchService dispatchService;

    public LocalImClusterMessageBroadcaster(ImMessageDispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @Override
    public void broadcastMessage(Long messageId, Long receiverId) {
        dispatchService.dispatchToReceiver(messageId, receiverId, "LOCAL");
    }
}
