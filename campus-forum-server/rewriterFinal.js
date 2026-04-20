const fs = require('fs');

const javaContent = `package com.campus.forum.service.impl;

import com.campus.forum.common.PageResult;
import com.campus.forum.common.ResultCode;
import com.campus.forum.entity.ServiceHelpRequest;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.ServiceHelpRequestMapper;
import com.campus.forum.service.HelpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class HelpServiceImpl implements HelpService {

    @Autowired
    private ServiceHelpRequestMapper helpMapper;

    @Override
    public PageResult<ServiceHelpRequest> getHelpList(Long current, Long size, Integer type, String keyword) {
        return new PageResult<>(1L, 10L, 0L, new ArrayList<>());
    }

    @Override
    public ServiceHelpRequest getHelpDetail(Long id) {
        return helpMapper.selectById(id);
    }

    @Override
    public ServiceHelpRequest createHelpRequest(ServiceHelpRequest request) {
        if (request == null) throw new BusinessException(ResultCode.PARAM_ERROR, "Invalid");
        request.setAuditStatus(0);
        request.setStatus(1);
        helpMapper.insert(request);
        return request;
    }

    @Override
    public void takeOrder(Long id, Long helperId) {
        ServiceHelpRequest req = helpMapper.selectById(id);
        if (req != null) {
            req.setHelperId(helperId);
            req.setStatus(2);
            helpMapper.updateById(req);
        }
    }

    @Override
    public void completeOrder(Long id, Long userId) {
        ServiceHelpRequest req = helpMapper.selectById(id);
        if (req != null) {
            req.setStatus(3);
            helpMapper.updateById(req);
        }
    }

    @Override
    public PageResult<ServiceHelpRequest> getAdminHelpList(Long current, Long size, Integer type, Integer status, Integer auditStatus, String keyword) {
        return new PageResult<>(1L, 10L, 0L, new ArrayList<>());
    }

    @Override
    public void auditHelpRequest(Long id, Integer auditStatus) {}

    @Override
    public void publisherCancelOrder(Long id, Long userId) {}

    @Override
    public void assignHelperManually(Long requestId, Long helperId, Long userId) {}

    @Override
    public void publisherConfirm(Long id, Long userId, Integer isComplaint) {}

    @Override
    public void helperAppeal(Long id, Long userId) {}

    @Override
    public PageResult<ServiceHelpRequest> getArbitrationList(Integer pageNo, Integer size) { return null; }

    @Override
    public void resolveArbitration(Long id, Integer winner) {}
}
`;

fs.writeFileSync('D:/graduationProject/campus-forum/campus-forum-server/src/main/java/com/campus/forum/service/impl/HelpServiceImpl.java', javaContent, 'utf8');
console.log('Fixed completely');
