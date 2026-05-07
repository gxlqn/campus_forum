/**
 * 聊天页面 - ui-ux-pro-max 升级版
 */
const api = require('../../../utils/api');
const imSocket = require('../../../utils/imSocket');
const imageHelper = require('../../../utils/imageHelper');

Page({
    data: {
        conversationId: '',
        targetUserId: '',
        targetNickname: '',
        targetAvatar: '',
        currentUserId: '',
        isOnline: false,
        isTyping: false,
        messages: [],
        content: '',
        canSend: false,
        sending: false,
        loading: false,
        scrollIntoView: '',
        sendPolicyText: '',
        sendRestricted: false
    },

    onLoad(options) {
        const { conversationId, targetUserId, targetNickname } = options;
        const decodedNickname = targetNickname ? decodeURIComponent(targetNickname) : '未知用户';
        const userInfo = wx.getStorageSync('userInfo') || {};

        console.log('[IM][Chat] onLoad', {
            conversationId,
            targetUserId,
            targetNickname: decodedNickname,
            currentUserId: userInfo.id || userInfo.userId || ''
        });

        this.setData({
            conversationId: conversationId || '',
            targetUserId: targetUserId || '',
            targetNickname: decodedNickname,
            currentUserId: userInfo.id || userInfo.userId || '',
            isOnline: false
        });

        wx.setNavigationBarTitle({ title: decodedNickname });

        this.loadMessages();
        this.connectIm();
    },

    onShow() {
        console.log('[IM][Chat] onShow', {
            conversationId: this.data.conversationId,
            targetUserId: this.data.targetUserId
        });
        this.connectIm();
    },

    onUnload() {
        console.log('[IM][Chat] onUnload');
        const { conversationId } = this.data;
        if (conversationId) {
            api.markConversationRead(conversationId).catch(() => {});
        }
        this.disconnectIm();
    },

    connectIm() {
        console.log('[IM][Chat] connectIm', {
            isConnected: imSocket.isConnected(),
            conversationId: this.data.conversationId
        });

        if (!this._imBound) {
            this._onImConnected = () => {
                console.log('[IM][Chat] socket connected');
                this.requestConversationSync();
            };
            this._onImDisconnected = () => {
                console.log('[IM][Chat] socket disconnected');
            };
            this._onImMessage = (msg) => {
                console.log('[IM][Chat] im-message', msg);
                this.handleIncomingMessage(msg);
            };
            this._onImDelivery = (data) => {
                console.log('[IM][Chat] im-delivery', data);
                this.updateOutgoingMessageStatus(data.clientMessageId, 'delivered');
            };
            this._onImSendAck = (data) => {
                console.log('[IM][Chat] im-send-ack', data);
                this.updateOutgoingMessageStatus(data.clientMessageId, 'sent', data.messageId);
            };
            this._onImSync = (data) => {
                console.log('[IM][Chat] im-sync', data);
                this.handleSyncResult(data);
            };
            this._onImPresence = (data) => {
                console.log('[IM][Chat] im-presence', data);
                if (data && String(data.userId) === String(this.data.targetUserId)) {
                    this.setData({ isOnline: !!data.online });
                }
            };
            this._onImTyping = (data) => {
                console.log('[IM][Chat] im-typing', data);
                if (String(data.fromUserId) === String(this.data.targetUserId)) {
                    this.setData({ isTyping: true });
                    this.resetTypingTimer();
                }
            };

            imSocket.on('connected', this._onImConnected);
            imSocket.on('disconnected', this._onImDisconnected);
            imSocket.on('im-message', this._onImMessage);
            imSocket.on('im-delivery', this._onImDelivery);
            imSocket.on('im-send-ack', this._onImSendAck);
            imSocket.on('im-sync', this._onImSync);
            imSocket.on('im-presence', this._onImPresence);
            imSocket.on('im-typing', this._onImTyping);

            this._imBound = true;
        }

        imSocket.ensureConnected();

        if (imSocket.isConnected()) {
            this.requestConversationSync();
        }
    },

    disconnectIm() {
        if (this._imBound) {
            imSocket.off('connected', this._onImConnected);
            imSocket.off('disconnected', this._onImDisconnected);
            imSocket.off('im-message', this._onImMessage);
            imSocket.off('im-delivery', this._onImDelivery);
            imSocket.off('im-send-ack', this._onImSendAck);
            imSocket.off('im-sync', this._onImSync);
            imSocket.off('im-presence', this._onImPresence);
            imSocket.off('im-typing', this._onImTyping);
            this._imBound = false;
        }
        if (this._typingTimer) {
            clearTimeout(this._typingTimer);
        }
    },

    resetTypingTimer() {
        if (this._typingTimer) {
            clearTimeout(this._typingTimer);
        }
        this._typingTimer = setTimeout(() => {
            this.setData({ isTyping: false });
        }, 3000);
    },

    async loadMessages() {
        const { conversationId } = this.data;
        if (!conversationId) return;

        console.log('[IM][Chat] loadMessages:start', { conversationId });
        this.setData({ loading: true });
        try {
            const res = await api.getMessages(conversationId, { current: 1, size: 50 });
            const payload = res && res.data ? res.data : res;
            const msgs = (payload && payload.page && payload.page.records) || [];
            const conversation = (payload && payload.conversation) || {};

            console.log('[IM][Chat] loadMessages:done', {
                count: msgs.length,
                conversation
            });

            if (typeof conversation.isOnline !== 'undefined') {
                this.setData({ isOnline: !!conversation.isOnline });
            }

            if (conversation.targetAvatar) {
                this.setData({ targetAvatar: imageHelper.getFullImageUrl(conversation.targetAvatar) });
            }

            const messages = msgs.map((msg) => this.normalizeMessage(msg));
            const merged = this.mergeMessages(this.data.messages || [], messages);
            const unreadCount = conversation.unreadCount ? Number(conversation.unreadCount) : 0;

            this.setData({
                messages: merged,
                loading: false,
                scrollIntoView: ''
            }, () => {
                if (merged.length > 0 && unreadCount > 0) {
                    this.setData({ scrollIntoView: 'msg-' + merged[merged.length - 1].id });
                }
            });
        } catch (err) {
            console.error('加载消息失败', err);
            this.setData({ loading: false });
        }
    },

    normalizeMessage(msg) {
        const id = msg.id || msg.clientMessageId || Date.now();
        return {
            id,
            clientMessageId: msg.clientMessageId || '',
            content: msg.content || '',
            isSelf: String(msg.senderId) === String(this.data.currentUserId),
            createTime: this.formatTime(msg.createTime),
            sortTime: msg.createTime || new Date().toISOString(),
            status: msg.status || (msg.isRead ? 'read' : 'sent'),
            avatar: this.getAvatarForMessage(msg)
        };
    },

    mergeMessages(existing, incoming) {
        const merged = [];
        const seen = new Set();

        const pushItem = (item) => {
            if (!item) return;
            const keys = [];
            if (item.id !== undefined && item.id !== null) {
                keys.push('id:' + String(item.id));
            }
            if (item.clientMessageId) {
                keys.push('client:' + String(item.clientMessageId));
            }
            for (const key of keys) {
                if (seen.has(key)) {
                    return;
                }
            }
            keys.forEach((key) => seen.add(key));
            merged.push(item);
        };

        (existing || []).forEach(pushItem);
        (incoming || []).forEach(pushItem);

        merged.sort((a, b) => {
            const aTime = Date.parse(a.sortTime || a.createTime || '') || 0;
            const bTime = Date.parse(b.sortTime || b.createTime || '') || 0;
            if (aTime !== bTime) {
                return aTime - bTime;
            }
            return String(a.id).localeCompare(String(b.id));
        });

        return merged;
    },

    getAvatarForMessage(msg) {
        if (String(msg.senderId) !== String(this.data.currentUserId)) {
            return imageHelper.getFullImageUrl(this.data.targetAvatar);
        }
        // 自己的头像从缓存获取
        const userInfo = wx.getStorageSync('userInfo');
        return imageHelper.getFullImageUrl(userInfo && userInfo.avatar);
    },

    onInput(e) {
        const content = e.detail.value || '';
        this.setData({
            content,
            canSend: content.trim().length > 0
        });

        // 发送正在输入通知
        if (this.data.conversationId && imSocket.isConnected()) {
            if (typeof imSocket.sendTyping === 'function') {
                imSocket.sendTyping({ conversationId: this.data.conversationId });
            }
        }
    },

    async send() {
        const { content, canSend, sending, targetUserId, conversationId } = this.data;
        if (!canSend || sending) return;

        const trimmed = content.trim();
        if (!trimmed) return;

        this.setData({ sending: true });

        const clientMessageId = 'local_' + Date.now();
        const newMsg = {
            id: clientMessageId,
            clientMessageId,
            content: trimmed,
            isSelf: true,
            createTime: this.formatTime(new Date().toISOString()),
            sortTime: new Date().toISOString(),
            status: 'sending',
            avatar: this.getAvatarForMessage({ senderId: this.data.currentUserId })
        };

        const messages = [...this.data.messages, newMsg];
        this.setData({
            messages,
            content: '',
            canSend: false,
            scrollIntoView: ''
        }, () => {
            this.setData({ scrollIntoView: 'msg-' + clientMessageId });
        });

        try {
            const res = await api.sendMessage(targetUserId, trimmed, 1, clientMessageId);
            const ackData = res && res.data ? res.data : res;
            const payload = ackData && ackData.data ? ackData.data : ackData;
            this.updateOutgoingMessageStatus(clientMessageId, 'sent', payload && payload.messageId);
            this.requestConversationSync();
        } catch (err) {
            console.error('发送消息失败', err);
            this.updateOutgoingMessageStatus(clientMessageId, 'failed');
            wx.showToast({ title: '发送失败', icon: 'none' });
        } finally {
            this.setData({ sending: false });
        }
    },

    updateOutgoingMessageStatus(clientMessageId, status, messageId) {
        const messages = (this.data.messages || []).map((msg) => {
            if (msg.id === clientMessageId || msg.clientMessageId === clientMessageId) {
                return { ...msg, status, id: messageId || msg.id };
            }
            return msg;
        });
        this.setData({ messages });
    },

    handleSyncResult(data) {
        if (!data || !Array.isArray(data.records)) {
            console.log('[IM][Chat] handleSyncResult:skip', data);
            return;
        }

        console.log('[IM][Chat] handleSyncResult:records', data.records.length);
        const incoming = data.records.map((msg) => this.normalizeMessage(msg));
        const merged = this.mergeMessages(this.data.messages || [], incoming);

        this.setData({
            messages: merged,
            scrollIntoView: ''
        }, () => {
            if (merged.length > 0) {
                this.setData({ scrollIntoView: 'msg-' + merged[merged.length - 1].id });
            }
        });
    },

    handleIncomingMessage(msg) {
        const { targetUserId } = this.data;
        if (String(msg.senderId) !== String(targetUserId)) {
            console.log('[IM][Chat] handleIncomingMessage:skip', {
                senderId: msg && msg.senderId,
                targetUserId
            });
            return;
        }

        const newMsg = {
            id: msg.id || Date.now(),
            clientMessageId: msg.clientMessageId || '',
            content: msg.content || '',
            senderId: msg.senderId,
            isSelf: false,
            createTime: this.formatTime(msg.createTime),
            sortTime: msg.createTime || new Date().toISOString(),
            status: 'read',
            avatar: this.getAvatarForMessage(msg)
        };

        console.log('[IM][Chat] handleIncomingMessage:append', newMsg);

        this.setData({
            messages: this.mergeMessages(this.data.messages || [], [newMsg]),
            scrollIntoView: ''
        }, () => {
            this.setData({ scrollIntoView: 'msg-' + newMsg.id });
        });

        if (msg.id && imSocket.isConnected() && typeof imSocket.ackMessage === 'function') {
            imSocket.ackMessage({ messageId: msg.id, receiptType: 'READ' });
        }
    },

    requestConversationSync() {
        const { conversationId } = this.data;
        if (!conversationId || !imSocket.isConnected() || typeof imSocket.syncConversation !== 'function') {
            console.log('[IM][Chat] requestConversationSync:skip', {
                conversationId,
                connected: imSocket.isConnected(),
                hasFn: typeof imSocket.syncConversation === 'function'
            });
            return;
        }
        console.log('[IM][Chat] requestConversationSync:send', { conversationId });
        imSocket.syncConversation({
            conversationId,
            cursorMessageId: 0,
            size: 100
        });
    },

    formatTime(time) {
        if (!time) return '';
        const str = String(time);
        if (str.length >= 16) {
            return str.substring(5, 16).replace('T', ' ');
        }
        return str;
    }
});
