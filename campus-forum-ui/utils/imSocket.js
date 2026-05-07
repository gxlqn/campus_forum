const STOMP_DESTINATIONS = {
  message: '/user/queue/im-message',
  sendAck: '/user/queue/im-send-ack',
  ack: '/user/queue/im-ack',
  delivery: '/user/queue/im-delivery',
  sync: '/user/queue/im-sync',
  presence: '/user/queue/im-presence',
  online: '/user/queue/im-online',
  typing: '/user/queue/im-typing',
  error: '/user/queue/im-error'
};

const STOMP_EVENT_MAP = {
  [STOMP_DESTINATIONS.message]: 'im-message',
  [STOMP_DESTINATIONS.sendAck]: 'im-send-ack',
  [STOMP_DESTINATIONS.ack]: 'im-ack',
  [STOMP_DESTINATIONS.delivery]: 'im-delivery',
  [STOMP_DESTINATIONS.sync]: 'im-sync',
  [STOMP_DESTINATIONS.presence]: 'im-presence',
  [STOMP_DESTINATIONS.online]: 'im-online',
  [STOMP_DESTINATIONS.typing]: 'im-typing',
  [STOMP_DESTINATIONS.error]: 'im-error'
};

const listeners = {};
let socketTask = null;
let connecting = false;
let connected = false;
let shouldReconnect = false;
let reconnectAttempts = 0;
let reconnectTimer = null;
let heartbeatTimer = null;
let frameBuffer = '';

function log(event, detail) {
  console.log(`[IM][Socket] ${event}`, detail || '');
}

function getAppBaseUrl() {
  try {
    const app = getApp();
    return app?.globalData?.baseUrl || 'http://localhost:8081/api';
  } catch (e) {
    return 'http://localhost:8081/api';
  }
}

function getToken() {
  return wx.getStorageSync('token') || '';
}

function buildWsUrl(token) {
  const baseUrl = getAppBaseUrl();
  const wsBase = baseUrl.replace(/^http:/i, 'ws:').replace(/^https:/i, 'wss:');
  const cleanBase = wsBase.endsWith('/') ? wsBase.slice(0, -1) : wsBase;
  const encodedToken = encodeURIComponent(token);
  return `${cleanBase}/ws-im?token=${encodedToken}`;
}

function emit(event, payload) {
  const handlers = listeners[event];
  if (!handlers || handlers.size === 0) {
    return;
  }
  handlers.forEach((handler) => {
    try {
      handler(payload);
    } catch (err) {
      console.error('IM event handler error:', event, err);
    }
  });
}

function on(event, handler) {
  if (!listeners[event]) {
    listeners[event] = new Set();
  }
  listeners[event].add(handler);
}

function off(event, handler) {
  if (!listeners[event]) {
    return;
  }
  listeners[event].delete(handler);
  if (listeners[event].size === 0) {
    delete listeners[event];
  }
}

function startHeartbeat() {
  stopHeartbeat();
  heartbeatTimer = setInterval(() => {
    if (!socketTask || !connected) {
      return;
    }
    socketTask.send({ data: '\n' });
  }, 10000);
}

function stopHeartbeat() {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer);
    heartbeatTimer = null;
  }
}

function clearReconnectTimer() {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer);
    reconnectTimer = null;
  }
}

function scheduleReconnect() {
  if (!shouldReconnect) {
    return;
  }
  clearReconnectTimer();
  const delay = Math.min(20000, 1000 * Math.pow(2, reconnectAttempts));
  reconnectAttempts += 1;
  reconnectTimer = setTimeout(() => {
    connect();
  }, delay);
}

function sendFrame(command, headers = {}, body = '') {
  if (!socketTask) {
    return false;
  }
  const headerLines = Object.keys(headers)
    .filter((key) => headers[key] !== undefined && headers[key] !== null && headers[key] !== '')
    .map((key) => `${key}:${String(headers[key])}`)
    .join('\n');

  const frame = `${command}\n${headerLines}\n\n${body}\0`;
  socketTask.send({ data: frame });
  return true;
}

function subscribeDefaultQueues() {
  Object.keys(STOMP_EVENT_MAP).forEach((destination, idx) => {
    sendFrame('SUBSCRIBE', {
      id: `sub-${idx + 1}`,
      destination,
      ack: 'auto'
    });
  });
}

function parseFrame(rawFrame) {
  const normalized = rawFrame.replace(/\r/g, '').replace(/^\n+/, '');
  if (!normalized || normalized === '\n') {
    return null;
  }
  const headerSplitIndex = normalized.indexOf('\n\n');
  const headerPart = headerSplitIndex >= 0 ? normalized.slice(0, headerSplitIndex) : normalized;
  const bodyPart = headerSplitIndex >= 0 ? normalized.slice(headerSplitIndex + 2) : '';
  const lines = headerPart.split('\n');
  const command = (lines.shift() || '').trim();
  const headers = {};
  lines.forEach((line) => {
    const idx = line.indexOf(':');
    if (idx <= 0) {
      return;
    }
    const key = line.slice(0, idx).trim();
    const value = line.slice(idx + 1).trim();
    headers[key] = value;
  });
  return {
    command,
    headers,
    body: bodyPart
  };
}

