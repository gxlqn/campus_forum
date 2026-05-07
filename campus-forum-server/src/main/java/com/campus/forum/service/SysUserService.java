package com.campus.forum.service;

import com.campus.forum.entity.SysUser;
import java.util.List;
import java.util.Map;

public interface SysUserService {

    SysUser getById(Long id);

    SysUser getByOpenid(String openid);

    SysUser getByUsername(String username);

    SysUser getByStudentId(String studentId);

    List<String> getUserRoles(Long userId);

    List<String> getUserPermissionCodes(Long userId);

    SysUser wxLogin(String code, String rawUserInfo);

    SysUser adminLogin(String username, String password);

    boolean bindStudentId(Long userId, String studentId, Integer userType);

    boolean updateUserInfo(SysUser user);

    List<Map<String, Object>> getDevUserList();
}
