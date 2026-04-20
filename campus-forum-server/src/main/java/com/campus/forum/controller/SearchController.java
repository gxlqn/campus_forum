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

    @GetMapping
    public Result<Map<String, Object>> searchAll(@RequestParam String keyword,
                                                 @RequestParam(defaultValue = "5") Integer size) {
        return Result.success(searchService.searchAll(keyword, size));
    }

    @GetMapping("/recommend")
    public Result<List<String>> recommend(@RequestParam(defaultValue = "10") Integer size) {
        return Result.success(searchService.recommendKeywords(size));
    }
}
