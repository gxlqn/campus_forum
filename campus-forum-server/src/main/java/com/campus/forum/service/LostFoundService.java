package com.campus.forum.service;

import com.campus.forum.common.PageResult;
import com.campus.forum.entity.ServiceLostFound;

public interface LostFoundService {

    PageResult<ServiceLostFound> getList(Long current, Long size, Integer type, String keyword);

    ServiceLostFound getDetail(Long id);

    ServiceLostFound create(ServiceLostFound item);

    ServiceLostFound update(ServiceLostFound item, Long userId);

    void delete(Long id, Long userId);

    void markComplete(Long id, Long userId);

    PageResult<ServiceLostFound> getAdminList(Long current, Long size, Integer type, Integer status, Integer auditStatus, String keyword);

    void auditItem(Long id, Integer auditStatus);

    void submitClaim(Long lostFoundId, Long userId, String description, String images);

    PageResult<java.util.Map<String, Object>> getClaimList(Long current, Long size, Integer status);

    void auditClaim(Long claimId, Long auditorId, Integer status, String remark);
}
