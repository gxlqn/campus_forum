import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// 请求拦截器
http.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        
        // 添加请求时间戳防止缓存（可选）
        if (config.method === 'get') {
            config.params = {
                ...config.params,
                _t: Date.now()
            }
        }
        
        return config
    },
    error => {
        console.error('Request error:', error)
        return Promise.reject(error)
    }
)

// 响应拦截器
http.interceptors.response.use(
    response => {
        const res = response.data
        
        // 根据实际后端返回结构调整
        if (res.code === 200 || res.code === 0) {
            return res.data !== undefined ? res.data : res
        }
        
        // 业务错误
        if (res.code === 401) {
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')
            
            if (window.location.pathname !== '/login') {
                ElMessage.error(res.message || '登录已过期')
                window.location.href = '/login'
            }
            return Promise.reject(res)
        }

        // 权限不足（业务层返回的 code=403）
        if (res.code === 403) {
            ElMessage.error(res.message || '无权访问该资源')
            return Promise.reject(res)
        }
        
        ElMessage.error(res.message || `请求失败 (${res.code})`)
        return Promise.reject(res)
    },
    error => {
        // 网络错误或超时
        if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
            ElMessage.error('请求超时，请稍后重试')
        } else if (error.message.includes('Network Error')) {
            ElMessage.error('网络连接失败，请检查网络')
        } else if (error.response) {
            const { status, data } = error.request ? { status: error.response.status, data: null } : { status: 500, data: null }
            
            // HTTP 401：未认证 → 跳转登录页
            if (status === 401) {
                localStorage.removeItem('token')
                localStorage.removeItem('userInfo')
                if (window.location.pathname !== '/login') {
                    ElMessage.error('登录已过期，请重新登录')
                    window.location.href = '/login'
                }
                return Promise.reject(error)
            }
            
            // HTTP 403：权限不足
            if (status === 403) {
                const errorMsg = (data && typeof data === 'string') ? data
                    : (data?.message) || '无权访问该资源，权限不足'
                ElMessage.error(errorMsg)
                return Promise.reject(error)
            }

            const errorMsg = data?.message || `请求失败 (${status})`
            ElMessage.error(errorMsg)
        } else {
            ElMessage.error(error.message || '未知错误')
        }
        
        return Promise.reject(error)
    }
)

export default http