const fs = require('fs');

const fileCode = `package com.campus.forum.controller;

import com.campus.forum.common.PageResult;
import com.campus.forum.common.Result;
import com.campus.forum.entity.ServiceHelpRequest;
import com.campus.forum.service.HelpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/help")
public class HelpController {

    @Autowired
    private HelpService helpService;

    @GetMapping
    public Result<PageResult<ServiceHelpRequest>> getHelpList(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String keyword) {
        return Result.success(helpService.getHelpList(page, size, type, keyword));
    }

    @GetMapping("/{id}")
    public Result<ServiceHelpRequest> getHelpDetail(@PathVariable Long id) {
        return Result.success(helpService.getHelpDetail(id));
    }

    @PostMapping
    public Result<ServiceHelpRequest> publishHelp(@RequestBody ServiceHelpRequest request) {
        // mock user ID if missing
        if(request.getPublisherId() == null) request.setPublisherId(2L);
        return Result.success(helpService.createHelpRequest(request));
    }

    @PostMapping("/{id}/accept")
    public Result<Boolean> acceptHelp(@PathVariable Long id) {
        helpService.takeOrder(id, 3L); // mock helper ID
        return Result.success(true);
    }

    @GetMapping("/admin/arbitration/list")
    public Result<PageResult<ServiceHelpRequest>> getArbitrationList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(helpService.getArbitrationList(page, size));
    }

    @PostMapping("/admin/arbitration/resolve")
    public Result<Boolean> resolveArbitration(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer resolution = Integer.valueOf(params.get("resolution").toString());
        helpService.resolveArbitration(id, resolution);
        return Result.success(true);
    }
}
`;

fs.writeFileSync('D:/graduationProject/campus-forum/campus-forum-server/src/main/java/com/campus/forum/controller/HelpController.java', fileCode, 'utf8');

// Also fix HelpService duplicate arbitration declarations
let helpSvc = fs.readFileSync('D:/graduationProject/campus-forum/campus-forum-server/src/main/java/com/campus/forum/service/HelpService.java', 'utf8');
helpSvc = helpSvc.replace(/com\.campus\.forum\.common\.PageResult<ServiceHelpRequest> getArbitrationList\(Integer pageNo, Integer size\);\s*void resolveArbitration\(Long id, Integer winner\);/g, '');
helpSvc += `\n    com.campus.forum.common.PageResult<ServiceHelpRequest> getArbitrationList(Integer pageNo, Integer size);\n    void resolveArbitration(Long id, Integer winner);\n}\n`;
helpSvc = helpSvc.replace(/\}\s*com\.campus\.forum\.common\.PageResult/, 'com.campus.forum.common.PageResult'); // remove duplicate trailing braces if any
fs.writeFileSync('D:/graduationProject/campus-forum/campus-forum-server/src/main/java/com/campus/forum/service/HelpService.java', helpSvc, 'utf8');

// Fix HelpServiceImpl mangling
let impl = fs.readFileSync('D:/graduationProject/campus-forum/campus-forum-server/src/main/java/com/campus/forum/service/impl/HelpServiceImpl.java', 'utf8');
impl = impl.replace(/@org.springframework.web.bind.annotation.GetMapping\(""/g, '');
// just removing everything that could be completely malformed.
