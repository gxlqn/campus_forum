package com.campus.forum.service;

import com.campus.forum.common.PageResult;
import com.campus.forum.entity.ServiceActivity;

public interface ActivityService {

    PageResult<ServiceActivity> getActivityList(Long current, Long size, String type, String keyword);

    ServiceActivity getActivityDetail(Long id, Long userId);

    ServiceActivity createActivity(ServiceActivity activity);

    void signupActivity(Long activityId, Long userId);

    void cancelSignup(Long activityId, Long userId);

    PageResult<ServiceActivity> getAdminActivityList(Long current, Long size, String type, Integer status, Integer auditStatus, String keyword);

    void auditActivity(Long id, Integer auditStatus);
}
