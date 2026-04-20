package com.campus.forum.controller;

import com.campus.forum.common.Result;
import com.campus.forum.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        return Result.success(statisticsService.getOverview());
    }

    @GetMapping("/user")
    public Result<Map<String, Object>> getUserStatistics() {
        return Result.success(statisticsService.getUserStatistics());
    }

    @GetMapping("/service")
    public Result<Map<String, Object>> getServiceStatistics() {
        return Result.success(statisticsService.getServiceStatistics());
    }

    @GetMapping("/forum")
    public Result<Map<String, Object>> getForumStatistics() {
        return Result.success(statisticsService.getForumStatistics());
    }
}