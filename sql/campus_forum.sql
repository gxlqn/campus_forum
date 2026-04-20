-- =====================================================
-- 校园服务论坛系统数据库设计
-- 数据库名称: campus_forum
-- 创建时间: 2026-01-10
-- 说明: 请在Navicat中执行此脚本
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS campus_forum DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_forum;

-- =====================================================
-- 一、系统管理模块
-- =====================================================

-- 1.1 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    openid VARCHAR(64) COMMENT '微信openid',
    union_id VARCHAR(64) COMMENT '微信unionid',
    student_id VARCHAR(20) COMMENT '学号/工号',
    username VARCHAR(50) COMMENT '用户名',
    password VARCHAR(100) COMMENT '密码(管理员登录用)',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) DEFAULT '/static/images/default-avatar.png' COMMENT '头像',
    gender TINYINT DEFAULT 0 COMMENT '性别: 0-未知 1-男 2-女',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    bio VARCHAR(500) COMMENT '个人简介',
    college VARCHAR(100) COMMENT '学院',
    major VARCHAR(100) COMMENT '专业',
    grade VARCHAR(20) COMMENT '年级',
    user_type TINYINT DEFAULT 1 COMMENT '用户类型: 1-学生 2-教师 3-其他',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常 2-待审核',
    is_verified TINYINT DEFAULT 0 COMMENT '是否实名认证: 0-否 1-是',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    INDEX idx_openid (openid),
    INDEX idx_student_id (student_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 1.2 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    description VARCHAR(255) COMMENT '角色描述',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 1.3 权限表
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID',
    permission_name VARCHAR(50) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL COMMENT '权限编码',
    permission_type TINYINT DEFAULT 1 COMMENT '权限类型: 1-菜单 2-按钮 3-接口',
    path VARCHAR(255) COMMENT '路由路径',
    icon VARCHAR(100) COMMENT '图标',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_permission_code (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 1.4 用户角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 1.5 角色权限关联表
DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- =====================================================
-- 二、论坛管理模块
-- =====================================================

-- 2.1 板块表
DROP TABLE IF EXISTS forum_section;
CREATE TABLE forum_section (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '板块ID',
    section_name VARCHAR(50) NOT NULL COMMENT '板块名称',
    section_code VARCHAR(50) NOT NULL COMMENT '板块编码',
    description VARCHAR(255) COMMENT '板块描述',
    icon VARCHAR(255) COMMENT '板块图标',
    cover_image VARCHAR(255) COMMENT '板块封面图',
    sort INT DEFAULT 0 COMMENT '排序',
    post_count INT DEFAULT 0 COMMENT '帖子数量',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认板块: 0-否 1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_section_code (section_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论坛板块表';

-- 2.2 帖子表
DROP TABLE IF EXISTS forum_post;
CREATE TABLE forum_post (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '帖子ID',
    user_id BIGINT NOT NULL COMMENT '发布者ID',
    section_id BIGINT NOT NULL COMMENT '板块ID',
    title VARCHAR(200) NOT NULL COMMENT '帖子标题',
    content TEXT COMMENT '帖子内容',
    images VARCHAR(2000) COMMENT '图片列表(JSON数组)',
    attachments VARCHAR(2000) COMMENT '附件列表(JSON数组)',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    favorite_count INT DEFAULT 0 COMMENT '收藏数',
    share_count INT DEFAULT 0 COMMENT '分享数',
    is_top TINYINT DEFAULT 0 COMMENT '是否置顶: 0-否 1-是',
    is_essence TINYINT DEFAULT 0 COMMENT '是否精华: 0-否 1-是',
    is_anonymous TINYINT DEFAULT 0 COMMENT '是否匿名: 0-否 1-是',
    audit_status TINYINT DEFAULT 1 COMMENT '审核状态: 0-待审核 1-已通过 2-已拒绝',
    audit_remark VARCHAR(255) COMMENT '审核备注',
    source_type TINYINT DEFAULT 0 COMMENT '来源类型: 0-普通帖子 1-二手商品 2-失物招领 3-活动 4-互助',
    source_id BIGINT COMMENT '来源ID',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-已关闭 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id),
    INDEX idx_section_id (section_id),
    INDEX idx_create_time (create_time),
    INDEX idx_source (source_type, source_id),
    FULLTEXT INDEX ft_title_content (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论坛帖子表';

-- 2.3 评论表
DROP TABLE IF EXISTS forum_comment;
CREATE TABLE forum_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    user_id BIGINT NOT NULL COMMENT '评论者ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父评论ID(0表示一级评论)',
    reply_user_id BIGINT COMMENT '回复的用户ID',
    content TEXT NOT NULL COMMENT '评论内容',
    images VARCHAR(1000) COMMENT '图片列表(JSON数组)',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    is_anonymous TINYINT DEFAULT 0 COMMENT '是否匿名: 0-否 1-是',
    audit_status TINYINT DEFAULT 1 COMMENT '审核状态: 0-待审核 1-已通过 2-已拒绝',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-已删除 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论坛评论表';

-- 2.4 点赞表
DROP TABLE IF EXISTS forum_like;
CREATE TABLE forum_like (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_type TINYINT NOT NULL COMMENT '目标类型: 1-帖子 2-评论',
    target_id BIGINT NOT NULL COMMENT '目标ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_target (user_id, target_type, target_id),
    INDEX idx_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞表';

-- 2.5 收藏表
DROP TABLE IF EXISTS forum_favorite;
CREATE TABLE forum_favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_type TINYINT NOT NULL COMMENT '目标类型: 1-帖子 2-商品 3-活动',
    target_id BIGINT NOT NULL COMMENT '目标ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_target (user_id, target_type, target_id),
    INDEX idx_user_id (user_id),
    INDEX idx_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- 2.6 关注表
DROP TABLE IF EXISTS forum_follow;
CREATE TABLE forum_follow (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID(关注者)',
    follow_user_id BIGINT NOT NULL COMMENT '被关注用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_follow (user_id, follow_user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_follow_user_id (follow_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关注表';

-- =====================================================
-- 三、服务管理模块
-- =====================================================

-- 3.1 二手商品表
DROP TABLE IF EXISTS service_product;
CREATE TABLE service_product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    user_id BIGINT NOT NULL COMMENT '发布者ID',
    post_id BIGINT COMMENT '关联帖子ID',
    title VARCHAR(200) NOT NULL COMMENT '商品标题',
    description TEXT COMMENT '商品描述',
    images VARCHAR(2000) COMMENT '商品图片(JSON数组)',
    category_id BIGINT COMMENT '分类ID',
    original_price DECIMAL(10,2) COMMENT '原价',
    price DECIMAL(10,2) NOT NULL COMMENT '售价',
    is_negotiable TINYINT DEFAULT 0 COMMENT '是否可议价: 0-否 1-是',
    trade_type TINYINT DEFAULT 1 COMMENT '交易方式: 1-面交 2-邮寄 3-均可',
    trade_location VARCHAR(255) COMMENT '交易地点',
    product_condition TINYINT DEFAULT 1 COMMENT '成色: 1-全新 2-几乎全新 3-轻微使用 4-正常使用 5-明显使用',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    want_count INT DEFAULT 0 COMMENT '想要数',
    audit_status TINYINT DEFAULT 1 COMMENT '审核状态: 0-待审核 1-已通过 2-已拒绝',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-已下架 1-在售 2-已售出 3-已预定',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id),
    INDEX idx_category_id (category_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='二手商品表';

-- 3.2 商品分类表
-- 3.1.1 商品订单表
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

-- 3.2 商品分类表
DROP TABLE IF EXISTS service_product_category;
CREATE TABLE service_product_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
    icon VARCHAR(255) COMMENT '分类图标',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 3.3 失物招领表
DROP TABLE IF EXISTS service_lost_found;
CREATE TABLE service_lost_found (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '发布者ID',
    post_id BIGINT COMMENT '关联帖子ID',
    type TINYINT NOT NULL COMMENT '类型: 1-寻物 2-招领',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    description TEXT COMMENT '详细描述',
    images VARCHAR(2000) COMMENT '图片(JSON数组)',
    item_name VARCHAR(100) COMMENT '物品名称',
    item_category VARCHAR(50) COMMENT '物品类别',
    lost_time DATETIME COMMENT '丢失/拾取时间',
    lost_location VARCHAR(255) COMMENT '丢失/拾取地点',
    contact_name VARCHAR(50) COMMENT '联系人姓名',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    contact_wechat VARCHAR(50) COMMENT '联系微信',
    reward DECIMAL(10,2) COMMENT '悬赏金额(寻物时)',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    audit_status TINYINT DEFAULT 1 COMMENT '审核状态: 0-待审核 1-已通过 2-已拒绝',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-已关闭 1-进行中 2-已完成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='失物招领表';

-- 3.4 校园活动表
DROP TABLE IF EXISTS service_activity;
CREATE TABLE service_activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '活动ID',
    user_id BIGINT NOT NULL COMMENT '发布者ID',
    post_id BIGINT COMMENT '关联帖子ID',
    title VARCHAR(200) NOT NULL COMMENT '活动标题',
    description TEXT COMMENT '活动描述',
    cover_image VARCHAR(255) COMMENT '活动封面图',
    images VARCHAR(2000) COMMENT '活动图片(JSON数组)',
    activity_type VARCHAR(50) COMMENT '活动类型',
    start_time DATETIME COMMENT '活动开始时间',
    end_time DATETIME COMMENT '活动结束时间',
    signup_start_time DATETIME COMMENT '报名开始时间',
    signup_end_time DATETIME COMMENT '报名截止时间',
    location VARCHAR(255) COMMENT '活动地点',
    max_participants INT COMMENT '最大参与人数',
    current_participants INT DEFAULT 0 COMMENT '当前报名人数',
    organizer VARCHAR(100) COMMENT '主办方',
    contact_name VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    fee DECIMAL(10,2) DEFAULT 0 COMMENT '报名费用',
    requirements TEXT COMMENT '报名要求',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    audit_status TINYINT DEFAULT 1 COMMENT '审核状态: 0-待审核 1-已通过 2-已拒绝',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-已取消 1-报名中 2-进行中 3-已结束',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='校园活动表';

-- 3.5 活动报名表
DROP TABLE IF EXISTS service_activity_signup;
CREATE TABLE service_activity_signup (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    user_id BIGINT NOT NULL COMMENT '报名用户ID',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '联系电话',
    remark VARCHAR(500) COMMENT '备注信息',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-已取消 1-已报名 2-已签到',
    signup_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    checkin_time DATETIME COMMENT '签到时间',
    UNIQUE KEY uk_activity_user (activity_id, user_id),
    INDEX idx_activity_id (activity_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动报名表';

-- 3.6 互助请求表(快递代取/跑腿帮忙)
DROP TABLE IF EXISTS service_help_request;
CREATE TABLE service_help_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '发布者ID',
    helper_id BIGINT COMMENT '接单者ID',
    post_id BIGINT COMMENT '关联帖子ID',
    type TINYINT NOT NULL COMMENT '类型: 1-快递代取 2-跑腿帮忙',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    description TEXT COMMENT '详细描述',
    images VARCHAR(2000) COMMENT '图片(JSON数组)',
    -- 快递代取专用字段
    express_company VARCHAR(50) COMMENT '快递公司',
    express_code VARCHAR(50) COMMENT '取件码',
    express_location VARCHAR(255) COMMENT '快递站点',
    -- 通用字段
    pickup_location VARCHAR(255) COMMENT '取货/出发地点',
    delivery_location VARCHAR(255) COMMENT '送达地点',
    expected_time DATETIME COMMENT '期望完成时间',
    reward DECIMAL(10,2) NOT NULL COMMENT '酬劳金额',
    fund_status TINYINT DEFAULT 1 COMMENT '资金状态: 1-已冻结 2-已退款 3-已放款',
    fund_freeze_time DATETIME COMMENT '冻结时间',
    fund_refund_time DATETIME COMMENT '退款时间',
    fund_settle_time DATETIME COMMENT '放款时间',
    tips VARCHAR(500) COMMENT '备注/小费说明',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    audit_status TINYINT DEFAULT 1 COMMENT '审核状态: 0-待审核 1-已通过 2-已拒绝',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-已取消 1-待接单 2-进行中 3-已完成 4-已评价 6-锁定等待时段结束',
    lock_deadline DATETIME COMMENT '锁定截止时间(到点后自动进入进行中)',
    publisher_confirmed TINYINT DEFAULT 0 COMMENT '发布者是否确认完成: 0否 1是',
    helper_confirmed TINYINT DEFAULT 0 COMMENT '接单人是否确认完成: 0否 1是',
    publisher_confirm_time DATETIME COMMENT '发布者确认完成时间',
    helper_confirm_time DATETIME COMMENT '接单人确认完成时间',
    complete_time DATETIME COMMENT '完成时间',
    rating TINYINT COMMENT '评分(1-5)',
    rating_content VARCHAR(500) COMMENT '评价内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id),
    INDEX idx_helper_id (helper_id),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='互助请求表';

DROP TABLE IF EXISTS service_help_candidate;
CREATE TABLE service_help_candidate (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    help_id BIGINT NOT NULL COMMENT '互助单ID',
    user_id BIGINT NOT NULL COMMENT '接单人ID',
    credit_score INT NOT NULL COMMENT '报名时信誉分',
    is_selected TINYINT DEFAULT 0 COMMENT '是否被分配',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    UNIQUE KEY uk_help_user (help_id, user_id),
    INDEX idx_help_id (help_id),
    INDEX idx_score_time (credit_score, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='互助抢单候选池';

-- =====================================================
-- 四、消息通知模块
-- =====================================================

-- 4.1 系统通知表
DROP TABLE IF EXISTS message_notification;
CREATE TABLE message_notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '接收用户ID(0表示全体用户)',
    sender_id BIGINT COMMENT '发送者ID(系统通知为空)',
    type TINYINT NOT NULL COMMENT '通知类型: 1-系统公告 2-帖子回复 3-评论回复 4-点赞 5-关注 6-交易通知 7-活动通知 8-互助通知',
    title VARCHAR(200) COMMENT '通知标题',
    content VARCHAR(500) COMMENT '通知内容',
    target_type TINYINT COMMENT '目标类型: 1-帖子 2-评论 3-商品 4-活动 5-互助',
    target_id BIGINT COMMENT '目标ID',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读: 0-未读 1-已读',
    read_time DATETIME COMMENT '阅读时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知表';

-- 4.2 私信消息表
DROP TABLE IF EXISTS message_private;
CREATE TABLE message_private (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    conversation_id VARCHAR(64) NOT NULL COMMENT '会话ID',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    receiver_id BIGINT NOT NULL COMMENT '接收者ID',
    content TEXT NOT NULL COMMENT '消息内容',
    content_type TINYINT DEFAULT 1 COMMENT '内容类型: 1-文本 2-图片 3-商品卡片',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读: 0-未读 1-已读',
    read_time DATETIME COMMENT '阅读时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_receiver_id (receiver_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私信消息表';

-- 4.3 会话表
DROP TABLE IF EXISTS message_conversation;
CREATE TABLE message_conversation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    conversation_id VARCHAR(64) NOT NULL COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_user_id BIGINT NOT NULL COMMENT '对方用户ID',
    last_message_id BIGINT COMMENT '最后一条消息ID',
    last_message_content VARCHAR(200) COMMENT '最后一条消息内容预览',
    last_message_time DATETIME COMMENT '最后消息时间',
    unread_count INT DEFAULT 0 COMMENT '未读消息数',
    is_top TINYINT DEFAULT 0 COMMENT '是否置顶: 0-否 1-是',
    is_muted TINYINT DEFAULT 0 COMMENT '是否免打扰: 0-否 1-是',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-已删除 1-正常',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_conversation_user (conversation_id, user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_update_time (update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';

-- =====================================================
-- 五、信息聚合模块
-- =====================================================

-- 5.1 校园资讯表
DROP TABLE IF EXISTS info_news;
CREATE TABLE info_news (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    title VARCHAR(200) NOT NULL COMMENT '资讯标题',
    summary VARCHAR(500) COMMENT '摘要',
    content TEXT COMMENT '内容',
    cover_image VARCHAR(255) COMMENT '封面图',
    source VARCHAR(100) COMMENT '来源',
    source_url VARCHAR(500) COMMENT '原文链接',
    category VARCHAR(50) COMMENT '分类',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    is_top TINYINT DEFAULT 0 COMMENT '是否置顶',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-下架 1-正常',
    publish_time DATETIME COMMENT '发布时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_category (category),
    INDEX idx_publish_time (publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='校园资讯表';

-- 5.2 校内服务导航表
DROP TABLE IF EXISTS info_service_nav;
CREATE TABLE info_service_nav (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    category VARCHAR(50) NOT NULL COMMENT '分类',
    name VARCHAR(100) NOT NULL COMMENT '服务名称',
    description VARCHAR(255) COMMENT '描述',
    icon VARCHAR(255) COMMENT '图标',
    url VARCHAR(500) COMMENT '链接地址',
    phone VARCHAR(50) COMMENT '联系电话',
    address VARCHAR(255) COMMENT '地址',
    longitude DECIMAL(10,6) COMMENT '经度',
    latitude DECIMAL(10,6) COMMENT '纬度',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='校内服务导航表';

-- =====================================================
-- 六、系统配置模块
-- =====================================================

-- 6.1 系统配置表
DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type VARCHAR(50) COMMENT '配置类型',
    description VARCHAR(255) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 6.2 举报表
DROP TABLE IF EXISTS sys_report;
CREATE TABLE sys_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '举报人ID',
    target_type TINYINT NOT NULL COMMENT '举报目标类型: 1-帖子 2-评论 3-用户 4-商品 5-活动',
    target_id BIGINT NOT NULL COMMENT '举报目标ID',
    reason_type TINYINT COMMENT '举报原因类型',
    reason VARCHAR(500) COMMENT '举报原因详情',
    images VARCHAR(2000) COMMENT '举报截图(JSON数组)',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-待处理 1-已处理 2-已忽略',
    handle_user_id BIGINT COMMENT '处理人ID',
    handle_result VARCHAR(500) COMMENT '处理结果',
    handle_time DATETIME COMMENT '处理时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_target (target_type, target_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报表';

-- 6.3 操作日志表
DROP TABLE IF EXISTS sys_operation_log;
CREATE TABLE sys_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '操作用户名',
    operation VARCHAR(100) COMMENT '操作描述',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    result TEXT COMMENT '返回结果',
    ip VARCHAR(50) COMMENT 'IP地址',
    duration BIGINT COMMENT '执行时长(毫秒)',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-失败 1-成功',
    error_msg TEXT COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- =====================================================
-- 七、初始化数据
-- =====================================================

-- 7.1 初始化角色
INSERT INTO sys_role (role_name, role_code, description, sort) VALUES
('超级管理员', 'SUPER_ADMIN', '系统超级管理员，拥有所有权限', 1),
('管理员', 'ADMIN', '普通管理员', 2),
('版主', 'MODERATOR', '板块版主，管理特定板块', 3),
('普通用户', 'USER', '普通注册用户', 4);

-- 7.2 初始化管理员账号 (密码: admin123, 使用BCrypt加密)
INSERT INTO sys_user (username, password, nickname, avatar, user_type, status, is_verified) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt0tM0i', '系统管理员', '/static/images/admin-avatar.png', 3, 1, 1);

-- 为管理员分配超级管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 7.3 初始化论坛板块
INSERT INTO forum_section (section_name, section_code, description, icon, sort, is_default) VALUES
('跳蚤市场', 'FLEA_MARKET', '二手物品交易专区，商品自动同步至此板块', '🛒', 1, 0),
('失物招领', 'LOST_FOUND', '失物招领信息发布区，寻物/招领自动同步', '🔍', 2, 0),
('校园活动', 'ACTIVITY', '校园活动信息发布与讨论区', '🎉', 3, 0),
('互助广场', 'HELP_SQUARE', '快递代取、跑腿帮忙等互助信息', '🤝', 4, 0),
('课程资料', 'COURSE_MATERIAL', '课程笔记、考试资料分享交流', '📚', 5, 0),
('学术交流', 'ACADEMIC', '学术讨论、科研合作、竞赛组队', '🎓', 6, 0),
('实习就业', 'JOB', '实习信息、招聘信息、求职经验', '💼', 7, 0),
('情感树洞', 'EMOTION', '情感交流、心情分享、匿名倾诉', '💬', 8, 0),
('灌水区', 'WATER', '自由灌水、日常分享、闲聊吐槽', '💧', 9, 1);

-- 7.4 初始化商品分类
INSERT INTO service_product_category (category_name, icon, sort) VALUES
('电子数码', '📱', 1),
('书籍教材', '📖', 2),
('生活用品', '🏠', 3),
('服饰鞋包', '👕', 4),
('美妆护肤', '💄', 5),
('运动户外', '⚽', 6),
('乐器文具', '🎸', 7),
('其他', '📦', 8);

-- 7.5 初始化校内服务导航
INSERT INTO info_service_nav (category, name, description, icon, url, phone, sort) VALUES
('常用网站', '教务系统', '查课表、成绩、选课', '📋', 'https://jwc.example.edu.cn', NULL, 1),
('常用网站', '图书馆', '图书查询、借阅续借', '📚', 'https://lib.example.edu.cn', NULL, 2),
('常用网站', '校园卡', '校园卡充值、挂失', '💳', 'https://card.example.edu.cn', NULL, 3),
('校园服务', '后勤服务', '报修、投诉、建议', '🔧', NULL, '400-123-4567', 1),
('校园服务', '心理咨询', '心理健康咨询预约', '❤️', NULL, '400-123-4568', 2),
('校园服务', '校医院', '医疗服务、体检预约', '🏥', NULL, '400-123-4569', 3),
('交通出行', '校车时刻', '校车路线及时刻表', '🚌', NULL, NULL, 1);

-- 7.6 初始化系统配置
INSERT INTO sys_config (config_key, config_value, config_type, description) VALUES
('site_name', '校园服务论坛', 'basic', '网站名称'),
('site_logo', '/static/images/logo.png', 'basic', '网站Logo'),
('site_description', '基于微信小程序的校园服务论坛系统', 'basic', '网站描述'),
('post_audit_enabled', 'false', 'audit', '帖子是否需要审核'),
('comment_audit_enabled', 'false', 'audit', '评论是否需要审核'),
('product_audit_enabled', 'false', 'audit', '商品是否需要审核'),
('register_verification_enabled', 'true', 'user', '注册是否需要实名认证'),
('student_id_pattern', '^[0-9]{12}$', 'user', '学号格式正则表达式'),
('teacher_id_pattern', '^[A-Z][0-9]{6}$', 'user', '工号格式正则表达式');

-- =====================================================
-- 完成
-- =====================================================
SELECT '数据库初始化完成！' AS message;
