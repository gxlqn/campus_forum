package com.campus.forum.service;

import com.campus.forum.common.PageResult;
import com.campus.forum.entity.ForumComment;
import com.campus.forum.entity.ForumPost;
import com.campus.forum.entity.ForumSection;
import java.util.List;

public interface ForumService {

    List<ForumSection> getAllSections();

    PageResult<ForumPost> getPostList(Long current, Long size, Long sectionId, String keyword, String orderBy, Integer hotWindowHours);

    ForumPost getPostDetail(Long postId);

    ForumPost getPostDetail(Long postId, Long userId);
    
    ForumPost getPostDetail(Long postId, Long userId, boolean incrementView);

    ForumPost createPost(ForumPost post);

    ForumPost updatePost(ForumPost post, Long userId);

    void deletePost(Long postId, Long userId);

    void likePost(Long postId, Long userId);

    void unlikePost(Long postId, Long userId);

    void favoritePost(Long postId, Long userId);

    void unfavoritePost(Long postId, Long userId);

    PageResult<ForumPost> getMyPosts(Long userId, Long current, Long size, String keyword);

    PageResult<ForumPost> getMyFavoritePosts(Long userId, Long current, Long size);

    List<ForumComment> getCommentsByPostId(Long postId);

    ForumComment addComment(ForumComment comment);
}
