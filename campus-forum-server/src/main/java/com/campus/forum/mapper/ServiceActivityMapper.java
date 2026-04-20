package com.campus.forum.mapper;

import com.campus.forum.entity.ServiceActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ServiceActivityMapper {

    @Select("""
            <script>
            SELECT * FROM service_activity
            WHERE deleted = 0
              AND status IN (1,2)
              AND audit_status = 1
            <if test='type != null and type != ""'>
              AND activity_type = #{type}
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
    List<ServiceActivity> selectPage(@Param("type") String type,
            @Param("keyword") String keyword,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1) FROM service_activity
            WHERE deleted = 0
              AND status IN (1,2)
              AND audit_status = 1
            <if test='type != null and type != ""'>
              AND activity_type = #{type}
            </if>
            <if test='keyword != null and keyword != ""'>
              AND (
                title LIKE CONCAT('%', #{keyword}, '%')
                OR description LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            </script>
            """)
    Long countPage(@Param("type") String type,
            @Param("keyword") String keyword);

    @Select("SELECT * FROM service_activity WHERE id = #{id} AND deleted = 0")
    ServiceActivity selectById(@Param("id") Long id);

    @Insert("""
            INSERT INTO service_activity(user_id, post_id, title, description, cover_image, images, activity_type,
              start_time, end_time, signup_start_time, signup_end_time, location, max_participants, current_participants,
              organizer, contact_name, contact_phone, fee, requirements, view_count, audit_status, status, create_time,
              update_time, deleted)
            VALUES(#{userId}, #{postId}, #{title}, #{description}, #{coverImage}, #{images}, #{activityType},
              #{startTime}, #{endTime}, #{signupStartTime}, #{signupEndTime}, #{location}, #{maxParticipants}, 0,
              #{organizer}, #{contactName}, #{contactPhone}, #{fee}, #{requirements}, 0, #{auditStatus}, #{status},
              NOW(), NOW(), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ServiceActivity activity);

    @Update("UPDATE service_activity SET post_id = #{postId}, update_time = NOW() WHERE id = #{id} AND deleted = 0")
    int updatePostId(@Param("id") Long id, @Param("postId") Long postId);

    @Update("UPDATE service_activity SET view_count = view_count + 1, update_time = NOW() WHERE id = #{id} AND deleted = 0")
    int increaseViewCount(@Param("id") Long id);

    @Insert("INSERT IGNORE INTO service_activity_signup(activity_id, user_id, status, signup_time) VALUES(#{activityId}, #{userId}, 1, NOW())")
    int insertSignup(@Param("activityId") Long activityId, @Param("userId") Long userId);

    @Update("DELETE FROM service_activity_signup WHERE activity_id = #{activityId} AND user_id = #{userId}")
    int deleteSignup(@Param("activityId") Long activityId, @Param("userId") Long userId);

    @Select("SELECT COUNT(1) FROM service_activity_signup WHERE activity_id = #{activityId} AND user_id = #{userId}")
    Long countSignup(@Param("activityId") Long activityId, @Param("userId") Long userId);

    @Update("UPDATE service_activity SET current_participants = current_participants + #{delta}, update_time = NOW() WHERE id = #{activityId} AND deleted = 0")
    int updateParticipantCount(@Param("activityId") Long activityId, @Param("delta") Integer delta);

    @Select("""
            <script>
            SELECT * FROM service_activity
            WHERE deleted = 0
            <if test='type != null and type != ""'>
              AND activity_type = #{type}
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
                OR organizer LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            ORDER BY create_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<ServiceActivity> selectAdminPage(@Param("type") String type,
            @Param("status") Integer status,
            @Param("auditStatus") Integer auditStatus,
            @Param("keyword") String keyword,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1) FROM service_activity
            WHERE deleted = 0
            <if test='type != null and type != ""'>
              AND activity_type = #{type}
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
                OR organizer LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            </script>
            """)
    Long countAdminPage(@Param("type") String type,
            @Param("status") Integer status,
            @Param("auditStatus") Integer auditStatus,
            @Param("keyword") String keyword);

    @Update("UPDATE service_activity SET audit_status = #{auditStatus}, update_time = NOW() WHERE id = #{id} AND deleted = 0")
    int updateAuditStatus(@Param("id") Long id, @Param("auditStatus") Integer auditStatus);

    @Select("SELECT COUNT(1) FROM service_activity WHERE deleted = 0")
    Long countAll();

    @Select("SELECT COUNT(1) FROM service_activity WHERE deleted = 0 AND audit_status = #{auditStatus}")
    Long countByAuditStatus(@Param("auditStatus") Integer auditStatus);
}
