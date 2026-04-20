package com.campus.forum.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.campus.forum.common.ResultCode;
import com.campus.forum.entity.SysUser;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.SysUserMapper;
import com.campus.forum.service.SysUserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.chanjar.weixin.common.error.WxErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class SysUserServiceImpl implements SysUserService {

    private static final Logger log = LoggerFactory.getLogger(SysUserServiceImpl.class);

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${auth.student-id-pattern:^[0-9]{12}$}")
    private String studentIdPattern;

    @Value("${auth.teacher-id-pattern:^[A-Z][0-9]{6}$}")
    private String teacherIdPattern;

    @Override
    public SysUser getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public SysUser getByOpenid(String openid) {
        return userMapper.selectByOpenid(openid);
    }

    @Override
    public SysUser getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public SysUser getByStudentId(String studentId) {
        return userMapper.selectByStudentId(studentId);
    }

    @Override
    public List<String> getUserRoles(Long userId) {
        List<String> roles = userMapper.selectUserRoles(userId);
        if (roles == null || roles.isEmpty()) {
            roles = new ArrayList<>();
            roles.add("USER");
        }
        return roles;
    }

    @Autowired
    private WxMaService wxMaService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public SysUser wxLogin(String code, String rawUserInfo) {
        if (!StringUtils.hasText(code)) {
            throw new BusinessException(ResultCode.WX_CODE_INVALID);
        }

        WxMaJscode2SessionResult sessionResult;
        try {
            sessionResult = wxMaService.getUserService().getSessionInfo(code);
        } catch (WxErrorException e) {
            log.error("微信会话获取失败", e);
            throw new BusinessException(ResultCode.WX_CODE_INVALID, "微信授权码无效");
        }

        String openid = sessionResult.getOpenid();
        String unionId = sessionResult.getUnionid();
        if (!StringUtils.hasText(openid)) {
            throw new BusinessException(ResultCode.WX_CODE_INVALID, "无法获取微信openid");
        }

        SysUser user = getByOpenid(openid);
        if (user == null && StringUtils.hasText(unionId)) {
            user = userMapper.selectByUnionId(unionId);
            if (user != null) {
                userMapper.updateOpenidAndUnionid(user.getId(), openid, unionId);
                user = getById(user.getId());
            }
        }

        Map<String, Object> userInfoMap = parseRawUserInfo(rawUserInfo);
        String nickname = getNicknameFromUserInfo(userInfoMap, openid);
        String avatar = getAvatarFromUserInfo(userInfoMap);
        Integer gender = getGenderFromUserInfo(userInfoMap);

        if (user == null) {
            user = new SysUser();
            user.setOpenid(openid);
            user.setUnionId(unionId);
            user.setUsername(getDefaultUsername(openid));
            user.setNickname(nickname);
            user.setAvatar(avatar);
            user.setGender(gender);
            user.setUserType(1);
            user.setStatus(1);
            user.setIsVerified(0);
            userMapper.insert(user);
            user = getById(user.getId());
        } else {
            boolean needUpdate = false;
            if (StringUtils.hasText(unionId) && !unionId.equals(user.getUnionId())) {
                user.setUnionId(unionId);
                needUpdate = true;
            }
            if (StringUtils.hasText(nickname) && (user.getNickname() == null || user.getNickname().startsWith("用户"))) {
                user.setNickname(nickname);
                needUpdate = true;
            }
            if (StringUtils.hasText(avatar) && (user.getAvatar() == null || user.getAvatar().contains("default-avatar"))) {
                user.setAvatar(avatar);
                needUpdate = true;
            }
            if (gender != null && (user.getGender() == null || user.getGender() == 0)) {
                user.setGender(gender);
                needUpdate = true;
            }
            if (needUpdate) {
                userMapper.updateUserInfo(user);
                user = getById(user.getId());
            }
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        user.setPassword(null);
        return user;
    }

    private Map<String, Object> parseRawUserInfo(String rawUserInfo) {
        if (!StringUtils.hasText(rawUserInfo)) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(rawUserInfo, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            log.warn("解析微信用户信息失败", e);
            return new HashMap<>();
        }
    }

    private String getDefaultUsername(String openid) {
        String suffix = openid.length() > 8 ? openid.substring(0, 8) : openid;
        return "wx_" + suffix;
    }

    private String getNicknameFromUserInfo(Map<String, Object> userInfoMap, String openid) {
        if (userInfoMap == null) {
            return "用户" + openid.substring(Math.max(0, openid.length() - 4));
        }
        Object rawNickname = userInfoMap.get("nickName");
        if (rawNickname instanceof String && StringUtils.hasText((String) rawNickname)) {
            return (String) rawNickname;
        }
        return "用户" + openid.substring(Math.max(0, openid.length() - 4));
    }

    private String getAvatarFromUserInfo(Map<String, Object> userInfoMap) {
        if (userInfoMap == null) {
            return "/static/images/default-avatar.png";
        }
        Object rawAvatar = userInfoMap.get("avatarUrl");
        if (rawAvatar instanceof String && StringUtils.hasText((String) rawAvatar)) {
            return (String) rawAvatar;
        }
        return "/static/images/default-avatar.png";
    }

    private Integer getGenderFromUserInfo(Map<String, Object> userInfoMap) {
        if (userInfoMap == null) {
            return null;
        }
        Object rawGender = userInfoMap.get("gender");
        if (rawGender instanceof Number) {
            return ((Number) rawGender).intValue();
        }
        if (rawGender instanceof String) {
            try {
                return Integer.parseInt((String) rawGender);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    @Override
    public SysUser adminLogin(String username, String password) {
        log.info("Admin login: {}", username);
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new BusinessException(ResultCode.PARAM_MISSING);
        }
        SysUser user = getByUsername(username);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        if (!StringUtils.hasText(user.getPassword()) || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }
        List<String> roles = getUserRoles(user.getId());
        boolean canAdmin = roles.stream().anyMatch(role -> "ADMIN".equalsIgnoreCase(role) || "SUPER_ADMIN".equalsIgnoreCase(role));
        if (!canAdmin) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public boolean bindStudentId(Long userId, String studentId, Integer userType) {
        log.info("Bind student ID: {} for user: {}", studentId, userId);
        if (userId == null || !StringUtils.hasText(studentId) || userType == null) {
            throw new BusinessException(ResultCode.PARAM_MISSING);
        }
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        validateStudentOrTeacherId(studentId, userType);
        SysUser existed = getByStudentId(studentId);
        if (existed != null && !existed.getId().equals(userId)) {
            throw new BusinessException(ResultCode.USER_EXISTS, "该学号/工号已绑定其他账号");
        }
        return userMapper.bindStudentId(userId, studentId, userType) > 0;
    }

    @Override
    public boolean updateUserInfo(SysUser user) {
        if (user == null || user.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_MISSING);
        }
        log.info("Update user info: {}", user.getId());
        return userMapper.updateUserInfo(user) > 0;
    }

    private void validateStudentOrTeacherId(String studentId, Integer userType) {
        String value = studentId.trim().toUpperCase(Locale.ROOT);
        if (userType == 1) {
            if (!value.matches(studentIdPattern)) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "学号格式不正确");
            }
            return;
        }
        if (userType == 2) {
            if (!value.matches(teacherIdPattern)) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "工号格式不正确");
            }
            return;
        }
        if (!value.matches(studentIdPattern) && !value.matches(teacherIdPattern)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "学号/工号格式不正确");
        }
    }

    @Override
    public List<Map<String, Object>> getDevUserList() {
        return userMapper.selectDevUserList();
    }
}
