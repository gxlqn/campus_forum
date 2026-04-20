-- 搜索索引同步重试任务表
CREATE TABLE IF NOT EXISTS search_sync_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  index_name VARCHAR(64) NOT NULL,
  entity_type VARCHAR(32) NOT NULL,
  document_id BIGINT NOT NULL,
  operation_type VARCHAR(16) NOT NULL COMMENT 'UPSERT/DELETE',
  payload_json LONGTEXT NULL,
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0=pending,1=success,2=dead,3=processing',
  retry_count INT NOT NULL DEFAULT 0,
  max_retry INT NOT NULL DEFAULT 6,
  last_error VARCHAR(1000) NULL,
  next_retry_time DATETIME NOT NULL,
  last_tried_time DATETIME NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_status_next_retry (status, next_retry_time),
  KEY idx_entity (entity_type, document_id),
  KEY idx_created (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
