/**
 * 校园服务论坛系统 - 小程序入口
 */
App({
  globalData: {
    userInfo: null,
    token: null,
    isLogin: false,
    baseUrl: 'http://localhost:8081/api',
    //baseUrl: 'http://10.139.248.115:8080/api',
    // 静态资源地址（图片等，不带/api前缀）
    //staticBaseUrl: 'http://10.139.248.115:8080/api',
    staticBaseUrl: 'http://localhost:8081/api'
  },

  onLaunch() {
    // 检查登录状态
    this.checkLoginStatus();
  },

  // 检查登录状态
  checkLoginStatus() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    
    if (token && userInfo) {
      this.globalData.token = token;
      this.globalData.userInfo = userInfo;
      this.globalData.isLogin = true;
    }
  },

  // 设置登录信息
  setLoginInfo(token, userInfo) {
    this.globalData.token = token;
    this.globalData.userInfo = userInfo;
    this.globalData.isLogin = true;
    
    wx.setStorageSync('token', token);
    wx.setStorageSync('userInfo', userInfo);
  },

  // 清除登录信息
  clearLoginInfo() {
    this.globalData.token = null;
    this.globalData.userInfo = null;
    this.globalData.isLogin = false;
    
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
  },

  // 检查是否需要登录
  checkNeedLogin() {
    if (!this.globalData.isLogin) {
      wx.navigateTo({
        url: '/pages/auth/login/login'
      });
      return true;
    }
    return false;
  }
});
