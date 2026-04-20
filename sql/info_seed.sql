-- 信息聚合中心示例数据
-- 执行前请先完成 campus_forum.sql 初始化

-- 校园资讯示例
INSERT INTO info_news
(title, summary, content, cover_image, source, source_url, category, view_count, is_top, status, publish_time, create_time, update_time, deleted)
VALUES
('图书馆延长开放通知', '期末周图书馆开放时间延长至22:30。', '为方便同学复习备考，图书馆自本周起工作日延长开放至22:30，请遵守馆内秩序。', '', '校图书馆', 'https://library.example.edu.cn', '图书馆', 0, 1, 1, NOW(), NOW(), NOW(), 0),
('教务系统维护公告', '本周六凌晨教务系统维护，期间无法选课与查分。', '维护时间：周六 00:30-03:30。维护完成后服务自动恢复，如有问题请联系教务处。', '', '教务处', 'https://jwc.example.edu.cn', '教务', 0, 0, 1, NOW(), NOW(), NOW(), 0),
('春季校园招聘会报名开启', '2026 春季校园招聘会已开放报名。', '请有意向参会的同学在就业系统完成报名，携带学生证按时入场。', '', '就业指导中心', 'https://job.example.edu.cn', '就业', 0, 0, 1, NOW(), NOW(), NOW(), 0),
('校车班次调整说明', '东门至南苑方向新增晚间班次。', '新增班次将于下周一试运行，具体时刻请关注后勤处最新通知。', '', '后勤处', 'https://hq.example.edu.cn', '后勤', 0, 0, 1, NOW(), NOW(), NOW(), 0);

-- 校内服务导航示例
INSERT INTO info_service_nav
(category, name, description, icon, url, phone, address, longitude, latitude, sort, status, create_time, update_time)
VALUES
('教学服务', '教务处', '课程安排、成绩与学籍相关业务', '', 'https://jwc.example.edu.cn', '010-12345601', '行政楼 201', 116.397128, 39.916527, 1, 1, NOW(), NOW()),
('图书服务', '图书馆总服务台', '借阅、续借与馆藏咨询', '', 'https://library.example.edu.cn', '010-12345602', '图书馆一层服务台', 116.398120, 39.917621, 2, 1, NOW(), NOW()),
('生活服务', '校园卡中心', '校园卡补办、充值与挂失', '', '', '010-12345603', '生活服务中心 108', 116.396612, 39.915902, 3, 1, NOW(), NOW()),
('交通服务', '校车调度室', '校车班次咨询与失物登记', '', '', '010-12345604', '北门交通服务站', 116.394951, 39.918103, 4, 1, NOW(), NOW());
