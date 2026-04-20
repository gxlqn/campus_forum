
-- MySQL 兼容写法：部分版本不支持 ADD COLUMN IF NOT EXISTS
-- 通过 information_schema 判断后再动态执行 ALTER，支持重复执行

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'sys_user'
    )
    AND NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'sys_user' AND column_name = 'balance'
    ),
    'ALTER TABLE sys_user ADD COLUMN balance DECIMAL(10,2) DEFAULT 0.00 COMMENT ''钱包余额''',
    'SELECT ''skip sys_user.balance'''
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'service_help_request'
    )
    AND NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'service_help_request' AND column_name = 'lock_deadline'
    ),
    'ALTER TABLE service_help_request ADD COLUMN lock_deadline DATETIME COMMENT ''锁定截止时间(到点后自动进入进行中)''',
    'SELECT ''skip service_help_request.lock_deadline'''
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'service_help_request'
    )
    AND NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'service_help_request' AND column_name = 'publisher_confirmed'
    ),
    'ALTER TABLE service_help_request ADD COLUMN publisher_confirmed TINYINT DEFAULT 0 COMMENT ''发布者是否确认完成: 0否 1是''',
    'SELECT ''skip service_help_request.publisher_confirmed'''
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'service_help_request'
    )
    AND NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'service_help_request' AND column_name = 'helper_confirmed'
    ),
    'ALTER TABLE service_help_request ADD COLUMN helper_confirmed TINYINT DEFAULT 0 COMMENT ''接单人是否确认完成: 0否 1是''',
    'SELECT ''skip service_help_request.helper_confirmed'''
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'service_help_request'
    )
    AND NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'service_help_request' AND column_name = 'publisher_confirm_time'
    ),
    'ALTER TABLE service_help_request ADD COLUMN publisher_confirm_time DATETIME COMMENT ''发布者确认完成时间''',
    'SELECT ''skip service_help_request.publisher_confirm_time'''
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'service_help_request'
    )
    AND NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'service_help_request' AND column_name = 'helper_confirm_time'
    ),
    'ALTER TABLE service_help_request ADD COLUMN helper_confirm_time DATETIME COMMENT ''接单人确认完成时间''',
    'SELECT ''skip service_help_request.helper_confirm_time'''
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'sys_user'
    )
    AND NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'sys_user' AND column_name = 'credit_score'
    ),
    'ALTER TABLE sys_user ADD COLUMN credit_score INT DEFAULT 100 COMMENT ''信誉积分''',
    'SELECT ''skip sys_user.credit_score'''
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS service_help_candidate (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  help_id BIGINT NOT NULL COMMENT '互助单ID',
  user_id BIGINT NOT NULL COMMENT '接单人ID',
  credit_score INT NOT NULL COMMENT '当时信誉积分',
  is_selected TINYINT DEFAULT 0 COMMENT '是否被选中',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '抢单时间' 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='互助单抢单候选人';

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'service_help_candidate'
    )
    AND NOT EXISTS (
      SELECT 1 FROM information_schema.statistics
      WHERE table_schema = DATABASE() AND table_name = 'service_help_candidate' AND index_name = 'uk_help_user'
    ),
    'ALTER TABLE service_help_candidate ADD UNIQUE KEY uk_help_user (help_id, user_id)',
    'SELECT ''skip service_help_candidate.uk_help_user'''
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'service_help_request'
    )
    AND NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'service_help_request' AND column_name = 'audit_pass_time'
    ),
    'ALTER TABLE service_help_request ADD COLUMN audit_pass_time DATETIME COMMENT ''审核通过时间，用于3分钟抢单池起点''',
    'SELECT ''skip service_help_request.audit_pass_time'''
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'service_help_request'
    )
    AND NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'service_help_request' AND column_name = 'is_frozen'
    ),
    'ALTER TABLE service_help_request ADD COLUMN is_frozen TINYINT DEFAULT 0 COMMENT ''佣金是否被冻结（0否，1是）''',
    'SELECT ''skip service_help_request.is_frozen'''
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'service_help_request'
    )
    AND NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'service_help_request' AND column_name = 'freeze_time'
    ),
    'ALTER TABLE service_help_request ADD COLUMN freeze_time DATETIME COMMENT ''冻结开始时间，三天后默认解冻''',
    'SELECT ''skip service_help_request.freeze_time'''
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'service_help_request'
    )
    AND NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'service_help_request' AND column_name = 'complaint_status'
    ),
    'ALTER TABLE service_help_request ADD COLUMN complaint_status TINYINT DEFAULT 0 COMMENT ''投诉状态: 0无, 1平台仲裁中, 2退回发单人, 3驳回发给接单人''',
    'SELECT ''skip service_help_request.complaint_status'''
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS sys_platform_wallet (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  amount DECIMAL(10,2) NOT NULL COMMENT '平台收益/变动金额',
  source_type INT NOT NULL COMMENT '来源类型: 1违约金',
  relation_id BIGINT COMMENT '关联单据ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台流水表';

CREATE TABLE IF NOT EXISTS user_wallet_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  type INT NOT NULL COMMENT '1充值 2发单扣款 3接单收入 4违约扣款 5退款 6违约补偿',
  relation_id BIGINT COMMENT '业务ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户钱包流水表';

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'service_help_request'
    )
    AND NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'service_help_request' AND column_name = 'fund_status'
    ),
    'ALTER TABLE service_help_request ADD COLUMN fund_status TINYINT DEFAULT 1 COMMENT ''资金状态: 1-已冻结 2-已退款 3-已放款''',
    'SELECT ''skip service_help_request.fund_status'''
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'service_help_request'
    )
    AND NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'service_help_request' AND column_name = 'fund_freeze_time'
    ),
    'ALTER TABLE service_help_request ADD COLUMN fund_freeze_time DATETIME COMMENT ''冻结时间''',
    'SELECT ''skip service_help_request.fund_freeze_time'''
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'service_help_request'
    )
    AND NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'service_help_request' AND column_name = 'fund_refund_time'
    ),
    'ALTER TABLE service_help_request ADD COLUMN fund_refund_time DATETIME COMMENT ''退款时间''',
    'SELECT ''skip service_help_request.fund_refund_time'''
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'service_help_request'
    )
    AND NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'service_help_request' AND column_name = 'fund_settle_time'
    ),
    'ALTER TABLE service_help_request ADD COLUMN fund_settle_time DATETIME COMMENT ''放款时间''',
    'SELECT ''skip service_help_request.fund_settle_time'''
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
