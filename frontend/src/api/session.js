import request from '@/utils/request'

// 获取会话列表
export function getSessionList(params) {
  return request.get('/session/list', { params })
}

// 获取会话详情
export function getSessionDetail(sessionId) {
  return request.get(`/session/${sessionId}`)
}

// 创建新会话
export function createSession(data) {
  return request.post('/session/create', data)
}

// 更新会话信息
export function updateSession(sessionId, data) {
  return request.put(`/session/${sessionId}`, data)
}

// 删除会话
export function deleteSession(sessionId) {
  return request.delete(`/session/${sessionId}`)
}

// 重命名会话
export function renameSession(sessionId, name) {
  return request.put(`/session/${sessionId}/rename`, { name })
}

// 归档会话
export function archiveSessionApi(sessionId) {
  return request.post(`/session/${sessionId}/archive`)
}

// 取消归档（恢复会话）
export function unarchiveSessionApi(sessionId) {
  return request.post(`/session/${sessionId}/unarchive`)
}
