<template>
  <div class="memory-management">
    <!-- 顶部操作栏 -->
    <div class="top-action-bar">
      <div class="bar-left">
        <h2 class="page-title">记忆管理</h2>
        <p class="page-subtitle">管理和搜索 AI 的长期记忆数据，提升对话连贯性</p>
      </div>
      <div class="bar-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索会话内容或关键词..."
          clearable
          class="search-input"
          :prefix-icon="Search"
          @keyup.enter="handleSearch"
          @clear="handleClearSearch"
        />
        <el-button type="danger" @click="handleClearAll" :loading="clearing">
          <el-icon><Delete /></el-icon>
          清空所有记忆
        </el-button>
      </div>
    </div>

    <!-- 统计卡片区域 -->
    <div class="stats-cards">
      <div class="stat-card stat-card-blue">
        <div class="stat-icon">
          <el-icon :size="32"><ChatDotRound /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.totalSessions }}</div>
          <div class="stat-label">总对话数</div>
        </div>
      </div>
      <div class="stat-card stat-card-green">
        <div class="stat-icon">
          <el-icon :size="32"><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.totalMessages }}</div>
          <div class="stat-label">总消息数</div>
        </div>
      </div>
      <div class="stat-card stat-card-purple">
        <div class="stat-icon">
          <el-icon :size="32"><Search /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ isSearching ? searchResults.length : '-' }}</div>
          <div class="stat-label">搜索结果</div>
        </div>
      </div>
    </div>

    <!-- 记忆列表区域 -->
    <div class="memory-list-container" v-loading="loading">
      <!-- 空状态 -->
      <div v-if="!loading && groupedMemories.length === 0" class="empty-state">
        <el-empty description="暂无记忆数据">
          <template #image>
            <el-icon :size="80" color="#dcdfe6"><FolderOpened /></el-icon>
          </template>
        </el-empty>
      </div>

      <!-- 按日期分组的记忆列表 -->
      <div v-else class="memory-groups">
        <div v-for="group in groupedMemories" :key="group.date" class="memory-group">
          <div class="group-header">
            <span class="group-date">{{ group.dateLabel }}</span>
            <span class="group-count">{{ group.sessions.length }} 个会话</span>
          </div>

          <div class="memory-cards">
            <div
              v-for="session in group.sessions"
              :key="session.id"
              class="memory-card"
              @click="handleViewSession(session)"
            >
              <div class="card-header">
                <h3 class="card-title">{{ session.title || '未命名会话' }}</h3>
                <div class="card-actions">
                  <el-button link type="primary" size="small" @click.stop="handleViewSession(session)">
                    查看详情
                  </el-button>
                  <el-button link type="danger" size="small" @click.stop="handleDeleteSession(session)">
                    删除
                  </el-button>
                </div>
              </div>

              <div class="card-content">
                <p class="card-preview">{{ truncateText(session.lastMessage || '暂无消息', 120) }}</p>
              </div>

              <div class="card-footer">
                <div class="card-meta">
                  <span class="meta-item">
                    <el-icon><ChatLineSquare /></el-icon>
                    {{ session.messageCount || 0 }} 条消息
                  </span>
                  <span class="meta-item">
                    <el-icon><Clock /></el-icon>
                    {{ formatRelativeTime(session.updatedAt || session.createdAt) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 记忆详情抽屉（消息时间线） -->
    <el-drawer
      v-model="showDetailDrawer"
      :title="currentSession?.title || '会话详情'"
      direction="rtl"
      size="50%"
      :destroy-on-close="true"
    >
      <div class="drawer-content" v-loading="detailLoading">
        <!-- 会话信息 -->
        <div class="session-info">
          <div class="info-row">
            <span class="info-label">会话ID：</span>
            <span class="info-value">{{ currentSession?.id }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">创建时间：</span>
            <span class="info-value">{{ formatDateTime(currentSession?.createdAt) }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">最后更新：</span>
            <span class="info-value">{{ formatDateTime(currentSession?.updatedAt) }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">消息数量：</span>
            <span class="info-value">{{ sessionMessages.length }} 条</span>
          </div>
        </div>

        <!-- 消息时间线 -->
        <div class="timeline-section">
          <h4 class="timeline-title">对话时间线</h4>

          <div v-if="sessionMessages.length === 0" class="empty-timeline">
            <el-empty description="暂无消息记录" />
          </div>

          <el-timeline v-else>
            <el-timeline-item
              v-for="(message, index) in sessionMessages"
              :key="index"
              :timestamp="formatDateTime(message.timestamp)"
              placement="top"
              :type="message.role === 'user' ? 'primary' : 'success'"
              :hollow="message.role === 'assistant'"
            >
              <div class="timeline-message" :class="`message-${message.role}`">
                <div class="message-role">
                  <el-tag :type="message.role === 'user' ? '' : 'success'" size="small">
                    {{ message.role === 'user' ? '用户' : 'AI助手' }}
                  </el-tag>
                </div>
                <div class="message-content" v-html="renderMarkdown(message.content)"></div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Delete,
  ChatDotRound,
  Document,
  FolderOpened,
  ChatLineSquare,
  Clock
} from '@element-plus/icons-vue'
import { getMemoryList, getSessionDetail, deleteSessionMemory, searchMemory, clearAllMemory } from '@/api/memory'
import { formatDate, formatRelativeTime, debounce } from '@/utils/index'

// 响应式数据
const loading = ref(false)
const clearing = ref(false)
const detailLoading = ref(false)
const showDetailDrawer = ref(false)
const searchKeyword = ref('')
const isSearching = ref(false)

// 数据列表
const memoryList = ref([])
const searchResults = ref([])
const currentSession = ref(null)
const sessionMessages = ref([])

// 统计数据
const stats = ref({
  totalSessions: 0,
  totalMessages: 0
})

// 按日期分组后的记忆列表
const groupedMemories = computed(() => {
  const list = isSearching.value ? searchResults.value : memoryList.value
  const groups = {}

  list.forEach(session => {
    const date = new Date(session.updatedAt || session.createdAt).toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    })

    if (!groups[date]) {
      groups[date] = {
        date: date,
        dateLabel: getRelativeDateLabel(date),
        sessions: []
      }
    }

    groups[date].sessions.push(session)
  })

  return Object.values(groups).sort((a, b) => new Date(b.date) - new Date(a.date))
})

// 获取相对日期标签
function getRelativeDateLabel(dateStr) {
  const today = new Date()
  const targetDate = new Date(dateStr)
  const diffTime = today - targetDate
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))

  if (diffDays === 0) return '今天'
  if (diffDays === 1) return '昨天'
  if (diffDays === 2) return '前天'
  if (diffDays < 7) return `${diffDays}天前`
  return dateStr
}

