/**
 * 失物招领详情页
 */
const api = require('../../../../utils/api');
const imageHelper = require('../../../../utils/imageHelper');

Page({
    data: {
        id: null,
        detail: null,
        images: [],
        loading: true,
        showReportDialog: false,
        currentUserId: null,
        showClaimModal: false,
        claimDesc: '',
        claimImages: [],
        submittingClaim: false
    },

    onLoad(options) {
        const { id } = options;
        if (!id) {
            wx.showToast({ title: '参数错误', icon: 'none' });
            wx.navigateBack();
            return;
        }
        this.setData({ id: Number(id) });
        this.loadDetail();
        this.loadCurrentUserId();
    },

    // 加载详情
    async loadDetail() {
        this.setData({ loading: true });
        try {
            const res = await api.getLostFoundDetail(this.data.id);
            const detail = res.data;
            const images = imageHelper.getImageList(detail.images);

            if (detail.publisher) {
                detail.publisher = {
                    ...detail.publisher,
                    avatar: imageHelper.getFullImageUrl(detail.publisher.avatar)
                };
            }
            if (detail.claimer) {
                detail.claimer = {
                    ...detail.claimer,
                    avatar: imageHelper.getFullImageUrl(detail.claimer.avatar)
                };
            }

            this.setData({
                detail,
                images,
                loading: false
            });
            console.log('Current User ID:', this.data.currentUserId);
            console.log('Post User ID:', detail.userId);
            console.log('Post Type:', detail.type);
            console.log('Post Status:', detail.status);
        } catch (err) {
            console.error('加载详情失败', err);
            wx.showToast({ title: '加载失败，请重试', icon: 'none' });
            this.setData({ loading: false });
        }
    },

    // 拨打电话
    makeCall(e) {
        const phone = e.currentTarget.dataset.phone;
        if (phone) {
            wx.makePhoneCall({
                phoneNumber: phone
            });
        }
    },

    // 复制微信号
    copyWechat(e) {
        const wechat = e.currentTarget.dataset.wechat;
        if (wechat) {
            wx.setClipboardData({
                data: wechat,
                success: () => {
                    wx.showToast({ title: '微信号已复制', icon: 'success' });
                }
            });
        }
    },

    // 举报
    onShowReport() {
        const app = getApp();
        if (app.checkNeedLogin && app.checkNeedLogin()) return;
        this.selectComponent('#reportDialog').show(6, this.data.id);
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

    async loadCurrentUserId() {
        try {
            const res = await api.getUserInfo();
            this.setData({ currentUserId: res.data?.id });
        } catch (e) {}
    },

    onShowClaim() {
        const token = wx.getStorageSync('token');
        if (!token) {
            wx.navigateTo({ url: '/pages/auth/login/login' });
            return;
        }
        this.setData({ showClaimModal: true });
    },

    onHideClaim() {
        this.setData({ showClaimModal: false });
    },

    onClaimDescInput(e) {
        this.setData({ claimDesc: e.detail.value });
    },

    chooseClaimImage() {
        wx.chooseImage({
            count: 3 - this.data.claimImages.length,
            sizeType: ['compressed'],
            sourceType: ['album', 'camera'],
            success: (res) => {
                this.setData({
                    claimImages: [...this.data.claimImages, ...res.tempFilePaths]
                });
            }
        });
    },

    removeClaimImage(e) {
        const index = e.currentTarget.dataset.index;
        const images = this.data.claimImages;
        images.splice(index, 1);
        this.setData({ claimImages: images });
    },

    async submitClaim() {
        if (!this.data.claimDesc.trim()) {
            wx.showToast({ title: '请输入认领理由及证据', icon: 'none' });
            return;
        }

        this.setData({ submittingClaim: true });
        try {
            // 1. 上传图片
            const imageUrls = [];
            for (const path of this.data.claimImages) {
                const url = await api.uploadFile(path);
                imageUrls.push(url);
            }

            // 2. 提交申请
            await api.submitLostFoundClaim(this.data.id, {
                description: this.data.claimDesc,
                images: JSON.stringify(imageUrls)
            });

            wx.showToast({ title: '申请已提交，请等待审核', icon: 'success' });
            this.setData({ showClaimModal: false, claimDesc: '', claimImages: [] });
            
            // 重新刷新详情，有些系统可能直接变更状态(如果是通过的话)，这里为了保险可以刷新
            this.loadDetail();
        } catch (err) {
            console.error('认领申请失败', err);
            wx.showToast({ title: err.message || '申请失败，请重试', icon: 'none' });
        } finally {
            this.setData({ submittingClaim: false });
        }
    }
});