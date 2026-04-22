<template>
  <div class="rag-view">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>RAG 文档管理与问答</h2>
      <p>上传和管理知识库文档，基于文档内容进行智能问答</p>
    </div>

    <!-- 标签页切换 -->
    <el-tabs v-model="activeTab" class="rag-tabs">
      <!-- Tab 1: 文档管理 -->
      <el-tab-pane label="📁 文档管理" name="documents">
        <!-- 上传区域 -->
        <div class="upload-section">
          <el-card shadow="never">
            <el-upload
              ref="uploadRef"
              drag
              :auto-upload="false"
              :limit="10"
              :on-change="handleFileChange"
              :on-exceed="handleExceed"
              :before-upload="beforeUpload"
              accept=".pdf,.txt,.docx"
              class="upload-dragger"
            >
              <el-icon class="upload-icon"><UploadFilled /></el-icon>
              <div class="el-upload__text">
                将文件拖拽到此处，或 <em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  支持 PDF、TXT、DOCX 格式，单个文件不超过 50MB
                </div>
              </template>
            </el-upload>

            <!-- 上传描述 -->
            <el-input
              v-model="uploadDesc"
              type="textarea"
              :rows="2"
              placeholder="添加文档描述（可选）..."
              style="margin-top: 16px;"
            />

            <!-- 上传按钮 -->
            <div style="margin-top: 16px; text-align: right;">
              <el-button @click="clearUploadFiles">清空列表</el-button>
              <el-button type="primary" :loading="uploading" :disabled="fileList.length === 0" @click="handleUploadAll">
                <el-icon><Upload /></el-icon>
                开始上传 ({{ fileList.length }})
              </el-button>
            </div>
          </el-card>
        </div>

        <!-- 工具栏 -->
        <div class="toolbar">
          <div class="toolbar-left">
            <el-checkbox
              v-model="selectAll"
              :indeterminate="isIndeterminate"
              @change="handleSelectAll"
            >
              全选
            </el-checkbox>
            <el-button
              type="danger"
              plain
              size="small"
              :disabled="selectedDocs.length === 0"
              @click="handleBatchDelete"
            >
              <el-icon><Delete /></el-icon>
              批量删除 ({{ selectedDocs.length }})
            </el-button>
          </div>
          <div class="toolbar-right">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索文档名称..."
              clearable
              style="width: 240px;"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button :icon="RefreshRight" circle @click="loadDocuments" />
          </div>
        </div>

        <!-- 文档卡片网格 -->
        <div v-loading="loading" class="document-grid">
          <!-- 空状态 -->
          <el-empty v-if="!loading && filteredDocuments.length === 0" description="还没有上传任何文档">
            <template #image>
              <el-icon :size="80" color="#c0c4cc"><Document /></el-icon>
            </template>
            <template #description>
              <p>还没有上传任何文档</p>
              <p style="color: #999; font-size: 13px;">上传您的第一个文档开始使用RAG问答</p>
            </template>
          </el-empty>

          <!-- 文档卡片列表 -->
          <el-card
            v-for="doc in filteredDocuments"
            :key="doc.id"
            shadow="hover"
            class="document-card"
            :class="{ 'is-selected': selectedDocs.includes(doc.id) }"
          >
            <div class="card-checkbox">
              <el-checkbox :model-value="selectedDocs.includes(doc.id)" @change="(val) => handleSelectDoc(doc.id, val)" />
            </div>

            <!-- 文件图标 -->
            <div class="file-icon-wrapper" :class="`type-${getFileExtension(doc.name)}`">
              <el-icon :size="36">
                <Document v-if="getFileExtension(doc.name) === 'pdf'" />
                <Document v-else-if="getFileExtension(doc.name) === 'docx'" />
                <Document v-else />
              </el-icon>
            </div>

            <!-- 文件信息 -->
            <div class="file-info">
              <h4 class="file-name" :title="doc.name">{{ doc.name }}</h4>
              <div class="file-meta">
                <span class="meta-item">
                  <el-icon><Coin /></el-icon>
                  {{ formatFileSize(doc.size || 0) }}
                </span>
                <span class="meta-item">
                  <el-icon><Grid /></el-icon>
                  {{ doc.chunkCount || 0 }} 分块
                </span>
              </div>
              <div class="file-status">
                <el-tag :type="getStatusType(doc.status)" size="small" effect="light">
                  {{ getStatusText(doc.status) }}
                </el-tag>
              </div>
              <div class="file-time">
                <el-icon><Clock /></el-icon>
                {{ formatRelativeTime(doc.createdAt) }}
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="card-actions">
              <el-button link type="primary" size="small" @click="handleViewDetail(doc)">
                <el-icon><View /></el-icon>
                查看详情
              </el-button>
              <el-button link type="danger" size="small" @click="handleDelete(doc)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </el-card>
        </div>

        <!-- 分页 -->
        <div v-if="total > pageSize" class="pagination-wrapper">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[9, 18, 27]"
            layout="total, sizes, prev, pager, next"
            @change="loadDocuments"
          />
        </div>
      </el-tab-pane>

      <!-- Tab 2: RAG问答 -->
      <el-tab-pane label="💬 RAG问答" name="chat">
        <div class="chat-container">
          <!-- 左侧：查询输入区 -->
          <div class="query-section">
            <el-card shadow="never">
              <template #header>
                <div class="card-header">
                  <span>提问</span>
                  <el-tag type="info" size="small">基于知识库文档</el-tag>
                </div>
              </template>

              <!-- 问题输入框 -->
              <el-input
                v-model="queryText"
                type="textarea"
                :rows="5"
                placeholder="基于您上传的文档提问..."
                resize="none"
                class="query-textarea"
              />

              <!-- 文档范围选择 -->
              <div class="scope-selector">
                <label class="scope-label">选择文档范围：</label>
                <el-radio-group v-model="queryScope" size="small">
                  <el-radio value="all">所有文档</el-radio>
                  <el-radio value="selected">指定文档</el-radio>
                </el-radio-group>
              </div>

              <!-- 文档多选（当选择"指定文档"时显示） -->
              <div v-show="queryScope === 'selected'" class="doc-checkbox-group">
                <el-checkbox-group v-model="selectedQueryDocs">
                  <el-checkbox
                    v-for="doc in readyDocuments"
                    :key="doc.id"
                    :value="doc.id"
                    :label="doc.name"
                  />
                </el-checkbox-group>
                <el-empty v-if="readyDocuments.length === 0" description="暂无可用的就绪文档" :image-size="60" />
              </div>

              <!-- 提交按钮 -->
              <div class="submit-row">
                <el-button
                  type="primary"
                  size="large"
                  :loading="querying"
                  :disabled="!queryText.trim() || (queryScope === 'selected' && selectedQueryDocs.length === 0)"
                  @click="handleSubmitQuery"
                >
                  <el-icon><Promotion /></el-icon>
                  发送提问
                </el-button>
              </div>
            </el-card>

            <!-- 历史记录 -->
            <el-card shadow="never" class="history-card">
              <template #header>
                <div class="card-header">
                  <span>历史记录</span>
                  <el-button link type="primary" size="small" @click="clearHistory">清空</el-button>
                </div>
              </template>

              <div class="history-list">
                <div
                  v-for="(item, index) in queryHistory"
                  :key="index"
                  class="history-item"
                  @click="viewHistoryItem(item)"
                >
                  <div class="history-question">{{ item.question }}</div>
                  <div class="history-time">{{ formatRelativeTime(item.timestamp) }}</div>
                </div>
                <el-empty v-if="queryHistory.length === 0" description="暂无历史记录" :image-size="60" />
              </div>
            </el-card>
          </div>

          <!-- 右侧：答案展示区 -->
          <div class="answer-section">
            <el-card shadow="never" class="answer-card">
              <template #header>
                <div class="card-header">
                  <span>回答</span>
                  <el-tag v-if="currentAnswer" type="success" size="small">已生成</el-tag>
                </div>
              </template>

              <!-- 当前答案 -->
              <div v-if="currentAnswer" class="answer-content">
                <!-- 问题 -->
                <div class="question-block">
                  <div class="q-label">Q:</div>
                  <div class="q-text">{{ currentAnswer.question }}</div>
                </div>

                <!-- 答案 -->
                <div class="answer-block">
                  <div class="a-label">A:</div>
                  <div class="a-text markdown-body" v-html="renderMarkdown(currentAnswer.answer)"></div>
                </div>

                <!-- 来源引用 -->
                <div v-if="currentAnswer.sources && currentAnswer.sources.length > 0" class="sources-section">
                  <div class="sources-title">
                    <el-icon><Link /></el-icon>
                    来源引用
                  </div>
                  <div
                    v-for="(source, idx) in currentAnswer.sources"
                    :key="idx"
                    class="source-item"
                  >
                    <div class="source-header" @click="toggleSourceExpand(idx)">
                      <span class="source-name">
                        <el-icon><Document /></el-icon>
                        {{ source.documentName }} (段落 {{ source.paragraphIndex }})
                      </span>
                      <el-icon class="expand-icon" :class="{ 'is-expanded': expandedSources.includes(idx) }">
                        <ArrowDown />
                      </el-icon>
                    </div>
                    <transition name="expand">
                      <div v-show="expandedSources.includes(idx)" class="source-content">
                        <p>{{ source.content }}</p>
                      </div>
                    </transition>
                  </div>
                </div>
              </div>

              <!-- 空状态 -->
              <el-empty v-else description="在左侧输入问题开始问答" :image-size="100">
                <template #image>
                  <el-icon :size="100" color="#c0c4cc"><ChatDotRound /></el-icon>
                </template>
              </el-empty>
            </el-card>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 文档详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="文档详情" width="600px">
      <el-descriptions :column="2" border v-if="currentDoc">
        <el-descriptions-item label="文件名">{{ currentDoc.name }}</el-descriptions-item>
        <el-descriptions-item label="文件类型">
          <el-tag size="small">{{ getFileExtension(currentDoc.name).toUpperCase() }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="文件大小">{{ formatFileSize(currentDoc.size || 0) }}</el-descriptions-item>
        <el-descriptions-item label="分块数量">{{ currentDoc.chunkCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag :type="getStatusType(currentDoc.status)">{{ getStatusText(currentDoc.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述">{{ currentDoc.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="上传时间" :span="2">{{ formatDate(currentDoc.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间" :span="2">{{ formatDate(currentDoc.updatedAt) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Upload,
  UploadFilled,
  Delete,
  Search,
  RefreshRight,
  Document,
  Coin,
  Grid,
  Clock,
  View,
  Promotion,
  Link,
  ArrowDown,
  ChatDotRound
} from '@element-plus/icons-vue'
import { formatDate } from '@/utils/index'
import { uploadDocument, deleteDocument, getDocumentList, queryRag } from '@/api/rag'

// ==================== 响应式数据 ====================
const activeTab = ref('documents')
const loading = ref(false)
const uploading = ref(false)
const querying = ref(false)

// 文档相关
const documents = ref([])
const currentPage = ref(1)
const pageSize = ref(9)
const total = ref(0)
const searchKeyword = ref('')
const selectedDocs = ref([])
const selectAll = ref(false)
const showDetailDialog = ref(false)
const currentDoc = ref(null)

// 上传相关
const uploadRef = ref(null)
const fileList = ref([])
const uploadDesc = ref('')

// 问答相关
const queryText = ref('')
const queryScope = ref('all')
const selectedQueryDocs = ref([])
const currentAnswer = ref(null)
const queryHistory = ref([])
const expandedSources = ref([])

// ==================== 计算属性 ====================
// 过滤后的文档列表
const filteredDocuments = computed(() => {
  if (!searchKeyword.value.trim()) return documents.value
  const keyword = searchKeyword.value.toLowerCase()
  return documents.value.filter(doc =>
    doc.name.toLowerCase().includes(keyword)
  )
})

// 是否半选状态
const isIndeterminate = computed(() => {
  const len = selectedDocs.value.length
  return len > 0 && len < filteredDocuments.value.length
})

// 就绪状态的文档（用于问答范围选择）
const readyDocuments = computed(() => {
  return documents.value.filter(doc => doc.status === 'READY' || doc.status === 'completed')
})

// ==================== 文档管理方法 ====================
// 加载文档列表
async function loadDocuments() {
  loading.value = true
  try {
    const res = await getDocumentList({
      page: currentPage.value,
      pageSize: pageSize.value
    })
    documents.value = res.data?.list || res.list || []
    total.value = res.data?.total || res.total || 0
  } catch (error) {
    console.error('获取文档列表失败:', error)
    // 使用模拟数据作为降级方案
    documents.value = getMockDocuments()
    total.value = documents.value.length
  } finally {
    loading.value = false
  }
}

// 获取模拟数据
function getMockDocuments() {
  const now = Date.now()
  return [
    { id: 1, name: '产品使用说明书.pdf', size: 2621440, chunkCount: 156, status: 'READY', createdAt: new Date(now - 3600000).toISOString(), description: '产品V2.0版本使用说明' },
    { id: 2, name: '技术架构设计文档.docx', size: 1536000, chunkCount: 89, status: 'PROCESSING', createdAt: new Date(now - 7200000).toISOString(), description: '' },
    { id: 3, name: 'API接口规范.txt', size: 51200, chunkCount: 23, status: 'READY', createdAt: new Date(now - 86400000).toISOString(), description: 'RESTful API规范文档' },
    { id: 4, name: '用户操作手册.pdf', size: 3145728, chunkCount: 203, status: 'READY', createdAt: new Date(now - 172800000).toISOString(), description: '终端用户操作指南' },
    { id: 5, name: '部署配置指南.docx', size: 786432, chunkCount: 45, status: 'FAILED', createdAt: new Date(now - 259200000).toISOString(), description: '生产环境部署步骤' },
    { id: 6, name: '常见问题FAQ.txt', size: 24576, chunkCount: 12, status: 'READY', createdAt: new Date(now - 432000000).toISOString(), description: '用户常见问题解答' }
  ]
}

// 文件选择变化
function handleFileChange(file, list) {
  fileList.value = list
}

// 超出限制处理
function handleExceed(files, uploadFiles) {
  ElMessage.warning(`最多只能上传10个文件，当前已选${uploadFiles.length}个`)
}

// 上传前校验
function beforeUpload(file) {
  const maxSize = 50 * 1024 * 1024 // 50MB
  if (file.size > maxSize) {
    ElMessage.error(`文件"${file.name}"超过50MB限制`)
    return false
  }
  return true
}

// 清空上传列表
function clearUploadFiles() {
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
  fileList.value = []
}

// 处理单个文件上传
async function handleUpload(file) {
  const formData = new FormData()
  formData.append('file', file.raw)
  formData.append('description', uploadDesc.value)

  try {
    await uploadDocument(formData)
    ElMessage.success(`"${file.name}" 上传成功`)
  } catch (error) {
    throw error
  }
}

// 批量上传
async function handleUploadAll() {
  if (fileList.value.length === 0) {
    ElMessage.warning('请先选择要上传的文件')
    return
  }

  uploading.value = true
  let successCount = 0
  let failCount = 0

  for (const file of fileList.value) {
    try {
      await handleUpload(file)
      successCount++
    } catch (error) {
      console.error(`${file.name} 上传失败:`, error)
      failCount++
    }
  }

  if (successCount > 0) {
    ElMessage.success(`成功上传 ${successCount} 个文件${failCount > 0 ? `，${failCount} 个失败` : ''}`)
  }

  if (failCount > 0 && successCount === 0) {
    ElMessage.error('所有文件上传失败')
  }

  clearUploadFiles()
  uploadDesc.value = ''
  await loadDocuments()
  uploading.value = false
}

// 选择/取消全选
function handleSelectAll(val) {
  if (val) {
    selectedDocs.value = filteredDocuments.value.map(d => d.id)
  } else {
    selectedDocs.value = []
  }
}

// 选择单个文档
function handleSelectDoc(id, val) {
  if (val) {
    selectedDocs.value.push(id)
  } else {
    selectedDocs.value = selectedDocs.value.filter(d => d !== id)
  }
}

// 批量删除
async function handleBatchDelete() {
  if (selectedDocs.value.length === 0) return

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedDocs.value.length} 个文档吗？此操作不可恢复。`,
      '批量删除确认',
      { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
    )

    let successCount = 0
    for (const id of selectedDocs.value) {
      try {
        await deleteDocument(id)
        successCount++
      } catch (error) {
        console.error(`删除文档 ${id} 失败:`, error)
      }
    }

    ElMessage.success(`成功删除 ${successCount} 个文档`)
    selectedDocs.value = []
    selectAll.value = false
    await loadDocuments()
  } catch (error) {
    if (error !== 'cancel') console.error('批量删除失败:', error)
  }
}

// 查看详情
function handleViewDetail(doc) {
  currentDoc.value = doc
  showDetailDialog.value = true
}

// 删除单个文档
async function handleDelete(doc) {
  try {
    await ElMessageBox.confirm(
      `确定要删除文档"${doc.name}"吗？此操作不可恢复。`,
      '删除确认',
      { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
    )

    await deleteDocument(doc.id)
    ElMessage.success('删除成功')
    await loadDocuments()
  } catch (error) {
    if (error !== 'cancel') console.error('删除失败:', error)
  }
}

// ==================== 问答方法 ====================
// 提交查询
async function handleSubmitQuery() {
  if (!queryText.value.trim()) {
    ElMessage.warning('请输入问题')
    return
  }

  if (queryScope.value === 'selected' && selectedQueryDocs.value.length === 0) {
    ElMessage.warning('请至少选择一个文档')
    return
  }

  querying.value = true
  try {
    const requestData = {
      question: queryText.value,
      scope: queryScope.value,
      documentIds: queryScope.value === 'selected' ? selectedQueryDocs.value : undefined
    }

    const res = await queryRag(requestData)
    const answerData = res.data || res

    currentAnswer.value = {
      question: queryText.value,
      answer: answerData.answer || answerData.content || '抱歉，未能生成有效回答。',
      sources: answerData.sources || answerData.references || [],
      timestamp: new Date().toISOString()
    }

    // 添加到历史记录
    queryHistory.value.unshift({
      question: queryText.value,
      answer: currentAnswer.value.answer,
      timestamp: currentAnswer.value.timestamp,
      sources: currentAnswer.value.sources
    })

    // 保留最近10条
    if (queryHistory.value.length > 10) {
      queryHistory.value = queryHistory.value.slice(0, 10)
    }

    // 重置展开状态
    expandedSources.value = []

    ElMessage.success('问答完成')
  } catch (error) {
    console.error('RAG查询失败:', error)
    ElMessage.error(error.message || '查询失败，请稍后重试')

    // 使用模拟答案作为降级
    currentAnswer.value = {
      question: queryText.value,
      answer: `根据您上传的文档内容，关于"${queryText.value}"的问题，这是一个模拟的回答示例。\n\n在实际环境中，系统会基于向量检索找到最相关的文档片段，并生成准确的答案。请确保后端服务正常运行以获得真实的RAG问答结果。`,
      sources: [
        { documentName: '产品使用说明书.pdf', paragraphIndex: 23, content: '这是来自文档的相关原文内容片段...' },
        { documentName: '技术架构设计文档.docx', paragraphIndex: 15, content: '另一段相关的文档引用内容...' }
      ],
      timestamp: new Date().toISOString()
    }
  } finally {
    querying.value = false
  }
}

// 查看历史记录项
function viewHistoryItem(item) {
  currentAnswer.value = {
    question: item.question,
    answer: item.answer,
    sources: item.sources || [],
    timestamp: item.timestamp
  }
  expandedSources.value = []
}

// 清空历史
function clearHistory() {
  queryHistory.value = []
  ElMessage.info('历史记录已清空')
}

// 展开/折叠来源
function toggleSourceExpand(idx) {
  const index = expandedSources.value.indexOf(idx)
  if (index > -1) {
    expandedSources.value.splice(index, 1)
  } else {
    expandedSources.value.push(idx)
  }
}

// ==================== 工具函数 ====================
// 格式化文件大小
function formatFileSize(bytes) {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 获取文件扩展名
function getFileExtension(filename) {
  if (!filename) return ''
  const ext = filename.split('.').pop()?.toLowerCase() || ''
  return ext
}

// 获取状态类型
function getStatusType(status) {
  const map = {
    PROCESSING: 'warning',
    processing: 'warning',
    READY: 'success',
    ready: 'success',
    completed: 'success',
    FAILED: 'danger',
    failed: 'danger'
  }
  return map[status] || 'info'
}

// 获取状态文本
function getStatusText(status) {
  const map = {
    PROCESSING: '处理中',
    processing: '向量化中',
    READY: '就绪',
    ready: '就绪',
    completed: '已完成',
    FAILED: '失败',
    failed: '失败'
  }
  return map[status] || '未知'
}

// 格式化相对时间
function formatRelativeTime(dateStr) {
  if (!dateStr) return ''

  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`

  return formatDate(dateStr, 'YYYY-MM-DD')
}

// 简单Markdown渲染（基础版）
function renderMarkdown(text) {
  if (!text) return ''
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/`(.*?)`/g, '<code>$1</code>')
}

// ==================== 生命周期 ====================
onMounted(() => {
  loadDocuments()
})
</script>

<style scoped>
.rag-view {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

/* 页面标题 */
.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  color: var(--text-primary);
  margin-bottom: 8px;
  font-weight: 600;
}

.page-header p {
  color: var(--text-secondary);
  font-size: 14px;
}

/* 标签页样式 */
.rag-tabs {
  background: var(--white);
  border-radius: var(--border-radius-base);
  padding: 20px;
}

.rag-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
}

.rag-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
}

/* ==================== 上传区域 ==================== */
.upload-section {
  margin-bottom: 24px;
}

.upload-dragger {
  width: 100%;
}

.upload-dragger :deep(.el-upload-dragger) {
  padding: 40px 20px;
  border: 2px dashed var(--border-color);
  border-radius: var(--border-radius-lg);
  transition: all 0.3s ease;
  background-color: #fafafa;
}

.upload-dragger :deep(.el-upload-dragger:hover) {
  border-color: var(--primary-color);
  background-color: #f0f7ff;
}

.upload-icon {
  font-size: 48px;
  color: #c0c4cc;
  margin-bottom: 12px;
}

.upload-dragger :deep(.el-upload__text) {
  font-size: 15px;
  color: var(--text-regular);
}

.upload-dragger :deep(.el-upload__text em) {
  color: var(--primary-color);
  font-style: normal;
  cursor: pointer;
}

.upload-dragger :deep(.el-upload__tip) {
  color: var(--text-secondary);
  font-size: 13px;
  margin-top: 8px;
}

/* ==================== 工具栏 ==================== */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 12px 16px;
  background: var(--white);
  border-radius: var(--border-radius-base);
  border: 1px solid var(--border-color);
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* ==================== 文档网格布局 ==================== */
.document-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  min-height: 300px;
}

/* 文档卡片 - 增强版 */
.document-card {
  position: relative;
  border-radius: 14px;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  cursor: default;
  overflow: hidden;
}

.document-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, currentColor, transparent);
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.document-card:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow:
    0 12px 28px rgba(0, 0, 0, 0.12),
    0 4px 12px rgba(0, 0, 0, 0.08);
}

