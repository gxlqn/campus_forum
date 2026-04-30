const api = require('../../../../utils/api');
const imageHelper = require('../../../../utils/imageHelper');

Page({
  data: {
    role: 'buyer',
    roleTabs: [
      { label: '我买到的', value: 'buyer' },
      { label: '我卖出的', value: 'seller' }
    ],
    status: 'all',
    statusTabs: [
      { label: '全部', value: 'all' },
      { label: '待筛选', value: '0' },
      { label: '进行中', value: '1' },
      { label: '已取消', value: '2' },
      { label: '已完成', value: '3' },
      { label: '已拒绝', value: '4' },
      { label: '未入选', value: '5' }
    ],
    page: 1,
    size: 10,
    hasMore: true,
    loading: false,
    orders: [],
    keyword: '',
    focusOrderId: null,
    showMeetupEditor: false,
    meetupForm: {
      orderId: null,
      mode: 'schedule',
      place: '',
      latitude: null,
      longitude: null,
      date: '',
      time: ''
    }
  },

  onLoad(options) {
    const role = options?.role === 'seller' ? 'seller' : 'buyer';
    const orderId = options?.orderId ? Number(options.orderId) : null;
    this.setData({ role, focusOrderId: orderId });
    this.loadOrders(true);
  },

  onPullDownRefresh() {
    this.loadOrders(true).finally(() => wx.stopPullDownRefresh());
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadOrders(false);
    }
  },

  async loadOrders(refresh) {
    if (refresh) {
      this.setData({ page: 1, hasMore: true });
    }

    this.setData({ loading: true });
    try {
      const res = await api.getMyProductOrders({
        role: this.data.role,
        keyword: this.data.keyword || undefined,
        current: this.data.page,
        size: this.data.size
      });
      const raw = res?.data || {};
      const records = Array.isArray(raw) ? raw : (raw.records || []);
      const mapped = records.map((item) => this.mapOrder(item));
      const merged = refresh ? mapped : this.data.orders.concat(mapped);
      const filtered = this.applyStatusFilter(merged, this.data.status);

      this.setData({
        orders: filtered,
        page: this.data.page + 1,
        hasMore: records.length >= this.data.size
      });
    } catch (err) {
      console.error('加载订单失败', err);
      wx.showToast({ title: '加载订单失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  mapOrder(item) {
    const status = Number(item?.status || 0);
    let statusText = '申请中';
    if (status === 0) statusText = '申请中';
    if (status === 1) statusText = '进行中';
    if (status === 2) statusText = '已取消';
    if (status === 3) statusText = '已完成';
    if (status === 4) statusText = '已拒绝';
    if (status === 5) statusText = '未入选';

    const product = item?.product || {};
    const productImage = imageHelper.getFullImageUrl(
      product.image || imageHelper.getImageList(product.images)[0],
      '/static/images/default-image.png'
    );

    const counterpart = this.data.role === 'buyer'
      ? (item?.seller?.nickname || item?.seller?.username || '卖家')
      : (item?.buyer?.nickname || item?.buyer?.username || '买家');

    return {
      ...item,
      status,
      statusText,
      statusKey: String(status),
      productTitle: product.title || '商品',
      productImage,
      amountText: Number(item?.amount || 0).toFixed(2),
      counterpart,
      meetupPlace: item?.meetupPlace || '',
      meetupTime: item?.meetupTime || '',
      meetupCode: item?.meetupCode || '',
      meetupVerified: Number(item?.meetupVerified || 0) === 1,
      rescheduleCount: Number(item?.rescheduleCount || 0),
      canCancel: this.data.role === 'buyer' ? (status === 0 || status === 1) : (status === 1),
      cancelText: this.data.role === 'buyer'
        ? (status === 0 ? '撤回申请' : '取消订单')
        : '取消订单',
      canConfirm: this.data.role === 'buyer' && status === 1,
      canSchedule: status === 1,
      canReschedule: status === 1,
      canVerify: status === 1,
      canAccept: this.data.role === 'seller' && status === 0,
      canReject: this.data.role === 'seller' && status === 0
    };
  },

  applyStatusFilter(list, status) {
    if (status === 'all') {
      return list;
    }
    return list.filter((item) => item.statusKey === status);
  },

  onRoleTabTap(e) {
    const role = e.currentTarget.dataset.role;
    if (!role || role === this.data.role) {
      return;
    }
    this.setData({ role, status: 'all' }, () => {
      this.loadOrders(true);
    });
  },

  onStatusTabTap(e) {
    const status = e.currentTarget.dataset.status || 'all';
    if (status === this.data.status) {
      return;
    }
    this.setData({ status }, () => {
      this.loadOrders(true);
    });
  },

  onSearchInput(e) {
    this.setData({ keyword: (e.detail.value || '').trim() });
  },

  onSearch() {
    this.loadOrders(true);
  },

  onClearSearch() {
    if (!this.data.keyword) return;
    this.setData({ keyword: '' }, () => this.loadOrders(true));
  },

  noop() {},

  onGoProductDetail(e) {
    const productId = e.currentTarget.dataset.productId;
    if (!productId) {
      return;
    }
    wx.navigateTo({
      url: `/pages/service/product/detail/detail?id=${productId}`
    });
  },

  onCancelOrder(e) {
    const orderId = e.currentTarget.dataset.orderId;
    if (!orderId) {
      return;
    }

    wx.showModal({
      title: '取消订单',
      content: '确认取消当前订单吗？',
      success: async (res) => {
        if (!res.confirm) {
          return;
        }
        try {
          await api.cancelProductOrder(orderId, '用户主动取消');
          wx.showToast({ title: '已取消', icon: 'success' });
          this.loadOrders(true);
        } catch (err) {
          console.error('取消订单失败', err);
          wx.showToast({ title: '取消失败', icon: 'none' });
        }
      }
    });
  },

  onConfirmOrder(e) {
    const orderId = e.currentTarget.dataset.orderId;
    if (!orderId) {
      return;
    }

    wx.showModal({
      title: '确认收货',
      content: '确认已完成当面交付并收货吗？',
      success: async (res) => {
        if (!res.confirm) {
          return;
        }
        try {
          await api.confirmProductOrder(orderId);
          wx.showToast({ title: '已确认收货', icon: 'success' });
          this.loadOrders(true);
        } catch (err) {
          console.error('确认收货失败', err);
          wx.showToast({ title: '确认失败', icon: 'none' });
        }
      }
    });
  },

  onScheduleMeetup(e) {
    const orderId = e.currentTarget.dataset.orderId;
    if (!orderId) return;
    this.openMeetupEditor(orderId, 'schedule');
  },

  onRescheduleMeetup(e) {
    const orderId = e.currentTarget.dataset.orderId;
    if (!orderId) return;
    this.openMeetupEditor(orderId, 'reschedule');
  },

  onAcceptOrder(e) {
    const orderId = e.currentTarget.dataset.orderId;
    if (!orderId) return;
    wx.showModal({
      title: '确认交易对象',
      content: '确认接受该申请并进入交易吗？确认后商品将进入交易中。',
      success: async (res) => {
        if (!res.confirm) return;
        try {
          await api.acceptProductOrder(orderId);
          wx.showToast({ title: '已确认交易对象', icon: 'success' });
          this.loadOrders(true);
        } catch (err) {
          console.error('接受申请失败', err);
          wx.showToast({ title: err?.message || '操作失败', icon: 'none' });
        }
      }
    });
  },

  onRejectOrder(e) {
    const orderId = e.currentTarget.dataset.orderId;
    if (!orderId) return;
    wx.showModal({
      title: '拒绝申请',
      editable: true,
      placeholderText: '可填写原因（选填）',
      success: async (res) => {
        if (!res.confirm) return;
        try {
          await api.rejectProductOrder(orderId, (res.content || '').trim());
          wx.showToast({ title: '已拒绝申请', icon: 'success' });
          this.loadOrders(true);
        } catch (err) {
          console.error('拒绝申请失败', err);
          wx.showToast({ title: err?.message || '操作失败', icon: 'none' });
        }
      }
    });
  },

  onVerifyMeetup(e) {
    const orderId = e.currentTarget.dataset.orderId;
    if (!orderId) return;

    wx.showModal({
      title: '输入见面码',
      editable: true,
      placeholderText: '请输入6位见面码',
      success: async (res) => {
        if (!res.confirm) return;
        const code = (res.content || '').trim();
        if (!code) {
          wx.showToast({ title: '见面码不能为空', icon: 'none' });
          return;
        }
        try {
          await api.verifyProductMeetup(orderId, code);
          wx.showToast({ title: '核销成功', icon: 'success' });
          this.loadOrders(true);
        } catch (err) {
          console.error('核销失败', err);
          wx.showToast({ title: err?.message || '核销失败', icon: 'none' });
        }
      }
    });
  },

  openMeetupEditor(orderId, mode) {
    const now = new Date(Date.now() + 30 * 60 * 1000);
    const date = this.formatDate(now);
    const time = `${String(now.getHours()).padStart(2, '0')}:${now.getMinutes() < 30 ? '30' : '00'}`;
    this.setData({
      showMeetupEditor: true,
      meetupForm: {
        orderId,
        mode,
        place: '',
        latitude: null,
        longitude: null,
        date,
        time
      }
    });
  },

  onMeetupDateChange(e) {
    this.setData({ 'meetupForm.date': e.detail.value });
  },

  onMeetupTimeChange(e) {
    this.setData({ 'meetupForm.time': e.detail.value });
  },

  chooseMeetupLocation() {
    wx.chooseLocation({
      success: (res) => {
        const place = `${res.name || ''}${res.address ? ` (${res.address})` : ''}`.trim() || res.address || '';
        this.setData({
          'meetupForm.place': place,
          'meetupForm.latitude': res.latitude,
          'meetupForm.longitude': res.longitude
        });
      },
      fail: () => {
        wx.showToast({ title: '请开启定位权限后重试', icon: 'none' });
      }
    });
  },

  closeMeetupEditor() {
    this.setData({ showMeetupEditor: false });
  },

  async submitMeetupEditor() {
    const { orderId, mode, place, date, time } = this.data.meetupForm;
    if (!orderId) return;
    if (!place) {
      wx.showToast({ title: '请选择约见地点', icon: 'none' });
      return;
    }
    if (!date || !time) {
      wx.showToast({ title: '请选择约见日期和时间', icon: 'none' });
      return;
    }

    const meetupTime = `${date} ${time}`;
    try {
      if (mode === 'reschedule') {
        await api.rescheduleProductMeetup(orderId, place, meetupTime);
      } else {
        await api.scheduleProductMeetup(orderId, place, meetupTime);
      }
      wx.showToast({ title: mode === 'reschedule' ? '改约成功' : '约见已安排', icon: 'success' });
      this.setData({ showMeetupEditor: false });
      this.loadOrders(true);
    } catch (err) {
      console.error('提交约见失败', err);
      wx.showToast({ title: err?.message || '提交失败', icon: 'none' });
    }
  },

  formatDate(date) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }
});
