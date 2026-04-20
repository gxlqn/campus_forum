const api = require('../../../../utils/api');
const request = require('../../../../utils/request');

const PRODUCT_LIST_REFRESH_KEY = 'refresh_product_list';

Page({
    data: {
        mode: 'sell',
        modeText: '发布商品',
        categories: [
            { id: 1, name: '数码' },
            { id: 2, name: '书籍' },
            { id: 3, name: '生活' },
            { id: 4, name: '美妆' },
            { id: 5, name: '其他' }
        ],
        categoryIndex: 0,
        title: '',
        price: '',
        description: '',
        images: [],
        submitting: false,
        todayDate: '',
        wantedDate: '',
        timeOptions: [],
        timeIndex: -1
    },

    onLoad(options) {
        const mode = options?.mode === 'wanted' ? 'wanted' : 'sell';
        const timeOptions = this.buildTimeOptions();
        const now = new Date();
        const todayDate = this.formatDate(now);
        const defaultTimeIndex = this.getNearestTimeIndex(now, timeOptions);
        this.setData({ categoryIndex: 0 });
        if (mode === 'wanted') {
            wx.setNavigationBarTitle({ title: '发布求购' });
            this.setData({
                mode,
                modeText: '发布求购',
                todayDate,
                wantedDate: todayDate,
                timeOptions,
                timeIndex: defaultTimeIndex
            });
        } else {
            this.setData({ todayDate, timeOptions });
        }
    },

    buildTimeOptions() {
        const options = [];
        for (let hour = 0; hour < 24; hour++) {
            options.push(`${String(hour).padStart(2, '0')}:00`);
            options.push(`${String(hour).padStart(2, '0')}:30`);
        }
        return options;
    },

    formatDate(date) {
        const y = date.getFullYear();
        const m = String(date.getMonth() + 1).padStart(2, '0');
        const d = String(date.getDate()).padStart(2, '0');
        return `${y}-${m}-${d}`;
    },

    getNearestTimeIndex(date, timeOptions) {
        const minutes = date.getHours() * 60 + date.getMinutes() + 30;
        const step = 30;
        const rounded = Math.ceil(minutes / step) * step;
        const hour = Math.min(Math.floor(rounded / 60), 23);
        const min = rounded % 60 >= 30 ? '30' : '00';
        const timeText = `${String(hour).padStart(2, '0')}:${min}`;
        const idx = timeOptions.indexOf(timeText);
        return idx >= 0 ? idx : 0;
    },

    onWantedDateChange(e) {
        this.setData({ wantedDate: e.detail.value });
    },

    onWantedTimeChange(e) {
        this.setData({ timeIndex: Number(e.detail.value || 0) });
    },

    normalizeWantedDateTime() {
        const { wantedDate, timeOptions, timeIndex } = this.data;
        if (!wantedDate || timeIndex < 0 || !timeOptions[timeIndex]) {
            return '';
        }
        const normalized = `${wantedDate} ${timeOptions[timeIndex]}`;
        return normalized;
    },

    onCategoryChange(e) {
        const index = Number(e.detail.value || 0);
        this.setData({ categoryIndex: index });
    },

    onInput(e) {
        const field = e.currentTarget.dataset.field;
        this.setData({ [field]: e.detail.value });
    },

    chooseImage() {
        const count = 5 - this.data.images.length;
        if (count <= 0) {
            wx.showToast({ title: '最多上传 5 张图片', icon: 'none' });
            return;
        }
        wx.chooseImage({
            count,
            sizeType: ['compressed'],
            sourceType: ['album', 'camera'],
            success: (res) => {
                this.setData({ images: [...this.data.images, ...res.tempFilePaths] });
            }
        });
    },

    removeImage(e) {
        const index = Number(e.currentTarget.dataset.index);
        const images = [...this.data.images];
        images.splice(index, 1);
        this.setData({ images });
    },

    async uploadImages() {
        const { images } = this.data;
        if (!images.length) {
            return [];
        }
        const urls = [];
        for (const filePath of images) {
            try {
                const url = await request.uploadFile(filePath);
                urls.push(url);
            } catch (err) {
                console.error('图片上传失败', err);
                throw err;
            }
        }
        return urls;
    },

    async submit() {
        const { categories, categoryIndex, title, price, description, images } = this.data;
        if (!title.trim() || !price.trim() || !description.trim()) {
            wx.showToast({ title: '请完善商品标题、价格和描述', icon: 'none' });
            return;
        }

        const wantedDateTime = this.data.mode === 'wanted' ? this.normalizeWantedDateTime() : '';
        if (this.data.mode === 'wanted' && !wantedDateTime) {
            wx.showToast({ title: '请选择期望交易日期和时间', icon: 'none' });
            return;
        }

        const priceNum = parseFloat(price.trim());
        if (isNaN(priceNum) || priceNum <= 0) {
            wx.showToast({ title: '请输入有效的商品价格', icon: 'none' });
            return;
        }

        const categoryId = categories[categoryIndex]?.id;
        if (!categoryId) {
            wx.showToast({ title: '请选择商品分类', icon: 'none' });
            return;
        }

        this.setData({ submitting: true });

        try {
            const imageUrls = await this.uploadImages();
            const finalTitle = this.data.mode === 'wanted' && !title.trim().startsWith('[求购]')
                ? `[求购] ${title.trim()}`
                : title.trim();

            const finalDescription = this.data.mode === 'wanted'
                ? `${description.trim()}\n\n[期望交易时间] ${wantedDateTime}`
                : description.trim();

            await api.publishProduct({
                title: finalTitle,
                description: finalDescription,
                images: JSON.stringify(imageUrls),
                categoryId,
                price: price.trim(),
                originalPrice: price.trim(),
                tradeType: this.data.mode === 'wanted' ? 2 : 1,
                productCondition: 3,
                isNegotiable: 0,
                tradeLocation: ''
            });
            wx.setStorageSync(PRODUCT_LIST_REFRESH_KEY, true);
            wx.showToast({ title: '发布成功', icon: 'success' });
            setTimeout(() => {
                wx.navigateBack();
            }, 1200);
        } catch (err) {
            console.error('发布失败', err);
            wx.showToast({ title: '发布失败，请重试', icon: 'none' });
        } finally {
            this.setData({ submitting: false });
        }
    }
});
