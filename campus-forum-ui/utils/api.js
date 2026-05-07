/**
 * API Module
 */
const http = require('./request');

const USE_MOCK = false;

const mockData = {
    sections: [
        { id: 1, sectionName: '跳蚤市场' },
        { id: 2, sectionName: '失物招领' },
        { id: 3, sectionName: '课程资料' },
        { id: 4, sectionName: '互助广场' },
        { id: 5, sectionName: '校园活动' },
        { id: 6, sectionName: '灌水区' }
    ],
    posts: [
        {
            id: 101,
            title: '求购二手课本',
            content: '急需一本高数教材，最好成色较新。',
            sectionName: '跳蚤市场',
            author: { nickname: '校内小红' },
            createTime: '2026-04-08 14:20'
        },
        {
            id: 102,
            title: '图书馆丢失钱包求助',
            content: '今天在图书馆自习时遗失钱包，内有学生证。',
            sectionName: '失物招领',
            author: { nickname: '匿名用户' },
            createTime: '2026-04-09 09:12'
        },
        {
            id: 103,
            title: '转让二手手机',
            content: 'iPhone 12，原装充电器，8成新。',
            sectionName: '跳蚤市场',
            author: { nickname: '小杨' },
            createTime: '2026-04-07 20:05'
        }
    ],
    products: [
        {
            id: 201,
            title: '二手小米手环',
            description: '功能正常，带原装充电线。',
            price: '59',
            category: '数码',
            imageList: ['/static/images/default-image.png'],
            seller: { nickname: '学长' }
        },
        {
            id: 202,
            title: '课程资料合集',
            description: '涵盖三门专业课程，电子版资料整理。',
            price: '20',
            category: '课程资料',
            imageList: ['/static/images/default-image.png'],
            seller: { nickname: '资料君' }
        }
    ],
    activities: [
        {
            id: 301,
            title: '校园环保志愿活动',
            description: '周末组织清洁校园，参与可获得志愿时长。',
            time: '2026-04-15 14:00',
            location: '图书馆广场',
            joinCount: 45
        },
        {
            id: 302,
            title: '大学生创业沙龙',
            description: '邀请校友分享创业经验，名额有限。',
            time: '2026-04-18 19:00',
            location: '综合楼报告厅',
            joinCount: 78
        }
    ],
    helps: [
        {
            id: 401,
            title: '帮忙代取快递',
            description: '宿舍附近取件，酬劳10元。',
            time: '今天',
            location: '宿舍楼下',
            contact: '微信: help123'
        },
        {
            id: 402,
            title: '求助打印资料',
            description: '急需打印50页材料，可酬劳。',
            time: '明天上午',
            location: '文印中心',
            contact: '电话: 13800000000'
        }
    ]
};

function delay(ms = 300) {
    return new Promise((resolve) => setTimeout(resolve, ms));
}

function fuzzyFilter(items, keyword, fields = []) {
    if (!keyword) return items;
    const text = String(keyword).toLowerCase();
    return items.filter((item) => fields.some((field) => String(item[field] || '').toLowerCase().includes(text)));
}

// Auth
function wxLogin(data) {
    return http.post('/auth/wx/login', data);
}

function bindStudentId(studentId, userType) {
    return http.post('/auth/bind', null, {
        params: { studentId, userType }
    });
}

// Dev Mode - 账号切换
function getDevUsers() {
    return http.get('/auth/dev/users');
}

function devLogin(userId) {
    return http.post('/auth/dev/login', { userId });
}

// Forum
function getSections() {
    return http.get('/forum/sections');
}

function getPostList(params) {
    return http.get('/forum/posts', params);
}

function getPostDetail(id, params) {
    return http.get(`/forum/posts/${id}`, params);
}

function publishPost(data) {
    return http.post('/forum/posts', data);
}

function likePost(id) {
    return http.post(`/forum/posts/${id}/like`);
}

function unlikePost(id) {
    return http.del(`/forum/posts/${id}/like`);
}

function favoritePost(id) {
    return http.post(`/forum/posts/${id}/favorite`);
}

function unfavoritePost(id) {
    return http.del(`/forum/posts/${id}/favorite`);
}

function updatePost(id, data) {
    return http.put(`/forum/posts/${id}`, data);
}

function deletePost(id) {
    return http.del(`/forum/posts/${id}`);
}

// Comment
function getComments(postId, params) {
    return http.get(`/forum/posts/${postId}/comments`, params);
}

function addComment(postId, content, parentId = null) {
    return http.post(`/forum/posts/${postId}/comments`, { content, parentId });
}

// Product
function getProductList(params) {
    return http.get('/products', params);
}

//简洁 隐式返回
function getProductDetail(id) {
    return http.get(`/products/${id}`);
}

//块语句 显式返回 加了mock判断
function publishProduct(data) {
    if (USE_MOCK) {
        return delay(500).then(() => ({
            data: {
                id: Date.now(),
                title: data.title,
                price: data.price,
                category: data.category,
                description: data.description,
                images: data.images,
                contact: data.contact,
                status: 'published'
            }
        }));
    }
    return http.post('/products', data);
}


