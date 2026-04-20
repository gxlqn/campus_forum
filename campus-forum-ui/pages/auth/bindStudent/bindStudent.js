/**
 * 绑定学号/工号页
 */
const api = require('../../../utils/api');
const app = getApp();

Page({
    data: {
        studentId: '',
        userType: 1,
        typeOptions: ['学生', '教师'],
        submitting: false
    },

    onStudentIdInput(e) {
        this.setData({
            studentId: (e.detail.value || '').trim()
        });
    },

    onTypeChange(e) {
        const index = Number(e.detail.value || 0);
        this.setData({
            userType: index === 1 ? 2 : 1
        });
    },

    async submitBind() {
        if (this.data.submitting) {
            return;
        }
        const studentId = this.data.studentId;
        if (!studentId) {
            wx.showToast({
                title: '请输入学号或工号',
                icon: 'none'
            });
            return;
        }

        const userInfo = wx.getStorageSync('userInfo') || {};
        if (!userInfo.id) {
            wx.showToast({
                title: '登录信息失效，请重新登录',
                icon: 'none'
            });
            return;
        }

        this.setData({ submitting: true });
        try {
            await api.bindStudentId(studentId, this.data.userType);

            const updatedUserInfo = Object.assign({}, userInfo, {
                studentId,
                userType: this.data.userType,
                isVerified: 1
            });
            wx.setStorageSync('userInfo', updatedUserInfo);
            if (app.globalData) {
                app.globalData.userInfo = updatedUserInfo;
            }

            wx.showToast({
                title: '绑定成功',
                icon: 'success'
            });
            setTimeout(() => {
                wx.switchTab({
                    url: '/pages/index/index'
                });
            }, 1200);
        } catch (err) {
            console.error('绑定失败', err);
        } finally {
            this.setData({ submitting: false });
        }
    }
});
