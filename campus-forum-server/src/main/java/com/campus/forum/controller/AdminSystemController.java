package com.campus.forum.controller;

import com.campus.forum.common.PageResult;
import com.campus.forum.common.Result;
import com.campus.forum.dto.admin.AuditActionRequest;
import com.campus.forum.dto.admin.HandleReportRequest;
import com.campus.forum.dto.admin.RolePermissionAssignRequest;
import com.campus.forum.dto.admin.StatusUpdateRequest;
import com.campus.forum.dto.admin.UserRoleAssignRequest;
import com.campus.forum.dto.admin.UserVerifyUpdateRequest;
import com.campus.forum.entity.ForumSection;
import com.campus.forum.entity.ServiceProductCategory;
import com.campus.forum.entity.SysPermission;
import com.campus.forum.entity.SysRole;
import com.campus.forum.entity.SysUser;
import com.campus.forum.entity.AuditSensitiveWord;
import com.campus.forum.service.AdminSystemService;
import com.campus.forum.service.ContentFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/system")
public class AdminSystemController {

    @Autowired
    private AdminSystemService adminSystemService;

    @Autowired
    private ContentFilterService contentFilterService;

    @GetMapping("/users")
    public Result<PageResult<Map<String, Object>>> getUsers(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer isVerified) {
        return Result.success(adminSystemService.getUsers(current, size, keyword, status, isVerified));
    }

    @PatchMapping("/users/{userId}/status")
    public Result<Void> updateUserStatus(@PathVariable Long userId, @RequestBody StatusUpdateRequest request) {
        adminSystemService.updateUserStatus(userId, request.getStatus());
        return Result.success();
    }

    @PatchMapping("/users/{userId}/verify")
    public Result<Void> updateUserVerify(@PathVariable Long userId, @RequestBody UserVerifyUpdateRequest request) {
        adminSystemService.updateUserVerify(userId, request.getIsVerified());
        return Result.success();
    }

    @PatchMapping("/users/{userId}/roles")
    public Result<Void> assignUserRoles(@PathVariable Long userId, @RequestBody UserRoleAssignRequest request) {
        adminSystemService.assignUserRoles(userId, request.getRoleIds());
        return Result.success();
    }

    @GetMapping("/roles")
    public Result<List<SysRole>> getRoles() {
        return Result.success(adminSystemService.getRoles());
    }

    @GetMapping("/permissions")
    public Result<List<SysPermission>> getPermissions() {
        return Result.success(adminSystemService.getPermissions());
    }

    @GetMapping("/roles/{roleId}/permissions")
    public Result<List<Long>> getRolePermissions(@PathVariable Long roleId) {
        return Result.success(adminSystemService.getRolePermissionIds(roleId));
    }

    @PatchMapping("/roles/{roleId}/permissions")
    public Result<Void> assignRolePermissions(@PathVariable Long roleId, @RequestBody RolePermissionAssignRequest request) {
        adminSystemService.assignRolePermissions(roleId, request.getPermissionIds());
        return Result.success();
    }

    @GetMapping("/audit/items")
    public Result<PageResult<Map<String, Object>>> getAuditItems(
            @RequestParam(defaultValue = "post") String type,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer auditStatus) {
        return Result.success(adminSystemService.getAuditItems(type, current, size, keyword, auditStatus));
    }

    @PatchMapping("/audit/items/{type}/{id}")
    public Result<Void> auditItem(@PathVariable String type, @PathVariable Long id, @RequestBody AuditActionRequest request) {
        adminSystemService.auditItem(type, id, request.getAuditStatus(), request.getAuditRemark());
        return Result.success();
    }

    @GetMapping("/sections")
    public Result<List<ForumSection>> getSections(@RequestParam(required = false) String keyword) {
        return Result.success(adminSystemService.getSections(keyword));
    }

    @PostMapping("/sections")
    public Result<ForumSection> createSection(@RequestBody ForumSection section) {
        return Result.success(adminSystemService.createSection(section));
    }

