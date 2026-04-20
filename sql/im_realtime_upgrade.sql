-- IM P0 实时能力增量脚本
-- 执行前请先备份数据库

USE campus_forum;

-- 1) 消息表增加客户端幂等ID
ALTER TABLE message_private
    ADD COLUMN client_message_id VARCHAR(64) NULL COMMENT '客户端消息ID(幂等键)' AFTER content_type;

CREATE UNIQUE INDEX uk_sender_client_message
    ON message_private(sender_id, client_message_id);

CREATE INDEX idx_conversation_cursor
    ON message_private(conversation_id, id);

-- 2) 已读/送达回执表
CREATE TABLE IF NOT EXISTS message_read_receipt (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    message_id BIGINT NOT NULL COMMENT '消息ID',
    user_id BIGINT NOT NULL COMMENT '回执用户ID',
    receipt_type VARCHAR(20) NOT NULL COMMENT '回执类型: DELIVERED|READ',
    client_message_id VARCHAR(64) NULL COMMENT '客户端消息ID',
    receipt_time DATETIME NOT NULL COMMENT '回执时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_message_user_type (message_id, user_id, receipt_type),
    INDEX idx_user_receipt_time (user_id, receipt_time),
    INDEX idx_message_id (message_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私信回执表';

-- 3) 至少一次投递任务表 (ACK 超时重发)
CREATE TABLE IF NOT EXISTS message_delivery_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    message_id BIGINT NOT NULL COMMENT '消息ID',
    conversation_id VARCHAR(64) NOT NULL COMMENT '会话ID',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    receiver_id BIGINT NOT NULL COMMENT '接收者ID',
    client_message_id VARCHAR(64) NULL COMMENT '客户端消息ID',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-PENDING 1-ACKED 2-GIVE_UP',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    next_retry_time DATETIME NOT NULL COMMENT '下一次重试时间',
    ack_time DATETIME NULL COMMENT 'ACK确认时间',
    last_error VARCHAR(255) NULL COMMENT '最近一次错误',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_message_id (message_id),
    INDEX idx_pending_retry (status, next_retry_time),
    INDEX idx_receiver_status (receiver_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息投递任务表';