function offProduct(id) {
    return http.post(`/products/${id}/off`);
}

function soldProduct(id) {
    return http.post(`/products/${id}/sold`);
}

function updateWantedProductStatus(id, status) {
    return http.post(`/products/${id}/wanted/status`, null, {
        params: { status }
    });
}

function createProductOrder(id) {
    return http.post(`/products/${id}/order`);
}

function acceptProductOrder(orderId) {
    return http.post(`/products/orders/${orderId}/accept`);
}

function rejectProductOrder(orderId, reason) {
    return http.post(`/products/orders/${orderId}/reject`, { reason });
}

function cancelProductOrder(orderId, cancelReason) {
    return http.post(`/products/orders/${orderId}/cancel`, { cancelReason });
}

function confirmProductOrder(orderId) {
    return http.post(`/products/orders/${orderId}/confirm`);
}

function getMyProductOrders(params) {
    return http.get('/products/orders/my', params);
}

function scheduleProductMeetup(orderId, meetupPlace, meetupTime) {
    return http.post(`/products/orders/${orderId}/meetup`, { meetupPlace, meetupTime });
}

function rescheduleProductMeetup(orderId, meetupPlace, meetupTime) {
    return http.post(`/products/orders/${orderId}/meetup/reschedule`, { meetupPlace, meetupTime });
}

function verifyProductMeetup(orderId, meetupCode) {
    return http.post(`/products/orders/${orderId}/meetup/verify`, { meetupCode });
}

// Lost and Found
function getLostFoundList(params) {
    return http.get('/lostfound', params);
}

function getLostFoundDetail(id) {
    return http.get(`/lostfound/${id}`);
}

function publishLostFound(data) {
    return http.post('/lostfound', data);
}

function submitLostFoundClaim(id, data) {
    return http.post(`/lostfound/${id}/claim`, data);
}

function uploadFile(filePath) {
    return http.uploadFile(filePath);
}
// Activity
function getActivityList(params) {
    if (USE_MOCK) {
        const records = fuzzyFilter(mockData.activities, params && params.keyword, ['title', 'description']);
        return delay(300).then(() => ({ data: { records } }));
    }
    return http.get('/activities', params);
}

function getActivityDetail(id) {
    if (USE_MOCK) {
        const record = mockData.activities.find((item) => String(item.id) === String(id));
        return delay(200).then(() => ({ data: record || null }));
    }
    return http.get(`/activities/${id}`);
}

function signupActivity(id) {
    return http.post(`/activities/${id}/signup`);
}

function publishActivity(data) {
    return http.post('/activities', data);
}

// Help
function getHelpList(params) {
    if (USE_MOCK) {
        const records = fuzzyFilter(mockData.helps, params && params.keyword, ['title', 'description']);
        return delay(300).then(() => ({ data: { records } }));
    }
    return http.get('/help', params);
}

function getHelpDetail(id) {
    if (USE_MOCK) {
        const record = mockData.helps.find((item) => String(item.id) === String(id));
        return delay(200).then(() => ({ data: record || null }));
    }
    return http.get(`/help/${id}`);
}

function publishHelp(data) {
    return http.post('/help', data);
}

function acceptHelp(id) {
    if (USE_MOCK) {
        return delay(200).then(() => ({ data: { id, status: 'accepted' } }));
    }
    return http.post(`/help/${id}/accept`);
}

function cancelHelp(id) {
    return http.post(`/help/${id}/cancel`);
}

function completeHelp(id) {
    return http.post(`/help/${id}/complete`);
}

function publisherConfirmHelp(id, isComplaint = 0) {
    return http.post(`/help/${id}/publisher-confirm`, null, {
        params: { isComplaint }
    });
}

function helperAppealHelp(id) {
    return http.post(`/help/${id}/helper-appeal`);
}

function searchAll(params) {
    if (USE_MOCK) {
        const keyword = (params && params.keyword) || '';
        const sections = fuzzyFilter(mockData.sections, keyword, ['sectionName']);
        const posts = fuzzyFilter(mockData.posts, keyword, ['title', 'content']);
        const products = fuzzyFilter(mockData.products, keyword, ['title', 'description']);
        const activities = fuzzyFilter(mockData.activities, keyword, ['title', 'description']);
        const helps = fuzzyFilter(mockData.helps, keyword, ['title', 'description']);
        return delay(400).then(() => ({ data: { keyword, sections, posts, products, activities, helps } }));
    }
    return http.get('/search', params);
}

function getSearchRecommend(params) {
    if (USE_MOCK) {
        const words = ['二手教材', '校园活动', '失物招领', '拼车', '跑腿'];
        return delay(200).then(() => ({ data: words.slice(0, (params && params.size) || 10) }));
    }
    return http.get('/search/recommend', params);
}

