/**
 * Homepage - 校园服务论坛首页
 */
const api = require('../../utils/api');
const imageHelper = require('../../utils/imageHelper');

const FORUM_SECTION_TARGET_KEY = 'forum_section_target';

// 获取应用实例
const app = getApp();

Page({
    data: {
        // ========== 轮播图数据 ==========
        banners: [],
        currentBanner: 0,

        quickEntries: [
            { icon: '\uD83D\uDED2', name: '二手市场', url: '/pages/service/product/list/list', isTab: true, bgClass: 'bg-amber' },
            { icon: '\uD83D\uDD0D', name: '失物招领', url: '/pages/service/lostfound/list/list', bgClass: 'bg-sky' },
            { icon: '\uD83C\uDF89', name: '活动', url: '/pages/service/activity/list/list', bgClass: 'bg-pink' },
            { icon: '\uD83E\uDD1D', name: '互助', url: '/pages/service/help/list/list', bgClass: 'bg-mint' },
            { icon: '\uD83D\uDCF0', name: '校园资讯', url: '/pages/info/news/list', bgClass: 'bg-purple' },
            { icon: '\uD83E\uDDAD', name: '服务导航', url: '/pages/info/nav/list', bgClass: 'bg-rose' }
        ],
        sections: [],
        hotPosts: [],
        loading: true
    },

    onLoad() {
        this.loadData();
    },

    onShow() {
        // Refresh on show
    },

    onPullDownRefresh() {
        this.loadData().finally(() => {
            wx.stopPullDownRefresh();
        });
    },

    /**
     * 安全解析图片数据
     */
    safeParseImages(images) {
        return imageHelper.getImageList(images);
    },

    /**
     * 加载所有首页数据（含轮播图）
     */
    async loadData() {
        this.setData({ loading: true });

        try {
            // 并行加载所有数据，单接口失败不影响其他区块渲染
            const results = await Promise.allSettled([
                this.loadBanners(),
                this.loadSections(),
                this.loadHotPosts()
            ]);

            const failedCount = results.filter(item => item.status === 'rejected').length;
            if (failedCount === results.length) {
                wx.showToast({
                    title: '加载失败',
                    icon: 'none',
                    duration: 2000
                });
            }
        } catch (err) {
            console.error('加载数据失败:', err);
        } finally {
            this.setData({ loading: false });
        }
    },

    /**
     * 加载轮播图
     * 优先从后端API获取，失败时使用静态默认数据
     */
    async loadBanners() {
        try {
            const res = await api.getBanners && await api.getBanners();
            if (res.data && res.data.length > 0) {
                const banners = res.data.map((item, idx) => ({
                    id: item.id || idx,
                    image: item.imageUrl || item.image || '',
                    title: item.title || '',
                    subtitle: item.subtitle || item.description || '',
                    linkType: item.linkType || 'page',   // page / web / miniapp / none
                    linkUrl: item.linkUrl || item.url || '',
                    appId: item.appId || '',             // 小程序跳转用
                    bgColor: item.bgColor || this.getGradientByIndex(idx),
                    priority: item.priority || 0,
                    status: item.status !== undefined ? item.status : 1
                })).filter(b => b.status === 1);

                if (banners.length > 0) {
                    this.setData({ banners });
                    console.log('轮播图加载成功:', banners.length);
                    return;
                }
            }
        } catch (e) {
            console.log('后端无轮播图接口或请求失败，使用默认轮播');
        }

        // 默认轮播图数据（纯前端）
        const defaultBanners = [
            {
                id: 1,
                image: '/static/images/banner1.png',
                title: '欢迎来到校园服务论坛',
                subtitle: '发现校园精彩生活',
                linkType: 'page',
                linkUrl: '/pages/forum/list/list',
                bgColor: 'linear-gradient(135deg, #FF6B9D 0%, #A78BFA 100%)'
            },
            {
                id: 2,
                image: '/static/images/banner2.png',
                title: '二手好物等你淘',
                subtitle: '闲置物品循环利用',
                linkType: 'page',
                linkUrl: '/pages/service/product/list/list',
                bgColor: 'linear-gradient(135deg, #60A5FA 0%, #34D399 100%)'
            },
            {
                id: 3,
                image: '/static/images/banner1.png',
                title: '校园活动火热报名中',
                subtitle: '参与活动赢取奖励',
                linkType: 'page',
                linkUrl: '/pages/service/activity/list/list',
                bgColor: 'linear-gradient(135deg, #FBBF24 0%, #F87171 100%)'
            },
            {
                id: 4,
                image: '/static/images/banner2.png',
                title: '互助有爱 温暖同行',
                subtitle: '发布需求 接单帮忙',
                linkType: 'page',
                linkUrl: '/pages/service/help/list/list',
                bgColor: 'linear-gradient(135deg, #A78BFA 0%, #EC4899 100%)'
            }
        ];

        this.setData({ banners: defaultBanners });
    },

    /**
     * 根据索引获取渐变色背景（用于默认轮播）
     */
    getGradientByIndex(index) {
        const gradients = [
            'linear-gradient(135deg, #FF6B9D 0%, #A78BFA 100%)',
            'linear-gradient(135deg, #60A5FA 0%, #34D399 100%)',
            'linear-gradient(135deg, #FBBF24 0%, #F87171 100%)',
            'linear-gradient(135deg, #A78BFA 0%, #EC4899 100%)'
        ];
        return gradients[index % gradients.length];
    },

    /**
     * 轮播图切换事件
     */
    onBannerChange(e) {
        this.setData({
            currentBanner: e.detail.current
        });
    },

    /**
     * 点击轮播图
     */
    onBannerTap(e) {
        const idx = e.currentTarget.dataset.idx;
        const banner = this.data.banners[idx];
        if (!banner) return;

        switch (banner.linkType) {
            case 'page':
                if (!banner.linkUrl) return;
                // 判断是否为 tabBar 页面
                const tabPages = [
                    '/pages/index/index/index',
                    '/pages/forum/list/list',
                    '/pages/service/service/service',
                    '/pages/message/message/message',
                    '/pages/mine/index/index'
                ];
                if (tabPages.some(t => banner.linkUrl.includes(t))) {
                    wx.switchTab({ url: banner.linkUrl.split('?')[0] });
                } else {
                    wx.navigateTo({ url: banner.linkUrl });
                }
                break;

            case 'web':
                if (banner.linkUrl) {
                    wx.navigateTo({
                        url: `/pages/webview/webview?url=${encodeURIComponent(banner.linkUrl)}`
                    });
                }
                break;

            case 'miniapp':
                if (banner.appId && banner.linkUrl) {
                    wx.navigateToMiniProgram({
                        appId: banner.appId,
                        path: banner.linkUrl
                    }).catch(() => {
                        wx.showToast({ title: '跳转失败', icon: 'none' });
                    });
                }
                break;

            case 'none':
            default:
                break;
        }
    },

    /**
     * 加载板块列表
     */
    async loadSections() {
        try {
            const sectionsRes = await api.getSections();
            if (sectionsRes.data) {
                const sections = (sectionsRes.data || []).map((section) => ({
                    ...section,
                    postCount: Number(section.postCount || 0)
                }));
                this.setData({ sections });
            }
        } catch (e) {
            console.error('板块加载失败:', e);
        }
    },

    /**
     * 加载热门帖子
     */
    async loadHotPosts() {
        try {
            const postsRes = await api.getPostList({ orderBy: 'hot', page: 1, size: 5 });

            if (postsRes.data) {
                const records = postsRes.data.records || postsRes.data || [];
                const posts = records.map(post => ({
                    ...post,
                    images: this.safeParseImages(post.images),
                    author: {
                        ...post.author,
                        avatar: imageHelper.getFullImageUrl(post.author?.avatar),
                        nickname: post.author?.nickname || '匿名用户'
                    },
                    section: {
                        ...post.section,
                        sectionName: post.section?.sectionName || '未知板块'
                    },
                    viewCount: post.viewCount || 0,
                    likeCount: post.likeCount || 0,
                    commentCount: post.commentCount || 0,
                    createTime: this.formatTime(post.createTime)
                }));

                this.setData({ hotPosts: posts });
            }
        } catch (e) {
            console.error('帖子加载失败:', e);
        }
    },

    /**
     * 格式化时间
     */
    formatTime(time) {
        if (!time) return '';

        try {
            const date = new Date(time);
            const now = new Date();
            const diff = now - date;

            if (diff < 60000) return '刚刚';
            if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
            if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
            if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`;
            return `${date.getMonth() + 1}-${date.getDate()}`;
        } catch (e) {
            return time;
        }
    },

    /**
     * 图片加载失败处理
     */
    onImageError(e) {
        const { index, imgindex } = e.currentTarget.dataset;
        if (index !== undefined && imgindex !== undefined) {
            const { hotPosts } = this.data;
            if (hotPosts[index] && hotPosts[index].images[imgindex]) {
                hotPosts[index].images[imgindex] = '/static/images/default-image.png';
                this.setData({ hotPosts });
            }
        }
    },

    goToSearch() {
        wx.navigateTo({
            url: '/pages/search/search'
        });
    },

    goToEntry(e) {
        const url = e.currentTarget.dataset.url;
        const isTab = e.currentTarget.dataset.istab;
        if (url) {
            if (isTab) {
                wx.switchTab({ url });
            } else {
                wx.navigateTo({ url });
            }
        }
    },

    goToSection(e) {
        const id = e.currentTarget.dataset.id;
        const name = e.currentTarget.dataset.name;
        if (id !== undefined && id !== null && id !== '') {
            wx.setStorageSync(FORUM_SECTION_TARGET_KEY, {
                sectionId: Number(id),
                sectionName: name || ''
            });
        } else {
            wx.removeStorageSync(FORUM_SECTION_TARGET_KEY);
        }
        wx.switchTab({
            url: '/pages/forum/list/list'
        });
    },

    goToPost(e) {
        const id = e.currentTarget.dataset.id;
        if (id) {
            wx.navigateTo({
                url: `/pages/forum/detail/detail?id=${id}`
            });
        }
    },

    goToUserProfile(e) {
        const userId = e.currentTarget.dataset.userId;
        if (!userId) {
            wx.showToast({ title: '用户信息缺失', icon: 'none' });
            return;
        }
        wx.navigateTo({
            url: `/pages/user/public/public?userId=${userId}`
        });
    },

    goToAllSections() {
        wx.switchTab({
            url: '/pages/forum/list/list'
        });
    }
});
