/**
 * Search Page - ui-ux-pro-max 升级版
 */
const api = require('../../utils/api');

const SEARCH_HISTORY_KEY = 'search_history_keywords';
const DEFAULT_RECOMMEND_KEYWORDS = [
  '二手', '跑腿', '活动', '失物', '课程资料', '兼职', '拼车', '考研'
];
const HL_START = '__HL_START__';
const HL_END = '__HL_END__';
const MAX_SNIPPET_LEN = 140;
const SUGGEST_DEBOUNCE_MS = 300;

Page({
  data: {
    keyword: '',
    autoFocus: true,
    loading: false,
    hasSearched: false,
    showSuggestions: false,
    suggestList: [],
    merged: [],
    sections: [],
    posts: [],
    products: [],
    activities: [],
    helps: [],
    correctedKeyword: '',
    suggestions: [],
    historyKeywords: [],
    hotRecommendKeywords: [],
    recommendKeywords: [],
    totalCount: 0
  },

  _suggestTimer: null,

  onLoad(options) {
    this.loadHistoryKeywords();
    this.loadRecommendKeywords();
    if (options.keyword) {
      const keyword = decodeURIComponent(options.keyword);
      this.setData({ keyword });
      this.search(keyword);
    }
  },

  onShow() {
    this.loadHistoryKeywords();
    this.loadRecommendKeywords();
  },

  onKeywordInput(e) {
    const keyword = e.detail.value || '';
    this.setData({ keyword });

    if (this._suggestTimer) {
      clearTimeout(this._suggestTimer);
    }

    if (!keyword.trim()) {
      this.setData({ showSuggestions: false, suggestList: [] });
      return;
    }

    this._suggestTimer = setTimeout(() => {
      this.fetchSuggestions(keyword);
    }, SUGGEST_DEBOUNCE_MS);
  },

  async fetchSuggestions(keyword) {
    if (typeof api.searchSuggest !== 'function') {
      console.warn('searchSuggest 未定义，跳过实时建议');
      this.setData({ showSuggestions: false, suggestList: [] });
      return;
    }
    try {
      const res = await api.searchSuggest({ keyword, size: 6 });
      const list = Array.isArray(res.data) ? res.data : [];
      const suggestList = list.map(item => ({
        ...item,
        highlight: this.highlightKeyword(item, keyword)
      }));
      this.setData({ showSuggestions: true, suggestList });
    } catch (err) {
      console.warn('获取搜索建议失败', err);
      this.setData({ showSuggestions: false, suggestList: [] });
    }
  },

  highlightKeyword(text, keyword) {
    if (!text || !keyword) return text;
    const escaped = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    const reg = new RegExp(`(${escaped})`, 'ig');
    return String(text).replace(reg, '<span style="color:#D6336C;font-weight:700;">$1</span>');
  },

  onClearKeyword() {
    this.setData({
      keyword: '',
      showSuggestions: false,
      suggestList: [],
      autoFocus: true
    });
  },

  onSearch() {
    this.setData({ showSuggestions: false });
    this.search(this.data.keyword);
  },

  async search(keyword) {
    if (!keyword || !keyword.trim()) {
      wx.showToast({ title: '请输入搜索关键字', icon: 'none' });
      return;
    }
    const normalized = keyword.trim();
    this.saveHistoryKeyword(normalized);
    this.setData({ loading: true, hasSearched: true, showSuggestions: false });

    try {
      const res = await api.searchAll({ keyword: normalized, size: 12 });
      const data = res.data || {};
      const transformed = this.buildDisplayLists(data, normalized);
      const totalCount =
        (transformed.merged.length || 0) +
        (data.sections || []).length +
        (transformed.posts.length || 0) +
        (transformed.products.length || 0) +
        (transformed.activities.length || 0) +
        (transformed.helps.length || 0);

      this.setData({
        hasSearched: true,
        merged: transformed.merged,
        sections: data.sections || [],
        posts: transformed.posts,
        products: transformed.products,
        activities: transformed.activities,
        helps: transformed.helps,
        correctedKeyword: this.pickCorrectedKeyword(normalized, data.correctedKeyword, data.suggestions),
        suggestions: this.normalizeSuggestions(normalized, data.suggestions),
        totalCount
      });
    } catch (err) {
      console.error('搜索失败', err);
      wx.showToast({ title: '搜索失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  goToPost(e) {
    const id = e.currentTarget.dataset.id;
    if (id) {
      wx.navigateTo({ url: `/pages/forum/detail/detail?id=${id}` });
    }
  },

  goToProduct(e) {
    const id = e.currentTarget.dataset.id;
    if (id) {
      wx.navigateTo({ url: `/pages/service/product/detail/detail?id=${id}` });
    }
  },

  goToMerged(e) {
    const id = e.currentTarget.dataset.id;
    const type = e.currentTarget.dataset.type;
    if (!id || !type) return;

    const routes = {
      post: `/pages/forum/detail/detail?id=${id}`,
      product: `/pages/service/product/detail/detail?id=${id}`,
      activity: `/pages/service/activity/detail/detail?id=${id}`,
      help: `/pages/service/help/detail/detail?id=${id}`
    };

    const url = routes[type];
    if (url) {
      wx.navigateTo({ url });
    }
  },

  tapKeyword(e) {
    const keyword = e.currentTarget.dataset.keyword;
    if (!keyword) return;
    this.setData({ keyword, showSuggestions: false });
    this.search(keyword);
  },

  clearHistory() {
    wx.showModal({
      title: '提示',
      content: '确定要清空搜索历史吗？',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync(SEARCH_HISTORY_KEY);
          this.setData({ historyKeywords: [] });
          this.refreshRecommendKeywords(this.data.hotRecommendKeywords || []);
          wx.showToast({ title: '已清空', icon: 'none' });
        }
      }
    });
  },

  loadHistoryKeywords() {
    const list = wx.getStorageSync(SEARCH_HISTORY_KEY);
    this.setData({ historyKeywords: Array.isArray(list) ? list : [] });
  },

  saveHistoryKeyword(keyword) {
    const current = wx.getStorageSync(SEARCH_HISTORY_KEY);
    const list = Array.isArray(current) ? current : [];
    const deduped = [keyword, ...list.filter((item) => item !== keyword)].slice(0, 12);
    wx.setStorageSync(SEARCH_HISTORY_KEY, deduped);
    this.setData({ historyKeywords: deduped });
    this.refreshRecommendKeywords(this.data.hotRecommendKeywords || []);
  },

  async loadRecommendKeywords() {
    try {
      const res = await api.getSearchRecommend({ size: 10 });
      const hotRecommendKeywords = Array.isArray(res.data) ? res.data : [];
      this.setData({ hotRecommendKeywords });
      this.refreshRecommendKeywords(hotRecommendKeywords);
    } catch (err) {
      console.warn('加载推荐搜索词失败，使用本地默认词', err);
      this.setData({ hotRecommendKeywords: [] });
      this.refreshRecommendKeywords(DEFAULT_RECOMMEND_KEYWORDS);
    }
  },

  refreshRecommendKeywords(dynamicKeywords = []) {
    const history = this.data.historyKeywords || [];
    const source = dynamicKeywords.length ? dynamicKeywords : DEFAULT_RECOMMEND_KEYWORDS;
    const merged = [...history, ...source];
    const unique = Array.from(new Set(merged)).slice(0, 10);
    this.setData({ recommendKeywords: unique });
  },

  buildDisplayLists(data, keyword) {
    return {
      merged: this.decorateMergedList(data.merged, keyword),
      posts: this.decorateList(data.posts, keyword, 'content'),
      products: this.decorateList(data.products, keyword, 'description'),
      activities: this.decorateList(data.activities, keyword, 'description'),
      helps: this.decorateList(data.helps, keyword, 'description')
    };
  },

  decorateMergedList(list, keyword) {
    if (!Array.isArray(list)) return [];
    return list.map((item) => {
      const highlight = item && item._highlight ? item._highlight : {};
      const displayTitle = this.toHighlightHtml(item && item.title, highlight.title, keyword);
      const highlightDesc = this.pickFirstNonEmpty([
        highlight.content,
        highlight.description,
        highlight.location,
        highlight.pickupLocation,
        highlight.deliveryLocation
      ]);
      const displayDesc = this.toHighlightHtml(item && item.snippet, highlightDesc, keyword, MAX_SNIPPET_LEN);
      return {
        ...item,
        typeLabel: this.getTypeLabel(item && item.type),
        typeIcon: this.getTypeIcon(item && item.type),
        displayTitle,
        displayDesc
      };
    });
  },

  getTypeIcon(type) {
    const icons = {
      post: '📝',
      product: '🛒',
      activity: '🎉',
      help: '🤝'
    };
    return icons[type] || '📄';
  },

  getTypeLabel(type) {
    const labels = {
      post: '帖子',
      product: '商品',
      activity: '活动',
      help: '互助'
    };
    return labels[type] || '综合';
  },

  decorateList(list, keyword, descField) {
    if (!Array.isArray(list)) return [];
    return list.map((item) => {
      const highlight = item && item._highlight ? item._highlight : {};
      const displayTitle = this.toHighlightHtml(item && item.title, highlight.title, keyword);
      const highlightDesc = this.pickFirstNonEmpty([
        highlight[descField],
        highlight.content,
        highlight.description,
        highlight.location,
        highlight.pickupLocation,
        highlight.deliveryLocation
      ]);
      const rawDesc = item ? item[descField] : '';
      const displayDesc = this.toHighlightHtml(rawDesc, highlightDesc, keyword, MAX_SNIPPET_LEN);
      return { ...item, displayTitle, displayDesc };
    });
  },

  pickFirstNonEmpty(list) {
    if (!Array.isArray(list)) return '';
    for (let i = 0; i < list.length; i++) {
      if (list[i]) return list[i];
    }
    return '';
  },

  toHighlightHtml(rawText, esHighlightText, keyword, limit) {
    let text = esHighlightText || rawText || '';
    if (!text) return '';
    if (typeof limit === 'number' && limit > 0 && text.length > limit) {
      text = `${text.slice(0, limit)}...`;
    }
    text = String(text)
      .replace(/<em[^>]*>/gi, HL_START)
      .replace(/<\/em>/gi, HL_END);

    if (!esHighlightText && keyword) {
      text = this.injectKeywordMarkers(text, keyword);
    }

    text = this.escapeHtml(text)
      .replace(new RegExp(HL_START, 'g'), '<span style="color:#D6336C;font-weight:700;">')
      .replace(new RegExp(HL_END, 'g'), '</span>')
      .replace(/\n/g, '<br/>');

    return text;
  },

  injectKeywordMarkers(text, keyword) {
    const safeKeyword = String(keyword || '').trim();
    if (!safeKeyword) return text;
    const escaped = safeKeyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    const reg = new RegExp(escaped, 'ig');
    return String(text).replace(reg, (matched) => `${HL_START}${matched}${HL_END}`);
  },

  escapeHtml(text) {
    return String(text || '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  },

  pickCorrectedKeyword(currentKeyword, correctedKeyword, suggestions) {
    const normalizedCurrent = String(currentKeyword || '').trim().toLowerCase();
    const normalizedCorrected = String(correctedKeyword || '').trim();
    if (normalizedCorrected && normalizedCorrected.toLowerCase() !== normalizedCurrent) {
      return normalizedCorrected;
    }
    const list = this.normalizeSuggestions(currentKeyword, suggestions);
    return list.length ? list[0] : '';
  },

  normalizeSuggestions(currentKeyword, suggestions) {
    const normalizedCurrent = String(currentKeyword || '').trim().toLowerCase();
    if (!Array.isArray(suggestions)) return [];
    const unique = [];
    for (let i = 0; i < suggestions.length; i++) {
      const text = String(suggestions[i] || '').trim();
      if (!text) continue;
      const lower = text.toLowerCase();
      if (lower === normalizedCurrent) continue;
      if (!unique.some((item) => item.toLowerCase() === lower)) {
        unique.push(text);
      }
      if (unique.length >= 8) break;
    }
    return unique;
  }
});
