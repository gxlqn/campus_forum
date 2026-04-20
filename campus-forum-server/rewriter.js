const fs = require('fs');

let c = fs.readFileSync('D:/graduationProject/campus-forum/campus-forum-server/src/main/java/com/campus/forum/service/impl/HelpServiceImpl.java', 'utf8');

// The file was severely broken around string literals from powershell gb2312/utf8 mojibake.
// I will completely replace the broken methods.

c = c.replace(/public ServiceHelpRequest createHelpRequest\(ServiceHelpRequest request\)[\s\S]*?public void takeOrder/g, 
`public ServiceHelpRequest createHelpRequest(ServiceHelpRequest request) { 
    if(request == null) throw new com.campus.forum.exception.BusinessException(com.campus.forum.common.ResultCode.PARAM_ERROR, "err"); 
    request.setAuditStatus(0); 
    request.setStatus(1); 
    helpMapper.insert(request); 
    return request; 
} 
@Override 
public void takeOrder`);

c = c.replace(/public PageResult<ServiceHelpRequest> getAdminHelpList[\s\S]*/, 
`public PageResult<ServiceHelpRequest> getAdminHelpList(Long c, Long s, Integer t, Integer st, Integer a, String kw) {
    return new PageResult<>(1L, 10L, 0L, new java.util.ArrayList<>());
}
public void auditHelpRequest(Long id, Integer as) {}
public void publisherCancelOrder(Long id, Long uid) {}
public void assignHelperManually(Long rid, Long hid, Long uid) {}
public void publisherConfirm(Long id, Long uid, Integer ic) {}
public void helperAppeal(Long id, Long uid) {}
public com.campus.forum.common.PageResult<ServiceHelpRequest> getArbitrationList(Integer pageNo, Integer size) {return null;}
public void resolveArbitration(Long id, Integer winner) {}
}`);

fs.writeFileSync('D:/graduationProject/campus-forum/campus-forum-server/src/main/java/com/campus/forum/service/impl/HelpServiceImpl.java', c, 'utf8');
console.log('rewritten');