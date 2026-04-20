package com.campus.forum.search.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import com.campus.forum.mapper.SearchIndexMapper;
import com.campus.forum.search.SearchIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchIndexServiceImpl implements SearchIndexService {

    private static final Logger log = LoggerFactory.getLogger(SearchIndexServiceImpl.class);

    @Autowired(required = false)
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private SearchIndexMapper searchIndexMapper;

    @Value("${search.es.index.posts:forum_post}")
    private String postIndex;

    @Value("${search.es.index.products:service_product}")
    private String productIndex;

    @Value("${search.es.index.activities:service_activity}")
    private String activityIndex;

    @Value("${search.es.index.helps:service_help_request}")
    private String helpIndex;

    @Override
    public Map<String, Object> rebuildAll() {
        ensureEsEnabled();

        Map<String, Object> result = new HashMap<>();
        Map<String, Object> detail = new HashMap<>();

        try {
            recreateIndex(postIndex);
            recreateIndex(productIndex);
            recreateIndex(activityIndex);
            recreateIndex(helpIndex);

            detail.put(postIndex, bulkIndex(postIndex, searchIndexMapper.selectAllPostDocs()));
            detail.put(productIndex, bulkIndex(productIndex, searchIndexMapper.selectAllProductDocs()));
            detail.put(activityIndex, bulkIndex(activityIndex, searchIndexMapper.selectAllActivityDocs()));
            detail.put(helpIndex, bulkIndex(helpIndex, searchIndexMapper.selectAllHelpDocs()));

            result.put("success", true);
            result.put("detail", detail);
            return result;
        } catch (Exception ex) {
            log.error("Rebuild search indices failed", ex);
            result.put("success", false);
            result.put("message", ex.getMessage());
            result.put("detail", detail);
            return result;
        }
    }

    private void ensureEsEnabled() {
        if (elasticsearchClient == null) {
            throw new IllegalStateException("ElasticsearchClient unavailable. Check ES configuration and dependencies.");
        }
    }

    private void recreateIndex(String indexName) throws IOException {
        boolean exists = elasticsearchClient.indices().exists(req -> req.index(indexName)).value();
        if (exists) {
            elasticsearchClient.indices().delete(req -> req.index(indexName));
        }
        elasticsearchClient.indices().create(req -> req.index(indexName));
    }

    private Map<String, Object> bulkIndex(String indexName, List<Map<String, Object>> docs) throws IOException {
        Map<String, Object> stat = new HashMap<>();
        stat.put("total", docs.size());

        if (docs.isEmpty()) {
            stat.put("indexed", 0);
            stat.put("failed", 0);
            return stat;
        }

        BulkResponse response = elasticsearchClient.bulk(bulk -> {
            for (Map<String, Object> doc : docs) {
                Object id = doc.get("id");
                String docId = id == null ? null : String.valueOf(id);
                bulk.operations(op -> op.index(idx -> idx.index(indexName).id(docId).document(doc)));
            }
            return bulk;
        });

        int failed = 0;
        if (response.errors()) {
            failed = (int) response.items().stream().filter(item -> item.error() != null).count();
        }
        stat.put("indexed", docs.size() - failed);
        stat.put("failed", failed);
        return stat;
    }
}
