# 后端依赖漏洞扫描（稳定离线/镜像方案）

本文给出可落地的两段式方案：
- 第一步：在线预热漏洞库（可接内网镜像）
- 第二步：离线扫描（开发机/CI 均可稳定复用）

## 1. 背景与目标

你之前失败的核心原因是 dependency-check 在更新远程数据源时超时（NVD/RetireJS）。
解决思路是把“更新数据”与“执行扫描”拆开：

- 更新阶段允许联网、重试、镜像
- 扫描阶段关闭更新，只读取本地缓存

## 2. 目录内脚本

- 在线预热脚本：`scripts/dependency-check-bootstrap.ps1`
- 离线扫描脚本：`scripts/dependency-check-offline.ps1`

## 3. 一次性在线预热（建议在网络较好机器执行）

在 `campus-forum-server/` 下执行：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\dependency-check-bootstrap.ps1 -RetryCount 3 -PurgeCache
```

可选环境变量：

```powershell
$env:NVD_API_KEY = "你的NVD_API_KEY"
$env:DC_RETIREJS_URL = "https://你的镜像/retirejs/jsrepository.json"
$env:DC_KEV_URL = "https://你的镜像/known_exploited_vulnerabilities.json"
```

说明：
- `NVD_API_KEY` 可以降低 NVD 429 频率限制问题。
- `DC_RETIREJS_URL` 和 `DC_KEV_URL` 用于内网镜像。
- 脚本会自动清理常见的 `odc.update.lock` 陈旧锁。
- 在线模式失败时会自动降级为禁用 RetireJS 的回退扫描，确保 Java 依赖 CVE 结果可产出。

## 4. 日常离线扫描（推荐开发机/CI默认使用）

在 `campus-forum-server/` 下执行：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\dependency-check-offline.ps1
```

该模式会使用：
- `-DautoUpdate=false`
- `-DretireJsAnalyzerEnabled=false`（默认）

这样可避免每次扫描都依赖外网更新，从而稳定出报告。

## 5. CI建议流程

建议拆成两个 Job：

1. `security-db-warmup`（定时执行，例如每天凌晨）
- 执行 bootstrap 脚本更新本地库。
- 产物缓存 `C:\Users\<runner-user>\.m2\repository\org\owasp\dependency-check-data\11.0`。

2. `security-scan-offline`（每次提交执行）
- 恢复上一步缓存。
- 执行 offline 脚本，产出 `target/dependency-check-report.json/html`。

## 6. 故障排查

1. 若提示锁文件占用：
```powershell
Remove-Item "C:\Users\$env:USERNAME\.m2\repository\org\owasp\dependency-check-data\11.0\odc.update.lock" -Force
```

2. 若提示 RetireJS JSON 损坏：
```powershell
.\mvnw.cmd org.owasp:dependency-check-maven:purge
```
然后重新执行 bootstrap。

3. 若遇到 NVD 429：
- 配置 `NVD_API_KEY`
- 改为低峰时段更新
- 使用内网镜像（`DC_RETIREJS_URL` / `DC_KEV_URL`）

## 7. 报告位置

- `target/dependency-check-report.json`
- `target/dependency-check-report.html`

## 8. Elasticsearch 索引初始化与回填

新增一键脚本：`scripts/es-init-and-reindex.ps1`。

用途：
- 按映射文件初始化索引
- 可选触发后端 `/admin/search/reindex` 全量回填

执行示例：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\es-init-and-reindex.ps1 -EsBaseUrl "http://localhost:9200" -ApiBaseUrl "http://localhost:8080/api" -AdminToken "<管理员JWT>"
```

索引映射文件位置：
- `src/main/resources/elasticsearch/mappings/forum_post.json`
- `src/main/resources/elasticsearch/mappings/service_product.json`
- `src/main/resources/elasticsearch/mappings/service_activity.json`
- `src/main/resources/elasticsearch/mappings/service_help_request.json`

## 9. ES 增量同步失败重试队列

为保证最终一致性，项目新增了失败重试任务队列：

- 失败入队表：`search_sync_task`
- 建表脚本：`sql/search_sync_task.sql`
- 定时重放：`SearchSyncRetryScheduler`（默认每 30 秒）

状态说明：
- `0` 待重试
- `1` 已成功
- `2` 失败终态（达到最大重试次数）
- `3` 处理中

管理接口：
- `GET /api/admin/search/sync-tasks` 查询任务（支持分页/筛选）
- `POST /api/admin/search/sync-tasks/{taskId}/retry` 手动重试
