/**
 * 互助详情页
 */
const api = require('../../../../utils/api');
const imageHelper = require('../../../../utils/imageHelper');

Page({
    data: {
        id: null,
        detail: null,
        loading: true,
        accepting: false,
        cancelling: false,
        completing: false,
        confirming: false,
        complaining: false,
        appealing: false,
        statusText: '待接单',
        statusClass: 'pending',
        canAccept: false,
        canContact: false,
        canCancel: false,
        canCompleteByHelper: false,
        canPublisherConfirm: false,
        canPublisherComplain: false,
        canHelperAppeal: false,
        isOwner: false,
        isHelper: false
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
            const res = await api.getHelpDetail(this.data.id);
            if (res.data) {
                const normalized = this.normalizeDetail(res.data);
                const currentUser = wx.getStorageSync('userInfo') || {};
                const currentUserId = currentUser.id;
                const ownerId = normalized.userId || normalized.publisherId;
                const helperId = normalized.helperId || normalized.helper?.id;
                const isOwner = !!currentUserId && !!ownerId && String(currentUserId) === String(ownerId);
                const isHelper = !!currentUserId && !!helperId && String(currentUserId) === String(helperId);
                this.setData({
                    detail: normalized,
                    statusText: this.getStatusText(normalized.status),
                    statusClass: this.getStatusClass(normalized.status),
                    isOwner,
                    isHelper,
                    canAccept: this.canAcceptStatus(normalized.status) && !isOwner,
                    canContact: this.canContact(normalized, isOwner, isHelper, normalized.status),
                    canCancel: isOwner && this.canCancelStatus(normalized.status),
                    canCompleteByHelper: isHelper && this.canCompleteByHelperStatus(normalized.status),
                    canPublisherConfirm: isOwner && this.canCompleteByHelperStatus(normalized.status),
                    canPublisherComplain: isOwner && this.canCompleteByHelperStatus(normalized.status) && !this.hasComplaint(normalized),
                    canHelperAppeal: isHelper && this.canHelperAppealStatus(normalized),
                    loading: false
                });
                wx.setNavigationBarTitle({ title: normalized.title || '互助详情' });
            } else {
                wx.showToast({ title: '互助不存在', icon: 'none' });
                this.setData({ loading: false });
            }
        } catch (err) {
            console.error('加载互助详情失败:', err);
            wx.showToast({ title: '加载失败', icon: 'none' });
            this.setData({ loading: false });
        }
    },

    // 接单/接受互助任务
    onAccept() {
        const app = getApp();
        if (app.checkNeedLogin && app.checkNeedLogin()) return;

        const d = this.data.detail;
        if (!this.canAcceptStatus(d.status)) {
            return wx.showToast({ title: '该任务已被接单或已完成', icon: 'none' });
        }
        if (this.data.isOwner) {
            return wx.showToast({ title: '不能接自己发布的互助单', icon: 'none' });
        }

        if (this.data.accepting) return;

        wx.showModal({
            title: '确认接单',
            content: `确定要接取「${d.title}」吗？完成后可获得 ${d.reward || 0} 元悬赏`,
            confirmColor: '#FF6B9D',
            success: (res) => {
                if (res.confirm) {
                    this.doAccept();
                }
            }
        });
    },

    doAccept() {
        this.setData({ accepting: true });
        api.acceptHelp(this.data.id)
            .then(res => {
                if (res.code === 200 || res.code === 0) {
                    wx.showToast({ title: '接单成功', icon: 'success' });
                    // 刷新详情
                    setTimeout(() => this.loadDetail(), 1500);
                } else {
                    wx.showToast({ title: res.msg || '接单失败', icon: 'none' });
                }
            })
            .catch(err => {
                console.error('接单失败:', err);
                wx.showToast({ title: '网络错误', icon: 'none' });
            })
            .finally(() => {
                this.setData({ accepting: false });
            });
    },

    // 联系发布者（跳转聊天）
    onContact() {
        const d = this.data.detail;
        const targetUserId = this.getContactTargetUserId(d);
        if (!targetUserId) {
            wx.showToast({ title: '暂无可联系用户', icon: 'none' });
            return;
        }
        const currentUser = wx.getStorageSync('userInfo') || {};
        if (currentUser.id && String(currentUser.id) === String(targetUserId)) {
            wx.showToast({ title: '不能给自己发私信', icon: 'none' });
            return;
        }
        
        // 跳转到聊天页面
        const targetNickname = this.getContactTargetNickname(d);
        wx.navigateTo({
            url: `/pages/message/chat/chat?targetUserId=${targetUserId}&targetNickname=${encodeURIComponent(targetNickname)}`
        }).catch(() => {
            wx.showToast({ title: '请先打开消息页面', icon: 'none' });
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

    onCancelOrder() {
        if (!this.data.canCancel || this.data.cancelling) {
            return;
        }
        wx.showModal({
            title: '取消订单',
            content: '确定要取消这条互助单吗？',
            confirmColor: '#FF6B9D',
            success: (res) => {
                if (res.confirm) {
                    this.doCancelOrder();
                }
            }
        });
    },

    doCancelOrder() {
        this.setData({ cancelling: true });
        api.cancelHelp(this.data.id)
            .then((res) => {
                if (res.code === 200 || res.code === 0) {
                    wx.showToast({ title: '已取消', icon: 'success' });
                    this.loadDetail();
                } else {
                    wx.showToast({ title: res.msg || '取消失败', icon: 'none' });
                }
            })
            .catch((err) => {
                console.error('取消互助单失败:', err);
                wx.showToast({ title: '网络错误', icon: 'none' });
            })
            .finally(() => {
                this.setData({ cancelling: false });
            });
    },

    onCompleteByHelper() {
        if (!this.data.canCompleteByHelper || this.data.completing) {
            return;
        }
        wx.showModal({
            title: '提交完成',
            content: '确认已完成本次互助吗？提交后等待发布者确认。',
            confirmColor: '#FF6B9D',
            success: (res) => {
                if (res.confirm) {
                    this.doCompleteByHelper();
                }
            }
        });
    },

    doCompleteByHelper() {
        this.setData({ completing: true });
        api.completeHelp(this.data.id)
            .then((res) => {
                if (res.code === 200 || res.code === 0) {
                    wx.showToast({ title: '已提交完成', icon: 'success' });
                    this.loadDetail();
                } else {
                    wx.showToast({ title: res.msg || '提交失败', icon: 'none' });
                }
            })
            .catch((err) => {
                console.error('提交完成失败:', err);
                wx.showToast({ title: '网络错误', icon: 'none' });
            })
            .finally(() => this.setData({ completing: false }));
    },

    onPublisherConfirm() {
        if (!this.data.canPublisherConfirm || this.data.confirming) {
            return;
        }
        wx.showModal({
            title: '确认订单完成',
            content: '确认后订单将进入已完成状态。',
            confirmColor: '#FF6B9D',
            success: (res) => {
                if (res.confirm) {
                    this.doPublisherConfirm();
                }
            }
        });
    },

    doPublisherConfirm() {
        this.setData({ confirming: true });
        api.completeHelp(this.data.id)
            .then((res) => {
                if (res.code === 200 || res.code === 0) {
                    wx.showToast({ title: '确认成功', icon: 'success' });
                    this.loadDetail();
                } else {
                    wx.showToast({ title: res.msg || '确认失败', icon: 'none' });
                }
            })
            .catch((err) => {
                console.error('发布者确认失败:', err);
                wx.showToast({ title: '网络错误', icon: 'none' });
            })
            .finally(() => this.setData({ confirming: false }));
    },

    onPublisherComplain() {
        if (!this.data.canPublisherComplain || this.data.complaining) {
            return;
        }
        wx.showModal({
            title: '投诉并申请仲裁',
            content: '发起投诉后订单将冻结，并进入仲裁流程。',
            confirmColor: '#FF6B9D',
            success: (res) => {
                if (res.confirm) {
                    this.doPublisherComplain();
                }
            }
        });
    },

    doPublisherComplain() {
        this.setData({ complaining: true });
        api.publisherConfirmHelp(this.data.id, 1)
            .then((res) => {
                if (res.code === 200 || res.code === 0) {
                    wx.showToast({ title: '投诉已提交', icon: 'success' });
                    this.loadDetail();
                } else {
                    wx.showToast({ title: res.msg || '投诉失败', icon: 'none' });
                }
            })
            .catch((err) => {
                console.error('发布者投诉失败:', err);
                wx.showToast({ title: '网络错误', icon: 'none' });
            })
            .finally(() => this.setData({ complaining: false }));
    },

    onHelperAppeal() {
        if (!this.data.canHelperAppeal || this.data.appealing) {
            return;
        }
        wx.showModal({
            title: '申诉',
            content: '确认发起申诉吗？平台将进行仲裁处理。',
            confirmColor: '#FF6B9D',
            success: (res) => {
                if (res.confirm) {
                    this.doHelperAppeal();
                }
            }
        });
    },

    doHelperAppeal() {
        this.setData({ appealing: true });
        api.helperAppealHelp(this.data.id)
            .then((res) => {
                if (res.code === 200 || res.code === 0) {
                    wx.showToast({ title: '申诉已提交', icon: 'success' });
                    this.loadDetail();
                } else {
                    wx.showToast({ title: res.msg || '申诉失败', icon: 'none' });
                }
            })
            .catch((err) => {
                console.error('申诉失败:', err);
                wx.showToast({ title: '网络错误', icon: 'none' });
            })
            .finally(() => this.setData({ appealing: false }));
    },

    onShareAppMessage() {
        const d = this.data.detail;
        return {
            title: d?.title ? `「${d.title}」需要帮助` : '校园互助分享',
            path: `/pages/service/help/detail/detail?id=${this.data.id}`
        };
    },

    copyLocation() {
        const loc = this.data.detail?.displayLocation || this.data.detail?.location;
        if (!loc) return;
        wx.setClipboardData({
            data: loc,
            success: () => wx.showToast({ title: '已复制地址', icon: 'success' })
        });
    },

    normalizeDetail(raw) {
        const item = raw || {};
        const reward = Number(item.reward || item.bounty || 0);
        const publisher = item.publisher || null;
        const displayLocation = item.location || item.pickupLocation || item.deliveryLocation || item.expressLocation || '地点待定';
        const displayTime = item.time || item.expectedTime || item.expireTime || '时间待定';
        return {
            ...item,
            publisherId: item.publisherId || item.userId,
            rewardText: reward.toFixed(2),
            displayLocation,
            location: displayLocation,
            displayTime: this.formatDateTime(displayTime),
            createTime: item.createTime || item.createAt || '',
            publisher: publisher ? {
                ...publisher,
                avatar: imageHelper.getFullImageUrl(publisher.avatar)
            } : null
        };
    },

    formatDateTime(value) {
        if (!value) return '时间待定';
        if (typeof value !== 'string') return String(value);
        return value.replace('T', ' ').replace(/\.\d+$/, '');
    },

    canAcceptStatus(status) {
        return status === 0 || status === 1 || status === 'pending';
    },

    canCompleteByHelperStatus(status) {
        return status === 2 || status === 'accepted' || status === 'processing';
    },

    canPublisherConfirmStatus(status) {
        return status === 2 || status === 'accepted' || status === 'processing';
    },

    canHelperAppealStatus(detail) {
        return Number(detail?.isFrozen || 0) === 1 && Number(detail?.complaintStatus || 0) === 1;
    },

    hasComplaint(detail) {
        return Number(detail?.complaintStatus || 0) > 0;
    },

    canContact(detail, isOwner, isHelper, status) {
        if (!detail) return false;
        if (isOwner) {
            // 发布者在进行中阶段优先展示确认/投诉主流程，避免底部操作过载
            if (this.canPublisherConfirmStatus(status)) {
                return false;
            }
            return !!(detail.helperId || detail.helper?.id);
        }
        if (isHelper) {
            return !!(detail.publisherId || detail.userId);
        }
        return !!(detail.publisherId || detail.userId);
    },

    getContactTargetUserId(detail) {
        if (!detail) return null;
        if (this.data.isOwner) {
            return detail.helperId || detail.helper?.id;
        }
        return detail.publisherId || detail.userId;
    },

    getContactTargetNickname(detail) {
        if (!detail) return '互助用户';
        if (this.data.isOwner) {
            return detail.helper?.nickname || '接单人';
        }
        return detail.publisher?.nickname || '发布者';
    },

    canCancelStatus(status) {
        return status === 0 || status === 1 || status === 6 || status === 'pending' || status === 'locking';
    },

    getStatusText(status) {
        if (status === 0 || status === 1 || status === 'pending') return '待接单';
        if (status === 6 || status === 'locking') return '锁定中(等待时段结束)';
        if (status === 2 || status === 'accepted' || status === 'processing') return '进行中';
        if (status === 3 || status === 'completed') return '已完成';
        if (status === 4 || status === 'cancelled') return '已取消';
        return '未知状态';
    },

    getStatusClass(status) {
        if (status === 0 || status === 1 || status === 'pending') return 'pending';
        if (status === 6 || status === 'locking') return 'pending';
        if (status === 2 || status === 'accepted' || status === 'processing') return 'accepted';
        if (status === 3 || status === 'completed') return 'completed';
        if (status === 4 || status === 'cancelled') return 'cancelled';
        return 'completed';
    }
});
