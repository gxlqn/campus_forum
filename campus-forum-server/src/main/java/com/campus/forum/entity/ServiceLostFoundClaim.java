package com.campus.forum.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 失物认领申请表
 */
public class ServiceLostFoundClaim implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long lostFoundId;
    private Long userId; // 申请人ID
    private String description; // 认领理由及证据说明
    private String images; // 证据图片（JSON数组）
    private Integer status; // 审核状态: 0-待审核 1-审核通过 2-审核拒绝
    private String auditRemark; // 审核备注
    private Long auditorId; // 审核人ID
    private LocalDateTime auditTime; // 审核时间
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getLostFoundId() { return lostFoundId; }
    public void setLostFoundId(Long lostFoundId) { this.lostFoundId = lostFoundId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    public Long getAuditorId() { return auditorId; }
    public void setAuditorId(Long auditorId) { this.auditorId = auditorId; }
    public LocalDateTime getAuditTime() { return auditTime; }
    public void setAuditTime(LocalDateTime auditTime) { this.auditTime = auditTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}