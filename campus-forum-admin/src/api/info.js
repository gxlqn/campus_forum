import http from '@/utils/request'

export function getAdminNewsList(params) {
  return http.get('/admin/info/news', { params })
}

export function getAdminNewsDetail(id) {
  return http.get(`/admin/info/news/${id}`)
}

export function createAdminNews(data) {
  return http.post('/admin/info/news', data)
}

export function updateAdminNews(id, data) {
  return http.put(`/admin/info/news/${id}`, data)
}

export function updateAdminNewsStatus(id, status) {
  return http.patch(`/admin/info/news/${id}/status`, { status })
}

export function deleteAdminNews(id) {
  return http.delete(`/admin/info/news/${id}`)
}

export function getAdminNewsCategories() {
  return http.get('/admin/info/news/categories')
}

export function getAdminNavList(params) {
  return http.get('/admin/info/nav', { params })
}

export function createAdminNav(data) {
  return http.post('/admin/info/nav', data)
}

export function updateAdminNav(id, data) {
  return http.put(`/admin/info/nav/${id}`, data)
}

export function updateAdminNavStatus(id, status) {
  return http.patch(`/admin/info/nav/${id}/status`, { status })
}

export function deleteAdminNav(id) {
  return http.delete(`/admin/info/nav/${id}`)
}

export function getAdminNavCategories() {
  return http.get('/admin/info/nav/categories')
}