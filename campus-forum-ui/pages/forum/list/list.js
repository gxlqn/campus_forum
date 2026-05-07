/**
 * Forum Post List Page
 */
const api = require('../../../utils/api');
const imageHelper = require('../../../utils/imageHelper');

const FORUM_LIST_REFRESH_KEY = 'refresh_forum_list';
const FORUM_SECTION_TARGET_KEY = 'forum_section_target';

Page({
    data: {
        sectionId: '',
        sectionName: '',
        sections: [],
        posts: [],
        currentSection: '',
        orderBy: 'latest',
        page: 1,
        size: 10,
        hasMore: true,
        loading: true,
        searchKeyword: ''
    },

    onLoad(options) {
        let initialSectionId;
        if (options.sectionId) {
            const sectionId = Number(options.sectionId);
            const sectionName = decodeURIComponent(options.sectionName || '');
            this.setData({
                sectionId: Number.isNaN(sectionId) ? '' : sectionId,
                currentSection: Number.isNaN(sectionId) ? '' : sectionId,
                sectionName
            });
            initialSectionId = Number.isNaN(sectionId) ? '' : sectionId;
            if (sectionName) {
                wx.setNavigationBarTitle({
                    title: sectionName
                });
            }
        }

        // 优先消费首页写入的板块目标，避免 onLoad 与 onShow 双请求竞态覆盖筛选结果。
        const targetSectionId = this.applySectionTargetFromStorage();
        if (targetSectionId !== undefined) {
            initialSectionId = targetSectionId;
        }

        this.loadSections();
        this.loadPosts(true, initialSectionId);
    },

    onShow() {
        const targetSectionId = this.applySectionTargetFromStorage();
        if (targetSectionId !== undefined) {
            this.loadPosts(true, targetSectionId);
            return;
        }

        const needRefresh = wx.getStorageSync(FORUM_LIST_REFRESH_KEY);
        if (needRefresh) {
            wx.removeStorageSync(FORUM_LIST_REFRESH_KEY);
            this.loadPosts(true);
        }
    },

    onPullDownRefresh() {
        this.loadPosts(true).finally(() => {
            wx.stopPullDownRefresh();
        });
    },

    onReachBottom() {
        if (this.data.hasMore && !this.data.loading) {
            this.loadPosts(false);
        }
    },

    async loadSections() {
        try {
            const res = await api.getSections();
            if (res.data) {
                this.setData({ sections: res.data });
            }
        } catch (err) {
            console.error('Load sections failed', err);
        }
    },

    async loadPosts(refresh, sectionIdOverride) {
        if (refresh) {
            this.setData({ page: 1, hasMore: true });
        }

        this.setData({ loading: true });

        try {
            const requestSectionId = sectionIdOverride !== undefined ? sectionIdOverride : this.data.currentSection;
            const res = await api.getPostList({
                sectionId: requestSectionId,
                orderBy: this.data.orderBy,
                keyword: this.data.searchKeyword || undefined,
                current: this.data.page,
                size: this.data.size
            });

            if (res.data) {
                const records = res.data.records || res.data;
                const processedPosts = records.map(post => ({
                    ...post,
                    author: {
                        ...post.author,
                        avatar: imageHelper.getFullImageUrl(post.author?.avatar)
                    }
                }));
                const newPosts = refresh ? processedPosts : [...this.data.posts, ...processedPosts];

                this.setData({
                    posts: newPosts,
                    page: this.data.page + 1,
                    hasMore: records.length >= this.data.size
                });
            }
        } catch (err) {
            console.error('Load posts failed', err);
        }

        this.setData({ loading: false });
    },

    onSectionTap(e) {
        const id = e.currentTarget.dataset.id;
        const sectionId = id === '' || id === undefined || id === null ? '' : Number(id);
        const nextSectionId = Number.isNaN(sectionId) ? '' : sectionId;
        this.setData({ currentSection: nextSectionId, searchKeyword: '' });
        this.loadPosts(true, nextSectionId);
    },

    applySectionTargetFromStorage() {
        const target = wx.getStorageSync(FORUM_SECTION_TARGET_KEY);
        if (!target || !target.sectionId) {
            return undefined;
        }
        const sectionId = Number(target.sectionId);
        if (Number.isNaN(sectionId) || !sectionId) {
            wx.removeStorageSync(FORUM_SECTION_TARGET_KEY);
            return undefined;
        }
        const sectionName = target.sectionName || '';
        this.setData({
            sectionId,
            currentSection: sectionId,
            sectionName
        });
        if (sectionName) {
            wx.setNavigationBarTitle({ title: sectionName });
        }
        wx.removeStorageSync(FORUM_SECTION_TARGET_KEY);
        return sectionId;
    },

    onOrderTap(e) {
        const order = e.currentTarget.dataset.order;
        if (order !== this.data.orderBy) {
            this.setData({ orderBy: order });
            this.loadPosts(true);
        }
    },

    onSearch(e) {
        const keyword = (e.detail.value || '').trim();
        this.setData({ searchKeyword: keyword });
        this.loadPosts(true);
    },

    onSearchInput(e) {
        this.setData({ searchKeyword: e.detail.value || '' });
    },

    onClearSearch() {
        this.setData({ searchKeyword: '' });
        this.loadPosts(true);
    },

    goToDetail(e) {
        const id = e.currentTarget.dataset.id;
        wx.navigateTo({
            url: `/pages/forum/detail/detail?id=${id}`
        });
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

    goToPublish() {
        wx.navigateTo({
            url: `/pages/forum/publish/publish?sectionId=${this.data.currentSection || ''}`
        });
    }
});
