/**
 * 通知详情页
 */
const api = require('../../../utils/api');

Page({
    data: {
        notificationId: null,
        detail: null,
        loading: false
    },

    onLoad(options) {
        const id = options.id;
        this.setData({ notificationId: id });
        this.loadDetail();
    },

    onPullDownRefresh() {
        this.loadDetail().finally(() => {
            wx.stopPullDownRefresh();
        });
    },

    async loadDetail() {
        if (!this.data.notificationId) {
            return;
        }
        this.setData({ loading: true });
        try {
            const res = await api.getNotificationDetail(this.data.notificationId);
            this.setData({
                detail: res.data || null
            });
        } catch (err) {
            console.error('加载通知详情失败', err);
            wx.showToast({
                title: '加载失败',
                icon: 'none'
            });
        } finally {
            this.setData({ loading: false });
        }
    },

    goTarget() {
        const detail = this.data.detail || {};
        const targetType = Number(detail.targetType);
        const targetId = detail.targetId;
        if (!targetType || !targetId) {
            wx.showToast({
                title: '无关联目标',
                icon: 'none'
            });
            return;
        }
        if (targetType === 1) {
            wx.navigateTo({ url: `/pages/forum/detail/detail?id=${targetId}` });
            return;
        }
        if (targetType === 3) {
            wx.navigateTo({ url: `/pages/service/product/detail/detail?id=${targetId}` });
            return;
        }
        if (targetType === 4) {
            wx.navigateTo({ url: `/pages/service/activity/detail/detail?id=${targetId}` });
            return;
        }
        if (targetType === 5) {
            wx.navigateTo({ url: `/pages/service/help/detail/detail?id=${targetId}` });
            return;
        }
        if (targetType === 6) {
            wx.navigateTo({ url: `/pages/service/lostfound/detail/detail?id=${targetId}` });
            return;
        }
        wx.showToast({
            title: '暂不支持跳转',
            icon: 'none'
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
