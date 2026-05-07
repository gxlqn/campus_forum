package com.campus.forum.search.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.campus.forum.mapper.SearchIndexMapper;
import com.campus.forum.entity.SearchSyncTask;
import com.campus.forum.mapper.SearchSyncTaskMapper;
import com.campus.forum.search.SearchSyncService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchSyncServiceImpl implements SearchSyncService {

    private static final Logger log = LoggerFactory.getLogger(SearchSyncServiceImpl.class);

    @Autowired(required = false)
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private SearchIndexMapper searchIndexMapper;

    @Autowired
    private SearchSyncTaskMapper searchSyncTaskMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${search.engine:db}")
    private String searchEngine;

    @Value("${search.es.index.posts:forum_post}")
    private String postIndex;

    @Value("${search.es.index.products:service_product}")
    private String productIndex;

    @Value("${search.es.index.activities:service_activity}")
    private String activityIndex;

    @Value("${search.es.index.helps:service_help_request}")
    private String helpIndex;

    @Override
    public void syncPost(Long id) {
        syncById(postIndex, "post", id, searchIndexMapper.selectPostDocById(id));
    }

    @Override
    public void deletePost(Long id) {
        deleteById(postIndex, "post", id);
    }

    @Override
    public void syncProduct(Long id) {
        syncById(productIndex, "product", id, searchIndexMapper.selectProductDocById(id));
    }

    @Override
    public void deleteProduct(Long id) {
        deleteById(productIndex, "product", id);
    }

    @Override
    public void syncActivity(Long id) {
        syncById(activityIndex, "activity", id, searchIndexMapper.selectActivityDocById(id));
    }

    @Override
    public void deleteActivity(Long id) {
        deleteById(activityIndex, "activity", id);
    }

    @Override
    public void syncHelp(Long id) {
        syncById(helpIndex, "help", id, searchIndexMapper.selectHelpDocById(id));
    }

    @Override
    public void deleteHelp(Long id) {
        deleteById(helpIndex, "help", id);
    }

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private void syncById(String index, String entityType, Long id, Map<String, Object> doc) {
        if (!isEnabled() || id == null) {
            return;
        }
        try {
            if (doc == null) {
                deleteById(index, entityType, id);
                return;
            }
            elasticsearchClient.index(req -> req.index(index).id(String.valueOf(id)).document(sanitizeDoc(doc)));
        } catch (Exception ex) {
            log.warn("Search sync upsert failed. index={}, id={}, reason={}", index, id, ex.getMessage());
            enqueueFailure(index, entityType, id, "UPSERT", doc, ex.getMessage());
        }
    }

    /**
     * 将文档中的 LocalDateTime/LocalDate 转为字符串，避免 ES Jackson 序列化异常
     */
    private Map<String, Object> sanitizeDoc(Map<String, Object> doc) {
        Map<String, Object> sanitized = new HashMap<>(doc.size());
        for (Map.Entry<String, Object> entry : doc.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof LocalDateTime) {
                sanitized.put(entry.getKey(), ((LocalDateTime) val).format(DT_FMT));
            } else if (val instanceof LocalDate) {
                sanitized.put(entry.getKey(), val.toString());
            } else {
                sanitized.put(entry.getKey(), val);
            }
        }
        // 注入 completion suggest 字段（取 title 作为补全候选）
        Object title = sanitized.get("title");
        if (title != null && !String.valueOf(title).isBlank()) {
            sanitized.put("suggest", Map.of("input", List.of(String.valueOf(title))));
        }
        return sanitized;
    }

    private void deleteById(String index, String entityType, Long id) {
        if (!isEnabled() || id == null) {
            return;
        }
        try {
            elasticsearchClient.delete(req -> req.index(index).id(String.valueOf(id)));
        } catch (Exception ex) {
            log.warn("Search sync delete failed. index={}, id={}, reason={}", index, id, ex.getMessage());
            enqueueFailure(index, entityType, id, "DELETE", null, ex.getMessage());
        }
    }

    private void enqueueFailure(String index,
                                String entityType,
                                Long documentId,
                                String operationType,
                                Map<String, Object> payload,
                                String error) {
        try {
            SearchSyncTask task = new SearchSyncTask();
            task.setIndexName(index);
            task.setEntityType(entityType);
            task.setDocumentId(documentId);
            task.setOperationType(operationType);
            task.setPayloadJson(payload == null ? null : objectMapper.writeValueAsString(payload));
            task.setStatus(0);
            task.setRetryCount(0);
            task.setMaxRetry(6);
            task.setLastError(error);
            task.setNextRetryTime(java.time.LocalDateTime.now());
            searchSyncTaskMapper.insert(task);
        } catch (Exception enqueueEx) {
            log.error("Enqueue search sync task failed. index={}, id={}, op={}, reason={}",
                    index, documentId, operationType, enqueueEx.getMessage());
        }
    }

    private boolean isEnabled() {
        return "es".equalsIgnoreCase(searchEngine) && elasticsearchClient != null;
    }
}
