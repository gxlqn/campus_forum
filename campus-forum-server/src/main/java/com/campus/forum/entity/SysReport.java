package com.campus.forum.entity;

import java.time.LocalDateTime;

/**
 * 举报实体
 */
public class SysReport {

    private Long id;
    private Long userId;
    private Integer targetType;
    private Long targetId;
    private Integer reasonType;
    private String reason;
    private String images;
    private Integer status;           // 0-待处理 1-已处理 2-已忽略
    private Long handleUserId;
    private String handleResult;
    private LocalDateTime handleTime;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getTargetType() { return targetType; }
    public void setTargetType(Integer targetType) { this.targetType = targetType; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public Integer getReasonType() { return reasonType; }
    public void setReasonType(Integer reasonType) { this.reasonType = reasonType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Long getHandleUserId() { return handleUserId; }
    public void setHandleUserId(Long handleUserId) { this.handleUserId = handleUserId; }

    public String getHandleResult() { return handleResult; }
    public void setHandleResult(String handleResult) { this.handleResult = handleResult; }

    public LocalDateTime getHandleTime() { return handleTime; }
    public void setHandleTime(LocalDateTime handleTime) { this.handleTime = handleTime; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
