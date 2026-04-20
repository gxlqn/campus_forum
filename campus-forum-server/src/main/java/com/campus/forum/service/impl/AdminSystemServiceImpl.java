package com.campus.forum.service.impl;

import com.campus.forum.common.PageResult;
import com.campus.forum.common.ResultCode;
import com.campus.forum.entity.AuditLog;
import com.campus.forum.entity.ForumSection;
import com.campus.forum.entity.ServiceProductCategory;
import com.campus.forum.entity.SysPermission;
import com.campus.forum.entity.SysRole;
import com.campus.forum.entity.SysUser;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.AdminSystemMapper;
import com.campus.forum.mapper.AuditLogMapper;
import com.campus.forum.mapper.MessageMapper;
import com.campus.forum.mapper.ServiceLostFoundClaimMapper;
import com.campus.forum.service.AdminSystemService;
import com.campus.forum.service.LostFoundService;
import com.campus.forum.service.impl.ReportServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AdminSystemServiceImpl implements AdminSystemService {

    private static final Logger log = LoggerFactory.getLogger(AdminSystemServiceImpl.class);

    @Autowired
    private AdminSystemMapper adminSystemMapper;

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private ServiceLostFoundClaimMapper claimMapper;

    @Autowired
    private LostFoundService lostFoundService;

    @Override
    public PageResult<Map<String, Object>> getUsers(Long current, Long size, String keyword, Integer status, Integer isVerified) {
        long pageNo = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 10 : size;
        long offset = (pageNo - 1) * pageSize;
        List<SysUser> users = adminSystemMapper.selectUserPage(keyword, status, isVerified, offset, pageSize);
        Long total = adminSystemMapper.countUsers(keyword, status, isVerified);

        List<Map<String, Object>> records = new ArrayList<>();
        for (SysUser user : users) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", user.getId());
            row.put("username", user.getUsername());
            row.put("nickname", user.getNickname());
            row.put("avatar", user.getAvatar());
            row.put("studentId", user.getStudentId());
            row.put("userType", user.getUserType());
            row.put("status", user.getStatus());
            row.put("isVerified", user.getIsVerified());
            row.put("phone", user.getPhone());
            row.put("email", user.getEmail());
            row.put("createTime", user.getCreateTime());
            row.put("updateTime", user.getUpdateTime());
            List<Map<String, Object>> roles = adminSystemMapper.selectUserRoleInfo(user.getId());
            row.put("roles", roles);
            row.put("roleCodes", roles.stream().map(item -> String.valueOf(item.get("roleCode"))).collect(Collectors.toList()));
            records.add(row);
        }
        return new PageResult<>(pageNo, pageSize, total == null ? 0L : total, records);
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        if (userId == null || status == null || status < 0 || status > 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户状态参数无效");
        }
        int changed = adminSystemMapper.updateUserStatus(userId, status);
        if (changed == 0) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
    }

    @Override
    public void updateUserVerify(Long userId, Integer isVerified) {
        if (userId == null || isVerified == null || (isVerified != 0 && isVerified != 1)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "实名认证参数无效");
        }
        int changed = adminSystemMapper.updateUserVerified(userId, isVerified);
        if (changed == 0) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
    }

    @Override
    public void assignUserRoles(Long userId, List<Long> roleIds) {
        if (userId == null) {
            throw new BusinessException(ResultCode.PARAM_MISSING);
        }
        adminSystemMapper.deleteUserRoles(userId);
        if (roleIds != null && !roleIds.isEmpty()) {
            adminSystemMapper.insertUserRoles(userId, roleIds);
        }
    }

    @Override
    public List<SysRole> getRoles() {
        return adminSystemMapper.selectRoles();
    }

    @Override
    public List<SysPermission> getPermissions() {
        return adminSystemMapper.selectPermissions();
    }

    @Override
    public List<Long> getRolePermissionIds(Long roleId) {
        if (roleId == null) {
            throw new BusinessException(ResultCode.PARAM_MISSING);
        }
        return adminSystemMapper.selectRolePermissionIds(roleId);
    }

    @Override
    public void assignRolePermissions(Long roleId, List<Long> permissionIds) {
        if (roleId == null) {
            throw new BusinessException(ResultCode.PARAM_MISSING);
        }
        adminSystemMapper.deleteRolePermissions(roleId);
        if (permissionIds != null && !permissionIds.isEmpty()) {
            adminSystemMapper.insertRolePermissions(roleId, permissionIds);
        }
    }

    @Override
    public PageResult<Map<String, Object>> getAuditItems(String type, Long current, Long size, String keyword, Integer auditStatus) {
        long pageNo = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 10 : size;
        long offset = (pageNo - 1) * pageSize;
        String auditType = normalizeAuditType(type);
        if (auditStatus != null && (auditStatus < 0 || auditStatus > 2)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "审核状态参数无效");
        }

        List<Map<String, Object>> records;
        Long total;
        switch (auditType) {
            case "comment":
                records = adminSystemMapper.selectCommentAuditItems(keyword, auditStatus, offset, pageSize);
                total = adminSystemMapper.countCommentAuditItems(keyword, auditStatus);
                break;
            case "product":
                records = adminSystemMapper.selectProductAuditItems(keyword, auditStatus, offset, pageSize);
                total = adminSystemMapper.countProductAuditItems(keyword, auditStatus);
                break;
            case "activity":
                records = adminSystemMapper.selectActivityAuditItems(keyword, auditStatus, offset, pageSize);
                total = adminSystemMapper.countActivityAuditItems(keyword, auditStatus);
                break;
            case "help":
                records = adminSystemMapper.selectHelpAuditItems(keyword, auditStatus, offset, pageSize);
                total = adminSystemMapper.countHelpAuditItems(keyword, auditStatus);
                break;
            case "lostfound":
                records = adminSystemMapper.selectLostFoundAuditItems(keyword, auditStatus, offset, pageSize);
                total = adminSystemMapper.countLostFoundAuditItems(keyword, auditStatus);
                break;
            default:
                records = adminSystemMapper.selectPostAuditItems(keyword, auditStatus, offset, pageSize);
                total = adminSystemMapper.countPostAuditItems(keyword, auditStatus);
                break;
        }
        return new PageResult<>(pageNo, pageSize, total == null ? 0L : total, records);
    }

    @Override
    public void auditItem(String type, Long id, Integer auditStatus, String auditRemark) {
        if (id == null || auditStatus == null || auditStatus < 0 || auditStatus > 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "审核参数无效");
        }
        String auditType = normalizeAuditType(type);
        int changed;
        switch (auditType) {
            case "comment":
                changed = adminSystemMapper.updateCommentAuditStatus(id, auditStatus);
                break;
            case "product":
                changed = adminSystemMapper.updateProductAuditStatus(id, auditStatus);
                break;
            case "activity":
                changed = adminSystemMapper.updateActivityAuditStatus(id, auditStatus);
                break;
            case "help":
                changed = adminSystemMapper.updateHelpAuditStatus(id, auditStatus);
                break;
            case "lostfound":
                changed = adminSystemMapper.updateLostFoundAuditStatus(id, auditStatus);
                break;
            default:
                changed = adminSystemMapper.updatePostAuditStatus(id, auditStatus, auditRemark);
                break;
        }
        if (changed == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "审核对象不存在");
        }

        // 记录人工审核日志
        saveManualAuditLog(auditType, id, auditStatus, auditRemark);

        // 向内容作者发送审核结果通知
        sendAuditNotification(auditType, id, auditStatus, auditRemark);
    }

    private void saveManualAuditLog(String targetType, Long targetId, Integer auditStatus, String auditRemark) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setTargetType(targetType);
            auditLog.setTargetId(targetId);
            auditLog.setFilterResult(null);
            auditLog.setMatchedKeywords(null);
            auditLog.setMatchedLevel(null);
            auditLog.setAiAuditStatus(null);
            auditLog.setFinalStatus(mapContentStatusToAuditStatus(auditStatus));
            auditLog.setAuditMethod(2); // 2-人工审核
            auditLog.setAuditRemark(auditRemark);
            auditLog.setContentSnapshot(null);
            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            log.error("保存人工审核日志失败: type={}, id={}", targetType, targetId, e);
        }
    }

    /**
     * 向内容作者发送审核结果通知
     */
    private void sendAuditNotification(String auditType, Long contentId, Integer auditStatus, String remark) {
        try {
            Long authorId = switch (auditType) {
                case "comment" -> adminSystemMapper.selectCommentAuthorId(contentId);
                case "product" -> adminSystemMapper.selectProductAuthorId(contentId);
                case "activity" -> adminSystemMapper.selectActivityAuthorId(contentId);
                case "help" -> adminSystemMapper.selectHelpAuthorId(contentId);
                case "lostfound" -> adminSystemMapper.selectLostFoundAuthorId(contentId);
                default -> adminSystemMapper.selectPostAuthorId(contentId);
            };

            if (authorId == null) return;

            boolean passed = (auditStatus == 1);
            String typeLabel = switch (auditType) {
                case "post" -> "帖子";
                case "comment" -> "评论";
                case "product" -> "商品";
                case "activity" -> "活动";
                case "help" -> "互助";
                case "lostfound" -> "失物招领";
                default -> "内容";
            };
            String title = passed ? typeLabel + "审核通过" : typeLabel + "审核未通过";
            String content;
            if (StringUtils.hasText(remark)) {
                content = remark;
            } else if (passed) {
                content = "您发布的" + typeLabel + "已通过审核，感谢您的发布";
            } else {
                content = "您发布的" + typeLabel + "未通过审核，请查看原因后重新发布";
            }
            messageMapper.insertNotification(authorId, null, 8, title, content, null, null);
        } catch (Exception e) {
            log.error("发送审核结果通知失败: type={}, id={}, status={}", auditType, contentId, auditStatus, e);
        }
    }

    /**
     * 将内容表的审核状态映射回审核日志的finalStatus
     * 内容表: 0-待审核, 1-审核通过, 2-审核拒绝
     * 审核日志: 1-自动通过, 2-自动拒绝 (人工审核时直接映射)
     */
    private int mapContentStatusToAuditStatus(int contentStatus) {
        switch (contentStatus) {
            case 1: return 1; // 通过
            case 2: return 2; // 拒绝
            default: return 0; // 待审核
        }
    }

    @Override
    public List<ForumSection> getSections(String keyword) {
        return adminSystemMapper.selectSections(keyword);
    }

    @Override
    public ForumSection createSection(ForumSection section) {
        if (section == null || !StringUtils.hasText(section.getSectionName())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "板块名称不能为空");
        }
        if (!StringUtils.hasText(section.getSectionCode())) {
            section.setSectionCode("SEC_" + System.currentTimeMillis());
        }
        if (section.getSort() == null) {
            section.setSort(0);
        }
        if (section.getStatus() == null) {
            section.setStatus(1);
        }
        if (section.getIsDefault() == null) {
            section.setIsDefault(0);
        }
        adminSystemMapper.insertSection(section);
        List<ForumSection> sections = adminSystemMapper.selectSections(null);
        return sections.stream().filter(item -> Objects.equals(item.getId(), section.getId())).findFirst().orElse(section);
    }

    @Override
    public ForumSection updateSection(Long id, ForumSection section) {
        if (id == null || section == null || !StringUtils.hasText(section.getSectionName())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "板块参数无效");
        }
        section.setId(id);
        if (!StringUtils.hasText(section.getSectionCode())) {
            section.setSectionCode("SEC_" + id);
        }
        if (section.getSort() == null) {
            section.setSort(0);
        }
        if (section.getStatus() == null) {
            section.setStatus(1);
        }
        if (section.getIsDefault() == null) {
            section.setIsDefault(0);
        }
        int changed = adminSystemMapper.updateSection(section);
        if (changed == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "板块不存在");
        }
        List<ForumSection> sections = adminSystemMapper.selectSections(null);
        return sections.stream().filter(item -> Objects.equals(item.getId(), id)).findFirst().orElse(section);
    }

    @Override
    public void updateSectionStatus(Long id, Integer status) {
        if (id == null || status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "板块状态参数无效");
        }
        int changed = adminSystemMapper.updateSectionStatus(id, status);
        if (changed == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "板块不存在");
        }
    }

    @Override
    public List<ServiceProductCategory> getProductCategories() {
        return adminSystemMapper.selectProductCategories();
    }

    @Override
    public ServiceProductCategory createProductCategory(ServiceProductCategory category) {
        if (category == null || !StringUtils.hasText(category.getCategoryName())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "分类名称不能为空");
        }
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        adminSystemMapper.insertProductCategory(category);
        return category;
    }

    @Override
    public ServiceProductCategory updateProductCategory(Long id, ServiceProductCategory category) {
        if (id == null || category == null || !StringUtils.hasText(category.getCategoryName())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "分类参数无效");
        }
        category.setId(id);
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        int changed = adminSystemMapper.updateProductCategory(category);
        if (changed == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分类不存在");
        }
        return category;
    }

    @Override
    public void updateProductCategoryStatus(Long id, Integer status) {
        if (id == null || status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "分类状态参数无效");
        }
        int changed = adminSystemMapper.updateProductCategoryStatus(id, status);
        if (changed == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分类不存在");
        }
    }

    @Override
    public PageResult<Map<String, Object>> getReports(Long current, Long size, Integer status, Integer targetType) {
        long pageNo = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 10 : size;
        long offset = (pageNo - 1) * pageSize;
        List<Map<String, Object>> records = adminSystemMapper.selectReports(status, targetType, offset, pageSize);
        Long total = adminSystemMapper.countReports(status, targetType);
        return new PageResult<>(pageNo, pageSize, total == null ? 0L : total, records);
    }

    @Override
    public void handleReport(Long reportId, Long handlerId, Integer status, String handleResult) {
        if (reportId == null || handlerId == null || status == null || status < 0 || status > 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "举报处理参数无效");
        }
        // 精确查询举报记录获取举报人ID（用于后续发送通知）
        Map<String, Object> report = adminSystemMapper.selectReportById(reportId);
        Long reporterUserId = null;
        if (report != null && report.get("userId") != null) {
            reporterUserId = ((Number) report.get("userId")).longValue();
        }

        int changed = adminSystemMapper.handleReport(reportId, status, handleResult, handlerId);
        if (changed == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "举报记录不存在");
        }

        // 向举报人发送处理结果通知
        if (reporterUserId != null) {
            try {
                String title = (status == 1) ? "举报处理完成" : "举报已忽略";
                String content = handleResult;
                if (!StringUtils.hasText(content)) {
                    content = (status == 1)
                            ? "您提交的举报已被管理员处理，感谢您的反馈"
                            : "您提交的举报已被管理员忽略，如有疑问请联系客服";
                }
                messageMapper.insertNotification(reporterUserId, null, 9, title, content, null, null);
            } catch (Exception e) {
                log.error("发送举报处理通知失败: userId={}", reporterUserId, e);
            }
        }
    }

    @Override
    public PageResult<Map<String, Object>> getLostFoundClaims(Long current, Long size, Integer status) {
        return lostFoundService.getClaimList(current, size, status);
    }

    @Override
    public void auditLostFoundClaim(Long claimId, Long operatorId, Integer auditStatus, String auditRemark) {
        lostFoundService.auditClaim(claimId, operatorId, auditStatus, auditRemark);
    }

    @Override
    public Map<String, Object> getOverviewStats() {
        Map<String, Object> data = new LinkedHashMap<>();
        Long productTotal = safeLong(adminSystemMapper.countServiceProducts());
        Long activityTotal = safeLong(adminSystemMapper.countServiceActivities());
        Long helpTotal = safeLong(adminSystemMapper.countServiceHelpRequests());
        Long lostFoundTotal = safeLong(adminSystemMapper.countServiceLostFound());
        Long pendingAudit = safeLong(adminSystemMapper.countPendingPostAudit())
                + safeLong(adminSystemMapper.countPendingCommentAudit())
                + safeLong(adminSystemMapper.countPendingProductAudit())
                + safeLong(adminSystemMapper.countPendingActivityAudit())
                + safeLong(adminSystemMapper.countPendingHelpAudit())
                + safeLong(adminSystemMapper.countPendingLostFoundAudit());

        data.put("userTotal", safeLong(adminSystemMapper.countTotalUsers()));
        data.put("verifiedUserTotal", safeLong(adminSystemMapper.countVerifiedUsers()));
        data.put("postTotal", safeLong(adminSystemMapper.countForumPosts()));
        data.put("commentTotal", safeLong(adminSystemMapper.countForumComments()));
        data.put("serviceTotal", productTotal + activityTotal + helpTotal + lostFoundTotal);
        data.put("productTotal", productTotal);
        data.put("activityTotal", activityTotal);
        data.put("helpTotal", helpTotal);
        data.put("lostFoundTotal", lostFoundTotal);
        data.put("pendingAuditTotal", pendingAudit);
        data.put("pendingReportTotal", safeLong(adminSystemMapper.countPendingReports()));
        return data;
    }

    @Override
    public List<Map<String, Object>> getTrendStats(Integer days) {
        int range = days == null || days < 1 ? 7 : Math.min(days, 30);
        LocalDate startDay = LocalDate.now().minusDays(range - 1L);
        LocalDateTime startTime = startDay.atStartOfDay();

        Map<String, Long> userTrend = toDailyMap(adminSystemMapper.selectUserTrend(startTime));
        Map<String, Long> postTrend = toDailyMap(adminSystemMapper.selectPostTrend(startTime));
        Map<String, Long> serviceTrend = toDailyMap(adminSystemMapper.selectServiceTrend(startTime));

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < range; i++) {
            String date = startDay.plusDays(i).toString();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", date);
            row.put("userCount", userTrend.getOrDefault(date, 0L));
            row.put("postCount", postTrend.getOrDefault(date, 0L));
            row.put("serviceCount", serviceTrend.getOrDefault(date, 0L));
            result.add(row);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getSectionDistribution() {
        return adminSystemMapper.selectSectionDistribution();
    }

    private String normalizeAuditType(String type) {
        if (!StringUtils.hasText(type)) {
            return "post";
        }
        String value = type.trim().toLowerCase();
        switch (value) {
            case "post":
            case "comment":
            case "product":
            case "activity":
            case "help":
            case "lostfound":
                return value;
            default:
                throw new BusinessException(ResultCode.PARAM_ERROR, "不支持的审核类型");
        }
    }

    private Long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private Map<String, Long> toDailyMap(List<Map<String, Object>> data) {
        Map<String, Long> result = new LinkedHashMap<>();
        if (data == null) {
            return result;
        }
        for (Map<String, Object> row : data) {
            String day = String.valueOf(row.get("day"));
            Object total = row.get("total");
            Long count;
            if (total instanceof Number) {
                count = ((Number) total).longValue();
            } else {
                count = Long.parseLong(String.valueOf(total));
            }
            result.put(day, count);
        }
        return result;
    }
}
