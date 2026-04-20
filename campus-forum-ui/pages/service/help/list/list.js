const api = require('../../../../utils/api');

const HELP_LIST_REFRESH_KEY = 'refresh_help_list';

Page({
    data: {
        keyword: '',
        activeStatus: 'all',
        statusTabs: [
            { label: '全部', value: 'all' },
            { label: '待接单', value: 'pending' },
            { label: '进行中', value: 'processing' },
            { label: '已完成', value: 'completed' }
        ],
        allList: [],
        list: [],
        loading: false
    },

    onLoad() {
        this.loadData();
    },

    onShow() {
        const needRefresh = wx.getStorageSync(HELP_LIST_REFRESH_KEY);
        if (needRefresh) {
            wx.removeStorageSync(HELP_LIST_REFRESH_KEY);
            this.loadData();
        }
    },

    onPullDownRefresh() {
        this.loadData().finally(() => {
            wx.stopPullDownRefresh();
        });
    },

    onKeywordInput(e) {
        this.setData({ keyword: e.detail.value || '' });
    },

    onSearch() {
        this.loadData();
    },

    onChangeStatusTab(e) {
        const status = e.currentTarget.dataset.status;
        this.setData({ activeStatus: status || 'all' });
        this.applyFilter();
    },

    async loadData() {
        this.setData({ loading: true });
        try {
            const res = await api.getHelpList({ keyword: this.data.keyword });
            const raw = res.data || {};
            const records = Array.isArray(raw) ? raw : (raw.records || []);
            const list = records.map((item) => {
                const status = item.status;
                let statusText = '待接单';
                if (status === 6 || status === 'locking') statusText = '锁定中';
                if (status === 2 || status === 'accepted' || status === 'processing') statusText = '进行中';
                if (status === 3 || status === 'completed') statusText = '已完成';
                if (status === 4 || status === 'cancelled') statusText = '已取消';
                let statusKey = 'pending';
                if (status === 6 || status === 'locking') statusKey = 'pending';
                if (status === 2 || status === 'accepted' || status === 'processing') statusKey = 'processing';
                if (status === 3 || status === 'completed') statusKey = 'completed';
                if (status === 4 || status === 'cancelled') statusKey = 'cancelled';
                return {
                    ...item,
                    statusKey,
                    statusText,
                    displayLocation: item.location || '地点待定',
                    displayTime: item.time || item.expireTime || '时间待定',
                    rewardText: Number(item.reward || 0).toFixed(2),
                    helperName: item.helper?.nickname || item.helper?.username || '接单用户',
                    helperCreditScore: Number(item.helper?.creditScore || 0)
                };
            });
            this.setData({ allList: list }, () => {
                this.applyFilter();
            });
        } catch (err) {
            console.error('加载互助列表失败', err);
            wx.showToast({ title: '加载互助列表失败', icon: 'none' });
        } finally {
            this.setData({ loading: false });
        }
    },

    viewHelp(e) {
        const id = e.currentTarget.dataset.id;
        if (id) {
            wx.navigateTo({
                url: `/pages/service/help/detail/detail?id=${id}`
            });
        }
    },

    goPublish() {
        wx.navigateTo({
            url: '/pages/service/help/publish/publish'
        });
    },

    applyFilter() {
        const { allList, activeStatus } = this.data;
        if (!Array.isArray(allList)) {
            this.setData({ list: [] });
            return;
        }
        if (activeStatus === 'all') {
            this.setData({ list: allList });
            return;
        }
        this.setData({
            list: allList.filter((item) => item.statusKey === activeStatus)
        });
    }
});
