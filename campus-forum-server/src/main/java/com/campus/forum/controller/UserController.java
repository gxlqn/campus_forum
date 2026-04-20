package com.campus.forum.controller;

import com.campus.forum.common.PageResult;
import com.campus.forum.common.Result;
import com.campus.forum.entity.SysUser;
import com.campus.forum.service.UserCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserCenterService userCenterService;

    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo(@AuthenticationPrincipal SysUser currentUser) {
        return Result.success(userCenterService.getUserInfo(currentUser.getId()));
    }

    @GetMapping("/public/{userId}")
    public Result<Map<String, Object>> getPublicUserProfile(
            @AuthenticationPrincipal SysUser currentUser,
            @PathVariable Long userId) {
        return Result.success(userCenterService.getPublicUserProfile(currentUser == null ? null : currentUser.getId(), userId));
    }

    @PutMapping("/info")
    public Result<Map<String, Object>> updateUserInfo(@AuthenticationPrincipal SysUser currentUser,
            @RequestBody SysUser update) {
        return Result.success(userCenterService.updateUserInfo(currentUser.getId(), update));
    }

    @GetMapping("/my/publish")
    public Result<PageResult<Map<String, Object>>> getMyPublish(
            @AuthenticationPrincipal SysUser currentUser,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long current,
            @RequestParam(required = false) Long page,
            @RequestParam(defaultValue = "10") Long size) {
        Long pageNo = current != null ? current : page;
        if (pageNo == null) {
            pageNo = 1L;
        }
        return Result.success(userCenterService.getMyPublishes(currentUser.getId(), type, pageNo, size));
    }

    @GetMapping("/follows")
    public Result<PageResult<Map<String, Object>>> getMyFollows(
            @AuthenticationPrincipal SysUser currentUser,
            @RequestParam(required = false) Long current,
            @RequestParam(required = false) Long page,
            @RequestParam(defaultValue = "10") Long size) {
        Long pageNo = current != null ? current : page;
        if (pageNo == null) {
            pageNo = 1L;
        }
        return Result.success(userCenterService.getMyFollows(currentUser.getId(), pageNo, size));
    }

    @PostMapping("/follows/{followUserId}")
    public Result<Void> followUser(@AuthenticationPrincipal SysUser currentUser,
            @PathVariable Long followUserId) {
        userCenterService.followUser(currentUser.getId(), followUserId);
        return Result.success();
    }

    @DeleteMapping("/follows/{followUserId}")
    public Result<Void> unfollowUser(@AuthenticationPrincipal SysUser currentUser,
            @PathVariable Long followUserId) {
        userCenterService.unfollowUser(currentUser.getId(), followUserId);
        return Result.success();
    }

    @GetMapping("/evaluations")
    public Result<PageResult<Map<String, Object>>> getMyEvaluations(
            @AuthenticationPrincipal SysUser currentUser,
            @RequestParam(required = false) Long current,
            @RequestParam(required = false) Long page,
            @RequestParam(defaultValue = "10") Long size) {
        Long pageNo = current != null ? current : page;
        if (pageNo == null) {
            pageNo = 1L;
        }
        return Result.success(userCenterService.getMyEvaluations(currentUser.getId(), pageNo, size));
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getMyStats(@AuthenticationPrincipal SysUser currentUser) {
        return Result.success(userCenterService.getMyStats(currentUser.getId()));
    }

    @PostMapping("/wallet/recharge")
    public Result<Map<String, Object>> rechargeWallet(@AuthenticationPrincipal SysUser currentUser,
            @RequestBody Map<String, Object> body) {
        Object amountObj = body == null ? null : body.get("amount");
        if (amountObj == null) {
            return Result.error("充值金额不能为空");
        }
        BigDecimal amount;
        try {
            amount = new BigDecimal(String.valueOf(amountObj));
        } catch (Exception e) {
            return Result.error("充值金额格式不正确");
        }
        return Result.success(userCenterService.rechargeWallet(currentUser.getId(), amount));
    }
}
