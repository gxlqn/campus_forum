const fs = require('fs');
let c = fs.readFileSync('D:/graduationProject/campus-forum/campus-forum-server/src/main/java/com/campus/forum/service/impl/HelpServiceImpl.java', 'utf8');

c = c.replace(/throw\s+new\s+BusinessException\(ResultCode\.PARAM_ERROR,\s*"([^"]*?)[^\n\r]*\n/g, 'throw new BusinessException(ResultCode.PARAM_ERROR, "Invalid Param");\n');
c = c.replace(/log\.warn\("([^"]*)[^\n\r]*\n/g, 'log.warn("Warning");\n');
c = c.replace(/throw\s+new\s+BusinessException\(ResultCode\.CONTENT_AUDIT_BLOCKED,\s*[^)]*\);\n/g, 'throw new BusinessException(ResultCode.CONTENT_AUDIT_BLOCKED, "Blocked");\n');
// Also getArbitrationListXYXZ bug
c = c.replace(/public com\.campus\.forum\.common\.PageResult<ServiceHelpRequest> getArbitrationListXYXZ[^\{]*\{[^\}]*\}/g, '');

fs.writeFileSync('D:/graduationProject/campus-forum/campus-forum-server/src/main/java/com/campus/forum/service/impl/HelpServiceImpl.java', c, 'utf8');
console.log('Processed');
