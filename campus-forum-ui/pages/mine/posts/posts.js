const api = require('../../../utils/api');

Page({
  data: {
    list: [],
    loading: false,
    keyword: ''
  },

  onLoad() {
    this.loadList();
  },

  onShow() {
    if (wx.getStorageSync('refresh_forum_list')) {
      wx.removeStorageSync('refresh_forum_list');
      this.loadList();
    }
  },

  async loadList() {
    this.setData({ loading: true });
    try {
      const params = { current: 1, size: 50 };
      if (this.data.keyword) {
        params.keyword = this.data.keyword;
      }
      const res = await api.getMyPosts(params);
      const records = res?.data?.records || res?.records || [];
      this.setData({
        list: records.map((item) => ({
          ...item,
          auditText: this.getAuditText(item.auditStatus)
        }))
      });
    } catch (err) {
      console.error('加载我的帖子失败', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  getAuditText(status) {
    const s = Number(status);
    if (s === 1) return '已通过';
    if (s === 2) return '已拒绝';
    return '待审核';
  },

  onSearchInput(e) {
    this.setData({ keyword: (e.detail.value || '').trim() });
  },

  onSearch() {
    this.loadList();
  },

  onClearSearch() {
    if (!this.data.keyword) return;
    this.setData({ keyword: '' }, () => this.loadList());
  },

  goDetail(e) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({ url: `/pages/forum/detail/detail?id=${id}` });
  }
});
