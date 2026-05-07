# Campus Forum

校园服务论坛系统，包含后端、管理端和微信小程序三部分。

## 项目结构

```text
campus-forum/
├─ campus-forum-server/   Spring Boot 后端（Java 17）
├─ campus-forum-admin/    Vue 3 + Vite 管理后台
├─ campus-forum-ui/       微信小程序端
├─ sql/                   数据库脚本
└─ README.md
```

## 核心功能

- 论坛内容发布、详情、编辑、删除、举报
- 二手商品、活动、互助、失物招领等校园服务模块
- 资讯和导航管理
- 消息通知与实时通信
- 后台审核、版主管理、敏感词管理
- 敏感词黑名单/白名单管理
- ES 搜索与数据库回退搜索

## 技术栈

- 后端：Spring Boot 3.2、Spring Security、MyBatis-Plus、MySQL、Redis、Elasticsearch、WebSocket
- 管理端：Vue 3、Vite、Element Plus、Pinia、Axios
- 小程序端：微信小程序原生开发

## 后端结构

后端主入口：`campus-forum-server/src/main/java/com/campus/forum/CampusForumServerApplication.java`

主要分层：

- `controller/`：对外 HTTP 接口
- `service/`：业务接口
- `service/impl/`：业务实现
- `mapper/`：MyBatis 数据访问
- `entity/`：数据库实体
- `dto/`：请求/响应数据对象
- `config/`：配置与启动初始化
- `security/`：认证与权限控制
- `search/`：搜索同步与检索相关逻辑

常见业务模块：

- 论坛：帖子、评论、点赞、收藏、举报
- 服务：商品、活动、失物招领、互助
- 管理后台：统计、审核、版主管理、敏感词管理
- 搜索：ES 优先，数据库兜底

## 管理端结构

管理端入口：`campus-forum-admin/src/main.js`

常用目录：

- `src/views/`：页面视图
- `src/api/`：接口封装
- `src/router/`：路由配置
- `src/layouts/`：布局组件
- `src/styles/`：全局样式

## 小程序端结构

小程序入口：`campus-forum-ui/app.json`

常用目录：

- `pages/`：业务页面
- `components/`：通用组件
- `utils/`：请求封装、工具方法
- `static/`：图片与静态资源

## 数据库

数据库脚本位于：`sql/campus_forum.sql`

说明：

- 该脚本包含表结构和初始化数据
- 当前敏感词表支持 `word_type` 字段，用于区分黑名单和白名单
- 如果你已有旧库，后端启动时会尝试自动补齐 `word_type`

## 本地运行

### 1. 初始化数据库

```bash
mysql -u root -p < sql/campus_forum.sql
```

### 2. 启动后端

```bash
cd campus-forum-server
./mvnw.cmd spring-boot:run
```

默认地址：`http://localhost:8081/api`

### 3. 启动管理端

```bash
cd campus-forum-admin
npm install
npm run dev
```

默认地址：`http://localhost:3000`

### 4. 打开小程序端

- 使用微信开发者工具打开 `campus-forum-ui/`
- 根据本地后端地址调整请求配置

## 构建命令

### 后端

```bash
cd campus-forum-server
./mvnw.cmd test
./mvnw.cmd clean package
./mvnw.cmd -DskipTests compile
```

### 管理端

```bash
cd campus-forum-admin
npm run build
```

## 配置说明

### 后端配置

主要配置文件：`campus-forum-server/src/main/resources/application.yml`

关键项：

- `server.port`: 8081
- `server.servlet.context-path`: `/api`
- `spring.datasource.*`: MySQL 连接
- `search.engine`: `es` 或 `db`
- `wechat.miniapp.*`: 小程序配置

### 管理端代理

管理端通过前端请求封装访问后端，默认走 `/api` 前缀。

## 代码阅读建议

如果你想快速理解项目，建议按下面顺序看：

1. 后端入口和配置：`CampusForumServerApplication.java`、`application.yml`
2. 论坛主流程：`ForumController`、`ForumServiceImpl`
3. 内容审核：`ContentFilterServiceImpl`、`SensitiveWordEngine`
4. 管理后台页面：`campus-forum-admin/src/views/`
5. 小程序主页面：`campus-forum-ui/pages/`

## 当前敏感词设计

- 黑名单：正常敏感词拦截、替换、审核分级
- 白名单：业务词豁免，优先级高于黑名单
- 后端启动时会确保旧库尽量自动补齐 `word_type`

## 注意事项

- 不要提交真实密钥、数据库密码或 JWT 密钥
- `dist/`、`node_modules/`、`temp/`、`output/` 等生成目录通常不需要纳入版本控制
- 小程序端需要在微信开发者工具里单独预览和调试

## 许可证

未单独声明，默认以项目当前仓库约定为准。
