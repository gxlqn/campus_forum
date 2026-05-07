package com.campus.forum.mapper;

import com.campus.forum.entity.AuditSensitiveWord;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Set;

@Mapper
public interface AuditSensitiveWordMapper {

    @Select("SELECT * FROM audit_sensitive_word WHERE is_enabled = 1 ORDER BY word_type ASC, level DESC, LENGTH(word) DESC")
    List<AuditSensitiveWord> selectAllEnabled();

    @Select("SELECT * FROM audit_sensitive_word WHERE is_enabled = 1 AND category = #{category} ORDER BY word_type ASC, level DESC, LENGTH(word) DESC")
    List<AuditSensitiveWord> selectByCategory(@Param("category") Integer category);

    @Select("SELECT * FROM audit_sensitive_word WHERE is_enabled = 1 AND level >= #{level} ORDER BY word_type ASC, level DESC, LENGTH(word) DESC")
    List<AuditSensitiveWord> selectByMinLevel(@Param("level") Integer level);

    @Select("SELECT word FROM audit_sensitive_word WHERE is_enabled = 1 AND word_type = 1")
    Set<String> selectAllWords();

    @Insert("INSERT INTO audit_sensitive_word (word, word_type, category, level, replacement, is_enabled, remark, creator_id) " +
            "VALUES (#{word}, #{wordType}, #{category}, #{level}, #{replacement}, #{isEnabled}, #{remark}, #{creatorId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AuditSensitiveWord word);

    @Update("UPDATE audit_sensitive_word SET word = #{word}, word_type = #{wordType}, category = #{category}, level = #{level}, " +
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

    @Select("<script>SELECT * FROM audit_sensitive_word WHERE 1=1"
            + "<if test='wordType != null'> AND word_type = #{wordType}</if>"
            + "<if test='category != null'> AND category = #{category}</if>"
            + "<if test='keyword != null and keyword != \"\"'> AND word LIKE CONCAT('%', #{keyword}, '%')</if>"
            + " ORDER BY word_type ASC, id LIMIT #{offset}, #{size}</script>")
    List<AuditSensitiveWord> selectPageWithFilter(@Param("offset") long offset, @Param("size") long size,
            @Param("wordType") Integer wordType, @Param("category") Integer category, @Param("keyword") String keyword);

    @Select("<script>SELECT COUNT(*) FROM audit_sensitive_word WHERE 1=1"
            + "<if test='wordType != null'> AND word_type = #{wordType}</if>"
            + "<if test='category != null'> AND category = #{category}</if>"
            + "<if test='keyword != null and keyword != \"\"'> AND word LIKE CONCAT('%', #{keyword}, '%')</if>"
            + "</script>")
    int countWithFilter(@Param("wordType") Integer wordType, @Param("category") Integer category, @Param("keyword") String keyword);
}
