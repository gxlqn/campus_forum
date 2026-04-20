package com.campus.forum.controller;

import com.campus.forum.common.PageResult;
import com.campus.forum.common.Result;
import com.campus.forum.entity.SysNotice;
import com.campus.forum.entity.SysUser;
import com.campus.forum.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notices")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @GetMapping
    public Result<PageResult<SysNotice>> getNoticeList(
            @RequestParam(required = false) Long current,
            @RequestParam(required = false) Long page,
            @RequestParam(defaultValue = "10") Long size,
            @AuthenticationPrincipal SysUser currentUser) {
        Long pageNo = current != null ? current : page;
        if (pageNo == null) {
            pageNo = 1L;
        }
        return Result.success(noticeService.getNoticeList(currentUser.getId(), pageNo, size));
    }

    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(@AuthenticationPrincipal SysUser currentUser) {
        return Result.success(noticeService.getUnreadCount(currentUser.getId()));
    }

    @PostMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id, @AuthenticationPrincipal SysUser currentUser) {
        noticeService.markAsRead(id, currentUser.getId());
        return Result.success();
    }

    @PostMapping("/read-all")
    public Result<Void> markAllAsRead(@AuthenticationPrincipal SysUser currentUser) {
        noticeService.markAllAsRead(currentUser.getId());
        return Result.success();
    }
}