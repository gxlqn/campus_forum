// pages/auth/login/login.js
const api = require('../../../utils/api');
const app = getApp();

Page({
    data: {
        loading: false
    },

    onLoad() {
        // 检查是否已登录
        const token = wx.getStorageSync('token');
        if (token) {
            this.goBack();
        }
    },

    /**
     * 直接执行登录流程
     */
    handleLogin() {
        if (this.data.loading) return;
        this.setData({ loading: true });

        // 执行登录流程
        this.doLogin();
    },

    /**
     * 执行登录（调用后端接口）
     */
    doLogin() {
        wx.login({
            success: (res) => {
                if (!res.code) {
                    wx.showToast({ 
                        title: '获取微信授权失败', 
                        icon: 'none' 
                    });
                    this.setData({ loading: false });
                    return;
                }
                
                const code = res.code;
                console.log('登录 code:', code);
                
                // 调用后端登录接口，只传递code
                api.wxLogin({ 
                    code
                })
                .then((result) => {
                    console.log('登录结果:', result);
                    
                    if (result && result.code === 200 && result.data) {
                        const { token, user, needBind } = result.data;
                        
                        // 保存登录信息
                        app.setLoginInfo(token, user);
                        
                        wx.showToast({ 
                            title: '登录成功', 
                            icon: 'success', 
                            duration: 1500 
                        });
                        
                        // 判断是否需要绑定学号
                        setTimeout(() => {
                            if (needBind) {
                                wx.navigateTo({
                                    url: '/pages/auth/bindStudent/bindStudent'
                                });
                            } else {
                                this.goBack();
                            }
                        }, 1500);
                    } else {
                        wx.showToast({ 
                            title: result?.message || '登录失败', 
                            icon: 'none' 
                        });
                    }
                })
                .catch((err) => {
                    console.error('登录失败:', err);
                    wx.showToast({ 
                        title: err.message || '登录失败，请重试', 
                        icon: 'none' 
                    });
                })
                .finally(() => {
                    this.setData({ loading: false });
                });
            },
            fail: (err) => {
                console.error('wx.login 失败:', err);
                this.setData({ loading: false });
                wx.showToast({ 
                    title: '微信登录失败', 
                    icon: 'none' 
                });
            }
        });
    },

    /**
     * 返回上一页或跳转首页
     */
    goBack() {
        const pages = getCurrentPages();
        if (pages.length > 1) {
            wx.navigateBack({ delta: 1 });
        } else {
            wx.switchTab({ url: '/pages/index/index' });
        }
    }
});