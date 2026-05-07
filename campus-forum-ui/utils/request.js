/**
 * HTTP Request Module
 */
const REQUEST_TIMEOUT = 15000;
const UPLOAD_TIMEOUT = 30000;
const MAX_CONCURRENT_REQUESTS = 6;
const DEFAULT_RETRY_TIMES = 2;

let activeRequestCount = 0;
const requestQueue = [];
let lastToastAt = 0;

function delay(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
}

function showErrorToast(message) {
    const now = Date.now();
    if (now - lastToastAt < 1200) {
        return;
    }
    lastToastAt = now;
    wx.showToast({
        title: message || '网络异常',
        icon: 'none'
    });
}

function runWithQueue(task) {
    return new Promise((resolve, reject) => {
        const execute = () => {
            activeRequestCount += 1;
            task()
                .then(resolve)
                .catch(reject)
                .finally(() => {
                    activeRequestCount -= 1;
                    const nextTask = requestQueue.shift();
                    if (nextTask) {
                        nextTask();
                    }
                });
        };

        if (activeRequestCount < MAX_CONCURRENT_REQUESTS) {
            execute();
        } else {
            requestQueue.push(execute);
        }
    });
}

function isRetryableError(err) {
    const msg = err?.errMsg || '';
    return msg.includes('timeout') || msg.includes('fail') || msg.includes('Network');
}

function getBaseUrl() {
    try {
        const app = getApp();
        if (app && app.globalData && app.globalData.baseUrl) {
            return app.globalData.baseUrl;
        }
    } catch (e) {
        // ignore getApp error before App init
    }
    return 'http://localhost:8081/api';
}

function buildUrl(url, params) {
    const baseUrl = getBaseUrl();
    if (!params) {
        return baseUrl + url;
    }
    const query = Object.keys(params)
        .filter((key) => params[key] !== undefined && params[key] !== null && params[key] !== '' && params[key] !== 'null')
        .map((key) => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
        .join('&');
    if (!query) {
        return baseUrl + url;
    }
    return `${baseUrl}${url}${url.includes('?') ? '&' : '?'}${query}`;
}

function request(url, method, data, options = {}) {
    const timeout = options.timeout || REQUEST_TIMEOUT;
    const retryTimes = typeof options.retryTimes === 'number' ? options.retryTimes : DEFAULT_RETRY_TIMES;

    const doRequest = (attempt = 0) => new Promise((resolve, reject) => {
        const token = wx.getStorageSync('token');

        wx.request({
            url: buildUrl(url, options.params),
            method: method,
            data: data,
            timeout,
            header: {
                'Content-Type': 'application/json',
                'Authorization': token ? `Bearer ${token}` : ''
            },
            success(res) {
                if (res.statusCode === 200) {
                    if (res.data.code === 200 || res.data.code === 0) {
                        resolve(res.data);
                    } else {
                        showErrorToast(res.data.message || res.data.msg || '请求失败');
                        reject(res.data);
                    }
                    return;
                }

                if (res.statusCode === 401) {
                    wx.removeStorageSync('token');
                    wx.removeStorageSync('userInfo');
                    showErrorToast('请先登录');
                    reject(res.data);
                    return;
                }

                // 5xx 错误允许短暂重试，减轻瞬时波动影响
                if (res.statusCode >= 500 && attempt < retryTimes) {
                    delay(250 * Math.pow(2, attempt)).then(() => {
                        doRequest(attempt + 1).then(resolve).catch(reject);
                    });
                    return;
                }

                showErrorToast('网络异常');
                reject(res.data);
            },
            fail(err) {
                if (attempt < retryTimes && isRetryableError(err)) {
                    delay(250 * Math.pow(2, attempt)).then(() => {
                        doRequest(attempt + 1).then(resolve).catch(reject);
                    });
                    return;
                }

                const msg = err?.errMsg && err.errMsg.includes('timeout') ? '请求超时，请重试' : '网络异常';
                showErrorToast(msg);
                reject(err);
            }
        });
    });

    return runWithQueue(() => doRequest(0));
}

module.exports = {
    get(url, params) {
        return request(url, 'GET', null, { params });
    },
    post(url, data, options) {
        return request(url, 'POST', data, options);
    },
    put(url, data) {
        return request(url, 'PUT', data);
    },
    patch(url, data) {
        return request(url, 'PATCH', data);
    },
    del(url, data) {
        return request(url, 'DELETE', data);
    },
    uploadFile(filePath) {
        return new Promise((resolve, reject) => {
            const token = wx.getStorageSync('token');
            wx.uploadFile({
                url: getBaseUrl() + '/file/upload',
                filePath: filePath,
                name: 'file',
                timeout: UPLOAD_TIMEOUT,
                header: {
                    'Authorization': token ? `Bearer ${token}` : ''
                },
                success(res) {
                    let data;
                    try {
                        data = JSON.parse(res.data);
                    } catch (e) {
                        wx.showToast({
                            title: '上传响应异常',
                            icon: 'none'
                        });
                        reject(e);
                        return;
                    }
                    if (res.statusCode === 200 && data && (data.code === 200 || data.code === 0) && data.data && data.data.url) {
                        resolve(data.data.url);
                        return;
                    }
                    wx.showToast({
                        title: (data && (data.message || data.msg)) || '上传失败',
                        icon: 'none'
                    });
                    reject(data || res);
                },
                fail(err) {
                    const msg = err?.errMsg && err.errMsg.includes('timeout') ? '上传超时，请重试' : '上传失败';
                    showErrorToast(msg);
                    reject(err);
                }
            });
        });
    }
};
