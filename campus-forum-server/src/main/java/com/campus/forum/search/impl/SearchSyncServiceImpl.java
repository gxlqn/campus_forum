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

    private void syncById(String index, String entityType, Long id, Map<String, Object> doc) {
        if (!isEnabled() || id == null) {
            return;
        }
        try {
            if (doc == null) {
                deleteById(index, entityType, id);
                return;
            }
            elasticsearchClient.index(req -> req.index(index).id(String.valueOf(id)).document(doc));
        } catch (Exception ex) {
            log.warn("Search sync upsert failed. index={}, id={}, reason={}", index, id, ex.getMessage());
            enqueueFailure(index, entityType, id, "UPSERT", doc, ex.getMessage());
        }
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
