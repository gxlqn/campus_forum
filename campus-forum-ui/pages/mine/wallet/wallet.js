const api = require('../../../utils/api');
const wallet = require('../../../utils/wallet');

Page({
  data: {
    balance: 0,
    balanceText: '0.00',
    presetAmounts: [10, 20, 50, 100, 200, 500],
    customAmount: '',
    recharging: false
  },

  onShow() {
    this.loadBalance();
  },

  loadBalance() {
    const balance = wallet.getBalance();
    this.setData({
      balance,
      balanceText: balance.toFixed(2)
    });

    api.getUserInfo().then((res) => {
      if (res.code === 200 && res.data) {
        const syncedBalance = wallet.syncBalanceFromRemote(res.data.balance || 0);
        this.setData({
          balance: syncedBalance,
          balanceText: syncedBalance.toFixed(2)
        });
      }
    }).catch(() => {});
  },

  selectAmount(e) {
    const amount = Number(e.currentTarget.dataset.amount || 0);
    if (amount <= 0) return;
    this.doRecharge(amount);
  },

  onCustomInput(e) {
    this.setData({ customAmount: e.detail.value || '' });
  },

  recharge() {
    const amount = Number(this.data.customAmount || 0);
    if (amount <= 0) {
      wx.showToast({ title: '请输入有效金额', icon: 'none' });
      return;
    }
    this.doRecharge(amount);
    this.setData({ customAmount: '' });
  },

  doRecharge(amount) {
    if (this.data.recharging) {
      return;
    }
    this.setData({ recharging: true });
    api.rechargeWallet(amount).then((res) => {
      const remoteBalance = Number((res && res.data && res.data.balance) || 0);
      const syncedBalance = wallet.syncBalanceFromRemote(remoteBalance);
      this.setData({
        balance: syncedBalance,
        balanceText: syncedBalance.toFixed(2)
      });
      wx.showToast({ title: '充值成功', icon: 'success' });
    }).catch(() => {
      wx.showToast({ title: '充值失败', icon: 'none' });
    }).finally(() => {
      this.setData({ recharging: false });
    });
  }
});
