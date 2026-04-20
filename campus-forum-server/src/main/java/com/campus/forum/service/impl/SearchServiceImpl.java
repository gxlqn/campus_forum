package com.campus.forum.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.campus.forum.common.PageResult;
import com.campus.forum.entity.ForumPost;
import com.campus.forum.entity.ForumSection;
import com.campus.forum.entity.ServiceActivity;
import com.campus.forum.entity.ServiceHelpRequest;
import com.campus.forum.entity.ServiceProduct;
import com.campus.forum.mapper.ForumPostMapper;
import com.campus.forum.mapper.ForumSectionMapper;
import com.campus.forum.service.ActivityService;
import com.campus.forum.service.ForumService;
import com.campus.forum.service.HelpService;
import com.campus.forum.service.ProductService;
import com.campus.forum.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchServiceImpl.class);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    private ForumSectionMapper forumSectionMapper;

    @Autowired
    private ForumPostMapper forumPostMapper;

    @Autowired
    private ForumService forumService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private HelpService helpService;

    @Autowired(required = false)
    private ElasticsearchClient elasticsearchClient;

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
    public Map<String, Object> searchAll(String keyword, Integer size) {
        String kw = keyword == null ? "" : keyword.trim();
        int limit = normalizeSize(size);

        Map<String, Object> result = new HashMap<>();
        result.put("keyword", kw);
        result.put("sections", searchSections(kw, limit));

        if (kw.isEmpty()) {
            result.put("posts", List.of());
            result.put("products", List.of());
            result.put("activities", List.of());
            result.put("helps", List.of());
            return result;
        }

        SearchPayload payload;
        if ("es".equalsIgnoreCase(searchEngine)) {
            payload = searchByEsOrFallback(kw, limit);
        } else {
            payload = searchByDb(kw, limit);
        }

        result.put("posts", payload.posts);
        result.put("products", payload.products);
        result.put("activities", payload.activities);
        result.put("helps", payload.helps);
        return result;
    }

    @Override
    public List<String> recommendKeywords(Integer size) {
        int limit = normalizeSize(size);
        int fetchSize = Math.max(limit * 3, 20);
        List<String> titles = forumPostMapper.selectHotPostTitles(fetchSize);

        return safe(titles).stream()
                .map(this::normalizeTitleKeyword)
                .filter(Objects::nonNull)
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }

    private SearchPayload searchByEsOrFallback(String keyword, int limit) {
        if (elasticsearchClient == null) {
            log.warn("search.engine=es but ElasticsearchClient is unavailable, fallback to DB search");
            return searchByDb(keyword, limit);
        }

        try {
            List<Map<String, Object>> postDocs = searchIndex(postIndex, List.of("title", "content"), keyword, limit);
            List<Map<String, Object>> productDocs = searchIndex(productIndex, List.of("title", "description"), keyword, limit);
            List<Map<String, Object>> activityDocs = searchIndex(activityIndex, List.of("title", "description", "location"), keyword, limit);
            List<Map<String, Object>> helpDocs = searchIndex(helpIndex, List.of("title", "description", "pickupLocation", "deliveryLocation"), keyword, limit);

            return new SearchPayload(
                    normalizePostMaps(postDocs),
                    normalizeProductMaps(productDocs),
                    normalizeActivityMaps(activityDocs),
                    normalizeHelpMaps(helpDocs)
            );
        } catch (Exception ex) {
            log.warn("Elasticsearch search failed, fallback to DB search. reason={}", ex.getMessage());
            return searchByDb(keyword, limit);
        }
    }

    private SearchPayload searchByDb(String keyword, int limit) {
        PageResult<ForumPost> postsPage = forumService.getPostList(1L, (long) limit, null, keyword, "latest", 72);
        PageResult<ServiceProduct> productPage = productService.getProductList(1L, (long) limit, null, null, null, keyword);
        PageResult<ServiceActivity> activityPage = activityService.getActivityList(1L, (long) limit, null, keyword);
        PageResult<ServiceHelpRequest> helpPage = helpService.getHelpList(1L, (long) limit, null, keyword);

        return new SearchPayload(
                mapPosts(postsPage.getRecords()),
                mapProducts(productPage.getRecords()),
                mapActivities(activityPage.getRecords()),
                mapHelps(helpPage.getRecords())
        );
    }

    private List<Map<String, Object>> searchSections(String keyword, int size) {
        List<ForumSection> sections = forumSectionMapper.selectEnabledSections();
        String kwLower = keyword == null ? "" : keyword.toLowerCase(Locale.ROOT);

        List<Map<String, Object>> result = new ArrayList<>();
        for (ForumSection item : sections) {
            String name = item.getSectionName() == null ? "" : item.getSectionName();
            if (!kwLower.isEmpty() && !name.toLowerCase(Locale.ROOT).contains(kwLower)) {
                continue;
            }
            Map<String, Object> mapped = new HashMap<>();
            mapped.put("id", item.getId());
            mapped.put("sectionName", name);
            result.add(mapped);
            if (result.size() >= size) {
                break;
            }
        }
        return result;
    }

    private List<Map<String, Object>> mapPosts(List<ForumPost> posts) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ForumPost post : safe(posts)) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", post.getId());
            item.put("title", post.getTitle());
            item.put("content", post.getContent());
            item.put("sectionName", post.getSection() != null ? post.getSection().getSectionName() : null);

            Map<String, Object> author = new HashMap<>();
            String nickname = post.getAuthor() != null ? post.getAuthor().getNickname() : null;
            author.put("nickname", nickname == null ? "匿名用户" : nickname);
            item.put("author", author);
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> mapProducts(List<ServiceProduct> products) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ServiceProduct product : safe(products)) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", product.getId());
            item.put("title", product.getTitle());
            item.put("description", product.getDescription());
            item.put("price", product.getPrice());

            Map<String, Object> seller = new HashMap<>();
            String nickname = product.getSeller() != null ? product.getSeller().getNickname() : null;
            if (nickname == null && product.getUser() != null) {
                nickname = product.getUser().getNickname();
            }
            seller.put("nickname", nickname == null ? "匿名用户" : nickname);
            item.put("seller", seller);
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> mapActivities(List<ServiceActivity> activities) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ServiceActivity activity : safe(activities)) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", activity.getId());
            item.put("title", activity.getTitle());
            item.put("description", activity.getDescription());
            item.put("time", formatTime(activity.getStartTime()));
            item.put("location", activity.getLocation());
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> mapHelps(List<ServiceHelpRequest> helps) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ServiceHelpRequest help : safe(helps)) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", help.getId());
            item.put("title", help.getTitle());
            item.put("description", help.getDescription());
            item.put("time", formatTime(help.getExpectedTime()));
            item.put("location", firstNonBlank(help.getDeliveryLocation(), help.getPickupLocation(), help.getExpressLocation()));
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> searchIndex(String index, List<String> fields, String keyword, int size) throws IOException {
        SearchResponse<Map> response = elasticsearchClient.search(s -> s
                        .index(index)
                        .size(size)
                        .query(q -> q.multiMatch(mm -> mm.query(keyword).fields(fields))),
                Map.class);

        List<Map<String, Object>> docs = new ArrayList<>();
        response.hits().hits().forEach(hit -> {
            Map source = hit.source();
            if (source != null) {
                docs.add(source);
            }
        });
        return docs;
    }

    private List<Map<String, Object>> normalizePostMaps(List<Map<String, Object>> docs) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> doc : safe(docs)) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", parseLong(doc.get("id")));
            item.put("title", stringValue(doc.get("title")));
            item.put("content", stringValue(doc.get("content")));
            item.put("sectionName", firstNonBlank(stringValue(doc.get("sectionName")), stringValue(doc.get("section_name"))));

            Map<String, Object> author = new HashMap<>();
            author.put("nickname", firstNonBlank(readNestedString(doc, "author", "nickname"), stringValue(doc.get("authorNickname")), "匿名用户"));
            item.put("author", author);
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> normalizeProductMaps(List<Map<String, Object>> docs) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> doc : safe(docs)) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", parseLong(doc.get("id")));
            item.put("title", stringValue(doc.get("title")));
            item.put("description", stringValue(doc.get("description")));
            item.put("price", doc.get("price"));

            Map<String, Object> seller = new HashMap<>();
            seller.put("nickname", firstNonBlank(readNestedString(doc, "seller", "nickname"), stringValue(doc.get("sellerNickname")), "匿名用户"));
            item.put("seller", seller);
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> normalizeActivityMaps(List<Map<String, Object>> docs) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> doc : safe(docs)) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", parseLong(doc.get("id")));
            item.put("title", stringValue(doc.get("title")));
            item.put("description", stringValue(doc.get("description")));
            item.put("time", firstNonBlank(stringValue(doc.get("time")), stringValue(doc.get("startTime"))));
            item.put("location", stringValue(doc.get("location")));
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> normalizeHelpMaps(List<Map<String, Object>> docs) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> doc : safe(docs)) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", parseLong(doc.get("id")));
            item.put("title", stringValue(doc.get("title")));
            item.put("description", stringValue(doc.get("description")));
            item.put("time", firstNonBlank(stringValue(doc.get("time")), stringValue(doc.get("expectedTime"))));
            item.put("location", firstNonBlank(stringValue(doc.get("location")), stringValue(doc.get("deliveryLocation")), stringValue(doc.get("pickupLocation"))));
            result.add(item);
        }
        return result;
    }

    private int normalizeSize(Integer size) {
        if (size == null || size <= 0) {
            return 5;
        }
        return Math.min(size, 20);
    }

    private String normalizeTitleKeyword(String title) {
        if (title == null) {
            return null;
        }
        String text = title
                .replaceAll("[\\r\\n\\t]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (text.isEmpty()) {
            return null;
        }
        // 保留短语形式，避免过度分词造成语义损失
        if (text.length() > 14) {
            text = text.substring(0, 14).trim();
        }
        return text.length() < 2 ? null : text;
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? null : time.format(TIME_FMT);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String readNestedString(Map<String, Object> source, String nestedKey, String field) {
        Object nested = source.get(nestedKey);
        if (nested instanceof Map<?, ?> nestedMap) {
            Object value = ((Map<String, Object>) nestedMap).get(field);
            return stringValue(value);
        }
        return null;
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private <T> List<T> safe(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static class SearchPayload {
        private final List<Map<String, Object>> posts;
        private final List<Map<String, Object>> products;
        private final List<Map<String, Object>> activities;
        private final List<Map<String, Object>> helps;

        private SearchPayload(List<Map<String, Object>> posts,
                              List<Map<String, Object>> products,
                              List<Map<String, Object>> activities,
                              List<Map<String, Object>> helps) {
            this.posts = Objects.requireNonNullElse(posts, List.of());
            this.products = Objects.requireNonNullElse(products, List.of());
            this.activities = Objects.requireNonNullElse(activities, List.of());
            this.helps = Objects.requireNonNullElse(helps, List.of());
        }
    }
}
