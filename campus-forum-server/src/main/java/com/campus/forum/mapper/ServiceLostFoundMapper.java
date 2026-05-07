package com.campus.forum.mapper;

import com.campus.forum.entity.ServiceLostFound;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ServiceLostFoundMapper {

    @Select("""
            <script>
            SELECT * FROM service_lost_found
            WHERE deleted = 0
              AND status IN (1, 2)
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
            ORDER BY status ASC, create_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<ServiceLostFound> selectPage(@Param("type") Integer type,
            @Param("keyword") String keyword,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1) FROM service_lost_found
            WHERE deleted = 0
              AND status IN (1, 2)
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

    @Select("SELECT * FROM service_lost_found WHERE id = #{id} AND deleted = 0")
    ServiceLostFound selectById(@Param("id") Long id);

    @Insert("""
            INSERT INTO service_lost_found(user_id, post_id, type, title, description, images, item_name, item_category,
              lost_time, lost_location, contact_name, contact_phone, contact_wechat, reward, view_count, audit_status,
              status, create_time, update_time, deleted)
            VALUES(#{userId}, #{postId}, #{type}, #{title}, #{description}, #{images}, #{itemName}, #{itemCategory},
              #{lostTime}, #{lostLocation}, #{contactName}, #{contactPhone}, #{contactWechat}, #{reward}, 0,
              #{auditStatus}, #{status}, NOW(), NOW(), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ServiceLostFound item);

    @Update("""
            UPDATE service_lost_found
            SET type = #{type},
                title = #{title},
                description = #{description},
                images = #{images},
                item_name = #{itemName},
                item_category = #{itemCategory},
                lost_time = #{lostTime},
                lost_location = #{lostLocation},
                contact_name = #{contactName},
                contact_phone = #{contactPhone},
                contact_wechat = #{contactWechat},
                reward = #{reward},
                status = #{status},
                update_time = NOW()
            WHERE id = #{id} AND user_id = #{userId} AND deleted = 0
            """)
    int updateByOwner(ServiceLostFound item);

    @Update("UPDATE service_lost_found SET deleted = 1, update_time = NOW() WHERE id = #{id} AND user_id = #{userId} AND deleted = 0")
    int deleteByOwner(@Param("id") Long id, @Param("userId") Long userId);

    /** 管理员删除（不限制 owner） */
    @Update("UPDATE service_lost_found SET deleted = 1, update_time = NOW() WHERE id = #{id} AND deleted = 0")
    int deleteById(@Param("id") Long id);

    @Update("UPDATE service_lost_found SET status = 2, update_time = NOW() WHERE id = #{id} AND user_id = #{userId} AND deleted = 0")
    int markComplete(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE service_lost_found SET view_count = view_count + 1, update_time = NOW() WHERE id = #{id} AND deleted = 0")
    int increaseViewCount(@Param("id") Long id);

    @Update("UPDATE service_lost_found SET post_id = #{postId}, update_time = NOW() WHERE id = #{id} AND deleted = 0")
    int updatePostId(@Param("id") Long id, @Param("postId") Long postId);

    @Select("""
            <script>
            SELECT * FROM service_lost_found
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
                OR item_name LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            ORDER BY create_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<ServiceLostFound> selectAdminPage(@Param("type") Integer type,
            @Param("status") Integer status,
            @Param("auditStatus") Integer auditStatus,
            @Param("keyword") String keyword,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1) FROM service_lost_found
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
                OR item_name LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            </script>
            """)
    Long countAdminPage(@Param("type") Integer type,
            @Param("status") Integer status,
            @Param("auditStatus") Integer auditStatus,
            @Param("keyword") String keyword);

    @Update("UPDATE service_lost_found SET audit_status = #{auditStatus}, update_time = NOW() WHERE id = #{id} AND deleted = 0")
    int updateAuditStatus(@Param("id") Long id, @Param("auditStatus") Integer auditStatus);

    @Update("UPDATE service_lost_found SET status = #{status}, update_time = NOW() WHERE id = #{id} AND deleted = 0")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Select("SELECT COUNT(1) FROM service_lost_found WHERE deleted = 0")
    Long countAll();

    @Select("SELECT COUNT(1) FROM service_lost_found WHERE deleted = 0 AND audit_status = #{auditStatus}")
    Long countByAuditStatus(@Param("auditStatus") Integer auditStatus);
}
