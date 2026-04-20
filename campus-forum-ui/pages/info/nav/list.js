/**
 * 校内服务导航页
 */
const api = require('../../../utils/api');

Page({
    data: {
        categories: ['全部'],
        currentCategory: '全部',
        keyword: '',
        list: [],
        loading: false
    },

    onLoad() {
        this.loadData();
    },

    onPullDownRefresh() {
        this.loadData().finally(() => {
            wx.stopPullDownRefresh();
        });
    },

    async loadData() {
        this.setData({ loading: true });
        try {
            const [categoryRes, listRes] = await Promise.all([
                api.getServiceNavCategories(),
                api.getServiceNavList(this.buildQuery())
            ]);
            this.setData({
                categories: ['全部'].concat((categoryRes.data || [])),
                list: listRes.data || []
            });
        } catch (err) {
            console.error('加载服务导航失败', err);
            wx.showToast({
                title: '加载失败',
                icon: 'none'
            });
        } finally {
            this.setData({ loading: false });
        }
    },

    buildQuery() {
        const params = {};
        if (this.data.currentCategory && this.data.currentCategory !== '全部') {
            params.category = this.data.currentCategory;
        }
        if (this.data.keyword) {
            params.keyword = this.data.keyword;
        }
        return params;
    },

    onCategoryTap(e) {
        const category = e.currentTarget.dataset.category;
        if (!category || category === this.data.currentCategory) {
            return;
        }
        this.setData({
            currentCategory: category
        });
        this.loadData();
    },

    onKeywordInput(e) {
        this.setData({
            keyword: (e.detail.value || '').trim()
        });
    },

    onSearch() {
        this.loadData();
    },

    callPhone(e) {
        const phone = e.currentTarget.dataset.phone;
        if (!phone) {
            wx.showToast({
                title: '暂无电话',
                icon: 'none'
            });
            return;
        }
        wx.makePhoneCall({
            phoneNumber: String(phone)
        });
    },

    copyAddress(e) {
        const address = e.currentTarget.dataset.address;
        if (!address) {
            wx.showToast({
                title: '暂无地址',
                icon: 'none'
            });
            return;
        }
        wx.setClipboardData({
            data: String(address)
        });
    },

    copyUrl(e) {
        const url = e.currentTarget.dataset.url;
        if (!url) {
            wx.showToast({
                title: '暂无链接',
                icon: 'none'
            });
            return;
        }
        wx.setClipboardData({
            data: String(url)
        });
    }
});
