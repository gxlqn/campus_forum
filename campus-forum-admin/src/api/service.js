import http from "@/utils/request"

// ==================== 活动管理 ====================
export function getActivityList(params) {
  return http.get("/activities/admin", { params })
}

export function auditActivity(id, status) {
  return http.post(`/activities/${id}/audit`, null, { params: { auditStatus: status } })
}

// ==================== 互助管理 ====================
export function getHelpList(params) {
  return http.get("/help/admin", { params })
}

export function auditHelp(id, status) {
  return http.post(`/help/admin/${id}/audit`, null, { params: { auditStatus: status } })
}

// ==================== 互助仲裁 ====================
export function getArbitrationList(params) {
  return http.get("/help/admin/arbitration/list", { params })
}

export function resolveArbitration(data) {
  return http.post("/help/admin/arbitration/resolve", data)
}

// ==================== 失物招领管理 ====================
export function getLostFoundAdminList(params) {
  return http.get("/lostfound/admin", { params })
}

export function auditLostFound(id, auditStatus) {
  return http.post(`/lostfound/${id}/audit`, null, { params: { auditStatus } })
}

export function deleteLostFound(id) {
  return http.delete(`/lostfound/admin/${id}`)
}

export function getLostFoundDetail(id) {
  return http.get(`/lostfound/${id}`)
}

// ==================== 商品管理（管理员） ====================
export function getProductAdminList(params) {
  return http.get("/products/admin", { params })
}

export function auditProduct(id, auditStatus) {
  return http.post(`/products/${id}/audit`, null, { params: { auditStatus } })
}

export function offProduct(id, status = 0) {
  return http.post(`/products/admin/${id}/status`, null, { params: { status } })
}

export function deleteProduct(id) {
  return http.delete(`/products/admin/${id}`)
}

export function getProductDetail(id) {
  return http.get(`/products/${id}`)
}

// ==================== 敏感词管理 ====================
export function getSensitiveWords(params) {
  return http.get("/admin/system/sensitive-words", { params })
}

export function addSensitiveWord(data) {
  return http.post("/admin/system/sensitive-words", data)
}

export function updateSensitiveWord(id, data) {
  return http.put(`/admin/system/sensitive-words/${id}`, data)
}

export function deleteSensitiveWord(id) {
  return http.delete(`/admin/system/sensitive-words/${id}`)
}
