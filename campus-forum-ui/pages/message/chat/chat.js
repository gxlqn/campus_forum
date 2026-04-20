/**
 * 私信会话页
 */
const api = require('../../../utils/api');

Page({
    data: {
        conversationId: '',
        targetUserId: null,
        targetNickname: '私信会话',
        messages: [],
        content: '',
        canSend: false,
        loading: false,
        sending: false,
        scrollIntoView: '',
        sendRestricted: false,
        sendPolicyText: ''
    },

    onLoad(options) {
        const conversationId = decodeURIComponent(options.conversationId || '');
        const targetUserId = Number(options.targetUserId || 0) || null;
        const targetNickname = decodeURIComponent(options.targetNickname || '私信会话');
        this.setData({
            conversationId,
            targetUserId,
            targetNickname
        });
        wx.setNavigationBarTitle({
            title: targetNickname || '私信会话'
        });
        this.loadMessages();
    },

    onPullDownRefresh() {
        this.loadMessages().finally(() => {
            wx.stopPullDownRefresh();
        });
    },

    async loadMessages() {
        const conversationId = this.data.conversationId;
        if (!conversationId) {
            this.setData({
                sendRestricted: false,
                sendPolicyText: '首次发起私信后，需等待对方回复才能继续发送'
            });
            return;
        }
        this.setData({ loading: true });
        try {
            const res = await api.getMessages(conversationId, { current: 1, size: 50 });
            const data = res.data || {};
            const page = data.page || {};
            const records = page.records || [];
            const conversation = data.conversation || {};
            const sendPolicy = data.sendPolicy || {};

            const self = wx.getStorageSync('userInfo') || {};
            const selfId = self.id;
            const messages = records.map((item) => ({
                id: item.id,
                senderId: item.senderId,
                receiverId: item.receiverId,
                content: item.content,
                contentType: item.contentType,
                createTime: this.formatTime(item.createTime),
                isSelf: selfId && item.senderId === selfId
            }));

            const targetUserId = this.data.targetUserId || conversation.targetUserId;
            const targetNickname = this.data.targetNickname === '私信会话'
                ? (conversation.targetNickname || this.data.targetNickname)
                : this.data.targetNickname;

            this.setData({
                messages,
                targetUserId,
                targetNickname,
                sendRestricted: !!sendPolicy.restricted,
                sendPolicyText: sendPolicy.reason || ''
            });

            wx.setNavigationBarTitle({
                title: targetNickname || '私信会话'
            });

            this.scrollToBottom();
        } catch (err) {
            console.error('加载会话失败', err);
            wx.showToast({
                title: '加载会话失败',
                icon: 'none'
            });
        } finally {
            this.setData({ loading: false });
        }
    },

    onInput(e) {
        const content = e.detail.value || '';
        this.setData({
            content,
            canSend: !!content.trim() && !this.data.sending
        });
    },

    async send() {
        const content = (this.data.content || '').trim();
        if (!content) {
            return;
        }
        if (!this.data.targetUserId) {
            wx.showToast({
                title: '接收方不存在',
                icon: 'none'
            });
            return;
        }
        if (this.data.sending) {
            return;
        }
        this.setData({ sending: true, canSend: false });
        try {
            const res = await api.sendMessage(this.data.targetUserId, content, 1);
            const data = res.data || {};
            const sendPolicy = data.sendPolicy || {};
            if (data.conversationId && !this.data.conversationId) {
                this.setData({
                    conversationId: data.conversationId
                });
            }
            this.setData({
                content: '',
                canSend: false,
                sendRestricted: !!sendPolicy.restricted,
                sendPolicyText: sendPolicy.reason || ''
            });
            await this.loadMessages();
        } catch (err) {
            console.error('发送消息失败', err);
        } finally {
            this.setData({
                sending: false,
                canSend: !!(this.data.content || '').trim()
            });
        }
    },

    scrollToBottom() {
        const messages = this.data.messages || [];
        if (!messages.length) {
            return;
        }
        const last = messages[messages.length - 1];
        this.setData({
            scrollIntoView: `msg-${last.id}`
        });
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
    }
});
