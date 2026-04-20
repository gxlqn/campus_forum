-- =====================================================
-- 二手商品订单表
-- 创建日期: 2026-04-17
-- =====================================================

DROP TABLE IF EXISTS service_product_order;
CREATE TABLE service_product_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    buyer_id BIGINT NOT NULL COMMENT '买家ID',
    seller_id BIGINT NOT NULL COMMENT '卖家ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '成交金额',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-已下单 2-已取消 3-已完成',
    cancel_reason VARCHAR(255) COMMENT '取消原因',
    meetup_place VARCHAR(255) COMMENT '约见地点',
    meetup_time DATETIME COMMENT '约见时间',
    meetup_code VARCHAR(12) COMMENT '见面核销码',
    meetup_verified TINYINT DEFAULT 0 COMMENT '是否完成见面码核销: 0-否 1-是',
    reschedule_count TINYINT DEFAULT 0 COMMENT '改约次数',
    pay_time DATETIME COMMENT '支付时间(预留)',
    cancel_time DATETIME COMMENT '取消时间',
    finish_time DATETIME COMMENT '完成时间(确认收货)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_product_id (product_id),
    INDEX idx_buyer_id (buyer_id),
    INDEX idx_seller_id (seller_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='二手商品订单表';
