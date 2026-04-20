package com.campus.forum.im.service;

import com.campus.forum.dto.im.ImAckRequest;
import com.campus.forum.dto.im.ImSendMessageRequest;
import com.campus.forum.dto.im.ImSyncRequest;

import java.util.Map;

public interface ImRealtimeService {

    Map<String, Object> send(Long senderId, ImSendMessageRequest request);

    Map<String, Object> ack(Long userId, ImAckRequest request);

    Map<String, Object> sync(Long userId, ImSyncRequest request);
}
