package com.campus.forum.mapper;

import com.campus.forum.entity.ForumComment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ForumCommentMapper {

    @Select("""
            SELECT * FROM forum_comment
            WHERE post_id = #{postId}
              AND deleted = 0
              AND status = 1
              AND audit_status = 1
            ORDER BY create_time ASC
            """)
    List<ForumComment> selectByPostId(@Param("postId") Long postId);

    @Insert("""
            INSERT INTO forum_comment(post_id, user_id, parent_id, reply_user_id, content, images, like_count, is_anonymous,
              audit_status, status, create_time, update_time, deleted)
            VALUES(#{postId}, #{userId}, #{parentId}, #{replyUserId}, #{content}, #{images}, 0, #{isAnonymous},
              #{auditStatus}, 1, NOW(), NOW(), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ForumComment comment);

    @Update("UPDATE forum_post SET comment_count = comment_count + 1, update_time = NOW() WHERE id = #{postId} AND deleted = 0")
    int increasePostCommentCount(@Param("postId") Long postId);
}
