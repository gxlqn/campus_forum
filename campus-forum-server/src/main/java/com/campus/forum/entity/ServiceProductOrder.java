package com.campus.forum.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ServiceProductOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String orderNo;
    private Long productId;
    private Long buyerId;
    private Long sellerId;
    private BigDecimal amount;
    private Integer status;
    private String cancelReason;
    private String meetupPlace;
    private LocalDateTime meetupTime;
    private String meetupCode;
    private Integer meetupVerified;
    private Integer rescheduleCount;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime cancelTime;
    private LocalDateTime finishTime;
    private LocalDateTime updateTime;
    private Integer deleted;

    private ServiceProduct product;
    private SysUser buyer;
    private SysUser seller;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public String getMeetupPlace() {
        return meetupPlace;
    }

    public void setMeetupPlace(String meetupPlace) {
        this.meetupPlace = meetupPlace;
    }

    public LocalDateTime getMeetupTime() {
        return meetupTime;
    }

    public void setMeetupTime(LocalDateTime meetupTime) {
        this.meetupTime = meetupTime;
    }

    public String getMeetupCode() {
        return meetupCode;
    }

    public void setMeetupCode(String meetupCode) {
        this.meetupCode = meetupCode;
    }

    public Integer getMeetupVerified() {
        return meetupVerified;
    }

    public void setMeetupVerified(Integer meetupVerified) {
        this.meetupVerified = meetupVerified;
    }

    public Integer getRescheduleCount() {
        return rescheduleCount;
    }

    public void setRescheduleCount(Integer rescheduleCount) {
        this.rescheduleCount = rescheduleCount;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getPayTime() {
        return payTime;
    }

    public void setPayTime(LocalDateTime payTime) {
        this.payTime = payTime;
    }

    public LocalDateTime getCancelTime() {
        return cancelTime;
    }

    public void setCancelTime(LocalDateTime cancelTime) {
        this.cancelTime = cancelTime;
    }

    public LocalDateTime getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(LocalDateTime finishTime) {
        this.finishTime = finishTime;
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

    public ServiceProduct getProduct() {
        return product;
    }

    public void setProduct(ServiceProduct product) {
        this.product = product;
    }

    public SysUser getBuyer() {
        return buyer;
    }

    public void setBuyer(SysUser buyer) {
        this.buyer = buyer;
    }

    public SysUser getSeller() {
        return seller;
    }

    public void setSeller(SysUser seller) {
        this.seller = seller;
    }
}