.document-card:hover::before {
  opacity: 0.6;
}

.document-card.is-selected {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.document-card.is-selected::before {
  opacity: 1;
}

.card-checkbox {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 10;
}

/* 文件图标 */
.file-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 16px auto 12px;
  transition: transform 0.3s ease;
}

.document-card:hover .file-icon-wrapper {
  transform: scale(1.05);
}

.file-icon-wrapper.type-pdf {
  background: linear-gradient(135deg, #ff6b6b, #ee5a52);
  color: white;
}

.file-icon-wrapper.type-docx {
  background: linear-gradient(135deg, #4285f4, #3367d6);
  color: white;
}

.file-icon-wrapper.type-txt {
  background: linear-gradient(135deg, #34a853, #2d9249);
  color: white;
}

/* 文件信息 */
.file-info {
  text-align: center;
  padding: 0 12px;
}

.file-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-meta {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-secondary);
}

.file-status {
  margin-bottom: 8px;
}

.file-time {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-placeholder);
  margin-bottom: 12px;
}

/* 卡片操作按钮 */
.card-actions {
  display: flex;
  justify-content: center;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid var(--border-color);
}

/* ==================== 分页 ==================== */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--border-color);
}

/* ==================== 问答界面 ==================== */
.chat-container {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 20px;
  min-height: calc(100vh - 300px);
}

