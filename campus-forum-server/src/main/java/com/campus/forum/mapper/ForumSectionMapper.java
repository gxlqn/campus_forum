package com.campus.forum.mapper;

import com.campus.forum.entity.ForumSection;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ForumSectionMapper {

    @Select("""
            SELECT s.id,
               s.section_name,
               s.section_code,
               s.description,
               s.icon,
               s.cover_image,
               s.sort,
               s.status,
               s.is_default,
               s.create_time,
               s.update_time,
               s.deleted,
               (
                   SELECT COUNT(1)
                   FROM forum_post p
                   WHERE p.section_id = s.id
                 AND p.deleted = 0
                 AND p.status = 1
                 AND p.audit_status = 1
               ) AS post_count
            FROM forum_section s
            WHERE s.deleted = 0 AND s.status = 1
            ORDER BY sort ASC, id ASC
            """)
    List<ForumSection> selectEnabledSections();

    @Select("SELECT * FROM forum_section WHERE id = #{id} AND deleted = 0")
    ForumSection selectById(@Param("id") Long id);

    @Select("SELECT * FROM forum_section WHERE section_code = #{sectionCode} AND deleted = 0 LIMIT 1")
    ForumSection selectByCode(@Param("sectionCode") String sectionCode);
}
