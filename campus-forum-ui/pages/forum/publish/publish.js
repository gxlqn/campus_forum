/**
 * 发帖页
 */
const api = require('../../../utils/api');
const http = require('../../../utils/request');
const imageHelper = require('../../../utils/imageHelper');

const FORUM_LIST_REFRESH_KEY = 'refresh_forum_list';

Page({
    data: {
        sections: [],
        sectionId: null,
        sectionName: '选择板块',
        title: '',
        content: '',
        images: [],
        isAnonymous: false,
        submitting: false
    },

    onLoad(options) {
        if (options.sectionId) {
            this.setData({
                sectionId: options.sectionId,
                sectionName: options.sectionName || '选择板块'
            });
        }
        this.loadSections();
    },

    // 加载板块
    async loadSections() {
        try {
            const res = await api.getSections();
            this.setData({ sections: (res && res.data) || [] });
        } catch (err) {
            console.error('加载板块失败', err);
        }
    },

    // 选择板块
    onSectionChange(e) {
        const index = e.detail.value;
        const section = this.data.sections[index];
        if (section) {
            this.setData({
                sectionId: section.id,
                sectionName: section.sectionName
            });
        }
    },

    // 输入标题
    onTitleInput(e) {
        this.setData({ title: e.detail.value });
    },

    // 输入内容
    onContentInput(e) {
        this.setData({ content: e.detail.value });
    },

    // 选择图片
    chooseImage() {
        const count = 9 - this.data.images.length;
        if (count <= 0) {
            wx.showToast({ title: '最多上传9张图片', icon: 'none' });
            return;
        }

        wx.chooseImage({
            count: count,
            sizeType: ['compressed'],
            sourceType: ['album', 'camera'],
            success: (res) => {
                this.uploadImages(res.tempFilePaths);
            }
        });
    },

    // 上传图片
    async uploadImages(tempFiles) {
        wx.showLoading({ title: '上传中...' });

        try {
            const images = [...this.data.images];

            for (const filePath of tempFiles) {
                const url = await http.uploadFile(filePath);
                images.push(imageHelper.getFullImageUrl(url));
            }

            this.setData({ images });
        } catch (err) {
            wx.showToast({ title: '图片上传失败', icon: 'none' });
        } finally {
            wx.hideLoading();
        }
    },

    // 删除图片
    removeImage(e) {
        const index = e.currentTarget.dataset.index;
        const images = this.data.images;
        images.splice(index, 1);
        this.setData({ images });
    },

    // 切换匿名
    toggleAnonymous() {
        this.setData({ isAnonymous: !this.data.isAnonymous });
    },

    // 发布
    async submit() {
        const { sectionId, title, content, images, isAnonymous } = this.data;

        // 验证
        if (!sectionId) {
            wx.showToast({ title: '请选择板块', icon: 'none' });
            return;
        }
        if (!title.trim()) {
            wx.showToast({ title: '请输入标题', icon: 'none' });
            return;
        }
        if (!content.trim()) {
            wx.showToast({ title: '请输入内容', icon: 'none' });
            return;
        }

        this.setData({ submitting: true });

        try {
            const res = await api.publishPost({
                sectionId: sectionId,
                title: title.trim(),
                content: content.trim(),
                images: images.length > 0 ? JSON.stringify(images) : null,
                isAnonymous: isAnonymous ? 1 : 0
            });

            // 根据审核状态给用户不同提示
            const post = res.data;
            if (post && post.auditStatus === 0) {
                wx.showToast({
                    title: '发布成功，等待审核',
                    icon: 'none',
                    duration: 1500
                });
            } else {
                wx.showToast({
                    title: '发布成功',
                    icon: 'success',
                    duration: 1500
                });
            }

            wx.setStorageSync(FORUM_LIST_REFRESH_KEY, true);

            setTimeout(() => {
                wx.navigateBack();
            }, 1500);
        } catch (err) {
            console.error('发布失败', err);
            // request.js 已对服务端错误自动 showToast，此处补充兜底提示
            if (err && !err.message) {
                wx.showToast({ title: '发布失败，请稍后重试', icon: 'none' });
            }
        } finally {
            this.setData({ submitting: false });
        }
    }
});
