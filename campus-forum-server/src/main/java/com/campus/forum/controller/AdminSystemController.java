package com.campus.forum.controller;

import com.campus.forum.common.PageResult;
import com.campus.forum.common.Result;
import com.campus.forum.dto.admin.AuditActionRequest;
import com.campus.forum.dto.admin.ResolveReportRequest;
import com.campus.forum.dto.admin.HandleReportRequest;
import com.campus.forum.dto.admin.RolePermissionAssignRequest;
import com.campus.forum.dto.admin.StatusUpdateRequest;
import com.campus.forum.dto.admin.UserRoleAssignRequest;
import com.campus.forum.dto.admin.UserVerifyUpdateRequest;
import com.campus.forum.entity.ForumModerator;
import com.campus.forum.entity.ForumSection;
import com.campus.forum.entity.ServiceProductCategory;
import com.campus.forum.entity.SysPermission;
import com.campus.forum.entity.SysRole;
import com.campus.forum.entity.SysUser;
import com.campus.forum.entity.AuditSensitiveWord;
import com.campus.forum.service.AdminSystemService;
import com.campus.forum.service.ContentFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasAuthority('system:user')")
    @GetMapping("/users")
    public Result<PageResult<Map<String, Object>>> getUsers(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer isVerified) {
        return Result.success(adminSystemService.getUsers(current, size, keyword, status, isVerified));
    }

    @PreAuthorize("hasAuthority('system:user')")
    @PatchMapping("/users/{userId}/status")
    public Result<Void> updateUserStatus(@PathVariable Long userId, @RequestBody StatusUpdateRequest request) {
        adminSystemService.updateUserStatus(userId, request.getStatus());
        return Result.success();
    }

    @PreAuthorize("hasAuthority('system:user')")
    @PatchMapping("/users/{userId}/verify")
    public Result<Void> updateUserVerify(@PathVariable Long userId, @RequestBody UserVerifyUpdateRequest request) {
        adminSystemService.updateUserVerify(userId, request.getIsVerified());
        return Result.success();
    }

    /**
     * 分配用户角色：SUPER_ADMIN 可分配任意角色；ADMIN 仅可分配版主类角色（不能分配 SUPER_ADMIN/ADMIN）
     */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @PatchMapping("/users/{userId}/roles")
    public Result<Void> assignUserRoles(@PathVariable Long userId,
                                        @RequestBody UserRoleAssignRequest request,
                                        @AuthenticationPrincipal SysUser currentUser) {
        // 非超管校验：ADMIN 只能给用户分配 MODERATOR / MODERATOR_* 角色，不可提升为 ADMIN/SUPER_ADMIN
        List<String> currentRoles = adminSystemService.getUserRoleCodes(currentUser.getId());
        boolean isSuperAdmin = currentRoles.contains("SUPER_ADMIN");
        if (!isSuperAdmin && request.getRoleIds() != null) {
            List<SysRole> allRoles = adminSystemService.getRoles();
            for (Long roleId : request.getRoleIds()) {
                SysRole target = allRoles.stream().filter(r -> r.getId().equals(roleId)).findFirst().orElse(null);
                if (target != null) {
                    String code = target.getRoleCode();
                    if ("SUPER_ADMIN".equals(code) || "ADMIN".equals(code)) {
                        return Result.error(403, "无权分配该角色，仅可分配版主类角色（MODERATOR/MODERATOR_*）");
                    }
                }
            }
        }
        adminSystemService.assignUserRoles(userId, request.getRoleIds());
        return Result.success();
    }

    /** 仅超管可管理角色和权限 */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @GetMapping("/roles")
    public Result<List<SysRole>> getRoles() {
        return Result.success(adminSystemService.getRoles());
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @GetMapping("/permissions")
    public Result<List<SysPermission>> getPermissions() {
        return Result.success(adminSystemService.getPermissions());
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @GetMapping("/roles/{roleId}/permissions")
    public Result<List<Long>> getRolePermissions(@PathVariable Long roleId) {
        return Result.success(adminSystemService.getRolePermissionIds(roleId));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @PatchMapping("/roles/{roleId}/permissions")
    public Result<Void> assignRolePermissions(@PathVariable Long roleId, @RequestBody RolePermissionAssignRequest request) {
        adminSystemService.assignRolePermissions(roleId, request.getPermissionIds());
        return Result.success();
    }

    @PreAuthorize("hasAuthority('system:audit')")
    @GetMapping("/audit/items")
    public Result<PageResult<Map<String, Object>>> getAuditItems(
            @RequestParam(defaultValue = "post") String type,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer auditStatus) {
        return Result.success(adminSystemService.getAuditItems(type, current, size, keyword, auditStatus));
    }

    @PreAuthorize("hasAuthority('system:audit')")
    @PatchMapping("/audit/items/{type}/{id}")
    public Result<Void> auditItem(@PathVariable String type, @PathVariable Long id, @RequestBody AuditActionRequest request) {
        adminSystemService.auditItem(type, id, request.getAuditStatus(), request.getAuditRemark());
        return Result.success();
    }

    /**
     * 基础数据查询：板块列表（所有管理角色（含各类版主）可访问，作为业务模块的依赖数据）
     */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATOR','MODERATOR_FORUM','MODERATOR_MARKET','MODERATOR_LOSTFOUND','MODERATOR_ACTIVITY','MODERATOR_HELP','MODERATOR_INFO')")
    @GetMapping("/sections")
    public Result<List<ForumSection>> getSections(@RequestParam(required = false) String keyword) {
        return Result.success(adminSystemService.getSections(keyword));
    }

    @PreAuthorize("hasAuthority('forum:section')")
    @PostMapping("/sections")
    public Result<ForumSection> createSection(@RequestBody ForumSection section) {
        return Result.success(adminSystemService.createSection(section));
    }

    @PreAuthorize("hasAuthority('forum:section')")
    @PutMapping("/sections/{id}")
    public Result<ForumSection> updateSection(@PathVariable Long id, @RequestBody ForumSection section) {
        return Result.success(adminSystemService.updateSection(id, section));
    }

    @PreAuthorize("hasAuthority('forum:section')")
    @PatchMapping("/sections/{id}/status")
    public Result<Void> updateSectionStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        adminSystemService.updateSectionStatus(id, request.getStatus());
        return Result.success();
    }

    /**
     * 基础数据查询：商品分类列表（所有管理角色（含各类版主）可访问，作为业务模块的依赖数据）
     */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATOR','MODERATOR_FORUM','MODERATOR_MARKET','MODERATOR_LOSTFOUND','MODERATOR_ACTIVITY','MODERATOR_HELP','MODERATOR_INFO')")
    @GetMapping("/categories")
    public Result<List<ServiceProductCategory>> getProductCategories() {
        return Result.success(adminSystemService.getProductCategories());
    }

    @PreAuthorize("hasAuthority('service:category')")
    @PostMapping("/categories")
    public Result<ServiceProductCategory> createProductCategory(@RequestBody ServiceProductCategory category) {
        return Result.success(adminSystemService.createProductCategory(category));
    }

    @PreAuthorize("hasAuthority('service:category')")
    @PutMapping("/categories/{id}")
    public Result<ServiceProductCategory> updateProductCategory(@PathVariable Long id,
            @RequestBody ServiceProductCategory category) {
        return Result.success(adminSystemService.updateProductCategory(id, category));
    }

    @PreAuthorize("hasAuthority('service:category')")
    @PatchMapping("/categories/{id}/status")
    public Result<Void> updateProductCategoryStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        adminSystemService.updateProductCategoryStatus(id, request.getStatus());
        return Result.success();
    }

    @PreAuthorize("hasAuthority('system:report')")
    @GetMapping("/reports")
    public Result<PageResult<Map<String, Object>>> getReports(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer targetType) {
        return Result.success(adminSystemService.getReports(current, size, status, targetType));
    }

    @PreAuthorize("hasAuthority('system:report')")
    @PatchMapping("/reports/{id}/handle")
    public Result<Void> handleReport(@PathVariable Long id,
            @RequestBody HandleReportRequest request,
            @AuthenticationPrincipal SysUser currentUser) {
        adminSystemService.handleReport(id, currentUser.getId(), request.getStatus(), request.getHandleResult());
        return Result.success();
    }

    @PreAuthorize("hasAuthority('system:report')")
    @PatchMapping("/reports/{id}/resolve-post")
    public Result<Void> resolvePostReport(@PathVariable Long id,
            @RequestBody ResolveReportRequest request,
            @AuthenticationPrincipal SysUser currentUser) {
        adminSystemService.resolvePostReport(id, currentUser.getId(), request.getPostAuditStatus(), request.getPostAuditRemark(), request.getHandleResult());
        return Result.success();
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('lostfound:manage')")
    @GetMapping("/lostfound-claims")
    public Result<PageResult<Map<String, Object>>> getLostFoundClaims(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer status) {
        return Result.success(adminSystemService.getLostFoundClaims(current, size, status));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('lostfound:manage')")
    @PostMapping("/lostfound-claims/{id}/audit")
    public Result<Void> auditLostFoundClaim(@PathVariable Long id,
            @RequestBody AuditActionRequest request,
            @AuthenticationPrincipal SysUser currentUser) {
        adminSystemService.auditLostFoundClaim(id, currentUser.getId(), request.getAuditStatus(), request.getAuditRemark());
        return Result.success();
    }

    /** 统计接口：所有管理角色（含各类版主）可访问 */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATOR','MODERATOR_FORUM','MODERATOR_MARKET','MODERATOR_LOSTFOUND','MODERATOR_ACTIVITY','MODERATOR_HELP','MODERATOR_INFO')")
    @GetMapping("/stats/overview")
    public Result<Map<String, Object>> getOverviewStats() {
        return Result.success(adminSystemService.getOverviewStats());
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATOR','MODERATOR_FORUM','MODERATOR_MARKET','MODERATOR_LOSTFOUND','MODERATOR_ACTIVITY','MODERATOR_HELP','MODERATOR_INFO')")
    @GetMapping("/stats/trend")
    public Result<List<Map<String, Object>>> getTrendStats(
            @RequestParam(defaultValue = "7") Integer days) {
        return Result.success(adminSystemService.getTrendStats(days));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATOR','MODERATOR_FORUM','MODERATOR_MARKET','MODERATOR_LOSTFOUND','MODERATOR_ACTIVITY','MODERATOR_HELP','MODERATOR_INFO')")
    @GetMapping("/stats/sections")
    public Result<List<Map<String, Object>>> getSectionDistribution() {
        return Result.success(adminSystemService.getSectionDistribution());
    }

    @PreAuthorize("hasAuthority('system:sensitive')")
    @GetMapping("/sensitive-words")
    public Result<PageResult<AuditSensitiveWord>> getSensitiveWords(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer wordType,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) String keyword) {
        return Result.success(contentFilterService.getSensitiveWordsPage(current, size, wordType, category, keyword));
    }

    @PreAuthorize("hasAuthority('system:sensitive')")
    @PostMapping("/sensitive-words")
    public Result<AuditSensitiveWord> addSensitiveWord(@RequestBody AuditSensitiveWord word) {
        return Result.success(contentFilterService.addSensitiveWord(word));
    }

    @PreAuthorize("hasAuthority('system:sensitive')")
    @PutMapping("/sensitive-words/{id}")
    public Result<Void> updateSensitiveWord(@PathVariable Integer id, @RequestBody AuditSensitiveWord word) {
        word.setId(id);
        contentFilterService.updateSensitiveWord(word);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('system:sensitive')")
    @DeleteMapping("/sensitive-words/{id}")
    public Result<Void> deleteSensitiveWord(@PathVariable Integer id) {
        contentFilterService.deleteSensitiveWord(id);
        return Result.success();
    }

    // ==================== 版主模块管理接口 ====================

    /** 6个业务模块定义（与权限码一一对应） */
    private static final List<Map<String, String>> MODULES = List.of(
            Map.of("code", "market:manage", "name", "二手市场管理"),
            Map.of("code", "lostfound:manage", "name", "失物招领管理"),
            Map.of("code", "activity:manage", "name", "活动管理"),
            Map.of("code", "help:manage", "name", "互助管理"),
            Map.of("code", "info:news", "name", "校园资讯管理"),
            Map.of("code", "info:nav", "name", "服务导航管理")
    );

    /**
     * 获取所有可用模块列表（已认证用户可访问，用于下拉选择）
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/moderators/modules")
    public Result<List<Map<String, String>>> getModeratorModules() {
        return Result.success(MODULES);
    }

    /**
     * 获取版主分配列表（分页）
     */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @GetMapping("/moderators")
    public Result<PageResult<Map<String, Object>>> getModeratorList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String moduleCode) {
        return Result.success(adminSystemService.getModerators(current, size, keyword, moduleCode));
    }

    /**
     * 分配版主模块
     */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @PostMapping("/moderators")
    public Result<Void> assignModerator(@RequestBody ForumModerator moderator,
                                        @AuthenticationPrincipal SysUser currentUser) {
        adminSystemService.assignModerator(moderator.getUserId(), moderator.getModuleCode(),
                moderator.getModuleName(), currentUser.getId());
        return Result.success();
    }

    /**
     * 移除版主的模块分配
     */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @DeleteMapping("/moderators/{id}")
    public Result<Void> removeModerator(@PathVariable Long id) {
        adminSystemService.removeModerator(id);
        return Result.success();
    }

    /**
     * 获取当前登录用户的版主模块列表
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/moderators/my-modules")
    public Result<List<ForumModerator>> getMyModules(@AuthenticationPrincipal SysUser currentUser) {
        return Result.success(adminSystemService.getModeratorsByUserId(currentUser.getId()));
    }

    /**
     * 获取指定用户负责的模块编码列表
     */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @GetMapping("/moderators/user/{userId}/modules")
    public Result<List<String>> getUserModules(@PathVariable Long userId) {
        return Result.success(adminSystemService.getModeratorModuleCodes(userId));
    }
}
