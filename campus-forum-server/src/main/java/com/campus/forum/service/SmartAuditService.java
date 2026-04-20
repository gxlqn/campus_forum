package com.campus.forum.service;

import com.campus.forum.entity.AuditLog;

import java.util.List;

public interface SmartAuditService {

    AuditResult auditPost(Long userId, String title, String content, Long postId);

    AuditResult auditProduct(Long userId, String title, String description, Long productId);

    AuditResult auditLostFound(Long userId, String title, String description, Long lostFoundId);

    AuditResult auditActivity(Long userId, String title, String description, Long activityId);

    AuditResult auditComment(Long userId, String content, Long commentId);

    AuditLog getAuditLog(String targetType, Long targetId);

    enum AuditStatus {
        PENDING(0, "待审核"),
        AUTO_PASS(1, "自动通过"),
        AUTO_REJECT(2, "自动拒绝"),
        AI_UNCERTAIN(3, "AI不确定"),
        MANUAL_REVIEW(4, "待人工复核");

        private final int code;
        private final String desc;

        AuditStatus(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() { return code; }
        public String getDesc() { return desc; }

        public static AuditStatus fromCode(int code) {
            for (AuditStatus s : values()) {
                if (s.code == code) return s;
            }
            return PENDING;
        }
    }

    class AuditResult {
        private boolean passed;
        private int auditStatus;
        private String reason;
        private List<String> matchedKeywords;
        private Integer matchedLevel;
        private Long auditLogId;

        public AuditResult(boolean passed, int auditStatus, String reason) {
            this.passed = passed;
            this.auditStatus = auditStatus;
            this.reason = reason;
        }

        public static AuditResult pass() {
            return new AuditResult(true, AuditStatus.AUTO_PASS.getCode(), "内容审核通过");
        }

        public static AuditResult reject(String reason) {
            return new AuditResult(false, AuditStatus.AUTO_REJECT.getCode(), reason);
        }

        public static AuditResult needManual(String reason) {
            return new AuditResult(false, AuditStatus.MANUAL_REVIEW.getCode(), reason);
        }

        public static AuditResult suspicious(String reason) {
            return new AuditResult(false, AuditStatus.AI_UNCERTAIN.getCode(), reason);
        }

        public boolean isPassed() { return passed; }
        public int getAuditStatus() { return auditStatus; }
        public String getReason() { return reason; }
        public List<String> getMatchedKeywords() { return matchedKeywords; }
        public void setMatchedKeywords(List<String> matchedKeywords) { this.matchedKeywords = matchedKeywords; }
        public Integer getMatchedLevel() { return matchedLevel; }
        public void setMatchedLevel(Integer matchedLevel) { this.matchedLevel = matchedLevel; }
        public Long getAuditLogId() { return auditLogId; }
        public void setAuditLogId(Long auditLogId) { this.auditLogId = auditLogId; }
    }
}
