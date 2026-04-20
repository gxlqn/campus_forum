package com.campus.forum.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ServiceHelpRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private Long helperId;
    private Long postId;
    private Integer type;
    private String title;
    private String description;
    private String images;
    private String expressCompany;
    private String expressCode;
    private String expressLocation;
    private String pickupLocation;
    private String deliveryLocation;
    private LocalDateTime expectedTime;
    private BigDecimal reward;
    private Integer fundStatus;
    private LocalDateTime fundFreezeTime;
    private LocalDateTime fundRefundTime;
    private LocalDateTime fundSettleTime;
    private String tips;
    private String contactPhone;
    private Integer viewCount;
    private Integer auditStatus;
    private Integer status;
    private LocalDateTime lockDeadline;
    private Integer publisherConfirmed;
    private Integer helperConfirmed;
    private LocalDateTime publisherConfirmTime;
    private LocalDateTime helperConfirmTime;
    private LocalDateTime completeTime;
    private Integer rating;
    private String ratingContent;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
    private LocalDateTime auditPassTime;
    private Integer isFrozen;
    private LocalDateTime freezeTime;
    private Integer complaintStatus;
    private SysUser publisher;
    private SysUser helper;

    public LocalDateTime getAuditPassTime() {
        return auditPassTime;
    }

    public void setAuditPassTime(LocalDateTime auditPassTime) {
        this.auditPassTime = auditPassTime;
    }

    public Integer getIsFrozen() {
        return isFrozen;
    }

    public void setIsFrozen(Integer isFrozen) {
        this.isFrozen = isFrozen;
    }

    public LocalDateTime getFreezeTime() {
        return freezeTime;
    }

    public void setFreezeTime(LocalDateTime freezeTime) {
        this.freezeTime = freezeTime;
    }

    public Integer getComplaintStatus() {
        return complaintStatus;
    }

    public void setComplaintStatus(Integer complaintStatus) {
        this.complaintStatus = complaintStatus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getHelperId() {
        return helperId;
    }

    public void setHelperId(Long helperId) {
        this.helperId = helperId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getExpressCompany() {
        return expressCompany;
    }

    public void setExpressCompany(String expressCompany) {
        this.expressCompany = expressCompany;
    }

    public String getExpressCode() {
        return expressCode;
    }

    public void setExpressCode(String expressCode) {
        this.expressCode = expressCode;
    }

    public String getExpressLocation() {
        return expressLocation;
    }

    public void setExpressLocation(String expressLocation) {
        this.expressLocation = expressLocation;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public LocalDateTime getExpectedTime() {
        return expectedTime;
    }

    public void setExpectedTime(LocalDateTime expectedTime) {
        this.expectedTime = expectedTime;
    }

    public BigDecimal getReward() {
        return reward;
    }

    public void setReward(BigDecimal reward) {
        this.reward = reward;
    }

    public Integer getFundStatus() {
        return fundStatus;
    }

    public void setFundStatus(Integer fundStatus) {
        this.fundStatus = fundStatus;
    }

    public LocalDateTime getFundFreezeTime() {
        return fundFreezeTime;
    }

    public void setFundFreezeTime(LocalDateTime fundFreezeTime) {
        this.fundFreezeTime = fundFreezeTime;
    }

    public LocalDateTime getFundRefundTime() {
        return fundRefundTime;
    }

    public void setFundRefundTime(LocalDateTime fundRefundTime) {
        this.fundRefundTime = fundRefundTime;
    }

    public LocalDateTime getFundSettleTime() {
        return fundSettleTime;
    }

    public void setFundSettleTime(LocalDateTime fundSettleTime) {
        this.fundSettleTime = fundSettleTime;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getLockDeadline() {
        return lockDeadline;
    }

    public void setLockDeadline(LocalDateTime lockDeadline) {
        this.lockDeadline = lockDeadline;
    }

    public Integer getPublisherConfirmed() {
        return publisherConfirmed;
    }

    public void setPublisherConfirmed(Integer publisherConfirmed) {
        this.publisherConfirmed = publisherConfirmed;
    }

    public Integer getHelperConfirmed() {
        return helperConfirmed;
    }

    public void setHelperConfirmed(Integer helperConfirmed) {
        this.helperConfirmed = helperConfirmed;
    }

    public LocalDateTime getPublisherConfirmTime() {
        return publisherConfirmTime;
    }

    public void setPublisherConfirmTime(LocalDateTime publisherConfirmTime) {
        this.publisherConfirmTime = publisherConfirmTime;
    }

    public LocalDateTime getHelperConfirmTime() {
        return helperConfirmTime;
    }

    public void setHelperConfirmTime(LocalDateTime helperConfirmTime) {
        this.helperConfirmTime = helperConfirmTime;
    }

    public LocalDateTime getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(LocalDateTime completeTime) {
        this.completeTime = completeTime;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getRatingContent() {
        return ratingContent;
    }

    public void setRatingContent(String ratingContent) {
        this.ratingContent = ratingContent;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public SysUser getPublisher() {
        return publisher;
    }

    public void setPublisher(SysUser publisher) {
        this.publisher = publisher;
    }

    public SysUser getHelper() {
        return helper;
    }

    public void setHelper(SysUser helper) {
        this.helper = helper;
    }
}
