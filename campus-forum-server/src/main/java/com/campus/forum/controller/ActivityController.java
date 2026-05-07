package com.campus.forum.controller;

import com.campus.forum.common.PageResult;
import com.campus.forum.common.Result;
import com.campus.forum.entity.ServiceActivity;
import com.campus.forum.entity.SysUser;
import com.campus.forum.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/activities")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @GetMapping
    public Result<PageResult<ServiceActivity>> getList(
            @RequestParam(required = false) Long current,
            @RequestParam(required = false) Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {
        Long pageNo = current != null ? current : page;
        if (pageNo == null) {
            pageNo = 1L;
        }
        return Result.success(activityService.getActivityList(pageNo, size, type, keyword));
    }

    @GetMapping("/{id}")
    public Result<ServiceActivity> getDetail(@PathVariable Long id,
            @AuthenticationPrincipal SysUser currentUser) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        return Result.success(activityService.getActivityDetail(id, userId));
    }

    @PostMapping
    public Result<ServiceActivity> create(@RequestBody ServiceActivity activity,
            @AuthenticationPrincipal SysUser currentUser) {
        activity.setUserId(currentUser.getId());
        return Result.success(activityService.createActivity(activity));
    }

    @PostMapping("/{id}/signup")
    public Result<Void> signup(@PathVariable Long id,
            @AuthenticationPrincipal SysUser currentUser) {
        activityService.signupActivity(id, currentUser.getId());
        return Result.success();
    }

    @PostMapping("/{id}/cancel-signup")
    public Result<Void> cancelSignup(@PathVariable Long id,
            @AuthenticationPrincipal SysUser currentUser) {
        activityService.cancelSignup(id, currentUser.getId());
        return Result.success();
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('activity:manage')")
    @GetMapping("/admin")
    public Result<PageResult<ServiceActivity>> getAdminList(
            @RequestParam(required = false) Long current,
            @RequestParam(required = false) Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) String keyword) {
        Long pageNo = current != null ? current : page;
        if (pageNo == null) {
            pageNo = 1L;
        }
        return Result.success(activityService.getAdminActivityList(pageNo, size, type, status, auditStatus, keyword));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('activity:manage')")
    @PostMapping("/{id}/audit")
    public Result<Void> auditActivity(@PathVariable Long id,
            @RequestParam Integer auditStatus) {
        activityService.auditActivity(id, auditStatus);
        return Result.success();
    }
}
