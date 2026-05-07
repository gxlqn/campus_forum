package com.campus.forum.controller;

import com.campus.forum.common.PageResult;
import com.campus.forum.common.Result;
import com.campus.forum.entity.ForumComment;
import com.campus.forum.entity.ForumPost;
import com.campus.forum.entity.ForumSection;
import com.campus.forum.entity.SysUser;
import com.campus.forum.service.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/forum")
public class ForumController {

    @Autowired
    private ForumService forumService;

    @GetMapping("/sections")
    public Result<List<ForumSection>> getSections() {
        return Result.success(forumService.getAllSections());
    }

    @GetMapping("/posts")
    public Result<PageResult<ForumPost>> getPosts(
            @RequestParam(required = false) Long current,
            @RequestParam(required = false) Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "latest") String orderBy,
            @RequestParam(required = false, defaultValue = "72") Integer hotWindowHours) {
        Long pageNo = current != null ? current : page;
        if (pageNo == null) {
            pageNo = 1L;
        }
        return Result.success(forumService.getPostList(pageNo, size, sectionId, keyword, orderBy, hotWindowHours));
    }

    @GetMapping("/posts/{id}")
    public Result<ForumPost> getPostDetail(@PathVariable Long id,
            @AuthenticationPrincipal SysUser currentUser,
            @RequestParam(defaultValue = "true") boolean incrementView) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        return Result.success(forumService.getPostDetail(id, userId, incrementView));
    }

    @PostMapping("/posts")
    public Result<ForumPost> createPost(@RequestBody ForumPost post,
            @AuthenticationPrincipal SysUser currentUser) {
        post.setUserId(currentUser.getId());
        return Result.success(forumService.createPost(post));
    }

    @PutMapping("/posts/{id}")
    public Result<ForumPost> updatePost(@PathVariable Long id,
            @RequestBody ForumPost post,
            @AuthenticationPrincipal SysUser currentUser) {
        post.setId(id);
        return Result.success(forumService.updatePost(post, currentUser.getId()));
    }

    @DeleteMapping("/posts/{id}")
    public Result<Void> deletePost(@PathVariable Long id,
            @AuthenticationPrincipal SysUser currentUser) {
        forumService.deletePost(id, currentUser.getId());
        return Result.success();
    }

    @PostMapping("/posts/{id}/like")
    public Result<Void> likePost(@PathVariable Long id,
            @AuthenticationPrincipal SysUser currentUser) {
        forumService.likePost(id, currentUser.getId());
        return Result.success();
    }

    @DeleteMapping("/posts/{id}/like")
    public Result<Void> unlikePost(@PathVariable Long id,
            @AuthenticationPrincipal SysUser currentUser) {
        forumService.unlikePost(id, currentUser.getId());
        return Result.success();
    }

    @PostMapping("/posts/{id}/favorite")
    public Result<Void> favoritePost(@PathVariable Long id,
            @AuthenticationPrincipal SysUser currentUser) {
        forumService.favoritePost(id, currentUser.getId());
        return Result.success();
    }

    @DeleteMapping("/posts/{id}/favorite")
    public Result<Void> unfavoritePost(@PathVariable Long id,
            @AuthenticationPrincipal SysUser currentUser) {
        forumService.unfavoritePost(id, currentUser.getId());
        return Result.success();
    }

    @GetMapping("/my/posts")
    public Result<PageResult<ForumPost>> getMyPosts(
            @AuthenticationPrincipal SysUser currentUser,
            @RequestParam(required = false) Long current,
            @RequestParam(required = false) Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword) {
        Long pageNo = current != null ? current : page;
        if (pageNo == null) {
            pageNo = 1L;
        }
        return Result.success(forumService.getMyPosts(currentUser.getId(), pageNo, size, keyword));
    }

    @GetMapping("/my/favorites")
    public Result<PageResult<ForumPost>> getMyFavorites(
            @AuthenticationPrincipal SysUser currentUser,
            @RequestParam(required = false) Long current,
            @RequestParam(required = false) Long page,
            @RequestParam(defaultValue = "10") Long size) {
        Long pageNo = current != null ? current : page;
        if (pageNo == null) {
            pageNo = 1L;
        }
        return Result.success(forumService.getMyFavoritePosts(currentUser.getId(), pageNo, size));
    }

    @GetMapping("/posts/{postId}/comments")
    public Result<List<ForumComment>> getComments(@PathVariable Long postId) {
        return Result.success(forumService.getCommentsByPostId(postId));
    }

    @PostMapping("/posts/{postId}/comments")
    public Result<ForumComment> addComment(@PathVariable Long postId,
            @RequestBody ForumComment comment,
            @AuthenticationPrincipal SysUser currentUser) {
        comment.setPostId(postId);
        comment.setUserId(currentUser.getId());
        return Result.success(forumService.addComment(comment));
    }
}
