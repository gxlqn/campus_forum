/*
 Navicat Premium Dump SQL

 Source Server         : gxlqn_mysql
 Source Server Type    : MySQL
 Source Server Version : 80100 (8.1.0)
 Source Host           : localhost:3306
 Source Schema         : campus_forum

 Target Server Type    : MySQL
 Target Server Version : 80100 (8.1.0)
 File Encoding         : 65001

 Date: 30/04/2026 18:33:39
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for audit_log
-- ----------------------------
DROP TABLE IF EXISTS `audit_log`;
CREATE TABLE `audit_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标类型: post/product/lostfound/comment/activity/help',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `user_id` bigint NOT NULL COMMENT '发布者ID',
  `filter_result` tinyint NULL DEFAULT NULL COMMENT '过滤结果: 0-通过 1-命中弱级(可疑) 2-命中中级(转人工) 3-命中强级(拒绝)',
  `matched_keywords` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '命中的关键词(JSON数组)',
  `matched_level` tinyint NULL DEFAULT NULL COMMENT '命中的最高等级',
  `ai_audit_status` tinyint NULL DEFAULT 0 COMMENT 'AI状态: 0-未调用 1-安全 2-不安全 3-不确定',
  `ai_confidence` decimal(5, 4) NULL DEFAULT NULL COMMENT '置信度(0-1)',
  `ai_labels` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '检测标签',
  `ai_response_time` int NULL DEFAULT NULL COMMENT '响应时间(ms)',
  `final_status` tinyint NOT NULL COMMENT '最终状态: 0-待审核 1-自动通过 2-自动拒绝 3-AI不确定 4-待人工复核',
  `audit_method` tinyint NULL DEFAULT NULL COMMENT '审核方式: 1-关键词自动 2-AI自动 3-人工复核',
  `auditor_id` bigint NULL DEFAULT NULL COMMENT '人工审核员ID',
  `audit_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核备注',
  `content_snapshot` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '内容快照(标题+正文)',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_final_status`(`final_status` ASC) USING BTREE,
  INDEX `idx_filter_result`(`filter_result` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '审核日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of audit_log
-- ----------------------------

-- ----------------------------
-- Table structure for audit_sensitive_word
-- ----------------------------
DROP TABLE IF EXISTS `audit_sensitive_word`;
CREATE TABLE `audit_sensitive_word`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `word` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '敏感词',
  `word_type` tinyint NOT NULL DEFAULT 1 COMMENT '词类型: 1-黑名单 2-白名单',
  `category` tinyint NOT NULL DEFAULT 1 COMMENT '类别: 1-政治敏感 2-色情低俗 3-暴力恐怖 4-广告营销 5-其他违规',
  `level` tinyint NOT NULL DEFAULT 1 COMMENT '等级: 1-弱(标记可疑) 2-中(转人工) 3-强(直接拒绝)',
  `replacement` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '替换词(可选)',
  `is_enabled` tinyint NOT NULL DEFAULT 1 COMMENT '是否启用: 0-禁用 1-启用',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注说明',
  `creator_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_word_type`(`word` ASC, `word_type` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_level`(`level` ASC) USING BTREE,
  INDEX `idx_word_type`(`word_type` ASC) USING BTREE,
  INDEX `idx_enabled`(`is_enabled` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 45 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '敏感词库' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of audit_sensitive_word
-- ----------------------------
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (1, '法轮功', 1, 3, NULL, 1, '邪教组织', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (2, '台独', 1, 3, NULL, 1, '分裂国家', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (3, '藏独', 1, 3, NULL, 1, '分裂国家', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (4, '疆独', 1, 3, NULL, 1, '分裂国家', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (5, '六四', 1, 3, NULL, 1, '政治事件', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (6, '天安门事件', 1, 3, NULL, 1, '政治事件', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (7, '反党', 1, 3, NULL, 1, '政治敏感', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (8, '推翻', 1, 3, NULL, 1, '政治敏感', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (9, '暴动', 1, 3, NULL, 1, '政治敏感', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (10, '颠覆', 1, 3, NULL, 1, '政治敏感', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (11, '色情', 2, 3, NULL, 1, '色情', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (12, '淫秽', 2, 3, NULL, 1, '色情', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (13, '裸聊', 2, 3, NULL, 1, '色情服务', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (14, '一夜情', 2, 3, NULL, 1, '色情', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (15, '约炮', 2, 3, NULL, 1, '色情', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (16, '援交', 2, 3, NULL, 1, '色情交易', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (17, '包养', 2, 3, NULL, 1, '不当关系', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (18, 'sm', 2, 2, NULL, 1, '可疑', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (19, 'AV', 2, 2, NULL, 1, '可疑', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (20, '黄片', 2, 3, NULL, 1, '色情', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (21, '杀人', 3, 3, NULL, 1, '暴力', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (22, '炸弹', 3, 3, NULL, 1, '危险品', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (23, '炸药', 3, 3, NULL, 1, '危险品', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (24, '恐怖袭击', 3, 3, NULL, 1, '暴力', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (25, '砍人', 3, 3, NULL, 1, '暴力', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (26, '血腥', 3, 2, NULL, 1, '可疑', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (27, '加微信', 4, 2, NULL, 1, '引流', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (28, '加QQ', 4, 2, NULL, 1, '引流', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (29, '扫码关注', 4, 2, NULL, 1, '引流', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (30, '兼职赚钱', 4, 2, NULL, 1, '广告', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (31, '日赚', 4, 2, NULL, 1, '诈骗嫌疑', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (32, '刷单', 4, 2, NULL, 1, '违规行为', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (33, '代购', 4, 1, NULL, 1, '商业推广', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (34, '代理', 4, 1, NULL, 1, '商业推广', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (35, '优惠券', 4, 1, NULL, 1, '促销', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (36, '打折', 4, 1, NULL, 1, '促销', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (37, '赌博', 5, 3, NULL, 1, '违法', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (38, '博彩', 5, 3, NULL, 1, '违法', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (39, '彩票', 5, 2, NULL, 1, '需注意', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (40, '毒品', 5, 3, NULL, 1, '违法', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (41, '吸毒', 5, 3, NULL, 1, '违法', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (42, '假币', 5, 3, NULL, 1, '违法', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (43, '发票', 5, 2, NULL, 1, '需注意', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (44, '办证', 5, 2, NULL, 1, '需注意', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');
INSERT INTO `audit_sensitive_word` (`id`, `word`, `word_type`, `category`, `level`, `replacement`, `is_enabled`, `remark`, `creator_id`, `create_time`, `update_time`) VALUES (45, '求购', 2, 5, 1, NULL, 1, '业务白名单', NULL, '2026-04-16 18:38:31', '2026-04-16 18:38:31');

-- ----------------------------
-- Table structure for forum_comment
-- ----------------------------
DROP TABLE IF EXISTS `forum_comment`;
CREATE TABLE `forum_comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '评论者ID',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父评论ID(0表示一级评论)',
  `reply_user_id` bigint NULL DEFAULT NULL COMMENT '回复的用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `images` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片列表(JSON数组)',
  `like_count` int NULL DEFAULT 0 COMMENT '点赞数',
  `is_anonymous` tinyint NULL DEFAULT 0 COMMENT '是否匿名: 0-否 1-是',
  `audit_status` tinyint NULL DEFAULT 1 COMMENT '审核状态: 0-待审核 1-已通过 2-已拒绝',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-已删除 1-正常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '论坛评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of forum_comment
-- ----------------------------
INSERT INTO `forum_comment` VALUES (1, 2, 4, 0, NULL, '是懂', NULL, 0, 0, 1, 1, '2026-04-12 14:18:56', '2026-04-12 14:18:56', 0);
INSERT INTO `forum_comment` VALUES (2, 2, 4, 0, NULL, '上的发', NULL, 0, 0, 1, 1, '2026-04-12 14:26:46', '2026-04-12 14:26:46', 0);
INSERT INTO `forum_comment` VALUES (3, 7, 4, 0, NULL, '实时', NULL, 0, 0, 1, 1, '2026-04-12 14:29:54', '2026-04-12 14:29:54', 0);
INSERT INTO `forum_comment` VALUES (4, 7, 5, 0, NULL, '怎么了你累了', NULL, 0, 0, 1, 1, '2026-04-12 14:38:58', '2026-04-12 14:38:58', 0);
INSERT INTO `forum_comment` VALUES (5, 7, 4, 0, NULL, '无语了', NULL, 0, 0, 1, 1, '2026-04-12 15:22:50', '2026-04-12 15:22:50', 0);
INSERT INTO `forum_comment` VALUES (6, 8, 4, 0, NULL, '无法入睡', NULL, 0, 0, 1, 1, '2026-04-13 13:37:48', '2026-04-13 13:37:48', 0);
INSERT INTO `forum_comment` VALUES (7, 18, 4, 0, NULL, '非常good', NULL, 0, 0, 1, 1, '2026-04-20 13:08:43', '2026-04-20 13:08:43', 0);
INSERT INTO `forum_comment` VALUES (8, 21, 4, 0, NULL, '没事的小伙子', NULL, 0, 0, 1, 1, '2026-04-22 16:25:18', '2026-04-22 16:25:18', 0);
INSERT INTO `forum_comment` VALUES (9, 21, 5, 0, NULL, '真的没事的小伙子', NULL, 0, 0, 1, 1, '2026-04-22 16:26:08', '2026-04-22 16:26:08', 0);

-- ----------------------------
-- Table structure for forum_favorite
-- ----------------------------
DROP TABLE IF EXISTS `forum_favorite`;
CREATE TABLE `forum_favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_type` tinyint NOT NULL COMMENT '目标类型: 1-帖子 2-商品 3-活动',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_target`(`user_id` ASC, `target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 39 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of forum_favorite
-- ----------------------------
INSERT INTO `forum_favorite` VALUES (31, 4, 1, 2, '2026-04-12 14:19:07');
INSERT INTO `forum_favorite` VALUES (32, 4, 1, 7, '2026-04-12 14:29:57');
INSERT INTO `forum_favorite` VALUES (34, 5, 1, 7, '2026-04-12 14:47:29');
INSERT INTO `forum_favorite` VALUES (37, 4, 1, 18, '2026-04-20 13:08:23');
INSERT INTO `forum_favorite` VALUES (38, 4, 1, 21, '2026-04-22 16:24:25');

-- ----------------------------
-- Table structure for forum_follow
-- ----------------------------
DROP TABLE IF EXISTS `forum_follow`;
CREATE TABLE `forum_follow`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID(关注者)',
  `follow_user_id` bigint NOT NULL COMMENT '被关注用户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_follow`(`user_id` ASC, `follow_user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_follow_user_id`(`follow_user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '关注表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of forum_follow
-- ----------------------------
INSERT INTO `forum_follow` VALUES (1, 4, 5, '2026-04-17 17:33:37');

-- ----------------------------
-- Table structure for forum_like
-- ----------------------------
DROP TABLE IF EXISTS `forum_like`;
CREATE TABLE `forum_like`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_type` tinyint NOT NULL COMMENT '目标类型: 1-帖子 2-评论',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_target`(`user_id` ASC, `target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 58 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of forum_like
-- ----------------------------
INSERT INTO `forum_like` VALUES (47, 4, 1, 2, '2026-04-12 14:19:14');
INSERT INTO `forum_like` VALUES (48, 4, 1, 7, '2026-04-12 14:29:56');
INSERT INTO `forum_like` VALUES (49, 5, 1, 7, '2026-04-12 14:39:03');
INSERT INTO `forum_like` VALUES (53, 4, 1, 18, '2026-04-20 13:08:14');
INSERT INTO `forum_like` VALUES (54, 5, 1, 20, '2026-04-20 13:10:14');
INSERT INTO `forum_like` VALUES (55, 4, 1, 11, '2026-04-20 13:59:00');
INSERT INTO `forum_like` VALUES (56, 4, 1, 8, '2026-04-20 13:59:06');
INSERT INTO `forum_like` VALUES (57, 4, 1, 21, '2026-04-22 16:24:22');

-- ----------------------------
-- Table structure for forum_moderator
-- ----------------------------
DROP TABLE IF EXISTS `forum_moderator`;
CREATE TABLE `forum_moderator`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '版主用户ID',
  `module_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '负责的业务模块编码',
  `module_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务模块名称',
  `assigned_by` bigint NULL DEFAULT NULL COMMENT '分配人ID(管理员)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_module`(`user_id` ASC, `module_code` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_module_code`(`module_code` ASC) USING BTREE,
  CONSTRAINT `forum_moderator_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '版主业务模块关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of forum_moderator
-- ----------------------------

-- ----------------------------
-- Table structure for forum_post
-- ----------------------------
DROP TABLE IF EXISTS `forum_post`;
CREATE TABLE `forum_post`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '发布者ID',
  `section_id` bigint NOT NULL COMMENT '板块ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '帖子标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '帖子内容',
  `images` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片列表(JSON数组)',
  `attachments` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '附件列表(JSON数组)',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览量',
  `like_count` int NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` int NULL DEFAULT 0 COMMENT '评论数',
  `favorite_count` int NULL DEFAULT 0 COMMENT '收藏数',
  `share_count` int NULL DEFAULT 0 COMMENT '分享数',
  `is_top` tinyint NULL DEFAULT 0 COMMENT '是否置顶: 0-否 1-是',
  `is_essence` tinyint NULL DEFAULT 0 COMMENT '是否精华: 0-否 1-是',
  `is_anonymous` tinyint NULL DEFAULT 0 COMMENT '是否匿名: 0-否 1-是',
  `audit_status` tinyint NULL DEFAULT 1 COMMENT '审核状态: 0-待审核 1-已通过 2-已拒绝',
  `audit_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核备注',
  `source_type` tinyint NULL DEFAULT 0 COMMENT '来源类型: 0-普通帖子 1-二手商品 2-失物招领 3-活动 4-互助',
  `source_id` bigint NULL DEFAULT NULL COMMENT '来源ID',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-已关闭 1-正常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_section_id`(`section_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_source`(`source_type` ASC, `source_id` ASC) USING BTREE,
  FULLTEXT INDEX `ft_title_content`(`title`, `content`)
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '论坛帖子表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of forum_post
-- ----------------------------
INSERT INTO `forum_post` VALUES (1, 2, 1, '微软', '单位', '[\"/uploads/372fa149-9fb5-454e-93b8-cf3d548cec3b.png\"]', NULL, 4, 0, 0, 0, 0, 0, 0, 0, 2, '审核驳回', 0, NULL, 0, '2026-04-08 19:44:51', '2026-04-28 14:10:00', 0);
INSERT INTO `forum_post` VALUES (2, 2, 1, '二万人', '热污染', '[\"/uploads/b8bbcda7-a337-4358-90cf-13c4ac66858c.png\"]', NULL, 87, 1, 2, 1, 0, 0, 0, 0, 1, '审核通过', 0, NULL, 1, '2026-04-08 19:45:34', '2026-04-28 13:36:09', 1);
INSERT INTO `forum_post` VALUES (3, 2, 1, '微软', '是为', '[\"http://localhost:8080/api/uploads/0e66cc14-05cb-4659-8bc5-b6b25752fddf.png\"]', NULL, 1, 0, 0, 0, 0, 0, 0, 0, 2, '不完整', 0, NULL, 0, '2026-04-08 20:04:01', '2026-04-28 14:09:57', 0);
INSERT INTO `forum_post` VALUES (4, 4, 2, 'oppo A5', '我的oppo A5 不知道被谁给捡到了 如果捡到有偿答谢', '[\"http://localhost:8080/api/uploads/e18da433-f457-4459-aeef-c1f6cac7cc76.jpg\"]', NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, NULL, 1, '2026-04-10 14:57:11', '2026-04-10 14:57:11', 0);
INSERT INTO `forum_post` VALUES (5, 4, 1, '1', '1\n联系方式: 1', '[]', NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 1, 1, 1, '2026-04-11 16:32:53', '2026-04-11 16:32:53', 0);
INSERT INTO `forum_post` VALUES (6, 4, 1, '相机', '可刀', '[\"http://localhost:8080/api/uploads/618e91b2-9ecc-43df-8ee1-199d6968aa49.jpg\"]', NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 1, 2, 1, '2026-04-12 13:19:45', '2026-04-12 13:19:45', 0);
INSERT INTO `forum_post` VALUES (7, 4, 4, '代取', '有没有人想帮我拿一下快递', '[\"http://localhost:8080/api/uploads/9be6c8c2-34ff-46a3-bbc6-8c8ee5e37b2e.png\"]', NULL, 55, 2, 3, 2, 0, 0, 0, 0, 1, '审核通过', 0, NULL, 1, '2026-04-12 14:28:46', '2026-04-28 21:14:54', 0);
INSERT INTO `forum_post` VALUES (8, 5, 6, '考研', '考研上岸清华', '[\"38c184af-421f-40af-9bbf-5e33779edfb9.jpg\"]', NULL, 17, 1, 1, 0, 0, 0, 0, 0, 1, '审核通过', 0, NULL, 1, '2026-04-12 16:03:07', '2026-04-20 13:59:25', 0);
INSERT INTO `forum_post` VALUES (9, 5, 2, '雨伞', '雨伞', '[\"a54ca1ec-e99d-4ad1-8379-863f46369e91.webp\"]', NULL, 2, 0, 0, 0, 0, 0, 0, 0, 1, '审核通过', 2, 1, 1, '2026-04-13 15:44:00', '2026-04-17 16:13:22', 0);
INSERT INTO `forum_post` VALUES (10, 5, 5, '考研', '有人能推荐好用的考研指导书吗', '[\"http://localhost:8080/api/uploads/3de5fbdc-35e3-4a12-a5b5-80788ab6b945.png\"]', NULL, 6, 0, 0, 0, 0, 0, 0, 0, 1, NULL, 0, NULL, 1, '2026-04-14 13:47:34', '2026-04-28 21:19:59', 0);
INSERT INTO `forum_post` VALUES (11, 5, 1, '敏感词判断', 'cnm 福瑞', '[\"http://localhost:8080/api/uploads/0fe67770-f991-49b9-bd6e-ffcf616a5cec.png\"]', NULL, 14, 1, 0, 0, 0, 0, 0, 0, 2, '涉嫌违禁 请下架', 0, NULL, 0, '2026-04-14 17:06:28', '2026-04-28 21:21:20', 0);
INSERT INTO `forum_post` VALUES (12, 5, 1, '敏感词判断二代', 'cnm 福瑞', '[\"http://localhost:8080/api/uploads/6802c78c-03c5-466a-b5ee-1bada59c2a77.png\"]', NULL, 9, 0, 0, 0, 0, 0, 0, 0, 2, '驳回通知', 0, NULL, 0, '2026-04-14 17:30:50', '2026-04-28 21:13:57', 0);
INSERT INTO `forum_post` VALUES (13, 4, 1, '敏感词判断三代', '福瑞 cnm', '[\"http://localhost:8080/api/uploads/aeca29fc-5126-4939-bfd6-6594260e5ba1.png\"]', NULL, 5, 0, 0, 0, 0, 0, 0, 0, 2, '消息通知测试 驳回', 0, NULL, 0, '2026-04-15 13:30:35', '2026-04-15 16:53:10', 0);
INSERT INTO `forum_post` VALUES (14, 4, 2, '钱包', '黑色钱包', '[\"c4e54ef6-abe5-485d-afdd-4be612ef6c91.jpg\"]', NULL, 3, 0, 0, 0, 0, 0, 0, 0, 1, NULL, 2, 2, 1, '2026-04-15 17:35:16', '2026-04-25 17:20:14', 0);
INSERT INTO `forum_post` VALUES (15, 4, 2, '校园卡（测试）', '蓝绿色', '[\"f51ea92d-6553-4b5b-9d42-65728ccec706.png\"]', NULL, 7, 0, 0, 0, 0, 0, 0, 0, 1, NULL, 2, 3, 1, '2026-04-15 19:01:34', '2026-04-18 14:54:25', 0);
INSERT INTO `forum_post` VALUES (16, 4, 3, '支援活动', '打扫街道', NULL, NULL, 2, 0, 0, 0, 0, 0, 0, 0, 1, NULL, 3, 1, 1, '2026-04-16 19:41:01', '2026-04-22 16:09:58', 0);
INSERT INTO `forum_post` VALUES (17, 4, 9, '水水水', '太水了', '[\"http://localhost:8080/api/uploads/239d6573-c403-4272-90f9-49f48d0a9811.jpg\"]', NULL, 3, 0, 0, 0, 0, 0, 0, 0, 1, NULL, 0, NULL, 1, '2026-04-16 19:54:38', '2026-04-22 16:20:17', 0);
INSERT INTO `forum_post` VALUES (18, 5, 1, '相机', '黑色九九新', '[\"90658943-0748-4291-ac9b-c37aa12a8d8f.jpg\"]', NULL, 9, 1, 1, 1, 0, 0, 0, 0, 1, NULL, 1, 3, 1, '2026-04-17 15:41:05', '2026-04-22 13:29:15', 0);
INSERT INTO `forum_post` VALUES (19, 4, 1, '二轮测试', '二轮测试', '[]', NULL, 3, 0, 0, 0, 0, 0, 0, 0, 1, NULL, 1, 4, 1, '2026-04-18 16:23:44', '2026-04-22 16:20:13', 0);
INSERT INTO `forum_post` VALUES (20, 4, 1, '[求购] 考研指导书', '章鱼\n\n[期望交易时间] 2026-04-18 19:00', '[\"198dd36a-5e2d-4343-bdea-928c80fc5376.jpg\"]', NULL, 4, 1, 0, 0, 0, 0, 0, 0, 1, NULL, 1, 5, 1, '2026-04-18 18:20:24', '2026-04-28 13:35:55', 0);
INSERT INTO `forum_post` VALUES (21, 4, 8, '失恋了怎么办', '我和我谈了十年的女朋友分手了 我感觉很迷茫 不知道怎么办才好', NULL, NULL, 4, 1, 2, 1, 0, 0, 0, 0, 1, NULL, 0, NULL, 1, '2026-04-22 16:24:02', '2026-04-22 16:26:33', 0);
INSERT INTO `forum_post` VALUES (22, 3, 7, '有谁找到工作了', '到底都是谁找到工作了 我想找个工作怎么这么难', NULL, NULL, 1, 0, 0, 0, 0, 0, 0, 0, 1, NULL, 0, NULL, 1, '2026-04-22 16:29:06', '2026-04-22 16:35:11', 0);
INSERT INTO `forum_post` VALUES (23, 4, 2, '鞋子', '科比球鞋', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 1, NULL, 0, NULL, 1, '2026-04-24 17:20:51', '2026-04-24 17:20:51', 0);
INSERT INTO `forum_post` VALUES (24, 4, 7, '杭州求职', '有没有谁在杭州找到工作啊', '[\"http://localhost:8081/api/uploads/eeabb900-771c-4064-89b5-049627f877f1.jpg\"]', NULL, 10, 0, 0, 0, 0, 0, 0, 0, 1, '无问题', 0, NULL, 1, '2026-04-27 18:00:48', '2026-04-28 21:10:38', 0);
INSERT INTO `forum_post` VALUES (25, 5, 2, '算法导论', '黑色书籍 第三版', '[\"9bc7dfee-2eae-4b1f-a163-b031f9a9ad91.png\"]', NULL, 4, 0, 0, 0, 0, 0, 0, 0, 1, NULL, 2, 4, 1, '2026-04-27 18:40:35', '2026-04-28 17:10:15', 0);
INSERT INTO `forum_post` VALUES (26, 2, 9, '我真服了 毕业论文要怎么做', '这个格式一直改都改不对', NULL, NULL, 2, 0, 0, 0, 0, 0, 0, 0, 1, NULL, 0, NULL, 1, '2026-04-28 13:56:38', '2026-04-28 14:09:51', 0);

-- ----------------------------
-- Table structure for forum_section
-- ----------------------------
DROP TABLE IF EXISTS `forum_section`;
CREATE TABLE `forum_section`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '板块ID',
  `section_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '板块名称',
  `section_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '板块编码',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '板块描述',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '板块图标',
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '板块封面图',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `post_count` int NULL DEFAULT 0 COMMENT '帖子数量',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
  `is_default` tinyint NULL DEFAULT 0 COMMENT '是否默认板块: 0-否 1-是',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_section_code`(`section_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '论坛板块表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of forum_section
-- ----------------------------
INSERT INTO `forum_section` VALUES (1, '跳蚤市场', 'FLEA_MARKET', '二手物品交易专区，商品自动同步至此板块', '🛒', NULL, 1, 0, 1, 0, '2026-04-08 16:26:18', '2026-04-11 15:02:54', 0);
INSERT INTO `forum_section` VALUES (2, '失物招领', 'LOST_FOUND', '失物招领信息发布区，寻物/招领自动同步', '🔍', NULL, 2, 0, 1, 0, '2026-04-08 16:26:18', '2026-04-11 15:02:55', 0);
INSERT INTO `forum_section` VALUES (3, '校园活动', 'ACTIVITY', '校园活动信息发布与讨论区', '🎉', NULL, 3, 0, 1, 0, '2026-04-08 16:26:18', '2026-04-11 15:02:56', 0);
INSERT INTO `forum_section` VALUES (4, '互助广场', 'HELP_SQUARE', '快递代取、跑腿帮忙等互助信息', '🤝', NULL, 4, 0, 1, 0, '2026-04-08 16:26:18', '2026-04-08 16:26:18', 0);
INSERT INTO `forum_section` VALUES (5, '课程资料', 'COURSE_MATERIAL', '课程笔记、考试资料分享交流', '📚', NULL, 5, 0, 1, 0, '2026-04-08 16:26:18', '2026-04-08 16:26:18', 0);
INSERT INTO `forum_section` VALUES (6, '学术交流', 'ACADEMIC', '学术讨论、科研合作、竞赛组队', '🎓', NULL, 6, 0, 1, 0, '2026-04-08 16:26:18', '2026-04-08 16:26:18', 0);
INSERT INTO `forum_section` VALUES (7, '实习就业', 'JOB', '实习信息、招聘信息、求职经验', '💼', NULL, 7, 0, 1, 0, '2026-04-08 16:26:18', '2026-04-08 16:26:18', 0);
INSERT INTO `forum_section` VALUES (8, '情感树洞', 'EMOTION', '情感交流、心情分享、匿名倾诉', '💬', NULL, 8, 0, 1, 0, '2026-04-08 16:26:18', '2026-04-08 16:26:18', 0);
INSERT INTO `forum_section` VALUES (9, '灌水区', 'WATER', '自由灌水、日常分享、闲聊吐槽', '💧', NULL, 9, 0, 1, 1, '2026-04-08 16:26:18', '2026-04-08 16:26:18', 0);

-- ----------------------------
-- Table structure for info_news
-- ----------------------------
DROP TABLE IF EXISTS `info_news`;
CREATE TABLE `info_news`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资讯标题',
  `summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '摘要',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '内容',
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图',
  `source` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源',
  `source_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '原文链接',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览量',
  `is_top` tinyint NULL DEFAULT 0 COMMENT '是否置顶',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-下架 1-正常',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_publish_time`(`publish_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '校园资讯表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of info_news
-- ----------------------------
INSERT INTO `info_news` VALUES (1, '图书馆延长开放通知', '期末周图书馆开放时间延长至22:30。', '为方便同学复习备考，图书馆自本周起工作日延长开放至22:30，请遵守馆内秩序。', '', '校图书馆', 'https://library.example.edu.cn', '图书馆', 3, 1, 1, '2026-04-08 16:26:29', '2026-04-08 16:26:29', '2026-04-22 19:27:23', 0);
INSERT INTO `info_news` VALUES (2, '教务系统维护公告', '本周六凌晨教务系统维护，期间无法选课与查分。', '维护时间：周六 00:30-03:30。维护完成后服务自动恢复，如有问题请联系教务处。', '', '教务处', 'https://jwc.example.edu.cn', '教务', 1, 0, 1, '2026-04-08 16:26:29', '2026-04-08 16:26:29', '2026-04-17 16:32:06', 0);
INSERT INTO `info_news` VALUES (3, '春季校园招聘会报名开启', '2026 春季校园招聘会已开放报名。', '请有意向参会的同学在就业系统完成报名，携带学生证按时入场。', '', '就业指导中心', 'https://job.example.edu.cn', '就业', 2, 0, 1, '2026-04-08 16:26:29', '2026-04-08 16:26:29', '2026-04-20 14:18:31', 0);
INSERT INTO `info_news` VALUES (4, '校车班次调整说明', '东门至南苑方向新增晚间班次。', '新增班次将于下周一试运行，具体时刻请关注后勤处最新通知。', '', '后勤处', 'https://hq.example.edu.cn', '后勤', 3, 0, 1, '2026-04-08 16:26:29', '2026-04-08 16:26:29', '2026-04-20 14:18:28', 0);
INSERT INTO `info_news` VALUES (5, '论文查重以及有关事项', '查重平台', '针对2026届毕业生论文工作 我发表以下几个内容', '', '教务处', '', '教务处', 2, 0, 1, '2026-04-20 14:18:07', '2026-04-20 14:18:07', '2026-04-20 14:18:36', 0);

-- ----------------------------
-- Table structure for info_service_nav
-- ----------------------------
DROP TABLE IF EXISTS `info_service_nav`;
CREATE TABLE `info_service_nav`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '服务名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '链接地址',
  `phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地址',
  `longitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '纬度',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '校内服务导航表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of info_service_nav
-- ----------------------------
INSERT INTO `info_service_nav` VALUES (1, '常用网站', '教务系统', '查课表、成绩、选课', '📋', 'https://jwc.example.edu.cn', NULL, NULL, NULL, NULL, 1, 1, '2026-04-08 16:26:18', '2026-04-08 16:26:18');
INSERT INTO `info_service_nav` VALUES (2, '常用网站', '图书馆', '图书查询、借阅续借', '📚', 'https://lib.example.edu.cn', NULL, NULL, NULL, NULL, 2, 1, '2026-04-08 16:26:18', '2026-04-08 16:26:18');
INSERT INTO `info_service_nav` VALUES (3, '常用网站', '校园卡', '校园卡充值、挂失', '💳', 'https://card.example.edu.cn', NULL, NULL, NULL, NULL, 3, 1, '2026-04-08 16:26:18', '2026-04-08 16:26:18');
INSERT INTO `info_service_nav` VALUES (4, '校园服务', '后勤服务', '报修、投诉、建议', '🔧', NULL, '400-123-4567', NULL, NULL, NULL, 1, 1, '2026-04-08 16:26:18', '2026-04-08 16:26:18');
INSERT INTO `info_service_nav` VALUES (5, '校园服务', '心理咨询', '心理健康咨询预约', '❤️', NULL, '400-123-4568', NULL, NULL, NULL, 2, 1, '2026-04-08 16:26:18', '2026-04-08 16:26:18');
INSERT INTO `info_service_nav` VALUES (6, '校园服务', '校医院', '医疗服务、体检预约', '🏥', NULL, '400-123-4569', NULL, NULL, NULL, 3, 1, '2026-04-08 16:26:18', '2026-04-08 16:26:18');
INSERT INTO `info_service_nav` VALUES (7, '交通出行', '校车时刻', '校车路线及时刻表', '🚌', NULL, NULL, NULL, NULL, NULL, 1, 1, '2026-04-08 16:26:18', '2026-04-08 16:26:18');
INSERT INTO `info_service_nav` VALUES (8, '教学服务', '教务处', '课程安排、成绩与学籍相关业务', '', 'https://jwc.example.edu.cn', '010-12345601', '行政楼 201', 116.397128, 39.916527, 1, 1, '2026-04-08 16:26:29', '2026-04-08 16:26:29');
INSERT INTO `info_service_nav` VALUES (9, '图书服务', '图书馆总服务台', '借阅、续借与馆藏咨询', '', 'https://library.example.edu.cn', '010-12345602', '图书馆一层服务台', 116.398120, 39.917621, 2, 1, '2026-04-08 16:26:29', '2026-04-08 16:26:29');
INSERT INTO `info_service_nav` VALUES (10, '生活服务', '校园卡中心', '校园卡补办、充值与挂失', '', '', '010-12345603', '生活服务中心 108', 116.396612, 39.915902, 3, 1, '2026-04-08 16:26:29', '2026-04-08 16:26:29');
INSERT INTO `info_service_nav` VALUES (11, '交通服务', '校车调度室', '校车班次咨询与失物登记', '', '', '010-12345604', '北门交通服务站', 116.394951, 39.918103, 4, 1, '2026-04-08 16:26:29', '2026-04-08 16:26:29');
INSERT INTO `info_service_nav` VALUES (12, '生活服务', '失物招领处', '领取遗失的物品', '', '', '10086', '北16', NULL, NULL, 0, 1, '2026-04-20 14:28:25', '2026-04-22 19:27:28');

-- ----------------------------
-- Table structure for message_conversation
-- ----------------------------
DROP TABLE IF EXISTS `message_conversation`;
CREATE TABLE `message_conversation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `conversation_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_user_id` bigint NOT NULL COMMENT '对方用户ID',
  `last_message_id` bigint NULL DEFAULT NULL COMMENT '最后一条消息ID',
  `last_message_content` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最后一条消息内容预览',
  `last_message_time` datetime NULL DEFAULT NULL COMMENT '最后消息时间',
  `unread_count` int NULL DEFAULT 0 COMMENT '未读消息数',
  `is_top` tinyint NULL DEFAULT 0 COMMENT '是否置顶: 0-否 1-是',
  `is_muted` tinyint NULL DEFAULT 0 COMMENT '是否免打扰: 0-否 1-是',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-已删除 1-正常',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_conversation_user`(`conversation_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_update_time`(`update_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 115 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of message_conversation
-- ----------------------------
INSERT INTO `message_conversation` VALUES (1, '4_5', 5, 4, 52, '刘焉', '2026-04-22 16:00:05', 0, 0, 0, 1, '2026-04-28 18:11:04');
INSERT INTO `message_conversation` VALUES (2, '4_5', 4, 5, 52, '刘焉', '2026-04-22 16:00:05', 0, 0, 0, 1, '2026-04-23 18:25:35');
INSERT INTO `message_conversation` VALUES (105, '3_4', 3, 4, 57, '你好', '2026-04-22 16:35:26', 1, 0, 0, 1, '2026-04-22 16:35:26');
INSERT INTO `message_conversation` VALUES (106, '3_4', 4, 3, 57, '你好', '2026-04-22 16:35:26', 1, 0, 0, 1, '2026-04-22 16:35:26');
INSERT INTO `message_conversation` VALUES (107, '3_5', 5, 3, 55, '你好', '2026-04-22 16:32:04', 0, 0, 0, 1, '2026-04-22 16:32:07');
INSERT INTO `message_conversation` VALUES (108, '3_5', 3, 5, 55, '你好', '2026-04-22 16:32:04', 0, 0, 0, 1, '2026-04-22 16:32:04');
INSERT INTO `message_conversation` VALUES (111, '2_5', 2, 5, 56, '你好', '2026-04-22 16:33:01', 0, 0, 0, 1, '2026-04-22 16:33:01');
INSERT INTO `message_conversation` VALUES (112, '2_5', 5, 2, 56, '你好', '2026-04-22 16:33:01', 0, 0, 0, 1, '2026-04-22 16:33:20');

-- ----------------------------
-- Table structure for message_delivery_task
-- ----------------------------
DROP TABLE IF EXISTS `message_delivery_task`;
CREATE TABLE `message_delivery_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `message_id` bigint NOT NULL COMMENT '消息ID',
  `conversation_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint NOT NULL COMMENT '接收者ID',
  `client_message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '客户端消息ID',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态: 0-PENDING 1-ACKED 2-GIVE_UP',
  `retry_count` int NOT NULL DEFAULT 0 COMMENT '重试次数',
  `next_retry_time` datetime NOT NULL COMMENT '下一次重试时间',
  `ack_time` datetime NULL DEFAULT NULL COMMENT 'ACK确认时间',
  `last_error` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最近一次错误',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_message_id`(`message_id` ASC) USING BTREE,
  INDEX `idx_pending_retry`(`status` ASC, `next_retry_time` ASC) USING BTREE,
  INDEX `idx_receiver_status`(`receiver_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息投递任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of message_delivery_task
-- ----------------------------
INSERT INTO `message_delivery_task` VALUES (1, 40, '4_5', 5, 4, 'wx-1776843621629-shyr9eyh', 2, 6, '2026-04-22 15:48:41', NULL, 'ACK_TIMEOUT_MAX_RETRY_REACHED', '2026-04-22 15:40:23', '2026-04-22 15:48:41');
INSERT INTO `message_delivery_task` VALUES (2, 41, '4_5', 5, 4, 'wx-1776843653470-w957ngdt', 2, 6, '2026-04-22 15:49:16', NULL, 'ACK_TIMEOUT_MAX_RETRY_REACHED', '2026-04-22 15:40:55', '2026-04-22 15:49:16');
INSERT INTO `message_delivery_task` VALUES (3, 42, '4_5', 4, 5, 'wx-1776843672102-zbdfmt5k', 2, 6, '2026-04-22 15:49:36', NULL, 'ACK_TIMEOUT_MAX_RETRY_REACHED', '2026-04-22 15:41:13', '2026-04-22 15:49:36');
INSERT INTO `message_delivery_task` VALUES (4, 43, '4_5', 5, 4, 'wx-1776843683474-uhcgm596', 2, 6, '2026-04-22 15:49:41', NULL, 'ACK_TIMEOUT_MAX_RETRY_REACHED', '2026-04-22 15:41:25', '2026-04-22 15:49:41');
INSERT INTO `message_delivery_task` VALUES (5, 44, '4_5', 5, 4, 'wx-1776843736613-p3lgxaos', 2, 6, '2026-04-22 15:50:51', NULL, 'ACK_TIMEOUT_MAX_RETRY_REACHED', '2026-04-22 15:42:18', '2026-04-22 15:50:51');
INSERT INTO `message_delivery_task` VALUES (6, 45, '4_5', 5, 4, 'wx-1776843742238-i93d2qky', 2, 6, '2026-04-22 15:50:51', NULL, 'ACK_TIMEOUT_MAX_RETRY_REACHED', '2026-04-22 15:42:23', '2026-04-22 15:50:51');
INSERT INTO `message_delivery_task` VALUES (7, 46, '4_5', 5, 4, 'wx-1776843788548-f5dwaprl', 2, 6, '2026-04-22 15:51:16', NULL, 'ACK_TIMEOUT_MAX_RETRY_REACHED', '2026-04-22 15:43:10', '2026-04-22 15:51:16');
INSERT INTO `message_delivery_task` VALUES (8, 47, '4_5', 4, 5, 'wx-1776843796633-1a3obmil', 2, 6, '2026-04-22 15:51:21', NULL, 'ACK_TIMEOUT_MAX_RETRY_REACHED', '2026-04-22 15:43:18', '2026-04-22 15:51:21');
INSERT INTO `message_delivery_task` VALUES (9, 48, '4_5', 4, 5, 'wx-1776843822512-8bt4itti', 2, 6, '2026-04-22 15:52:12', NULL, 'ACK_TIMEOUT_MAX_RETRY_REACHED', '2026-04-22 15:43:44', '2026-04-22 15:52:16');
INSERT INTO `message_delivery_task` VALUES (10, 49, '4_5', 5, 4, 'wx-1776843828183-nlwne32i', 2, 6, '2026-04-22 15:52:12', NULL, 'ACK_TIMEOUT_MAX_RETRY_REACHED', '2026-04-22 15:43:49', '2026-04-22 15:52:16');
INSERT INTO `message_delivery_task` VALUES (11, 50, '4_5', 4, 5, 'wx-1776844038778-trxo4sqe', 2, 6, '2026-04-22 15:55:27', NULL, 'ACK_TIMEOUT_MAX_RETRY_REACHED', '2026-04-22 15:47:20', '2026-04-22 15:55:27');
INSERT INTO `message_delivery_task` VALUES (12, 51, '4_5', 5, 4, 'wx-1776844042527-h4zoune2', 2, 6, '2026-04-22 15:55:32', NULL, 'ACK_TIMEOUT_MAX_RETRY_REACHED', '2026-04-22 15:47:24', '2026-04-22 15:55:32');
INSERT INTO `message_delivery_task` VALUES (13, 52, '4_5', 5, 4, 'wx-1776844804141-7i786bni', 2, 6, '2026-04-22 16:08:24', NULL, 'ACK_TIMEOUT_MAX_RETRY_REACHED', '2026-04-22 16:00:05', '2026-04-22 16:08:28');
INSERT INTO `message_delivery_task` VALUES (14, 53, '3_4', 3, 4, 'wx-1776846568893-c07oxk6x', 2, 6, '2026-04-22 16:37:37', NULL, 'ACK_TIMEOUT_MAX_RETRY_REACHED', '2026-04-22 16:29:30', '2026-04-22 16:37:39');
INSERT INTO `message_delivery_task` VALUES (15, 54, '3_5', 5, 3, 'wx-1776846649544-2glb3lp2', 2, 6, '2026-04-22 16:39:12', NULL, 'ACK_TIMEOUT_MAX_RETRY_REACHED', '2026-04-22 16:30:51', '2026-04-22 16:39:15');
INSERT INTO `message_delivery_task` VALUES (16, 55, '3_5', 3, 5, 'wx-1776846722765-c7so0xzw', 2, 6, '2026-04-22 16:40:24', NULL, 'ACK_TIMEOUT_MAX_RETRY_REACHED', '2026-04-22 16:32:04', '2026-04-22 16:40:25');
INSERT INTO `message_delivery_task` VALUES (17, 56, '2_5', 2, 5, 'wx-1776846780153-1xmvxk8d', 2, 6, '2026-04-22 16:41:20', NULL, 'ACK_TIMEOUT_MAX_RETRY_REACHED', '2026-04-22 16:33:01', '2026-04-22 16:41:20');
INSERT INTO `message_delivery_task` VALUES (18, 57, '3_4', 4, 3, 'wx-1776846925153-9csdam31', 2, 6, '2026-04-22 16:43:31', NULL, 'ACK_TIMEOUT_MAX_RETRY_REACHED', '2026-04-22 16:35:26', '2026-04-22 16:43:31');

-- ----------------------------
-- Table structure for message_notification
-- ----------------------------
DROP TABLE IF EXISTS `message_notification`;
CREATE TABLE `message_notification`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '接收用户ID(0表示全体用户)',
  `sender_id` bigint NULL DEFAULT NULL COMMENT '发送者ID(系统通知为空)',
  `type` tinyint NOT NULL COMMENT '通知类型: 1-系统公告 2-帖子回复 3-评论回复 4-点赞 5-关注 6-交易通知 7-活动通知 8-互助通知',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '通知标题',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '通知内容',
  `target_type` tinyint NULL DEFAULT NULL COMMENT '目标类型: 1-帖子 2-评论 3-商品 4-活动 5-互助',
  `target_id` bigint NULL DEFAULT NULL COMMENT '目标ID',
  `is_read` tinyint NULL DEFAULT 0 COMMENT '是否已读: 0-未读 1-已读',
  `read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_is_read`(`is_read` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of message_notification
-- ----------------------------
INSERT INTO `message_notification` VALUES (1, 4, NULL, 6, '商品审核通知', '您发布的商品《1》已审核通过，现在可以在平台上看到了。', 3, NULL, 0, NULL, '2026-04-11 16:41:53');
INSERT INTO `message_notification` VALUES (2, 4, NULL, 6, '商品审核通知', '您发布的商品《相机》已审核通过，现在可以在平台上看到了。', 3, NULL, 0, NULL, '2026-04-12 13:21:13');
INSERT INTO `message_notification` VALUES (3, 5, 4, 3, '管理孤倩 评论了你的帖子', '无法入睡', 1, 8, 1, '2026-04-15 19:21:55', '2026-04-13 13:37:48');
INSERT INTO `message_notification` VALUES (4, 5, NULL, 6, '失物招领审核通知', '您发布的失物招领《雨伞》已审核通过，现在可以在平台上看到了。', 3, NULL, 0, NULL, '2026-04-13 15:44:18');
INSERT INTO `message_notification` VALUES (5, 4, NULL, 9, '举报处理完成', '已删除', NULL, NULL, 1, '2026-04-15 17:01:10', '2026-04-15 16:24:46');
INSERT INTO `message_notification` VALUES (6, 4, NULL, 8, '帖子审核未通过', '消息通知测试 驳回', NULL, NULL, 1, '2026-04-15 17:01:19', '2026-04-15 16:52:53');
INSERT INTO `message_notification` VALUES (8, 4, NULL, 9, '举报处理完成', '感谢您的举报 我们已经处理', NULL, NULL, 1, '2026-04-15 17:01:07', '2026-04-15 17:00:57');
INSERT INTO `message_notification` VALUES (12, 4, NULL, 8, '互助单被接单通知', '您发布的互助单《代买》已被接单，请及时联系对方。', 5, 2, 1, '2026-04-17 16:40:56', '2026-04-16 20:07:40');
INSERT INTO `message_notification` VALUES (13, 4, NULL, 8, '互助单被接单通知', '您发布的互助单《大大》已被接单，请及时联系对方。', 5, 4, 1, '2026-04-17 16:40:45', '2026-04-17 16:40:19');
INSERT INTO `message_notification` VALUES (14, 4, NULL, 8, '互助单完成通知', '您发布的互助单《大大》已完成，感谢您的使用。', 5, 4, 1, '2026-04-17 16:56:14', '2026-04-17 16:55:50');
INSERT INTO `message_notification` VALUES (15, 4, NULL, 8, '互助审核通过', '审核通过', NULL, NULL, 1, '2026-04-17 19:01:28', '2026-04-17 16:57:42');
INSERT INTO `message_notification` VALUES (16, 4, NULL, 8, '互助单被接单通知', '您发布的互助单《互助闭环测试》已被接单，请及时联系对方。', 5, 5, 1, '2026-04-17 17:33:17', '2026-04-17 17:00:07');
INSERT INTO `message_notification` VALUES (17, 4, NULL, 8, '互助单完成通知', '您发布的互助单《互助闭环测试》已完成，感谢您的使用。', 5, 5, 1, '2026-04-17 17:02:23', '2026-04-17 17:00:49');
INSERT INTO `message_notification` VALUES (18, 5, 4, 5, '你有新的关注', '管理孤倩 关注了你', NULL, NULL, 0, NULL, '2026-04-17 17:33:37');
INSERT INTO `message_notification` VALUES (19, 5, NULL, 7, '活动报名通知', '您已成功报名活动《支援活动》，请按时参加。', 4, 1, 0, NULL, '2026-04-17 18:48:55');
INSERT INTO `message_notification` VALUES (20, 5, NULL, 8, '互助单被接单通知', '您发布的互助单《测试问题》已被接单，请及时联系对方。', 5, 7, 1, '2026-04-22 16:26:26', '2026-04-17 18:56:52');
INSERT INTO `message_notification` VALUES (21, 5, NULL, 8, '互助单完成通知', '您发布的互助单《测试问题》已完成，感谢您的使用。', 5, 7, 1, '2026-04-20 13:59:32', '2026-04-17 19:01:09');
INSERT INTO `message_notification` VALUES (22, 5, NULL, 8, '互助单被接单通知', '您发布的互助单《第二次测试》已被接单，请及时联系对方。', 5, 8, 1, '2026-04-20 13:09:17', '2026-04-17 19:23:23');
INSERT INTO `message_notification` VALUES (23, 5, NULL, 8, '互助单完成通知', '您发布的互助单《第二次测试》已完成，感谢您的使用。', 5, 8, 1, '2026-04-20 13:09:26', '2026-04-17 19:47:08');
INSERT INTO `message_notification` VALUES (24, 5, 4, 3, '管理孤倩 评论了你的帖子', '非常good', 1, 18, 1, '2026-04-20 13:09:11', '2026-04-20 13:08:43');
INSERT INTO `message_notification` VALUES (25, 5, 4, 4, '管理孤倩 赞了你的帖子', '敏感词判断', 1, 11, 1, '2026-04-22 16:26:19', '2026-04-20 13:59:00');
INSERT INTO `message_notification` VALUES (26, 5, 4, 4, '管理孤倩 赞了你的帖子', '考研', 1, 8, 1, '2026-04-20 13:59:21', '2026-04-20 13:59:06');
INSERT INTO `message_notification` VALUES (27, 4, 5, 3, '用户星倩 评论了你的帖子', '真的没事的小伙子', 1, 21, 0, NULL, '2026-04-22 16:26:08');
INSERT INTO `message_notification` VALUES (28, 4, NULL, 8, '帖子审核通过', '无问题', NULL, NULL, 1, '2026-04-28 21:11:42', '2026-04-28 21:10:38');
INSERT INTO `message_notification` VALUES (29, 5, NULL, 9, '举报处理完成', '帖子已审查，举报已完成处理', NULL, NULL, 1, '2026-04-28 21:11:18', '2026-04-28 21:10:38');
INSERT INTO `message_notification` VALUES (30, 5, NULL, 8, '帖子审核未通过', '驳回通知', NULL, NULL, 1, '2026-04-28 21:14:15', '2026-04-28 21:13:57');
INSERT INTO `message_notification` VALUES (31, 4, NULL, 9, '举报处理完成', '已下架', NULL, NULL, 1, '2026-04-28 21:21:54', '2026-04-28 21:13:57');
INSERT INTO `message_notification` VALUES (32, 5, NULL, 8, '帖子审核未通过', '涉嫌违禁 请下架', NULL, NULL, 0, NULL, '2026-04-28 21:21:20');
INSERT INTO `message_notification` VALUES (33, 4, NULL, 9, '举报处理完成', '帖子已审查，举报已完成处理', NULL, NULL, 1, '2026-04-28 21:21:58', '2026-04-28 21:21:20');

-- ----------------------------
-- Table structure for message_private
-- ----------------------------
DROP TABLE IF EXISTS `message_private`;
CREATE TABLE `message_private`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `conversation_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint NOT NULL COMMENT '接收者ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `content_type` tinyint NULL DEFAULT 1 COMMENT '内容类型: 1-文本 2-图片 3-商品卡片',
  `client_message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '客户端消息ID(幂等键)',
  `is_read` tinyint NULL DEFAULT 0 COMMENT '是否已读: 0-未读 1-已读',
  `read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sender_client_message`(`sender_id` ASC, `client_message_id` ASC) USING BTREE,
  INDEX `idx_conversation_id`(`conversation_id` ASC) USING BTREE,
  INDEX `idx_sender_id`(`sender_id` ASC) USING BTREE,
  INDEX `idx_receiver_id`(`receiver_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_conversation_cursor`(`conversation_id` ASC, `id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 58 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私信消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of message_private
-- ----------------------------
INSERT INTO `message_private` VALUES (1, '4_5', 5, 4, '你好', 1, NULL, 1, '2026-04-16 21:01:26', '2026-04-16 21:00:54', 0);
INSERT INTO `message_private` VALUES (2, '4_5', 4, 5, '你好', 1, NULL, 1, '2026-04-17 15:57:38', '2026-04-16 21:09:25', 0);
INSERT INTO `message_private` VALUES (3, '4_5', 4, 5, '你好啊', 1, NULL, 1, '2026-04-17 15:57:38', '2026-04-17 14:40:34', 0);
INSERT INTO `message_private` VALUES (4, '4_5', 5, 4, '你好', 1, NULL, 1, '2026-04-22 13:30:29', '2026-04-18 17:39:23', 0);
INSERT INTO `message_private` VALUES (5, '4_5', 5, 4, '你好', 1, NULL, 1, '2026-04-22 13:30:29', '2026-04-20 18:56:17', 0);
INSERT INTO `message_private` VALUES (6, '4_5', 5, 4, '你好啊', 1, 'f2d60023-e8da-4c7e-80d1-42470adf4aac', 1, '2026-04-22 13:30:29', '2026-04-22 13:05:03', 0);
INSERT INTO `message_private` VALUES (7, '4_5', 5, 4, '你好', 1, '49a9e79e-1737-49d2-9a09-a86da76b12ea', 1, '2026-04-22 13:30:47', '2026-04-22 13:30:34', 0);
INSERT INTO `message_private` VALUES (8, '4_5', 4, 5, '你好', 1, '7c3b389f-fdbe-4263-9950-c2689d926652', 1, '2026-04-22 13:56:31', '2026-04-22 13:30:58', 0);
INSERT INTO `message_private` VALUES (9, '4_5', 5, 4, '你好', 1, '2b11a465-0509-46cb-8730-1c42cef47376', 1, '2026-04-22 13:58:56', '2026-04-22 13:56:31', 0);
INSERT INTO `message_private` VALUES (10, '4_5', 5, 4, '你好', 1, '59fe85db-6845-4713-afbd-d784ea80e7d2', 1, '2026-04-22 13:58:56', '2026-04-22 13:57:19', 0);
INSERT INTO `message_private` VALUES (11, '4_5', 5, 4, '你好', 1, 'bc396c5d-5ebb-42b7-a6fa-709d36bede0b', 1, '2026-04-22 13:58:56', '2026-04-22 13:57:40', 0);
INSERT INTO `message_private` VALUES (12, '4_5', 5, 4, '实时通信测试', 1, 'bba6abf1-2e49-47dc-b7fb-f55e1453eee9', 1, '2026-04-22 13:58:56', '2026-04-22 13:58:38', 0);
INSERT INTO `message_private` VALUES (13, '4_5', 5, 4, '通信二次测试', 1, '64d9c93e-9496-441c-899a-5618284bec48', 1, '2026-04-22 14:00:26', '2026-04-22 14:00:19', 0);
INSERT INTO `message_private` VALUES (14, '4_5', 5, 4, '通信三次', 1, 'e20e28a2-2359-4973-9eb7-13696bc00764', 1, '2026-04-22 14:00:54', '2026-04-22 14:00:43', 0);
INSERT INTO `message_private` VALUES (15, '4_5', 4, 5, '通信测试 星倩', 1, 'c3bd88e9-d3f5-4813-8f6c-0acc8c7a0b64', 1, '2026-04-22 14:05:23', '2026-04-22 14:05:16', 0);
INSERT INTO `message_private` VALUES (16, '4_5', 4, 5, '通信测试二次 星倩', 1, '32dfbd12-5436-4a5a-8775-2cedaa046037', 1, '2026-04-22 14:06:07', '2026-04-22 14:06:02', 0);
INSERT INTO `message_private` VALUES (17, '4_5', 4, 5, '真机测试', 1, '3796ea67-be03-4192-9188-29b8a4656beb', 1, '2026-04-22 14:23:27', '2026-04-22 14:23:22', 0);
INSERT INTO `message_private` VALUES (18, '4_5', 5, 4, '会话', 1, '41240a33-e116-477c-a94c-77a0d44ce04e', 1, '2026-04-22 14:38:25', '2026-04-22 14:38:16', 0);
INSERT INTO `message_private` VALUES (19, '4_5', 4, 5, '回话', 1, 'eb70cae5-8cf1-4899-ac6f-6d3512ef4a49', 1, '2026-04-22 14:38:42', '2026-04-22 14:38:25', 0);
INSERT INTO `message_private` VALUES (20, '4_5', 4, 5, '回话', 1, '39a2fa07-8482-4ec2-bd9c-ca04dd88997d', 1, '2026-04-22 14:38:42', '2026-04-22 14:38:30', 0);
INSERT INTO `message_private` VALUES (21, '4_5', 5, 4, '回话', 1, '4ccb00d7-393a-4878-b117-50cedccaa700', 1, '2026-04-22 14:39:20', '2026-04-22 14:38:42', 0);
INSERT INTO `message_private` VALUES (22, '4_5', 4, 5, '发起回话查询', 1, 'd9695eae-9447-4ea6-97d2-8a16c9ed966c', 1, '2026-04-22 14:39:29', '2026-04-22 14:39:20', 0);
INSERT INTO `message_private` VALUES (23, '4_5', 5, 4, '发起回话查询', 1, '55bc9fff-b483-472d-a7a2-b3240ef321e8', 1, '2026-04-22 14:45:34', '2026-04-22 14:39:29', 0);
INSERT INTO `message_private` VALUES (24, '4_5', 4, 5, '错误', 1, '15fc228e-5e49-4403-a061-b12ef4d29b77', 1, '2026-04-22 14:56:15', '2026-04-22 14:45:43', 0);
INSERT INTO `message_private` VALUES (25, '4_5', 4, 5, '本地留存测试', 1, 'wx-1776840885725-p8tqoqr8', 1, '2026-04-22 14:56:15', '2026-04-22 14:54:47', 0);
INSERT INTO `message_private` VALUES (26, '4_5', 5, 4, '本地留存测试', 1, 'wx-1776840993534-1lbfzxvk', 1, '2026-04-22 14:56:39', '2026-04-22 14:56:35', 0);
INSERT INTO `message_private` VALUES (27, '4_5', 5, 4, '本地留存测试二', 1, 'wx-1776841047920-kte7zvq4', 1, '2026-04-22 14:57:32', '2026-04-22 14:57:29', 0);
INSERT INTO `message_private` VALUES (28, '4_5', 4, 5, '本地留存测试二', 1, 'wx-1776841071641-cnzuo7is', 1, '2026-04-22 14:58:02', '2026-04-22 14:57:53', 0);
INSERT INTO `message_private` VALUES (29, '4_5', 4, 5, '真测实时', 1, 'wx-1776841438068-i5g7gse9', 1, '2026-04-22 15:04:03', '2026-04-22 15:03:58', 0);
INSERT INTO `message_private` VALUES (30, '4_5', 5, 4, '实时真测', 1, 'wx-1776841469593-hnm56od7', 1, '2026-04-22 15:04:35', '2026-04-22 15:04:30', 0);
INSERT INTO `message_private` VALUES (31, '4_5', 4, 5, '检验', 1, 'wx-1776841487723-8vyzrc6g', 1, '2026-04-22 15:04:53', '2026-04-22 15:04:48', 0);
INSERT INTO `message_private` VALUES (32, '4_5', 5, 4, '同等检验', 1, 'wx-1776841581749-wmo36gb9', 1, '2026-04-22 15:06:46', '2026-04-22 15:06:23', 0);
INSERT INTO `message_private` VALUES (33, '4_5', 4, 5, '真机端', 1, 'wx-1776841879233-q72vljgs', 1, '2026-04-22 15:11:35', '2026-04-22 15:11:20', 0);
INSERT INTO `message_private` VALUES (34, '4_5', 5, 4, '开发端', 1, 'wx-1776841903355-7sqei0l0', 1, '2026-04-22 15:11:46', '2026-04-22 15:11:44', 0);
INSERT INTO `message_private` VALUES (35, '4_5', 4, 5, '本地测试', 1, 'wx-1776842863200-aiuo1da0', 1, '2026-04-22 15:28:04', '2026-04-22 15:27:44', 0);
INSERT INTO `message_private` VALUES (36, '4_5', 5, 4, '本地测试', 1, 'wx-1776842882796-etm9b816', 1, '2026-04-22 15:28:55', '2026-04-22 15:28:04', 0);
INSERT INTO `message_private` VALUES (37, '4_5', 4, 5, '会', 1, 'wx-1776842934015-yc3j0cu8', 1, '2026-04-22 15:29:13', '2026-04-22 15:28:55', 0);
INSERT INTO `message_private` VALUES (38, '4_5', 5, 4, '会', 1, 'wx-1776842952043-mlaegnis', 1, '2026-04-22 15:29:54', '2026-04-22 15:29:13', 0);
INSERT INTO `message_private` VALUES (39, '4_5', 5, 4, '灰', 1, 'wx-1776843012990-y2gbvw2r', 1, '2026-04-22 15:30:16', '2026-04-22 15:30:14', 0);
INSERT INTO `message_private` VALUES (40, '4_5', 5, 4, '你好', 1, 'wx-1776843621629-shyr9eyh', 1, '2026-04-22 15:40:31', '2026-04-22 15:40:23', 0);
INSERT INTO `message_private` VALUES (41, '4_5', 5, 4, '你好', 1, 'wx-1776843653470-w957ngdt', 1, '2026-04-22 15:41:13', '2026-04-22 15:40:55', 0);
INSERT INTO `message_private` VALUES (42, '4_5', 4, 5, '你好', 1, 'wx-1776843672102-zbdfmt5k', 1, '2026-04-22 15:41:25', '2026-04-22 15:41:13', 0);
INSERT INTO `message_private` VALUES (43, '4_5', 5, 4, '你好', 1, 'wx-1776843683474-uhcgm596', 1, '2026-04-22 15:42:00', '2026-04-22 15:41:25', 0);
INSERT INTO `message_private` VALUES (44, '4_5', 5, 4, '你好', 1, 'wx-1776843736613-p3lgxaos', 1, '2026-04-22 15:43:18', '2026-04-22 15:42:18', 0);
INSERT INTO `message_private` VALUES (45, '4_5', 5, 4, '你好', 1, 'wx-1776843742238-i93d2qky', 1, '2026-04-22 15:43:18', '2026-04-22 15:42:23', 0);
INSERT INTO `message_private` VALUES (46, '4_5', 5, 4, '你好', 1, 'wx-1776843788548-f5dwaprl', 1, '2026-04-22 15:43:18', '2026-04-22 15:43:10', 0);
INSERT INTO `message_private` VALUES (47, '4_5', 4, 5, '你好', 1, 'wx-1776843796633-1a3obmil', 1, '2026-04-22 15:43:49', '2026-04-22 15:43:18', 0);
INSERT INTO `message_private` VALUES (48, '4_5', 4, 5, '你好', 1, 'wx-1776843822512-8bt4itti', 1, '2026-04-22 15:43:49', '2026-04-22 15:43:44', 0);
INSERT INTO `message_private` VALUES (49, '4_5', 5, 4, '你好', 1, 'wx-1776843828183-nlwne32i', 1, '2026-04-22 15:44:34', '2026-04-22 15:43:49', 0);
INSERT INTO `message_private` VALUES (50, '4_5', 4, 5, '？？', 1, 'wx-1776844038778-trxo4sqe', 1, '2026-04-22 15:47:24', '2026-04-22 15:47:20', 0);
INSERT INTO `message_private` VALUES (51, '4_5', 5, 4, '？？', 1, 'wx-1776844042527-h4zoune2', 1, '2026-04-22 15:54:50', '2026-04-22 15:47:24', 0);
INSERT INTO `message_private` VALUES (52, '4_5', 5, 4, '刘焉', 1, 'wx-1776844804141-7i786bni', 1, '2026-04-22 16:00:17', '2026-04-22 16:00:05', 0);
INSERT INTO `message_private` VALUES (53, '3_4', 3, 4, '你好', 1, 'wx-1776846568893-c07oxk6x', 0, NULL, '2026-04-22 16:29:30', 0);
INSERT INTO `message_private` VALUES (54, '3_5', 5, 3, '你好', 1, 'wx-1776846649544-2glb3lp2', 1, '2026-04-22 16:31:42', '2026-04-22 16:30:51', 0);
INSERT INTO `message_private` VALUES (55, '3_5', 3, 5, '你好', 1, 'wx-1776846722765-c7so0xzw', 1, '2026-04-22 16:32:07', '2026-04-22 16:32:04', 0);
INSERT INTO `message_private` VALUES (56, '2_5', 2, 5, '你好', 1, 'wx-1776846780153-1xmvxk8d', 1, '2026-04-22 16:33:19', '2026-04-22 16:33:01', 0);
INSERT INTO `message_private` VALUES (57, '3_4', 4, 3, '你好', 1, 'wx-1776846925153-9csdam31', 0, NULL, '2026-04-22 16:35:26', 0);

-- ----------------------------
-- Table structure for message_read_receipt
-- ----------------------------
DROP TABLE IF EXISTS `message_read_receipt`;
CREATE TABLE `message_read_receipt`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `message_id` bigint NOT NULL COMMENT '消息ID',
  `user_id` bigint NOT NULL COMMENT '回执用户ID',
  `receipt_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '回执类型: DELIVERED|READ',
  `client_message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '客户端消息ID',
  `receipt_time` datetime NOT NULL COMMENT '回执时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_message_user_type`(`message_id` ASC, `user_id` ASC, `receipt_type` ASC) USING BTREE,
  INDEX `idx_user_receipt_time`(`user_id` ASC, `receipt_time` ASC) USING BTREE,
  INDEX `idx_message_id`(`message_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私信回执表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of message_read_receipt
-- ----------------------------

-- ----------------------------
-- Table structure for search_sync_task
-- ----------------------------
DROP TABLE IF EXISTS `search_sync_task`;
CREATE TABLE `search_sync_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `index_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `entity_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `document_id` bigint NOT NULL,
  `operation_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'UPSERT/DELETE',
  `payload_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0=pending,1=success,2=dead,3=processing',
  `retry_count` int NOT NULL DEFAULT 0,
  `max_retry` int NOT NULL DEFAULT 6,
  `last_error` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `next_retry_time` datetime NOT NULL,
  `last_tried_time` datetime NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status_next_retry`(`status` ASC, `next_retry_time` ASC) USING BTREE,
  INDEX `idx_entity`(`entity_type` ASC, `document_id` ASC) USING BTREE,
  INDEX `idx_created`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of search_sync_task
-- ----------------------------

-- ----------------------------
-- Table structure for service_activity
-- ----------------------------
DROP TABLE IF EXISTS `service_activity`;
CREATE TABLE `service_activity`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `user_id` bigint NOT NULL COMMENT '发布者ID',
  `post_id` bigint NULL DEFAULT NULL COMMENT '关联帖子ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '活动描述',
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '活动封面图',
  `images` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '活动图片(JSON数组)',
  `activity_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '活动类型',
  `start_time` datetime NULL DEFAULT NULL COMMENT '活动开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '活动结束时间',
  `signup_start_time` datetime NULL DEFAULT NULL COMMENT '报名开始时间',
  `signup_end_time` datetime NULL DEFAULT NULL COMMENT '报名截止时间',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '活动地点',
  `max_participants` int NULL DEFAULT NULL COMMENT '最大参与人数',
  `current_participants` int NULL DEFAULT 0 COMMENT '当前报名人数',
  `organizer` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '主办方',
  `contact_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `fee` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '报名费用',
  `requirements` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '报名要求',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览量',
  `audit_status` tinyint NULL DEFAULT 1 COMMENT '审核状态: 0-待审核 1-已通过 2-已拒绝',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-已取消 1-报名中 2-进行中 3-已结束',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '校园活动表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of service_activity
-- ----------------------------
INSERT INTO `service_activity` VALUES (1, 4, 16, '支援活动', '打扫街道', NULL, NULL, NULL, '2026-04-16 19:34:00', '2026-04-17 00:00:00', NULL, NULL, '重庆市涪陵区兴华中路 中共重庆市涪陵区自来水有限公司委员会(兴华中路北)', 10, 1, NULL, NULL, NULL, NULL, NULL, 5, 1, 1, '2026-04-16 19:41:01', '2026-04-27 18:24:35', 0);

-- ----------------------------
-- Table structure for service_activity_signup
-- ----------------------------
DROP TABLE IF EXISTS `service_activity_signup`;
CREATE TABLE `service_activity_signup`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `user_id` bigint NOT NULL COMMENT '报名用户ID',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注信息',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-已取消 1-已报名 2-已签到',
  `signup_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  `checkin_time` datetime NULL DEFAULT NULL COMMENT '签到时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_activity_user`(`activity_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_activity_id`(`activity_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '活动报名表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of service_activity_signup
-- ----------------------------
INSERT INTO `service_activity_signup` VALUES (1, 1, 5, NULL, NULL, NULL, 1, '2026-04-17 18:48:55', NULL);

-- ----------------------------
-- Table structure for service_help_candidate
-- ----------------------------
DROP TABLE IF EXISTS `service_help_candidate`;
CREATE TABLE `service_help_candidate`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `help_id` bigint NOT NULL COMMENT '互助单ID',
  `user_id` bigint NOT NULL COMMENT '接单人ID',
  `credit_score` int NOT NULL COMMENT '当时信誉积分',
  `is_selected` tinyint NULL DEFAULT 0 COMMENT '是否被选中',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '抢单时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_help_user`(`help_id` ASC, `user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '互助单抢单候选人' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of service_help_candidate
-- ----------------------------
INSERT INTO `service_help_candidate` VALUES (1, 7, 4, 100, 1, '2026-04-17 18:55:01');
INSERT INTO `service_help_candidate` VALUES (2, 8, 4, 100, 1, '2026-04-17 19:20:24');

-- ----------------------------
-- Table structure for service_help_request
-- ----------------------------
DROP TABLE IF EXISTS `service_help_request`;
CREATE TABLE `service_help_request`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '发布者ID',
  `helper_id` bigint NULL DEFAULT NULL COMMENT '接单者ID',
  `post_id` bigint NULL DEFAULT NULL COMMENT '关联帖子ID',
  `type` tinyint NOT NULL COMMENT '类型: 1-快递代取 2-跑腿帮忙',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '详细描述',
  `images` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片(JSON数组)',
  `express_company` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '快递公司',
  `express_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '取件码',
  `express_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '快递站点',
  `pickup_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '取货/出发地点',
  `delivery_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '送达地点',
  `expected_time` datetime NULL DEFAULT NULL COMMENT '期望完成时间',
  `reward` decimal(10, 2) NOT NULL COMMENT '酬劳金额',
  `tips` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注/小费说明',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览量',
  `audit_status` tinyint NULL DEFAULT 1 COMMENT '审核状态: 0-待审核 1-已通过 2-已拒绝',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-已取消 1-待接单 2-进行中 3-已完成 4-已评价',
  `complete_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `rating` tinyint NULL DEFAULT NULL COMMENT '评分(1-5)',
  `rating_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '评价内容',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  `audit_pass_time` datetime NULL DEFAULT NULL COMMENT '审核通过时间，用于3分钟抢单池起点',
  `is_frozen` tinyint NULL DEFAULT 0 COMMENT '佣金是否被冻结（0否，1是）',
  `freeze_time` datetime NULL DEFAULT NULL COMMENT '冻结开始时间，三天后默认解冻',
  `complaint_status` tinyint NULL DEFAULT 0 COMMENT '投诉状态: 0无, 1平台仲裁中, 2退回发单人, 3驳回发给接单人',
  `lock_deadline` datetime NULL DEFAULT NULL COMMENT '锁定截止时间(到点后自动进入进行中)',
  `publisher_confirmed` tinyint NULL DEFAULT 0 COMMENT '发布者是否确认完成: 0否 1是',
  `helper_confirmed` tinyint NULL DEFAULT 0 COMMENT '接单人是否确认完成: 0否 1是',
  `publisher_confirm_time` datetime NULL DEFAULT NULL COMMENT '发布者确认完成时间',
  `helper_confirm_time` datetime NULL DEFAULT NULL COMMENT '接单人确认完成时间',
  `fund_status` tinyint NULL DEFAULT 1 COMMENT '资金状态: 1-已冻结 2-已退款 3-已放款',
  `fund_freeze_time` datetime NULL DEFAULT NULL COMMENT '冻结时间',
  `fund_refund_time` datetime NULL DEFAULT NULL COMMENT '退款时间',
  `fund_settle_time` datetime NULL DEFAULT NULL COMMENT '放款时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_helper_id`(`helper_id` ASC) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '互助请求表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of service_help_request
-- ----------------------------
INSERT INTO `service_help_request` VALUES (1, 4, NULL, NULL, 1, '代取', '快递代取', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 20.00, NULL, NULL, 3, 1, 4, NULL, NULL, NULL, '2026-04-16 19:52:51', '2026-04-17 19:18:40', 0, NULL, 0, NULL, 0, NULL, 0, 0, NULL, NULL, 1, NULL, NULL, NULL);
INSERT INTO `service_help_request` VALUES (2, 4, 4, NULL, 1, '代买', '小吃街带份10元炒饭', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 20.00, NULL, NULL, 23, 1, 4, NULL, NULL, NULL, '2026-04-16 20:03:41', '2026-04-17 19:18:35', 0, NULL, 0, NULL, 0, NULL, 0, 0, NULL, NULL, 1, NULL, NULL, NULL);
INSERT INTO `service_help_request` VALUES (3, 5, NULL, NULL, 1, '测似', '测试', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 20.00, NULL, NULL, 18, 1, 4, NULL, NULL, NULL, '2026-04-16 20:25:35', '2026-04-17 18:53:01', 0, NULL, 0, NULL, 0, '2026-04-17 18:55:22', 0, 0, NULL, NULL, 1, NULL, NULL, NULL);
INSERT INTO `service_help_request` VALUES (4, 4, 5, NULL, 1, '大大', '大 大大大', NULL, NULL, NULL, NULL, '重庆市涪陵区兴华中路 中共重庆市涪陵区自来水有限公司委员会(兴华中路北)', NULL, '2026-04-17 15:00:00', 10.00, NULL, NULL, 17, 1, 3, '2026-04-17 16:55:49', NULL, NULL, '2026-04-17 14:44:13', '2026-04-17 19:48:11', 0, NULL, 0, NULL, 0, NULL, 0, 0, NULL, NULL, 1, NULL, NULL, NULL);
INSERT INTO `service_help_request` VALUES (5, 4, 5, NULL, 1, '互助闭环测试', '测试', NULL, NULL, NULL, NULL, '重庆市涪陵区兴华中路 中共重庆市涪陵区自来水有限公司委员会(兴华中路北)', NULL, '2026-04-17 18:00:00', 10.00, NULL, NULL, 7, 1, 3, '2026-04-17 17:00:49', NULL, NULL, '2026-04-17 16:59:16', '2026-04-17 17:00:49', 0, NULL, 0, NULL, 0, NULL, 0, 0, NULL, NULL, 1, NULL, NULL, NULL);
INSERT INTO `service_help_request` VALUES (6, 5, NULL, NULL, 1, '闭环测试', '闭环测试', NULL, NULL, NULL, NULL, '重庆市重庆市', NULL, '2026-04-17 18:00:00', 20.00, NULL, NULL, 1, 1, 4, NULL, NULL, NULL, '2026-04-17 18:49:56', '2026-04-17 18:50:21', 0, NULL, 0, NULL, 0, NULL, 0, 0, NULL, NULL, 1, NULL, NULL, NULL);
INSERT INTO `service_help_request` VALUES (7, 5, 4, NULL, 1, '测试问题', '微软', NULL, NULL, NULL, NULL, '重庆市涪陵区荔枝街道兴华中路66号 堡子城公园', NULL, '2026-04-17 21:00:00', 10.00, NULL, NULL, 12, 1, 3, '2026-04-17 19:01:09', NULL, NULL, '2026-04-17 18:53:33', '2026-04-17 19:18:52', 0, NULL, 0, NULL, 0, NULL, 1, 1, '2026-04-17 19:00:22', '2026-04-17 19:01:09', 1, NULL, NULL, NULL);
INSERT INTO `service_help_request` VALUES (8, 5, 4, NULL, 1, '第二次测试', '测试', NULL, NULL, NULL, NULL, '重庆市涪陵区兴华中路 中共重庆市涪陵区自来水有限公司委员会(兴华中路北)', NULL, '2026-04-17 21:00:00', 21.00, NULL, NULL, 15, 1, 3, '2026-04-17 19:47:08', NULL, NULL, '2026-04-17 19:20:07', '2026-04-20 13:09:28', 0, NULL, 0, NULL, 0, NULL, 1, 1, '2026-04-17 19:47:08', '2026-04-17 19:23:38', 3, NULL, NULL, '2026-04-17 19:47:08');

-- ----------------------------
-- Table structure for service_lost_found
-- ----------------------------
DROP TABLE IF EXISTS `service_lost_found`;
CREATE TABLE `service_lost_found`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '发布者ID',
  `post_id` bigint NULL DEFAULT NULL COMMENT '关联帖子ID',
  `type` tinyint NOT NULL COMMENT '类型: 1-寻物 2-招领',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '详细描述',
  `images` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片(JSON数组)',
  `item_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '物品名称',
  `item_category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '物品类别',
  `lost_time` datetime NULL DEFAULT NULL COMMENT '丢失/拾取时间',
  `lost_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '丢失/拾取地点',
  `contact_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `contact_wechat` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系微信',
  `reward` decimal(10, 2) NULL DEFAULT NULL COMMENT '悬赏金额(寻物时)',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览量',
  `audit_status` tinyint NULL DEFAULT 1 COMMENT '审核状态: 0-待审核 1-已通过 2-已拒绝',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-已关闭 1-进行中 2-已完成',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '失物招领表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of service_lost_found
-- ----------------------------
INSERT INTO `service_lost_found` VALUES (1, 5, 9, 1, '雨伞', '雨伞', '[\"a54ca1ec-e99d-4ad1-8379-863f46369e91.webp\"]', '雨伞', '雨伞', '2026-04-13 15:41:00', '中共重庆市涪陵区自来水有限公司委员会(兴华中路北)', '', '15223125124', '', NULL, 5, 1, 1, '2026-04-13 15:44:00', '2026-04-15 17:35:45', 0);
INSERT INTO `service_lost_found` VALUES (2, 4, 14, 2, '钱包', '黑色钱包', '[\"c4e54ef6-abe5-485d-afdd-4be612ef6c91.jpg\"]', '证件', '证件', '2026-04-15 17:34:00', '李氏海鲜大排档烧烤(黎明路店)', '', '15223730874', '', NULL, 8, 1, 2, '2026-04-15 17:35:16', '2026-04-16 14:10:07', 0);
INSERT INTO `service_lost_found` VALUES (3, 4, 15, 2, '校园卡（测试）', '蓝绿色', '[\"f51ea92d-6553-4b5b-9d42-65728ccec706.png\"]', '证件', '证件', '2026-04-15 19:00:00', '仙蹄记', '', '15223458965', '', NULL, 8, 1, 1, '2026-04-15 19:01:34', '2026-04-16 20:24:26', 0);
INSERT INTO `service_lost_found` VALUES (4, 5, 25, 2, '算法导论', '黑色书籍 第三版', '[\"9bc7dfee-2eae-4b1f-a163-b031f9a9ad91.png\"]', '书籍', '书籍', '2026-04-27 18:39:00', '涪陵区重庆市涪陵广播电视大学东(兴华中路北)', '', '10086', '', NULL, 2, 1, 1, '2026-04-27 18:40:35', '2026-04-27 18:45:04', 0);

-- ----------------------------
-- Table structure for service_lost_found_claim
-- ----------------------------
DROP TABLE IF EXISTS `service_lost_found_claim`;
CREATE TABLE `service_lost_found_claim`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `lost_found_id` bigint NOT NULL COMMENT '招领信息ID',
  `user_id` bigint NOT NULL COMMENT '申请人ID',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '认领理由及证据证明',
  `images` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '证据图片(JSON数组)',
  `status` tinyint NULL DEFAULT 0 COMMENT '审核状态: 0-待审核 1-通过 2-拒绝',
  `audit_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核备注',
  `auditor_id` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_lost_found_id`(`lost_found_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '失物认领申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of service_lost_found_claim
-- ----------------------------
INSERT INTO `service_lost_found_claim` VALUES (1, 2, 5, '就是我的', '[\"64b4502c-f38a-4b82-b6d6-34c7fc1a4e55.jpg\"]', 1, '确认', 1, '2026-04-15 18:30:24', '2026-04-15 17:42:12', '2026-04-15 18:30:24');
INSERT INTO `service_lost_found_claim` VALUES (2, 3, 5, '这上面有我的照片（测试）', '[\"225cfa2a-ee05-402e-b094-c72a56ded180.png\"]', 2, '不符合\n', 1, '2026-04-15 19:05:15', '2026-04-15 19:02:56', '2026-04-15 19:05:15');
INSERT INTO `service_lost_found_claim` VALUES (3, 3, 5, '测试 就是我本人的额', '[\"667f8d83-4cc1-46dd-a943-52fb1cd84445.jpg\"]', 2, '', 1, '2026-04-15 19:32:31', '2026-04-15 19:32:13', '2026-04-15 19:32:31');
INSERT INTO `service_lost_found_claim` VALUES (4, 4, 4, '我在涪陵广播电视台丢的 我有这本书的购买记录 以及这本书上面有我的名字', '[\"383300be-e1ab-4a19-9ac5-fa6da6048641.jpg\"]', 0, NULL, NULL, NULL, '2026-04-27 18:45:04', '2026-04-27 18:45:04');

-- ----------------------------
-- Table structure for service_product
-- ----------------------------
DROP TABLE IF EXISTS `service_product`;
CREATE TABLE `service_product`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `user_id` bigint NOT NULL COMMENT '发布者ID',
  `post_id` bigint NULL DEFAULT NULL COMMENT '关联帖子ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '商品描述',
  `images` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商品图片(JSON数组)',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类ID',
  `original_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '原价',
  `price` decimal(10, 2) NOT NULL COMMENT '售价',
  `is_negotiable` tinyint NULL DEFAULT 0 COMMENT '是否可议价: 0-否 1-是',
  `trade_type` tinyint NULL DEFAULT 1 COMMENT '交易方式: 1-面交 2-邮寄 3-均可',
  `trade_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '交易地点',
  `product_condition` tinyint NULL DEFAULT 1 COMMENT '成色: 1-全新 2-几乎全新 3-轻微使用 4-正常使用 5-明显使用',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览量',
  `want_count` int NULL DEFAULT 0 COMMENT '想要数',
  `audit_status` tinyint NULL DEFAULT 1 COMMENT '审核状态: 0-待审核 1-已通过 2-已拒绝',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-已下架 1-在售 2-已售出 3-已预定',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '二手商品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of service_product
-- ----------------------------
INSERT INTO `service_product` VALUES (1, 4, 5, '1', '1\n联系方式: 1', '[]', 1, 1.00, 1.00, 0, 1, '', 3, 17, 0, 1, 0, '2026-04-11 16:32:53', '2026-04-27 18:17:21', 1);
INSERT INTO `service_product` VALUES (2, 4, 6, '相机', '可刀', '[\"http://localhost:8080/api/uploads/618e91b2-9ecc-43df-8ee1-199d6968aa49.jpg\"]', 1, 999.00, 999.00, 0, 1, '', 3, 41, 0, 1, 1, '2026-04-12 13:19:45', '2026-04-27 18:10:22', 0);
INSERT INTO `service_product` VALUES (3, 5, 18, '相机', '黑色九九新', '[\"90658943-0748-4291-ac9b-c37aa12a8d8f.jpg\"]', 1, 9999.00, 9999.00, 0, 1, '', 3, 10, 0, 1, 3, '2026-04-17 15:41:05', '2026-04-27 18:16:54', 0);
INSERT INTO `service_product` VALUES (4, 4, 19, '二轮测试', '二轮测试', '[]', 1, 100.00, 100.00, 0, 1, '', 3, 4, 0, 1, 2, '2026-04-18 16:23:44', '2026-04-27 18:17:17', 1);
INSERT INTO `service_product` VALUES (5, 4, 20, '[求购] 考研指导书', '章鱼\n\n[期望交易时间] 2026-04-18 19:00', '[\"198dd36a-5e2d-4343-bdea-928c80fc5376.jpg\"]', 1, 100.00, 100.00, 0, 2, '', 3, 29, 0, 1, 1, '2026-04-18 18:20:24', '2026-04-20 18:38:35', 0);

-- ----------------------------
-- Table structure for service_product_category
-- ----------------------------
DROP TABLE IF EXISTS `service_product_category`;
CREATE TABLE `service_product_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父分类ID',
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类图标',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of service_product_category
-- ----------------------------
INSERT INTO `service_product_category` VALUES (1, 0, '电子数码', '📱', 1, 1, '2026-04-08 16:26:18', '2026-04-22 19:27:44');
INSERT INTO `service_product_category` VALUES (2, 0, '书籍教材', '📖', 2, 1, '2026-04-08 16:26:18', '2026-04-08 16:26:18');
INSERT INTO `service_product_category` VALUES (3, 0, '生活用品', '🏠', 3, 1, '2026-04-08 16:26:18', '2026-04-08 16:26:18');
INSERT INTO `service_product_category` VALUES (4, 0, '服饰鞋包', '👕', 4, 1, '2026-04-08 16:26:18', '2026-04-08 16:26:18');
INSERT INTO `service_product_category` VALUES (5, 0, '美妆护肤', '💄', 5, 1, '2026-04-08 16:26:18', '2026-04-08 16:26:18');
INSERT INTO `service_product_category` VALUES (6, 0, '运动户外', '⚽', 6, 1, '2026-04-08 16:26:18', '2026-04-08 16:26:18');
INSERT INTO `service_product_category` VALUES (7, 0, '乐器文具', '🎸', 7, 1, '2026-04-08 16:26:18', '2026-04-08 16:26:18');
INSERT INTO `service_product_category` VALUES (8, 0, '其他', '📦', 8, 1, '2026-04-08 16:26:18', '2026-04-08 16:26:18');

-- ----------------------------
-- Table structure for service_product_order
-- ----------------------------
DROP TABLE IF EXISTS `service_product_order`;
CREATE TABLE `service_product_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `buyer_id` bigint NOT NULL COMMENT '买家ID',
  `seller_id` bigint NOT NULL COMMENT '卖家ID',
  `amount` decimal(10, 2) NOT NULL COMMENT '成交金额',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 1-已下单 2-已取消 3-已完成',
  `cancel_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '取消原因',
  `meetup_place` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '约见地点',
  `meetup_time` datetime NULL DEFAULT NULL COMMENT '约见时间',
  `meetup_code` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '见面核销码',
  `meetup_verified` tinyint NULL DEFAULT 0 COMMENT '是否完成见面码核销: 0-否 1-是',
  `reschedule_count` tinyint NULL DEFAULT 0 COMMENT '改约次数',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间(预留)',
  `cancel_time` datetime NULL DEFAULT NULL COMMENT '取消时间',
  `finish_time` datetime NULL DEFAULT NULL COMMENT '完成时间(确认收货)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_buyer_id`(`buyer_id` ASC) USING BTREE,
  INDEX `idx_seller_id`(`seller_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '二手商品订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of service_product_order
-- ----------------------------
INSERT INTO `service_product_order` VALUES (1, 'PO202604181624219694', 4, 5, 4, 100.00, 2, '用户主动取消', NULL, NULL, '741721', 0, 0, NULL, '2026-04-18 16:26:30', NULL, '2026-04-18 16:24:21', '2026-04-18 16:26:30', 0);
INSERT INTO `service_product_order` VALUES (2, 'PO202604181626346866', 4, 5, 4, 100.00, 2, '用户主动取消', NULL, NULL, '678751', 0, 0, NULL, '2026-04-18 16:26:44', NULL, '2026-04-18 16:26:34', '2026-04-18 16:26:44', 0);
INSERT INTO `service_product_order` VALUES (3, 'PO202604181739275684', 2, 5, 4, 999.00, 2, '用户主动取消', '中共重庆市涪陵区自来水有限公司委员会(兴华中路北) (重庆市涪陵区兴华中路)', '2026-04-18 19:15:00', '278711', 0, 0, NULL, '2026-04-18 18:11:41', NULL, '2026-04-18 17:39:27', '2026-04-18 18:11:41', 0);
INSERT INTO `service_product_order` VALUES (4, 'PO202604181743379234', 4, 5, 4, 100.00, 3, NULL, NULL, NULL, '746831', 0, 0, NULL, NULL, '2026-04-18 17:45:47', '2026-04-18 17:43:37', '2026-04-18 17:45:47', 0);
INSERT INTO `service_product_order` VALUES (5, 'PO202604181811459019', 2, 5, 4, 999.00, 1, NULL, NULL, NULL, '241877', 0, 0, NULL, NULL, NULL, '2026-04-18 18:11:45', '2026-04-18 18:12:23', 0);
INSERT INTO `service_product_order` VALUES (6, 'PO202604271810502038', 3, 4, 5, 9999.00, 1, NULL, NULL, NULL, '672193', 0, 0, NULL, NULL, NULL, '2026-04-27 18:10:50', '2026-04-27 18:16:54', 0);

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '配置值',
  `config_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '配置类型',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, 'site_name', '校园服务论坛', 'basic', '网站名称', '2026-04-08 16:26:18', '2026-04-08 16:26:18');
INSERT INTO `sys_config` VALUES (2, 'site_logo', '/static/images/logo.png', 'basic', '网站Logo', '2026-04-08 16:26:18', '2026-04-08 16:26:18');
INSERT INTO `sys_config` VALUES (3, 'site_description', '基于微信小程序的校园服务论坛系统', 'basic', '网站描述', '2026-04-08 16:26:18', '2026-04-08 16:26:18');
INSERT INTO `sys_config` VALUES (4, 'post_audit_enabled', 'false', 'audit', '帖子是否需要审核', '2026-04-08 16:26:18', '2026-04-08 16:26:18');
INSERT INTO `sys_config` VALUES (5, 'comment_audit_enabled', 'false', 'audit', '评论是否需要审核', '2026-04-08 16:26:18', '2026-04-08 16:26:18');
INSERT INTO `sys_config` VALUES (6, 'product_audit_enabled', 'false', 'audit', '商品是否需要审核', '2026-04-08 16:26:18', '2026-04-08 16:26:18');
INSERT INTO `sys_config` VALUES (7, 'register_verification_enabled', 'true', 'user', '注册是否需要实名认证', '2026-04-08 16:26:18', '2026-04-08 16:26:18');
INSERT INTO `sys_config` VALUES (8, 'student_id_pattern', '^[0-9]{12}$', 'user', '学号格式正则表达式', '2026-04-08 16:26:18', '2026-04-08 16:26:18');
INSERT INTO `sys_config` VALUES (9, 'teacher_id_pattern', '^[A-Z][0-9]{6}$', 'user', '工号格式正则表达式', '2026-04-08 16:26:18', '2026-04-08 16:26:18');

-- ----------------------------
-- Table structure for sys_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '操作用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作用户名',
  `operation` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作描述',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求方法',
  `params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '请求参数',
  `result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '返回结果',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `duration` bigint NULL DEFAULT NULL COMMENT '执行时长(毫秒)',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-失败 1-成功',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_operation_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父权限ID',
  `permission_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限名称',
  `permission_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限编码',
  `permission_type` tinyint NULL DEFAULT 1 COMMENT '权限类型: 1-菜单 2-按钮 3-接口',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '路由路径',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_permission_code`(`permission_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 48 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_permission
-- ----------------------------
INSERT INTO `sys_permission` VALUES (29, 0, '系统管理', 'system', 1, '/system', 'Setting', 1, 1, '2026-04-22 19:37:47', '2026-04-22 19:37:47');
INSERT INTO `sys_permission` VALUES (30, 0, '论坛管理', 'forum', 1, '/forum', 'ChatDotRound', 2, 1, '2026-04-22 19:37:47', '2026-04-22 19:37:47');
INSERT INTO `sys_permission` VALUES (31, 0, '服务管理', 'service', 1, '/service', 'Service', 3, 1, '2026-04-22 19:37:47', '2026-04-22 19:37:47');
INSERT INTO `sys_permission` VALUES (32, 0, '信息管理', 'info', 1, '/info', 'Notification', 4, 1, '2026-04-22 19:37:47', '2026-04-22 19:37:47');
INSERT INTO `sys_permission` VALUES (33, 1, '用户管理', 'system:user', 1, '/system/user', 'User', 11, 1, '2026-04-22 19:37:47', '2026-04-22 19:37:47');
INSERT INTO `sys_permission` VALUES (34, 1, '角色权限', 'system:role', 1, '/system/role', 'UserFilled', 12, 1, '2026-04-22 19:37:47', '2026-04-22 19:37:47');
INSERT INTO `sys_permission` VALUES (35, 1, '内容审核', 'system:audit', 1, '/system/audit', 'Document', 13, 1, '2026-04-22 19:37:47', '2026-04-22 19:37:47');
INSERT INTO `sys_permission` VALUES (36, 1, '举报处理', 'system:report', 1, '/system/report', 'Warning', 14, 1, '2026-04-22 19:37:47', '2026-04-22 19:37:47');
INSERT INTO `sys_permission` VALUES (37, 1, '敏感词库', 'system:sensitive', 1, '/system/sensitive-words', 'Warning', 15, 1, '2026-04-22 19:37:47', '2026-04-22 19:37:47');
INSERT INTO `sys_permission` VALUES (38, 2, '板块管理', 'forum:section', 1, '/forum/section', 'Grid', 21, 1, '2026-04-22 19:37:47', '2026-04-22 19:37:47');
INSERT INTO `sys_permission` VALUES (39, 2, '帖子管理', 'forum:post', 1, '/forum/post', 'Document', 22, 1, '2026-04-22 19:37:47', '2026-04-22 19:37:47');
INSERT INTO `sys_permission` VALUES (40, 3, '服务分类', 'service:category', 1, '/service/product', 'ShoppingCart', 31, 1, '2026-04-22 19:37:47', '2026-04-22 19:37:47');
INSERT INTO `sys_permission` VALUES (41, 3, '二手市场管理', 'market:manage', 1, '/service/product/manage', 'ShoppingCart', 32, 1, '2026-04-22 19:37:47', '2026-04-22 19:37:47');
INSERT INTO `sys_permission` VALUES (42, 3, '失物招领管理', 'lostfound:manage', 1, '/service/lostfound', 'Search', 33, 1, '2026-04-22 19:37:47', '2026-04-22 19:37:47');
INSERT INTO `sys_permission` VALUES (43, 3, '活动管理', 'activity:manage', 1, '/service/activity', 'Calendar', 34, 1, '2026-04-22 19:37:47', '2026-04-22 19:37:47');
INSERT INTO `sys_permission` VALUES (44, 3, '互助管理', 'help:manage', 1, '/service/help', 'Service', 35, 1, '2026-04-22 19:37:47', '2026-04-22 19:37:47');
INSERT INTO `sys_permission` VALUES (45, 4, '校园资讯管理', 'info:news', 1, '/info/news', 'Notification', 41, 1, '2026-04-22 19:37:47', '2026-04-22 19:37:47');
INSERT INTO `sys_permission` VALUES (46, 4, '服务导航管理', 'info:nav', 1, '/info/nav', 'Compass', 42, 1, '2026-04-22 19:37:47', '2026-04-22 19:37:47');

-- ----------------------------
-- Table structure for sys_platform_wallet
-- ----------------------------
DROP TABLE IF EXISTS `sys_platform_wallet`;
CREATE TABLE `sys_platform_wallet`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(10, 2) NOT NULL COMMENT '平台收益/变动金额',
  `source_type` int NOT NULL COMMENT '来源类型: 1违约金',
  `relation_id` bigint NULL DEFAULT NULL COMMENT '关联单据ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '平台流水表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_platform_wallet
-- ----------------------------

-- ----------------------------
-- Table structure for sys_report
-- ----------------------------
DROP TABLE IF EXISTS `sys_report`;
CREATE TABLE `sys_report`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '举报人ID',
  `target_type` tinyint NOT NULL COMMENT '举报目标类型: 1-帖子 2-评论 3-用户 4-商品 5-活动 6-失物招领 7-互助',
  `target_id` bigint NOT NULL COMMENT '举报目标ID',
  `reason_type` tinyint NULL DEFAULT NULL COMMENT '举报原因类型',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '举报原因详情',
  `images` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '举报截图(JSON数组)',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态: 0-待处理 1-已处理 2-已忽略',
  `handle_user_id` bigint NULL DEFAULT NULL COMMENT '处理人ID',
  `handle_result` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '处理结果',
  `handle_time` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `target_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '举报内容标题快照',
  `target_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '举报内容正文快照',
  `target_audit_status` tinyint NULL DEFAULT NULL COMMENT '举报内容审核状态快照',
  `target_status` tinyint NULL DEFAULT NULL COMMENT '举报内容业务状态快照',
  `target_audit_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '举报内容审核备注快照',
  `target_create_time` datetime NULL DEFAULT NULL COMMENT '举报内容创建时间快照',
  `target_author_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '举报内容作者快照',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_user_target_pending`(`user_id` ASC, `target_type` ASC, `target_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '举报表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_report
-- ----------------------------
INSERT INTO `sys_report` VALUES (1, 4, 1, 12, 1, '垃圾', '[{\"filename\":\"fd359da6-05ff-4b74-8d71-a1e7793179f2.png\",\"url\":\"fd359da6-05ff-4b74-8d71-a1e7793179f2.png\"}]', 1, 1, '已删除', '2026-04-15 16:24:46', '2026-04-15 16:23:10', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_report` VALUES (2, 4, 1, 12, 1, '更是垃圾', NULL, 1, 1, '已下架', '2026-04-28 21:13:57', '2026-04-15 16:34:52', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_report` VALUES (3, 4, 1, 11, 1, '垃圾', NULL, 1, 1, '感谢您的举报 我们已经处理', '2026-04-15 17:00:57', '2026-04-15 17:00:04', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_report` VALUES (4, 5, 1, 24, 6, '涉及虚假宣传', NULL, 1, 1, '帖子已审查，举报已完成处理', '2026-04-28 21:10:38', '2026-04-28 20:20:52', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_report` VALUES (5, 5, 1, 7, 2, '违规', NULL, 0, NULL, NULL, NULL, '2026-04-28 21:15:04', '代取', '有没有人想帮我拿一下快递', 1, 1, '审核通过', '2026-04-12 14:28:46', '管理孤倩');
INSERT INTO `sys_report` VALUES (6, 4, 1, 11, 2, '涉及违禁词', NULL, 1, 1, '帖子已审查，举报已完成处理', '2026-04-28 21:21:20', '2026-04-28 21:20:30', '敏感词判断', 'cnm 福瑞', 1, 1, NULL, '2026-04-14 17:06:28', '用户星倩');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色描述',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`role_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'SUPER_ADMIN', '系统超级管理员，拥有所有权限', 1, 1, '2026-04-08 16:26:18', '2026-04-08 21:08:18');
INSERT INTO `sys_role` VALUES (2, '管理员', 'ADMIN', '普通管理员', 2, 1, '2026-04-08 16:26:18', '2026-04-08 16:26:18');
INSERT INTO `sys_role` VALUES (3, '版主', 'MODERATOR', '板块版主，管理特定板块', 3, 1, '2026-04-08 16:26:18', '2026-04-08 16:26:18');
INSERT INTO `sys_role` VALUES (4, '普通用户', 'USER', '普通注册用户', 4, 1, '2026-04-08 16:26:18', '2026-04-08 16:26:18');
INSERT INTO `sys_role` VALUES (5, '论坛管理版主', 'MODERATOR_FORUM', '负责论坛帖子板块的内容审核与管理', 4, 1, '2026-04-22 19:15:12', '2026-04-22 19:15:12');
INSERT INTO `sys_role` VALUES (6, '二手市场版主', 'MODERATOR_MARKET', '负责二手市场板块的内容审核与管理', 5, 1, '2026-04-22 19:15:12', '2026-04-22 19:15:12');
INSERT INTO `sys_role` VALUES (7, '失物招领版主', 'MODERATOR_LOSTFOUND', '负责失物招领板块的内容审核与管理', 6, 1, '2026-04-22 19:15:12', '2026-04-22 19:15:12');
INSERT INTO `sys_role` VALUES (8, '活动管理版主', 'MODERATOR_ACTIVITY', '负责校园活动板块的内容审核与管理', 7, 1, '2026-04-22 19:15:12', '2026-04-22 19:15:12');
INSERT INTO `sys_role` VALUES (9, '互助管理版主', 'MODERATOR_HELP', '负责互助广场板块的内容审核与管理', 8, 1, '2026-04-22 19:15:12', '2026-04-22 19:15:12');
INSERT INTO `sys_role` VALUES (10, '资讯和导航版主', 'MODERATOR_INFO', '负责校园资讯与服务导航的内容审核与管理', 9, 1, '2026-04-22 19:15:12', '2026-04-22 19:15:12');

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_permission`(`role_id` ASC, `permission_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_permission_id`(`permission_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 244 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色权限关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_permission
-- ----------------------------
INSERT INTO `sys_role_permission` VALUES (181, 1, 43, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (182, 1, 30, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (183, 1, 39, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (184, 1, 38, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (185, 1, 44, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (186, 1, 32, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (187, 1, 46, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (188, 1, 45, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (189, 1, 42, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (190, 1, 41, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (191, 1, 31, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (192, 1, 40, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (193, 1, 29, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (194, 1, 35, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (195, 1, 36, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (196, 1, 34, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (197, 1, 37, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (198, 1, 33, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (212, 2, 43, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (213, 2, 30, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (214, 2, 39, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (215, 2, 38, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (216, 2, 44, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (217, 2, 32, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (218, 2, 46, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (219, 2, 45, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (220, 2, 42, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (221, 2, 41, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (222, 2, 31, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (223, 2, 40, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (224, 2, 29, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (225, 2, 35, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (226, 2, 36, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (227, 2, 33, '2026-04-22 19:37:59');
INSERT INTO `sys_role_permission` VALUES (243, 6, 41, '2026-04-22 19:40:22');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '微信openid',
  `union_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '微信unionid',
  `student_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学号/工号',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码(管理员登录用)',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '/static/images/default-avatar.png' COMMENT '头像',
  `gender` tinyint NULL DEFAULT 0 COMMENT '性别: 0-未知 1-男 2-女',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `bio` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '个人简介',
  `college` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学院',
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专业',
  `grade` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '年级',
  `user_type` tinyint NULL DEFAULT 1 COMMENT '用户类型: 1-学生 2-教师 3-其他',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常 2-待审核',
  `is_verified` tinyint NULL DEFAULT 0 COMMENT '是否实名认证: 0-否 1-是',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  `balance` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '钱包余额',
  `credit_score` int NULL DEFAULT 100 COMMENT '信誉积分',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_openid`(`openid` ASC) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, NULL, NULL, NULL, 'admin', '$2y$10$t8qe5E9QzYrj41Y/rTX0ZONhNRH4YTwCkOXdTt6PJdThSm1OHfxye', '系统管理员', '/static/images/admin-avatar.png', 0, NULL, NULL, NULL, NULL, NULL, NULL, 3, 1, 1, NULL, NULL, '2026-04-08 16:26:18', '2026-04-08 17:10:29', 0, 0.00, 100);
INSERT INTO `sys_user` VALUES (2, 'mock_0c1yk0000MppaW1GS4300pH2ks2yk00E', NULL, '222154101226', 'wx_1775642155933', '$2y$10$t8qe5E9QzYrj41Y/rTX0ZONhNRH4YTwCkOXdTt6PJdThSm1OHfxye', '模拟用户二号', '/static/images/default-avatar.png', NULL, '', '', '', '', '', '', 1, 1, 1, NULL, NULL, '2026-04-08 17:55:55', '2026-04-22 17:44:11', 0, 0.00, 100);
INSERT INTO `sys_user` VALUES (3, 'mock_0b1lgzFa1uTtvL0paEJa1osdxC2lgzFl', NULL, '222154101227', 'wx_1775724646971', NULL, '模拟用户一号', '/static/images/default-avatar.png', NULL, '', '', '', '', '', '', 1, 1, 1, NULL, NULL, '2026-04-09 16:50:46', '2026-04-22 16:28:06', 0, 0.00, 100);
INSERT INTO `sys_user` VALUES (4, 'oSiJs19dqBEEBL9M28b2xAYGZaR0', NULL, '222154101228', 'wx_oSiJs19d', NULL, '管理孤倩', '6ab9dcd1-014b-46a0-8d04-630d0e79d3ee.png', 0, '', '', '', '', '', '', 1, 1, 1, NULL, NULL, '2026-04-09 17:33:11', '2026-04-17 19:47:08', 0, 21.00, 102);
INSERT INTO `sys_user` VALUES (5, 'oSiJs1xgLgXLkn8yIc6s6flXeFYc', NULL, '222154101229', 'wx_oSiJs1xg', NULL, '用户星倩', 'http://localhost:8081/api/uploads/f279f90f-fc4b-42de-8207-2fef88f8fe53.jpg', NULL, '10086', 'gxlqn@gmail.com', '诗一般的男子', '大数据与智能工程学院', '计算机科学与技术', '2022', 1, 1, 1, NULL, NULL, '2026-04-10 16:10:26', '2026-04-22 16:25:53', 0, 0.00, 101);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id` ASC, `role_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, 1, '2026-04-08 16:26:18');
INSERT INTO `sys_user_role` VALUES (7, 2, 6, '2026-04-22 19:24:52');

-- ----------------------------
-- Table structure for user_wallet_record
-- ----------------------------
DROP TABLE IF EXISTS `user_wallet_record`;
CREATE TABLE `user_wallet_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `amount` decimal(10, 2) NOT NULL,
  `type` int NOT NULL COMMENT '1充值 2发单扣款 3接单收入 4违约扣款 5退款 6违约补偿',
  `relation_id` bigint NULL DEFAULT NULL COMMENT '业务ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户钱包流水表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_wallet_record
-- ----------------------------
INSERT INTO `user_wallet_record` VALUES (1, 4, 21.00, 3, 8, '2026-04-17 19:47:08');

SET FOREIGN_KEY_CHECKS = 1;
