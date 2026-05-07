package com.campus.forum.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface MessageMapper {

    @Select("""
            <script>
            SELECT n.id, n.user_id AS userId, n.sender_id AS senderId, n.type, n.title, n.content,
              n.target_type AS targetType, n.target_id AS targetId, n.is_read AS isRead, n.read_time AS readTime,
              n.create_time AS createTime, COALESCE(s.nickname, s.username) AS senderName, s.avatar AS senderAvatar
            FROM message_notification n
            LEFT JOIN sys_user s ON s.id = n.sender_id
            WHERE (n.user_id = #{userId} OR n.user_id = 0)
            <if test='type != null'>
              AND n.type = #{type}
            </if>
            <if test='isRead != null'>
              AND n.is_read = #{isRead}
            </if>
            ORDER BY n.is_read ASC, n.create_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectNotificationPage(@Param("userId") Long userId,
            @Param("type") Integer type,
            @Param("isRead") Integer isRead,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM message_notification n
            WHERE (n.user_id = #{userId} OR n.user_id = 0)
            <if test='type != null'>
              AND n.type = #{type}
            </if>
            <if test='isRead != null'>
              AND n.is_read = #{isRead}
            </if>
            </script>
            """)
    Long countNotificationPage(@Param("userId") Long userId,
            @Param("type") Integer type,
            @Param("isRead") Integer isRead);

    @Select("SELECT COUNT(1) FROM message_notification WHERE (user_id = #{userId} OR user_id = 0) AND is_read = 0")
    Long countUnreadNotifications(@Param("userId") Long userId);

    @Select("""
            SELECT n.id, n.user_id AS userId, n.sender_id AS senderId, n.type, n.title, n.content,
              n.target_type AS targetType, n.target_id AS targetId, n.is_read AS isRead, n.read_time AS readTime,
              n.create_time AS createTime, COALESCE(s.nickname, s.username) AS senderName, s.avatar AS senderAvatar
            FROM message_notification n
            LEFT JOIN sys_user s ON s.id = n.sender_id
            WHERE n.id = #{id}
              AND (n.user_id = #{userId} OR n.user_id = 0)
            LIMIT 1
            """)
    Map<String, Object> selectNotificationById(@Param("id") Long id, @Param("userId") Long userId);

    @Update("""
            UPDATE message_notification
            SET is_read = 1,
                read_time = NOW()
            WHERE id = #{id}
              AND (user_id = #{userId} OR user_id = 0)
              AND is_read = 0
            """)
    int markNotificationRead(@Param("id") Long id, @Param("userId") Long userId);

    @Update("""
            UPDATE message_notification
            SET is_read = 1,
                read_time = NOW()
            WHERE user_id = #{userId}
              AND is_read = 0
            """)
    int markUserNotificationsRead(@Param("userId") Long userId);

    @Update("""
            <script>
            DELETE FROM message_notification
            WHERE user_id = #{userId}
              AND type IN (1, 6, 7, 8, 9)
            <if test='onlyRead'>
              AND is_read = 1
            </if>
            </script>
            """)
    int deleteSystemNotifications(@Param("userId") Long userId, @Param("onlyRead") boolean onlyRead);

    @Select("""
            <script>
            SELECT c.conversation_id AS conversationId, c.user_id AS userId, c.target_user_id AS targetUserId,
              c.last_message_id AS lastMessageId, c.last_message_content AS lastMessageContent,
              c.last_message_time AS lastMessageTime, c.unread_count AS unreadCount,
              c.is_top AS isTop, c.is_muted AS isMuted, c.status, c.update_time AS updateTime,
              COALESCE(u.nickname, u.username) AS targetNickname, u.avatar AS targetAvatar,
              u.college AS targetCollege, u.major AS targetMajor
            FROM message_conversation c
            LEFT JOIN sys_user u ON u.id = c.target_user_id
            WHERE c.user_id = #{userId}
              AND c.status = 1
            <if test='keyword != null and keyword != ""'>
              AND (
                u.nickname LIKE CONCAT('%', #{keyword}, '%')
                OR u.username LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            ORDER BY c.is_top DESC, c.last_message_time DESC, c.update_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectConversationPage(@Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM message_conversation c
            LEFT JOIN sys_user u ON u.id = c.target_user_id
            WHERE c.user_id = #{userId}
              AND c.status = 1
            <if test='keyword != null and keyword != ""'>
              AND (
                u.nickname LIKE CONCAT('%', #{keyword}, '%')
                OR u.username LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            </script>
            """)
    Long countConversationPage(@Param("userId") Long userId, @Param("keyword") String keyword);

    @Select("SELECT COALESCE(SUM(unread_count), 0) FROM message_conversation WHERE user_id = #{userId} AND status = 1")
    Long countUnreadConversations(@Param("userId") Long userId);

        @Select("SELECT sender_id FROM message_private WHERE conversation_id = #{conversationId} AND deleted = 0 ORDER BY id ASC LIMIT 1")
        Long selectFirstMessageSenderId(@Param("conversationId") String conversationId);

        @Select("SELECT COUNT(1) FROM message_private WHERE conversation_id = #{conversationId} AND sender_id = #{senderId} AND deleted = 0")
        Long countMessagesBySender(@Param("conversationId") String conversationId,
          @Param("senderId") Long senderId);

    @Select("""
            SELECT conversation_id AS conversationId, user_id AS userId, target_user_id AS targetUserId,
              last_message_id AS lastMessageId, last_message_content AS lastMessageContent,
              last_message_time AS lastMessageTime, unread_count AS unreadCount,
              is_top AS isTop, is_muted AS isMuted, status, update_time AS updateTime
            FROM message_conversation
            WHERE conversation_id = #{conversationId}
              AND user_id = #{userId}
              AND status = 1
            LIMIT 1
            """)
    Map<String, Object> selectConversationById(@Param("conversationId") String conversationId,
            @Param("userId") Long userId);

    @Select("""
            SELECT m.id, m.conversation_id AS conversationId, m.sender_id AS senderId, m.receiver_id AS receiverId,
              m.content, m.content_type AS contentType, m.is_read AS isRead, m.read_time AS readTime,
              m.create_time AS createTime, COALESCE(su.nickname, su.username) AS senderName, su.avatar AS senderAvatar,
              COALESCE(ru.nickname, ru.username) AS receiverName, ru.avatar AS receiverAvatar
            FROM message_private m
            LEFT JOIN sys_user su ON su.id = m.sender_id
            LEFT JOIN sys_user ru ON ru.id = m.receiver_id
            WHERE m.deleted = 0
              AND m.conversation_id = #{conversationId}
              AND (m.sender_id = #{userId} OR m.receiver_id = #{userId})
            ORDER BY m.id DESC
            LIMIT #{offset}, #{size}
            """)
    List<Map<String, Object>> selectConversationMessages(@Param("conversationId") String conversationId,
            @Param("userId") Long userId,
            @Param("offset") Long offset,
            @Param("size") Long size);

    @Select("""
            SELECT COUNT(1)
            FROM message_private m
            WHERE m.deleted = 0
              AND m.conversation_id = #{conversationId}
              AND (m.sender_id = #{userId} OR m.receiver_id = #{userId})
            """)
    Long countConversationMessages(@Param("conversationId") String conversationId,
            @Param("userId") Long userId);

    @Insert("""
            INSERT INTO message_private(conversation_id, sender_id, receiver_id, content, content_type,
              client_message_id, is_read, create_time, deleted)
            VALUES(#{conversationId}, #{senderId}, #{receiverId}, #{content}, #{contentType}, #{clientMessageId}, 0, NOW(), 0)
            """)
    int insertPrivateMessage(@Param("conversationId") String conversationId,
            @Param("senderId") Long senderId,
            @Param("receiverId") Long receiverId,
            @Param("content") String content,
            @Param("contentType") Integer contentType,
            @Param("clientMessageId") String clientMessageId);

    @Select("""
            SELECT id, conversation_id AS conversationId, sender_id AS senderId, receiver_id AS receiverId,
              content, content_type AS contentType, client_message_id AS clientMessageId,
              is_read AS isRead, read_time AS readTime, create_time AS createTime
            FROM message_private
            WHERE sender_id = #{senderId}
              AND client_message_id = #{clientMessageId}
              AND deleted = 0
            LIMIT 1
            """)
    Map<String, Object> selectMessageByClientMessageId(@Param("senderId") Long senderId,
            @Param("clientMessageId") String clientMessageId);

    @Select("""
            SELECT m.id, m.conversation_id AS conversationId, m.sender_id AS senderId, m.receiver_id AS receiverId,
              m.content, m.content_type AS contentType, m.client_message_id AS clientMessageId,
              m.is_read AS isRead, m.read_time AS readTime, m.create_time AS createTime,
              COALESCE(su.nickname, su.username) AS senderName, su.avatar AS senderAvatar,
              COALESCE(ru.nickname, ru.username) AS receiverName, ru.avatar AS receiverAvatar
            FROM message_private m
            LEFT JOIN sys_user su ON su.id = m.sender_id
            LEFT JOIN sys_user ru ON ru.id = m.receiver_id
            WHERE m.id = #{messageId}
              AND m.deleted = 0
            LIMIT 1
            """)
    Map<String, Object> selectRealtimeMessageById(@Param("messageId") Long messageId);

    @Select("""
            SELECT id, conversation_id AS conversationId, sender_id AS senderId, receiver_id AS receiverId,
              client_message_id AS clientMessageId, is_read AS isRead, create_time AS createTime
            FROM message_private
            WHERE id = #{messageId}
              AND deleted = 0
            LIMIT 1
            """)
    Map<String, Object> selectMessageSimpleById(@Param("messageId") Long messageId);

    @Select("""
            SELECT m.id, m.conversation_id AS conversationId, m.sender_id AS senderId, m.receiver_id AS receiverId,
              m.content, m.content_type AS contentType, m.client_message_id AS clientMessageId,
              m.is_read AS isRead, m.read_time AS readTime, m.create_time AS createTime,
              COALESCE(su.nickname, su.username) AS senderName, su.avatar AS senderAvatar,
              COALESCE(ru.nickname, ru.username) AS receiverName, ru.avatar AS receiverAvatar
            FROM message_private m
            LEFT JOIN sys_user su ON su.id = m.sender_id
            LEFT JOIN sys_user ru ON ru.id = m.receiver_id
            WHERE m.deleted = 0
              AND m.conversation_id = #{conversationId}
              AND (m.sender_id = #{userId} OR m.receiver_id = #{userId})
              AND m.id > #{cursorId}
            ORDER BY m.id ASC
            LIMIT #{size}
            """)
    List<Map<String, Object>> selectConversationMessagesAfterCursor(@Param("conversationId") String conversationId,
            @Param("userId") Long userId,
            @Param("cursorId") Long cursorId,
            @Param("size") Integer size);

    @Insert("""
            INSERT INTO message_conversation(conversation_id, user_id, target_user_id, last_message_content,
              last_message_id, last_message_time, unread_count, is_top, is_muted, status, update_time)
            VALUES(#{conversationId}, #{userId}, #{targetUserId}, #{lastMessageContent},
              #{lastMessageId},
              NOW(), #{unreadIncrement}, 0, 0, 1, NOW())
            ON DUPLICATE KEY UPDATE
              target_user_id = VALUES(target_user_id),
              last_message_content = VALUES(last_message_content),
              last_message_id = VALUES(last_message_id),
              last_message_time = NOW(),
              unread_count = CASE
                  WHEN status = 0 THEN VALUES(unread_count)
                  ELSE unread_count + #{unreadIncrement}
              END,
              status = 1,
              update_time = NOW()
            """)
    int upsertConversation(@Param("conversationId") String conversationId,
            @Param("userId") Long userId,
            @Param("targetUserId") Long targetUserId,
            @Param("lastMessageContent") String lastMessageContent,
            @Param("lastMessageId") Long lastMessageId,
            @Param("unreadIncrement") Integer unreadIncrement);

    @Update("""
            UPDATE message_private
            SET is_read = 1,
                read_time = NOW()
            WHERE id = #{messageId}
              AND receiver_id = #{userId}
              AND deleted = 0
              AND is_read = 0
            """)
    int markSingleMessageRead(@Param("messageId") Long messageId, @Param("userId") Long userId);

    @Update("""
            UPDATE message_conversation
            SET unread_count = CASE WHEN unread_count > 0 THEN unread_count - 1 ELSE 0 END,
                update_time = NOW()
            WHERE conversation_id = #{conversationId}
              AND user_id = #{userId}
              AND status = 1
            """)
    int decreaseConversationUnread(@Param("conversationId") String conversationId, @Param("userId") Long userId);

    @Insert("""
            INSERT INTO message_read_receipt(message_id, user_id, receipt_type, client_message_id, receipt_time, create_time)
            VALUES(#{messageId}, #{userId}, #{receiptType}, #{clientMessageId}, NOW(), NOW())
            ON DUPLICATE KEY UPDATE
              receipt_time = VALUES(receipt_time)
            """)
    int insertReadReceipt(@Param("messageId") Long messageId,
            @Param("userId") Long userId,
            @Param("receiptType") String receiptType,
            @Param("clientMessageId") String clientMessageId);

    @Update("""
            UPDATE message_private
            SET is_read = 1,
                read_time = NOW()
            WHERE conversation_id = #{conversationId}
              AND receiver_id = #{userId}
              AND is_read = 0
              AND deleted = 0
            """)
    int markConversationMessagesRead(@Param("conversationId") String conversationId,
            @Param("userId") Long userId);

    @Update("""
            UPDATE message_conversation
            SET unread_count = 0,
                update_time = NOW()
            WHERE conversation_id = #{conversationId}
              AND user_id = #{userId}
              AND status = 1
            """)
    int clearConversationUnread(@Param("conversationId") String conversationId,
            @Param("userId") Long userId);

    @Insert("""
            INSERT INTO message_notification(user_id, sender_id, type, title, content, target_type, target_id,
              is_read, create_time)
            VALUES(#{userId}, #{senderId}, #{type}, #{title}, #{content}, #{targetType}, #{targetId}, 0, NOW())
            """)
    int insertNotification(@Param("userId") Long userId,
            @Param("senderId") Long senderId,
            @Param("type") Integer type,
            @Param("title") String title,
            @Param("content") String content,
            @Param("targetType") Integer targetType,
            @Param("targetId") Long targetId);
}
