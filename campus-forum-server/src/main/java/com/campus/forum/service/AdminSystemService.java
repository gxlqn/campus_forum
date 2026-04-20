package com.campus.forum.service;

import com.campus.forum.common.PageResult;
import com.campus.forum.entity.ForumSection;
import com.campus.forum.entity.ServiceProductCategory;
import com.campus.forum.entity.SysPermission;
import com.campus.forum.entity.SysRole;

import java.util.List;
import java.util.Map;

public interface AdminSystemService {

    PageResult<Map<String, Object>> getUsers(Long current, Long size, String keyword, Integer status, Integer isVerified);

    void updateUserStatus(Long userId, Integer status);

    void updateUserVerify(Long userId, Integer isVerified);

    void assignUserRoles(Long userId, List<Long> roleIds);

    List<SysRole> getRoles();

    List<SysPermission> getPermissions();

    List<Long> getRolePermissionIds(Long roleId);

    void assignRolePermissions(Long roleId, List<Long> permissionIds);

    PageResult<Map<String, Object>> getAuditItems(String type, Long current, Long size, String keyword, Integer auditStatus);

    void auditItem(String type, Long id, Integer auditStatus, String auditRemark);

    List<ForumSection> getSections(String keyword);

    ForumSection createSection(ForumSection section);

    ForumSection updateSection(Long id, ForumSection section);

    void updateSectionStatus(Long id, Integer status);

    List<ServiceProductCategory> getProductCategories();

    ServiceProductCategory createProductCategory(ServiceProductCategory category);

    ServiceProductCategory updateProductCategory(Long id, ServiceProductCategory category);

    void updateProductCategoryStatus(Long id, Integer status);

    PageResult<Map<String, Object>> getReports(Long current, Long size, Integer status, Integer targetType);

    void handleReport(Long reportId, Long handlerId, Integer status, String handleResult);
    PageResult<Map<String, Object>> getLostFoundClaims(Long current, Long size, Integer status);

    void auditLostFoundClaim(Long claimId, Long operatorId, Integer auditStatus, String auditRemark);
    Map<String, Object> getOverviewStats();

    List<Map<String, Object>> getTrendStats(Integer days);

    List<Map<String, Object>> getSectionDistribution();
}
