package com.campus.forum.mapper;

import com.campus.forum.entity.ForumPost;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface ForumPostMapper {

    @Select("""
            <script>
            SELECT * FROM forum_post
            WHERE deleted = 0
              AND status = 1
              AND audit_status = 1
            <if test='sectionId != null'>
              AND section_id = #{sectionId}
            </if>
            <if test='keyword != null and keyword != ""'>
              AND (
                title LIKE CONCAT('%', #{keyword}, '%')
                OR content LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            <if test='orderBy == "hot"'>
              AND (is_top = 1 OR create_time >= DATE_SUB(NOW(), INTERVAL #{hotWindowHours} HOUR))
            </if>
            <choose>
              <when test='orderBy == "hot"'>
                ORDER BY is_top DESC,
                         (
                            (IFNULL(like_count, 0) * 3 + IFNULL(comment_count, 0) * 5 + IFNULL(view_count, 0)) /
                            POW((TIMESTAMPDIFF(HOUR, create_time, NOW()) + 2), 0.5)
                         ) DESC,
                         create_time DESC
              </when>
              <otherwise>
                ORDER BY is_top DESC, create_time DESC
              </otherwise>
            </choose>
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<ForumPost> selectPostPage(@Param("sectionId") Long sectionId,
            @Param("keyword") String keyword,
            @Param("orderBy") String orderBy,
          @Param("hotWindowHours") Integer hotWindowHours,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1) FROM forum_post
            WHERE deleted = 0
              AND status = 1
              AND audit_status = 1
            <if test='sectionId != null'>
              AND section_id = #{sectionId}
            </if>
            <if test='keyword != null and keyword != ""'>
              AND (
                title LIKE CONCAT('%', #{keyword}, '%')
                OR content LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            <if test='orderBy == "hot"'>
              AND (is_top = 1 OR create_time >= DATE_SUB(NOW(), INTERVAL #{hotWindowHours} HOUR))
            </if>
            </script>
            """)
    Long countPostPage(@Param("sectionId") Long sectionId,
            @Param("keyword") String keyword,
            @Param("orderBy") String orderBy,
            @Param("hotWindowHours") Integer hotWindowHours);

    @Select("SELECT * FROM forum_post WHERE id = #{id} AND deleted = 0")
    ForumPost selectById(@Param("id") Long id);

    @Insert("""
            INSERT INTO forum_post(user_id, section_id, title, content, images, attachments, view_count, like_count,
              comment_count, favorite_count, share_count, is_top, is_essence, is_anonymous, audit_status, audit_remark,
              source_type, source_id, status, create_time, update_time, deleted)
            VALUES(#{userId}, #{sectionId}, #{title}, #{content}, #{images}, #{attachments}, 0, 0, 0, 0, 0, 0, 0,
              #{isAnonymous}, #{auditStatus}, #{auditRemark}, #{sourceType}, #{sourceId}, 1, NOW(), NOW(), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ForumPost post);

    @Update("""
            UPDATE forum_post
            SET section_id = #{sectionId},
                title = #{title},
                content = #{content},
                images = #{images},
                attachments = #{attachments},
                is_anonymous = #{isAnonymous},
                update_time = NOW()
            WHERE id = #{id} AND user_id = #{userId} AND deleted = 0
            """)
    int updateByOwner(ForumPost post);

    @Update("UPDATE forum_post SET deleted = 1, update_time = NOW() WHERE id = #{id} AND user_id = #{userId} AND deleted = 0")
    int deleteByOwner(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE forum_post SET view_count = view_count + 1, update_time = NOW() WHERE id = #{id} AND deleted = 0")
    int increaseViewCount(@Param("id") Long id);

    @Insert("INSERT IGNORE INTO forum_like(user_id, target_type, target_id, create_time) VALUES(#{userId}, 1, #{postId}, NOW())")
    int insertPostLike(@Param("userId") Long userId, @Param("postId") Long postId);

    @Update("DELETE FROM forum_like WHERE user_id = #{userId} AND target_type = 1 AND target_id = #{postId}")
    int deletePostLike(@Param("userId") Long userId, @Param("postId") Long postId);

    @Select("SELECT COUNT(1) FROM forum_like WHERE user_id = #{userId} AND target_type = 1 AND target_id = #{postId}")
    Long countUserPostLike(@Param("userId") Long userId, @Param("postId") Long postId);

    @Update("UPDATE forum_post SET like_count = like_count + #{delta}, update_time = NOW() WHERE id = #{postId} AND deleted = 0")
    int updateLikeCount(@Param("postId") Long postId, @Param("delta") Integer delta);

    @Insert("INSERT IGNORE INTO forum_favorite(user_id, target_type, target_id, create_time) VALUES(#{userId}, 1, #{postId}, NOW())")
    int insertPostFavorite(@Param("userId") Long userId, @Param("postId") Long postId);

    @Update("DELETE FROM forum_favorite WHERE user_id = #{userId} AND target_type = 1 AND target_id = #{postId}")
    int deletePostFavorite(@Param("userId") Long userId, @Param("postId") Long postId);

    @Select("SELECT COUNT(1) FROM forum_favorite WHERE user_id = #{userId} AND target_type = 1 AND target_id = #{postId}")
    Long countUserPostFavorite(@Param("userId") Long userId, @Param("postId") Long postId);

    @Update("UPDATE forum_post SET favorite_count = favorite_count + #{delta}, update_time = NOW() WHERE id = #{postId} AND deleted = 0")
    int updateFavoriteCount(@Param("postId") Long postId, @Param("delta") Integer delta);

    @Select("""
            <script>
            SELECT * FROM forum_post
            WHERE deleted = 0 AND user_id = #{userId}
            <if test='keyword != null and keyword != ""'>
              AND (title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY create_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<ForumPost> selectMyPosts(@Param("userId") Long userId,
            @Param("offset") Long offset,
            @Param("size") Long size,
            @Param("keyword") String keyword);

    @Select("""
            <script>
            SELECT COUNT(1) FROM forum_post
            WHERE deleted = 0 AND user_id = #{userId}
            <if test='keyword != null and keyword != ""'>
              AND (title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    Long countMyPosts(@Param("userId") Long userId, @Param("keyword") String keyword);

    @Select("""
            SELECT p.*
            FROM forum_favorite f
            INNER JOIN forum_post p ON p.id = f.target_id
            WHERE f.user_id = #{userId}
              AND f.target_type = 1
              AND p.deleted = 0
            ORDER BY f.create_time DESC
            LIMIT #{offset}, #{size}
            """)
    List<ForumPost> selectMyFavoritePosts(@Param("userId") Long userId,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            SELECT COUNT(1)
            FROM forum_favorite f
            INNER JOIN forum_post p ON p.id = f.target_id
            WHERE f.user_id = #{userId}
              AND f.target_type = 1
              AND p.deleted = 0
            """)
    Long countMyFavoritePosts(@Param("userId") Long userId);

    @Select("SELECT COUNT(1) FROM forum_post WHERE deleted = 0")
    Long countAll();

    @Select("SELECT COUNT(1) FROM forum_comment WHERE deleted = 0")
    Long countTotalComments();

    @Select("SELECT COUNT(1) FROM forum_like WHERE deleted = 0")
    Long countTotalLikes();

    @Select("""
            SELECT title
            FROM forum_post
            WHERE deleted = 0
              AND status = 1
              AND audit_status = 1
            ORDER BY is_top DESC,
                     (
                       IFNULL(like_count, 0) * 3 + IFNULL(comment_count, 0) * 5 + IFNULL(view_count, 0)
                     ) DESC,
                     create_time DESC
            LIMIT #{limit}
            """)
    List<String> selectHotPostTitles(@Param("limit") Integer limit);

    @Select("""
            SELECT s.id, s.name, COUNT(p.id) as post_count
            FROM forum_section s
            LEFT JOIN forum_post p ON s.id = p.section_id AND p.deleted = 0
            WHERE s.status = 1
            GROUP BY s.id, s.name
            ORDER BY post_count DESC
            LIMIT 10
            """)
    List<Map<String, Object>> getTopSections();
}
