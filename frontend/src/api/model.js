import request from '@/utils/request'

// 获取模型列表
export function getModelList(params) {
  return request.get('/model/list', { params })
}

// 获取模型详情
export function getModelDetail(modelId) {
  return request.get(`/model/${modelId}`)
}

// 创建自定义模型配置
export function createModel(data) {
  return request.post('/model/create', data)
}

// 更新模型配置
export function updateModel(modelId, data) {
  return request.put(`/model/${modelId}`, data)
}

// 删除模型
export function deleteModel(modelId) {
  return request.delete(`/model/${modelId}`)
}

// 测试模型连接
export function testModel(data) {
  return request.post('/model/test', data)
}

// 单模型健康检查
export function healthCheck(modelCode) {
  return request.post(`/model/${modelCode}/health-check`)
}

// 批量健康检查
export function batchHealthCheck(data) {
  return request.post('/model/batch-health-check', data)
}
