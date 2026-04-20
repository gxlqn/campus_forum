const api = require('../../../utils/api');
const imageHelper = require('../../../utils/imageHelper');
const app = getApp();

Page({
  data: {
    loading: true,
    users: [],
    currentUserId: null,
    searchKeyword: '',
    filteredUsers: [],
    switchingId: null
  },

  onLoad() {
    const userInfo = app.globalData.userInfo;
    if (userInfo) {
      this.setData({ currentUserId: userInfo.id });
    }
    this.loadUsers();
  },

  onShow() {
    if (!this.data.loading) {
      this.loadUsers();
    }
  },

  loadUsers() {
    this.setData({ loading: true });
    
    api.getDevUsers()
      .then(res => {
        console.log('获取用户列表:', res);
        if (res && res.code === 200 && res.data) {
          const users = res.data.map(user => ({
            ...user,
            avatar: imageHelper.getFullImageUrl(user.avatar),
            userTypeText: user.user_type === 1 ? '学生' : '教师',
            statusText: user.status === 1 ? '正常' : '禁用',
            verifiedText: user.is_verified === 1 ? '已认证' : '未认证',
            isCurrentUser: user.id === this.data.currentUserId
          }));
          
          this.setData({
            users,
            filteredUsers: users
          });
        } else {
          wx.showToast({
            title: res?.message || '获取用户列表失败',
            icon: 'none'
          });
        }
      })
      .catch(err => {
        console.error('获取用户列表失败:', err);
        wx.showToast({
          title: err.message || '网络错误',
          icon: 'none'
        });
      })
      .finally(() => {
        this.setData({ loading: false });
      });
  },

  onSearchInput(e) {
    const keyword = e.detail.value.trim();
    let filteredUsers = this.data.users;
    
    if (keyword) {
      filteredUsers = this.data.users.filter(user => 
        (user.nickname && user.nickname.includes(keyword)) ||
        (user.student_id && user.student_id.includes(keyword)) ||
        String(user.id).includes(keyword)
      );
    }
    
    this.setData({
      searchKeyword: keyword,
      filteredUsers
    });
  },

  clearSearch() {
    this.setData({
      searchKeyword: '',
      filteredUsers: this.data.users
    });
  },

  switchToUser(e) {
    const userId = e.currentTarget.dataset.id;
    const user = this.data.users.find(u => u.id === userId);
    
    if (!user) return;

    if (userId === this.data.currentUserId) {
      wx.showToast({
        title: '当前已是该账号',
        icon: 'none'
      });
      return;
    }

    wx.showModal({
      title: '切换账号',
      content: `确定要切换到「${user.nickname || '用户' + userId}」吗？`,
      confirmText: '确定切换',
      confirmColor: '#667eea',
      success: (res) => {
        if (res.confirm) {
          this.doSwitch(userId);
        }
      }
    });
  },

  doSwitch(userId) {
    this.setData({ switchingId: userId });
    
    api.devLogin(userId)
      .then(res => {
        console.log('切换账号成功:', res);
        
        if (res && res.code === 200 && res.data) {
          const { token, user, needBind } = res.data;
          
          app.setLoginInfo(token, user);
          
          wx.showToast({
            title: '切换成功',
            icon: 'success',
            duration: 1500
          });
          
          setTimeout(() => {
            if (needBind) {
              wx.redirectTo({
                url: '/pages/auth/bindStudent/bindStudent'
              });
            } else {
              wx.switchTab({
                url: '/pages/mine/index/index'
              });
            }
          }, 1500);
        } else {
          wx.showToast({
            title: res?.message || '切换失败',
            icon: 'none'
          });
        }
      })
      .catch(err => {
        console.error('切换账号失败:', err);
        wx.showToast({
          title: err.message || '切换失败，请重试',
          icon: 'none'
        });
      })
      .finally(() => {
        this.setData({ switchingId: null });
      });
  },

  previewAvatar(e) {
    const avatar = e.currentTarget.dataset.avatar;
    if (avatar && !avatar.startsWith('/static')) {
      wx.previewImage({
        urls: [avatar],
        current: avatar
      });
    }
  },

  goBack() {
    wx.navigateBack({
      fail: () => {
        wx.switchTab({ url: '/pages/mine/index/index' });
      }
    });
  }
});