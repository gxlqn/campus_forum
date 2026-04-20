package com.campus.forum.mapper;

import com.campus.forum.entity.AuditLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AuditLogMapper {

    @Insert("INSERT INTO audit_log (target_type, target_id, user_id, filter_result, matched_keywords, matched_level, " +
            "ai_audit_status, ai_confidence, ai_labels, ai_response_time, final_status, audit_method, auditor_id, audit_remark, content_snapshot) " +
            "VALUES (#{targetType}, #{targetId}, #{userId}, #{filterResult}, #{matchedKeywords}, #{matchedLevel}, " +
            "#{aiAuditStatus}, #{aiConfidence}, #{aiLabels}, #{aiResponseTime}, #{finalStatus}, #{auditMethod}, #{auditorId}, #{auditRemark}, #{contentSnapshot})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AuditLog log);

    @Select("SELECT * FROM audit_log WHERE id = #{id}")
    AuditLog selectById(@Param("id") Long id);

    @Select("SELECT * FROM audit_log WHERE target_type = #{targetType} AND target_id = #{targetId}")
    AuditLog selectByTarget(@Param("targetType") String targetType, @Param("targetId") Long targetId);

    @Update("UPDATE audit_log SET final_status = #{finalStatus}, audit_method = #{auditMethod}, " +
            "auditor_id = #{auditorId}, audit_remark = #{auditRemark}, updated_at = NOW() WHERE id = #{id}")
    int updateAuditResult(AuditLog log);

    @Select("SELECT * FROM audit_log WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{offset}, #{size}")
    List<AuditLog> selectByUserId(@Param("userId") Long userId, @Param("offset") long offset, @Param("size") long size);

    @Select("SELECT COUNT(*) FROM audit_log WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM audit_log WHERE final_status = #{finalStatus} ORDER BY created_at DESC LIMIT #{offset}, #{size}")
    List<AuditLog> selectByFinalStatus(@Param("finalStatus") Integer finalStatus, @Param("offset") long offset, @Param("size") long size);

    @Select("SELECT COUNT(*) FROM audit_log WHERE final_status = #{finalStatus}")
    int countByFinalStatus(@Param("finalStatus") Integer finalStatus);

    @Select("SELECT * FROM audit_log WHERE created_at >= DATE_SUB(NOW(), INTERVAL #{days} DAY) ORDER BY created_at DESC")
    List<AuditLog> selectRecent(@Param("days") int days);

    @Delete("DELETE FROM audit_log WHERE created_at < DATE_SUB(NOW(), INTERVAL #{days} DAY)")
    int deleteOld(@Param("days") int days);
}