// 格式化日期时间
function formatDateTime(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 截断文本
function truncateText(text, maxLength) {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

// 简单的 Markdown 渲染（仅支持基本格式）
function renderMarkdown(content) {
  if (!content) return ''
  return content
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/`(.*?)`/g, '<code>$1</code>')
    .replace(/\n/g, '<br/>')
}

// 获取记忆列表
async function fetchMemories() {
  loading.value = true
  try {
    const res = await getMemoryList({})
    memoryList.value = res.data || res.list || []

    // 计算统计数据
    stats.value.totalSessions = memoryList.value.length
    stats.value.totalMessages = memoryList.value.reduce((sum, session) => {
      return sum + (session.messageCount || 0)
    }, 0)
  } catch (error) {
    console.error('获取记忆列表失败:', error)
    ElMessage.error('获取记忆列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索功能（带防抖）
const debouncedSearch = debounce(async () => {
  if (!searchKeyword.value.trim()) {
    isSearching.value = false
    searchResults.value = []
    return
  }

  loading.value = true
  try {
    const res = await searchMemory({ keyword: searchKeyword.value.trim() })
    searchResults.value = res.data || res.list || []
    isSearching.value = true
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}, 500)

// 执行搜索
function handleSearch() {
  debouncedSearch()
}

// 清除搜索
function handleClearSearch() {
  searchKeyword.value = ''
  isSearching.value = false
  searchResults.value = []
}

// 查看会话详情
async function handleViewSession(session) {
  currentSession.value = session
  showDetailDrawer.value = true
  detailLoading.value = true

  try {
    const res = await getSessionDetail(session.id)
    sessionMessages.value = res.data?.messages || res.messages || []
  } catch (error) {
    console.error('获取会话详情失败:', error)
    ElMessage.error('获取会话详情失败')
    sessionMessages.value = []
  } finally {
    detailLoading.value = false
  }
}

// 删除会话
async function handleDeleteSession(session) {
  try {
    await ElMessageBox.confirm(
      `确定要删除会话"${session.title || '未命名会话'}"吗？此操作将永久删除该会话的所有记忆数据。`,
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteSessionMemory(session.id)
    memoryList.value = memoryList.value.filter(s => s.id !== session.id)

    // 更新统计
    stats.value.totalSessions--
    stats.value.totalMessages -= (session.messageCount || 0)

    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 清空所有记忆
async function handleClearAll() {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有记忆数据吗？此操作将永久删除所有会话记录和消息历史，且不可恢复！',
      '危险操作警告',
      {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'error',
        confirmButtonClass: 'el-button--danger'
      }
    )

    clearing.value = true
    await clearAllMemory()

    // 重置所有数据
    memoryList.value = []
    searchResults.value = []
    stats.value.totalSessions = 0
    stats.value.totalMessages = 0
    isSearching.value = false

    ElMessage.warning('已清空所有记忆数据')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空失败:', error)
      ElMessage.error('清空失败')
    }
  } finally {
    clearing.value = false
  }
}

// 初始化加载
onMounted(() => {
  fetchMemories()
})
</script>

<style scoped>
.memory-management {
  padding: 24px;
  min-height: calc(100vh - 84px);
  background-color: #f5f7fa;
}

/* 顶部操作栏 */
.top-action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.bar-left {
  color: white;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.page-subtitle {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

.bar-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  width: 300px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 统计卡片区域 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.stat-card-blue {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-card-green {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: white;
}

.stat-card-purple {
  background: linear-gradient(135deg, #ee0979 0%, #ff6a00 100%);
  color: white;
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  margin-right: 16px;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

/* 记忆列表容器 */
.memory-list-container {
  min-height: 400px;
}

/* 空状态 */
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* 记忆分组 */
.memory-groups {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.memory-group {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: linear-gradient(to right, #f8f9fa, #ffffff);
  border-bottom: 1px solid #ebeef5;
}

.group-date {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.group-count {
  font-size: 13px;
  color: #909399;
}

/* 记忆卡片网格 */
.memory-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 16px;
  padding: 20px;
}

/* 记忆卡片 - 增强版 */
.memory-card {
  background: white;
  border: 1px solid #ebeef5;
  border-radius: 14px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  overflow: hidden;
}

.memory-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.memory-card:hover {
  border-color: #667eea;
  box-shadow:
    0 8px 24px rgba(102, 126, 234, 0.18),
    0 4px 12px rgba(0, 0, 0, 0.06);
  transform: translateY(-4px) scale(1.01);
}

.memory-card:hover::before {
  opacity: 1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.card-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  flex: 1;
  margin-right: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.memory-card:hover .card-actions {
  opacity: 1;
}

.card-content {
  margin-bottom: 16px;
}

.card-preview {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  padding-top: 12px;
  border-top: 1px solid #f0f2f5;
}

.card-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #909399;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 抽屉样式 */
.drawer-content {
  padding: 0 20px;
}

.session-info {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 24px;
}

.info-row {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-label {
  color: #909399;
  width: 80px;
  flex-shrink: 0;
}

.info-value {
  color: #303133;
  word-break: break-all;
}

.timeline-section {
  margin-top: 24px;
}

.timeline-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid #667eea;
}

.empty-timeline {
  text-align: center;
  padding: 40px 0;
}

.timeline-message {
  background: white;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  margin-top: 8px;
}

.message-user {
  border-left: 3px solid #409eff;
}

.message-assistant {
  border-left: 3px solid #67c23a;
}

.message-role {
  margin-bottom: 8px;
}

.message-content {
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
  word-break: break-word;
}

.message-content :deep(code) {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
  color: #e74c3c;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }

  .memory-cards {
    grid-template-columns: 1fr;
  }

  .top-action-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .bar-right {
    width: 100%;
    flex-direction: column;
  }

  .search-input {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .memory-management {
    padding: 16px;
  }

  .page-title {
    font-size: 20px;
  }

  .stat-number {
    font-size: 24px;
  }
}
</style>
