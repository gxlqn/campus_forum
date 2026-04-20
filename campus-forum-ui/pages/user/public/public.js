/**
 * 用户公开信息页
 */
const api = require('../../../utils/api');
const imageHelper = require('../../../utils/imageHelper');

Page({
  data: {
    userId: null,
    profile: null,
    loading: true,
    currentUserId: null,
    showReportDialog: false
  },

  onLoad(options) {
    const userId = Number(options.userId || 0);
    if (!userId) {
      wx.showToast({ title: '用户参数错误', icon: 'none' });
      setTimeout(() => wx.navigateBack(), 300);
      return;
    }

    const currentUser = wx.getStorageSync('userInfo') || {};
    this.setData({
      userId,
      currentUserId: currentUser.id || null
    });
    this.loadProfile();
  },

  onPullDownRefresh() {
    this.loadProfile().finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  async loadProfile() {
    this.setData({ loading: true });
    try {
      const res = await api.getPublicUserProfile(this.data.userId);
      const profile = res.data || {};
      const nickname = profile.nickname || profile.username || '用户';
      wx.setNavigationBarTitle({ title: nickname });

      this.setData({
        profile: {
          ...profile,
          avatar: imageHelper.getFullImageUrl(profile.avatar),
          stats: profile.stats || {}
        }
      });
    } catch (err) {
      console.error('加载公开资料失败', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  async onToggleFollow() {
    const profile = this.data.profile;
    if (!profile || profile.isSelf) {
      return;
    }

    try {
      if (profile.isFollowing) {
        await api.unfollowUser(profile.id);
        wx.showToast({ title: '已取消关注', icon: 'success' });
      } else {
        await api.followUser(profile.id);
        wx.showToast({ title: '关注成功', icon: 'success' });
      }
      this.setData({
        'profile.isFollowing': !profile.isFollowing
      });
    } catch (err) {
      console.error('关注操作失败', err);
    }
  },

  onStartChat() {
    const profile = this.data.profile;
    if (!profile || !profile.canMessage) {
      wx.showToast({ title: '无法发起私信', icon: 'none' });
      return;
    }

    wx.navigateTo({
      url: `/pages/message/chat/chat?targetUserId=${profile.id}&targetNickname=${encodeURIComponent(profile.nickname || profile.username || '用户')}`
    });
  },

  onShowReport() {
    const profile = this.data.profile;
    if (!profile || !profile.canReport) {
      return;
    }
    this.selectComponent('#reportDialog').show(3, profile.id);
  }
});
