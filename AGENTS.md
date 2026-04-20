# Repository Guidelines

## 项目结构与模块组织
本仓库由 4 个目录组成：
- `campus-forum-server/`：Spring Boot 后端（Java 17），核心代码在 `src/main/java/com/campus/forum`，按 `controller`、`service`、`mapper`、`entity`、`config`、`security` 分层。
- `campus-forum-admin/`：Vue 3 + Vite 管理端，业务页面在 `src/views`，路由在 `src/router`，样式在 `src/styles`。
- `campus-forum-ui/`：微信小程序端，页面在 `pages/**`，请求封装在 `utils/request.js`，静态资源在 `static/`。
- `sql/`：数据库脚本（`campus_forum.sql`）。

## 构建、测试与开发命令
- 管理端：
  - `cd campus-forum-admin && npm install`
  - `npm run dev`：本地开发（Vite，默认 `3000`，代理 `/api` 到 `8080`）。
  - `npm run build`：生成生产构建。
- 后端（PowerShell）：
  - `cd campus-forum-server; .\mvnw.cmd spring-boot:run`：启动后端（默认 `http://localhost:8080/api`）。
  - `.\mvnw.cmd test`：运行测试。
  - `.\mvnw.cmd clean package`：打包。
- 小程序端：使用微信开发者工具打开 `campus-forum-ui/` 目录进行编译与预览。
- 数据库初始化示例：`mysql -u root -p < sql/campus_forum.sql`。

## 代码风格与命名规范
- 保持与现有文件一致的缩进风格：Java 使用 4 空格；Vue/WXML/JS 通常 2 空格。
- Java 类名使用 `PascalCase`，方法与字段使用 `camelCase`；按分层目录放置代码。
- Vue 页面目录采用业务分组（如 `views/system/user`），小程序页面采用 `pages/<模块>/<页面>`。
- 项目未配置 ESLint/Prettier/Checkstyle；提交前请至少确保可编译、无明显格式噪音。

## 测试规范
- 后端使用 Spring Boot Test（JUnit 5）；测试目录为 `campus-forum-server/src/test/java`，命名建议 `*Tests.java`。
- 新增接口或核心逻辑时，至少补充 1 个对应测试（单元或集成），并保证 `.\mvnw.cmd test` 通过。
- 管理端与小程序端当前无自动化测试框架；变更需提供关键页面/流程的手工验证结果。

## 提交与合并请求规范
- 当前目录未检测到 `.git` 历史，无法提炼既有提交风格；建议采用 Conventional Commits：
  - 示例：`feat(server): 新增帖子点赞接口`
  - 示例：`fix(admin): 修复用户列表分页参数`
- PR 需包含：变更范围（admin/server/ui/sql）、动机与影响、测试结果、必要截图（UI 变更）、数据库变更说明（若涉及 SQL）。

## 安全与配置提示
- `application.yml` 中包含敏感配置（数据库、JWT、微信小程序密钥）；协作时请使用本地覆盖配置或环境变量，不要提交真实密钥。
- 保持前后端 API 前缀一致（`/api`），避免硬编码多个不一致的地址。
