package com.campus.forum.mapper;

import com.campus.forum.entity.SysNotice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SysNoticeMapper {

    @Select("""
            <script>
            SELECT id, user_id AS userId, sender_id AS senderId, type, title, content, target_type AS targetType, target_id AS targetId, is_read AS readStatus, create_time AS createTime, read_time AS readTime
            FROM message_notification
            WHERE user_id = #{userId}
            ORDER BY create_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<SysNotice> selectByUserId(@Param("userId") Long userId, @Param("offset") Long offset, @Param("size") Long size);

    @Select("SELECT COUNT(1) FROM message_notification WHERE user_id = #{userId}")
    Long countByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(1) FROM message_notification WHERE user_id = #{userId} AND is_read = 0")
    Long countUnreadByUserId(@Param("userId") Long userId);

    @Insert("""
            INSERT INTO message_notification(user_id, sender_id, type, title, content, target_type, target_id, is_read, read_time, create_time)
            VALUES(#{userId}, #{senderId}, #{type}, #{title}, #{content}, #{targetType}, #{targetId}, 0, NULL, NOW())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysNotice notice);

    @Update("UPDATE message_notification SET is_read = 1, read_time = NOW() WHERE id = #{id} AND user_id = #{userId}")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE message_notification SET is_read = 1, read_time = NOW() WHERE user_id = #{userId} AND is_read = 0")
    int markAllAsRead(@Param("userId") Long userId);
}