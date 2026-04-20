package com.campus.forum.search;

import com.campus.forum.common.PageResult;
import com.campus.forum.entity.SearchSyncTask;

public interface SearchSyncTaskService {

    PageResult<SearchSyncTask> getTasks(Long current, Long size, Integer status, String entityType);

    void retryTask(Long taskId);
}
