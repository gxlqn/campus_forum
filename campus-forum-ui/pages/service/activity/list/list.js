const api = require('../../../../utils/api');
const PUBLISH_PATCH_KEY = 'activity_publish_patches';
const ACTIVITY_LIST_REFRESH_KEY = 'refresh_activity_list';

Page({
    data: {
        keyword: '',
        activeStatus: 'all',
        statusTabs: [
            { label: '全部', value: 'all' },
            { label: '待开始', value: 'pending' },
            { label: '进行中', value: 'ongoing' },
            { label: '已结束', value: 'ended' }
        ],
        allList: [],
        list: [],
        loading: false,
        refreshing: false
    },

    onLoad() {
        this.loadData();
    },

    onShow() {
        const needRefresh = wx.getStorageSync(ACTIVITY_LIST_REFRESH_KEY);
        if (needRefresh) {
            wx.removeStorageSync(ACTIVITY_LIST_REFRESH_KEY);
            this.loadData();
        }
        this.applyPublishPatch();
        this.applySignupPatch();
    },

    onPullDownRefresh() {
        this.setData({ refreshing: true });
        this.loadData().finally(() => {
            this.setData({ refreshing: false });
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
        const status = e.currentTarget.dataset.status || 'all';
        this.setData({ activeStatus: status });
        this.applyFilter();
    },

    async loadData() {
        this.setData({ loading: true });
        try {
            const res = await api.getActivityList({ keyword: this.data.keyword });
            const raw = res.data || {};
            const records = Array.isArray(raw) ? raw : (raw.records || []);
            const list = records.map((item) => {
                const statusCode = item.status;
                const joinCount = item.currentParticipants || item.joinCount || item.signupCount || 0;
                const maxParticipants = item.maxParticipants || 0;
                const statusText = statusCode === 2 ? '已结束' : (statusCode === 1 ? '进行中' : '待开始');
                const statusKey = statusCode === 2 ? 'ended' : (statusCode === 1 ? 'ongoing' : 'pending');
                return {
                    ...item,
                    displayTime: item.time || (item.startTime ? `${item.startTime}${item.endTime ? ` - ${item.endTime}` : ''}` : '时间待定'),
                    displayLocation: item.location || '地点待定',
                    joinCount,
                    maxParticipants,
                    statusKey,
                    statusText,
                    isEnded: statusCode === 2
                };
            });
            this.setData({ allList: this.mergePublishPatches(list) }, () => {
                this.applyFilter();
            });
        } catch (err) {
            console.error('加载活动失败', err);
            wx.showToast({ title: '加载活动失败', icon: 'none' });
        } finally {
            this.setData({ loading: false });
        }
    },

    viewActivity(e) {
        const id = e.currentTarget.dataset.id;
        if (id) {
            wx.navigateTo({
                url: `/pages/service/activity/detail/detail?id=${id}`
            });
        }
    },

    goPublish() {
        wx.navigateTo({
            url: '/pages/service/activity/publish/publish'
        });
    },

    applySignupPatch() {
        const patch = wx.getStorageSync('activity_signup_patch');
        if (!patch || !patch.id || !Array.isArray(this.data.allList) || this.data.allList.length === 0) {
            return;
        }
        const allList = this.data.allList.map((item) => {
            if (String(item.id) !== String(patch.id)) return item;
            const nextJoinCount = (item.joinCount || 0) + (patch.joinDelta || 0);
            return {
                ...item,
                joinCount: nextJoinCount,
                hasSignedUp: true
            };
        });
        this.setData({ allList }, () => {
            this.applyFilter();
        });
        wx.removeStorageSync('activity_signup_patch');
    },

    applyPublishPatch() {
        if (!Array.isArray(this.data.allList) || this.data.allList.length === 0) {
            return;
        }
        const merged = this.mergePublishPatches(this.data.allList);
        this.setData({ allList: merged }, () => {
            this.applyFilter();
        });
    },

    mergePublishPatches(baseList) {
        const patches = wx.getStorageSync(PUBLISH_PATCH_KEY) || [];
        if (!Array.isArray(patches) || patches.length === 0) {
            return baseList;
        }

        const normalizedPatches = patches
            .filter((p) => p && p.title)
            .map((p) => ({
                id: p.tempId,
                title: p.title,
                description: p.description,
                displayLocation: p.location || '地点待定',
                displayTime: p.startTime ? `${p.startTime}${p.endTime ? ` - ${p.endTime}` : ''}` : '时间待定',
                location: p.location || '地点待定',
                startTime: p.startTime || '',
                endTime: p.endTime || '',
                maxParticipants: p.maxParticipants || 0,
                joinCount: 0,
                statusKey: 'pending',
                statusText: '待审核',
                isEnded: false,
                isLocalDraft: true,
                createdAt: p.createdAt || 0
            }));

        const merged = [...baseList];
        normalizedPatches.forEach((patchItem) => {
            const existed = merged.some((item) => (
                item.isLocalDraft && String(item.id) === String(patchItem.id)
            ) || (
                !item.isLocalDraft &&
                item.title === patchItem.title &&
                (item.startTime || '') === (patchItem.startTime || '') &&
                (item.location || item.displayLocation || '') === (patchItem.location || patchItem.displayLocation || '')
            ));
            if (!existed) {
                merged.unshift(patchItem);
            }
        });

        return merged;
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
