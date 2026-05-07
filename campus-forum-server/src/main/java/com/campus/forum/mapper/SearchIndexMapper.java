package com.campus.forum.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SearchIndexMapper {

    @Select("""
            SELECT p.id,
                   p.title,
                   p.content,
                   s.section_name AS sectionName,
                   u.nickname AS authorNickname,
                   p.like_count AS likeCount,
                   p.view_count AS viewCount,
                   p.create_time AS createTime
            FROM forum_post p
            LEFT JOIN forum_section s ON s.id = p.section_id
            LEFT JOIN sys_user u ON u.id = p.user_id
            WHERE p.deleted = 0
              AND p.status = 1
              AND p.audit_status = 1
            ORDER BY p.id ASC
            """)
    List<Map<String, Object>> selectAllPostDocs();

    @Select("""
            SELECT p.id,
                   p.title,
                   p.content,
                   s.section_name AS sectionName,
                   u.nickname AS authorNickname,
                   p.like_count AS likeCount,
                   p.view_count AS viewCount,
                   p.create_time AS createTime
            FROM forum_post p
            LEFT JOIN forum_section s ON s.id = p.section_id
            LEFT JOIN sys_user u ON u.id = p.user_id
            WHERE p.id = #{id}
              AND p.deleted = 0
              AND p.status = 1
              AND p.audit_status = 1
            LIMIT 1
            """)
    Map<String, Object> selectPostDocById(@Param("id") Long id);

    @Select("""
            SELECT p.id,
                   p.title,
                   p.description,
                   p.price,
                   u.nickname AS sellerNickname,
                   p.update_time AS updateTime
            FROM service_product p
            LEFT JOIN sys_user u ON u.id = p.user_id
            WHERE p.deleted = 0
              AND p.status IN (1, 3)
              AND p.audit_status = 1
            ORDER BY p.id ASC
            """)
    List<Map<String, Object>> selectAllProductDocs();

    @Select("""
            SELECT p.id,
                   p.title,
                   p.description,
                   p.price,
                   u.nickname AS sellerNickname,
                   p.update_time AS updateTime
            FROM service_product p
            LEFT JOIN sys_user u ON u.id = p.user_id
            WHERE p.id = #{id}
              AND p.deleted = 0
              AND p.status IN (1, 3)
              AND p.audit_status = 1
            LIMIT 1
            """)
    Map<String, Object> selectProductDocById(@Param("id") Long id);

    @Select("""
            SELECT a.id,
                   a.title,
                   a.description,
                   a.location,
                   a.start_time AS startTime,
                   a.organizer,
                   a.update_time AS updateTime
            FROM service_activity a
            WHERE a.deleted = 0
              AND a.status = 1
              AND a.audit_status = 1
            ORDER BY a.id ASC
            """)
    List<Map<String, Object>> selectAllActivityDocs();

    @Select("""
            SELECT a.id,
                   a.title,
                   a.description,
                   a.location,
                   a.start_time AS startTime,
                   a.organizer,
                   a.update_time AS updateTime
            FROM service_activity a
            WHERE a.id = #{id}
              AND a.deleted = 0
              AND a.status = 1
              AND a.audit_status = 1
            LIMIT 1
            """)
    Map<String, Object> selectActivityDocById(@Param("id") Long id);

    @Select("""
            SELECT h.id,
                   h.title,
                   h.description,
                   h.pickup_location AS pickupLocation,
                   h.delivery_location AS deliveryLocation,
                   h.expected_time AS expectedTime,
                   h.update_time AS updateTime
            FROM service_help_request h
            WHERE h.deleted = 0
              AND h.status = 1
              AND h.audit_status = 1
            ORDER BY h.id ASC
            """)
    List<Map<String, Object>> selectAllHelpDocs();

    @Select("""
            SELECT h.id,
                   h.title,
                   h.description,
                   h.pickup_location AS pickupLocation,
                   h.delivery_location AS deliveryLocation,
                   h.expected_time AS expectedTime,
                   h.update_time AS updateTime
            FROM service_help_request h
            WHERE h.id = #{id}
              AND h.deleted = 0
              AND h.status = 1
              AND h.audit_status = 1
            LIMIT 1
            """)
    Map<String, Object> selectHelpDocById(@Param("id") Long id);
}
