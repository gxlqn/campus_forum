package com.campus.forum.dto.admin;

public class ResolveReportRequest {

    private Integer postAuditStatus;
    private String postAuditRemark;
    private String handleResult;

    public Integer getPostAuditStatus() {
        return postAuditStatus;
    }

    public void setPostAuditStatus(Integer postAuditStatus) {
        this.postAuditStatus = postAuditStatus;
    }

    public String getPostAuditRemark() {
        return postAuditRemark;
    }

    public void setPostAuditRemark(String postAuditRemark) {
        this.postAuditRemark = postAuditRemark;
    }

    public String getHandleResult() {
        return handleResult;
    }

    public void setHandleResult(String handleResult) {
        this.handleResult = handleResult;
    }
}
