package com.campus.forum.mapper;

import com.campus.forum.entity.ServiceHelpRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ServiceHelpRequestMapper {

    @Select("""
            <script>
            SELECT * FROM service_help_request
            WHERE deleted = 0
              AND status IN (1,2,3,4,6)
              AND audit_status = 1
            <if test='type != null'>
              AND type = #{type}
            </if>
            <if test='keyword != null and keyword != ""'>
              AND (
                title LIKE CONCAT('%', #{keyword}, '%')
                OR description LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            ORDER BY create_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<ServiceHelpRequest> selectPage(@Param("type") Integer type,
            @Param("keyword") String keyword,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1) FROM service_help_request
            WHERE deleted = 0
              AND status IN (1,2,3,4,6)
              AND audit_status = 1
            <if test='type != null'>
              AND type = #{type}
            </if>
            <if test='keyword != null and keyword != ""'>
              AND (
                title LIKE CONCAT('%', #{keyword}, '%')
                OR description LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            </script>
            """)
    Long countPage(@Param("type") Integer type,
            @Param("keyword") String keyword);

    @Select("SELECT * FROM service_help_request WHERE id = #{id} AND deleted = 0")
    ServiceHelpRequest selectById(@Param("id") Long id);

    @Insert("""
            INSERT INTO service_help_request(user_id, helper_id, post_id, type, title, description, images, express_company,
              express_code, express_location, pickup_location, delivery_location, expected_time, reward, tips, contact_phone,
              fund_status, fund_freeze_time, fund_refund_time, fund_settle_time,
              view_count, audit_status, status, lock_deadline, publisher_confirmed, helper_confirmed, publisher_confirm_time,
              helper_confirm_time, complete_time, rating, rating_content, create_time, update_time, deleted)
            VALUES(#{userId}, NULL, #{postId}, #{type}, #{title}, #{description}, #{images}, #{expressCompany},
              #{expressCode}, #{expressLocation}, #{pickupLocation}, #{deliveryLocation}, #{expectedTime}, #{reward},
              #{tips}, #{contactPhone}, #{fundStatus}, #{fundFreezeTime}, NULL, NULL,
              0, #{auditStatus}, #{status}, NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NOW(), NOW(), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ServiceHelpRequest request);

    @Update("UPDATE service_help_request SET post_id = #{postId}, update_time = NOW() WHERE id = #{id} AND deleted = 0")
    int updatePostId(@Param("id") Long id, @Param("postId") Long postId);

    @Update("""
        UPDATE service_help_request
        SET helper_id = #{helperId},
          status = 2,
          lock_deadline = NULL,
          publisher_confirmed = 0,
          helper_confirmed = 0,
          publisher_confirm_time = NULL,
          helper_confirm_time = NULL,
          complete_time = NULL,
          update_time = NOW()
        WHERE id = #{id} AND deleted = 0 AND helper_id IS NULL AND status = 1
        """)
    int assignHelper(@Param("id") Long id, @Param("helperId") Long helperId);

    @Update("""
            UPDATE service_help_request
            SET lock_deadline = DATE_ADD(COALESCE(audit_pass_time, create_time), INTERVAL 180 SECOND),
                update_time = NOW()
            WHERE deleted = 0
              AND audit_status = 1
              AND status = 1
              AND helper_id IS NULL
              AND lock_deadline IS NULL
              AND (expected_time IS NULL OR expected_time > NOW())
            """)
    int initAllocateWindows();

    @Select("""
            SELECT * FROM service_help_request
            WHERE deleted = 0
              AND audit_status = 1
              AND status = 1
              AND helper_id IS NULL
              AND lock_deadline IS NOT NULL
              AND lock_deadline <= NOW()
              AND (expected_time IS NULL OR expected_time > NOW())
            ORDER BY lock_deadline ASC, id ASC
            """)
    java.util.List<ServiceHelpRequest> selectDueAllocateRequests();

    @Update("""
            UPDATE service_help_request
            SET lock_deadline = #{nextDeadline},
                update_time = NOW()
            WHERE id = #{id}
              AND deleted = 0
              AND status = 1
              AND helper_id IS NULL
            """)
    int extendAllocateWindow(@Param("id") Long id, @Param("nextDeadline") java.time.LocalDateTime nextDeadline);

    @Update("""
            UPDATE service_help_request
            SET status = 4,
                update_time = NOW()
            WHERE deleted = 0
              AND status = 1
              AND helper_id IS NULL
              AND expected_time IS NOT NULL
              AND expected_time <= NOW()
            """)
    int timeoutUnassignedRequests();

    @Update("UPDATE service_help_request SET status = 3, complete_time = NOW(), update_time = NOW() WHERE id = #{id} AND deleted = 0")
    int completeOrder(@Param("id") Long id);

    @Update("UPDATE service_help_request SET status = 3, complete_time = NOW(), update_time = NOW() WHERE id = #{id} AND deleted = 0 AND status = 2")
    int completeOrderByArbitration(@Param("id") Long id);

    @Update("""
        UPDATE service_help_request
        SET publisher_confirmed = 1,
          publisher_confirm_time = NOW(),
          update_time = NOW()
        WHERE id = #{id} AND deleted = 0 AND status = 2 AND user_id = #{userId}
        """)
    int publisherConfirmComplete(@Param("id") Long id, @Param("userId") Long userId);

    @Update("""
        UPDATE service_help_request
        SET helper_confirmed = 1,
          helper_confirm_time = NOW(),
          update_time = NOW()
        WHERE id = #{id} AND deleted = 0 AND status = 2 AND helper_id = #{userId}
        """)
    int helperConfirmComplete(@Param("id") Long id, @Param("userId") Long userId);

    @Update("""
        UPDATE service_help_request
        SET status = 3,
          complete_time = NOW(),
          update_time = NOW()
        WHERE id = #{id} AND deleted = 0 AND status = 2 AND publisher_confirmed = 1 AND helper_confirmed = 1
        """)
    int completeWhenBothConfirmed(@Param("id") Long id);

    @Update("UPDATE service_help_request SET view_count = view_count + 1, update_time = NOW() WHERE id = #{id} AND deleted = 0")
    int increaseViewCount(@Param("id") Long id);

    @Select("""
            <script>
            SELECT * FROM service_help_request
            WHERE deleted = 0
            <if test='type != null'>
              AND type = #{type}
            </if>
            <if test='status != null'>
              AND status = #{status}
            </if>
            <if test='auditStatus != null'>
              AND audit_status = #{auditStatus}
            </if>
            <if test='keyword != null and keyword != ""'>
              AND (
                title LIKE CONCAT('%', #{keyword}, '%')
                OR description LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            ORDER BY create_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<ServiceHelpRequest> selectAdminPage(@Param("type") Integer type,
            @Param("status") Integer status,
            @Param("auditStatus") Integer auditStatus,
            @Param("keyword") String keyword,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1) FROM service_help_request
            WHERE deleted = 0
            <if test='type != null'>
              AND type = #{type}
            </if>
            <if test='status != null'>
              AND status = #{status}
            </if>
            <if test='auditStatus != null'>
              AND audit_status = #{auditStatus}
            </if>
            <if test='keyword != null and keyword != ""'>
              AND (
                title LIKE CONCAT('%', #{keyword}, '%')
                OR description LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            </script>
            """)
    Long countAdminPage(@Param("type") Integer type,
            @Param("status") Integer status,
            @Param("auditStatus") Integer auditStatus,
            @Param("keyword") String keyword);

    @Update("UPDATE service_help_request SET audit_status = #{auditStatus}, audit_pass_time = NOW(), update_time = NOW() WHERE id = #{id} AND deleted = 0")
    int updateAuditStatus(@Param("id") Long id, @Param("auditStatus") Integer auditStatus);

    @Select("SELECT COUNT(1) FROM service_help_request WHERE deleted = 0")
    Long countAll();

    @Select("SELECT COUNT(1) FROM service_help_request WHERE deleted = 0 AND audit_status = #{auditStatus}")
    Long countByAuditStatus(@Param("auditStatus") Integer auditStatus);

    @Select("SELECT * FROM service_help_request WHERE status = 0 AND audit_status = 1 AND deleted = 0 AND audit_pass_time <= DATE_SUB(NOW(), INTERVAL 180 SECOND)")
    List<ServiceHelpRequest> selectPendingAllocationRequests();

    @Select("SELECT * FROM service_help_request WHERE status = 0 AND audit_status = 1 AND deleted = 0 AND audit_pass_time <= DATE_SUB(NOW(), INTERVAL 3 HOUR)")
    List<ServiceHelpRequest> selectExpiredRequests();

    @Update("UPDATE service_help_request SET status = 4, update_time = NOW() WHERE id = #{id}")
    int cancelOrder(@Param("id") Long id);

    @Update("UPDATE service_help_request SET fund_status = 2, fund_refund_time = NOW(), update_time = NOW() WHERE id = #{id} AND deleted = 0 AND fund_status = 1")
    int markFundRefunded(@Param("id") Long id);

    @Update("UPDATE service_help_request SET fund_status = 3, fund_settle_time = NOW(), update_time = NOW() WHERE id = #{id} AND deleted = 0 AND fund_status = 1")
    int markFundSettled(@Param("id") Long id);

    @Update("""
            UPDATE sys_user u
            INNER JOIN service_help_request h ON h.user_id = u.id
            SET u.balance = u.balance + h.reward
            WHERE h.deleted = 0
              AND h.status = 4
              AND h.helper_id IS NULL
              AND h.fund_status = 1
            """)
    int refundTimeoutFundsToPublisher();

    @Update("""
            UPDATE service_help_request
            SET fund_status = 2,
                fund_refund_time = NOW(),
                update_time = NOW()
            WHERE deleted = 0
              AND status = 4
              AND helper_id IS NULL
              AND fund_status = 1
            """)
    int markTimeoutFundsRefunded();

    @Select("""
            SELECT *
            FROM service_help_request
            WHERE deleted = 0
              AND status = 4
              AND helper_id IS NULL
              AND fund_status = 1
            """)
    List<ServiceHelpRequest> selectTimeoutUnrefundedRequests();

    @Update("UPDATE service_help_request SET is_frozen = #{isFrozen}, complaint_status = #{complaintStatus}, freeze_time = NOW() WHERE id = #{id}")
    int updateFreezeStatus(@Param("id") Long id, @Param("isFrozen") Integer isFrozen, @Param("complaintStatus") Integer complaintStatus);
}
