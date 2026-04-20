package com.campus.forum.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String targetType;
    private Long targetId;
    private Long userId;

    private Integer filterResult;
    private String matchedKeywords;
    private Integer matchedLevel;

    private Integer aiAuditStatus;
    private BigDecimal aiConfidence;
    private String aiLabels;
    private Integer aiResponseTime;

    private Integer finalStatus;
    private Integer auditMethod;
    private Long auditorId;
    private String auditRemark;

    private String contentSnapshot;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getFilterResult() {
        return filterResult;
    }

    public void setFilterResult(Integer filterResult) {
        this.filterResult = filterResult;
    }

    public String getMatchedKeywords() {
        return matchedKeywords;
    }

    public void setMatchedKeywords(String matchedKeywords) {
        this.matchedKeywords = matchedKeywords;
    }

    public Integer getMatchedLevel() {
        return matchedLevel;
    }

    public void setMatchedLevel(Integer matchedLevel) {
        this.matchedLevel = matchedLevel;
    }

    public Integer getAiAuditStatus() {
        return aiAuditStatus;
    }

    public void setAiAuditStatus(Integer aiAuditStatus) {
        this.aiAuditStatus = aiAuditStatus;
    }

    public BigDecimal getAiConfidence() {
        return aiConfidence;
    }

    public void setAiConfidence(BigDecimal aiConfidence) {
        this.aiConfidence = aiConfidence;
    }

    public String getAiLabels() {
        return aiLabels;
    }

    public void setAiLabels(String aiLabels) {
        this.aiLabels = aiLabels;
    }

    public Integer getAiResponseTime() {
        return aiResponseTime;
    }

    public void setAiResponseTime(Integer aiResponseTime) {
        this.aiResponseTime = aiResponseTime;
    }

    public Integer getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(Integer finalStatus) {
        this.finalStatus = finalStatus;
    }

    public Integer getAuditMethod() {
        return auditMethod;
    }

    public void setAuditMethod(Integer auditMethod) {
        this.auditMethod = auditMethod;
    }

    public Long getAuditorId() {
        return auditorId;
    }

    public void setAuditorId(Long auditorId) {
        this.auditorId = auditorId;
    }

    public String getAuditRemark() {
        return auditRemark;
    }

    public void setAuditRemark(String auditRemark) {
        this.auditRemark = auditRemark;
    }

    public String getContentSnapshot() {
        return contentSnapshot;
    }

    public void setContentSnapshot(String contentSnapshot) {
        this.contentSnapshot = contentSnapshot;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
