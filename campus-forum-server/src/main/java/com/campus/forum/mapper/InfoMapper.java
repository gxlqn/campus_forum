package com.campus.forum.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface InfoMapper {

    @Select("""
            <script>
            SELECT id, title, summary, content, cover_image AS coverImage, source, source_url AS sourceUrl,
              category, view_count AS viewCount, is_top AS isTop, status, publish_time AS publishTime,
              create_time AS createTime, update_time AS updateTime
            FROM info_news
            WHERE deleted = 0
              AND status = 1
            <if test='category != null and category != ""'>
              AND category = #{category}
            </if>
            <if test='keyword != null and keyword != ""'>
              AND (
                title LIKE CONCAT('%', #{keyword}, '%')
                OR summary LIKE CONCAT('%', #{keyword}, '%')
                OR content LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            ORDER BY is_top DESC, publish_time DESC, id DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectNewsPage(@Param("category") String category,
            @Param("keyword") String keyword,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM info_news
            WHERE deleted = 0
              AND status = 1
            <if test='category != null and category != ""'>
              AND category = #{category}
            </if>
            <if test='keyword != null and keyword != ""'>
              AND (
                title LIKE CONCAT('%', #{keyword}, '%')
                OR summary LIKE CONCAT('%', #{keyword}, '%')
                OR content LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            </script>
            """)
    Long countNewsPage(@Param("category") String category, @Param("keyword") String keyword);

    @Select("""
            SELECT id, title, summary, content, cover_image AS coverImage, source, source_url AS sourceUrl,
              category, view_count AS viewCount, is_top AS isTop, status, publish_time AS publishTime,
              create_time AS createTime, update_time AS updateTime
            FROM info_news
            WHERE id = #{id}
              AND deleted = 0
              AND status = 1
            LIMIT 1
            """)
    Map<String, Object> selectNewsById(@Param("id") Long id);

    @Update("UPDATE info_news SET view_count = view_count + 1, update_time = NOW() WHERE id = #{id} AND deleted = 0")
    int increaseNewsViewCount(@Param("id") Long id);

    @Select("""
            SELECT DISTINCT category
            FROM info_news
            WHERE deleted = 0
              AND status = 1
              AND category IS NOT NULL
              AND category != ''
            ORDER BY category ASC
            """)
    List<String> selectNewsCategories();

    @Select("""
            <script>
            SELECT id, category, name, description, icon, url, phone, address, longitude, latitude,
              sort, status, create_time AS createTime, update_time AS updateTime
            FROM info_service_nav
            WHERE status = 1
            <if test='category != null and category != ""'>
              AND category = #{category}
            </if>
            <if test='keyword != null and keyword != ""'>
              AND (
                name LIKE CONCAT('%', #{keyword}, '%')
                OR description LIKE CONCAT('%', #{keyword}, '%')
                OR address LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            ORDER BY sort ASC, id ASC
            </script>
            """)
    List<Map<String, Object>> selectServiceNavList(@Param("category") String category, @Param("keyword") String keyword);

    @Select("""
            SELECT DISTINCT category
            FROM info_service_nav
            WHERE status = 1
              AND category IS NOT NULL
              AND category != ''
            ORDER BY category ASC
            """)
    List<String> selectServiceNavCategories();

    @Select("""
            <script>
            SELECT id, title, summary, content, cover_image AS coverImage, source, source_url AS sourceUrl,
              category, view_count AS viewCount, is_top AS isTop, status, publish_time AS publishTime,
              create_time AS createTime, update_time AS updateTime
            FROM info_news
            WHERE deleted = 0
            <if test='category != null and category != ""'>
              AND category = #{category}
            </if>
            <if test='keyword != null and keyword != ""'>
              AND (
                title LIKE CONCAT('%', #{keyword}, '%')
                OR summary LIKE CONCAT('%', #{keyword}, '%')
                OR content LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            <if test='status != null'>
              AND status = #{status}
            </if>
            ORDER BY is_top DESC, publish_time DESC, id DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectAdminNewsPage(@Param("category") String category,
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM info_news
            WHERE deleted = 0
            <if test='category != null and category != ""'>
              AND category = #{category}
            </if>
            <if test='keyword != null and keyword != ""'>
              AND (
                title LIKE CONCAT('%', #{keyword}, '%')
                OR summary LIKE CONCAT('%', #{keyword}, '%')
                OR content LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            <if test='status != null'>
              AND status = #{status}
            </if>
            </script>
            """)
    Long countAdminNewsPage(@Param("category") String category,
            @Param("keyword") String keyword,
            @Param("status") Integer status);

    @Select("""
            SELECT id, title, summary, content, cover_image AS coverImage, source, source_url AS sourceUrl,
              category, view_count AS viewCount, is_top AS isTop, status, publish_time AS publishTime,
              create_time AS createTime, update_time AS updateTime
            FROM info_news
            WHERE id = #{id}
              AND deleted = 0
            LIMIT 1
            """)
    Map<String, Object> selectAdminNewsById(@Param("id") Long id);

    @Insert("""
            <script>
            INSERT INTO info_news(
              title, summary, content, cover_image, source, source_url, category,
              is_top, status, publish_time, create_time, update_time, deleted
            ) VALUES (
              #{title}, #{summary}, #{content}, #{coverImage}, #{source}, #{sourceUrl}, #{category},
              #{isTop}, #{status},
              CASE
                WHEN #{publishTime} IS NULL OR #{publishTime} = '' THEN NOW()
                ELSE #{publishTime}
              END,
              NOW(), NOW(), 0
            )
            </script>
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertAdminNews(Map<String, Object> payload);

    @Update("""
            <script>
            UPDATE info_news
            SET title = #{title},
                summary = #{summary},
                content = #{content},
                cover_image = #{coverImage},
                source = #{source},
                source_url = #{sourceUrl},
                category = #{category},
                is_top = #{isTop},
                status = #{status},
                publish_time = CASE
                  WHEN #{publishTime} IS NULL OR #{publishTime} = '' THEN publish_time
                  ELSE #{publishTime}
                END,
                update_time = NOW()
            WHERE id = #{id}
              AND deleted = 0
            </script>
            """)
    int updateAdminNews(Map<String, Object> payload);

    @Update("""
            UPDATE info_news
            SET status = #{status},
                update_time = NOW()
            WHERE id = #{id}
              AND deleted = 0
            """)
    int updateAdminNewsStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("""
            UPDATE info_news
            SET deleted = 1,
                update_time = NOW()
            WHERE id = #{id}
              AND deleted = 0
            """)
    int deleteAdminNews(@Param("id") Long id);

    @Select("""
            SELECT DISTINCT category
            FROM info_news
            WHERE deleted = 0
              AND category IS NOT NULL
              AND category != ''
            ORDER BY category ASC
            """)
    List<String> selectAdminNewsCategories();

    @Select("""
            <script>
            SELECT id, category, name, description, icon, url, phone, address, longitude, latitude,
              sort, status, create_time AS createTime, update_time AS updateTime
            FROM info_service_nav
            WHERE 1 = 1
            <if test='category != null and category != ""'>
              AND category = #{category}
            </if>
            <if test='keyword != null and keyword != ""'>
              AND (
                name LIKE CONCAT('%', #{keyword}, '%')
                OR description LIKE CONCAT('%', #{keyword}, '%')
                OR address LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            <if test='status != null'>
              AND status = #{status}
            </if>
            ORDER BY sort ASC, id ASC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectAdminNavPage(@Param("category") String category,
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM info_service_nav
            WHERE 1 = 1
            <if test='category != null and category != ""'>
              AND category = #{category}
            </if>
            <if test='keyword != null and keyword != ""'>
              AND (
                name LIKE CONCAT('%', #{keyword}, '%')
                OR description LIKE CONCAT('%', #{keyword}, '%')
                OR address LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            <if test='status != null'>
              AND status = #{status}
            </if>
            </script>
            """)
    Long countAdminNavPage(@Param("category") String category,
            @Param("keyword") String keyword,
            @Param("status") Integer status);

    @Insert("""
            INSERT INTO info_service_nav(
              category, name, description, icon, url, phone, address, longitude, latitude,
              sort, status, create_time, update_time
            ) VALUES (
              #{category}, #{name}, #{description}, #{icon}, #{url}, #{phone}, #{address}, #{longitude}, #{latitude},
              #{sort}, #{status}, NOW(), NOW()
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertAdminNav(Map<String, Object> payload);

    @Update("""
            UPDATE info_service_nav
            SET category = #{category},
                name = #{name},
                description = #{description},
                icon = #{icon},
                url = #{url},
                phone = #{phone},
                address = #{address},
                longitude = #{longitude},
                latitude = #{latitude},
                sort = #{sort},
                status = #{status},
                update_time = NOW()
            WHERE id = #{id}
            """)
    int updateAdminNav(Map<String, Object> payload);

    @Update("""
            UPDATE info_service_nav
            SET status = #{status},
                update_time = NOW()
            WHERE id = #{id}
            """)
    int updateAdminNavStatus(@Param("id") Long id, @Param("status") Integer status);

    @Delete("DELETE FROM info_service_nav WHERE id = #{id}")
    int deleteAdminNav(@Param("id") Long id);

    @Select("""
            SELECT DISTINCT category
            FROM info_service_nav
            WHERE category IS NOT NULL
              AND category != ''
            ORDER BY category ASC
            """)
    List<String> selectAdminNavCategories();
}
