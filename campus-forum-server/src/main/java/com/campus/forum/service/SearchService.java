package com.campus.forum.service;

import java.util.List;
import java.util.Map;

public interface SearchService {

    Map<String, Object> searchAll(String keyword, Integer size);

    List<String> recommendKeywords(Integer size);
}
