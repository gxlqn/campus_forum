package com.campus.forum.service.impl;

import com.campus.forum.common.PageResult;
import com.campus.forum.common.ResultCode;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.InfoMapper;
import com.campus.forum.service.InfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class InfoServiceImpl implements InfoService {

    @Autowired
    private InfoMapper infoMapper;

    @Override
    public Map<String, Object> getNewsList(Long current, Long size, String category, String keyword) {
        long pageNo = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 10 : size;
        long offset = (pageNo - 1) * pageSize;

        List<Map<String, Object>> records = infoMapper.selectNewsPage(category, keyword, offset, pageSize);
        Long total = infoMapper.countNewsPage(category, keyword);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("page", new PageResult<>(pageNo, pageSize, total == null ? 0L : total, records));
        data.put("categories", infoMapper.selectNewsCategories());
        return data;
    }

    @Override
    public Map<String, Object> getNewsDetail(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_MISSING);
        }
        Map<String, Object> news = infoMapper.selectNewsById(id);
        if (news == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "资讯不存在");
        }
        infoMapper.increaseNewsViewCount(id);
        Object viewCount = news.get("viewCount");
        if (viewCount instanceof Number) {
            news.put("viewCount", ((Number) viewCount).longValue() + 1L);
        }
        return news;
    }

    @Override
    public List<String> getNewsCategories() {
        return infoMapper.selectNewsCategories();
    }

    @Override
    public List<Map<String, Object>> getServiceNavList(String category, String keyword) {
        return infoMapper.selectServiceNavList(category, keyword);
    }

    @Override
    public List<String> getServiceNavCategories() {
        return infoMapper.selectServiceNavCategories();
    }
}
