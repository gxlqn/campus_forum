/**
 * Mine Page - Personal Center
 */
const app = getApp();
const api = require('../../../utils/api');
const imageHelper = require('../../../utils/imageHelper');

Page({
    data: {
        isLogin: false,
        userInfo: null,
        stats: {
            post: 0,
            like: 0,
            fans: 0
        },
        menuList: [
            { icon: '👤', title: '个人资料', url: '/pages/mine/profile/profile', iconBg: 'bg-pink-light' },
            { icon: '💰', title: '我的钱包', url: '/pages/mine/wallet/wallet', iconBg: 'bg-amber-light' },
            { icon: '📝', title: '我的帖子', url: '/pages/mine/posts/posts', iconBg: 'bg-purple-light' },
            { icon: '📚', title: '我的记录', url: '/pages/mine/records/records', iconBg: 'bg-mint-light' },
            { icon: '📦', title: '我的订单', url: '/pages/service/product/orders/orders', iconBg: 'bg-sky-light' },
            { icon: '🔄', title: '切换账号（开发）', url: '/pages/auth/switchAccount/switchAccount', iconBg: 'bg-purple-light' },
            { icon: '📝', title: '论坛首页', url: '/pages/forum/list/list', isTab: true, iconBg: 'bg-amber-light' },
            { icon: '🛒', title: '服务首页', url: '/pages/service/product/list/list', isTab: true, iconBg: 'bg-sky-light' },
            { icon: '💬', title: '消息中心', url: '/pages/message/list/list', isTab: true, iconBg: 'bg-mint-light' }
        ]
    },

    onLoad() {
        this.checkLogin();
    },

    onShow() {
        this.checkLogin();
    },

    checkLogin() {
        const token = wx.getStorageSync('token');
        let userInfo = wx.getStorageSync('userInfo');

        if (token && userInfo) {
            userInfo = {
                ...userInfo,
                avatar: imageHelper.getFullImageUrl(userInfo.avatar)
            };
            this.setData({
                isLogin: true,
                userInfo: userInfo
            });
            this.loadStats();
        } else {
            this.setData({
                isLogin: false,
                userInfo: null
            });
        }
    },

    async loadStats() {
        try {
            const res = await api.getUserStats();
            const data = res.data || {};
            this.setData({
                stats: {
                    post: data.postCount || 0,
                    like: data.favoritePostCount || 0,
                    fans: data.followerCount || 0
                }
            });
        } catch (err) {
            console.error('加载统计失败', err);
        }
    },

    goProfile() {
        if (!this.checkLoginStatus()) return;
        wx.navigateTo({
            url: '/pages/mine/profile/profile'
        });
    },

    navigateTo(e) {
        if (!this.checkLoginStatus()) return;
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

    logout() {
        if (!this.data.isLogin) {
            wx.showToast({
                title: '未登录',
                icon: 'none'
            });
            return;
        }
        app.clearLoginInfo();
        this.setData({
            isLogin: false,
            userInfo: null,
            stats: { post: 0, like: 0, fans: 0 }
        });
        wx.showToast({
            title: '已退出登录',
            icon: 'success'
        });
        setTimeout(() => {
            wx.switchTab({ url: '/pages/index/index' });
        }, 800);
    },

    checkLoginStatus() {
        if (!this.data.isLogin) {
            wx.showToast({
                title: '请先登录',
                icon: 'none'
            });
            setTimeout(() => {
                this.goLogin();
            }, 1500);
            return false;
        }
        return true;
    },

    goLogin() {
        wx.navigateTo({
            url: '/pages/auth/login/login'
        });
    }
});
