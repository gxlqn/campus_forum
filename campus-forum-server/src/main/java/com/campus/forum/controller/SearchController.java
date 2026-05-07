package com.campus.forum.controller;

import com.campus.forum.common.Result;
import com.campus.forum.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * 基础搜索（兼容旧接口）
     */
    @GetMapping
    public Result<Map<String, Object>> searchAll(@RequestParam String keyword,
                                                 @RequestParam(defaultValue = "5") Integer size) {
        return Result.success(searchService.searchAll(keyword, size));
    }

    /**
     * 高级搜索：支持板块过滤、价格区间、排序
     * @param keyword    搜索关键词（可为空，配合 filter 使用）
     * @param size       返回数量
     * @param sectionName 板块名称过滤（Filter Context，不参与评分）
     * @param priceRange 价格区间过滤，格式: "50-200" / "500+" / "0-50"
     * @param sort       排序方式: relevance(默认) / latest / oldest / price_asc / price_desc / popular
     */
    @GetMapping("/advanced")
    public Result<Map<String, Object>> searchAdvanced(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "5") Integer size,
            @RequestParam(required = false) String sectionName,
            @RequestParam(required = false) String priceRange,
            @RequestParam(defaultValue = "relevance") String sort) {
        return Result.success(searchService.searchAdvanced(keyword, size, sectionName, priceRange, sort));
    }

    /**
     * Completion Suggester：输入即提示自动补全
     * 用户在小程序输入框每打一个字就弹出候选词
     */
    @GetMapping("/suggest")
    public Result<List<String>> suggestCompletion(@RequestParam String prefix,
                                                   @RequestParam(defaultValue = "8") Integer size) {
        return Result.success(searchService.suggestCompletion(prefix, size));
    }

    /**
     * Phrase Suggester：纠错建议 / Did you mean?
     * 搜索 "考妍" 自动建议 "考研"
     */
    @GetMapping("/phrase-suggest")
    public Result<List<String>> phraseSuggest(@RequestParam String keyword) {
        return Result.success(searchService.phraseSuggest(keyword));
    }

    /**
     * More Like This：相似推荐
     * 帖子详情页底部"相关推荐"模块
     */
    @GetMapping("/more-like-this")
    public Result<List<Map<String, Object>>> moreLikeThis(
            @RequestParam String type,
            @RequestParam Long id,
            @RequestParam(defaultValue = "5") Integer size) {
        return Result.success(searchService.moreLikeThis(type, id, size));
    }

    /**
     * Aggregations：聚合分析
     * 返回板块分布、价格区间、时间分布等聚合数据
     */
    @GetMapping("/aggregations")
    public Result<Map<String, Object>> aggregations(@RequestParam String keyword,
                                                     @RequestParam(defaultValue = "5") Integer size) {
        return Result.success(searchService.getAggregations(keyword, size));
    }

    /**
     * 推荐关键词
     */
    @GetMapping("/recommend")
    public Result<List<String>> recommend(@RequestParam(defaultValue = "10") Integer size) {
        return Result.success(searchService.recommendKeywords(size));
    }
}
