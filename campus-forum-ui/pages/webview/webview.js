/**
 * WebView Page - 用于展示外部链接内容
 */
Page({
    data: {
        url: ''
    },

    onLoad(options) {
        if (options.url) {
            try {
                const decodedUrl = decodeURIComponent(options.url);
                // 安全检查：仅允许 https 协议
                if (decodedUrl.startsWith('https://') || decodedUrl.startsWith('http://')) {
                    this.setData({ url: decodedUrl });
                } else {
                    wx.showToast({ title: '不安全的链接', icon: 'none' });
                    setTimeout(() => wx.navigateBack(), 1500);
                }
            } catch (e) {
                wx.showToast({ title: '链接解析失败', icon: 'none' });
                setTimeout(() => wx.navigateBack(), 1500);
            }
        } else {
            wx.showToast({ title: '缺少链接参数', icon: 'none' });
            setTimeout(() => wx.navigateBack(), 1500);
        }
    }
});
