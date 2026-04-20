/**
 * Product List Page
 */
const api = require('../../../../utils/api');
const imageHelper = require('../../../../utils/imageHelper');

const PRODUCT_LIST_REFRESH_KEY = 'refresh_product_list';
const app=getApp();
Page({
    data: {
        mode: 'sell',
        modeTabs: [
            { key: 'sell', label: '闲置在售', tradeType: 1 },
            { key: 'wanted', label: '校园求购', tradeType: 2 }
        ],
        wantedStatus: 'all',
        wantedStatusTabs: [
            { key: 'all', label: '全部' },
            { key: '1', label: '开放中' },
            { key: '3', label: '已匹配' },
            { key: '0', label: '已关闭' }
        ],
        categories: [
            { id: 1, name: '数码' },
            { id: 2, name: '书籍' },
            { id: 3, name: '生活' },
            { id: 4, name: '美妆' },
            { id: 5, name: '其他' }
        ],
        products: [],
        leftProducts: [],
        rightProducts: [],
        currentCategory: null,
        keyword: '',
        page: 1,
        size: 10,
        hasMore: true,
        loading: true
    },

    onLoad() {
        this.loadProducts(true);
    },

    onShow() {
        const needRefresh = wx.getStorageSync(PRODUCT_LIST_REFRESH_KEY);
        if (needRefresh) {
            wx.removeStorageSync(PRODUCT_LIST_REFRESH_KEY);
            this.loadProducts(true);
        }
    },

    onPullDownRefresh() {
        this.loadProducts(true).finally(() => {
            wx.stopPullDownRefresh();
        });
    },

    onReachBottom() {
        if (this.data.hasMore && !this.data.loading) {
            this.loadProducts(false);
        }
    },

    async loadProducts(refresh) {
        if (refresh) {
            this.setData({ page: 1, hasMore: true });
        }

        this.setData({ loading: true });

        try {
            const requestParams = {
                categoryId: this.data.currentCategory,
                tradeType: this.data.mode === 'wanted' ? 2 : 1,
                keyword: this.data.keyword,
                current: this.data.page,
                size: this.data.size
            };

            if (this.data.mode === 'wanted' && this.data.wantedStatus !== 'all') {
                requestParams.status = Number(this.data.wantedStatus);
            }

            const res = await api.getProductList({
                ...requestParams
            });

            if (res.data) {
                const records = res.data.records || res.data;
                const processedRecords = records.map(item => {
                    const isWanted = Number(item.tradeType) === 2;
                    const imageList = item.images ? imageHelper.getImageList(item.images) : [];
                    const coverImage = item.image
                        ? imageHelper.getFullImageUrl(item.image, app.globalData.baseUrl+'/static/images/default-image.png')
                        : (imageList[0] || app.globalData.baseUrl+'/static/images/default-image.png');
                    return {
                        ...item,
                        isWanted,
                        wantedStatusText: isWanted ? this.getWantedStatusText(item.status) : '',
                        wantedStatusClass: isWanted ? this.getWantedStatusClass(item.status) : '',
                        user: {
                            ...item.user,
                            avatar: imageHelper.getFullImageUrl(item.user?.avatar)
                        },
                        imageList,
                        image: coverImage
                    };
                });
                const newProducts = refresh ? processedRecords : [...this.data.products, ...processedRecords];

                // Split into two columns for waterfall layout
                const left = [];
                const right = [];
                newProducts.forEach((item, index) => {
                    if (index % 2 === 0) {
                        left.push(item);
                    } else {
                        right.push(item);
                    }
                });

                this.setData({
                    products: newProducts,
                    leftProducts: left,
                    rightProducts: right,
                    page: this.data.page + 1,
                    hasMore: records.length >= this.data.size
                });
            }
        } catch (err) {
            console.error('Load products failed', err);
        }

        this.setData({ loading: false });
    },

    getWantedStatusText(status) {
        if (Number(status) === 3) return '已匹配';
        if (Number(status) === 0) return '已关闭';
        return '开放中';
    },

    getWantedStatusClass(status) {
        if (Number(status) === 3) return 'matched';
        if (Number(status) === 0) return 'closed';
        return 'open';
    },

    onCategoryTap(e) {
        const id = e.currentTarget.dataset.id;
        this.setData({ currentCategory: id });
        this.loadProducts(true);
    },

    onSearchInput(e) {
        this.setData({ keyword: e.detail.value });
    },

    onSearch() {
        this.loadProducts(true);
    },

    onModeTap(e) {
        const mode = e.currentTarget.dataset.mode;
        if (!mode || mode === this.data.mode) {
            return;
        }
        this.setData({ mode, wantedStatus: 'all' }, () => {
            this.loadProducts(true);
        });
    },

    onWantedStatusTap(e) {
        const status = String(e.currentTarget.dataset.status || 'all');
        if (status === this.data.wantedStatus) {
            return;
        }
        this.setData({ wantedStatus: status }, () => {
            this.loadProducts(true);
        });
    },

    goDetail(e) {
        const id = e.currentTarget.dataset.id;
        if (!id || id === 'null' || id === null) {
            wx.showToast({ title: '商品ID无效', icon: 'none' });
            return;
        }
        wx.navigateTo({
            url: `/pages/service/product/detail/detail?id=${id}`
        });
    },

    goPublish() {
        wx.navigateTo({
            url: '/pages/service/product/publish/publish'
        });
    },

    goWantedPublish() {
        wx.navigateTo({
            url: '/pages/service/product/publish/publish?mode=wanted'
        });
    }
});
