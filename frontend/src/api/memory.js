import request from '@/utils/request'

// 获取记忆列表（会话列表）
export function getMemoryList(params) {
  return request.get('/memory/list', { params })
}

// 获取会话详情（包含消息时间线）
export function getSessionDetail(sessionId) {
  return request.get(`/memory/session/${sessionId}`)
}

// 删除会话记忆
export function deleteSessionMemory(sessionId) {
  return request.delete(`/memory/session/${sessionId}`)
}

// 添加记忆
export function addMemory(data) {
  return request.post('/memory/add', data)
}

// 更新记忆
export function updateMemory(memoryId, data) {
  return request.put(`/memory/${memoryId}`, data)
}

// 删除记忆
export function deleteMemory(memoryId) {
  return request.delete(`/memory/${memoryId}`)
}

// 搜索记忆
export function searchMemory(params) {
  return request.get('/memory/search', { params })
}

// 导出记忆
export function exportMemory(params) {
  return request.get('/memory/export', {
    params,
    responseType: 'blob'
  })
}

// 清空所有记忆
export function clearAllMemory() {
  return request.delete('/memory/clear')
}
