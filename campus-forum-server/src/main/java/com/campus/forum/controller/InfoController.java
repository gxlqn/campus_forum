package com.campus.forum.controller;

import com.campus.forum.common.Result;
import com.campus.forum.service.InfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/info")
public class InfoController {

    @Autowired
    private InfoService infoService;

    @GetMapping("/news")
    public Result<Map<String, Object>> getNewsList(
            @RequestParam(required = false) Long current,
            @RequestParam(required = false) Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        Long pageNo = current != null ? current : page;
        if (pageNo == null) {
            pageNo = 1L;
        }
        return Result.success(infoService.getNewsList(pageNo, size, category, keyword));
    }

    @GetMapping("/news/categories")
    public Result<List<String>> getNewsCategories() {
        return Result.success(infoService.getNewsCategories());
    }

    @GetMapping("/news/{id}")
    public Result<Map<String, Object>> getNewsDetail(@PathVariable Long id) {
        return Result.success(infoService.getNewsDetail(id));
    }

    @GetMapping("/nav")
    public Result<List<Map<String, Object>>> getServiceNavList(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        return Result.success(infoService.getServiceNavList(category, keyword));
    }

    @GetMapping("/nav/categories")
    public Result<List<String>> getServiceNavCategories() {
        return Result.success(infoService.getServiceNavCategories());
    }
}
