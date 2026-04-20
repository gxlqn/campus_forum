/**
 * 互助发布页
 */
const api = require('../../../../utils/api');
const wallet = require('../../../../utils/wallet');

const HELP_LIST_REFRESH_KEY = 'refresh_help_list';

Page({
  data: {
    timeSlots: [
      '00:00-03:00',
      '03:00-06:00',
      '06:00-09:00',
      '09:00-12:00',
      '12:00-15:00',
      '15:00-18:00',
      '18:00-21:00',
      '21:00-24:00'
    ],
    form: {
      type: 1,
      title: '',
      description: '',
      location: '',
      pickupLocation: '',
      expectedDate: '',
      timeSlotIndex: 0,
      timeSlotLabel: '',
      time: '',
      expectedTime: '',
      reward: ''
    },
    balance: 0,
    submitting: false,
    rewardInsufficient: false
  },

  onShow() {
    this.getUserBalance();
  },

  onLoad() {
    const now = new Date();
    const y = now.getFullYear();
    const m = String(now.getMonth() + 1).padStart(2, '0');
    const d = String(now.getDate()).padStart(2, '0');
    const date = `${y}-${m}-${d}`;

    const currentHour = now.getHours();
    const slotIndex = Math.min(Math.floor(currentHour / 3) + 1, 7);
    this.setData({
      'form.expectedDate': date,
      'form.timeSlotIndex': slotIndex,
      'form.timeSlotLabel': this.data.timeSlots[slotIndex],
      'form.time': `${date} ${this.data.timeSlots[slotIndex]}`,
      'form.expectedTime': this.buildExpectedTime(date, slotIndex)
    });
  },

  getUserBalance() {
    const localBalance = wallet.getBalance();
    const reward = Number(this.data.form.reward || 0);
    this.setData({
      balance: localBalance,
      rewardInsufficient: reward > 0 && reward > localBalance
    });

    api.getUserInfo().then(res => {
      if (res.code === 200 && res.data) {
        const balance = wallet.syncBalanceFromRemote(res.data.balance || 0);
        const currentReward = Number(this.data.form.reward || 0);
        this.setData({
          balance,
          rewardInsufficient: currentReward > 0 && currentReward > balance
        });
      }
    }).catch(() => {});
  },

  onInputChange(e) {
    const field = e.currentTarget.dataset.field;
    const value = e.detail.value || '';
    const nextForm = {
      ...this.data.form,
      [field]: value
    };
    const reward = Number(nextForm.reward || 0);
    this.setData({
      [`form.${field}`]: value,
      rewardInsufficient: reward > 0 && reward > Number(this.data.balance || 0)
    });
  },

  goWallet() {
    wx.navigateTo({
      url: '/pages/mine/wallet/wallet'
    });
  },

  chooseLocation() {
    wx.chooseLocation({
      success: (res) => {
        if (res.address || res.name) {
          const locationText = [res.address, res.name].filter(Boolean).join(' ');
          this.setData({
            'form.location': locationText,
            'form.pickupLocation': locationText
          });
        }
      },
      fail: () => {}
    });
  },

  onDateChange(e) {
    const date = e.detail.value;
    const slotIndex = Number(this.data.form.timeSlotIndex || 0);
    const slotLabel = this.data.timeSlots[slotIndex] || this.data.timeSlots[0];
    this.setData({
      'form.expectedDate': date,
      'form.time': `${date} ${slotLabel}`,
      'form.expectedTime': this.buildExpectedTime(date, slotIndex)
    });
  },

  onTimeSlotChange(e) {
    const slotIndex = Number(e.detail.value || 0);
    const date = this.data.form.expectedDate;
    const slotLabel = this.data.timeSlots[slotIndex] || this.data.timeSlots[0];
    this.setData({
      'form.timeSlotIndex': slotIndex,
      'form.timeSlotLabel': slotLabel,
      'form.time': date ? `${date} ${slotLabel}` : slotLabel,
      'form.expectedTime': this.buildExpectedTime(date, slotIndex)
    });
  },

  handleSubmit() {
    const { title, description, location, time, reward } = this.data.form;
    const rewardValue = Number(reward);

    if (!title || !title.trim()) {
      return wx.showToast({ title: '请输入任务标题', icon: 'none' });
    }

    if (!description || !description.trim()) {
      return wx.showToast({ title: '请描述您的需求', icon: 'none' });
    }

    if (!reward || isNaN(rewardValue) || rewardValue <= 0) {
      return wx.showToast({ title: '请填写正确的悬赏金额', icon: 'none' });
    }

    if (rewardValue > Number(this.data.balance || 0)) {
      return wx.showToast({ title: '余额不足，请先充值', icon: 'none' });
    }

    wx.showModal({
      title: '支付确认',
      content: `本次发布将从您的钱包中冻结 ${rewardValue.toFixed(2)} 元作为悬赏金`,
      confirmColor: '#ff6b9d',
      success: (res) => {
        if (res.confirm) {
          this.doSubmit();
        }
      }
    });
  },

  doSubmit() {
    if (this.data.submitting) return;
    this.setData({ submitting: true });

    const rewardValue = Number(this.data.form.reward || 0);
    const payload = {
      ...this.data.form,
      pickupLocation: this.data.form.pickupLocation || this.data.form.location || null,
      expectedTime: this.data.form.expectedTime || null
    };
    api.publishHelp(payload)
      .then(r => {
        if (r.code === 200 || r.code === 0) {
          api.getUserInfo()
            .then((infoRes) => {
              if (infoRes.code === 200 && infoRes.data) {
                const syncedBalance = wallet.syncBalanceFromRemote(infoRes.data.balance || 0);
                this.setData({ balance: syncedBalance, rewardInsufficient: false });
              }
            })
            .catch(() => {})
            .finally(() => {
              wx.setStorageSync(HELP_LIST_REFRESH_KEY, true);
              wx.showToast({ title: '发布成功', icon: 'success' });
              setTimeout(() => wx.navigateBack(), 1500);
            });
        } else {
          wx.showToast({ title: r.msg || '发布失败', icon: 'none' });
        }
      })
      .catch(err => {
        console.error('发布互助失败:', err);
        wx.showToast({ title: '网络错误，请重试', icon: 'none' });
      })
      .finally(() => {
        this.setData({ submitting: false });
      });
  },

  buildExpectedTime(date, slotIndex) {
    if (!date) {
      return null;
    }
    const idx = Number(slotIndex || 0);
    const startHour = String(idx * 3).padStart(2, '0');
    return `${date}T${startHour}:00:00`;
  }
});
