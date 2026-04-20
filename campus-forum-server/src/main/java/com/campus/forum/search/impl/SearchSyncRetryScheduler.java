package com.campus.forum.search.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.campus.forum.entity.SearchSyncTask;
import com.campus.forum.mapper.SearchSyncTaskMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class SearchSyncRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(SearchSyncRetryScheduler.class);

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_SUCCESS = 1;
    private static final int STATUS_DEAD = 2;
    private static final int STATUS_PROCESSING = 3;

    @Autowired(required = false)
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private SearchSyncTaskMapper taskMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${search.engine:db}")
    private String searchEngine;

    @Value("${search.sync.retry.batch-size:20}")
    private Integer batchSize;

    @Value("${search.sync.retry.max-backoff-seconds:600}")
    private Integer maxBackoffSeconds;

    @Scheduled(fixedDelayString = "${search.sync.retry.fixed-delay-ms:30000}")
    public void replayFailedTasks() {
        if (!isEnabled()) {
            return;
        }

        List<SearchSyncTask> tasks = taskMapper.selectDuePendingTasks(batchSize);
        if (tasks == null || tasks.isEmpty()) {
            return;
        }

        for (SearchSyncTask task : tasks) {
            int claimed = taskMapper.markProcessing(task.getId());
            if (claimed == 0) {
                continue;
            }
            processTask(task);
        }
    }

    private void processTask(SearchSyncTask task) {
        try {
            if ("UPSERT".equalsIgnoreCase(task.getOperationType())) {
                replayUpsert(task);
            } else {
                replayDelete(task);
            }
            taskMapper.markSuccess(task.getId());
        } catch (Exception ex) {
            int retryCount = (task.getRetryCount() == null ? 0 : task.getRetryCount()) + 1;
            int maxRetry = task.getMaxRetry() == null ? 6 : task.getMaxRetry();
            int nextStatus = retryCount >= maxRetry ? STATUS_DEAD : STATUS_PENDING;
            LocalDateTime nextRetry = LocalDateTime.now().plusSeconds(calcBackoffSeconds(retryCount));
            String error = ex.getMessage();
            if (error != null && error.length() > 900) {
                error = error.substring(0, 900);
            }
            taskMapper.markFailed(task.getId(), nextStatus, retryCount, error, nextRetry);
            log.warn("Replay sync task failed. taskId={}, retryCount={}, nextStatus={}, reason={}",
                    task.getId(), retryCount, nextStatus, ex.getMessage());
        }
    }

    private void replayUpsert(SearchSyncTask task) throws Exception {
        Map<String, Object> doc = objectMapper.readValue(task.getPayloadJson(), new TypeReference<Map<String, Object>>() {
        });
        elasticsearchClient.index(req -> req
                .index(task.getIndexName())
                .id(String.valueOf(task.getDocumentId()))
                .document(doc));
    }

    private void replayDelete(SearchSyncTask task) throws Exception {
        elasticsearchClient.delete(req -> req
                .index(task.getIndexName())
                .id(String.valueOf(task.getDocumentId())));
    }

    private long calcBackoffSeconds(int retryCount) {
        long base = (long) Math.pow(2, Math.min(retryCount, 10));
        return Math.min(base, maxBackoffSeconds == null ? 600 : maxBackoffSeconds);
    }

    private boolean isEnabled() {
        return "es".equalsIgnoreCase(searchEngine) && elasticsearchClient != null;
    }
}
