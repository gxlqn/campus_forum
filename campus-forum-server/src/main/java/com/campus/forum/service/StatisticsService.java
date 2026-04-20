package com.campus.forum.service;

import java.util.Map;

public interface StatisticsService {

    Map<String, Object> getOverview();

    Map<String, Object> getUserStatistics();

    Map<String, Object> getServiceStatistics();

    Map<String, Object> getForumStatistics();
}