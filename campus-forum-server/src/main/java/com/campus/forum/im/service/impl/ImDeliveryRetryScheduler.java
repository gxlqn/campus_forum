package com.campus.forum.im.service.impl;

import com.campus.forum.im.config.ImProperties;
import com.campus.forum.im.service.ImClusterMessageBroadcaster;
import com.campus.forum.mapper.ImDeliveryTaskMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class ImDeliveryRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(ImDeliveryRetryScheduler.class);

    private final ImDeliveryTaskMapper taskMapper;
    private final ImClusterMessageBroadcaster broadcaster;
    private final ImProperties imProperties;

    public ImDeliveryRetryScheduler(ImDeliveryTaskMapper taskMapper,
            ImClusterMessageBroadcaster broadcaster,
            ImProperties imProperties) {
        this.taskMapper = taskMapper;
        this.broadcaster = broadcaster;
        this.imProperties = imProperties;
    }

    @Scheduled(fixedDelayString = "${im.retry-scan-interval-ms:5000}")
    public void scanAndRetry() {
        List<Map<String, Object>> tasks = taskMapper.selectDuePendingTasks(LocalDateTime.now(), 200);
        if (tasks == null || tasks.isEmpty()) {
            return;
        }

        for (Map<String, Object> task : tasks) {
            Long taskId = toLong(task.get("id"));
            Long messageId = toLong(task.get("messageId"));
            Long receiverId = toLong(task.get("receiverId"));
            Integer retryCount = toInt(task.get("retryCount"));
            int maxRetry = imProperties.getMaxRetryAttempts() == null ? 6 : imProperties.getMaxRetryAttempts();

            if (taskId == null || messageId == null || receiverId == null) {
                continue;
            }
            if (retryCount != null && retryCount >= maxRetry) {
                taskMapper.markGiveUp(taskId, "ACK_TIMEOUT_MAX_RETRY_REACHED");
                continue;
            }

            broadcaster.broadcastMessage(messageId, receiverId);
            int safeRetry = retryCount == null ? 0 : Math.max(retryCount, 0);
            int exponent = Math.min(safeRetry, 4);
            long backoff = (long) imProperties.getAckTimeoutMs() * (1L << exponent);
            LocalDateTime nextRetry = LocalDateTime.now().plusNanos(backoff * 1_000_000L);
            taskMapper.increaseRetry(taskId, nextRetry, "ACK_TIMEOUT_RETRY");
            log.debug("IM retry dispatched. taskId={}, messageId={}, retryCount={}", taskId, messageId, safeRetry + 1);
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

    private Integer toInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ignore) {
            return null;
        }
    }
}
