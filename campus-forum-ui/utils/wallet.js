const LEGACY_BALANCE_KEY = 'wallet_balance';

function getUserBalanceKey() {
  const userInfo = getCachedUserInfo();
  const userId = userInfo && userInfo.id;
  return userId ? `wallet_balance_${userId}` : LEGACY_BALANCE_KEY;
}

function getCachedUserInfo() {
  return wx.getStorageSync('userInfo') || null;
}

function saveCachedUserInfo(userInfo) {
  wx.setStorageSync('userInfo', userInfo || {});
}

function hasLocalBalance() {
  const local = wx.getStorageSync(getUserBalanceKey());
  return local !== '' && local !== null && local !== undefined;
}

function getBalance() {
  const key = getUserBalanceKey();
  const local = wx.getStorageSync(key);
  if (local !== '' && local !== null && local !== undefined) {
    return Number(local) || 0;
  }

  // 向后兼容：老版本统一键迁移到当前用户键。
  if (key !== LEGACY_BALANCE_KEY) {
    const legacy = wx.getStorageSync(LEGACY_BALANCE_KEY);
    if (legacy !== '' && legacy !== null && legacy !== undefined) {
      wx.setStorageSync(key, Number(legacy) || 0);
      return Number(legacy) || 0;
    }
  }

  const userInfo = getCachedUserInfo();
  return Number((userInfo && userInfo.balance) || 0);
}

function setBalance(balance) {
  const value = Number(balance) || 0;
  wx.setStorageSync(getUserBalanceKey(), value);

  const userInfo = getCachedUserInfo();
  if (userInfo) {
    saveCachedUserInfo({
      ...userInfo,
      balance: value
    });
  }
  return value;
}

function ensureBalanceFromRemote(remoteBalance) {
  if (!hasLocalBalance()) {
    return setBalance(remoteBalance);
  }
  return getBalance();
}

function syncBalanceFromRemote(remoteBalance) {
  return setBalance(remoteBalance);
}

module.exports = {
  getBalance,
  setBalance,
  ensureBalanceFromRemote,
  syncBalanceFromRemote
};
