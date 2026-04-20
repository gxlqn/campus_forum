/**
 * 消息列表页
 */
const api = require('../../../utils/api');
const imageHelper = require('../../../utils/imageHelper');

Page({
    data: {
        isLogin: false,
        currentTab: 'system',
        systemCount: 0,
        commentCount: 0,
        likeCount: 0,
        chatCount: 0,
        notifications: [],
        conversations: [],
        messages: [],
        loading: false,
        tabs: [
            { key: 'system', name: '系统', icon: '🔔' },
            { key: 'comment', name: '评论', icon: '💬' },
            { key: 'like', name: '点赞', icon: '❤️' },
            { key: 'chat', name: '私信', icon: '✉️' }
        ]
    },

    onLoad() {
        this.checkLogin();
    },

    onShow() {
        this.checkLogin();
    },

    onPullDownRefresh() {
        this.loadMessages().finally(() => {
            wx.stopPullDownRefresh();
        });
    },

    checkLogin() {
        const token = wx.getStorageSync('token');
        if (!token) {
            this.setData({
                isLogin: false,
                notifications: [],
                conversations: [],
                messages: [],
                loading: false,
                systemCount: 0,
                commentCount: 0,
                likeCount: 0,
                chatCount: 0
            });
            return;
        }
        this.setData({ isLogin: true });
        this.loadMessages();
    },

    async loadMessages() {
        if (!this.data.isLogin) {
            return;
        }
        this.setData({ loading: true });
        try {
            const [notificationRes, conversationRes] = await Promise.all([
                api.getNotifications({ current: 1, size: 50 }),
                api.getConversations({ current: 1, size: 50 })
            ]);

            const notifications = (notificationRes.data && notificationRes.data.page && notificationRes.data.page.records) || [];
            const conversations = (conversationRes.data && conversationRes.data.page && conversationRes.data.page.records) || [];

            const systemCount = notifications.filter((item) => (item.type === 1 || item.type === 8 || item.type === 9) && item.isRead !== 1).length;
            const commentCount = notifications.filter((item) => (item.type === 2 || item.type === 3) && item.isRead !== 1).length;
            const likeCount = notifications.filter((item) => item.type === 4 && item.isRead !== 1).length;
            const chatCount = conversationRes.data && conversationRes.data.unreadCount ? conversationRes.data.unreadCount : 0;

            this.setData({
                notifications,
                conversations,
                systemCount,
                commentCount,
                likeCount,
                chatCount,
                'tabs[0].count': systemCount,
                'tabs[1].count': commentCount,
                'tabs[2].count': likeCount,
                'tabs[3].count': chatCount
            });
            this.applyTabMessages(this.data.currentTab);
        } catch (err) {
            console.error('加载消息失败', err);
            wx.showToast({
                title: '加载消息失败',
                icon: 'none'
            });
        } finally {
            this.setData({ loading: false });
        }
    },

    onTabTap(e) {
        const tab = e.currentTarget.dataset.tab;
        if (!tab || tab === this.data.currentTab) {
            return;
        }
        this.applyTabMessages(tab);
    },

    onClearSystemNotifications() {
        if (this.data.currentTab !== 'system') {
            return;
        }
        wx.showActionSheet({
            itemList: ['仅清理已读系统通知', '清理全部系统通知'],
            success: async (res) => {
                const onlyRead = res.tapIndex === 0;
                wx.showModal({
                    title: onlyRead ? '仅清理已读' : '清理全部系统通知',
                    content: onlyRead ? '将保留未读系统通知，是否继续？' : '将清理全部系统通知（含未读），是否继续？',
                    confirmColor: '#FF6B9D',
                    success: async (confirmRes) => {
                        if (!confirmRes.confirm) {
                            return;
                        }
                        try {
                            await api.clearSystemNotifications(onlyRead);
                            wx.showToast({ title: '已清理', icon: 'success' });
                            this.loadMessages();
                        } catch (err) {
                            console.error('清理系统通知失败', err);
                            wx.showToast({ title: '清理失败', icon: 'none' });
                        }
                    }
                });
            },
            fail: () => {}
        });
    },

    applyTabMessages(tab) {
        let messages = [];
        if (tab === 'chat') {
            messages = this.data.conversations.map((item) => ({
                id: item.conversationId,
                targetUserId: item.targetUserId,
                targetNickname: item.targetNickname || '未知用户',
                user: {
                    nickname: item.targetNickname || '未知用户',
                    avatar: imageHelper.getFullImageUrl(item.targetAvatar)
                },
                lastTime: this.formatTime(item.lastMessageTime),
                lastMessage: item.lastMessageContent || '',
                unreadCount: item.unreadCount || 0
            }));
        } else {
            const types = tab === 'system' ? [1, 8, 9] : (tab === 'comment' ? [2, 3] : [4]);
            messages = this.data.notifications
                .filter((item) => types.includes(item.type))
                .map((item) => ({
                    id: item.id,
                    title: item.title || this.getTypeTitle(item.type),
                    content: item.content || '',
                    createTime: this.formatTime(item.createTime),
                    isRead: item.isRead === 1,
                    postId: item.targetId,
                    postTitle: item.title || '',
                    user: {
                        nickname: item.senderName || '系统',
                        avatar: imageHelper.getFullImageUrl(item.senderAvatar)
                    }
                }));
        }
        this.setData({
            currentTab: tab,
            messages
        });
    },

    async goToDetail(e) {
        const id = e.currentTarget.dataset.id;
        if (!id) {
            return;
        }
        wx.navigateTo({
            url: `/pages/message/detail/detail?id=${id}`
        });
    },

    async goToPost(e) {
        const id = e.currentTarget.dataset.id;
        const noticeId = e.currentTarget.dataset.noticeId;

        if (noticeId) {
            try {
                await api.markNotificationRead(noticeId);
            } catch (err) {
                console.error('标记通知已读失败', err);
            }
        }

        if (!id) {
            wx.showToast({
                title: '暂无关联帖子',
                icon: 'none'
            });
            return;
        }
        wx.navigateTo({
            url: `/pages/forum/detail/detail?id=${id}`
        });
    },

    async goToChat(e) {
        const conversationId = e.currentTarget.dataset.id;
        const targetUserId = e.currentTarget.dataset.targetUserId;
        const targetNickname = e.currentTarget.dataset.targetNickname || '';
        if (!conversationId || !targetUserId) {
            return;
        }
        try {
            await api.markConversationRead(conversationId);
            this.loadMessages();
        } catch (err) {
            console.error('标记会话已读失败', err);
        }
        wx.navigateTo({
            url: `/pages/message/chat/chat?conversationId=${encodeURIComponent(conversationId)}&targetUserId=${targetUserId}&targetNickname=${encodeURIComponent(targetNickname)}`
        });
    },

    getTypeTitle(type) {
        if (type === 2 || type === 3) {
            return '评论通知';
        }
        if (type === 4) {
            return '点赞通知';
        }
        if (type === 5) {
            return '关注通知';
        }
        if (type === 6) {
            return '交易通知';
        }
        if (type === 7) {
            return '活动通知';
        }
        if (type === 8) {
            return '互助通知';
        }
        return '系统通知';
    },

    formatTime(time) {
        if (!time) {
            return '';
        }
        const str = String(time);
        if (str.length >= 16) {
            return str.substring(5, 16).replace('T', ' ');
        }
        return str;
    },

    goLogin() {
        wx.navigateTo({
            url: '/pages/auth/login/login'
        });
    }
});
