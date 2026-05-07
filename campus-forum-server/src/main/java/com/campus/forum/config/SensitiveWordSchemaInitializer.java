package com.campus.forum.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Component("sensitiveWordSchemaInitializer")
public class SensitiveWordSchemaInitializer {

    private static final Logger log = LoggerFactory.getLogger(SensitiveWordSchemaInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        try {
            ensureWordTypeColumn();
            ensureWordTypeIndex();
            ensureDefaultWhitelistWord();
        } catch (Exception e) {
            log.error("敏感词表结构初始化失败", e);
        }
    }

    private void ensureWordTypeColumn() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() " +
                        "AND table_name = 'audit_sensitive_word' AND column_name = 'word_type'",
                Integer.class);
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE audit_sensitive_word ADD COLUMN word_type TINYINT NOT NULL DEFAULT 1 COMMENT '词类型: 1-黑名单 2-白名单' AFTER word");
        log.info("已为 audit_sensitive_word 表补充 word_type 字段");
    }

    private void ensureWordTypeIndex() {
        List<Map<String, Object>> indexes = jdbcTemplate.queryForList(
                "SELECT INDEX_NAME FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'audit_sensitive_word'");
        boolean hasComposite = indexes.stream().anyMatch(row -> "uk_word_type".equals(String.valueOf(row.get("INDEX_NAME"))));
        if (!hasComposite) {
            try {
                jdbcTemplate.execute("ALTER TABLE audit_sensitive_word DROP INDEX uk_word");
            } catch (Exception ignored) {
            }
            try {
                jdbcTemplate.execute("ALTER TABLE audit_sensitive_word ADD UNIQUE INDEX uk_word_type (word, word_type)");
                log.info("已调整 audit_sensitive_word 唯一索引为 (word, word_type)");
            } catch (Exception ignored) {
            }
        }
        Integer wordTypeIndexCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() " +
                        "AND table_name = 'audit_sensitive_word' AND index_name = 'idx_word_type'",
                Integer.class);
        if (wordTypeIndexCount == null || wordTypeIndexCount == 0) {
            jdbcTemplate.execute("ALTER TABLE audit_sensitive_word ADD INDEX idx_word_type (word_type)");
        }
    }

    private void ensureDefaultWhitelistWord() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_sensitive_word WHERE word = '求购' AND word_type = 2",
                Integer.class);
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.update(
                "INSERT INTO audit_sensitive_word (word, word_type, category, level, replacement, is_enabled, remark, creator_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                "求购", 2, 5, 1, null, 1, "业务白名单", null);
        log.info("已补充默认白名单词: 求购");
    }
}
