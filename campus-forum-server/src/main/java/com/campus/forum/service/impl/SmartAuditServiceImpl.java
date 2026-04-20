package com.campus.forum.service.impl;

import com.campus.forum.entity.AuditLog;
import com.campus.forum.mapper.AuditLogMapper;
import com.campus.forum.service.ContentFilterService;
import com.campus.forum.service.SmartAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SmartAuditServiceImpl implements SmartAuditService {

    private static final Logger log = LoggerFactory.getLogger(SmartAuditServiceImpl.class);

    @Autowired
    private ContentFilterService contentFilterService;

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Override
    public AuditResult auditPost(Long userId, String title, String content, Long postId) {
        return doAudit(userId, "post", postId, title, content);
    }

    @Override
    public AuditResult auditProduct(Long userId, String title, String description, Long productId) {
        return doAudit(userId, "product", productId, title, description);
    }

    @Override
    public AuditResult auditLostFound(Long userId, String title, String description, Long lostFoundId) {
        return doAudit(userId, "lostfound", lostFoundId, title, description);
    }

    @Override
    public AuditResult auditActivity(Long userId, String title, String description, Long activityId) {
        return doAudit(userId, "activity", activityId, title, description);
    }

    @Override
    public AuditResult auditComment(Long userId, String content, Long commentId) {
        return doAudit(userId, "comment", commentId, null, content);
    }

    private AuditResult doAudit(Long userId, String targetType, Long targetId, String title, String content) {
        long startTime = System.currentTimeMillis();

        ContentFilterService.FilterDetail detail = contentFilterService.filterWithDetails(title, content);

        int finalStatus;
        AuditResult result;
        String reason;

        switch (detail.getResult()) {
            case BLOCKED:
                finalStatus = SmartAuditService.AuditStatus.AUTO_REJECT.getCode();
                reason = "内容包含违规关键词: " + String.join(", ", detail.getMatchedKeywords());
                result = SmartAuditService.AuditResult.reject(reason);
                break;

            case NEED_MANUAL:
                finalStatus = SmartAuditService.AuditStatus.MANUAL_REVIEW.getCode();
                reason = "内容包含敏感词，需人工审核: " + String.join(", ", detail.getMatchedKeywords());
                result = SmartAuditService.AuditResult.needManual(reason);
                break;

            case SUSPICIOUS:
                finalStatus = SmartAuditService.AuditStatus.AI_UNCERTAIN.getCode();
                reason = "内容包含可疑词汇，已标记: " + String.join(", ", detail.getMatchedKeywords());
                result = SmartAuditService.AuditResult.suspicious(reason);
                break;

            default:
                finalStatus = SmartAuditService.AuditStatus.AUTO_PASS.getCode();
                reason = "内容审核通过";
                result = SmartAuditService.AuditResult.pass();
                break;
        }

        result.setMatchedKeywords(detail.getMatchedKeywords());
        result.setMatchedLevel(detail.getMaxLevel());

        saveAuditLog(targetType, targetId, userId, detail, finalStatus, startTime, title, content);

        return result;
    }

    private void saveAuditLog(String targetType, Long targetId, Long userId,
                               ContentFilterService.FilterDetail detail, int finalStatus,
                               long startTime, String title, String content) {
        if (targetId == null) {
            // 发布前预审核场景下目标ID尚未生成，跳过结构化日志入库，避免数据库非空约束报错。
            log.debug("跳过审核日志入库: targetId is null, type={}", targetType);
            return;
        }
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setTargetType(targetType);
            auditLog.setTargetId(targetId);
            auditLog.setUserId(userId);
            auditLog.setFilterResult(detail.getResult().getCode());
            auditLog.setMatchedKeywords(detail.getMatchedKeywords() != null ?
                    String.join(",", detail.getMatchedKeywords()) : null);
            auditLog.setMatchedLevel(detail.getMaxLevel());
            auditLog.setAiAuditStatus(0);
            auditLog.setFinalStatus(finalStatus);
            auditLog.setAuditMethod(1);
            auditLog.setContentSnapshot(buildSnapshot(title, content));

            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            log.error("保存审核日志失败: type={}, id={}", targetType, targetId, e);
        }
    }

    private String buildSnapshot(String title, String content) {
        StringBuilder sb = new StringBuilder();
        if (title != null) sb.append("标题: ").append(title).append("; ");
        if (content != null) {
            if (content.length() > 200) {
                sb.append("内容: ").append(content.substring(0, 200)).append("...");
            } else {
                sb.append("内容: ").append(content);
            }
        }
        return sb.toString();
    }

    @Override
    public AuditLog getAuditLog(String targetType, Long targetId) {
        return auditLogMapper.selectByTarget(targetType, targetId);
    }
}
