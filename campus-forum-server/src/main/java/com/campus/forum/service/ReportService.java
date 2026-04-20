package com.campus.forum.service;

/**
 * 用户端举报服务接口
 */
public interface ReportService {

    /**
     * 提交举报（含防重复检查）
     *
     * @param userId     当前用户ID
     * @param targetType 目标类型(1-帖子/2-评论/3-用户/4-商品/5-活动/6-失物招领/7-互助)
     * @param targetId   目标ID
     * @param reasonType 原因类型
     * @param reason     详细原因
     * @param images     截图JSON数组
     */
    void submitReport(Long userId, Integer targetType, Long targetId,
                       Integer reasonType, String reason, String images);
}
