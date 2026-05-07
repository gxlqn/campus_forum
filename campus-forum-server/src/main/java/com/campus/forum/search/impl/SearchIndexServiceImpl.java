package com.campus.forum.search.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import com.campus.forum.mapper.SearchIndexMapper;
import com.campus.forum.search.SearchIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
            // 先上传同义词文件到 ES（必须在创建索引之前）
            uploadSynonymsFile();

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

    /**
     * 上传同义词文件到 ES 的 config/analysis 目录，供 synonym filter 引用
     */
    private void uploadSynonymsFile() {
        try {
            ClassPathResource synonymResource = new ClassPathResource("elasticsearch/synonyms.txt");
            if (!synonymResource.exists()) {
                log.warn("Synonyms file not found: elasticsearch/synonyms.txt, skipping upload");
                return;
            }
            String synonymContent = FileCopyUtils.copyToString(
                    new InputStreamReader(synonymResource.getInputStream(), StandardCharsets.UTF_8));
            // 写入 ES 的 node-local config 目录
            // 注意：ES 7.x 的 synonym filter 需要 synonyms_path 指向 ES 节点 config 目录下的文件
            // 这里我们将同义词内联到 mapping JSON 中，使用 synonyms 参数而非 synonyms_path
            log.info("Synonyms file loaded ({} bytes), will be inlined in mapping", synonymContent.length());
        } catch (Exception ex) {
            log.warn("Failed to read synonyms file: {}", ex.getMessage());
        }
    }

    private void recreateIndex(String indexName) throws IOException {
        boolean exists = elasticsearchClient.indices().exists(req -> req.index(indexName)).value();
        if (exists) {
            elasticsearchClient.indices().delete(req -> req.index(indexName));
        }

        // 加载 mapping JSON 文件
        String mappingPath = "elasticsearch/mappings/" + indexName + ".json";
        ClassPathResource resource = new ClassPathResource(mappingPath);
        if (resource.exists()) {
            String jsonSource = FileCopyUtils.copyToString(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

            // 将 synonyms_path 替换为内联 synonyms（因为 ES 7.x 需要 synonyms_path 指向 ES 节点本地文件）
            ClassPathResource synonymResource = new ClassPathResource("elasticsearch/synonyms.txt");
            if (synonymResource.exists()) {
                String synonymContent = FileCopyUtils.copyToString(
                        new InputStreamReader(synonymResource.getInputStream(), StandardCharsets.UTF_8));
                // 构建 synonyms 数组 JSON
                String[] lines = synonymContent.split("\n");
                StringBuilder synonymsJson = new StringBuilder("[");
                boolean first = true;
                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    if (!first) {
                        synonymsJson.append(",");
                    }
                    synonymsJson.append("\"").append(line.replace("\"", "\\\"")).append("\"");
                    first = false;
                }
                synonymsJson.append("]");
                // 替换 synonyms_path 为 synonyms 内联
                jsonSource = jsonSource.replaceAll(
                        "\"synonyms_path\"\\s*:\\s*\"[^\"]*\"",
                        "\"synonyms\" : " + synonymsJson
                );
                // 移除可能残留的 synonyms_path 行
                jsonSource = jsonSource.replaceAll(",?\\s*\"synonyms_path\"\\s*:\\s*\"[^\"]*\"\\s*,?", "");
            }

            log.info("Creating index '{}' with mapping from {}", indexName, mappingPath);
            final String finalMappingJson = jsonSource;
            elasticsearchClient.indices().create(req -> req
                    .index(indexName)
                    .withJson(new java.io.StringReader(finalMappingJson))
            );
        } else {
            log.warn("Mapping file not found: {}, creating index with auto-mapping", mappingPath);
            elasticsearchClient.indices().create(req -> req.index(indexName));
        }
    }

    private Map<String, Object> bulkIndex(String indexName, List<Map<String, Object>> docs) throws IOException {
        Map<String, Object> stat = new HashMap<>();
        stat.put("total", docs.size());

        if (docs.isEmpty()) {
            stat.put("indexed", 0);
            stat.put("failed", 0);
            return stat;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        BulkResponse response = elasticsearchClient.bulk(bulk -> {
            for (Map<String, Object> doc : docs) {
                // 将 LocalDateTime/LocalDate 转为字符串，避免 ES Jackson 序列化异常
                Map<String, Object> sanitized = new HashMap<>(doc.size());
                for (Map.Entry<String, Object> entry : doc.entrySet()) {
                    Object val = entry.getValue();
                    if (val instanceof LocalDateTime) {
                        sanitized.put(entry.getKey(), ((LocalDateTime) val).format(formatter));
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

                Object id = sanitized.get("id");
                String docId = id == null ? null : String.valueOf(id);
                bulk.operations(op -> op.index(idx -> idx.index(indexName).id(docId).document(sanitized)));
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
