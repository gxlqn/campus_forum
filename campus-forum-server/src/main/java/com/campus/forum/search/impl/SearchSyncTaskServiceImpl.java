package com.campus.forum.search.impl;

import com.campus.forum.common.PageResult;
import com.campus.forum.common.ResultCode;
import com.campus.forum.entity.SearchSyncTask;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.SearchSyncTaskMapper;
import com.campus.forum.search.SearchSyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchSyncTaskServiceImpl implements SearchSyncTaskService {

    @Autowired
    private SearchSyncTaskMapper taskMapper;

    @Override
    public PageResult<SearchSyncTask> getTasks(Long current, Long size, Integer status, String entityType) {
        long pageNo = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 10 : Math.min(size, 100);
        long offset = (pageNo - 1) * pageSize;

        List<SearchSyncTask> records = taskMapper.selectPage(status, entityType, offset, pageSize);
        Long total = taskMapper.countPage(status, entityType);
        return new PageResult<>(pageNo, pageSize, total == null ? 0L : total, records);
    }

    @Override
    public void retryTask(Long taskId) {
        if (taskId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "任务ID不能为空");
        }
        int changed = taskMapper.retryNow(taskId);
        if (changed == 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "仅失败或处理中任务可重试");
        }
    }
}
