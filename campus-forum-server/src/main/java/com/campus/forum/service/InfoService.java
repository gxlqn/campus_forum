package com.campus.forum.service;

import java.util.List;
import java.util.Map;

public interface InfoService {

    Map<String, Object> getNewsList(Long current, Long size, String category, String keyword);

    Map<String, Object> getNewsDetail(Long id);

    List<String> getNewsCategories();

    List<Map<String, Object>> getServiceNavList(String category, String keyword);

    List<String> getServiceNavCategories();
}
