package com.campus.forum.controller;

import com.campus.forum.common.Result;
import com.campus.forum.dto.SubmitReportRequest;
import com.campus.forum.entity.SysUser;
import com.campus.forum.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping
    public Result<Void> submitReport(@RequestBody SubmitReportRequest request,
                                     @AuthenticationPrincipal SysUser currentUser) {
        reportService.submitReport(
                currentUser.getId(),
                request.getTargetType(),
                request.getTargetId(),
                request.getReasonType(),
                request.getReason(),
                request.getImages()
        );
        return Result.success();
    }
}
