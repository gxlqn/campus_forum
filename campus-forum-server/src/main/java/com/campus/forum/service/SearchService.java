package com.campus.forum.service;

import java.util.List;
import java.util.Map;

public interface SearchService {

    Map<String, Object> searchAll(String keyword, Integer size);

    /**
     * 高级搜索：支持板块过滤、价格区间、排序
     */
    Map<String, Object> searchAdvanced(String keyword, Integer size,
                                        String sectionName, String priceRange, String sort);

    /**
     * Completion Suggester：输入即提示自动补全
     */
    List<String> suggestCompletion(String prefix, Integer size);

    /**
     * Phrase Suggester：纠错建议 / Did you mean?
     */
    List<String> phraseSuggest(String keyword);

    /**
     * More Like This：相似推荐
     */
    List<Map<String, Object>> moreLikeThis(String type, Long id, Integer size);

    /**
     * 聚合分析：板块分布、价格区间、时间分布
     */
    Map<String, Object> getAggregations(String keyword, Integer size);

    List<String> recommendKeywords(Integer size);
}