function handleMessagePayload(payload) {
  let text = payload;
  if (typeof text !== 'string') {
    try {
      if (typeof TextDecoder !== 'undefined') {
        text = new TextDecoder('utf-8').decode(payload);
      } else {
        text = String.fromCharCode.apply(null, new Uint8Array(payload));
      }
    } catch (e) {
      text = '';
    }
  }
  if (!text) {
    return;
  }

  frameBuffer += text;

  let splitIdx = frameBuffer.indexOf('\0');
  while (splitIdx >= 0) {
    const rawFrame = frameBuffer.slice(0, splitIdx);
    frameBuffer = frameBuffer.slice(splitIdx + 1);

    const frame = parseFrame(rawFrame);
    if (frame) {
      handleStompFrame(frame);
    }

    splitIdx = frameBuffer.indexOf('\0');
  }
}

function handleStompFrame(frame) {
  const { command, headers, body } = frame;

  if (command === 'CONNECTED') {
    connected = true;
    connecting = false;
    reconnectAttempts = 0;
    log('CONNECTED', headers);
    subscribeDefaultQueues();
    startHeartbeat();
    emit('connected', headers);
    return;
  }

  if (command === 'MESSAGE') {
    const destination = headers.destination || '';
    const eventName = STOMP_EVENT_MAP[destination] || 'im-unknown';
    let data = body;
    try {
      data = body ? JSON.parse(body) : {};
    } catch (e) {
      data = { raw: body };
    }
    log('MESSAGE', {
      destination,
      eventName,
      bodyLength: body ? body.length : 0
    });
    emit(eventName, data);
    emit('im-message-frame', {
      destination,
      headers,
      data
    });
    return;
  }

  if (command === 'ERROR') {
    log('ERROR', { headers, body });
    emit('error', {
      headers,
      body
    });
    return;
  }
}

function cleanupSocketState() {
  connecting = false;
  connected = false;
  stopHeartbeat();
}

function bindSocketEvents(task) {
  task.onOpen(() => {
    const token = getToken();
    log('OPEN', { hasToken: !!token });
    sendFrame('CONNECT', {
      'accept-version': '1.2',
      'heart-beat': '10000,10000',
      Authorization: token ? `Bearer ${token}` : ''
    });
  });

  task.onMessage((res) => {
    handleMessagePayload(res.data);
  });

  task.onError((err) => {
    log('WS_ERROR', err);
    emit('error', err);
  });

  task.onClose(() => {
    log('CLOSE');
    cleanupSocketState();
    frameBuffer = '';
    emit('disconnected');
    socketTask = null;
    scheduleReconnect();
  });
}

function connect() {
  const token = getToken();
  if (!token) {
    log('CONNECT_SKIP', 'missing token');
    return;
  }
  if (connected || connecting) {
    return;
  }

  shouldReconnect = true;
  connecting = true;
  clearReconnectTimer();

  const url = buildWsUrl(token);
  log('CONNECT', { url });
  socketTask = wx.connectSocket({
    url,
    timeout: 10000
  });

  bindSocketEvents(socketTask);
}

function disconnect() {
  shouldReconnect = false;
  clearReconnectTimer();
  stopHeartbeat();
  frameBuffer = '';
  if (socketTask) {
    try {
      socketTask.close();
    } catch (e) {
      // ignore close error
    }
  }
  socketTask = null;
  cleanupSocketState();
}

function sendJson(destination, payload) {
  if (!connected) {
    log('SEND_BLOCKED', { destination, reason: 'not connected' });
    return false;
  }
  log('SEND', {
    destination,
    payloadKeys: payload && typeof payload === 'object' ? Object.keys(payload) : [],
    payload
  });
  return sendFrame('SEND', {
    destination,
    'content-type': 'application/json'
  }, JSON.stringify(payload || {}));
}

function sendMessage(payload) {
  return sendJson('/app/im/send', payload);
}

function ackMessage(payload) {
  return sendJson('/app/im/ack', payload);
}

function syncConversation(payload) {
  return sendJson('/app/im/sync', payload);
}

function sendTyping(payload) {
  return sendJson('/app/im/typing', payload);
}

function ensureConnected() {
  connect();
}

function isConnected() {
  return connected;
}

module.exports = {
  on,
  off,
  connect,
  disconnect,
  ensureConnected,
  isConnected,
  sendMessage,
  ackMessage,
  syncConversation,
  sendTyping
};
