-- 举报机制增强脚本
-- 执行时间: 2026-04-15
-- 说明: 扩展举报目标类型 + 添加防重复索引

USE campus_forum;

-- 更新 sys_report 表注释（添加失物招领和互助类型说明）
ALTER TABLE sys_report MODIFY COLUMN target_type TINYINT NOT NULL COMMENT '举报目标类型: 1-帖子 2-评论 3-用户 4-商品 5-活动 6-失物招领 7-互助';

-- 添加复合唯一索引防止同一用户重复举报同一目标（仅限待处理状态）
-- 注意：MySQL不支持部分唯一索引，这里用普通索引加速查询，防重复在应用层处理
ALTER TABLE sys_report ADD INDEX idx_user_target_pending (user_id, target_type, target_id, status);
