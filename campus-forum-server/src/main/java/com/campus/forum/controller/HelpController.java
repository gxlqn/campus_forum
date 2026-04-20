package com.campus.forum.controller;

import com.campus.forum.common.PageResult;
import com.campus.forum.common.Result;
import com.campus.forum.entity.ServiceHelpRequest;
import com.campus.forum.entity.SysUser;
import com.campus.forum.service.HelpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/help")
public class HelpController {

    @Autowired
    private HelpService helpService;

    @GetMapping
    public Result<PageResult<ServiceHelpRequest>> getHelpList(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String keyword) {
        return Result.success(helpService.getHelpList(page, size, type, keyword));
    }

    @GetMapping("/{id}")
    public Result<ServiceHelpRequest> getHelpDetail(@PathVariable Long id) {
        return Result.success(helpService.getHelpDetail(id));
    }

    @PostMapping
    public Result<ServiceHelpRequest> publishHelp(@RequestBody ServiceHelpRequest request,
                                                   @AuthenticationPrincipal SysUser currentUser) {
        request.setUserId(currentUser.getId());
        return Result.success(helpService.publishHelp(request));
    }

    @PostMapping("/{id}/accept")
    public Result<Void> acceptHelp(@PathVariable Long id,
                                    @AuthenticationPrincipal SysUser currentUser) {
        helpService.acceptHelp(id, currentUser.getId());
        return Result.success();
    }

    @PostMapping("/{id}/complete")
    public Result<Void> completeHelp(@PathVariable Long id,
                                      @AuthenticationPrincipal SysUser currentUser) {
        helpService.completeOrder(id, currentUser.getId());
        return Result.success();
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancelHelp(@PathVariable Long id,
                                    @AuthenticationPrincipal SysUser currentUser) {
        helpService.publisherCancelOrder(id, currentUser.getId());
        return Result.success();
    }

    @PostMapping("/{id}/publisher-confirm")
    public Result<Void> publisherConfirm(@PathVariable Long id,
                                         @AuthenticationPrincipal SysUser currentUser,
                                         @RequestParam(defaultValue = "0") Integer isComplaint) {
        helpService.publisherConfirm(id, currentUser.getId(), isComplaint);
        return Result.success();
    }

    @PostMapping("/{id}/helper-appeal")
    public Result<Void> helperAppeal(@PathVariable Long id,
                                     @AuthenticationPrincipal SysUser currentUser) {
        helpService.helperAppeal(id, currentUser.getId());
        return Result.success();
    }

    @GetMapping("/admin")
    public Result<PageResult<ServiceHelpRequest>> getAdminHelpList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) String keyword) {
        return Result.success(helpService.getAdminHelpList(current, size, type, status, auditStatus, keyword));
    }

    @PostMapping("/admin/{id}/audit")
    public Result<Void> auditHelp(@PathVariable Long id,
                                   @RequestParam Integer auditStatus) {
        helpService.auditHelpRequest(id, auditStatus);
        return Result.success();
    }

    @GetMapping("/admin/arbitration/list")
    public Result<PageResult<ServiceHelpRequest>> getArbitrationList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(helpService.getArbitrationList(page, size));
    }

    @PostMapping("/admin/arbitration/resolve")
    public Result<Void> resolveArbitration(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer resolution = Integer.valueOf(params.get("resolution").toString());
        helpService.resolveArbitration(id, resolution);
        return Result.success();
    }
}
