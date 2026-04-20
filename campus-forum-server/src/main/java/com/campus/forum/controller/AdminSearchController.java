package com.campus.forum.controller;

import com.campus.forum.common.Result;
import com.campus.forum.common.PageResult;
import com.campus.forum.entity.SearchSyncTask;
import com.campus.forum.search.SearchIndexService;
import com.campus.forum.search.SearchSyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/search")
public class AdminSearchController {

    @Autowired
    private SearchIndexService searchIndexService;

    @Autowired
    private SearchSyncTaskService searchSyncTaskService;

    @PostMapping("/reindex")
    public Result<Map<String, Object>> rebuildAllIndices() {
        return Result.success(searchIndexService.rebuildAll());
    }

    @GetMapping("/sync-tasks")
    public Result<PageResult<SearchSyncTask>> getSyncTasks(
            @RequestParam(required = false) Long current,
            @RequestParam(required = false) Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String entityType) {
        Long pageNo = current != null ? current : page;
        if (pageNo == null) {
            pageNo = 1L;
        }
        return Result.success(searchSyncTaskService.getTasks(pageNo, size, status, entityType));
    }

    @PostMapping("/sync-tasks/{taskId}/retry")
    public Result<Void> retrySyncTask(@PathVariable Long taskId) {
        searchSyncTaskService.retryTask(taskId);
        return Result.success();
    }
}
