import request from '@/utils/request'

// 获取会话列表
export function getSessionList(params) {
  return request.get('/sessions', { params })
}

// 获取会话详情
export function getSessionDetail(sessionId) {
  return request.get(`/sessions/${sessionId}`)
}

// 创建新会话
export function createSession(data) {
  return request.post('/sessions', data)
}

// 更新会话信息（包含重命名功能）
export function updateSession(sessionId, data) {
  return request.put(`/sessions/${sessionId}`, data)
}

// 删除会话
export function deleteSession(sessionId) {
  return request.delete(`/sessions/${sessionId}`)
}

// 重命名会话（复用 updateSession 接口）
export function renameSession(sessionId, name) {
  return request.put(`/sessions/${sessionId}`, { title: name })
}

// 归档会话
export function archiveSessionApi(sessionId) {
  return request.post(`/sessions/${sessionId}/archive`)
}

// 取消归档（恢复会话）
export function unarchiveSessionApi(sessionId) {
  return request.post(`/sessions/${sessionId}/unarchive`)
}