function searchSuggest(params) {
    const keyword = (params && params.keyword) || '';
    if (USE_MOCK) {
        const mockSuggestions = [
            { text: keyword + '推荐', type: 'post' },
            { text: keyword + '最新', type: 'post' },
            { text: keyword + '热门', type: 'post' },
            { text: '二手' + keyword, type: 'product' },
            { text: keyword + '活动', type: 'activity' }
        ].slice(0, (params && params.size) || 6);
        return delay(150).then(() => ({ data: mockSuggestions }));
    }
    // 后端接口期望参数名为 prefix，做映射
    const { size } = params || {};
    const queryParams = { prefix: keyword };
    if (size) queryParams.size = size;
    return http.get('/search/suggest', queryParams);
}

// User
function getUserInfo() {
    return http.get('/user/info');
}

function getPublicUserProfile(userId) {
    return http.get(`/user/public/${userId}`);
}

function updateUserInfo(data) {
    return http.put('/user/info', data);
}

function getMyPosts(params) {
    return http.get('/forum/my/posts', params);
}

function getMyFavorites(params) {
    return http.get('/forum/my/favorites', params);
}

function getMyProducts(params) {
    return http.get('/products/my', params);
}

function getMyPublish(params) {
    return http.get('/user/my/publish', params);
}

function getMyFollows(params) {
    return http.get('/user/follows', params);
}

function followUser(followUserId) {
    return http.post(`/user/follows/${followUserId}`);
}

function unfollowUser(followUserId) {
    return http.del(`/user/follows/${followUserId}`);
}

function getMyEvaluations(params) {
    return http.get('/user/evaluations', params);
}

function getUserStats() {
    return http.get('/user/stats');
}

function rechargeWallet(amount) {
    return http.post('/user/wallet/recharge', { amount });
}

// Message
function getNotifications(params) {
    return http.get('/message/notifications', params);
}

function getNotificationDetail(id) {
    return http.get(`/message/notifications/${id}`);
}

function getConversations(params) {
    return http.get('/message/conversations', params);
}

function getMessages(conversationId, params) {
    return http.get(`/message/conversations/${conversationId}`, params);
}

function sendMessage(receiverId, content, contentType = 1, clientMessageId = null) {
    return http.post('/message/send', { receiverId, content, contentType, clientMessageId });
}

function markNotificationRead(id) {
    return http.patch(`/message/notifications/${id}/read`);
}

function markAllNotificationsRead() {
    return http.patch('/message/notifications/read-all');
}

function clearSystemNotifications(onlyRead = false) {
    const query = onlyRead ? '?onlyRead=true' : '';
    return http.del(`/message/notifications/system${query}`);
}

function markConversationRead(conversationId) {
    return http.patch(`/message/conversations/${conversationId}/read`);
}

// Banner / Carousel
function getBanners() {
    return http.get('/banners');
}

// Info
function getNewsList(params) {
    return http.get('/info/news', params);
}

function getNewsDetail(id) {
    return http.get(`/info/news/${id}`);
}

function getNewsCategories() {
    return http.get('/info/news/categories');
}

function getServiceNavList(params) {
    return http.get('/info/nav', params);
}

function getServiceNavCategories() {
  return http.get('/info/nav/categories');
}

// Report
function submitReport(data) {
  return http.post('/report', data);
}

module.exports = {
    // Auth
    wxLogin,
    bindStudentId,
    // Dev Mode - 账号切换
    getDevUsers,
    devLogin,
    // Forum
    getSections,
    getPostList,
    getPostDetail,
    publishPost,
    likePost,
    unlikePost,
    favoritePost,
    unfavoritePost,
    updatePost,
    deletePost,
    // Comment
    getComments,
    addComment,
    // Product
    getProductList,
    getProductDetail,
    publishProduct,
    offProduct,
    soldProduct,
    updateWantedProductStatus,
    createProductOrder,
    acceptProductOrder,
    rejectProductOrder,
    cancelProductOrder,
    confirmProductOrder,
    getMyProductOrders,
    scheduleProductMeetup,
    rescheduleProductMeetup,
    verifyProductMeetup,
    // Lost and Found
    getLostFoundList,
    getLostFoundDetail,
    publishLostFound,
    submitLostFoundClaim,
    uploadFile,
    // Activity
    getActivityList,
    getActivityDetail,
    signupActivity,
    publishActivity,
    // Help
    getHelpList,
    getHelpDetail,
    publishHelp,
    acceptHelp,
    cancelHelp,
    completeHelp,
    publisherConfirmHelp,
    helperAppealHelp,
    searchAll,
    getSearchRecommend,
    searchSuggest,
    // User
    getUserInfo,
    getPublicUserProfile,
    updateUserInfo,
    getMyPosts,
    getMyFavorites,
    getMyProducts,
    getMyPublish,
    getMyFollows,
    followUser,
    unfollowUser,
    getMyEvaluations,
    getUserStats,
    rechargeWallet,
    // Message
    getNotifications,
    getNotificationDetail,
    getConversations,
    getMessages,
    sendMessage,
    markNotificationRead,
    markAllNotificationsRead,
    clearSystemNotifications,
    markConversationRead,
    // Info
    getNewsList,
    getNewsDetail,
    getNewsCategories,
    getServiceNavList,
    getServiceNavCategories,
    // Report
    submitReport,
    // Banner
    getBanners
};
