/**
 * 校园资讯列表页
 */
const api = require('../../../utils/api');

Page({
    data: {
        categories: ['全部'],
        currentCategory: '全部',
        keyword: '',
        list: [],
        current: 1,
        size: 10,
        hasMore: true,
        loading: false
    },

    onLoad() {
        this.loadNews(true);
    },

    onPullDownRefresh() {
        this.loadNews(true).finally(() => {
            wx.stopPullDownRefresh();
        });
    },

    onReachBottom() {
        if (this.data.hasMore && !this.data.loading) {
            this.loadNews(false);
        }
    },

    async loadNews(refresh) {
        const nextCurrent = refresh ? 1 : this.data.current;
        this.setData({ loading: true });
        try {
            const params = {
                current: nextCurrent,
                size: this.data.size
            };
            if (this.data.currentCategory && this.data.currentCategory !== '全部') {
                params.category = this.data.currentCategory;
            }
            if (this.data.keyword) {
                params.keyword = this.data.keyword;
            }

            const res = await api.getNewsList(params);
            const data = res.data || {};
            const page = data.page || {};
            const records = page.records || [];

            const list = refresh ? records : this.data.list.concat(records);
            const pages = page.pages || 1;
            const current = page.current || nextCurrent;
            const categories = ['全部'].concat(data.categories || []);

            this.setData({
                list,
                categories,
                current: current + 1,
                hasMore: current < pages
            });
        } catch (err) {
            console.error('加载资讯失败', err);
            wx.showToast({
                title: '加载失败',
                icon: 'none'
            });
        } finally {
            this.setData({ loading: false });
        }
    },

    onCategoryTap(e) {
        const category = e.currentTarget.dataset.category;
        if (!category || category === this.data.currentCategory) {
            return;
        }
        this.setData({
            currentCategory: category
        });
        this.loadNews(true);
    },

    onKeywordInput(e) {
        this.setData({
            keyword: (e.detail.value || '').trim()
        });
    },

    onSearch() {
        this.loadNews(true);
    },

    goDetail(e) {
        const id = e.currentTarget.dataset.id;
        if (!id) {
            return;
        }
        wx.navigateTo({
            url: `/pages/info/news/detail?id=${id}`
        });
    },

    formatTime(time) {
        if (!time) {
            return '';
        }
        const str = String(time);
        if (str.length >= 10) {
            return str.substring(0, 10);
        }
        return str;
    }
});
