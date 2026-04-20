-- =====================================================
-- 智能内容审核模块 - 数据库表结构
-- 创建时间: 2026-04-13
-- =====================================================

USE campus_forum;

-- =====================================================
-- 1. 敏感词库表
-- =====================================================
DROP TABLE IF EXISTS audit_sensitive_word;
CREATE TABLE audit_sensitive_word (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    word VARCHAR(100) NOT NULL COMMENT '敏感词',
    category TINYINT NOT NULL DEFAULT 1 COMMENT '类别: 1-政治敏感 2-色情低俗 3-暴力恐怖 4-广告营销 5-其他违规',
    level TINYINT NOT NULL DEFAULT 1 COMMENT '等级: 1-弱(标记可疑) 2-中(转人工) 3-强(直接拒绝)',
    replacement VARCHAR(100) COMMENT '替换词(可选)',
    is_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用: 0-禁用 1-启用',
    remark VARCHAR(255) COMMENT '备注说明',
    creator_id BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_word (word),
    INDEX idx_category (category),
    INDEX idx_level (level),
    INDEX idx_enabled (is_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词库';

-- =====================================================
-- 2. 审核日志表
-- =====================================================
DROP TABLE IF EXISTS audit_log;
CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    target_type VARCHAR(20) NOT NULL COMMENT '目标类型: post/product/lostfound/comment/activity/help',
    target_id BIGINT NOT NULL COMMENT '目标ID',
    user_id BIGINT NOT NULL COMMENT '发布者ID',
    
    -- 第一层：关键词过滤结果
    filter_result TINYINT COMMENT '过滤结果: 0-通过 1-命中弱级(可疑) 2-命中中级(转人工) 3-命中强级(拒绝)',
    matched_keywords VARCHAR(500) COMMENT '命中的关键词(JSON数组)',
    matched_level TINYINT COMMENT '命中的最高等级',
    
    -- 第二层：规则引擎/AI审核结果（预留）
    ai_audit_status TINYINT DEFAULT 0 COMMENT 'AI状态: 0-未调用 1-安全 2-不安全 3-不确定',
    ai_confidence DECIMAL(5,4) COMMENT '置信度(0-1)',
    ai_labels VARCHAR(500) COMMENT '检测标签',
    ai_response_time INT COMMENT '响应时间(ms)',
    
    -- 最终结果
    final_status TINYINT NOT NULL COMMENT '最终状态: 0-待审核 1-自动通过 2-自动拒绝 3-AI不确定 4-待人工复核',
    audit_method TINYINT COMMENT '审核方式: 1-关键词自动 2-AI自动 3-人工复核',
    auditor_id BIGINT COMMENT '人工审核员ID',
    audit_remark VARCHAR(500) COMMENT '审核备注',
    
    -- 内容快照（用于追溯）
    content_snapshot TEXT COMMENT '内容快照(标题+正文)',
    
    -- 时间戳
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_target (target_type, target_id),
    INDEX idx_user (user_id),
    INDEX idx_final_status (final_status),
    INDEX idx_filter_result (filter_result),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核日志表';

-- =====================================================
-- 3. 初始敏感词数据（示例数据，用于开发测试）
-- =====================================================

-- 政治敏感类（强级-直接拒绝）
INSERT INTO audit_sensitive_word (word, category, level, is_enabled, remark) VALUES
('法轮功', 1, 3, 1, '邪教组织'),
('台独', 1, 3, 1, '分裂国家'),
('藏独', 1, 3, 1, '分裂国家'),
('疆独', 1, 3, 1, '分裂国家'),
('六四', 1, 3, 1, '政治事件'),
('天安门事件', 1, 3, 1, '政治事件'),
('反党', 1, 3, 1, '政治敏感'),
('推翻', 1, 3, 1, '政治敏感'),
('暴动', 1, 3, 1, '政治敏感'),
('颠覆', 1, 3, 1, '政治敏感');

-- 色情低俗类（强级）
INSERT INTO audit_sensitive_word (word, category, level, is_enabled, remark) VALUES
('色情', 2, 3, 1, '色情'),
('淫秽', 2, 3, 1, '色情'),
('裸聊', 2, 3, 1, '色情服务'),
('一夜情', 2, 3, 1, '色情'),
('约炮', 2, 3, 1, '色情'),
('援交', 2, 3, 1, '色情交易'),
('包养', 2, 3, 1, '不当关系'),
('sm', 2, 2, 1, '可疑'),
('AV', 2, 2, 1, '可疑'),
('黄片', 2, 3, 1, '色情');

-- 暴力恐怖类（强级）
INSERT INTO audit_sensitive_word (word, category, level, is_enabled, remark) VALUES
('杀人', 3, 3, 1, '暴力'),
('炸弹', 3, 3, 1, '危险品'),
('炸药', 3, 3, 1, '危险品'),
('恐怖袭击', 3, 3, 1, '暴力'),
('砍人', 3, 3, 1, '暴力'),
('血腥', 3, 2, 1, '可疑');

-- 广告营销类（中级-转人工或标记）
INSERT INTO audit_sensitive_word (word, category, level, is_enabled, remark) VALUES
('加微信', 4, 2, 1, '引流'),
('加QQ', 4, 2, 1, '引流'),
('扫码关注', 4, 2, 1, '引流'),
('兼职赚钱', 4, 2, 1, '广告'),
('日赚', 4, 2, 1, '诈骗嫌疑'),
('刷单', 4, 2, 1, '违规行为'),
('代购', 4, 1, 1, '商业推广'),
('代理', 4, 1, 1, '商业推广'),
('优惠券', 4, 1, 1, '促销'),
('打折', 4, 1, 1, '促销');

-- 其他违规类
INSERT INTO audit_sensitive_word (word, category, level, is_enabled, remark) VALUES
('赌博', 5, 3, 1, '违法'),
('博彩', 5, 3, 1, '违法'),
('彩票', 5, 2, 1, '需注意'),
('毒品', 5, 3, 1, '违法'),
('吸毒', 5, 3, 1, '违法'),
('假币', 5, 3, 1, '违法'),
('发票', 5, 2, 1, '需注意'),
('办证', 5, 2, 1, '需注意');
