import request from '@/utils/request'

// 获取模型列表
export function getModelList(params) {
  return request.get('/model/list', { params })
}

// 单模型健康检查（后端路径: GET /model/health/{modelCode}）
export function healthCheck(modelCode) {
  return request.get(`/model/health/${modelCode}`)
}

// 批量模型健康检查（后端路径: POST /model/health/batch）
export function batchHealthCheck(data) {
  return request.post('/model/health/batch', data)
}
