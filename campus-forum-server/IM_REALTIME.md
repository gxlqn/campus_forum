# IM P0 实时通信落地说明

## 1. 功能范围

已实现：

- 协议层：Spring WebSocket + STOMP（单体可用）
- 鉴权：握手 JWT 校验，CONNECT 绑定 userId
- 数据模型：会话、消息、已读回执、未读计数、投递任务
- 投递语义：至少一次投递 + 客户端 clientMessageId 幂等
- 离线机制：离线落库，重连后按游标增量补拉
- 集群扩展：local / redis 两种分发模式
- 可靠性：心跳、断线重连支持、ACK 超时重发、限流防刷
- 可观测性：Actuator + Micrometer + Prometheus 指标

## 2. 初始化步骤

1. 执行数据库增量脚本：

```sql
SOURCE sql/im_realtime_upgrade.sql;
```

2. 启动后端：

```powershell
cd campus-forum-server
.\mvnw.cmd spring-boot:run
```

3. 默认 WebSocket 端点：

- `/api/ws-im?token=<jwt>`

## 3. STOMP 约定

### 3.1 客户端发送目的地

- 发送消息：`/app/im/send`
- ACK 回执：`/app/im/ack`
- 离线补拉：`/app/im/sync`

### 3.2 客户端订阅目的地

- 接收实时消息：`/user/queue/im-message`
- 发送确认：`/user/queue/im-send-ack`
- ACK 结果：`/user/queue/im-ack`
- 投递回执通知：`/user/queue/im-delivery`
- 补拉结果：`/user/queue/im-sync`
- 错误消息：`/user/queue/im-error`

### 3.3 消息体示例

发送：

```json
{
  "toUserId": 10002,
  "content": "你好",
  "contentType": 1,
  "clientMessageId": "c3f2d6a8-1b7b-4c07-a2ce-6e0b2b57f94b"
}
```

ACK：

```json
{
  "messageId": 12345,
  "clientMessageId": "c3f2d6a8-1b7b-4c07-a2ce-6e0b2b57f94b",
  "receiptType": "DELIVERED"
}
```

补拉：

```json
{
  "conversationId": "10001_10002",
  "cursorMessageId": 12000,
  "size": 50
}
```

## 4. 关键配置

`application.yml`：

- `im.endpoint`：WebSocket 端点
- `im.ack-timeout-ms`：ACK 超时阈值
- `im.retry-scan-interval-ms`：重试扫描间隔
- `im.max-retry-attempts`：最大重试次数
- `im.rate-limit-per-minute`：每用户每分钟发送上限
- `im.cluster.mode`：`local` 或 `redis`
- `management.endpoints.web.exposure.include`：已开放 `metrics,prometheus`

## 5. 验收指标与查询

### 5.1 指标端点

- `GET /api/actuator/metrics`
- `GET /api/actuator/prometheus`

### 5.2 关键指标

- `im.dispatch.attempt.total`：投递尝试数
- `im.dispatch.success.total`：投递成功数（在线送达）
- `im.dispatch.failed.total`：投递失败数
- `im.dispatch.skipped.total`：离线跳过数
- `im.send.accepted.total`：发送受理数
- `im.send.rejected.total`：发送拒绝数（限流/参数）
- `im.ack.accepted.total`：ACK 受理数
- `im.ack.rejected.total`：ACK 拒绝数
- `im.ack.e2e.latency`：消息创建到 ACK 的端到端延迟
- `im.retry.triggered.total`：超时重试触发数
- `im.retry.giveup.total`：达到上限放弃数
- `im.online.users`：在线用户数

### 5.3 口径建议

- 消息到达率：

```text
delivery_rate = im.dispatch.success.total / im.dispatch.attempt.total
```

- P95 延迟：

```text
im.ack.e2e.latency 的 p95
```

- 在线连接稳定性：

```text
观察 im.connection.events(type=connect|disconnect) 及在线人数波动
```

## 6. 压测建议

1. 准备 500~2000 并发连接，按 1:1 聊天对发送。
2. 设定消息速率，覆盖低峰/峰值/突刺三种场景。
3. 对 10% 客户端引入 ACK 延迟与断线重连，验证重试和补拉。
4. 在 `local` 与 `redis` 两种模式分别测一次。
5. 验收门槛：
   - 到达率 > 99.9%
   - P95 < 300ms
   - 在线连接波动可控且无明显雪崩掉线

## 7. 兼容与注意事项

- 幂等键唯一索引：`(sender_id, client_message_id)`。
- 同一个 `clientMessageId` 重试发送会命中去重，不会重复写入。
- ACK 建议客户端先回 `DELIVERED`，阅读后回 `READ`。
- 若启用 Redis 模式，请确保 Redis 可用并配置网络连通性。
