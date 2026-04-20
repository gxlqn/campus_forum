package com.campus.forum.service;

import com.campus.forum.common.PageResult;

import java.util.List;
import java.util.Map;

public interface AdminInfoService {

    PageResult<Map<String, Object>> getNewsList(Long current, Long size, String category, String keyword, Integer status);

    Map<String, Object> getNewsDetail(Long id);

    Long createNews(Map<String, Object> payload);

    void updateNews(Long id, Map<String, Object> payload);

    void updateNewsStatus(Long id, Integer status);

    void deleteNews(Long id);

    PageResult<Map<String, Object>> getNavList(Long current, Long size, String category, String keyword, Integer status);

    Long createNav(Map<String, Object> payload);

    void updateNav(Long id, Map<String, Object> payload);

    void updateNavStatus(Long id, Integer status);

    void deleteNav(Long id);

    List<String> getNewsCategories();

    List<String> getNavCategories();
}