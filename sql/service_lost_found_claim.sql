-- =====================================================
-- 失物认领申请表
-- 创建日期: 2026-04-15
-- =====================================================

DROP TABLE IF EXISTS service_lost_found_claim;
CREATE TABLE service_lost_found_claim (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    lost_found_id BIGINT NOT NULL COMMENT '招领信息ID',
    user_id BIGINT NOT NULL COMMENT '申请人ID',
    description TEXT NOT NULL COMMENT '认领理由及证据证明',
    images VARCHAR(2000) COMMENT '证据图片(JSON数组)',
    status TINYINT DEFAULT 0 COMMENT '审核状态: 0-待审核 1-通过 2-拒绝',
    audit_remark VARCHAR(255) COMMENT '审核备注',
    auditor_id BIGINT COMMENT '审核人ID',
    audit_time DATETIME COMMENT '审核时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_lost_found_id (lost_found_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='失物认领申请表';