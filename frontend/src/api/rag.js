import request from '@/utils/request'

// 上传文档
export function uploadDocument(data) {
  return request.post('/rag/upload', data, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 获取文档列表
export function getDocumentList(params) {
  return request.get('/rag/documents', { params })
}

// 删除文档
export function deleteDocument(documentId) {
  return request.delete(`/rag/document/${documentId}`)
}

// 获取知识库列表
export function getKnowledgeBaseList() {
  return request.get('/rag/knowledge-bases')
}

// 创建知识库
export function createKnowledgeBase(data) {
  return request.post('/rag/knowledge-base', data)
}

// 删除知识库
export function deleteKnowledgeBase(baseId) {
  return request.delete(`/rag/knowledge-base/${baseId}`)
}

// 文档向量化状态
export function getVectorizeStatus(documentId) {
  return request.get(`/rag/vectorize-status/${documentId}`)
}
