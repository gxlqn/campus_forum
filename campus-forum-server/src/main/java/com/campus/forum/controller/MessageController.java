package com.campus.forum.controller;

import com.campus.forum.common.Result;
import com.campus.forum.dto.message.SendMessageRequest;
import com.campus.forum.entity.SysUser;
import com.campus.forum.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/notifications")
    public Result<Map<String, Object>> getNotifications(
            @AuthenticationPrincipal SysUser currentUser,
            @RequestParam(required = false) Long current,
            @RequestParam(required = false) Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer isRead) {
        Long pageNo = current != null ? current : page;
        if (pageNo == null) {
            pageNo = 1L;
        }
        return Result.success(messageService.getNotifications(currentUser.getId(), pageNo, size, type, isRead));
    }

    @GetMapping("/notifications/{id}")
    public Result<Map<String, Object>> getNotificationDetail(
            @AuthenticationPrincipal SysUser currentUser,
            @PathVariable Long id) {
        return Result.success(messageService.getNotificationDetail(currentUser.getId(), id));
    }

    @PatchMapping("/notifications/{id}/read")
    public Result<Void> markNotificationRead(
            @AuthenticationPrincipal SysUser currentUser,
            @PathVariable Long id) {
        messageService.markNotificationRead(currentUser.getId(), id);
        return Result.success();
    }

    @PatchMapping("/notifications/read-all")
    public Result<Void> markAllNotificationsRead(@AuthenticationPrincipal SysUser currentUser) {
        messageService.markAllNotificationsRead(currentUser.getId());
        return Result.success();
    }

    @DeleteMapping("/notifications/system")
    public Result<Void> clearSystemNotifications(@AuthenticationPrincipal SysUser currentUser,
                                                 @RequestParam(defaultValue = "false") Boolean onlyRead) {
        messageService.clearSystemNotifications(currentUser.getId(), onlyRead);
        return Result.success();
    }

    @GetMapping("/conversations")
    public Result<Map<String, Object>> getConversations(
            @AuthenticationPrincipal SysUser currentUser,
            @RequestParam(required = false) Long current,
            @RequestParam(required = false) Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword) {
        Long pageNo = current != null ? current : page;
        if (pageNo == null) {
            pageNo = 1L;
        }
        return Result.success(messageService.getConversations(currentUser.getId(), pageNo, size, keyword));
    }

    @GetMapping("/conversations/{conversationId}")
    public Result<Map<String, Object>> getConversationMessages(
            @AuthenticationPrincipal SysUser currentUser,
            @PathVariable String conversationId,
            @RequestParam(required = false) Long current,
            @RequestParam(required = false) Long page,
            @RequestParam(defaultValue = "20") Long size) {
        Long pageNo = current != null ? current : page;
        if (pageNo == null) {
            pageNo = 1L;
        }
        return Result.success(messageService.getConversationMessages(currentUser.getId(), conversationId, pageNo, size));
    }

    @PatchMapping("/conversations/{conversationId}/read")
    public Result<Void> markConversationRead(
            @AuthenticationPrincipal SysUser currentUser,
            @PathVariable String conversationId) {
        messageService.markConversationRead(currentUser.getId(), conversationId);
        return Result.success();
    }

    @PostMapping("/send")
    public Result<Map<String, Object>> sendMessage(
            @AuthenticationPrincipal SysUser currentUser,
            @RequestBody SendMessageRequest request) {
        return Result.success(messageService.sendMessage(currentUser.getId(), request));
    }
}
