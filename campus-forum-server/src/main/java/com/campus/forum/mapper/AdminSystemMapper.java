package com.campus.forum.mapper;

import com.campus.forum.entity.ForumSection;
import com.campus.forum.entity.ServiceProductCategory;
import com.campus.forum.entity.SysPermission;
import com.campus.forum.entity.SysReport;
import com.campus.forum.entity.SysRole;
import com.campus.forum.entity.SysUser;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface AdminSystemMapper {

    @Select("""
            <script>
            SELECT * FROM sys_user
            WHERE deleted = 0
            <if test='keyword != null and keyword != ""'>
              AND (
                nickname LIKE CONCAT('%', #{keyword}, '%')
                OR username LIKE CONCAT('%', #{keyword}, '%')
                OR student_id LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            <if test='status != null'>
              AND status = #{status}
            </if>
            <if test='isVerified != null'>
              AND is_verified = #{isVerified}
            </if>
            ORDER BY id DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<SysUser> selectUserPage(@Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("isVerified") Integer isVerified,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1) FROM sys_user
            WHERE deleted = 0
            <if test='keyword != null and keyword != ""'>
              AND (
                nickname LIKE CONCAT('%', #{keyword}, '%')
                OR username LIKE CONCAT('%', #{keyword}, '%')
                OR student_id LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            <if test='status != null'>
              AND status = #{status}
            </if>
            <if test='isVerified != null'>
              AND is_verified = #{isVerified}
            </if>
            </script>
            """)
    Long countUsers(@Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("isVerified") Integer isVerified);

    @Update("UPDATE sys_user SET status = #{status}, update_time = NOW() WHERE id = #{userId} AND deleted = 0")
    int updateUserStatus(@Param("userId") Long userId, @Param("status") Integer status);

    @Update("UPDATE sys_user SET is_verified = #{isVerified}, update_time = NOW() WHERE id = #{userId} AND deleted = 0")
    int updateUserVerified(@Param("userId") Long userId, @Param("isVerified") Integer isVerified);

    @Select("""
            SELECT r.id AS roleId, r.role_name AS roleName, r.role_code AS roleCode
            FROM sys_role r
            INNER JOIN sys_user_role ur ON ur.role_id = r.id
            WHERE ur.user_id = #{userId}
            ORDER BY r.sort ASC, r.id ASC
            """)
    List<Map<String, Object>> selectUserRoleInfo(@Param("userId") Long userId);

    @Select("SELECT * FROM sys_role ORDER BY sort ASC, id ASC")
    List<SysRole> selectRoles();

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteUserRoles(@Param("userId") Long userId);

    @Insert("""
            <script>
            INSERT INTO sys_user_role(user_id, role_id)
            VALUES
            <foreach collection='roleIds' item='roleId' separator=','>
              (#{userId}, #{roleId})
            </foreach>
            </script>
            """)
    int insertUserRoles(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

    @Select("SELECT * FROM sys_permission ORDER BY sort ASC, id ASC")
    List<SysPermission> selectPermissions();

    @Select("SELECT permission_id FROM sys_role_permission WHERE role_id = #{roleId}")
    List<Long> selectRolePermissionIds(@Param("roleId") Long roleId);

    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    int deleteRolePermissions(@Param("roleId") Long roleId);

    @Insert("""
            <script>
            INSERT INTO sys_role_permission(role_id, permission_id)
            VALUES
            <foreach collection='permissionIds' item='permissionId' separator=','>
              (#{roleId}, #{permissionId})
            </foreach>
            </script>
            """)
    int insertRolePermissions(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);

    @Select("""
            <script>
            SELECT * FROM forum_section
            WHERE deleted = 0
            <if test='keyword != null and keyword != ""'>
              AND (
                section_name LIKE CONCAT('%', #{keyword}, '%')
                OR section_code LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            ORDER BY sort ASC, id ASC
            </script>
            """)
    List<ForumSection> selectSections(@Param("keyword") String keyword);

    @Insert("""
            INSERT INTO forum_section(section_name, section_code, description, icon, cover_image, sort, post_count,
              status, is_default, deleted, create_time, update_time)
            VALUES(#{sectionName}, #{sectionCode}, #{description}, #{icon}, #{coverImage}, #{sort}, 0,
              #{status}, #{isDefault}, 0, NOW(), NOW())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertSection(ForumSection section);

    @Update("""
            UPDATE forum_section
            SET section_name = #{sectionName},
                section_code = #{sectionCode},
                description = #{description},
                icon = #{icon},
                cover_image = #{coverImage},
                sort = #{sort},
                status = #{status},
                is_default = #{isDefault},
                update_time = NOW()
            WHERE id = #{id} AND deleted = 0
            """)
    int updateSection(ForumSection section);

    @Update("UPDATE forum_section SET status = #{status}, update_time = NOW() WHERE id = #{id} AND deleted = 0")
    int updateSectionStatus(@Param("id") Long id, @Param("status") Integer status);

    @Select("SELECT * FROM service_product_category ORDER BY sort ASC, id ASC")
    List<ServiceProductCategory> selectProductCategories();

    @Insert("""
            INSERT INTO service_product_category(parent_id, category_name, icon, sort, status, create_time, update_time)
            VALUES(#{parentId}, #{categoryName}, #{icon}, #{sort}, #{status}, NOW(), NOW())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertProductCategory(ServiceProductCategory category);

    @Update("""
            UPDATE service_product_category
            SET parent_id = #{parentId},
                category_name = #{categoryName},
                icon = #{icon},
                sort = #{sort},
                status = #{status},
                update_time = NOW()
            WHERE id = #{id}
            """)
    int updateProductCategory(ServiceProductCategory category);

    @Update("UPDATE service_product_category SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateProductCategoryStatus(@Param("id") Long id, @Param("status") Integer status);

    @Select("""
            <script>
            SELECT p.id, p.title, p.content, p.user_id AS userId, p.audit_status AS auditStatus,
                   p.status, p.create_time AS createTime, COALESCE(u.nickname, u.username) AS authorName,
                   'post' AS type
            FROM forum_post p
            LEFT JOIN sys_user u ON u.id = p.user_id
            WHERE p.deleted = 0
            <if test='keyword != null and keyword != ""'>
              AND p.title LIKE CONCAT('%', #{keyword}, '%')
            </if>
            <if test='auditStatus != null'>
              AND p.audit_status = #{auditStatus}
            </if>
            ORDER BY p.create_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectPostAuditItems(@Param("keyword") String keyword,
            @Param("auditStatus") Integer auditStatus,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1) FROM forum_post p
            WHERE p.deleted = 0
            <if test='keyword != null and keyword != ""'>
              AND p.title LIKE CONCAT('%', #{keyword}, '%')
            </if>
            <if test='auditStatus != null'>
              AND p.audit_status = #{auditStatus}
            </if>
            </script>
            """)
    Long countPostAuditItems(@Param("keyword") String keyword,
            @Param("auditStatus") Integer auditStatus);

    @Select("""
            <script>
            SELECT c.id, CONCAT('评论#', c.id) AS title, c.content, c.user_id AS userId,
                   c.audit_status AS auditStatus, c.status, c.create_time AS createTime,
                   COALESCE(u.nickname, u.username) AS authorName, 'comment' AS type
            FROM forum_comment c
            LEFT JOIN sys_user u ON u.id = c.user_id
            WHERE c.deleted = 0
            <if test='keyword != null and keyword != ""'>
              AND c.content LIKE CONCAT('%', #{keyword}, '%')
            </if>
            <if test='auditStatus != null'>
              AND c.audit_status = #{auditStatus}
            </if>
            ORDER BY c.create_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectCommentAuditItems(@Param("keyword") String keyword,
            @Param("auditStatus") Integer auditStatus,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1) FROM forum_comment c
            WHERE c.deleted = 0
            <if test='keyword != null and keyword != ""'>
              AND c.content LIKE CONCAT('%', #{keyword}, '%')
            </if>
            <if test='auditStatus != null'>
              AND c.audit_status = #{auditStatus}
            </if>
            </script>
            """)
    Long countCommentAuditItems(@Param("keyword") String keyword,
            @Param("auditStatus") Integer auditStatus);

    @Select("""
            <script>
            SELECT p.id, p.title, p.description AS content, p.user_id AS userId, p.audit_status AS auditStatus,
                   p.status, p.create_time AS createTime, COALESCE(u.nickname, u.username) AS authorName,
                   'product' AS type
            FROM service_product p
            LEFT JOIN sys_user u ON u.id = p.user_id
            WHERE p.deleted = 0
            <if test='keyword != null and keyword != ""'>
              AND p.title LIKE CONCAT('%', #{keyword}, '%')
            </if>
            <if test='auditStatus != null'>
              AND p.audit_status = #{auditStatus}
            </if>
            ORDER BY p.create_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectProductAuditItems(@Param("keyword") String keyword,
            @Param("auditStatus") Integer auditStatus,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1) FROM service_product p
            WHERE p.deleted = 0
            <if test='keyword != null and keyword != ""'>
              AND p.title LIKE CONCAT('%', #{keyword}, '%')
            </if>
            <if test='auditStatus != null'>
              AND p.audit_status = #{auditStatus}
            </if>
            </script>
            """)
    Long countProductAuditItems(@Param("keyword") String keyword,
            @Param("auditStatus") Integer auditStatus);

    @Select("""
            <script>
            SELECT a.id, a.title, a.description AS content, a.user_id AS userId, a.audit_status AS auditStatus,
                   a.status, a.create_time AS createTime, COALESCE(u.nickname, u.username) AS authorName,
                   'activity' AS type
            FROM service_activity a
            LEFT JOIN sys_user u ON u.id = a.user_id
            WHERE a.deleted = 0
            <if test='keyword != null and keyword != ""'>
              AND a.title LIKE CONCAT('%', #{keyword}, '%')
            </if>
            <if test='auditStatus != null'>
              AND a.audit_status = #{auditStatus}
            </if>
            ORDER BY a.create_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectActivityAuditItems(@Param("keyword") String keyword,
            @Param("auditStatus") Integer auditStatus,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1) FROM service_activity a
            WHERE a.deleted = 0
            <if test='keyword != null and keyword != ""'>
              AND a.title LIKE CONCAT('%', #{keyword}, '%')
            </if>
            <if test='auditStatus != null'>
              AND a.audit_status = #{auditStatus}
            </if>
            </script>
            """)
    Long countActivityAuditItems(@Param("keyword") String keyword,
            @Param("auditStatus") Integer auditStatus);

    @Select("""
            <script>
            SELECT h.id, h.title, h.description AS content, h.user_id AS userId, h.audit_status AS auditStatus,
                   h.status, h.create_time AS createTime, COALESCE(u.nickname, u.username) AS authorName,
                   'help' AS type
            FROM service_help_request h
            LEFT JOIN sys_user u ON u.id = h.user_id
            WHERE h.deleted = 0
            <if test='keyword != null and keyword != ""'>
              AND h.title LIKE CONCAT('%', #{keyword}, '%')
            </if>
            <if test='auditStatus != null'>
              AND h.audit_status = #{auditStatus}
            </if>
            ORDER BY h.create_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectHelpAuditItems(@Param("keyword") String keyword,
            @Param("auditStatus") Integer auditStatus,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1) FROM service_help_request h
            WHERE h.deleted = 0
            <if test='keyword != null and keyword != ""'>
              AND h.title LIKE CONCAT('%', #{keyword}, '%')
            </if>
            <if test='auditStatus != null'>
              AND h.audit_status = #{auditStatus}
            </if>
            </script>
            """)
    Long countHelpAuditItems(@Param("keyword") String keyword,
            @Param("auditStatus") Integer auditStatus);

    @Select("""
            <script>
            SELECT l.id, l.title, l.description AS content, l.user_id AS userId, l.audit_status AS auditStatus,
                   l.status, l.create_time AS createTime, COALESCE(u.nickname, u.username) AS authorName,
                   'lostfound' AS type
            FROM service_lost_found l
            LEFT JOIN sys_user u ON u.id = l.user_id
            WHERE l.deleted = 0
            <if test='keyword != null and keyword != ""'>
              AND l.title LIKE CONCAT('%', #{keyword}, '%')
            </if>
            <if test='auditStatus != null'>
              AND l.audit_status = #{auditStatus}
            </if>
            ORDER BY l.create_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectLostFoundAuditItems(@Param("keyword") String keyword,
            @Param("auditStatus") Integer auditStatus,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1) FROM service_lost_found l
            WHERE l.deleted = 0
            <if test='keyword != null and keyword != ""'>
              AND l.title LIKE CONCAT('%', #{keyword}, '%')
            </if>
            <if test='auditStatus != null'>
              AND l.audit_status = #{auditStatus}
            </if>
            </script>
            """)
    Long countLostFoundAuditItems(@Param("keyword") String keyword,
            @Param("auditStatus") Integer auditStatus);

    @Update("""
            UPDATE forum_post
            SET audit_status = #{auditStatus},
                audit_remark = #{remark},
                status = CASE WHEN #{auditStatus} = 2 THEN 0 ELSE status END,
                update_time = NOW()
            WHERE id = #{id} AND deleted = 0
            """)
    int updatePostAuditStatus(@Param("id") Long id,
            @Param("auditStatus") Integer auditStatus,
            @Param("remark") String remark);

    @Update("""
            UPDATE forum_comment
            SET audit_status = #{auditStatus},
                status = CASE WHEN #{auditStatus} = 2 THEN 0 ELSE status END,
                update_time = NOW()
            WHERE id = #{id} AND deleted = 0
            """)
    int updateCommentAuditStatus(@Param("id") Long id,
            @Param("auditStatus") Integer auditStatus);

    @Update("""
            UPDATE service_product
            SET audit_status = #{auditStatus},
                status = CASE WHEN #{auditStatus} = 2 THEN 0 ELSE status END,
                update_time = NOW()
            WHERE id = #{id} AND deleted = 0
            """)
    int updateProductAuditStatus(@Param("id") Long id,
            @Param("auditStatus") Integer auditStatus);

    @Update("""
            UPDATE service_activity
            SET audit_status = #{auditStatus},
                status = CASE WHEN #{auditStatus} = 2 THEN 0 ELSE status END,
                update_time = NOW()
            WHERE id = #{id} AND deleted = 0
            """)
    int updateActivityAuditStatus(@Param("id") Long id,
            @Param("auditStatus") Integer auditStatus);

    @Update("""
            UPDATE service_help_request
            SET audit_status = #{auditStatus},
                status = CASE WHEN #{auditStatus} = 2 THEN 0 ELSE status END,
                update_time = NOW()
            WHERE id = #{id} AND deleted = 0
            """)
    int updateHelpAuditStatus(@Param("id") Long id,
            @Param("auditStatus") Integer auditStatus);

    @Update("""
            UPDATE service_lost_found
            SET audit_status = #{auditStatus},
                status = CASE WHEN #{auditStatus} = 2 THEN 0 ELSE status END,
                update_time = NOW()
            WHERE id = #{id} AND deleted = 0
            """)
    int updateLostFoundAuditStatus(@Param("id") Long id,
            @Param("auditStatus") Integer auditStatus);

    @Select("""
            <script>
            SELECT r.id, r.user_id AS userId, r.target_type AS targetType, r.target_id AS targetId,
                   r.reason_type AS reasonType, r.reason, r.images, r.status, r.handle_user_id AS handleUserId,
                   r.handle_result AS handleResult, r.handle_time AS handleTime, r.create_time AS createTime,
                   COALESCE(u.nickname, u.username) AS reporterName,
                   COALESCE(h.nickname, h.username) AS handlerName
            FROM sys_report r
            LEFT JOIN sys_user u ON u.id = r.user_id
            LEFT JOIN sys_user h ON h.id = r.handle_user_id
            WHERE 1 = 1
            <if test='status != null'>
              AND r.status = #{status}
            </if>
            <if test='targetType != null'>
              AND r.target_type = #{targetType}
            </if>
            ORDER BY r.create_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectReports(@Param("status") Integer status,
            @Param("targetType") Integer targetType,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1) FROM sys_report r
            WHERE 1 = 1
            <if test='status != null'>
              AND r.status = #{status}
            </if>
            <if test='targetType != null'>
              AND r.target_type = #{targetType}
            </if>
            </script>
            """)
    Long countReports(@Param("status") Integer status,
            @Param("targetType") Integer targetType);

    @Select("""
            SELECT r.id, r.user_id AS userId, r.target_type AS targetType, r.target_id AS targetId,
                   r.reason_type AS reasonType, r.reason, r.images, r.status
            FROM sys_report r
            WHERE r.id = #{id}
            LIMIT 1
            """)
    Map<String, Object> selectReportById(@Param("id") Long id);

    @Update("""
            UPDATE sys_report
            SET status = #{status},
                handle_result = #{handleResult},
                handle_user_id = #{handleUserId},
                handle_time = NOW()
            WHERE id = #{id}
            """)
    int handleReport(@Param("id") Long id,
            @Param("status") Integer status,
            @Param("handleResult") String handleResult,
            @Param("handleUserId") Long handleUserId);

    @Select("SELECT user_id AS userId FROM forum_post WHERE id = #{id} AND deleted = 0 LIMIT 1")
    Long selectPostAuthorId(@Param("id") Long id);

    @Select("SELECT user_id AS userId FROM forum_comment WHERE id = #{id} AND deleted = 0 LIMIT 1")
    Long selectCommentAuthorId(@Param("id") Long id);

    @Select("SELECT user_id AS userId FROM service_product WHERE id = #{id} AND deleted = 0 LIMIT 1")
    Long selectProductAuthorId(@Param("id") Long id);

    @Select("SELECT user_id AS userId FROM service_activity WHERE id = #{id} AND deleted = 0 LIMIT 1")
    Long selectActivityAuthorId(@Param("id") Long id);

    @Select("SELECT user_id AS userId FROM service_help_request WHERE id = #{id} AND deleted = 0 LIMIT 1")
    Long selectHelpAuthorId(@Param("id") Long id);

    @Select("SELECT user_id AS userId FROM service_lost_found WHERE id = #{id} AND deleted = 0 LIMIT 1")
    Long selectLostFoundAuthorId(@Param("id") Long id);

    @Select("SELECT COUNT(1) FROM sys_user WHERE deleted = 0")
    Long countTotalUsers();

    @Select("SELECT COUNT(1) FROM sys_user WHERE deleted = 0 AND is_verified = 1")
    Long countVerifiedUsers();

    @Select("SELECT COUNT(1) FROM forum_post WHERE deleted = 0")
    Long countForumPosts();

    @Select("SELECT COUNT(1) FROM forum_comment WHERE deleted = 0")
    Long countForumComments();

    @Select("SELECT COUNT(1) FROM service_product WHERE deleted = 0")
    Long countServiceProducts();

    @Select("SELECT COUNT(1) FROM service_activity WHERE deleted = 0")
    Long countServiceActivities();

    @Select("SELECT COUNT(1) FROM service_help_request WHERE deleted = 0")
    Long countServiceHelpRequests();

    @Select("SELECT COUNT(1) FROM service_lost_found WHERE deleted = 0")
    Long countServiceLostFound();

    @Select("SELECT COUNT(1) FROM forum_post WHERE deleted = 0 AND audit_status = 0")
    Long countPendingPostAudit();

    @Select("SELECT COUNT(1) FROM forum_comment WHERE deleted = 0 AND audit_status = 0")
    Long countPendingCommentAudit();

    @Select("SELECT COUNT(1) FROM service_product WHERE deleted = 0 AND audit_status = 0")
    Long countPendingProductAudit();

    @Select("SELECT COUNT(1) FROM service_activity WHERE deleted = 0 AND audit_status = 0")
    Long countPendingActivityAudit();

    @Select("SELECT COUNT(1) FROM service_help_request WHERE deleted = 0 AND audit_status = 0")
    Long countPendingHelpAudit();

    @Select("SELECT COUNT(1) FROM service_lost_found WHERE deleted = 0 AND audit_status = 0")
    Long countPendingLostFoundAudit();

    @Select("SELECT COUNT(1) FROM sys_report WHERE status = 0")
    Long countPendingReports();

    @Select("SELECT COUNT(1) FROM sys_report WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId} AND status = 0")
    int checkDuplicateReport(@Param("userId") Long userId,
                            @Param("targetType") Integer targetType,
                            @Param("targetId") Long targetId);

    @Insert("""
            INSERT INTO sys_report(user_id, target_type, target_id, reason_type, reason, images, status)
            VALUES(#{report.userId}, #{report.targetType}, #{report.targetId}, #{report.reasonType}, #{report.reason}, #{report.images}, 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "report.id")
    int insertReport(@Param("report") SysReport report);

    @Select("""
            SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS day, COUNT(1) AS total
            FROM sys_user
            WHERE deleted = 0 AND create_time >= #{startTime}
            GROUP BY DATE_FORMAT(create_time,'%Y-%m-%d')
            """)
    //统一使用 DATE 或者DATA_FORMAT
    List<Map<String, Object>> selectUserTrend(@Param("startTime") LocalDateTime startTime);

    @Select("""
            SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS day, COUNT(1) AS total
            FROM forum_post
            WHERE deleted = 0 AND create_time >= #{startTime}
            GROUP BY DATE_FORMAT(create_time,'%Y-%m-%d')
            """)
    List<Map<String, Object>> selectPostTrend(@Param("startTime") LocalDateTime startTime);

    @Select("""
            SELECT day, SUM(total) AS total
            FROM (
                SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS day, COUNT(1) AS total
                FROM service_product
                WHERE deleted = 0 AND create_time >= #{startTime}
                GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d')
                UNION ALL
                SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS day, COUNT(1) AS total
                FROM service_activity
                WHERE deleted = 0 AND create_time >= #{startTime}
                GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d')
                UNION ALL
                SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS day, COUNT(1) AS total
                FROM service_help_request
                WHERE deleted = 0 AND create_time >= #{startTime}
                GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d')
                UNION ALL
                SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS day, COUNT(1) AS total
                FROM service_lost_found
                WHERE deleted = 0 AND create_time >= #{startTime}
                GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d')
            ) t
            GROUP BY day
            """)
    List<Map<String, Object>> selectServiceTrend(@Param("startTime") LocalDateTime startTime);

    @Select("""
            SELECT s.section_name AS name, COUNT(p.id) AS value
            FROM forum_section s
            LEFT JOIN forum_post p ON p.section_id = s.id AND p.deleted = 0
            WHERE s.deleted = 0
            GROUP BY s.id, s.section_name
            ORDER BY value DESC
            """)
    List<Map<String, Object>> selectSectionDistribution();
}
