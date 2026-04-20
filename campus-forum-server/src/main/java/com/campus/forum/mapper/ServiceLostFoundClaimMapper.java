package com.campus.forum.mapper;

import com.campus.forum.entity.ServiceLostFoundClaim;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ServiceLostFoundClaimMapper {

    @Insert("""
            INSERT INTO service_lost_found_claim(lost_found_id, user_id, description, images, status, create_time, update_time)
            VALUES(#{lostFoundId}, #{userId}, #{description}, #{images}, 0, NOW(), NOW())
            """)
    int insert(ServiceLostFoundClaim claim);

    @Select("""
            SELECT c.*, u.nickname as applicantName, u.avatar as applicantAvatar,
                   lf.title as lostFoundTitle, lf.type as lostFoundType
            FROM service_lost_found_claim c
            LEFT JOIN sys_user u ON c.user_id = u.id
            LEFT JOIN service_lost_found lf ON c.lost_found_id = lf.id
            WHERE c.id = #{id}
            """)
    Map<String, Object> selectClaimDetail(@Param("id") Long id);

    @Select("""
            <script>
            SELECT c.*, u.nickname as applicantName, lf.title as lostFoundTitle
            FROM service_lost_found_claim c
            LEFT JOIN sys_user u ON c.user_id = u.id
            LEFT JOIN service_lost_found lf ON c.lost_found_id = lf.id
            WHERE 1=1
            <if test='status != null'>
              AND c.status = #{status}
            </if>
            ORDER BY c.create_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectClaimPage(@Param("status") Integer status, @Param("offset") Long offset, @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM service_lost_found_claim
            WHERE 1=1
            <if test='status != null'>
              AND status = #{status}
            </if>
            </script>
            """)
    Long countClaimPage(@Param("status") Integer status);

    @Update("""
            UPDATE service_lost_found_claim
            SET status = #{status}, 
                audit_remark = #{auditRemark}, 
                auditor_id = #{auditorId}, 
                audit_time = NOW(),
                update_time = NOW()
            WHERE id = #{id}
            """)
    int auditClaim(@Param("id") Long id, @Param("status") Integer status, @Param("auditRemark") String auditRemark, @Param("auditorId") Long auditorId);

    @Select("SELECT * FROM service_lost_found_claim WHERE id = #{id}")
    ServiceLostFoundClaim selectById(@Param("id") Long id);

    @Select("SELECT u.* FROM service_lost_found_claim c JOIN sys_user u ON c.user_id = u.id WHERE c.lost_found_id = #{lostFoundId} AND c.status = 1 LIMIT 1")
    com.campus.forum.entity.SysUser selectClaimerByLostFoundId(@Param("lostFoundId") Long lostFoundId);

    @Select("SELECT * FROM service_lost_found_claim WHERE lost_found_id = #{lostFoundId} AND user_id = #{userId} AND status != 2 LIMIT 1")
    ServiceLostFoundClaim checkDuplicateClaim(@Param("lostFoundId") Long lostFoundId, @Param("userId") Long userId);
}
