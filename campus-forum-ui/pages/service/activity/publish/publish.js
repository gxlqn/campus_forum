const api = require('../../../../utils/api');
const PUBLISH_PATCH_KEY = 'activity_publish_patches';
const ACTIVITY_LIST_REFRESH_KEY = 'refresh_activity_list';

Page({
  data: {
    form: {
      title: '',
      description: '',
      location: '',
      startDate: '',
      startClock: '',
      endDate: '',
      endClock: '',
      maxParticipants: ''
    },
    submitting: false
  },

  onLoad() {
    const now = new Date();
    const y = now.getFullYear();
    const m = String(now.getMonth() + 1).padStart(2, '0');
    const d = String(now.getDate()).padStart(2, '0');
    const h = String(now.getHours()).padStart(2, '0');
    const min = String(now.getMinutes()).padStart(2, '0');
    this.setData({
      'form.startDate': `${y}-${m}-${d}`,
      'form.startClock': `${h}:${min}`
    });
  },

  onInputChange(e) {
    const field = e.currentTarget.dataset.field;
    const value = e.detail.value || '';
    this.setData({ [`form.${field}`]: value });
  },

  chooseLocation() {
    wx.chooseLocation({
      success: (res) => {
        const locationText = [res.address, res.name].filter(Boolean).join(' ');
        this.setData({ 'form.location': locationText || '位置未命名' });
      }
    });
  },

  onPickStartDate(e) {
    this.setData({ 'form.startDate': e.detail.value || '' });
  },

  onPickStartClock(e) {
    this.setData({ 'form.startClock': e.detail.value || '' });
  },

  onPickEndDate(e) {
    this.setData({ 'form.endDate': e.detail.value || '' });
  },

  onPickEndClock(e) {
    this.setData({ 'form.endClock': e.detail.value || '' });
  },

  submit() {
    const { form, submitting } = this.data;
    if (submitting) return;

    if (!form.title.trim()) {
      wx.showToast({ title: '请填写活动标题', icon: 'none' });
      return;
    }
    if (!form.description.trim()) {
      wx.showToast({ title: '请填写活动简介', icon: 'none' });
      return;
    }
    if (!form.location.trim()) {
      wx.showToast({ title: '请选择活动地点', icon: 'none' });
      return;
    }
    if (!form.startDate || !form.startClock) {
      wx.showToast({ title: '请选择开始时间', icon: 'none' });
      return;
    }

    const startTime = `${form.startDate} ${form.startClock}:00`;
    const endTime = form.endDate && form.endClock ? `${form.endDate} ${form.endClock}:00` : null;

    const payload = {
      title: form.title.trim(),
      description: form.description.trim(),
      location: form.location.trim(),
      startTime,
      endTime,
      maxParticipants: form.maxParticipants ? Number(form.maxParticipants) : null
    };

    this.setData({ submitting: true });
    api.publishActivity(payload)
      .then((res) => {
        if (res.code === 200 || res.code === 0) {
          const patchItem = {
            tempId: `local-${Date.now()}`,
            title: payload.title,
            description: payload.description,
            location: payload.location,
            startTime: payload.startTime,
            endTime: payload.endTime,
            maxParticipants: payload.maxParticipants,
            createdAt: Date.now()
          };
          const patches = wx.getStorageSync(PUBLISH_PATCH_KEY) || [];
          patches.unshift(patchItem);
          wx.setStorageSync(PUBLISH_PATCH_KEY, patches.slice(0, 20));
          wx.setStorageSync(ACTIVITY_LIST_REFRESH_KEY, true);
          wx.showToast({ title: '申请已提交', icon: 'success' });
          setTimeout(() => {
            wx.navigateBack();
          }, 1200);
          return;
        }
        wx.showToast({ title: res.msg || '提交失败', icon: 'none' });
      })
      .catch(() => {
        wx.showToast({ title: '网络错误', icon: 'none' });
      })
      .finally(() => {
        this.setData({ submitting: false });
      });
  }
});
