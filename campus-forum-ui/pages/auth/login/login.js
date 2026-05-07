// pages/auth/login/login.js
const api = require("../../../utils/api");
const app = getApp();

Page({
    data: {
        loading: false
    },

    onLoad() {
        // 检查是否已登录
        const token = wx.getStorageSync("token");
        if (token) {
            this.goBack();
        }
    },

    /**
     * 点击协议链接
     */
    onTapAgreement(e) {
        const type = e.currentTarget.dataset.type;
        let title = type === "privacy" ? "隐私政策" : "用户协议";
        let content = type === "privacy"
            ? "我们非常重视您的个人隐私保护。本应用仅收集必要信息用于提供校园服务功能，包括：\n\n• 微信授权基本信息（头像、昵称）\n• 学号/工号（用于身份认证）\n• 发布内容（帖子、商品等）\n\n您的所有数据均经过加密存储，不会向第三方透露。"
            : "欢迎使用校园服务论坛！使用本服务即表示您同意以下条款：\n\n1. 遵守国家法律法规，不发布违法违规内容\n2. 尊重他人，文明交流\n3. 真实发布交易信息，诚信交易\n4. 不得利用平台进行任何商业欺诈行为\n5. 平台有权对违规内容进行审核和处理";
            
        wx.showModal({
            title: title,
            content: content,
            showCancel: false,
            confirmText: "我知道了",
            confirmColor: "#FF6B9D"
        });
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
                        title: "获取微信授权失败", 
                        icon: "none" 
                    });
                    this.setData({ loading: false });
                    return;
                }
                
                const code = res.code;
                console.log("登录 code:", code);
                
                // 调用后端登录接口，只传递code
                api.wxLogin({ 
                    code
                })
                .then((result) => {
                    console.log("登录结果:", result);
                    
                    if (result && result.code === 200 && result.data) {
                        const { token, user, needBind } = result.data;
                        
                        // 保存登录信息
                        app.setLoginInfo(token, user);
                        
                        wx.showToast({ 
                            title: "登录成功", 
                            icon: "success", 
                            duration: 1500 
                        });
                        
                        // 判断是否需要绑定学号
                        setTimeout(() => {
                            if (needBind) {
                                wx.navigateTo({
                                    url: "/pages/auth/bindStudent/bindStudent"
                                });
                            } else {
                                this.goBack();
                            }
                        }, 1500);
                    } else {
                        wx.showToast({ 
                            title: result?.message || "登录失败", 
                            icon: "none" 
                        });
                    }
                })
                .catch((err) => {
                    console.error("登录失败:", err);
                    wx.showToast({ 
                        title: err.message || "登录失败，请重试", 
                        icon: "none" 
                    });
                })
                .finally(() => {
                    this.setData({ loading: false });
                });
            },
            fail: (err) => {
                console.error("wx.login 失败:", err);
                this.setData({ loading: false });
                wx.showToast({ 
                    title: "微信登录失败", 
                    icon: "none" 
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
            wx.switchTab({ url: "/pages/index/index" });
        }
    }
});
