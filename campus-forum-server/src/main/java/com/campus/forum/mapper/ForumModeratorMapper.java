package com.campus.forum.mapper;

import com.campus.forum.entity.ForumModerator;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ForumModeratorMapper {

    @Select("SELECT * FROM forum_moderator WHERE user_id = #{userId}")
    List<ForumModerator> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT module_code FROM forum_moderator WHERE user_id = #{userId}")
    List<String> selectModuleCodesByUserId(@Param("userId") Long userId);

    @Select("""
            <script>
            SELECT fm.id, fm.user_id AS userId, fm.module_code AS moduleCode, fm.module_name AS moduleName,
                   fm.assigned_by AS assignedBy, fm.create_time AS createTime, fm.update_time AS updateTime,
                   u.username, u.nickname, u.avatar
            FROM forum_moderator fm
            LEFT JOIN sys_user u ON u.id = fm.user_id AND u.deleted = 0
            WHERE 1 = 1
            <if test='keyword != null and keyword != ""'>
              AND (u.nickname LIKE CONCAT('%', #{keyword}, '%')
                   OR u.username LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test='moduleCode != null and moduleCode != ""'>
              AND fm.module_code = #{moduleCode}
            </if>
            ORDER BY fm.create_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectPage(@Param("keyword") String keyword,
                                         @Param("moduleCode") String moduleCode,
                                         @Param("offset") Long offset,
                                         @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1) FROM forum_moderator fm
            LEFT JOIN sys_user u ON u.id = fm.user_id AND u.deleted = 0
            WHERE 1 = 1
            <if test='keyword != null and keyword != ""'>
              AND (u.nickname LIKE CONCAT('%', #{keyword}, '%')
                   OR u.username LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test='moduleCode != null and moduleCode != ""'>
              AND fm.module_code = #{moduleCode}
            </if>
            </script>
            """)
    Long countPage(@Param("keyword") String keyword,
                   @Param("moduleCode") String moduleCode);

    @Insert("""
            INSERT INTO forum_moderator(user_id, module_code, module_name, assigned_by)
            VALUES(#{userId}, #{moduleCode}, #{moduleName}, #{assignedBy})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ForumModerator moderator);

    @Delete("DELETE FROM forum_moderator WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Delete("DELETE FROM forum_moderator WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    @Delete("DELETE FROM forum_moderator WHERE user_id = #{userId} AND module_code = #{moduleCode}")
    int deleteUserModule(@Param("userId") Long userId, @Param("moduleCode") String moduleCode);

    @Select("SELECT COUNT(1) FROM forum_moderator WHERE user_id = #{userId} AND module_code = #{moduleCode}")
    int checkExists(@Param("userId") Long userId, @Param("moduleCode") String moduleCode);
}
