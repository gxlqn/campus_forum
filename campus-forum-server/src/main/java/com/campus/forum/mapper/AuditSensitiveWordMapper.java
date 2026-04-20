package com.campus.forum.mapper;

import com.campus.forum.entity.AuditSensitiveWord;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Set;

@Mapper
public interface AuditSensitiveWordMapper {

    @Select("SELECT * FROM audit_sensitive_word WHERE is_enabled = 1 ORDER BY level DESC, LENGTH(word) DESC")
    List<AuditSensitiveWord> selectAllEnabled();

    @Select("SELECT * FROM audit_sensitive_word WHERE is_enabled = 1 AND category = #{category} ORDER BY level DESC, LENGTH(word) DESC")
    List<AuditSensitiveWord> selectByCategory(@Param("category") Integer category);

    @Select("SELECT * FROM audit_sensitive_word WHERE is_enabled = 1 AND level >= #{level} ORDER BY level DESC, LENGTH(word) DESC")
    List<AuditSensitiveWord> selectByMinLevel(@Param("level") Integer level);

    @Select("SELECT word FROM audit_sensitive_word WHERE is_enabled = 1")
    Set<String> selectAllWords();

    @Insert("INSERT INTO audit_sensitive_word (word, category, level, replacement, is_enabled, remark, creator_id) " +
            "VALUES (#{word}, #{category}, #{level}, #{replacement}, #{isEnabled}, #{remark}, #{creatorId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AuditSensitiveWord word);

    @Update("UPDATE audit_sensitive_word SET word = #{word}, category = #{category}, level = #{level}, " +
            "replacement = #{replacement}, is_enabled = #{isEnabled}, remark = #{remark} WHERE id = #{id}")
    int update(AuditSensitiveWord word);

    @Delete("DELETE FROM audit_sensitive_word WHERE id = #{id}")
    int deleteById(@Param("id") Integer id);

    @Select("SELECT COUNT(*) FROM audit_sensitive_word WHERE is_enabled = 1 AND category = #{category}")
    int countByCategory(@Param("category") Integer category);

    @Select("SELECT COUNT(*) FROM audit_sensitive_word WHERE is_enabled = 1")
    int countEnabled();

    @Select("SELECT * FROM audit_sensitive_word ORDER BY id LIMIT #{offset}, #{size}")
    List<AuditSensitiveWord> selectPage(@Param("offset") long offset, @Param("size") long size);

    @Select("SELECT COUNT(*) FROM audit_sensitive_word")
    int countTotal();
}
