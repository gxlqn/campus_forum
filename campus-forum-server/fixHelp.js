const fs = require('fs');

const fileCode = `package com.campus.forum.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.common.Result;
import com.campus.forum.entity.ServiceHelpRequest;
import com.campus.forum.service.IHelpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/help")
public class HelpController {

    @Autowired
    private IHelpService helpService;

    @GetMapping
    public Result<IPage<ServiceHelpRequest>> getHelpList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<ServiceHelpRequest> pageParam = new Page<>(page, size);
        return Result.success(helpService.getHelpList(pageParam, keyword));
    }

    @GetMapping("/{id}")
    public Result<ServiceHelpRequest> getHelpDetail(@PathVariable Long id) {
        return Result.success(helpService.getById(id));
    }

    @PostMapping
    public Result<Boolean> publishHelp(@RequestBody ServiceHelpRequest request) {
        request.setUserId(2L); // mock publisher
        return Result.success(helpService.publishHelp(request));
    }

    @PostMapping("/{id}/accept")
    public Result<Boolean> acceptHelp(@PathVariable Long id) {
        return Result.success(helpService.acceptHelp(id, 3L)); // mock helper
    }

    @GetMapping("/admin/arbitration/list")
    public Result<IPage<ServiceHelpRequest>> getArbitrationList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<ServiceHelpRequest> pageParam = new Page<>(page, size);
        return Result.success(helpService.getArbitrationList(pageParam));
    }

    @PostMapping("/admin/arbitration/resolve")
    public Result<Boolean> resolveArbitration(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer resolution = Integer.valueOf(params.get("resolution").toString());
        return Result.success(helpService.resolveArbitration(id, resolution));
    }
}
`;

fs.writeFileSync('D:/graduationProject/campus-forum/campus-forum-server/src/main/java/com/campus/forum/controller/HelpController.java', fileCode, 'utf8');
