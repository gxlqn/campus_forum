/**
 * Search Page
 */
const api = require('../../utils/api');

const SEARCH_HISTORY_KEY = 'search_history_keywords';
const DEFAULT_RECOMMEND_KEYWORDS = [
    '二手', '跑腿', '活动', '失物', '课程资料', '兼职', '拼车', '考研'
];

Page({
    data: {
        keyword: '',
        loading: false,
        hasSearched: false,
        sections: [],
        posts: [],
        products: [],
        activities: [],
        helps: [],
        historyKeywords: [],
        hotRecommendKeywords: [],
        recommendKeywords: []
    },

    onLoad(options) {
        this.loadHistoryKeywords();
        this.loadRecommendKeywords();
        if (options.keyword) {
            const keyword = decodeURIComponent(options.keyword);
            this.setData({ keyword });
            this.search(keyword);
        }
    },

    onShow() {
        this.loadHistoryKeywords();
        this.loadRecommendKeywords();
    },

    onKeywordInput(e) {
        this.setData({ keyword: e.detail.value || '' });
    },

    onSearch() {
        this.search(this.data.keyword);
    },

    async search(keyword) {
        if (!keyword || !keyword.trim()) {
            wx.showToast({ title: '请输入搜索关键字', icon: 'none' });
            return;
        }
        const normalized = keyword.trim();
        this.saveHistoryKeyword(normalized);
        this.setData({ loading: true });

        try {
            const res = await api.searchAll({ keyword: normalized });
            const data = res.data || {};
            this.setData({
                hasSearched: true,
                sections: data.sections || [],
                posts: data.posts || [],
                products: data.products || [],
                activities: data.activities || [],
                helps: data.helps || []
            });
        } catch (err) {
            console.error('搜索失败', err);
            wx.showToast({ title: '搜索失败', icon: 'none' });
        } finally {
            this.setData({ loading: false });
        }
    },

    goToPost(e) {
        const id = e.currentTarget.dataset.id;
        if (id) {
            wx.navigateTo({ url: `/pages/forum/detail/detail?id=${id}` });
        }
    },

    goToProduct(e) {
        const id = e.currentTarget.dataset.id;
        if (id) {
            wx.navigateTo({ url: `/pages/service/product/detail/detail?id=${id}` });
        }
    },

    tapKeyword(e) {
        const keyword = e.currentTarget.dataset.keyword;
        if (!keyword) {
            return;
        }
        this.setData({ keyword });
        this.search(keyword);
    },

    clearHistory() {
        wx.removeStorageSync(SEARCH_HISTORY_KEY);
        this.setData({ historyKeywords: [] });
        this.refreshRecommendKeywords(this.data.hotRecommendKeywords || []);
    },

    loadHistoryKeywords() {
        const list = wx.getStorageSync(SEARCH_HISTORY_KEY);
        const historyKeywords = Array.isArray(list) ? list : [];
        this.setData({ historyKeywords });
    },

    saveHistoryKeyword(keyword) {
        const current = wx.getStorageSync(SEARCH_HISTORY_KEY);
        const list = Array.isArray(current) ? current : [];
        const deduped = [keyword, ...list.filter((item) => item !== keyword)].slice(0, 12);
        wx.setStorageSync(SEARCH_HISTORY_KEY, deduped);
        this.setData({ historyKeywords: deduped });
        this.refreshRecommendKeywords(this.data.hotRecommendKeywords || []);
    },

    async loadRecommendKeywords() {
        try {
            const res = await api.getSearchRecommend({ size: 10 });
            const hotRecommendKeywords = Array.isArray(res.data) ? res.data : [];
            this.setData({ hotRecommendKeywords });
            this.refreshRecommendKeywords(hotRecommendKeywords);
        } catch (err) {
            console.warn('加载推荐搜索词失败，使用本地默认词', err);
            this.setData({ hotRecommendKeywords: [] });
            this.refreshRecommendKeywords(DEFAULT_RECOMMEND_KEYWORDS);
        }
    },

    refreshRecommendKeywords(dynamicKeywords = []) {
        const history = this.data.historyKeywords || [];
        const source = dynamicKeywords.length ? dynamicKeywords : DEFAULT_RECOMMEND_KEYWORDS;
        const merged = [...history, ...source];
        const unique = Array.from(new Set(merged)).slice(0, 10);
        this.setData({ recommendKeywords: unique });
    }
});
