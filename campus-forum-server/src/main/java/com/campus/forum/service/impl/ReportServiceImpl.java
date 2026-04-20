package com.campus.forum.service.impl;

import com.campus.forum.common.ResultCode;
import com.campus.forum.entity.SysReport;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.AdminSystemMapper;
import com.campus.forum.mapper.MessageMapper;
import com.campus.forum.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ReportServiceImpl implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    @Autowired
    private AdminSystemMapper adminSystemMapper;

    @Autowired
    private MessageMapper messageMapper;

    /** 举报原因类型名称 */
    private static final String[] REASON_TYPES = {
            "", "垃圾广告", "违法违规", "色情低俗", "人身攻击", "抄袭搬运", "虚假信息", "其他"
    };

    @Override
    public void submitReport(Long userId, Integer targetType, Long targetId,
                             Integer reasonType, String reason, String images) {
        // 参数校验
        if (userId == null || targetType == null || targetId == null) {
            throw new BusinessException(ResultCode.PARAM_MISSING);
        }
        if (targetType < 1 || targetType > 7) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "无效的举报目标类型");
        }
        if (!StringUtils.hasText(reason) && !StringUtils.hasText(images)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "请填写举报原因或上传截图");
        }

        // 防重复：同一用户对同一目标不能重复提交待处理的举报
        int duplicateCount = adminSystemMapper.checkDuplicateReport(userId, targetType, targetId);
        if (duplicateCount > 0) {
            throw new BusinessException(ResultCode.REPEAT_OPERATION, "您已举报过该内容，请勿重复提交");
        }

        // 写入举报记录
        SysReport report = new SysReport();
        report.setUserId(userId);
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setReasonType(reasonType != null ? reasonType : 7); // 默认"其他"
        report.setReason(StringUtils.hasText(reason) ? reason.trim() : "");
        report.setImages(StringUtils.hasText(images) ? images : null);
        report.setStatus(0);

        adminSystemMapper.insertReport(report);
        log.info("用户 {} 提交举报: type={}, id={}, reason={}", userId, targetType, targetId,
                getReasonLabel(reasonType));
    }

    /**
     * 处理举报结果通知（由AdminSystemService.handleReport调用）
     */
    public void notifyReportResult(Long reporterUserId, Integer status, String handleResult) {
        try {
            String title = (status == 1) ? "举报处理完成" : "举报已忽略";
            String content = handleResult;
            if (!StringUtils.hasText(content)) {
                content = (status == 1)
                        ? "您提交的举报已被管理员处理，感谢您的反馈"
                        : "您提交的举报已被管理员忽略，如有疑问请联系客服";
            }
            // type=9 自定义通知类型，用于举报结果通知
            messageMapper.insertNotification(reporterUserId, null, 9, title, content, null, null);
        } catch (Exception e) {
            log.error("发送举报处理通知失败: userId={}", reporterUserId, e);
        }
    }

    private String getReasonLabel(Integer reasonType) {
        if (reasonType == null || reasonType < 1 || reasonType >= REASON_TYPES.length) {
            return "其他";
        }
        return REASON_TYPES[reasonType];
    }
}
