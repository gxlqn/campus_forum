import http from '@/utils/request'

export function getUserList(params) {
  return http.get('/admin/system/users', { params })
}

export function updateUserStatus(userId, status) {
  return http.patch(`/admin/system/users/${userId}/status`, { status })
}

export function updateUserVerify(userId, isVerified) {
  return http.patch(`/admin/system/users/${userId}/verify`, { isVerified })
}

export function assignUserRoles(userId, roleIds) {
  return http.patch(`/admin/system/users/${userId}/roles`, { roleIds })
}

export function getRoleList() {
  return http.get('/admin/system/roles')
}

export function getPermissionList() {
  return http.get('/admin/system/permissions')
}

export function getRolePermissionIds(roleId) {
  return http.get(`/admin/system/roles/${roleId}/permissions`)
}

export function assignRolePermissions(roleId, permissionIds) {
  return http.patch(`/admin/system/roles/${roleId}/permissions`, { permissionIds })
}

// 审核相关接口
export function getAuditItems(params) {
  return http.get('/admin/system/audit/items', { params })
}

export function auditItem(type, id, payload) {
  return http.patch(`/admin/system/audit/items/${type}/${id}`, payload)
}

export function getSectionList(params) {
  return http.get('/admin/system/sections', { params })
}

export function createSection(data) {
  return http.post('/admin/system/sections', data)
}

export function updateSection(id, data) {
  return http.put(`/admin/system/sections/${id}`, data)
}

export function updateSectionStatus(id, status) {
  return http.patch(`/admin/system/sections/${id}/status`, { status })
}

export function getProductCategories() {
  return http.get('/admin/system/categories')
}

export function createProductCategory(data) {
  return http.post('/admin/system/categories', data)
}

export function updateProductCategory(id, data) {
  return http.put(`/admin/system/categories/${id}`, data)
}

export function updateProductCategoryStatus(id, status) {
  return http.patch(`/admin/system/categories/${id}/status`, { status })
}

export function getOverviewStats() {
  return http.get('/admin/system/stats/overview')
}

export function getTrendStats(days = 7) {
  return http.get('/admin/system/stats/trend', { params: { days } })
}

export function getSectionDistribution() {
  return http.get('/admin/system/stats/sections')
}

export function getReportList(params) {
  return http.get('/admin/system/reports', { params })
}

export function handleReport(id, payload) {
  return http.patch(`/admin/system/reports/${id}/handle`, payload)
}

export function resolvePostReport(id, payload) {
  return http.patch(`/admin/system/reports/${id}/resolve-post`, payload)
}

// 失物认领审核接口
export function getLostFoundClaimList(params) {
  return http.get('/admin/system/lostfound-claims', { params })
}

export function auditLostFoundClaim(id, payload) {
  return http.post(`/admin/system/lostfound-claims/${id}/audit`, payload)
}
