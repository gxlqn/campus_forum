package com.campus.forum.service;

import com.campus.forum.common.PageResult;
import com.campus.forum.entity.SysUser;

import java.math.BigDecimal;
import java.util.Map;

public interface UserCenterService {

    Map<String, Object> getUserInfo(Long userId);

    Map<String, Object> getPublicUserProfile(Long currentUserId, Long targetUserId);

    Map<String, Object> updateUserInfo(Long userId, SysUser update);

    PageResult<Map<String, Object>> getMyPublishes(Long userId, String type, String keyword, Long current, Long size);

    PageResult<Map<String, Object>> getMyFollows(Long userId, Long current, Long size);

    void followUser(Long userId, Long followUserId);

    void unfollowUser(Long userId, Long followUserId);

    PageResult<Map<String, Object>> getMyEvaluations(Long userId, Long current, Long size);

    Map<String, Object> getMyStats(Long userId);

    Map<String, Object> rechargeWallet(Long userId, BigDecimal amount);
}
