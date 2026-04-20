/**
 * 帖子详情页
 */
const api = require('../../../utils/api');
const imageHelper = require('../../../utils/imageHelper');

// 获取应用实例
const app = getApp();

Page({
    data: {
        postId: null,
        post: null,
        comments: [],
        commentContent: '',
        replyTo: null,
        loading: false,
        commentPage: 1,
        hasMoreComments: true,
        liking: false,
        favoriting: false,
        submittingComment: false,
        canSubmitComment: false,
        showReportDialog: false,
        currentUserInfo: {},
        pageLoading: false,
        initedAt: 0
    },

    onLoad(options) {
        this.setData({ postId: options.id });
        this.initializePage();
    },

    // 页面显示时刷新数据（更新浏览量）
    onShow() {
        // 避免 onLoad 后立刻触发 onShow 造成重复并发请求
        if (this.data.postId) {
            if (this.data.initedAt && Date.now() - this.data.initedAt < 1200) {
                return;
            }
            this.refreshPostDetail();
            this.loadComments(true);
        }
    },

    onPullDownRefresh() {
        Promise.allSettled([
            this.loadPostDetail(),
            this.loadComments(true)
        ]).finally(() => {
            wx.stopPullDownRefresh();
        });
    },

    async initializePage() {
        await Promise.allSettled([
            this.loadPostDetail(),
            this.loadComments(true),
            this.loadCurrentUserInfo()
        ]);
        this.setData({ initedAt: Date.now() });
    },

    onReachBottom() {
        if (this.data.hasMoreComments && !this.data.loading) {
            this.loadComments(false);
        }
    },

    /**
     * 安全解析图片数据
     * @param {string|array} images - 图片数据
     * @returns {array} 图片URL数组
     */
    safeParseImages(images) {
        return imageHelper.getImageList(images);
    },

    /**
     * 处理帖子数据中的图片
     * @param {object} post - 帖子对象
     * @returns {object} 处理后的帖子对象
     */
    processPostImages(post) {
        if (!post) return post;

        const imageList = imageHelper.getImageList(post.images);

        const authorAvatar = imageHelper.getFullImageUrl(post.author?.avatar);

        const sectionIcon = post.section?.icon ? imageHelper.getFullImageUrl(post.section.icon) : null;
        
        // 预处理格式化的数字（WXML无法直接调用方法）
        const viewCountFormatted = this.formatCount(post.viewCount);
        const commentCountFormatted = this.formatCount(post.commentCount);
        const likeCountFormatted = this.formatCount(post.likeCount);
        const favoriteCountFormatted = this.formatCount(post.favoriteCount);
        
        return {
            ...post,
            imageList: imageList,
            images: imageList,
            viewCountFormatted: viewCountFormatted,
            commentCountFormatted: commentCountFormatted,
            likeCountFormatted: likeCountFormatted,
            favoriteCountFormatted: favoriteCountFormatted,
            author: {
                ...post.author,
                avatar: authorAvatar,
                nickname: post.author?.nickname || '匿名用户'
            },
            section: {
                ...post.section,
                icon: sectionIcon,
                sectionName: post.section?.sectionName || '未知板块'
            }
        };
    },

    // ========== 图片预下载方案（已回退，使用 urlCheck:false）==========
    /*
    async downloadImageToLocal(url, index, field = 'image') {
        if (!url || url.startsWith('/static/') || url.startsWith('wxfile://')) {
            return url;
        }
        
        try {
            console.log(`开始下载图片[${field}-${index}]:`, url);
            
            const res = await new Promise((resolve, reject) => {
                wx.downloadFile({
                    url: url,
                    success: (res) => {
                        if (res.statusCode === 200) {
                            resolve(res.tempFilePath);
                        } else {
                            reject(new Error(`HTTP ${res.statusCode}`));
                        }
                    },
                    fail: (err) => reject(err)
                });
            });
            
            console.log(`图片下载成功[${field}-${index}]:`, res);
            
            const postData = this.data.post;
            if (!postData) return res;
            
            if (field === 'image' && postData.localImageList && index < postData.localImageList.length) {
                postData.localImageList[index] = res;
                this.setData({ 
                    [`post.localImageList[${index}]`]: res,
                    post: postData 
                });
            } else if (field === 'avatar') {
                postData.author.localAvatar = res;
                this.setData({ 
                    ['post.author.localAvatar']: res,
                    post: postData 
                });
            }
            
            return res;
        } catch (err) {
            console.error(`图片下载失败[${field}-${index}]:`, err);
            const defaultImg = field === 'avatar' 
                ? '/static/images/default-avatar.png' 
                : '/static/images/default-image.png';
                
            if (field === 'image') {
                const postData = this.data.post;
                if (postData?.localImageList && index < postData.localImageList.length) {
                    postData.localImageList[index] = defaultImg;
                    this.setData({ 
                        [`post.localImageList[${index}]`]: defaultImg,
                        post: postData 
                    });
                }
            } else if (field === 'avatar') {
                const postData = this.data.post;
                if (postData?.author) {
                    postData.author.localAvatar = defaultImg;
                    this.setData({ 
                        ['post.author.localAvatar']: defaultImg,
                        post: postData 
                    });
                }
            }
            return defaultImg;
        }
    },

    async downloadAllImages() {
        const post = this.data.post;
        if (!post) return;
        
        console.log('开始批量下载图片...');
        const concurrencyLimit = 3;
        let currentIndex = 0;
        
        const downloadNext = async () => {
            while (currentIndex < post.imageList.length) {
                const idx = currentIndex++;
                await this.downloadImageToLocal(post.imageList[idx], idx, 'image');
            }
        };
        
        const tasks = [];
        for (let i = 0; i < Math.min(concurrencyLimit, post.imageList.length); i++) {
            tasks.push(downloadNext());
        }
        
        if (post.author?.avatar && !post.author.avatar.startsWith('/static/')) {
            tasks.push(this.downloadImageToLocal(post.author.avatar, 0, 'avatar'));
        }
        
        await Promise.all(tasks);
        console.log('所有图片下载完成');
    },
    */

    // 加载帖子详情（首次加载，显示loading）
    async loadPostDetail() {
        try {
            this.setData({ pageLoading: true });
            wx.showLoading({ title: '加载中...' });
            
            const res = await api.getPostDetail(this.data.postId);
            const post = res.data;
            
            if (!post) {
                throw new Error('帖子不存在');
            }
            
            console.log('原始帖子数据:', post);
            console.log('原始图片字段:', post.images, '类型:', typeof post.images);
            
            // 处理帖子数据中的图片
            const processedPost = this.processPostImages(post);
            
            console.log('处理后的帖子数据:', processedPost);
            console.log('处理后的图片列表:', processedPost.imageList);
            
            // 真机调试诊断信息
            if (processedPost.imageList && processedPost.imageList.length > 0) {
                console.log('=== 图片URL诊断 ===');
                console.log('staticBaseUrl:', app.globalData?.staticBaseUrl);
                console.log('baseUrl:', app.globalData?.baseUrl);
                console.log('系统信息:', wx.getSystemInfoSync());
                processedPost.imageList.forEach((url, idx) => {
                    console.log(`图片[${idx}]:`, url);
                });
            }
            
            this.setData({ post: processedPost });
            
            wx.setNavigationBarTitle({
                title: post.section?.sectionName || '帖子详情'
            });
            
            // 注释：已回退预下载方案，使用 urlCheck:false 跳过域名检测
            // await this.downloadAllImages();
            
        } catch (err) {
            console.error('加载帖子详情失败', err);
            wx.showToast({
                title: err.message || '加载失败',
                icon: 'none'
            });
        } finally {
            wx.hideLoading();
            this.setData({ pageLoading: false });
        }
    },

    // 刷新帖子详情（静默刷新，不显示loading，不增加浏览量）
    async refreshPostDetail() {
        try {
            // 传入 incrementView=false 避免重复计算浏览量
            const res = await api.getPostDetail(this.data.postId, { incrementView: false });
            const post = res.data;
            
            if (!post) {
                return;
            }
            
            // 处理帖子数据中的图片
            const processedPost = this.processPostImages(post);
            
            // 检查浏览量是否有变化
            const oldViewCount = this.data.post?.viewCount || 0;
            const newViewCount = processedPost.viewCount || 0;
            
            if (oldViewCount !== newViewCount) {
                console.log(`浏览量更新: ${oldViewCount} -> ${newViewCount}`);
            }
            
            this.setData({ post: processedPost });
            
        } catch (err) {
            console.error('刷新帖子详情失败', err);
        }
    },

    // 加载评论
    async loadComments(refresh = false) {
        if (this.data.loading) return;

        const postId = this.data.postId;
        if (!postId) return;

        this.setData({ loading: true });

        try {
            const res = await api.getComments(postId);
            let comments = res.data || [];

            comments = comments.map(comment => ({
                ...comment,
                author: comment.author || { nickname: '匿名用户', avatar: '/static/images/default-avatar.png' },
                author: {
                    ...comment.author,
                    avatar: imageHelper.getFullImageUrl(comment.author?.avatar)
                }
            }));

            this.setData({
                comments: refresh ? comments : [...this.data.comments, ...comments],
                hasMoreComments: false,
                commentPage: 1
            });

            console.log(`加载评论成功，共 ${comments.length} 条`);
            
            // 注释：已回退预下载方案，使用 urlCheck:false
            // this.downloadCommentAvatars(comments);

        } catch (err) {
            console.error('加载评论失败', err);
            wx.showToast({
                title: err.message || '加载评论失败',
                icon: 'none',
                duration: 2000
            });
        } finally {
            this.setData({ loading: false });
        }
    },

    // ========== 评论区头像预下载方案（已回退）==========
    /*
    async downloadCommentAvatars(comments) {
        if (!comments || comments.length === 0) return;
        
        for (let i = 0; i < comments.length; i++) {
            const comment = comments[i];
            if (comment?.author?.avatar && !comment.author.avatar.startsWith('/static/')) {
                await this.downloadCommentAvatar(i, comment.author.avatar);
            }
        }
    },

    async downloadCommentAvatar(index, url) {
        try {
            const res = await new Promise((resolve, reject) => {
                wx.downloadFile({
                    url: url,
                    success: (res) => {
                        if (res.statusCode === 200) resolve(res.tempFilePath);
                        else reject(new Error(`HTTP ${res.statusCode}`));
                    },
                    fail: (err) => reject(err)
                });
            });
            
            const comments = this.data.comments;
            if (comments[index]?.author) {
                comments[index].author.localAvatar = res;
                this.setData({ [`comments[${index}].author.localAvatar`]: res });
            }
        } catch (err) {
            console.error(`评论[${index}]头像下载失败:`, err);
            this.setData({ [`comments[${index}].author.localAvatar`]: '/static/images/default-avatar.png' });
        }
    },
    */

    // 点赞（防重复优化）
    async handleLike() {
        const app = getApp();
        if (app.checkNeedLogin && app.checkNeedLogin()) return;

        const post = this.data.post;
        if (!post) return;

        if (this.data.liking) {
            console.log('点赞操作进行中，请稍候...');
            return;
        }

        this.setData({ liking: true });

        try {
            const originalIsLiked = post.isLiked;
            const originalLikeCount = post.likeCount || 0;

            // 乐观更新UI
            post.isLiked = !originalIsLiked;
            post.likeCount = originalIsLiked ? Math.max(0, originalLikeCount - 1) : originalLikeCount + 1;
            // 同步更新格式化的数字
            post.likeCountFormatted = this.formatCount(post.likeCount);
            this.setData({ post });

            // 发送请求
            if (originalIsLiked) {
                await api.unlikePost(post.id);
                console.log('取消点赞成功');
            } else {
                await api.likePost(post.id);
                console.log('点赞成功');
            }

        } catch (err) {
            console.error('点赞操作失败', err);

            // 回滚UI状态
            const currentPost = this.data.post;
            currentPost.isLiked = !currentPost.isLiked;
            currentPost.likeCount = currentPost.isLiked
                ? (currentPost.likeCount || 0) + 1
                : Math.max(0, (currentPost.likeCount || 0) - 1);
            // 同步更新格式化的数字
            currentPost.likeCountFormatted = this.formatCount(currentPost.likeCount);
            this.setData({ post: currentPost });

            wx.showToast({
                title: err.message || '操作失败，请重试',
                icon: 'none',
                duration: 2000
            });
        } finally {
            this.setData({ liking: false });
        }
    },

    // 收藏（防重复优化）
    async handleFavorite() {
        const app = getApp();
        if (app.checkNeedLogin && app.checkNeedLogin()) return;

        const post = this.data.post;
        if (!post) return;

        if (this.data.favoriting) {
            console.log('收藏操作进行中，请稍候...');
            return;
        }

        this.setData({ favoriting: true });

        try {
            const originalIsFavorited = post.isFavorited;
            const originalFavoriteCount = post.favoriteCount || 0;

            // 乐观更新UI
            post.isFavorited = !originalIsFavorited;
            post.favoriteCount = originalIsFavorited ? Math.max(0, originalFavoriteCount - 1) : originalFavoriteCount + 1;
            // 同步更新格式化的数字
            post.favoriteCountFormatted = this.formatCount(post.favoriteCount);
            this.setData({ post });

            // 发送请求
            if (originalIsFavorited) {
                await api.unfavoritePost(post.id);
                console.log('取消收藏成功');
            } else {
                await api.favoritePost(post.id);
                console.log('收藏成功');
            }

        } catch (err) {
            console.error('收藏操作失败', err);

            // 回滚UI状态
            const currentPost = this.data.post;
            currentPost.isFavorited = !currentPost.isFavorited;
            currentPost.favoriteCount = currentPost.isFavorited
                ? (currentPost.favoriteCount || 0) + 1
                : Math.max(0, (currentPost.favoriteCount || 0) - 1);
            // 同步更新格式化的数字
            currentPost.favoriteCountFormatted = this.formatCount(currentPost.favoriteCount);
            this.setData({ post: currentPost });

            wx.showToast({
                title: err.message || '操作失败，请重试',
                icon: 'none',
                duration: 2000
            });
        } finally {
            this.setData({ favoriting: false });
        }
    },

    // 输入评论
    onCommentInput(e) {
        const value = e.detail.value || '';
        const canSubmit = value.trim().length > 0;
        this.setData({ 
            commentContent: value,
            canSubmitComment: canSubmit
        });
    },

    // 发表评论（防重复优化）
    async submitComment() {
        const app = getApp();
        if (app.checkNeedLogin && app.checkNeedLogin()) return;

        const content = this.data.commentContent.trim();
        if (!content) {
            wx.showToast({ title: '请输入评论内容', icon: 'none' });
            return;
        }

        if (this.data.submittingComment) {
            console.log('评论提交中，请稍候...');
            return;
        }

        this.setData({ submittingComment: true });

        try {
            const postId = this.data.postId;
            const parentId = this.data.replyTo || null;

            await api.addComment(postId, content, parentId);
            
            console.log('评论发布成功:', content);
            
            wx.showToast({ 
                title: '评论成功', 
                icon: 'success',
                duration: 1500
            });
            
            this.setData({ commentContent: '', replyTo: null });
            
            // 更新帖子评论数
            const post = this.data.post;
            if (post) {
                post.commentCount = (post.commentCount || 0) + 1;
                // 同步更新格式化的评论数
                post.commentCountFormatted = this.formatCount(post.commentCount);
                this.setData({ post });
            }
            
            // 重新加载评论列表
            this.loadComments(true);
            
        } catch (err) {
            console.error('评论失败', err);
            wx.showToast({
                title: err.message || '评论失败，请重试',
                icon: 'none',
                duration: 2000
            });
        } finally {
            setTimeout(() => {
                this.setData({ submittingComment: false });
            }, 500);
        }
    },

    // 预览图片
    previewImage(e) {
        const { url } = e.currentTarget.dataset;
        const imageList = this.data.post?.imageList || [];
        
        if (!imageList.length) return;
        
        wx.previewImage({
            current: url,
            urls: imageList
        });
    },

    // 用户头像点击 - 跳转到用户公开信息页
    onAvatarTap(e) {
        const userId = e.currentTarget.dataset.userId;
        const isAnonymous = !!e.currentTarget.dataset.anonymous;
        if (isAnonymous) {
            wx.showToast({ title: '匿名用户不可查看', icon: 'none' });
            return;
        }
        if (userId) {
            wx.navigateTo({
                url: `/pages/user/public/public?userId=${userId}`
            });
        }
    },

    // 图片加载失败处理
    onImageError(e) {
        const { field, index } = e.currentTarget.dataset;
        const { post } = this.data;
        
        console.error('图片加载失败:', {
            field,
            index,
            detail: e.detail
        });
        
        if (field === 'avatar' && post?.author) {
            console.log('头像加载失败，使用默认头像');
            post.author.avatar = '/static/images/default-avatar.png';
            this.setData({ post });
        } else if (field === 'image' && index !== undefined && post?.imageList) {
            console.log(`图片[${index}]加载失败:`, post.imageList[index]);
            post.imageList[index] = '/static/images/default-image.png';
            this.setData({ post });
        }
    },

    // 分享
    onShareAppMessage() {
        const post = this.data.post;
        return {
            title: post?.title || '分享帖子',
            path: `/pages/forum/detail/detail?id=${this.data.postId}`,
            imageUrl: post?.imageList?.[0] || '/static/images/share.png'
        };
    },

    // 分享到朋友圈
    onShareTimeline() {
        const post = this.data.post;
        return {
            title: post?.title || '分享帖子',
            query: `id=${this.data.postId}`,
            imageUrl: post?.imageList?.[0] || '/static/images/share.png'
        };
    },

    // 举报
    onShowReport() {
        const app = getApp();
        if (app.checkNeedLogin && app.checkNeedLogin()) return;
        this.selectComponent('#reportDialog').show(1, this.data.postId);
    },

    async loadCurrentUserInfo() {
        try {
            const res = await api.getUserInfo();
            this.setData({ currentUserInfo: res.data || {} });
        } catch (e) { /* 静默 */ }
    },

    /**
     * @param {number} num - 数字
     * @returns {string} 格式化后的字符串
     */
    formatCount(num) {
        if (num === null || num === undefined || isNaN(num)) return '0';
        
        num = Number(num);
        
        if (num >= 100000000) {
            return (num / 100000000).toFixed(1) + '亿';
        } else if (num >= 10000) {
            return (num / 10000).toFixed(1) + 'w';
        } else if (num >= 1000) {
            return (num / 1000).toFixed(1) + 'k';
        } else {
            return String(num);
        }
    }
});