    @PutMapping("/sections/{id}")
    public Result<ForumSection> updateSection(@PathVariable Long id, @RequestBody ForumSection section) {
        return Result.success(adminSystemService.updateSection(id, section));
    }

    @PatchMapping("/sections/{id}/status")
    public Result<Void> updateSectionStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        adminSystemService.updateSectionStatus(id, request.getStatus());
        return Result.success();
    }

    @GetMapping("/categories")
    public Result<List<ServiceProductCategory>> getProductCategories() {
        return Result.success(adminSystemService.getProductCategories());
    }

    @PostMapping("/categories")
    public Result<ServiceProductCategory> createProductCategory(@RequestBody ServiceProductCategory category) {
        return Result.success(adminSystemService.createProductCategory(category));
    }

    @PutMapping("/categories/{id}")
    public Result<ServiceProductCategory> updateProductCategory(@PathVariable Long id,
            @RequestBody ServiceProductCategory category) {
        return Result.success(adminSystemService.updateProductCategory(id, category));
    }

    @PatchMapping("/categories/{id}/status")
    public Result<Void> updateProductCategoryStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        adminSystemService.updateProductCategoryStatus(id, request.getStatus());
        return Result.success();
    }

    @GetMapping("/reports")
    public Result<PageResult<Map<String, Object>>> getReports(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer targetType) {
        return Result.success(adminSystemService.getReports(current, size, status, targetType));
    }

    @PatchMapping("/reports/{id}/handle")
    public Result<Void> handleReport(@PathVariable Long id,
            @RequestBody HandleReportRequest request,
            @AuthenticationPrincipal SysUser currentUser) {
        adminSystemService.handleReport(id, currentUser.getId(), request.getStatus(), request.getHandleResult());
        return Result.success();
    }

    @GetMapping("/lostfound-claims")
    public Result<PageResult<Map<String, Object>>> getLostFoundClaims(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer status) {
        return Result.success(adminSystemService.getLostFoundClaims(current, size, status));
    }

    @PostMapping("/lostfound-claims/{id}/audit")
    public Result<Void> auditLostFoundClaim(@PathVariable Long id,
            @RequestBody AuditActionRequest request,
            @AuthenticationPrincipal SysUser currentUser) {
        adminSystemService.auditLostFoundClaim(id, currentUser.getId(), request.getAuditStatus(), request.getAuditRemark());
        return Result.success();
    }

    @GetMapping("/stats/overview")
    public Result<Map<String, Object>> getOverviewStats() {
        return Result.success(adminSystemService.getOverviewStats());
    }

    @GetMapping("/stats/trend")
    public Result<List<Map<String, Object>>> getTrendStats(
            @RequestParam(defaultValue = "7") Integer days) {
        return Result.success(adminSystemService.getTrendStats(days));
    }

    @GetMapping("/stats/sections")
    public Result<List<Map<String, Object>>> getSectionDistribution() {
        return Result.success(adminSystemService.getSectionDistribution());
    }

    @GetMapping("/sensitive-words")
    public Result<List<AuditSensitiveWord>> getSensitiveWords(
            @RequestParam(required = false) Integer category) {
        if (category != null) {
            return Result.success(contentFilterService.getSensitiveWordsByCategory(category));
        }
        return Result.success(contentFilterService.getAllSensitiveWords());
    }

    @PostMapping("/sensitive-words")
    public Result<AuditSensitiveWord> addSensitiveWord(@RequestBody AuditSensitiveWord word) {
        return Result.success(contentFilterService.addSensitiveWord(word));
    }

    @PutMapping("/sensitive-words/{id}")
    public Result<Void> updateSensitiveWord(@PathVariable Integer id, @RequestBody AuditSensitiveWord word) {
        word.setId(id);
        contentFilterService.updateSensitiveWord(word);
        return Result.success();
    }

    @DeleteMapping("/sensitive-words/{id}")
    public Result<Void> deleteSensitiveWord(@PathVariable Integer id) {
        contentFilterService.deleteSensitiveWord(id);
        return Result.success();
    }
}
