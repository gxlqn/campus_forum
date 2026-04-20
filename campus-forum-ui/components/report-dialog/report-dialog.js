/**
 * 举报弹窗组件
 * 使用方式:
 * 1. 在页面的 wxml 中引入: <include src="/components/report-dialog/report-dialog.wxml" />
 * 2. 在 wxss 中引入: @import "/components/report-dialog/report-dialog.wxss";
 * 3. 在 js 中调用: showReportDialog(targetType, targetId)
 */
Component({
  properties: {
    visible: { type: Boolean, value: false }
  },

  data: {
    reasonTypes: [
      { value: 1, label: '垃圾广告' },
      { value: 2, label: '违法违规' },
      { value: 3, label: '色情低俗' },
      { value: 4, label: '人身攻击' },
      { value: 5, label: '抄袭搬运' },
      { value: 6, label: '虚假信息' },
      { value: 7, label: '其他' }
    ],
    selectedReason: null,
    reasonDetail: '',
    imageList: [],
    submitting: false
  },

  methods: {
    onClose() {
      this.setData({ visible: false });
    },

    onMaskTap() {
      this.onClose();
    },

    preventBubbling() {},

    preventMove() {},

    selectReason(e) {
      const value = e.currentTarget.dataset.value;
      this.setData({ selectedReason: value });
    },

    onReasonInput(e) {
      this.setData({ reasonDetail: e.detail.value });
    },

    chooseImage() {
      const that = this;
      wx.chooseMedia({
        count: 3,
        mediaType: ['image'],
        sourceType: ['album', 'camera'],
        success(res) {
          const newImages = res.tempFiles.map(f => f.tempFilePath);
          that.setData({
            imageList: (that.data.imageList || []).concat(newImages).slice(0, 3)
          });
        }
      });
    },

    removeImage(e) {
      const idx = e.currentTarget.dataset.idx;
      const images = [...this.data.imageList];
      images.splice(idx, 1);
      this.setData({ imageList: images });
    },

    previewImage(e) {
      const url = e.currentTarget.dataset.url;
      wx.previewImage({ urls: [url], current: url });
    },

    async onSubmit() {
      if (!this.data.selectedReason) {
        return wx.showToast({ title: '请选择举报原因', icon: 'none' });
      }

      // 先上传图片到服务器
      let uploadedUrls = [];
      if (this.data.imageList.length > 0) {
        wx.showLoading({ title: '上传图片中...' });
        try {
          for (const imgPath of this.data.imageList) {
            await new Promise((resolve, reject) => {
              wx.uploadFile({
                url: getApp().globalData.baseUrl + '/file/upload',
                filePath: imgPath,
                name: 'file',
                header: { 'Authorization': 'Bearer ' + wx.getStorageSync('token') },
                success(res) {
                  try { uploadedUrls.push(JSON.parse(res.data).data); resolve(); } catch { reject(); }
                },
                fail: reject
              });
            });
          }
        } catch (e) {
          wx.hideLoading();
          wx.showToast({ title: '图片上传失败', icon: 'none' });
          return;
        }
        wx.hideLoading();
      }

      const api = require('../../utils/api');
      this.setData({ submitting: true });

      try {
        await api.submitReport({
          targetType: this.properties._targetType,
          targetId: this.properties._targetId,
          reasonType: this.data.selectedReason,
          reason: this.data.reasonDetail.trim(),
          images: uploadedUrls.length > 0 ? JSON.stringify(uploadedUrls) : ''
        });

        wx.showToast({ title: '举报成功，感谢反馈', icon: 'success', duration: 2000 });
        setTimeout(() => this.onClose(), 1500);
      } catch (err) {
        // request.js 已处理错误提示
      } finally {
        this.setData({ submitting: false });
      }
    },

    resetForm() {
      this.setData({
        selectedReason: null,
        reasonDetail: '',
        imageList: [],
        submitting: false
      });
    },

    /**
     * 外部调用方法
     */
    show(targetType, targetId) {
      this.properties._targetType = targetType;
      this.properties._targetId = targetId;
      this.resetForm();
      this.setData({ visible: true });
    }
  }
});
