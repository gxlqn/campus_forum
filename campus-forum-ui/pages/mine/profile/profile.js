/**
 * 个人资料页 */
const api = require('../../../utils/api');
const http = require('../../../utils/request');
const imageHelper = require('../../../utils/imageHelper');

const app = getApp();

Page({
    data: {
        loading: false,
        submitting: false,
        canSubmit: false,
        form: {
            avatar: '',
            nickname: '',
            bio: '',
            phone: '',
            email: '',
            college: '',
            major: '',
            grade: ''
        }
    },

    onLoad() {
        this.loadProfile();
    },

    async loadProfile() {
        this.setData({ loading: true });
        try {
            const res = await api.getUserInfo();
            const user = res.data || {};
            this.setData({
                form: {
                    avatar: imageHelper.getFullImageUrl(user.avatar) || '',
                    nickname: user.nickname || '',
                    bio: user.bio || '',
                    phone: user.phone || '',
                    email: user.email || '',
                    college: user.college || '',
                    major: user.major || '',
                    grade: user.grade || ''
                }
            });
        } catch (err) {
            console.error('加载个人资料失败', err);
            wx.showToast({
                title: '加载失败',
                icon: 'none'
            });
        } finally {
            this.setData({ loading: false });
            this.updateCanSubmit();
        }
    },

    onInput(e) {
        const field = e.currentTarget.dataset.field;
        const value = e.detail.value;
        this.setData({
            [`form.${field}`]: value
        });
        this.updateCanSubmit();
    },

    updateCanSubmit() {
        const nickname = (this.data.form.nickname || '').trim();
        this.setData({
            canSubmit: nickname.length > 0
        });
    },

    async chooseAvatar() {
        wx.chooseImage({
            count: 1,
            sizeType: ['compressed'],
            sourceType: ['album', 'camera'],
            success: async (res) => {
                const filePath = res.tempFilePaths && res.tempFilePaths[0];
                if (!filePath) {
                    return;
                }
                wx.showLoading({ title: '上传中...' });
                try {
                    const url = await http.uploadFile(filePath);
                    this.setData({
                        'form.avatar': imageHelper.getFullImageUrl(url)
                    });
                } catch (err) {
                    console.error('上传头像失败', err);
                } finally {
                    wx.hideLoading();
                }
            }
        });
    },

    async submit() {
        if (this.data.submitting) {
            return;
        }
        const form = this.data.form;
        if (!form.nickname || !form.nickname.trim()) {
            wx.showToast({
                title: '昵称不能为空',
                icon: 'none'
            });
            return;
        }

        this.setData({ submitting: true });
        try {
            const payload = {
                avatar: (form.avatar || '').trim(),
                nickname: (form.nickname || '').trim(),
                bio: (form.bio || '').trim(),
                phone: (form.phone || '').trim(),
                email: (form.email || '').trim(),
                college: (form.college || '').trim(),
                major: (form.major || '').trim(),
                grade: (form.grade || '').trim()
            };
            const res = await api.updateUserInfo(payload);
            const userInfo = wx.getStorageSync('userInfo') || {};
            const latest = Object.assign({}, userInfo, res.data || payload);
            wx.setStorageSync('userInfo', latest);
            if (app.globalData) {
                app.globalData.userInfo = latest;
            }

            wx.showToast({
                title: '保存成功',
                icon: 'success'
            });
            setTimeout(() => {
                wx.navigateBack();
            }, 1000);
        } catch (err) {
            console.error('保存个人资料失败', err);
        } finally {
            this.setData({ submitting: false });
        }
    }
});
