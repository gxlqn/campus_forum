// pages/service/lostfound/publish/publish.js
const api = require('../../../../utils/api');
const request = require('../../../../utils/request');

const LOSTFOUND_LIST_REFRESH_KEY = 'refresh_lostfound_list';

Page({
    data: {
        type: 1, // 1:寻物 2:招领
        title: '',
        itemName: '',
        desc: '',
        location: '',
        locationName: '',
        locationAddress: '',
        latitude: null,
        longitude: null,
        date: '',
        time: '',
        contact: '',
        images: [],
        tags: ['钱包', '证件', '电子产品', '书籍', '衣物', '钥匙', '雨伞', '其他'],
        selectedTags: [],
        uploading: false,
        privacyResolved: false
    },

    onLoad() {
        const now = new Date();
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0');
        const day = String(now.getDate()).padStart(2, '0');
        const hours = String(now.getHours()).padStart(2, '0');
        const minutes = String(now.getMinutes()).padStart(2, '0');
        
        this.setData({
            date: `${year}-${month}-${day}`,
            time: `${hours}:${minutes}`
        });
        
        this.initPrivacyHandler();
    },
    
    initPrivacyHandler() {
        if (wx.onNeedPrivacyAuthorization) {
            wx.onNeedPrivacyAuthorization((resolve) => {
                console.log('需要隐私授权');
                wx.showModal({
                    title: '隐私授权提示',
                    content: '本功能需要使用您的位置信息和相册权限，是否同意？',
                    confirmText: '同意',
                    cancelText: '拒绝',
                    success: (res) => {
                        if (res.confirm) {
                            resolve({ event: 'agree' });
                        } else {
                            resolve({ event: 'disagree' });
                        }
                    }
                });
            });
        }
    },

    // 切换类型
    switchType(e) {
        const type = parseInt(e.currentTarget.dataset.type);
        this.setData({ type });
    },

    // 选择图片（修复版）
    async chooseImage() {
        const remainCount = 3 - this.data.images.length;
        if (remainCount <= 0) {
            wx.showToast({ 
                title: '最多上传3张图片', 
                icon: 'none' 
            });
            return;
        }
        
        try {
            if (wx.requirePrivacyAuthorize) {
                await wx.requirePrivacyAuthorize();
            }
            
            wx.chooseImage({
                count: remainCount,
                sizeType: ['compressed'],
                sourceType: ['album', 'camera'],
                success: (res) => {
                    this.setData({
                        images: [...this.data.images, ...res.tempFilePaths]
                    });
                },
                fail: (err) => {
                    console.error('选择图片失败', err);
                    if (err.errMsg && err.errMsg.includes('cancel')) {
                        // 用户取消，不提示
                    } else if (err.errMsg && err.errMsg.includes('privacy')) {
                        wx.showToast({ 
                            title: '请先同意隐私协议', 
                            icon: 'none' 
                        });
                    } else {
                        wx.showToast({ 
                            title: '选择图片失败', 
                            icon: 'none' 
                        });
                    }
                }
            });
        } catch (privacyErr) {
            console.error('隐私授权失败', privacyErr);
            wx.showToast({ 
                title: '需要隐私授权才能使用此功能', 
                icon: 'none' 
            });
        }
    },

    // 删除图片
    deleteImage(e) {
        const index = e.currentTarget.dataset.index;
        const images = this.data.images;
        images.splice(index, 1);
        this.setData({ images });
    },

    // 物品类型选择回调（Picker）
    onTagChange(e) {
        const index = e.detail.value;
        const selectedTag = this.data.tags[index];
        this.setData({
            itemName: selectedTag,
            selectedTags: [selectedTag]
        });
    },

    // 选择位置（简化版）
    async chooseLocation() {
        try {
            if (wx.requirePrivacyAuthorize) {
                await wx.requirePrivacyAuthorize();
            }
            
            wx.chooseLocation({
                success: (res) => {
                    console.log('选择位置成功', res);
                    const locationText = res.name || res.address || '';
                    this.setData({
                        location: locationText,
                        locationName: res.name || '',
                        locationAddress: res.address || '',
                        latitude: res.latitude,
                        longitude: res.longitude
                    });
                },
                fail: (err) => {
                    console.error('选择位置失败', err);
                    
                    // 用户取消选择
                    if (err.errMsg && err.errMsg.includes('cancel')) {
                        return;
                    }
                    
                    // 隐私协议问题
                    if (err.errMsg && (err.errMsg.includes('privacy') || err.errMsg.includes('api scope'))) {
                        wx.showModal({
                            title: '需要隐私授权',
                            content: '使用位置功能需要您同意隐私保护指引，是否前往设置？',
                            confirmText: '去设置',
                            success: (res) => {
                                if (res.confirm) {
                                    wx.openPrivacyContract();
                                }
                            }
                        });
                        return;
                    }
                    
                    // 权限问题
                    if (err.errMsg && err.errMsg.includes('auth deny')) {
                        wx.showModal({
                            title: '需要位置权限',
                            content: '请在设置中开启位置权限，以便选择准确的失物招领地点',
                            confirmText: '去设置',
                            success: (res) => {
                                if (res.confirm) {
                                    wx.openSetting();
                                }
                            }
                        });
                    } else {
                        wx.showToast({ 
                            title: '选择位置失败', 
                            icon: 'none' 
                        });
                    }
                }
            });
        } catch (privacyErr) {
            console.error('隐私授权失败', privacyErr);
            wx.showToast({ 
                title: '需要隐私授权才能使用此功能', 
                icon: 'none' 
            });
        }
    },

    // 日期选择
    bindDateChange(e) {
        this.setData({ date: e.detail.value });
    },

    // 时间选择
    bindTimeChange(e) {
        this.setData({ time: e.detail.value });
    },

    // 输入处理
    onInput(e) {
        const { field } = e.currentTarget.dataset;
        this.setData({ [field]: e.detail.value });
    },

    // 提交
    async submit() {
        const app = getApp();
        if (!app.globalData.isLogin) {
            wx.showModal({
                title: '需要登录',
                content: '请先登录后再发布失物招领信息',
                confirmText: '去登录',
                cancelText: '取消',
                success: (res) => {
                    if (res.confirm) {
                        wx.navigateTo({ url: '/pages/auth/login/login' });
                    }
                }
            });
            return;
        }

        const { title, itemName, desc, location, contact, images, date, time, selectedTags, type } = this.data;
        
        // 表单验证
        if (!title.trim()) {
            wx.showToast({ title: '请填写标题', icon: 'none' });
            return;
        }
        if (!itemName) {
            wx.showToast({ title: '请选择物品类型', icon: 'none' });
            return;
        }
        if (!desc.trim()) {
            wx.showToast({ title: '请填写详细描述', icon: 'none' });
            return;
        }
        if (!location) {
            wx.showToast({ title: '请选择地点', icon: 'none' });
            return;
        }
        if (!contact.trim()) {
            wx.showToast({ title: '请填写联系方式', icon: 'none' });
            return;
        }

        this.setData({ uploading: true });
        wx.showLoading({ title: '发布中...' });

        try {
            // 上传图片
            const imageUrls = [];
            if (images.length > 0) {
                for (const filePath of images) {
                    try {
                        const url = await request.uploadFile(filePath);
                        imageUrls.push(url);
                    } catch (uploadErr) {
                        console.error('图片上传失败', uploadErr);
                        throw new Error('图片上传失败');
                    }
                }
            }

            // 构建提交数据
            const lostTime = `${date}T${time}:00`;
            const payload = {
                type,
                title: title.trim(),
                description: desc.trim(),
                images: imageUrls.length > 0 ? JSON.stringify(imageUrls) : '[]',
                itemName: itemName.trim(),
                itemCategory: selectedTags.length > 0 ? selectedTags.join(',') : itemName,
                lostTime,
                lostLocation: location.trim(),
                latitude: this.data.latitude,
                longitude: this.data.longitude,
                contactPhone: contact.trim(),
                contactName: '',
                contactWechat: '',
                reward: null
            };

            console.log('提交数据:', payload);
            
            const result = await api.publishLostFound(payload);
            console.log('发布结果:', result);

            wx.setStorageSync(LOSTFOUND_LIST_REFRESH_KEY, true);
            
            wx.hideLoading();
            wx.showToast({ 
                title: '发布成功', 
                icon: 'success' 
            });
            
            setTimeout(() => {
                wx.navigateBack();
            }, 1500);
            
        } catch (err) {
            console.error('失物发布失败', err);
            wx.hideLoading();
            wx.showToast({ 
                title: err.message || '发布失败，请重试', 
                icon: 'none' 
            });
        } finally {
            this.setData({ uploading: false });
        }
    }
});