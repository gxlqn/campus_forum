package com.campus.forum.controller;

import com.campus.forum.common.PageResult;
import com.campus.forum.common.Result;
import com.campus.forum.entity.ServiceLostFound;
import com.campus.forum.entity.SysUser;
import com.campus.forum.service.LostFoundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lostfound")
public class LostFoundController {

    @Autowired
    private LostFoundService lostFoundService;

    @GetMapping
    public Result<PageResult<ServiceLostFound>> getList(
            @RequestParam(required = false) Long current,
            @RequestParam(required = false) Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String keyword) {
        Long pageNo = current != null ? current : page;
        if (pageNo == null) {
            pageNo = 1L;
        }
        return Result.success(lostFoundService.getList(pageNo, size, type, keyword));
    }

    @GetMapping("/{id}")
    public Result<ServiceLostFound> getDetail(@PathVariable Long id) {
        return Result.success(lostFoundService.getDetail(id));
    }

    @PostMapping
    public Result<ServiceLostFound> create(@RequestBody ServiceLostFound item,
            @AuthenticationPrincipal SysUser currentUser) {
        item.setUserId(currentUser.getId());
        return Result.success(lostFoundService.create(item));
    }

    @PutMapping("/{id}")
    public Result<ServiceLostFound> update(@PathVariable Long id,
            @RequestBody ServiceLostFound item,
            @AuthenticationPrincipal SysUser currentUser) {
        item.setId(id);
        return Result.success(lostFoundService.update(item, currentUser.getId()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
            @AuthenticationPrincipal SysUser currentUser) {
        lostFoundService.delete(id, currentUser.getId());
        return Result.success();
    }

    @PostMapping("/{id}/complete")
    public Result<Void> markComplete(@PathVariable Long id,
            @AuthenticationPrincipal SysUser currentUser) {
        lostFoundService.markComplete(id, currentUser.getId());
        return Result.success();
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('lostfound:manage')")
    @GetMapping("/admin")
    public Result<PageResult<ServiceLostFound>> getAdminList(
            @RequestParam(required = false) Long current,
            @RequestParam(required = false) Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) String keyword) {
        Long pageNo = current != null ? current : page;
        if (pageNo == null) {
            pageNo = 1L;
        }
        return Result.success(lostFoundService.getAdminList(pageNo, size, type, status, auditStatus, keyword));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('lostfound:manage')")
    @PostMapping("/{id}/audit")
    public Result<Void> auditItem(@PathVariable Long id,
            @RequestParam Integer auditStatus) {
        lostFoundService.auditItem(id, auditStatus);
        return Result.success();
    }

    /** 管理员删除（不限制 owner） */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('lostfound:manage')")
    @DeleteMapping("/admin/{id}")
    public Result<Void> deleteById(@PathVariable Long id) {
        lostFoundService.deleteById(id);
        return Result.success();
    }

    @PostMapping("/{id}/claim")
    public Result<Void> submitClaim(@PathVariable Long id,
            @RequestBody com.campus.forum.entity.ServiceLostFoundClaim claim,
            @AuthenticationPrincipal SysUser currentUser) {
        lostFoundService.submitClaim(id, currentUser.getId(), claim.getDescription(), claim.getImages());
        return Result.success();
    }

    @GetMapping("/claims")
    public Result<PageResult<java.util.Map<String, Object>>> getClaimList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer status) {
        return Result.success(lostFoundService.getClaimList(current, size, status));
    }

    @PostMapping("/claims/{claimId}/audit")
    public Result<Void> auditClaim(@PathVariable Long claimId,
            @RequestBody com.campus.forum.dto.admin.AuditActionRequest request,
            @AuthenticationPrincipal SysUser currentUser) {
        lostFoundService.auditClaim(claimId, currentUser.getId(), request.getAuditStatus(), request.getAuditRemark());
        return Result.success();
    }
}
