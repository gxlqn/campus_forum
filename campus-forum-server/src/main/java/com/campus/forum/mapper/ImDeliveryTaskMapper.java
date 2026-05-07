package com.campus.forum.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ImDeliveryTaskMapper {

    @Insert("""
            INSERT INTO message_delivery_task(message_id, conversation_id, sender_id, receiver_id, client_message_id,
              status, retry_count, next_retry_time, create_time, update_time)
            VALUES(#{messageId}, #{conversationId}, #{senderId}, #{receiverId}, #{clientMessageId},
              0, 0, #{nextRetryTime}, NOW(), NOW())
            ON DUPLICATE KEY UPDATE
              status = 0,
              next_retry_time = VALUES(next_retry_time),
              last_error = NULL,
              update_time = NOW()
            """)
    int upsertPendingTask(@Param("messageId") Long messageId,
            @Param("conversationId") String conversationId,
            @Param("senderId") Long senderId,
            @Param("receiverId") Long receiverId,
            @Param("clientMessageId") String clientMessageId,
            @Param("nextRetryTime") LocalDateTime nextRetryTime);

    @Select("""
            SELECT id, message_id AS messageId, conversation_id AS conversationId,
              sender_id AS senderId, receiver_id AS receiverId, client_message_id AS clientMessageId,
              status, retry_count AS retryCount, next_retry_time AS nextRetryTime, last_error AS lastError,
              create_time AS createTime, update_time AS updateTime
            FROM message_delivery_task
            WHERE status = 0
              AND next_retry_time <= #{now}
            ORDER BY next_retry_time ASC
            LIMIT #{size}
            """)
    List<Map<String, Object>> selectDuePendingTasks(@Param("now") LocalDateTime now, @Param("size") Integer size);

    @Update("""
            UPDATE message_delivery_task
            SET retry_count = retry_count + 1,
                next_retry_time = #{nextRetryTime},
                last_error = #{lastError},
                update_time = NOW()
            WHERE id = #{taskId}
              AND status = 0
            """)
    int increaseRetry(@Param("taskId") Long taskId,
            @Param("nextRetryTime") LocalDateTime nextRetryTime,
            @Param("lastError") String lastError);

    @Update("""
            UPDATE message_delivery_task
            SET status = 1,
                ack_time = NOW(),
                update_time = NOW()
            WHERE message_id = #{messageId}
              AND status = 0
            """)
    int markAcked(@Param("messageId") Long messageId);

    @Update("""
            UPDATE message_delivery_task
            SET status = 2,
                last_error = #{lastError},
                update_time = NOW()
            WHERE id = #{taskId}
              AND status = 0
            """)
    int markGiveUp(@Param("taskId") Long taskId, @Param("lastError") String lastError);
}
