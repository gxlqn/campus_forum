package com.campus.forum.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.aggregations.RangeBucket;
import co.elastic.clients.elasticsearch._types.aggregations.DateHistogramBucket;
import co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval;
import co.elastic.clients.elasticsearch._types.aggregations.AggregationRange;
import co.elastic.clients.elasticsearch._types.query_dsl.FieldValueFactorModifier;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggester;
import co.elastic.clients.elasticsearch.core.search.FieldSuggester;
import co.elastic.clients.elasticsearch.core.search.HighlighterOrder;
import co.elastic.clients.elasticsearch.core.search.PhraseSuggester;
import co.elastic.clients.elasticsearch.core.search.Suggester;
import co.elastic.clients.json.JsonData;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    // ======================== 1. 主搜索（含聚合 + Function Score + Match Phrase + Filter + Sort） ========================

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
            payload = searchByEsOrFallback(kw, limit, null, null, null);
        } else {
            payload = searchByDb(kw, limit);
        }

        result.put("posts", payload.posts);
        result.put("products", payload.products);
        result.put("activities", payload.activities);
        result.put("helps", payload.helps);
        List<Map<String, Object>> merged = buildMergedResults(kw, payload, limit);
        result.put("merged", merged);
        result.put("mergedList", merged);

        // 聚合结果
        if ("es".equalsIgnoreCase(searchEngine) && payload.aggregations != null && !payload.aggregations.isEmpty()) {
            result.put("aggregations", payload.aggregations);
        }

        // 搜索建议（Phrase Suggester 纠错 + ES 候选）
        List<String> esCandidates = collectEsSuggestionCandidates(kw, limit);
        List<String> phraseSuggestions = phraseSuggest(kw);
        List<String> suggestions = buildSearchSuggestions(kw, payload, limit, esCandidates, phraseSuggestions);
        result.put("suggestions", suggestions);
        result.put("correctedKeyword", suggestions.isEmpty() ? null : suggestions.get(0));
        return result;
    }

    /**
     * 高级搜索：支持 section/priceRange/sort 参数
     */
    @Override
    public Map<String, Object> searchAdvanced(String keyword, Integer size,
                                               String sectionName, String priceRange, String sort) {
        String kw = keyword == null ? "" : keyword.trim();
        int limit = normalizeSize(size);

        Map<String, Object> result = new HashMap<>();
        result.put("keyword", kw);

        if (kw.isEmpty() && sectionName == null && priceRange == null) {
            result.put("posts", List.of());
            result.put("products", List.of());
            result.put("activities", List.of());
            result.put("helps", List.of());
            return result;
        }

        SearchPayload payload;
        if ("es".equalsIgnoreCase(searchEngine)) {
            payload = searchByEsOrFallback(kw.isEmpty() ? null : kw, limit, sectionName, priceRange, sort);
        } else {
            payload = searchByDb(kw.isEmpty() ? null : kw, limit);
        }

        result.put("posts", payload.posts);
        result.put("products", payload.products);
        result.put("activities", payload.activities);
        result.put("helps", payload.helps);

        List<Map<String, Object>> merged = buildMergedResults(kw, payload, limit);
        result.put("merged", merged);
        result.put("mergedList", merged);

        if (payload.aggregations != null && !payload.aggregations.isEmpty()) {
            result.put("aggregations", payload.aggregations);
        }
        return result;
    }

    // ======================== 2. Completion Suggester（自动补全） ========================

    @Override
    public List<String> suggestCompletion(String prefix, Integer size) {
        if (!"es".equalsIgnoreCase(searchEngine) || elasticsearchClient == null) {
            return recommendKeywords(size);
        }
        if (prefix == null || prefix.isBlank()) {
            return List.of();
        }
        int limit = normalizeSize(size);

        Set<String> results = new LinkedHashSet<>();
        List<String> indexes = Arrays.asList(postIndex, productIndex, activityIndex, helpIndex);

        // 1. Completion Suggester（基于 suggest 字段，ik_smart_pinyin + pinyin_uv_norm 做 ü→v 归一化）
        List<String> prefixes = buildSuggestPrefixes(prefix);
        for (String index : indexes) {
            for (String pfx : prefixes) {
                try {
                    SearchResponse<Void> response = elasticsearchClient.search(s -> s
                            .index(index)
                            .size(0)
                            .suggest(Suggester.of(sg -> sg
                                    .suggesters(Map.of(
                                            index + "_suggest", FieldSuggester.of(fsg -> fsg
                                                    .prefix(pfx)
                                                    .completion(CompletionSuggester.of(c -> c
                                                            .field("suggest")
                                                            .size(limit)
                                                            .skipDuplicates(true)
                                                    ))
                                            )
                                    ))
                            )), Void.class);

                    var suggestResult = response.suggest();
                    if (suggestResult != null) {
                        var entry = suggestResult.get(index + "_suggest");
                        if (entry != null) {
                            for (var suggestEntry : entry) {
                                for (CompletionSuggestOption<Void> option : suggestEntry.completion().options()) {
                                    if (option.text() != null) {
                                        results.add(option.text());
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    log.debug("Completion suggest failed for index={}, prefix={}: {}", index, pfx, ex.getMessage());
                }
            }
        }

        // 2. 拼音 Fallback：当 completion suggest 结果不足时，用 pinyin match query 补全
        if (results.size() < limit) {
            boolean hasChinese = prefix.chars().anyMatch(c -> Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN);
            if (!hasChinese) {
                Set<String> pinyinResults = searchByPinyinPrefix(prefix, indexes, limit - results.size());
                pinyinResults.removeAll(results);
                results.addAll(pinyinResults);
            }
        }

        return results.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * 拼音前缀匹配：通过 standard 分析器在 title 字段做 match 查询，
     * 用于支持输入拼音（如 "nv"）时匹配到中文标题（如 "女生宿舍"）
     */
    private Set<String> searchByPinyinPrefix(String prefix, List<String> indexes, int size) {
        Set<String> titles = new LinkedHashSet<>();
        for (String index : indexes) {
            try {
                SearchResponse<Map> response = elasticsearchClient.search(s -> s
                                .index(index)
                                .size(size)
                                .query(q -> q.match(m -> m
                                        .field("title")
                                        .query(prefix)
                                        .analyzer("standard")
                                )),
                        Map.class);
                response.hits().hits().forEach(hit -> {
                    Map source = hit.source();
                    if (source != null) {
                        Object title = source.get("title");
                        if (title != null && !String.valueOf(title).isBlank()) {
                            titles.add(String.valueOf(title));
                        }
                    }
                });
            } catch (Exception ex) {
                log.debug("Pinyin fallback search failed for index={}, prefix={}: {}", index, prefix, ex.getMessage());
            }
        }
        return titles;
    }

    private List<String> buildSuggestPrefixes(String input) {
        List<String> prefixes = new ArrayList<>();
        prefixes.add(input);
        return prefixes.stream().distinct().toList();
    }

    // ======================== 3. Phrase Suggester（纠错建议 / Did you mean?） ========================

    @Override
    public List<String> phraseSuggest(String keyword) {
        if (!"es".equalsIgnoreCase(searchEngine) || elasticsearchClient == null) {
            return List.of();
        }
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        List<String> suggestions = new ArrayList<>();
        try {
            SearchResponse<Void> response = elasticsearchClient.search(s -> s
                    .index(postIndex)
                    .size(0)
                    .suggest(Suggester.of(sg -> sg
                            .suggesters(Map.of(
                                    "phrase_suggest", FieldSuggester.of(fsg -> fsg
                                            .text(keyword)
                                            .phrase(PhraseSuggester.of(p -> p
                                                    .field("title")
                                                    .size(3)
                                                    .maxErrors(2.0)
                                                    .confidence(0.5)
                                                    .directGenerator(dg -> dg
                                                            .field("title")
                                                            .suggestMode(co.elastic.clients.elasticsearch._types.SuggestMode.Always)
                                                            .minWordLength(2)
                                                    )
                                            ))
                                    )
                            ))
                    )), Void.class);

            var suggestResult = response.suggest();
            if (suggestResult != null) {
                var entry = suggestResult.get("phrase_suggest");
                if (entry != null) {
                    for (var suggestEntry : entry) {
                        for (var option : suggestEntry.phrase().options()) {
                            if (option.text() != null && !option.text().equalsIgnoreCase(keyword)) {
                                suggestions.add(option.text());
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.debug("Phrase suggest failed: {}", ex.getMessage());
        }
        return suggestions;
    }

    // ======================== 7. More Like This（相似推荐） ========================

    @Override
    public List<Map<String, Object>> moreLikeThis(String type, Long id, Integer size) {
        if (!"es".equalsIgnoreCase(searchEngine) || elasticsearchClient == null) {
            return List.of();
        }
        if (type == null || id == null) {
            return List.of();
        }
        int limit = normalizeSize(size);

        String index = resolveIndex(type);
        if (index == null) {
            return List.of();
        }

        try {
            SearchResponse<Map> response = elasticsearchClient.search(s -> s
                    .index(index)
                    .size(limit)
                    .query(q -> q.moreLikeThis(mlt -> mlt
                            .fields("title", "content", "description")
                            .like(l -> l.document(d -> d.index(index).id(String.valueOf(id))))
                            .minTermFreq(1)
                            .minDocFreq(1)
                            .maxQueryTerms(10)
                    ))
                    .highlight(h -> h
                            .preTags("<em class=\"es-highlight\">").postTags("</em>")
                            .fields("title", hf -> hf.fragmentSize(150).numberOfFragments(3))
                            .fields("content", hf -> hf.fragmentSize(150).numberOfFragments(3))
                            .fields("description", hf -> hf.fragmentSize(150).numberOfFragments(3))
                    ), Map.class);

            List<Map<String, Object>> docs = new ArrayList<>();
            response.hits().hits().forEach(hit -> {
                Map source = hit.source();
                if (source != null) {
                    Map<String, Object> docWithScore = new HashMap<>(source);
                    if (hit.score() != null) {
                        docWithScore.put("_score", hit.score());
                    }
                    injectHighlight(docWithScore, hit);
                    docs.add(docWithScore);
                }
            });
            return docs;
        } catch (Exception ex) {
            log.debug("More Like This search failed: {}", ex.getMessage());
            return List.of();
        }
    }

    @Override
    public Map<String, Object> getAggregations(String keyword, Integer size) {
        if (!"es".equalsIgnoreCase(searchEngine) || elasticsearchClient == null) {
            return Map.of();
        }
        if (keyword == null || keyword.isBlank()) {
            return Map.of();
        }
        int limit = normalizeSize(size);
        return searchAggregations(keyword, limit);
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

    // ======================== 核心搜索逻辑 ========================

    private SearchPayload searchByEsOrFallback(String keyword, int limit,
                                                String sectionName, String priceRange, String sort) {
        if (elasticsearchClient == null) {
            log.warn("search.engine=es but ElasticsearchClient is unavailable, fallback to DB search");
            return searchByDb(keyword, limit);
        }

        try {
            List<Map<String, Object>> postDocs = searchIndexWithBoost(postIndex, List.of("title", "content"), keyword, limit, "post", sectionName, null, sort);
            List<Map<String, Object>> productDocs = searchIndexWithBoost(productIndex, List.of("title", "description"), keyword, limit, "product", null, priceRange, sort);
            List<Map<String, Object>> activityDocs = searchIndexWithBoost(activityIndex, List.of("title", "description", "location"), keyword, limit, "activity", null, null, sort);
            List<Map<String, Object>> helpDocs = searchIndexWithBoost(helpIndex, List.of("title", "description", "pickupLocation", "deliveryLocation"), keyword, limit, "help", null, null, sort);

            Map<String, Object> aggregations = searchAggregations(keyword, limit);

            return new SearchPayload(
                    normalizePostMaps(postDocs),
                    normalizeProductMaps(productDocs),
                    normalizeActivityMaps(activityDocs),
                    normalizeHelpMaps(helpDocs),
                    aggregations
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
                mapHelps(helpPage.getRecords()),
                null
        );
    }

    /**
     * ES 搜索：Function Score + Match Phrase + Filter Context + Sort + Highlight FVH
     */
    private List<Map<String, Object>> searchIndexWithBoost(String index, List<String> fields, String keyword,
                                                            int size, String type,
                                                            String sectionName, String priceRange, String sort) throws IOException {
        if (keyword == null || keyword.isBlank()) {
            return browseIndex(index, size, sectionName, priceRange, sort);
        }
        return doSearchIndexWithBoost(index, fields, keyword, size, type, sectionName, priceRange, sort);
    }

    private List<Map<String, Object>> doSearchIndexWithBoost(String index, List<String> fields, String keyword,
                                                               int size, String type,
                                                               String sectionName, String priceRange, String sort) throws IOException {
        boolean isAsciiOnly = keyword.chars().allMatch(c -> c < 0x80);
        SearchResponse<Map> response = elasticsearchClient.search(s -> {
            s.index(index).size(size);

            // ---- Function Score Query ----
            s.query(q -> q.functionScore(fs -> {
                // 基础 bool 查询
                fs.query(bq -> bq.bool(b -> {
                    // Match Phrase 短语匹配（加分项，仅中文有意义）
                    if (!isAsciiOnly && "title".equals(fields.get(0))) {
                        b.should(sq -> sq.matchPhrase(mp -> mp
                                .field("title")
                                .query(keyword)
                                .slop(1)
                                .boost(2.0f)
                        ));
                    }

                    // 主查询：multi_match（拼音输入用 standard 分析器避免 ik_smart 截断，中文输入用字段默认分析器）
                    co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery.Builder mmBuilder = new co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery.Builder()
                            .query(keyword)
                            .fields(fields.stream()
                                    .map(f -> "title".equals(f) ? f + "^3" : f)
                                    .toList()
                            );
                    if (isAsciiOnly) {
                        mmBuilder.analyzer("standard");
                    }
                    b.must(m -> m.multiMatch(mmBuilder.build()));

                    // Filter Context（不评分、可缓存）
                    if (sectionName != null && !sectionName.isBlank()) {
                        b.filter(f -> f.term(t -> t.field("sectionName").value(sectionName)));
                    }
                    if (priceRange != null && !priceRange.isBlank()) {
                        applyPriceRangeFilter(b, priceRange);
                    }

                    return b;
                }));

                // 4. Function Score 多因子排序
                if ("post".equals(type)) {
                    // 时间衰减：越新权重越高
                    fs.functions(fn -> fn.gauss(g -> g
                            .field("createTime")
                            .placement(p -> p
                                    .origin(JsonData.of("now"))
                                    .scale(JsonData.of("30d"))
                                    .offset(JsonData.of("7d"))
                                    .decay(0.5)
                            )
                    ));
                    // 点赞数加权
                    fs.functions(fn -> fn.fieldValueFactor(fvf -> fvf
                            .field("likeCount")
                            .factor(1.2)
                            .modifier(FieldValueFactorModifier.Log1p)
                    ));
                    // 浏览数加权
                    fs.functions(fn -> fn.fieldValueFactor(fvf -> fvf
                            .field("viewCount")
                            .factor(0.5)
                            .modifier(FieldValueFactorModifier.Log1p)
                    ));
                }
                // 类型权重
                double typeWeight = switch (type) {
                    case "post" -> 1.1;
                    case "product" -> 1.0;
                    case "activity" -> 0.95;
                    case "help" -> 0.9;
                    default -> 1.0;
                };
                fs.functions(fn -> fn.weight(typeWeight));

                fs.boostMode(FunctionBoostMode.Multiply);
                return fs;
            }));

            // 7. Sort 支持
            applySort(s, sort, index);

            // Highlight FVH（Fast Vector Highlighter）
            s.highlight(h -> h
                    .preTags("<em class=\"es-highlight\">").postTags("</em>")
                    .fields("title", hf -> hf.fragmentSize(150).numberOfFragments(3).type("fvh"))
                    .fields("content", hf -> hf.fragmentSize(150).numberOfFragments(3).type("fvh"))
                    .fields("description", hf -> hf.fragmentSize(150).numberOfFragments(3).type("fvh"))
                    .fields("location", hf -> hf.fragmentSize(100).numberOfFragments(2))
                    .fields("pickupLocation", hf -> hf.fragmentSize(100).numberOfFragments(2))
                    .fields("deliveryLocation", hf -> hf.fragmentSize(100).numberOfFragments(2))
                    .order(HighlighterOrder.Score)
            );

            return s;
        }, Map.class);

        return extractHits(response);
    }

    /**
     * 无关键词时简单浏览（带过滤和排序）
     */
    private List<Map<String, Object>> browseIndex(String index, int size,
                                                   String sectionName, String priceRange, String sort) throws IOException {
        SearchResponse<Map> response = elasticsearchClient.search(s -> {
            s.index(index).size(size);

            s.query(q -> q.bool(b -> {
                b.must(m -> m.matchAll(ma -> ma));
                if (sectionName != null && !sectionName.isBlank()) {
                    b.filter(f -> f.term(t -> t.field("sectionName").value(sectionName)));
                }
                if (priceRange != null && !priceRange.isBlank()) {
                    applyPriceRangeFilter(b, priceRange);
                }
                return b;
            }));

            applySort(s, sort, index);
            return s;
        }, Map.class);

        return extractHits(response);
    }

    // ======================== 1. Aggregations（聚合分析） ========================

    private Map<String, Object> searchAggregations(String keyword, int size) {
        if (keyword == null || keyword.isBlank()) {
            return Map.of();
        }

        Map<String, Object> aggsResult = new LinkedHashMap<>();

        try {
            // 在帖子索引聚合板块分布
            SearchResponse<Map> postAggResponse = elasticsearchClient.search(s -> s
                    .index(postIndex)
                    .size(0)
                    .query(q -> q.multiMatch(mm -> mm
                            .query(keyword)
                            .fields("title^3", "content")
                    ))
                    .aggregations("sections", Aggregation.of(a -> a
                            .terms(t -> t.field("sectionName").size(10))
                    ))
                    .aggregations("time_dist", Aggregation.of(a -> a
                            .dateHistogram(dh -> dh
                                    .field("createTime")
                                    .calendarInterval(CalendarInterval.Month)
                                    .format("yyyy-MM")
                                    .minDocCount(1)
                            )
                    )), Map.class);

            var postAggs = postAggResponse.aggregations();
            if (postAggs != null) {
                if (postAggs.containsKey("sections")) {
                    List<Map<String, Object>> sections = new ArrayList<>();
                    for (StringTermsBucket bucket : postAggs.get("sections").sterms().buckets().array()) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("name", bucket.key().stringValue());
                        item.put("count", bucket.docCount());
                        sections.add(item);
                    }
                    aggsResult.put("sections", sections);
                }
                if (postAggs.containsKey("time_dist")) {
                    List<Map<String, Object>> timeDist = new ArrayList<>();
                    for (DateHistogramBucket bucket : postAggs.get("time_dist").dateHistogram().buckets().array()) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("month", bucket.keyAsString());
                        item.put("count", bucket.docCount());
                        timeDist.add(item);
                    }
                    aggsResult.put("timeDist", timeDist);
                }
            }

            // 在商品索引聚合价格区间分布
            SearchResponse<Map> productAggResponse = elasticsearchClient.search(s -> s
                    .index(productIndex)
                    .size(0)
                    .query(q -> q.multiMatch(mm -> mm
                            .query(keyword)
                            .fields("title^3", "description")
                    ))
                    .aggregations("price_range", Aggregation.of(a -> a
                            .range(r -> r
                                    .field("price")
                                    .ranges(
                                            AggregationRange.of(rng -> rng.key("0-50").to("50")),
                                            AggregationRange.of(rng -> rng.key("50-200").from("50").to("200")),
                                            AggregationRange.of(rng -> rng.key("200-500").from("200").to("500")),
                                            AggregationRange.of(rng -> rng.key("500+").from("500"))
                                    )
                            )
                    )), Map.class);

            var productAggs = productAggResponse.aggregations();
            if (productAggs != null && productAggs.containsKey("price_range")) {
                List<Map<String, Object>> priceRanges = new ArrayList<>();
                for (RangeBucket bucket : productAggs.get("price_range").range().buckets().array()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("range", bucket.key());
                    item.put("count", bucket.docCount());
                    priceRanges.add(item);
                }
                aggsResult.put("priceRange", priceRanges);
            }

            // 各类型命中数量统计
            Map<String, Long> typeCounts = new LinkedHashMap<>();
            for (var entry : List.of(
                    Map.entry("posts", postIndex),
                    Map.entry("products", productIndex),
                    Map.entry("activities", activityIndex),
                    Map.entry("helps", helpIndex)
            )) {
                try {
                    var countResp = elasticsearchClient.count(c -> c
                            .index(entry.getValue())
                            .query(q -> q.multiMatch(mm -> mm
                                    .query(keyword)
                                    .fields("title^3", "content", "description")
                            ))
                    );
                    typeCounts.put(entry.getKey(), countResp.count());
                } catch (Exception ex) {
                    typeCounts.put(entry.getKey(), 0L);
                }
            }
            aggsResult.put("typeCounts", typeCounts);

        } catch (Exception ex) {
            log.debug("Aggregations failed: {}", ex.getMessage());
        }

        return aggsResult;
    }

    // ======================== 辅助方法 ========================

    private List<Map<String, Object>> extractHits(SearchResponse<Map> response) {
        List<Map<String, Object>> docs = new ArrayList<>();
        response.hits().hits().forEach(hit -> {
            Map source = hit.source();
            if (source != null) {
                Map<String, Object> docWithScore = new HashMap<>(source);
                if (hit.score() != null) {
                    docWithScore.put("_score", hit.score());
                }
                injectHighlight(docWithScore, hit);
                docs.add(docWithScore);
            }
        });
        return docs;
    }

    @SuppressWarnings("unchecked")
    private void injectHighlight(Map<String, Object> docWithScore, co.elastic.clients.elasticsearch.core.search.Hit<Map> hit) {
        Map<String, List<String>> highlights = hit.highlight();
        if (highlights != null && !highlights.isEmpty()) {
            Map<String, String> highlightMap = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : highlights.entrySet()) {
                highlightMap.put(entry.getKey(), String.join(" ... ", entry.getValue()));
            }
            docWithScore.put("_highlight", highlightMap);
        }
    }

    private void applyPriceRangeFilter(co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder b, String priceRange) {
        try {
            if (priceRange.contains("-")) {
                String[] parts = priceRange.split("-", 2);
                double min = Double.parseDouble(parts[0]);
                double max = Double.parseDouble(parts[1]);
                b.filter(f -> f.range(r -> r.field("price").gte(JsonData.of(min)).lte(JsonData.of(max))));
            } else if (priceRange.endsWith("+")) {
                double min = Double.parseDouble(priceRange.replace("+", ""));
                b.filter(f -> f.range(r -> r.field("price").gte(JsonData.of(min))));
            }
        } catch (NumberFormatException ex) {
            log.debug("Invalid priceRange format: {}", priceRange);
        }
    }

    private void applySort(co.elastic.clients.elasticsearch.core.SearchRequest.Builder s, String sort, String index) {
        if (sort == null || sort.isBlank() || "relevance".equalsIgnoreCase(sort)) {
            return;
        }
        switch (sort.toLowerCase(Locale.ROOT)) {
            case "latest" -> s.sort(so -> so.field(f -> f.field("createTime").order(SortOrder.Desc)));
            case "oldest" -> s.sort(so -> so.field(f -> f.field("createTime").order(SortOrder.Asc)));
            case "price_asc" -> s.sort(so -> so.field(f -> f.field("price").order(SortOrder.Asc)));
            case "price_desc" -> s.sort(so -> so.field(f -> f.field("price").order(SortOrder.Desc)));
            case "popular" -> {
                s.sort(so -> so.field(f -> f.field("viewCount").order(SortOrder.Desc)));
                s.sort(so -> so.field(f -> f.field("likeCount").order(SortOrder.Desc)));
            }
            default -> { /* 默认相关度排序 */ }
        }
    }

    private String resolveIndex(String type) {
        return switch (type) {
            case "post" -> postIndex;
            case "product" -> productIndex;
            case "activity" -> activityIndex;
            case "help" -> helpIndex;
            default -> null;
        };
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

    private List<String> buildSearchSuggestions(String keyword, SearchPayload payload, int limit,
                                                 List<String> esCandidates, List<String> phraseSuggestions) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        int resultCount = payload.posts.size() + payload.products.size() + payload.activities.size() + payload.helps.size();
        int suggestionLimit = resultCount >= Math.max(6, limit) ? 3 : 8;

        String normalized = keyword.trim().toLowerCase(Locale.ROOT);
        Set<String> candidates = new LinkedHashSet<>();
        candidates.addAll(safe(phraseSuggestions));
        candidates.addAll(recommendKeywords(Math.max(limit * 3, 20)));
        candidates.addAll(safe(esCandidates));
        candidates.addAll(extractPayloadTitles(payload));

        Map<String, Integer> scoreMap = new HashMap<>();
        for (String candidate : candidates) {
            if (candidate == null || candidate.isBlank()) {
                continue;
            }
            String c = candidate.trim();
            if (c.equalsIgnoreCase(keyword.trim())) {
                continue;
            }
            scoreMap.put(c, scoreSuggestion(normalized, c.toLowerCase(Locale.ROOT)));
        }

        return scoreMap.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey)
                .limit(suggestionLimit)
                .collect(Collectors.toList());
    }

    private List<String> collectEsSuggestionCandidates(String keyword, int limit) {
        if (!"es".equalsIgnoreCase(searchEngine) || elasticsearchClient == null || keyword == null || keyword.isBlank()) {
            return List.of();
        }

        int size = Math.max(6, limit * 2);
        Set<String> titles = new LinkedHashSet<>();

        try {
            List<String> indexes = Arrays.asList(postIndex, productIndex, activityIndex, helpIndex);
            for (String index : indexes) {
                SearchResponse<Map> response = elasticsearchClient.search(s -> s
                                .index(index)
                                .size(size)
                                .query(q -> q.match(m -> m
                                        .field("title")
                                        .query(keyword)
                                        .analyzer("standard")
                                        .fuzziness("AUTO")
                                        .prefixLength(1)
                                )),
                        Map.class);

                response.hits().hits().forEach(hit -> {
                    Map source = hit.source();
                    if (source != null) {
                        Object title = source.get("title");
                        if (title != null) {
                            String normalized = normalizeTitleKeyword(String.valueOf(title));
                            if (normalized != null) {
                                titles.add(normalized);
                            }
                        }
                    }
                });
            }
        } catch (Exception ex) {
            log.debug("collect ES suggestion candidates failed: {}", ex.getMessage());
        }

        return titles.stream().limit(size).collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildMergedResults(String keyword, SearchPayload payload, int limit) {
        List<Map<String, Object>> merged = new ArrayList<>();
        String kw = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);

        collectMergedItems(merged, payload.posts, "post", kw);
        collectMergedItems(merged, payload.products, "product", kw);
        collectMergedItems(merged, payload.activities, "activity", kw);
        collectMergedItems(merged, payload.helps, "help", kw);

        merged.sort((a, b) -> Double.compare(readScore(b), readScore(a)));
        int max = Math.max(limit * 2, 10);
        if (merged.size() > max) {
            return merged.subList(0, max);
        }
        return merged;
    }

    private void collectMergedItems(List<Map<String, Object>> output, List<Map<String, Object>> source, String type, String kwLower) {
        List<Map<String, Object>> safeSource = safe(source);
        for (int i = 0; i < safeSource.size(); i++) {
            Map<String, Object> item = safeSource.get(i);
            Map<String, Object> merged = new HashMap<>();
            merged.put("id", parseLong(item.get("id")));
            merged.put("type", type);
            merged.put("title", stringValue(item.get("title")));
            merged.put("snippet", resolveMergedSnippet(item, type));
            merged.put("meta", resolveMergedMeta(item, type));

            Object highlight = item.get("_highlight");
            if (highlight != null) {
                merged.put("_highlight", highlight);
            }

            double score = readScore(item);
            if (score == 0D) {
                score = computeFallbackScore(item, type, i, kwLower);
            }
            merged.put("_score", score);
            output.add(merged);
        }
    }

    private double computeFallbackScore(Map<String, Object> item, String type, int idx, String kwLower) {
        double score = 0D;
        score += switch (type) {
            case "post" -> 1.1D;
            case "product" -> 1.0D;
            case "activity" -> 0.95D;
            case "help" -> 0.9D;
            default -> 0D;
        };

        if (kwLower != null && !kwLower.isBlank()) {
            String title = stringValue(item.get("title"));
            if (title != null && title.toLowerCase(Locale.ROOT).contains(kwLower)) {
                score += 2.0D;
            }
            String snippet = resolveMergedSnippet(item, type);
            if (snippet != null && snippet.toLowerCase(Locale.ROOT).contains(kwLower)) {
                score += 1.0D;
            }
        }
        score -= idx * 0.02D;
        return score;
    }

    private String resolveMergedSnippet(Map<String, Object> item, String type) {
        return switch (type) {
            case "post" -> firstNonBlank(stringValue(item.get("content")), stringValue(item.get("title")));
            case "product" -> firstNonBlank(stringValue(item.get("description")), stringValue(item.get("title")));
            case "activity" -> firstNonBlank(stringValue(item.get("description")), stringValue(item.get("location")), stringValue(item.get("title")));
            case "help" -> firstNonBlank(stringValue(item.get("description")), stringValue(item.get("location")), stringValue(item.get("title")));
            default -> stringValue(item.get("title"));
        };
    }

    private String resolveMergedMeta(Map<String, Object> item, String type) {
        return switch (type) {
            case "post" -> firstNonBlank(stringValue(item.get("sectionName")), readNestedString(item, "author", "nickname"));
            case "product" -> {
                String price = item.get("price") == null ? null : "¥" + item.get("price");
                yield firstNonBlank(price, readNestedString(item, "seller", "nickname"));
            }
            case "activity" -> firstNonBlank(stringValue(item.get("time")), stringValue(item.get("location")));
            case "help" -> firstNonBlank(stringValue(item.get("location")), stringValue(item.get("time")));
            default -> null;
        };
    }

    private double readScore(Map<String, Object> item) {
        Object raw = item.get("_score");
        if (raw instanceof Number number) {
            return number.doubleValue();
        }
        return 0D;
    }

    private List<String> extractPayloadTitles(SearchPayload payload) {
        Set<String> words = new LinkedHashSet<>();
        collectTitles(payload.posts, words);
        collectTitles(payload.products, words);
        collectTitles(payload.activities, words);
        collectTitles(payload.helps, words);
        return new ArrayList<>(words);
    }

    private void collectTitles(List<Map<String, Object>> records, Set<String> out) {
        for (Map<String, Object> item : safe(records)) {
            String title = normalizeTitleKeyword(stringValue(item.get("title")));
            if (title != null) {
                out.add(title);
            }
        }
    }

    private int scoreSuggestion(String keyword, String candidate) {
        if (keyword.isBlank() || candidate.isBlank()) {
            return 0;
        }

        int score = 0;
        boolean hasContainsHit = false;
        if (candidate.contains(keyword)) {
            score += 80;
            hasContainsHit = true;
        }
        if (keyword.contains(candidate)) {
            score += 40;
            hasContainsHit = true;
        }

        String[] keywordParts = keyword.split("[\\s,，;；|/]+");
        int hitParts = 0;
        for (String part : keywordParts) {
            String token = part.trim();
            if (!token.isEmpty() && candidate.contains(token)) {
                hitParts++;
            }
        }
        score += hitParts * 20;

        int distance = levenshtein(keyword, candidate);
        boolean shortDistance = distance <= 2;
        if (!hasContainsHit && hitParts == 0 && !shortDistance) {
            return 0;
        }
        score += Math.max(0, 24 - distance * 4);
        return score;
    }

    private int levenshtein(String a, String b) {
        int n = a.length();
        int m = b.length();
        if (n == 0) {
            return m;
        }
        if (m == 0) {
            return n;
        }

        int[][] dp = new int[n + 1][m + 1];
        for (int i = 0; i <= n; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= m; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[n][m];
    }

    @SuppressWarnings("unchecked")
    private void appendHighlight(Map<String, Object> target, Map<String, Object> source, String... allowFields) {
        Object raw = source.get("_highlight");
        if (!(raw instanceof Map<?, ?> highlightMap)) {
            return;
        }

        Map<String, String> normalized = new LinkedHashMap<>();
        for (String field : allowFields) {
            Object value = ((Map<String, Object>) highlightMap).get(field);
            if (value != null) {
                String text = String.valueOf(value);
                if (!text.isBlank()) {
                    normalized.put(field, text);
                }
            }
        }

        if (!normalized.isEmpty()) {
            target.put("_highlight", normalized);
        }
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
            appendHighlight(item, doc, "title", "content");
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
            appendHighlight(item, doc, "title", "description");
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
            appendHighlight(item, doc, "title", "description", "location");
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
            appendHighlight(item, doc, "title", "description", "pickupLocation", "deliveryLocation", "location");
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
        private final Map<String, Object> aggregations;

        private SearchPayload(List<Map<String, Object>> posts,
                              List<Map<String, Object>> products,
                              List<Map<String, Object>> activities,
                              List<Map<String, Object>> helps,
                              Map<String, Object> aggregations) {
            this.posts = Objects.requireNonNullElse(posts, List.of());
            this.products = Objects.requireNonNullElse(products, List.of());
            this.activities = Objects.requireNonNullElse(activities, List.of());
            this.helps = Objects.requireNonNullElse(helps, List.of());
            this.aggregations = aggregations;
        }
    }
}
