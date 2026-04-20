package com.campus.forum.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.campus.forum.common.BaseEntity;

import java.time.LocalDateTime;

/**
 * 首页轮播图实体
 */
public class Banner extends BaseEntity {

    /** 轮播图片URL */
    private String imageUrl;

    /** 轮播标题 */
    private String title;

    /** 副标题/描述 */
    private String subtitle;

    /** 跳转类型: page-页面 web-网页 miniapp-小程序 none-无跳转 */
    private String linkType;

    /** 跳转地址 */
    private String linkUrl;

    /** 小程序AppId（linkType=miniapp时使用） */
    private String appId;

    /** 背景渐变色（CSS值） */
    private String bgColor;

    /** 排序权重（越大越靠前） */
    private Integer priority;

    /** 状态: 0-停用 1-启用 */
    private Integer status;

    // Getters & Setters
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public String getLinkType() { return linkType; }
    public void setLinkType(String linkType) { this.linkType = linkType; }

    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }

    public String getBgColor() { return bgColor; }
    public void setBgColor(String bgColor) { this.bgColor = bgColor; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
