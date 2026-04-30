const api = require('../../../utils/api');

Page({
  data: {
    tab: 'help',
    tabs: [
      { key: 'help', label: '互助记录' },
      { key: 'activity', label: '活动记录' }
    ],
    list: [],
    current: 1,
    size: 10,
    hasMore: true,
    loading: false,
    keyword: ''
  },

  onLoad() {
    this.loadRecords(true);
  },

  onPullDownRefresh() {
    this.loadRecords(true).finally(() => wx.stopPullDownRefresh());
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadRecords(false);
    }
  },

  onTabTap(e) {
    const tab = e.currentTarget.dataset.tab;
    if (!tab || tab === this.data.tab) return;
    this.setData({ tab }, () => this.loadRecords(true));
  },

  async loadRecords(refresh) {
    if (refresh) {
      this.setData({ current: 1, hasMore: true });
    }

    this.setData({ loading: true });
    try {
      const type = this.data.tab === 'help' ? 'help' : 'activity';
      const params = {
        type,
        current: this.data.current,
        size: this.data.size
      };
      if (this.data.keyword) {
        params.keyword = this.data.keyword;
      }
      const res = await api.getMyPublish(params);

      const records = res?.data?.records || res?.records || [];
      const normalized = records.map((item) => ({
        ...item,
        statusText: this.getStatusText(type, item.status),
        statusClass: this.getStatusClass(type, item.status),
        auditText: this.getAuditText(item.auditStatus)
      }));

      const merged = refresh ? normalized : this.data.list.concat(normalized);
      this.setData({
        list: merged,
        current: this.data.current + 1,
        hasMore: records.length >= this.data.size
      });
    } catch (err) {
      console.error('加载记录失败', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  getStatusText(type, status) {
    const s = Number(status);
    if (type === 'help') {
      if (s === 1) return '待接单';
      if (s === 2) return '进行中';
      if (s === 3) return '已完成';
      if (s === 4) return '已取消';
      if (s === 6) return '争议中';
      return '已关闭';
    }

    if (s === 0) return '待开始';
    if (s === 1) return '进行中';
    if (s === 2) return '已结束';
    return '未知状态';
  },

  getStatusClass(type, status) {
    const s = Number(status);
    if (type === 'help') {
      if (s === 2) return 'running';
      if (s === 3) return 'done';
      if (s === 4) return 'cancel';
      if (s === 6) return 'warning';
      return 'pending';
    }

    if (s === 1) return 'running';
    if (s === 2) return 'done';
    return 'pending';
  },

  getAuditText(auditStatus) {
    const a = Number(auditStatus);
    if (a === 1) return '已通过';
    if (a === 2) return '已拒绝';
    return '待审核';
  },

  onSearchInput(e) {
    this.setData({ keyword: (e.detail.value || '').trim() });
  },

  onSearch() {
    this.loadRecords(true);
  },

  onClearSearch() {
    if (!this.data.keyword) return;
    this.setData({ keyword: '' }, () => this.loadRecords(true));
  }
});
