-- 系统权限初始化脚本（可单独执行）
-- 依赖：sys_role、sys_permission、sys_role_permission 已创建

USE campus_forum;

DELETE FROM sys_role_permission;
DELETE FROM sys_permission;

INSERT INTO sys_permission(parent_id, permission_name, permission_code, permission_type, path, icon, sort, status)
VALUES
(0, '系统管理', 'system', 1, '/system', 'Setting', 1, 1),
(0, '论坛管理', 'forum', 1, '/forum', 'ChatDotRound', 2, 1),
(0, '服务管理', 'service', 1, '/service', 'Service', 3, 1),

(1, '用户管理', 'system:user', 1, '/system/user', 'User', 11, 1),
(1, '角色权限', 'system:role', 1, '/system/role', 'UserFilled', 12, 1),
(1, '内容审核', 'system:audit', 1, '/system/audit', 'Document', 13, 1),
(1, '举报处理', 'system:report', 1, '/system/report', 'Warning', 14, 1),

(2, '板块管理', 'forum:section', 1, '/forum/section', 'Grid', 21, 1),
(2, '帖子管理', 'forum:post', 1, '/forum/post', 'Document', 22, 1),

(3, '服务分类', 'service:category', 1, '/service/product', 'ShoppingCart', 31, 1);

-- 给 SUPER_ADMIN 分配全部权限
INSERT INTO sys_role_permission(role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
CROSS JOIN sys_permission p
WHERE r.role_code = 'SUPER_ADMIN';

-- 给 ADMIN 分配除“角色权限”外的主要管理权限
INSERT INTO sys_role_permission(role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.permission_code IN (
  'system',
  'forum',
  'service',
  'system:user',
  'system:audit',
  'system:report',
  'forum:section',
  'forum:post',
  'service:category'
)
WHERE r.role_code = 'ADMIN';
