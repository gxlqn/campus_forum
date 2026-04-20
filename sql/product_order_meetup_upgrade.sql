-- 二手订单约见能力升级脚本
-- 执行前请先备份数据库

ALTER TABLE service_product_order
  ADD COLUMN meetup_place VARCHAR(255) NULL COMMENT '约见地点' AFTER cancel_reason,
  ADD COLUMN meetup_time DATETIME NULL COMMENT '约见时间' AFTER meetup_place,
  ADD COLUMN meetup_code VARCHAR(12) NULL COMMENT '见面核销码' AFTER meetup_time,
  ADD COLUMN meetup_verified TINYINT DEFAULT 0 COMMENT '是否完成见面码核销: 0-否 1-是' AFTER meetup_code,
  ADD COLUMN reschedule_count TINYINT DEFAULT 0 COMMENT '改约次数' AFTER meetup_verified;