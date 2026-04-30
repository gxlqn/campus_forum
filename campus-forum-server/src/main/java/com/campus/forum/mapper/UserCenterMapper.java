package com.campus.forum.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserCenterMapper {

    @Select("""
            <script>
            SELECT id, title, content, create_time AS createTime, update_time AS updateTime,
              like_count AS likeCount, comment_count AS commentCount, favorite_count AS favoriteCount,
              audit_status AS auditStatus, status, 'post' AS type
            FROM forum_post
            WHERE deleted = 0 AND user_id = #{userId}
            <if test='keyword != null and keyword != ""'>
              AND (title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY create_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectMyPostPublishes(@Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1) FROM forum_post
            WHERE deleted = 0 AND user_id = #{userId}
            <if test='keyword != null and keyword != ""'>
              AND (title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    Long countMyPostPublishes(@Param("userId") Long userId, @Param("keyword") String keyword);

    @Select("""
            <script>
            SELECT id, title, description AS content, create_time AS createTime, update_time AS updateTime,
              want_count AS likeCount, 0 AS commentCount, 0 AS favoriteCount,
              audit_status AS auditStatus, status, 'product' AS type
            FROM service_product
            WHERE deleted = 0 AND user_id = #{userId}
            <if test='keyword != null and keyword != ""'>
              AND (title LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY create_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectMyProductPublishes(@Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1) FROM service_product
            WHERE deleted = 0 AND user_id = #{userId}
            <if test='keyword != null and keyword != ""'>
              AND (title LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    Long countMyProductPublishes(@Param("userId") Long userId, @Param("keyword") String keyword);

    @Select("""
            <script>
            SELECT id, title, description AS content, create_time AS createTime, update_time AS updateTime,
              view_count AS likeCount, 0 AS commentCount, 0 AS favoriteCount,
              audit_status AS auditStatus, status, 'activity' AS type
            FROM service_activity
            WHERE deleted = 0 AND user_id = #{userId}
            <if test='keyword != null and keyword != ""'>
              AND (title LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY create_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectMyActivityPublishes(@Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1) FROM service_activity
            WHERE deleted = 0 AND user_id = #{userId}
            <if test='keyword != null and keyword != ""'>
              AND (title LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    Long countMyActivityPublishes(@Param("userId") Long userId, @Param("keyword") String keyword);

    @Select("""
            <script>
            SELECT id, title, description AS content, create_time AS createTime, update_time AS updateTime,
              view_count AS likeCount, 0 AS commentCount, 0 AS favoriteCount,
              audit_status AS auditStatus, status, 'help' AS type
            FROM service_help_request
            WHERE deleted = 0 AND user_id = #{userId}
            <if test='keyword != null and keyword != ""'>
              AND (title LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY create_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectMyHelpPublishes(@Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1) FROM service_help_request
            WHERE deleted = 0 AND user_id = #{userId}
            <if test='keyword != null and keyword != ""'>
              AND (title LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    Long countMyHelpPublishes(@Param("userId") Long userId, @Param("keyword") String keyword);

    @Select("""
            <script>
            SELECT id, title, description AS content, create_time AS createTime, update_time AS updateTime,
              view_count AS likeCount, 0 AS commentCount, 0 AS favoriteCount,
              audit_status AS auditStatus, status, 'lostfound' AS type
            FROM service_lost_found
            WHERE deleted = 0 AND user_id = #{userId}
            <if test='keyword != null and keyword != ""'>
              AND (title LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY create_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectMyLostFoundPublishes(@Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1) FROM service_lost_found
            WHERE deleted = 0 AND user_id = #{userId}
            <if test='keyword != null and keyword != ""'>
              AND (title LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    Long countMyLostFoundPublishes(@Param("userId") Long userId, @Param("keyword") String keyword);

    @Select("""
            <script>
            SELECT *
            FROM (
              SELECT id, title, content, create_time AS createTime, update_time AS updateTime,
                like_count AS likeCount, comment_count AS commentCount, favorite_count AS favoriteCount,
                audit_status AS auditStatus, status, 'post' AS type
              FROM forum_post
              WHERE deleted = 0 AND user_id = #{userId}
              UNION ALL
              SELECT id, title, description AS content, create_time AS createTime, update_time AS updateTime,
                want_count AS likeCount, 0 AS commentCount, 0 AS favoriteCount,
                audit_status AS auditStatus, status, 'product' AS type
              FROM service_product
              WHERE deleted = 0 AND user_id = #{userId}
              UNION ALL
              SELECT id, title, description AS content, create_time AS createTime, update_time AS updateTime,
                view_count AS likeCount, 0 AS commentCount, 0 AS favoriteCount,
                audit_status AS auditStatus, status, 'activity' AS type
              FROM service_activity
              WHERE deleted = 0 AND user_id = #{userId}
              UNION ALL
              SELECT id, title, description AS content, create_time AS createTime, update_time AS updateTime,
                view_count AS likeCount, 0 AS commentCount, 0 AS favoriteCount,
                audit_status AS auditStatus, status, 'help' AS type
              FROM service_help_request
              WHERE deleted = 0 AND user_id = #{userId}
              UNION ALL
              SELECT id, title, description AS content, create_time AS createTime, update_time AS updateTime,
                view_count AS likeCount, 0 AS commentCount, 0 AS favoriteCount,
                audit_status AS auditStatus, status, 'lostfound' AS type
              FROM service_lost_found
              WHERE deleted = 0 AND user_id = #{userId}
            ) t
            <if test='keyword != null and keyword != ""'>
              WHERE title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%')
            </if>
            ORDER BY createTime DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectMyAllPublishes(@Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            <script>
            SELECT
              (SELECT COUNT(1) FROM forum_post WHERE deleted = 0 AND user_id = #{userId}
                <if test='keyword != null and keyword != ""'>
                  AND (title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%'))
                </if>)
              + (SELECT COUNT(1) FROM service_product WHERE deleted = 0 AND user_id = #{userId}
                <if test='keyword != null and keyword != ""'>
                  AND (title LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%'))
                </if>)
              + (SELECT COUNT(1) FROM service_activity WHERE deleted = 0 AND user_id = #{userId}
                <if test='keyword != null and keyword != ""'>
                  AND (title LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%'))
                </if>)
              + (SELECT COUNT(1) FROM service_help_request WHERE deleted = 0 AND user_id = #{userId}
                <if test='keyword != null and keyword != ""'>
                  AND (title LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%'))
                </if>)
              + (SELECT COUNT(1) FROM service_lost_found WHERE deleted = 0 AND user_id = #{userId}
                <if test='keyword != null and keyword != ""'>
                  AND (title LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%'))
                </if>)
            </script>
            """)
    Long countMyAllPublishes(@Param("userId") Long userId, @Param("keyword") String keyword);

    @Select("""
            SELECT u.id, u.nickname, u.avatar, u.college, u.major, u.grade, u.user_type AS userType
            FROM forum_follow f
            INNER JOIN sys_user u ON u.id = f.follow_user_id
            WHERE f.user_id = #{userId} AND u.deleted = 0
            ORDER BY f.create_time DESC
            LIMIT #{offset}, #{size}
            """)
    List<Map<String, Object>> selectMyFollowUsers(@Param("userId") Long userId,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            SELECT COUNT(1)
            FROM forum_follow f
            INNER JOIN sys_user u ON u.id = f.follow_user_id
            WHERE f.user_id = #{userId} AND u.deleted = 0
            """)
    Long countMyFollowUsers(@Param("userId") Long userId);

    @Insert("INSERT IGNORE INTO forum_follow(user_id, follow_user_id, create_time) VALUES(#{userId}, #{followUserId}, NOW())")
    int insertFollow(@Param("userId") Long userId, @Param("followUserId") Long followUserId);

    @Update("DELETE FROM forum_follow WHERE user_id = #{userId} AND follow_user_id = #{followUserId}")
    int deleteFollow(@Param("userId") Long userId, @Param("followUserId") Long followUserId);

    @Select("SELECT COUNT(1) FROM forum_follow WHERE user_id = #{userId}")
    Long countFollowing(@Param("userId") Long userId);

        @Select("SELECT COUNT(1) FROM forum_follow WHERE user_id = #{userId} AND follow_user_id = #{followUserId}")
        Long countFollowRelation(@Param("userId") Long userId, @Param("followUserId") Long followUserId);

    @Select("SELECT COUNT(1) FROM forum_follow WHERE follow_user_id = #{userId}")
    Long countFollowers(@Param("userId") Long userId);

    @Select("SELECT COUNT(1) FROM forum_favorite WHERE user_id = #{userId} AND target_type = 1")
    Long countMyPostFavorites(@Param("userId") Long userId);

    @Select("""
            SELECT id, title, reward, rating, rating_content AS ratingContent, complete_time AS completeTime
            FROM service_help_request
            WHERE deleted = 0
              AND rating IS NOT NULL
              AND (
                user_id = #{userId}
                OR helper_id = #{userId}
              )
            ORDER BY complete_time DESC
            LIMIT #{offset}, #{size}
            """)
    List<Map<String, Object>> selectMyEvaluations(@Param("userId") Long userId,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            SELECT COUNT(1)
            FROM service_help_request
            WHERE deleted = 0
              AND rating IS NOT NULL
              AND (
                user_id = #{userId}
                OR helper_id = #{userId}
              )
            """)
    Long countMyEvaluations(@Param("userId") Long userId);

    @Select("""
            SELECT ROUND(AVG(rating), 2)
            FROM service_help_request
            WHERE deleted = 0
              AND rating IS NOT NULL
              AND (
                user_id = #{userId}
                OR helper_id = #{userId}
              )
            """)
    Double avgMyEvaluationRating(@Param("userId") Long userId);
}
