package com.campus.forum.controller;

import com.campus.forum.common.Result;
import com.campus.forum.dto.LoginRequest;
import com.campus.forum.dto.LoginResponse;
import com.campus.forum.dto.WxLoginRequest;
import com.campus.forum.entity.SysUser;
import com.campus.forum.service.SysUserService;
import com.campus.forum.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private SysUserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${auth.dev-mode:false}")
    private boolean devMode;

    /**
     * 微信小程序登录
     */
    @PostMapping("/wx/login")
    public Result<LoginResponse> wxLogin(@RequestBody WxLoginRequest request) {
        log.info("微信登录请求: code={}", request.getCode());

        SysUser user = userService.wxLogin(request.getCode(), request.getUserInfo());
        String token = jwtUtils.generateToken(user.getId(), user.getNickname());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(user);
        response.setNeedBind(user.getIsVerified() != 1);

        return Result.success(response);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    /**
     * 管理员登录
     */
    @PostMapping("/admin/login")
    public Result<LoginResponse> adminLogin(@RequestBody LoginRequest request) {
        log.info("管理员登录请求: username={}", request.getUsername());

        SysUser user = userService.adminLogin(request.getUsername(), request.getPassword());
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        List<String> roles = userService.getUserRoles(user.getId());
        List<String> permissions = userService.getUserPermissionCodes(user.getId());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(user);
        response.setRoles(roles);
        response.setPermissions(permissions);

        return Result.success(response);
    }

    /**
     * 绑定学号/工号
     */
    @PostMapping("/bind")
    public Result<Void> bindStudentId(@AuthenticationPrincipal SysUser currentUser,
            @RequestParam String studentId,
            @RequestParam(defaultValue = "1") Integer userType) {
        userService.bindStudentId(currentUser.getId(), studentId, userType);
        return Result.success();
    }

    /**
     * 开发模式：获取用户列表（用于切换账号）
     */
    @GetMapping("/dev/users")
    public Result<List<Map<String, Object>>> getDevUsers() {
        if (!devMode) {
            return Result.error("开发模式未启用");
        }
        List<Map<String, Object>> users = userService.getDevUserList();
        return Result.success(users);
    }

    /**
     * 开发模式：通过用户ID切换账号
     */
    @PostMapping("/dev/login")
    public Result<LoginResponse> devLogin(@RequestBody Map<String, Long> request) {
        if (!devMode) {
            return Result.error("开发模式未启用");
        }
        
        Long userId = request.get("userId");
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        
        log.warn("[开发模式] 用户切换账号: targetUserId={}", userId);
        
        SysUser user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        String token = jwtUtils.generateToken(user.getId(), user.getNickname());
        
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(user);
        response.setNeedBind(user.getIsVerified() != 1);
        
        return Result.success(response);
    }
}
