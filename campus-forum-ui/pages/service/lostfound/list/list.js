/**
 * 失物招领列表
 */
const api = require('../../../../utils/api');
const imageHelper = require('../../../../utils/imageHelper');

const LOSTFOUND_LIST_REFRESH_KEY = 'refresh_lostfound_list';

Page({
    data: {
        tabs: ['全部', '寻物', '招领'],
        currentTab: 0,
        list: [],
        loading: false,
        page: 1,
        hasMore: true,
        keyword: ''
    },

    onLoad() {
        this.loadData(true);
    },

    onShow() {
        const needRefresh = wx.getStorageSync(LOSTFOUND_LIST_REFRESH_KEY);
        if (needRefresh) {
            wx.removeStorageSync(LOSTFOUND_LIST_REFRESH_KEY);
            this.loadData(true);
        }
    },

    onPullDownRefresh() {
        this.loadData(true).then(() => {
            wx.stopPullDownRefresh();
        });
    },

    onReachBottom() {
        if (this.data.hasMore && !this.data.loading) {
            this.loadData(false);
        }
    },

    // 切换类型
    switchTab(e) {
        const index = e.currentTarget.dataset.index;
        if (index !== this.data.currentTab) {
            this.setData({ currentTab: index });
            this.loadData(true);
        }
    },

    // 搜索
    onSearch(e) {
        this.setData({ keyword: e.detail.value });
        this.loadData(true);
    },

    // 加载数据
    async loadData(refresh = false) {
        if (this.data.loading) return;

        const page = refresh ? 1 : this.data.page;
        this.setData({ loading: true });

        try {
            const type = this.data.currentTab === 0 ? null : this.data.currentTab; // 1:寻物 2:招领
            const params = {
                current: page,
                size: 10,
                type,
                keyword: this.data.keyword || undefined
            };
            const res = await api.getLostFoundList(params);
            const records = res.data.records || [];
            const parsed = records.map((item) => {
                const images = imageHelper.getImageList(item.images);
                return {
                    ...item,
                    images,
                    imageList: images,
                    desc: item.description || '',
                    location: item.lostLocation || item.location || '',
                    time: item.lostTime ? item.lostTime : '',
                    user: {
                        ...(item.publisher || { nickname: '匿名用户', avatar: '' }),
                        avatar: imageHelper.getFullImageUrl(item.publisher?.avatar)
                    }
                };
            });

            this.setData({
                list: refresh ? parsed : [...this.data.list, ...parsed],
                page: page + 1,
                hasMore: parsed.length >= 10
            });
        } catch (err) {
            console.error('加载失败', err);
            wx.showToast({ title: '加载失败，请重试', icon: 'none' });
        } finally {
            this.setData({ loading: false });
        }
    },

    // 跳转详情
    goDetail(e) {
        const id = e.currentTarget.dataset.id;
        wx.navigateTo({
            url: `/pages/service/lostfound/detail/detail?id=${id}`
        });
        // 如果没有详情页，暂时提示
        // wx.showToast({ title: '查看详情: ' + id, icon: 'none' });
    },

    // 发布
    goPublish() {
        wx.navigateTo({
            url: '/pages/service/lostfound/publish/publish'
        });
    }
});
