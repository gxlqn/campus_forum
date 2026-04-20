/**
 * 校园资讯详情页
 */
const api = require('../../../utils/api');

Page({
    data: {
        id: null,
        detail: null,
        loading: false
    },

    onLoad(options) {
        this.setData({
            id: options.id || null
        });
        this.loadDetail();
    },

    onPullDownRefresh() {
        this.loadDetail().finally(() => {
            wx.stopPullDownRefresh();
        });
    },

    async loadDetail() {
        if (!this.data.id) {
            return;
        }
        this.setData({ loading: true });
        try {
            const res = await api.getNewsDetail(this.data.id);
            const detail = res.data || {};
            this.setData({ detail });
            if (detail.title) {
                wx.setNavigationBarTitle({
                    title: detail.title.length > 10 ? detail.title.slice(0, 10) + '...' : detail.title
                });
            }
        } catch (err) {
            console.error('加载资讯详情失败', err);
            wx.showToast({
                title: '加载失败',
                icon: 'none'
            });
        } finally {
            this.setData({ loading: false });
        }
    },

    copySourceUrl() {
        const detail = this.data.detail || {};
        if (!detail.sourceUrl) {
            wx.showToast({
                title: '暂无原文链接',
                icon: 'none'
            });
            return;
        }
        wx.setClipboardData({
            data: detail.sourceUrl
        });
    },

    formatTime(time) {
        if (!time) {
            return '';
        }
        const str = String(time);
        if (str.length >= 16) {
            return str.substring(0, 16).replace('T', ' ');
        }
        return str;
    }
});