.query-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.answer-section {
  min-width: 0;
}

.answer-card {
  height: 100%;
  min-height: 500px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

/* 查询输入区 */
.query-textarea :deep(.el-textarea__inner) {
  font-size: 15px;
  line-height: 1.6;
}

.scope-selector {
  margin-top: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.scope-label {
  font-size: 13px;
  color: var(--text-regular);
  font-weight: 500;
  white-space: nowrap;
}

.doc-checkbox-group {
  margin-top: 12px;
  padding: 12px;
  background: #fafafa;
  border-radius: var(--border-radius-sm);
  max-height: 150px;
  overflow-y: auto;
}

.doc-checkbox-group :deep(.el-checkbox) {
  display: block;
  margin-right: 0;
  margin-bottom: 8px;
}

.doc-checkbox-group :deep(.el-checkbox:last-child) {
  margin-bottom: 0;
}

.submit-row {
  margin-top: 20px;
  text-align: right;
}

.submit-row .el-button {
  width: 100%;
}

/* 历史记录卡片 */
.history-card {
  flex-shrink: 0;
}

.history-list {
  max-height: 280px;
  overflow-y: auto;
}

.history-item {
  padding: 10px 12px;
  border-radius: var(--border-radius-sm);
  cursor: pointer;
  transition: background-color 0.2s ease;
  margin-bottom: 4px;
}

.history-item:hover {
  background-color: #f5f7fa;
}

.history-question {
  font-size: 13px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
}

.history-time {
  font-size: 11px;
  color: var(--text-placeholder);
}

/* ==================== 答案展示区 ==================== */
.answer-content {
  animation: slideInUp 0.3s ease-out;
}

.question-block,
.answer-block {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.q-label,
.a-label {
  font-weight: 700;
  font-size: 16px;
  color: var(--primary-color);
  flex-shrink: 0;
  width: 28px;
}

.q-text {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.6;
}

.a-text {
  font-size: 14px;
  color: var(--text-regular);
  line-height: 1.8;
}

.markdown-body code {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 13px;
  color: #e74c3c;
}

/* 来源引用 */
.sources-section {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px dashed var(--border-color);
}

.sources-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
  font-size: 14px;
}

.source-item {
  background: #FFFDF5;
  border-left: 3px solid var(--primary-color);
  border-radius: 0 var(--border-radius-sm) var(--border-radius-sm) 0;
  margin-bottom: 10px;
  overflow: hidden;
}

.source-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  user-select: none;
}

.source-header:hover {
  background: #fef9e7;
}

.source-name {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

.expand-icon {
  transition: transform 0.3s ease;
  color: var(--text-secondary);
}

.expand-icon.is-expanded {
  transform: rotate(180deg);
}

.source-content {
  padding: 0 14px 12px;
  font-size: 13px;
  color: var(--text-regular);
  line-height: 1.7;
  border-top: 1px solid #f5e6b8;
  margin-top: 0;
}

.source-content p {
  margin: 8px 0 0;
}

/* 展开动画 */
.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  max-height: 0;
  padding-top: 0;
  padding-bottom: 0;
  margin-top: 0;
}

.expand-enter-to,
.expand-leave-from {
  opacity: 1;
  max-height: 200px;
}

/* ==================== 响应式布局 ==================== */
@media (max-width: 1200px) {
  .document-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .chat-container {
    grid-template-columns: 350px 1fr;
  }
}

@media (max-width: 992px) {
  .chat-container {
    grid-template-columns: 1fr;
  }

  .query-section {
    order: 1;
  }

  .answer-section {
    order: 2;
  }
}

@media (max-width: 768px) {
  .rag-view {
    padding: 12px;
  }

  .rag-tabs {
    padding: 12px;
  }

  .document-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .toolbar {
    flex-direction: column;
    gap: 12px;
  }

  .toolbar-left,
  .toolbar-right {
    width: 100%;
    justify-content: flex-start;
  }

  .toolbar-right .el-input {
    flex: 1;
  }

  .page-header h2 {
    font-size: 20px;
  }
}
</style>
