/**
 * 活动详情页
 */
const api = require('../../../../utils/api');

Page({
    data: {
        id: null,
        detail: null,
        loading: true,
        signingUp: false,
        hasSignedUp: false,
        joinCount: 0,
        statusText: '待开始',
        isEnded: false
    },

    onLoad(options) {
        if (options.id) {
            this.setData({ id: options.id });
            this.loadDetail();
        }
    },

    onPullDownRefresh() {
        this.loadDetail().finally(() => wx.stopPullDownRefresh());
    },

    async loadDetail() {
        this.setData({ loading: true });
        try {
            const res = await api.getActivityDetail(this.data.id);
            if (res.data) {
                const d = res.data;
                const statusCode = d.status;
                const statusText = statusCode === 2 ? '已结束' : (statusCode === 1 ? '进行中' : '待开始');
                const joinCount = d.currentParticipants || d.joinCount || d.signupCount || 0;
                const hasSignedUp = d.hasSignedUp === true || d.isSignedUp === true;
                const isEnded = statusCode === 2;

                this.setData({
                    detail: {
                        ...d,
                        displayTime: d.time || (d.startTime ? `${d.startTime}${d.endTime ? ` - ${d.endTime}` : ''}` : '时间待定')
                    },
                    hasSignedUp,
                    joinCount,
                    statusText,
                    isEnded,
                    loading: false
                });

                wx.setNavigationBarTitle({ title: d.title || '活动详情' });
            } else {
                wx.showToast({ title: '活动不存在', icon: 'none' });
                this.setData({ loading: false });
            }
        } catch (err) {
            console.error('加载活动详情失败:', err);
            wx.showToast({ title: '加载失败', icon: 'none' });
            this.setData({ loading: false });
        }
    },

    onSignup() {
        const app = getApp();
        if (app.checkNeedLogin && app.checkNeedLogin()) return;

        if (this.data.hasSignedUp) {
            return wx.showToast({ title: '您已报名该活动', icon: 'none' });
        }

        if (this.data.isEnded) {
            return wx.showToast({ title: '活动已结束，无法报名', icon: 'none' });
        }

        if (this.data.signingUp) return;

        wx.showModal({
            title: '确认报名',
            content: '确定要报名参加此活动吗？',
            confirmColor: '#FF6B9D',
            success: (res) => {
                if (res.confirm) {
                    this.doSignup();
                }
            }
        });
    },

    doSignup() {
        this.setData({ signingUp: true });
        api.signupActivity(this.data.id)
            .then(res => {
                if (res.code === 200 || res.code === 0) {
                    this.setData({
                        hasSignedUp: true,
                        joinCount: (this.data.joinCount || 0) + 1
                    });
                    wx.setStorageSync('activity_signup_patch', {
                        id: this.data.id,
                        joinDelta: 1,
                        hasSignedUp: true,
                        updatedAt: Date.now()
                    });
                    wx.showToast({ title: '报名成功', icon: 'success' });
                } else {
                    wx.showToast({ title: res.msg || '报名失败', icon: 'none' });
                }
            })
            .catch(err => {
                console.error('报名失败:', err);
                wx.showToast({ title: '网络错误', icon: 'none' });
            })
            .finally(() => {
                this.setData({ signingUp: false });
            });
    },

    onShareAppMessage() {
        const d = this.data.detail;
        return {
            title: d?.title || '校园活动分享',
            path: `/pages/service/activity/detail/detail?id=${this.data.id}`,
            imageUrl: d?.coverImage || '/static/images/share.png'
        };
    },

    // 复制地址到剪贴板
    copyLocation() {
        const loc = this.data.detail?.location;
        if (!loc) return;
        wx.setClipboardData({
            data: loc,
            success: () => wx.showToast({ title: '已复制地址', icon: 'success' })
        });
    },

    goBack() {
        wx.navigateBack({ fail: () => wx.switchTab({ url: '/pages/index/index' }) });
    }
});
