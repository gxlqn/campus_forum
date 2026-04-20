/**
 * 商品详情页
 */
const api = require('../../../../utils/api');
const imageHelper = require('../../../../utils/imageHelper');

const PRODUCT_LIST_REFRESH_KEY = 'refresh_product_list';

Page({
    data: {
        productId: null,
        product: null,
        loading: false,
        creatingOrder: false,
        showReportDialog: false,
        currentUserId: null
    },

    onLoad(options) {
        if (!options.id || options.id === 'null') {
            wx.showToast({ title: '商品ID无效', icon: 'none' });
            wx.navigateBack();
            return;
        }
        this.setData({ productId: options.id });
        this.loadProductDetail();
        this.loadCurrentUserId();
    },

    onPullDownRefresh() {
        this.loadProductDetail().then(() => {
            wx.stopPullDownRefresh();
        });
    },

    async loadProductDetail() {
        this.setData({ loading: true });

        try {
            const res = await api.getProductDetail(this.data.productId);
            const product = res.data;
            if (!product) {
                throw new Error('商品不存在');
            }

            product.imageList = imageHelper.getImageList(product.images);

            if (product.seller) {
                product.seller = {
                    ...product.seller,
                    avatar: imageHelper.getFullImageUrl(product.seller.avatar)
                };
            }

            product.isWanted = Number(product.tradeType) === 2;
            product.isOwner = Number(this.data.currentUserId) === Number(product.userId || product.sellerId || product.seller?.id);
            product.wantedStatusText = this.getWantedStatusText(product.status);
            product.wantedMeetTime = this.extractWantedMeetTime(product.description || '');
            product.displayDescription = this.stripWantedMeetTime(product.description || '');

            this.setData({ product });
        } catch (err) {
            console.error('加载商品详情失败', err);
        } finally {
            this.setData({ loading: false });
        }
    },

    previewImage(e) {
        const { url } = e.currentTarget.dataset;
        wx.previewImage({
            current: url,
            urls: this.data.product.imageList || []
        });
    },

    callSeller() {
        const phone = this.data.product?.seller?.phone;
        if (phone) {
            wx.makePhoneCall({ phoneNumber: phone });
        } else {
            wx.showToast({ title: '暂无联系方式', icon: 'none' });
        }
    },

    contactSeller() {
        const app = getApp();
        if (app.checkNeedLogin()) return;
        const product = this.data.product || {};
        const seller = product.seller || {};
        const targetUserId = product.sellerId || product.userId || seller.id;
        const currentUserId = this.data.currentUserId;
        if (!targetUserId) {
            wx.showToast({ title: '卖家信息缺失', icon: 'none' });
            return;
        }
        if (currentUserId && Number(currentUserId) === Number(targetUserId)) {
            wx.showToast({ title: '不能给自己发私信', icon: 'none' });
            return;
        }
        const nickname = seller.nickname || seller.username || '卖家';
        wx.setStorageSync(PRODUCT_LIST_REFRESH_KEY, true);
        wx.navigateTo({
            url: `/pages/message/chat/chat?targetUserId=${targetUserId}&targetNickname=${encodeURIComponent(nickname)}`
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

    async buyNow() {
        const app = getApp();
        if (app.checkNeedLogin()) return;

        if (this.data.creatingOrder) {
            return;
        }

        const product = this.data.product;
        if (!product || !product.id) {
            wx.showToast({ title: '商品信息异常', icon: 'none' });
            return;
        }

        if (product.isWanted) {
            this.contactSeller();
            return;
        }

        if (Number(this.data.currentUserId) === Number(product.userId || product.sellerId || product.seller?.id)) {
            wx.showToast({ title: '不能购买自己发布的商品', icon: 'none' });
            return;
        }

        this.setData({ creatingOrder: true });

        try {
            const res = await api.createProductOrder(product.id);
            const orderId = res?.data?.id;
            wx.showToast({ title: '申请已提交', icon: 'success' });

            setTimeout(() => {
                const target = orderId
                    ? `/pages/service/product/orders/orders?role=buyer&orderId=${orderId}`
                    : '/pages/service/product/orders/orders?role=buyer';
                wx.navigateTo({ url: target });
            }, 400);
        } catch (err) {
            console.error('创建订单失败', err);
            wx.showToast({
                title: err?.message || err?.msg || '下单失败，请重试',
                icon: 'none'
            });
        } finally {
            this.setData({ creatingOrder: false });
        }
    },

    onShareAppMessage() {
        const product = this.data.product;
        return {
            title: product?.title || '二手商品',
            path: `/pages/service/product/detail/detail?id=${this.data.productId}`
        };
    },

    // 举报
    onShowReport() {
        const app = getApp();
        if (app.checkNeedLogin && app.checkNeedLogin()) return;
        this.selectComponent('#reportDialog').show(4, this.data.productId);
    },

    async loadCurrentUserId() {
        try {
            const res = await api.getUserInfo();
            this.setData({ currentUserId: res.data?.id });
            if (this.data.product) {
                const product = this.data.product;
                product.isOwner = Number(res.data?.id) === Number(product.userId || product.sellerId || product.seller?.id);
                this.setData({ product });
            }
        } catch (e) {}
    },

    getWantedStatusText(status) {
        if (Number(status) === 3) return '已匹配';
        if (Number(status) === 0) return '已关闭';
        return '开放中';
    },

    extractWantedMeetTime(description) {
        const matched = String(description || '').match(/\[期望交易时间\]\s*([0-9]{4}-[0-9]{2}-[0-9]{2}\s+[0-9]{2}:[0-9]{2})/);
        return matched ? matched[1] : '';
    },

    stripWantedMeetTime(description) {
        return String(description || '')
            .replace(/\n*\[期望交易时间\]\s*[0-9]{4}-[0-9]{2}-[0-9]{2}\s+[0-9]{2}:[0-9]{2}\s*/g, '\n')
            .replace(/^\s+|\s+$/g, '');
    },

    async onUpdateWantedStatus(e) {
        const status = Number(e.currentTarget.dataset.status);
        const product = this.data.product;
        if (!product || !product.id || !product.isWanted || !product.isOwner) {
            return;
        }

        try {
            await api.updateWantedProductStatus(product.id, status);
            wx.setStorageSync(PRODUCT_LIST_REFRESH_KEY, true);
            wx.showToast({ title: '状态已更新', icon: 'success' });
            this.loadProductDetail();
        } catch (err) {
            console.error('更新求购状态失败', err);
            wx.showToast({ title: err?.message || '更新失败', icon: 'none' });
        }
    }
});
