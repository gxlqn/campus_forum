/**
 * 发帖页
 */
const api = require('../../../utils/api');
const http = require('../../../utils/request');
const imageHelper = require('../../../utils/imageHelper');

const FORUM_LIST_REFRESH_KEY = 'refresh_forum_list';

Page({
    data: {
        isEdit: false,
        editPostId: null,
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
        // 编辑模式：从参数中读取帖子数据
        if (options.edit === '1' && options.postId) {
            this.setData({
                isEdit: true,
                editPostId: options.postId,
                sectionId: options.sectionId || null,
                title: decodeURIComponent(options.title || ''),
                content: decodeURIComponent(options.content || ''),
                isAnonymous: parseInt(options.isAnonymous) === 1
            });
            try {
                const imagesArr = JSON.parse(decodeURIComponent(options.images || '[]'));
                this.setData({ images: Array.isArray(imagesArr) ? imagesArr : [] });
            } catch (e) {
                this.setData({ images: [] });
            }
            wx.setNavigationBarTitle({ title: '编辑帖子' });
        }
        this.loadSections();
    },

    // 加载板块
    async loadSections() {
        try {
            const res = await api.getSections();
            const sections = (res && res.data) || [];
            this.setData({ sections });
            // 编辑模式下，设置板块名称
            if (this.data.isEdit && this.data.sectionId) {
                const sec = sections.find(s => String(s.id) === String(this.data.sectionId));
                if (sec) {
                    this.setData({ sectionName: sec.sectionName });
                }
            }
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

    // 发布/编辑
    async submit() {
        const { sectionId, title, content, images, isAnonymous, isEdit, editPostId } = this.data;

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
            const postData = {
                sectionId: sectionId,
                title: title.trim(),
                content: content.trim(),
                images: images.length > 0 ? JSON.stringify(images) : null,
                isAnonymous: isAnonymous ? 1 : 0
            };

            if (isEdit) {
                await api.updatePost(editPostId, postData);
                wx.showToast({ title: '修改成功', icon: 'success', duration: 1500 });
            } else {
                const res = await api.publishPost(postData);
                const post = res.data;
                if (post && post.auditStatus === 0) {
                    wx.showToast({ title: '发布成功，等待审核', icon: 'none', duration: 1500 });
                } else {
                    wx.showToast({ title: '发布成功', icon: 'success', duration: 1500 });
                }
            }

            wx.setStorageSync(FORUM_LIST_REFRESH_KEY, true);

            setTimeout(() => {
                wx.navigateBack();
            }, 1500);
        } catch (err) {
            console.error('提交失败', err);
            if (err && !err.message) {
                wx.showToast({ title: '提交失败，请稍后重试', icon: 'none' });
            }
        } finally {
            this.setData({ submitting: false });
        }
    }
});
