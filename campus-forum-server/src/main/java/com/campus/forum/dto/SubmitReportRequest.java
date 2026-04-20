package com.campus.forum.dto;

/**
 * 用户提交举报请求
 */
public class SubmitReportRequest {

    /** 举报目标类型: 1-帖子 2-评论 3-用户 4-商品 5-活动 6-失物招领 7-互助 */
    private Integer targetType;

    /** 举报目标ID */
    private Long targetId;

    /** 举报原因类型: 1-垃圾广告 2-违法违规 3-色情低俗 4-人身攻击 5-抄袭搬运 6-虚假信息 7-其他 */
    private Integer reasonType;

    /** 举报原因详情（用户填写） */
    private String reason;

    /** 举报截图URL列表(JSON数组) */
    private String images;

    public Integer getTargetType() {
        return targetType;
    }

    public void setTargetType(Integer targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public Integer getReasonType() {
        return reasonType;
    }

    public void setReasonType(Integer reasonType) {
        this.reasonType = reasonType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }
}
