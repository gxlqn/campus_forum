package com.campus.forum.service.impl;

import com.campus.forum.mapper.ForumPostMapper;
import com.campus.forum.mapper.ServiceActivityMapper;
import com.campus.forum.mapper.ServiceHelpRequestMapper;
import com.campus.forum.mapper.ServiceLostFoundMapper;
import com.campus.forum.mapper.ServiceProductMapper;
import com.campus.forum.mapper.SysUserMapper;
import com.campus.forum.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private ForumPostMapper forumPostMapper;

    @Autowired
    private ServiceProductMapper productMapper;

    @Autowired
    private ServiceActivityMapper activityMapper;

    @Autowired
    private ServiceLostFoundMapper lostFoundMapper;

    @Autowired
    private ServiceHelpRequestMapper helpRequestMapper;

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new HashMap<>();
        overview.put("userCount", userMapper.countAll());
        overview.put("postCount", forumPostMapper.countAll());
        overview.put("productCount", productMapper.countAll());
        overview.put("activityCount", activityMapper.countAll());
        overview.put("lostFoundCount", lostFoundMapper.countAll());
        overview.put("helpRequestCount", helpRequestMapper.countAll());
        return overview;
    }

    @Override
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> userStats = new HashMap<>();
        userStats.put("totalUsers", userMapper.countAll());
        userStats.put("activeUsers", userMapper.countActiveUsers());
        userStats.put("newUsersToday", userMapper.countNewUsersToday());
        return userStats;
    }

    @Override
    public Map<String, Object> getServiceStatistics() {
        Map<String, Object> serviceStats = new HashMap<>();
        serviceStats.put("totalProducts", productMapper.countAll());
        serviceStats.put("pendingProducts", productMapper.countByAuditStatus(0));
        serviceStats.put("approvedProducts", productMapper.countByAuditStatus(1));
        serviceStats.put("totalActivities", activityMapper.countAll());
        serviceStats.put("pendingActivities", activityMapper.countByAuditStatus(0));
        serviceStats.put("approvedActivities", activityMapper.countByAuditStatus(1));
        serviceStats.put("totalLostFound", lostFoundMapper.countAll());
        serviceStats.put("pendingLostFound", lostFoundMapper.countByAuditStatus(0));
        serviceStats.put("approvedLostFound", lostFoundMapper.countByAuditStatus(1));
        serviceStats.put("totalHelpRequests", helpRequestMapper.countAll());
        serviceStats.put("pendingHelpRequests", helpRequestMapper.countByAuditStatus(0));
        serviceStats.put("approvedHelpRequests", helpRequestMapper.countByAuditStatus(1));
        return serviceStats;
    }

    @Override
    public Map<String, Object> getForumStatistics() {
        Map<String, Object> forumStats = new HashMap<>();
        forumStats.put("totalPosts", forumPostMapper.countAll());
        forumStats.put("totalComments", forumPostMapper.countTotalComments());
        forumStats.put("totalLikes", forumPostMapper.countTotalLikes());
        forumStats.put("topSections", forumPostMapper.getTopSections());
        return forumStats;
    }
}