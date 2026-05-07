package com.campus.forum.controller;

import com.campus.forum.common.PageResult;
import com.campus.forum.common.Result;
import com.campus.forum.dto.admin.StatusUpdateRequest;
import com.campus.forum.service.AdminInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/info")
public class AdminInfoController {

    @Autowired
    private AdminInfoService adminInfoService;

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('info:news')")
    @GetMapping("/news")
    public Result<PageResult<Map<String, Object>>> getNewsList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(adminInfoService.getNewsList(current, size, category, keyword, status));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('info:news')")
    @GetMapping("/news/{id}")
    public Result<Map<String, Object>> getNewsDetail(@PathVariable Long id) {
        return Result.success(adminInfoService.getNewsDetail(id));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('info:news')")
    @PostMapping("/news")
    public Result<Map<String, Object>> createNews(@RequestBody Map<String, Object> payload) {
        Long id = adminInfoService.createNews(payload);
        return Result.success(Map.of("id", id));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('info:news')")
    @PutMapping("/news/{id}")
    public Result<Void> updateNews(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        adminInfoService.updateNews(id, payload);
        return Result.success();
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('info:news')")
    @PatchMapping("/news/{id}/status")
    public Result<Void> updateNewsStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        adminInfoService.updateNewsStatus(id, request.getStatus());
        return Result.success();
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('info:news')")
    @DeleteMapping("/news/{id}")
    public Result<Void> deleteNews(@PathVariable Long id) {
        adminInfoService.deleteNews(id);
        return Result.success();
    }

    /**
     * 基础数据查询：新闻分类列表（所有管理角色（含各类版主）可访问，作为业务模块的依赖数据）
     */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATOR','MODERATOR_FORUM','MODERATOR_MARKET','MODERATOR_LOSTFOUND','MODERATOR_ACTIVITY','MODERATOR_HELP','MODERATOR_INFO')")
    @GetMapping("/news/categories")
    public Result<List<String>> getNewsCategories() {
        return Result.success(adminInfoService.getNewsCategories());
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('info:nav')")
    @GetMapping("/nav")
    public Result<PageResult<Map<String, Object>>> getNavList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(adminInfoService.getNavList(current, size, category, keyword, status));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('info:nav')")
    @PostMapping("/nav")
    public Result<Map<String, Object>> createNav(@RequestBody Map<String, Object> payload) {
        Long id = adminInfoService.createNav(payload);
        return Result.success(Map.of("id", id));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('info:nav')")
    @PutMapping("/nav/{id}")
    public Result<Void> updateNav(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        adminInfoService.updateNav(id, payload);
        return Result.success();
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('info:nav')")
    @PatchMapping("/nav/{id}/status")
    public Result<Void> updateNavStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        adminInfoService.updateNavStatus(id, request.getStatus());
        return Result.success();
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('info:nav')")
    @DeleteMapping("/nav/{id}")
    public Result<Void> deleteNav(@PathVariable Long id) {
        adminInfoService.deleteNav(id);
        return Result.success();
    }

    /**
     * 基础数据查询：导航分类列表（所有管理角色（含各类版主）可访问，作为业务模块的依赖数据）
     */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MODERATOR','MODERATOR_FORUM','MODERATOR_MARKET','MODERATOR_LOSTFOUND','MODERATOR_ACTIVITY','MODERATOR_HELP','MODERATOR_INFO')")
    @GetMapping("/nav/categories")
    public Result<List<String>> getNavCategories() {
        return Result.success(adminInfoService.getNavCategories());
    }
}