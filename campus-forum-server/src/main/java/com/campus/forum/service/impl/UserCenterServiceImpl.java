package com.campus.forum.service.impl;

import com.campus.forum.common.PageResult;
import com.campus.forum.common.ResultCode;
import com.campus.forum.entity.SysUser;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.MessageMapper;
import com.campus.forum.mapper.SysUserMapper;
import com.campus.forum.mapper.UserCenterMapper;
import com.campus.forum.mapper.UserWalletRecordMapper;
import com.campus.forum.service.UserCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserCenterServiceImpl implements UserCenterService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private UserCenterMapper userCenterMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private UserWalletRecordMapper userWalletRecordMapper;

    @Override
    public Map<String, Object> getUserInfo(Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.PARAM_MISSING);
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null || (user.getDeleted() != null && user.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return toUserInfoMap(user);
    }

    @Override
    public Map<String, Object> getPublicUserProfile(Long currentUserId, Long targetUserId) {
        if (targetUserId == null) {
            throw new BusinessException(ResultCode.PARAM_MISSING);
        }
        SysUser targetUser = userMapper.selectById(targetUserId);
        if (targetUser == null || (targetUser.getDeleted() != null && targetUser.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("id", targetUser.getId());
        profile.put("nickname", targetUser.getNickname());
        profile.put("username", targetUser.getUsername());
        profile.put("avatar", targetUser.getAvatar());
        profile.put("gender", targetUser.getGender());
        profile.put("bio", targetUser.getBio());
        profile.put("college", targetUser.getCollege());
        profile.put("major", targetUser.getMajor());
        profile.put("grade", targetUser.getGrade());
        profile.put("userType", targetUser.getUserType());
        profile.put("isVerified", targetUser.getIsVerified());
        profile.put("creditScore", targetUser.getCreditScore() == null ? 100 : targetUser.getCreditScore());
        profile.put("createTime", targetUser.getCreateTime());

        Map<String, Object> stats = new LinkedHashMap<>();
        Long postCount = safeLong(userCenterMapper.countMyPostPublishes(targetUserId, null));
        Long productCount = safeLong(userCenterMapper.countMyProductPublishes(targetUserId, null));
        Long activityCount = safeLong(userCenterMapper.countMyActivityPublishes(targetUserId, null));
        Long helpCount = safeLong(userCenterMapper.countMyHelpPublishes(targetUserId, null));
        Long lostFoundCount = safeLong(userCenterMapper.countMyLostFoundPublishes(targetUserId, null));
        stats.put("publishCount", postCount + productCount + activityCount + helpCount + lostFoundCount);
        stats.put("postCount", postCount);
        stats.put("productCount", productCount);
        stats.put("activityCount", activityCount);
        stats.put("helpCount", helpCount);
        stats.put("lostFoundCount", lostFoundCount);
        stats.put("followingCount", safeLong(userCenterMapper.countFollowing(targetUserId)));
        stats.put("followerCount", safeLong(userCenterMapper.countFollowers(targetUserId)));
        stats.put("evaluationCount", safeLong(userCenterMapper.countMyEvaluations(targetUserId)));
        stats.put("avgRating", safeDouble(userCenterMapper.avgMyEvaluationRating(targetUserId)));
        stats.put("creditScore", targetUser.getCreditScore() == null ? 100 : targetUser.getCreditScore());
        profile.put("stats", stats);

        boolean isSelf = currentUserId != null && currentUserId.equals(targetUserId);
        profile.put("isSelf", isSelf);

        long followRelation = currentUserId == null ? 0L : safeLong(userCenterMapper.countFollowRelation(currentUserId, targetUserId));
        profile.put("isFollowing", !isSelf && followRelation > 0);
        profile.put("canMessage", !isSelf);
        profile.put("canReport", !isSelf);
        return profile;
    }

    @Override
    public Map<String, Object> updateUserInfo(Long userId, SysUser update) {
        if (userId == null || update == null) {
            throw new BusinessException(ResultCode.PARAM_MISSING);
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null || (user.getDeleted() != null && user.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (update.getNickname() != null) {
            user.setNickname(update.getNickname().trim());
        }
        if (update.getAvatar() != null) {
            user.setAvatar(update.getAvatar().trim());
        }
        if (update.getGender() != null) {
            user.setGender(update.getGender());
        }
        if (update.getPhone() != null) {
            user.setPhone(update.getPhone().trim());
        }
        if (update.getEmail() != null) {
            user.setEmail(update.getEmail().trim());
        }
        if (update.getBio() != null) {
            user.setBio(update.getBio().trim());
        }
        if (update.getCollege() != null) {
            user.setCollege(update.getCollege().trim());
        }
        if (update.getMajor() != null) {
            user.setMajor(update.getMajor().trim());
        }
        if (update.getGrade() != null) {
            user.setGrade(update.getGrade().trim());
        }
        if (!StringUtils.hasText(user.getNickname())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "昵称不能为空");
        }

        userMapper.updateUserInfo(user);
        return getUserInfo(userId);
    }

    @Override
    public PageResult<Map<String, Object>> getMyPublishes(Long userId, String type, String keyword, Long current, Long size) {
        long pageNo = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 10 : size;
        long offset = (pageNo - 1) * pageSize;
        String publishType = normalizePublishType(type);
        String searchKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;

        List<Map<String, Object>> records;
        Long total;
        switch (publishType) {
            case "post":
                records = userCenterMapper.selectMyPostPublishes(userId, searchKeyword, offset, pageSize);
                total = userCenterMapper.countMyPostPublishes(userId, searchKeyword);
                break;
            case "product":
                records = userCenterMapper.selectMyProductPublishes(userId, searchKeyword, offset, pageSize);
                total = userCenterMapper.countMyProductPublishes(userId, searchKeyword);
                break;
            case "activity":
                records = userCenterMapper.selectMyActivityPublishes(userId, searchKeyword, offset, pageSize);
                total = userCenterMapper.countMyActivityPublishes(userId, searchKeyword);
                break;
            case "help":
                records = userCenterMapper.selectMyHelpPublishes(userId, searchKeyword, offset, pageSize);
                total = userCenterMapper.countMyHelpPublishes(userId, searchKeyword);
                break;
            case "lostfound":
                records = userCenterMapper.selectMyLostFoundPublishes(userId, searchKeyword, offset, pageSize);
                total = userCenterMapper.countMyLostFoundPublishes(userId, searchKeyword);
                break;
            default:
                records = userCenterMapper.selectMyAllPublishes(userId, searchKeyword, offset, pageSize);
                total = userCenterMapper.countMyAllPublishes(userId, searchKeyword);
                break;
        }

        return new PageResult<>(pageNo, pageSize, safeLong(total), records);
    }

    @Override
    public PageResult<Map<String, Object>> getMyFollows(Long userId, Long current, Long size) {
        long pageNo = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 10 : size;
        long offset = (pageNo - 1) * pageSize;
        List<Map<String, Object>> records = userCenterMapper.selectMyFollowUsers(userId, offset, pageSize);
        Long total = userCenterMapper.countMyFollowUsers(userId);
        return new PageResult<>(pageNo, pageSize, safeLong(total), records);
    }

    @Override
    public void followUser(Long userId, Long followUserId) {
        if (userId == null || followUserId == null) {
            throw new BusinessException(ResultCode.PARAM_MISSING);
        }
        if (userId.equals(followUserId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不能关注自己");
        }
        SysUser target = userMapper.selectById(followUserId);
        if (target == null || (target.getDeleted() != null && target.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "被关注用户不存在");
        }
        int changed = userCenterMapper.insertFollow(userId, followUserId);
        if (changed > 0) {
            SysUser me = userMapper.selectById(userId);
            String nickname = me == null ? "新用户" : me.getNickname();
            if (!StringUtils.hasText(nickname) && me != null) {
                nickname = me.getUsername();
            }
            messageMapper.insertNotification(
                    followUserId,
                    userId,
                    5,
                    "你有新的关注",
                    nickname + " 关注了你",
                    null,
                    null);
        }
    }

    @Override
    public void unfollowUser(Long userId, Long followUserId) {
        if (userId == null || followUserId == null) {
            throw new BusinessException(ResultCode.PARAM_MISSING);
        }
        userCenterMapper.deleteFollow(userId, followUserId);
    }

    @Override
    public PageResult<Map<String, Object>> getMyEvaluations(Long userId, Long current, Long size) {
        long pageNo = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 10 : size;
        long offset = (pageNo - 1) * pageSize;
        List<Map<String, Object>> records = userCenterMapper.selectMyEvaluations(userId, offset, pageSize);
        Long total = userCenterMapper.countMyEvaluations(userId);
        return new PageResult<>(pageNo, pageSize, safeLong(total), records);
    }

    @Override
    public Map<String, Object> getMyStats(Long userId) {
        Map<String, Object> data = new LinkedHashMap<>();

        Long postCount = safeLong(userCenterMapper.countMyPostPublishes(userId, null));
        Long productCount = safeLong(userCenterMapper.countMyProductPublishes(userId, null));
        Long activityCount = safeLong(userCenterMapper.countMyActivityPublishes(userId, null));
        Long helpCount = safeLong(userCenterMapper.countMyHelpPublishes(userId, null));
        Long lostFoundCount = safeLong(userCenterMapper.countMyLostFoundPublishes(userId, null));

        data.put("publishCount", postCount + productCount + activityCount + helpCount + lostFoundCount);
        data.put("postCount", postCount);
        data.put("productCount", productCount);
        data.put("activityCount", activityCount);
        data.put("helpCount", helpCount);
        data.put("lostFoundCount", lostFoundCount);

        data.put("favoritePostCount", safeLong(userCenterMapper.countMyPostFavorites(userId)));
        data.put("followingCount", safeLong(userCenterMapper.countFollowing(userId)));
        data.put("followerCount", safeLong(userCenterMapper.countFollowers(userId)));
        data.put("evaluationCount", safeLong(userCenterMapper.countMyEvaluations(userId)));
        data.put("avgRating", safeDouble(userCenterMapper.avgMyEvaluationRating(userId)));

        data.put("unreadNotificationCount", safeLong(messageMapper.countUnreadNotifications(userId)));
        data.put("unreadChatCount", safeLong(messageMapper.countUnreadConversations(userId)));
        return data;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> rechargeWallet(Long userId, BigDecimal amount) {
        if (userId == null || amount == null) {
            throw new BusinessException(ResultCode.PARAM_MISSING);
        }
        if (amount.signum() <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "充值金额必须大于0");
        }
        BigDecimal normalized = amount.setScale(2, java.math.RoundingMode.HALF_UP);
        int changed = userMapper.updateBalance(userId, normalized);
        if (changed == 0) {
            throw new BusinessException(ResultCode.ERROR, "充值失败，请稍后重试");
        }
        com.campus.forum.entity.UserWalletRecord record = new com.campus.forum.entity.UserWalletRecord();
        record.setUserId(userId);
        record.setAmount(normalized);
        record.setType(1);
        record.setRelationId(null);
        userWalletRecordMapper.insert(record);
        return getUserInfo(userId);
    }

    private Map<String, Object> toUserInfoMap(SysUser user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("openid", user.getOpenid());
        map.put("studentId", user.getStudentId());
        map.put("username", user.getUsername());
        map.put("nickname", user.getNickname());
        map.put("avatar", user.getAvatar());
        map.put("gender", user.getGender());
        map.put("phone", user.getPhone());
        map.put("email", user.getEmail());
        map.put("bio", user.getBio());
        map.put("college", user.getCollege());
        map.put("major", user.getMajor());
        map.put("grade", user.getGrade());
        map.put("userType", user.getUserType());
        map.put("status", user.getStatus());
        map.put("isVerified", user.getIsVerified());
        map.put("balance", user.getBalance() == null ? BigDecimal.ZERO : user.getBalance());
        map.put("creditScore", user.getCreditScore() == null ? 100 : user.getCreditScore());
        map.put("createTime", user.getCreateTime());
        map.put("updateTime", user.getUpdateTime());
        return map;
    }

    private String normalizePublishType(String type) {
        if (!StringUtils.hasText(type)) {
            return "all";
        }
        String value = type.trim().toLowerCase();
        switch (value) {
            case "all":
            case "post":
            case "product":
            case "activity":
            case "help":
            case "lostfound":
                return value;
            default:
                throw new BusinessException(ResultCode.PARAM_ERROR, "不支持的发布类型");
        }
    }

    private Long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private Double safeDouble(Double value) {
        return value == null ? 0D : value;
    }
}
