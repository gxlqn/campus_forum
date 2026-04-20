package com.campus.forum.service;

import com.campus.forum.common.PageResult;
import com.campus.forum.entity.ServiceHelpRequest;

public interface HelpService {

    PageResult<ServiceHelpRequest> getHelpList(Long current, Long size, Integer type, String keyword);

    ServiceHelpRequest getHelpDetail(Long id);

    ServiceHelpRequest publishHelp(ServiceHelpRequest request);

    void acceptHelp(Long id, Long helperId);

    void completeOrder(Long id, Long userId);

    PageResult<ServiceHelpRequest> getAdminHelpList(Long current, Long size, Integer type, Integer status, Integer auditStatus, String keyword);

    void auditHelpRequest(Long id, Integer auditStatus);

    void publisherCancelOrder(Long id, Long userId);

    void assignHelperManually(Long requestId, Long helperId, Long userId);

    void publisherConfirm(Long id, Long userId, Integer isComplaint);

    void helperAppeal(Long id, Long userId);

    PageResult<ServiceHelpRequest> getArbitrationList(Integer pageNo, Integer size);

    void resolveArbitration(Long id, Integer winner);
}
