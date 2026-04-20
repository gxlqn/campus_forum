package com.campus.forum.service.impl;

import com.campus.forum.common.PageResult;
import com.campus.forum.common.ResultCode;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.InfoMapper;
import com.campus.forum.service.AdminInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
public class AdminInfoServiceImpl implements AdminInfoService {

    @Autowired
    private InfoMapper infoMapper;

    @Override
    public PageResult<Map<String, Object>> getNewsList(Long current, Long size, String category, String keyword, Integer status) {
        long pageNo = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 10 : size;
        long offset = (pageNo - 1) * pageSize;

        List<Map<String, Object>> records = infoMapper.selectAdminNewsPage(category, keyword, status, offset, pageSize);
        Long total = infoMapper.countAdminNewsPage(category, keyword, status);
        return new PageResult<>(pageNo, pageSize, total == null ? 0L : total, records);
    }

    @Override
    public Map<String, Object> getNewsDetail(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_MISSING);
        }
        Map<String, Object> row = infoMapper.selectAdminNewsById(id);
        if (row == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "资讯不存在");
        }
        return row;
    }

    @Override
    public Long createNews(Map<String, Object> payload) {
        validateNewsPayload(payload);
        int changed = infoMapper.insertAdminNews(payload);
        if (changed == 0) {
            throw new BusinessException(ResultCode.ERROR, "创建资讯失败");
        }
        Object id = payload.get("id");
        return id instanceof Number ? ((Number) id).longValue() : null;
    }

    @Override
    public void updateNews(Long id, Map<String, Object> payload) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_MISSING);
        }
        validateNewsPayload(payload);
        payload.put("id", id);
        int changed = infoMapper.updateAdminNews(payload);
        if (changed == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "资讯不存在");
        }
    }

    @Override
    public void updateNewsStatus(Long id, Integer status) {
        if (id == null || status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "状态参数无效");
        }
        int changed = infoMapper.updateAdminNewsStatus(id, status);
        if (changed == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "资讯不存在");
        }
    }

    @Override
    public void deleteNews(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_MISSING);
        }
        int changed = infoMapper.deleteAdminNews(id);
        if (changed == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "资讯不存在");
        }
    }

    @Override
    public PageResult<Map<String, Object>> getNavList(Long current, Long size, String category, String keyword, Integer status) {
        long pageNo = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 10 : size;
        long offset = (pageNo - 1) * pageSize;

        List<Map<String, Object>> records = infoMapper.selectAdminNavPage(category, keyword, status, offset, pageSize);
        Long total = infoMapper.countAdminNavPage(category, keyword, status);
        return new PageResult<>(pageNo, pageSize, total == null ? 0L : total, records);
    }

    @Override
    public Long createNav(Map<String, Object> payload) {
        validateNavPayload(payload);
        int changed = infoMapper.insertAdminNav(payload);
        if (changed == 0) {
            throw new BusinessException(ResultCode.ERROR, "创建导航失败");
        }
        Object id = payload.get("id");
        return id instanceof Number ? ((Number) id).longValue() : null;
    }

    @Override
    public void updateNav(Long id, Map<String, Object> payload) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_MISSING);
        }
        validateNavPayload(payload);
        payload.put("id", id);
        int changed = infoMapper.updateAdminNav(payload);
        if (changed == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "导航不存在");
        }
    }

    @Override
    public void updateNavStatus(Long id, Integer status) {
        if (id == null || status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "状态参数无效");
        }
        int changed = infoMapper.updateAdminNavStatus(id, status);
        if (changed == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "导航不存在");
        }
    }

    @Override
    public void deleteNav(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_MISSING);
        }
        int changed = infoMapper.deleteAdminNav(id);
        if (changed == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "导航不存在");
        }
    }

    @Override
    public List<String> getNewsCategories() {
        return infoMapper.selectAdminNewsCategories();
    }

    @Override
    public List<String> getNavCategories() {
        return infoMapper.selectAdminNavCategories();
    }

    private void validateNewsPayload(Map<String, Object> payload) {
        if (payload == null || !StringUtils.hasText(String.valueOf(payload.get("title")))) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "资讯标题不能为空");
        }
        if (payload.get("status") == null) {
            payload.put("status", 1);
        }
        if (payload.get("isTop") == null) {
            payload.put("isTop", 0);
        }
    }

    private void validateNavPayload(Map<String, Object> payload) {
        if (payload == null
                || !StringUtils.hasText(String.valueOf(payload.get("name")))
                || !StringUtils.hasText(String.valueOf(payload.get("category")))) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "导航名称和分类不能为空");
        }
        if (payload.get("status") == null) {
            payload.put("status", 1);
        }
        if (payload.get("sort") == null) {
            payload.put("sort", 0);
        }
    }
}