package com.campus.forum.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ServiceHelpCandidate implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long helpId;
    private Long userId;
    private Integer creditScore;
    private Integer isSelected;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getHelpId() { return helpId; }
    public void setHelpId(Long helpId) { this.helpId = helpId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getCreditScore() { return creditScore; }
    public void setCreditScore(Integer creditScore) { this.creditScore = creditScore; }
    public Integer getIsSelected() { return isSelected; }
    public void setIsSelected(Integer isSelected) { this.isSelected = isSelected; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
