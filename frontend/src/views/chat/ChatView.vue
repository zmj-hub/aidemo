<template>
  <div class="chat-container">
    <!-- 左侧会话列表 -->
    <aside class="session-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <!-- 侧边栏头部 -->
      <div class="sidebar-header">
        <el-button type="primary" class="new-session-btn" @click="handleNewSession">
          <el-icon><Plus /></el-icon>
          <span>新建会话</span>
        </el-button>
      </div>

      <!-- 搜索框 -->
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索会话..."
          :prefix-icon="Search"
          clearable
        />
      </div>

      <!-- 会话列表 -->
      <div class="session-list" ref="sessionListRef">
        <!-- 空状态 -->
        <div v-if="displayedSessions.length === 0" class="empty-sessions">
          <el-icon :size="48"><ChatDotRound /></el-icon>
          <h3 v-if="!sessionStore.loading">开始您的第一次AI对话</h3>
          <p v-if="!sessionStore.loading">点击上方"+"按钮创建新会话</p>
          <p v-else>加载中...</p>
        </div>

        <!-- 活跃会话分组 -->
        <template v-if="filteredActiveSessions.length > 0">
          <div class="session-group">
            <div class="group-header">
              <span class="group-title">活跃会话</span>
              <span class="group-count">{{ filteredActiveSessions.length }}</span>
            </div>

            <transition-group name="session-list" tag="div" class="group-content">
              <el-dropdown
                v-for="session in filteredActiveSessions"
                :key="session.id"
                trigger="contextmenu"
                @command="(cmd) => handleSessionCommand(cmd, session)"
              >
                <div
                  class="session-item"
                  :class="{ active: session.id === sessionStore.currentSessionId }"
                  @click="handleSelectSession(session)"
                >
                  <el-icon class="session-icon"><ChatDotRound /></el-icon>
                  <div class="session-info">
                    <div
                      class="session-name"
                      v-if="editingSessionId !== session.id"
                      @dblclick="startEditSession(session)"
                    >
                      {{ session.name || session.title || '新会话' }}
                    </div>
                    <el-input
                      v-else
                      v-model="editingName"
                      size="small"
                      @blur="finishEditSession(session)"
                      @keyup.enter="finishEditSession(session)"
                      ref="editInputRef"
                      class="edit-input"
                    />
                    <div class="session-time">{{ formatSessionTime(session.updateTime) }}</div>
                  </div>
                  <el-icon
                    class="delete-btn"
                    @click.stop="handleDeleteSession(session.id)"
                  >
                    <Delete />
                  </el-icon>
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="rename" :icon="Edit">
                      重命名
                    </el-dropdown-item>
                    <el-dropdown-item command="archive" :icon="FolderOpened">
                      归档会话
                    </el-dropdown-item>
                    <el-dropdown-item
                      command="delete"
                      :icon="Delete"
                      divided
                      style="color: #F56C6C;"
                    >
                      删除会话
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </transition-group>
          </div>
        </template>

        <!-- 已归档会话分组 -->
        <template v-if="sessionStore.showArchived && filteredArchivedSessions.length > 0">
          <div class="session-group archived-group">
            <div class="group-header">
              <span class="group-title"><el-icon><Folder /></el-icon> 已归档</span>
              <span class="group-count">{{ filteredArchivedSessions.length }}</span>
            </div>

            <transition-group name="session-list" tag="div" class="group-content">
              <el-dropdown
                v-for="session in filteredArchivedSessions"
                :key="'archived-' + session.id"
                trigger="contextmenu"
                @command="(cmd) => handleSessionCommand(cmd, session)"
              >
                <div
                  class="session-item archived"
                  @click="handleSelectArchivedSession(session)"
                >
                  <el-icon class="session-icon"><FolderOpened /></el-icon>
                  <div class="session-info">
                    <div class="session-name archived-name">
                      {{ session.name || session.title || '已归档会话' }}
                    </div>
                    <div class="session-time">
                      {{ formatSessionTime(session.archivedAt || session.updateTime) }}
                    </div>
                  </div>
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="unarchive" :icon="Folder">
                      恢复会话
                    </el-dropdown-item>
                    <el-dropdown-item
                      command="delete"
                      :icon="Delete"
                      divided
                      style="color: #F56C6C;"
                    >
                      删除会话
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </transition-group>
          </div>
        </template>
      </div>

      <!-- 底部工具栏 -->
      <div class="sidebar-footer">
        <div class="archive-toggle">
          <span class="toggle-label">显示已归档</span>
          <el-switch
            v-model="sessionStore.showArchived"
            size="small"
            @change="handleToggleArchived"
          />
        </div>
      </div>
    </aside>

    <!-- 右侧聊天主区域 -->
    <main class="chat-main">
      <!-- 顶部工具栏 -->
      <header class="chat-header">
        <div class="header-left">
          <el-button
            class="collapse-btn mobile-only"
            :icon="Fold"
            circle
            @click="toggleSidebar"
          />
          <h2
            v-if="!editingTitle"
            class="session-title"
            @dblclick="startEditTitle"
          >
            {{ currentSession?.name || currentSession?.title || 'AI 助手' }}
          </h2>
          <el-input
            v-else
            v-model="editingTitleValue"
            size="small"
            class="title-input"
            @blur="finishEditTitle"
            @keyup.enter="finishEditTitle"
            ref="titleInputRef"
          />
        </div>

        <div class="header-right">
          <el-select
            v-model="selectedModel"
            placeholder="选择模型"
            class="model-select"
            size="default"
          >
            <el-option
              v-for="model in modelList"
              :key="model.code"
              :label="model.name"
              :value="model.code"
            />
          </el-select>

          <el-dropdown trigger="click" @command="handleCommand">
            <el-button :icon="Setting" circle />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="clear">清除对话</el-dropdown-item>
                <el-dropdown-item command="export">导出对话</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 消息列表区域 -->
      <div class="message-container" ref="messageContainerRef" @scroll="handleScroll">
        <!-- 空状态 -->
        <div v-if="currentMessages.length === 0 && !isLoading" class="empty-state">
          <div class="welcome-content">
            <el-icon :size="64" color="#409EFF"><ChatDotRound /></el-icon>
            <h3>欢迎使用 AI 助手</h3>
            <p>我可以帮助您回答问题、编写代码、分析数据等</p>
            <div class="quick-questions">
              <div
                v-for="(q, index) in quickQuestions"
                :key="index"
                class="question-card"
                @click="sendQuickQuestion(q)"
              >
                {{ q }}
              </div>
            </div>
          </div>
        </div>

        <!-- 消息列表 -->
        <div v-else class="messages-wrapper">
          <div
            v-for="msg in currentMessages"
            :key="msg.id"
            class="message-item"
            :class="[msg.role, { error: msg.error }]"
          >
            <!-- 用户消息 -->
            <template v-if="msg.role === 'user'">
              <div class="message-content user-content">
                <div class="bubble user-bubble" v-html="escapeHtml(msg.content)"></div>
                <div class="avatar user-avatar">
                  {{ getUserInitial() }}
                </div>
              </div>
              <div class="message-time">{{ formatMessageTime(msg.timestamp) }}</div>
            </template>

            <!-- AI消息 -->
            <template v-else>
              <div class="message-content assistant-content">
                <div class="avatar ai-avatar">
                  <el-icon><Aim /></el-icon>
                </div>
                <div class="bubble ai-bubble">
                  <!-- 加载动画 -->
                  <div v-if="msg.loading && !msg.content" class="loading-animation">
                    <span class="dot"></span>
                    <span class="dot"></span>
                    <span class="dot"></span>
                  </div>
                  <!-- Markdown内容 -->
                  <div v-else class="markdown-body" v-html="renderMarkdown(msg.content)"></div>
                  <!-- 流式输出光标 -->
                  <span v-if="msg.streaming" class="cursor-blink">|</span>
                </div>
              </div>
              <div class="message-time">{{ formatMessageTime(msg.timestamp) }}</div>
            </template>
          </div>
        </div>
      </div>

      <!-- 输入框区域 -->
      <footer class="input-area">
        <div class="input-wrapper">
          <div class="input-main">
            <textarea
              ref="textareaRef"
              v-model="inputMessage"
              class="message-input"
              placeholder="输入消息...（Enter 发送，Shift+Enter 换行）"
              rows="1"
              :disabled="isStreaming"
              @input="autoResize"
              @keydown.enter.exact.prevent="sendMessage"
              @keydown.enter.shift.exact="allowNewline"
            ></textarea>
          </div>

          <div class="input-actions">
            <div class="actions-left">
              <el-tooltip content="附件（即将推出）" placement="top">
                <el-button :icon="Paperclip" circle :disabled="true" size="small" />
              </el-tooltip>
            </div>

            <div class="actions-right">
              <span class="char-count" v-if="inputMessage.length > 0">
                {{ inputMessage.length }} / 4000
              </span>
              <el-button
                type="primary"
                :icon="Promotion"
                :loading="isStreaming"
                :disabled="!inputMessage.trim() || isStreaming || inputMessage.length > 4000"
                @click="sendMessage"
                class="send-btn"
              >
                发送
              </el-button>
              <el-button
                v-if="isStreaming"
                type="danger"
                :icon="VideoPause"
                @click="stopGeneration"
                class="stop-btn"
              >
                停止
              </el-button>
            </div>
          </div>
        </div>
      </footer>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted, watch } from 'vue'
import { useSessionStore } from '@/stores/session'
import { useChatStore } from '@/stores/chat'
import { getModelList } from '@/api/model'
import { streamChat, sendMessage as sendSyncMessage } from '@/api/agent'
import {
  Plus,
  ChatDotRound,
  Delete,
  Edit,
  Fold,
  Setting,
  Aim,
  Promotion,
  VideoPause,
  Paperclip,
  Search,
  FolderOpened,
  Folder
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// Markdown 配置
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/vs2015.css'

const md = new MarkdownIt({
  html: false,
  linkify: true,
  typographer: true,
  breaks: true,
  highlight(str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return `<pre class="code-block"><div class="code-header"><span class="code-lang">${lang}</span><button class="copy-btn" onclick="copyCode(this)">复制代码</button></div><code class="hljs language-${lang}">${hljs.highlight(str, { language: lang, ignoreIllegals: true }).value}</code></pre>`
      } catch (_) {}
    }
    return `<pre class="code-block"><code class="hljs">${md.utils.escapeHtml(str)}</code></pre>`
  }
})

// Store
const sessionStore = useSessionStore()
const chatStore = useChatStore()

// Refs
const messageContainerRef = ref(null)
const textareaRef = ref(null)
const sessionListRef = ref(null)
const editInputRef = ref(null)
const titleInputRef = ref(null)

// 状态
const inputMessage = ref('')
const searchKeyword = ref('')
const selectedModel = ref('')
const modelList = ref([])
const sidebarCollapsed = ref(false)
const editingSessionId = ref(null)
const editingName = ref('')
const editingTitle = ref(false)
const editingTitleValue = ref('')

// 快捷问题建议
const quickQuestions = ref([
  '帮我写一段 Python 代码',
  '解释一下机器学习的基本概念',
  '如何优化数据库查询性能？',
  '帮我总结这篇文章的主要内容'
])

// 计算属性 - 过滤后的活跃会话
const filteredActiveSessions = computed(() => {
  let sessions = sessionStore.activeSessions
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    sessions = sessions.filter(s =>
      (s.name || s.title || '').toLowerCase().includes(keyword)
    )
  }
  return sessions
})

// 计算属性 - 过滤后的归档会话
const filteredArchivedSessions = computed(() => {
  let sessions = sessionStore.archivedSessions
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    sessions = sessions.filter(s =>
      (s.name || s.title || '').toLowerCase().includes(keyword)
    )
  }
  return sessions
})

// 计算属性 - 显示的会话（用于判断空状态）
const displayedSessions = computed(() => {
  const sessions = [...filteredActiveSessions.value]
  if (sessionStore.showArchived) {
    sessions.push(...filteredArchivedSessions.value)
  }
  return sessions
})

const currentSession = computed(() => sessionStore.currentSession)

const currentMessages = computed(() => {
  const sessionId = sessionStore.currentSessionId
  return sessionId ? chatStore.getMessages(sessionId) : []
})

const isLoading = computed(() => {
  const sessionId = sessionStore.currentSessionId
  return sessionId ? chatStore.isLoading(sessionId) : false
})

const isStreaming = computed(() => {
  const sessionId = sessionStore.currentSessionId
  return sessionId ? chatStore.isStreaming(sessionId) : false
})

// 初始化
onMounted(async () => {
  await loadModelList()
  await loadSessions()

  // 全局点击关闭右键菜单（如果需要）
  document.addEventListener('click', () => {})

  // 复制代码函数挂载到 window
  window.copyCode = function(btn) {
    const codeBlock = btn.closest('.code-block')
    const code = codeBlock.querySelector('code').textContent
    navigator.clipboard.writeText(code).then(() => {
      ElMessage.success('代码已复制到剪贴板')
      btn.textContent = '已复制'
      setTimeout(() => {
        btn.textContent = '复制代码'
      }, 2000)
    })
  }

  // 设置默认模型
  if (modelList.value.length > 0 && !selectedModel.value) {
    selectedModel.value = modelList.value[0].code
  }
})

onUnmounted(() => {
  delete window.copyCode
})

// 监听会话切换，清空消息显示欢迎界面
watch(() => sessionStore.currentSessionId, async (newSessionId) => {
  if (newSessionId) {
    // 切换会话时清空当前消息（后端无历史记录API，消息仅在前端内存维护）
    chatStore.clearMessages(newSessionId)
    scrollToBottom()
  }
})

// 方法
async function loadModelList() {
  try {
    const res = await getModelList()
    modelList.value = res.list || res.data || []
  } catch (error) {
    console.error('获取模型列表失败:', error)
  }
}

async function loadSessions() {
  try {
    await sessionStore.fetchSessions({ includeArchived: true })
  } catch (error) {
    console.error('获取会话列表失败:', error)
  }
}

function toggleSidebar() {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

// ========== 会话操作 ==========

/**
 * 创建新会话
 */
async function handleNewSession() {
  try {
    const newSession = await sessionStore.createNewSession({ title: '新会话' })
    ElMessage.success(`会话 "${newSession.name || newSession.title}" 已创建`)

    // 清空当前聊天区域（准备新对话）
    chatStore.clearMessages(newSession.id)
  } catch (error) {
    console.error('创建会话失败:', error)
    ElMessage.error('创建会话失败')
  }
}

/**
 * 选择会话（带验证）
 */
function handleSelectSession(session) {
  // 如果正在编辑其他会话，先完成编辑
  if (editingSessionId.value && editingSessionId.value !== session.id) {
    editingSessionId.value = null
    editingName.value = ''
  }

  const success = sessionStore.selectSession(session.id)
  if (!success) {
    ElMessage.warning('该会话不可用')
  }
}

/**
 * 点击归档会话时的处理
 */
function handleSelectArchivedSession(session) {
  ElMessage.info('该会话已归档，请先恢复后再查看')
}

/**
 * 右键菜单命令处理
 */
async function handleSessionCommand(command, session) {
  switch (command) {
    case 'rename':
      startEditSession(session)
      break

    case 'archive':
      try {
        await ElMessageBox.confirm(
          '确定要归档此会话吗？<br/><small style="color: #909399;">归档后可随时恢复</small>',
          '归档确认',
          {
            confirmButtonText: '确认归档',
            cancelButtonText: '取消',
            type: 'info'
          }
        )
        await sessionStore.archiveSession(session.id)
        ElMessage.success('会话已归档')
      } catch (e) {
        // 用户取消
      }
      break

    case 'unarchive':
      try {
        await sessionStore.unarchiveSession(session.id)
        ElMessage.success('会话已恢复')
      } catch (error) {
        console.error('恢复会话失败:', error)
        ElMessage.error('恢复失败')
      }
      break

    case 'delete':
      try {
        await ElMessageBox.confirm(
          '确定要删除此会话吗？<br/><span style="color:#F56C6C; font-weight: bold;">此操作不可撤销！</span>',
          '危险操作',
          {
            confirmButtonText: '确认删除',
            cancelButtonText: '取消',
            type: 'warning',
            confirmButtonClass: 'el-button--danger',
            dangerouslyUseHTMLString: true
          }
        )

        await sessionStore.removeSession(session.id)
        chatStore.deleteSessionMessages(session.id)
        ElMessage.success('会话已删除')
      } catch (e) {
        // 用户取消或错误
        if (e !== 'cancel') {
          console.error('删除会话失败:', e)
        }
      }
      break
  }
}

/**
 * 开始编辑会话名称
 */
function startEditSession(session) {
  editingSessionId.value = session.id
  editingName.value = session.name || session.title || ''
  nextTick(() => {
    editInputRef.value?.focus?.()
  })
}

/**
 * 完成编辑会话名称
 */
async function finishEditSession(session) {
  if (editingName.value.trim() && editingName.value !== (session.name || session.title)) {
    try {
      await sessionStore.rename(session.id, editingName.value.trim())
      ElMessage.success('重命名成功')
    } catch (error) {
      console.error('重命名失败:', error)
      ElMessage.error('重命名失败')
    }
  }
  editingSessionId.value = null
  editingName.value = ''
}

/**
 * 删除会话
 */
async function handleDeleteSession(sessionId) {
  try {
    await ElMessageBox.confirm(
      '确定要删除该会话吗？删除后无法恢复。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await sessionStore.removeSession(sessionId)
    chatStore.deleteSessionMessages(sessionId)

    // 如果删除的是当前会话，清空消息
    if (sessionStore.currentSessionId === sessionId) {
      chatStore.clearMessages()
    }

    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除会话失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

/**
 * 切换显示归档会话
 */
function handleToggleArchived(value) {
  // 可以在这里添加额外的逻辑，比如加载归档会话数据
  console.log('显示归档:', value)
}

// 标题编辑
function startEditTitle() {
  if (!currentSession.value) return
  editingTitle.value = true
  editingTitleValue.value = currentSession.value.name || currentSession.value.title || ''
  nextTick(() => {
    titleInputRef.value?.focus?.()
  })
}

async function finishEditTitle() {
  if (editingTitleValue.value.trim() && currentSession.value) {
    try {
      await sessionStore.updateSessionTitle(currentSession.value.id, editingTitleValue.value.trim())
      ElMessage.success('标题更新成功')
    } catch (error) {
      console.error('重命名失败:', error)
      ElMessage.error('重命名失败')
    }
  }
  editingTitle.value = false
}

// 工具栏命令
async function handleCommand(command) {
  const sessionId = sessionStore.currentSessionId
  switch (command) {
    case 'clear':
      if (sessionId) {
        try {
          await ElMessageBox.confirm(
            '确定要清空当前对话吗？',
            '确认清空',
            {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }
          )
          chatStore.clearMessages(sessionId)
          ElMessage.success('对话已清空')
        } catch (error) {
          if (error !== 'cancel') {
            console.error('清空对话失败:', error)
          }
        }
      }
      break
    case 'export':
      if (sessionId && currentMessages.value.length > 0) {
        exportConversation(sessionId)
      } else {
        ElMessage.warning('没有可导出的内容')
      }
      break
  }
}

function exportConversation(sessionId) {
  const messages = chatStore.getMessages(sessionId)
  const content = messages.map(msg => {
    const time = formatMessageTime(msg.timestamp)
    const role = msg.role === 'user' ? '用户' : 'AI助手'
    return `[${time}] ${role}:\n${msg.content}\n`
  }).join('\n---\n\n')

  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `conversation_${sessionId}_${Date.now()}.txt`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

// 发送消息 - SSE流式模式
async function sendMessage() {
  const content = inputMessage.value.trim()
  if (!content) return

  const modelCode = selectedModel.value || 'qwen-turbo'

  let sessionId = sessionStore.currentSessionId

  // 如果没有当前会话，先创建一个
  if (!sessionId) {
    try {
      const session = await sessionStore.createNewSession({ name: content.slice(0, 20) })
      sessionId = session.id
    } catch (error) {
      console.error('创建会话失败:', error)
      ElMessage.error('创建会话失败')
      return
    }
  }

  // 清空输入框
  inputMessage.value = ''
  resetTextareaHeight()

  // 添加用户消息
  chatStore.addMessage(sessionId, {
    role: 'user',
    content
  })

  // 创建AI消息占位符
  const aiMessageId = Date.now()
  chatStore.addMessage(sessionId, {
    id: aiMessageId,
    role: 'assistant',
    content: '',
    loading: true,
    streaming: true
  })

  // 设置状态
  chatStore.setLoading(sessionId, true)
  chatStore.setStreaming(sessionId, true)

  // 滚动到底部（强制）
  scrollToBottom(true)

  // 创建 AbortController 用于取消
  const abortController = new AbortController()
  chatStore.setAbortController(sessionId, abortController)

  try {
    // 使用 SSE 流式请求
    await streamChat(
      {
        message: content,
        sessionId,
        modelCode
      },
      // onMessage 回调
      (parsed) => {
        if (parsed.type === 'start') {
          // 流开始
          chatStore.updateMessage(aiMessageId, {
            loading: false,
            traceId: parsed.traceId
          })
        } else if (parsed.type === 'content') {
          // 追加内容（打字机效果）
          const msg = chatStore.getMessageById(aiMessageId)
          if (msg) {
            chatStore.updateMessage(aiMessageId, {
              content: msg.content + parsed.content
            })
            scrollToBottom()
          }
        } else if (parsed.type === 'end') {
          // 流结束
          chatStore.updateMessage(aiMessageId, {
            streaming: false
          })
        } else if (parsed.type === 'error') {
          // 错误处理
          const msg = chatStore.getMessageById(aiMessageId)
          if (msg) {
            chatStore.updateMessage(aiMessageId, {
              content: msg.content + '\n\n❌ 错误: ' + (parsed.error || '未知错误'),
              error: true,
              streaming: false
            })
          }
        }
      },
      // onError 回调
      (error) => {
        console.error('流式请求错误:', error)
        const msg = chatStore.getMessageById(aiMessageId)
        if (msg) {
          chatStore.updateMessage(aiMessageId, {
            content: msg.content || '请求失败，请检查网络连接',
            error: true,
            loading: false,
            streaming: false
          })
        }
      },
      // onDone 回调
      () => {
        chatStore.setStreaming(sessionId, false)
        chatStore.setLoading(sessionId, false)
        const msg = chatStore.getMessageById(aiMessageId)
        if (msg && !msg.error) {
          chatStore.updateMessage(aiMessageId, { streaming: false })
        }
      },
      // signal
      abortController.signal
    )
  } catch (error) {
    console.error('发送消息失败:', error)
    const msg = chatStore.getMessageById(aiMessageId)
    if (msg) {
      chatStore.updateMessage(aiMessageId, {
        content: msg.content || '请求失败，请检查网络连接',
        error: true,
        loading: false,
        streaming: false
      })
    }
    chatStore.setStreaming(sessionId, false)
    chatStore.setLoading(sessionId, false)
  }
}

// 同步模式备选方案
async function sendSyncMode(content) {
  const modelCode = selectedModel.value || 'qwen-turbo'
  let sessionId = sessionStore.currentSessionId
  if (!sessionId) {
    const session = await sessionStore.createNewSession({ name: content.slice(0, 20) })
    sessionId = session.id
  }

  chatStore.addMessage(sessionId, { role: 'user', content })

  const aiMessageId = Date.now()
  chatStore.addMessage(sessionId, {
    id: aiMessageId,
    role: 'assistant',
    content: '',
    loading: true
  })

  chatStore.setLoading(sessionId, true)

  try {
    const res = await sendSyncMessage({
      message: content,
      sessionId,
      modelCode
    })

    chatStore.updateMessage(aiMessageId, {
      content: res.data?.content || res.content || res.message || '',
      loading: false
    })
  } catch (error) {
    chatStore.updateMessage(aiMessageId, {
      content: '请求失败: ' + (error.message || '未知错误'),
      error: true,
      loading: false
    })
  } finally {
    chatStore.setLoading(sessionId, false)
  }

  scrollToBottom(true)
}

// 停止生成
async function stopGeneration() {
  const sessionId = sessionStore.currentSessionId
  if (sessionId) {
    try {
      await chatStore.stopGeneration(sessionId)

      // 更新当前正在生成的消息状态
      const messages = chatStore.getMessages(sessionId)
      const lastAiMsg = [...messages].reverse().find(m => m.role === 'assistant')
      if (lastAiMsg && lastAiMsg.streaming) {
        chatStore.updateMessage(lastAiMsg.id, {
          streaming: false,
          loading: false
        })
      }

      ElMessage.info('已停止生成')
    } catch (error) {
      console.error('停止生成失败:', error)
      ElMessage.error('停止生成失败')
    }
  }
}

// 快捷问题
function sendQuickQuestion(question) {
  inputMessage.value = question
  sendMessage()
}

// 输入框自动高度调整
function autoResize() {
  const textarea = textareaRef.value
  if (!textarea) return

  textarea.style.height = 'auto'
  const newHeight = Math.min(textarea.scrollHeight, 200)
  textarea.style.height = newHeight + 'px'
}

function resetTextareaHeight() {
  if (textareaRef.value) {
    textareaRef.value.height = 'auto'
  }
}

function allowNewline() {
}

// 滚动到底部
function scrollToBottom(force = false) {
  if (!force && !isAtBottom) return
  nextTick(() => {
    if (messageContainerRef.value) {
      messageContainerRef.value.scrollTo({
        top: messageContainerRef.value.scrollHeight,
        behavior: 'instant'
      })
    }
  })
}

// 滚动事件处理
let isAtBottom = true
function handleScroll() {
  if (!messageContainerRef.value) return
  const { scrollTop, scrollHeight, clientHeight } = messageContainerRef.value
  isAtBottom = scrollTop + clientHeight >= scrollHeight - 50
}

// 工具函数
function renderMarkdown(content) {
  if (!content) return ''
  return md.render(content)
}

function escapeHtml(text) {
  if (!text) return ''
  const div = document.createElement('div')
  div.textContent = text
  return div.innerHTML
}

function formatTime(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()

  if (isToday) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }

  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }

  return date.toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function formatSessionTime(timestamp) {
  return formatTime(timestamp)
}

function formatMessageTime(timestamp) {
  return formatTime(timestamp)
}

function getUserInitial() {
  // 可以从用户 store 获取用户名首字母
  return 'U'
}
</script>

<style scoped>
.chat-container {
  display: flex;
  height: calc(100vh - 84px);
  background: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
}

/* ===== 左侧会话列表 ===== */
.session-sidebar {
  width: 280px;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  position: relative;
}

.session-sidebar.collapsed {
  width: 0;
  overflow: hidden;
  border-right: none;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.new-session-btn {
  width: 100%;
  height: 40px;
  font-size: 14px;
  font-weight: 500;
}

.search-box {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.session-list::-webkit-scrollbar {
  width: 6px;
}

.session-list::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 3px;
}

/* 空状态 */
.empty-sessions {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: #909399;
  gap: 12px;
}

.empty-sessions h3 {
  font-size: 16px;
  font-weight: 500;
  color: #606266;
  margin: 8px 0 4px;
}

.empty-sessions p {
  font-size: 13px;
  color: #909399;
}

/* 会话分组 */
.session-group {
  margin-bottom: 12px;
}

.archived-group {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px 6px;
}

.group-title {
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.group-count {
  font-size: 11px;
  color: #c0c4cc;
  background: #f5f7fa;
  padding: 2px 8px;
  border-radius: 10px;
}

.group-content {
  /* 容器样式 */
}

/* 会话项 */
.session-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  margin-bottom: 4px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.25s ease;
  position: relative;
  border-left: 3px solid transparent;
}

.session-item:hover {
  background: #f5f7fa;
}

.session-item.active {
  background: #ecf5ff;
  border-left-color: #409eff;
}

/* 归档会话样式 */
.session-item.archived {
  opacity: 0.65;
  cursor: default;
}

.session-item.archived:hover {
  background: #fafafa;
}

.session-icon {
  font-size: 18px;
  color: #606266;
  flex-shrink: 0;
  transition: color 0.2s;
}

.session-item.active .session-icon {
  color: #409eff;
}

.session-item.archived .session-icon {
  color: #c0c4cc;
}

.session-info {
  flex: 1;
  min-width: 0;
}

.session-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
  transition: color 0.2s;
}

.session-name:hover {
  color: #409eff;
}

/* 归档会话名称样式 */
.session-name.archived-name {
  color: #909399;
  text-decoration: line-through;
  cursor: default;
}

.edit-input {
  width: 100%;
}

.session-time {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.delete-btn {
  opacity: 0;
  transition: opacity 0.2s;
  color: #f56c6c;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
}

.session-item:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  background: #fef0f0;
}

/* 底部工具栏 */
.sidebar-footer {
  padding: 12px 16px;
  border-top: 1px solid #e4e7ed;
  background: #fafafa;
}

.archive-toggle {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.toggle-label {
  font-size: 13px;
  color: #606266;
}

/* ===== 过渡动画 ===== */
.session-list-enter-active,
.session-list-leave-active {
  transition: all 0.3s ease;
}

.session-list-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.session-list-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

.session-list-move {
  transition: transform 0.3s ease;
}

/* ===== 右侧聊天区域 ===== */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  overflow: hidden;
}

/* 顶部工具栏 */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  border-bottom: 1px solid #e4e7ed;
  background: #fff;
  min-height: 64px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.collapse-btn {
  display: none !important;
}

.session-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-title:hover {
  color: #409eff;
}

.title-input {
  max-width: 400px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.model-select {
  width: 180px;
}

/* 消息容器 */
.message-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: #f5f7fa;
}

.message-container::-webkit-scrollbar {
  width: 8px;
}

.message-container::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 4px;
}

.message-container::-webkit-scrollbar-thumb:hover {
  background: #909399;
}

/* 空状态 */
.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.welcome-content {
  text-align: center;
  max-width: 600px;
}

.welcome-content h3 {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 20px 0 12px;
}

.welcome-content p {
  font-size: 16px;
  color: #606266;
  margin-bottom: 32px;
}

.quick-questions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.question-card {
  padding: 16px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #409eff;
  transition: all 0.2s;
}

.question-card:hover {
  background: #ecf5ff;
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

/* 消息列表 */
.messages-wrapper {
  max-width: 900px;
  margin: 0 auto;
}

.message-item {
  margin-bottom: 20px;
  animation: fadeInUp 0.3s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-content {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.user-content {
  justify-content: flex-end;
}

.assistant-content {
  justify-content: flex-start;
}

/* 气泡样式 - 增强版 */
.bubble {
  max-width: 70%;
  padding: 14px 18px;
  line-height: 1.7;
  font-size: 14px;
  word-break: break-word;
  position: relative;
  transition: all var(--transition-fast);
}

.user-bubble {
  background: linear-gradient(135deg, #409eff 0%, #53a8ff 100%);
  color: #fff;
  border-radius: 4px 16px 16px 16px;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.25);
}

.user-bubble:hover {
  box-shadow: 0 4px 20px rgba(64, 158, 255, 0.35);
}

.ai-bubble {
  background: #fff;
  color: #303133;
  border: 1px solid #e4e7ed;
  border-radius: 16px 4px 16px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.ai-bubble:hover {
  border-color: #d9ecff;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.message-item.error .ai-bubble {
  border-color: #f56c6c;
  background: #fef0f0;
}

/* 头像 - 增强版 */
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
  transition: all var(--transition-normal);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.message-item:hover .avatar {
  transform: scale(1.05);
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.ai-avatar {
  background: linear-gradient(135deg, #409eff 0%, #53a8ff 100%);
  color: #fff;
  font-size: 18px;
}

/* 时间戳 */
.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
  padding: 0 48px;
}

.user-content ~ .message-time {
  text-align: right;
}

/* Markdown 样式 */
.markdown-body {
  line-height: 1.8;
}

.markdown-body :deep(p) {
  margin: 0 0 8px;
}

.markdown-body :deep(p:last-child) {
  margin-bottom: 0;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4) {
  margin: 16px 0 8px;
  font-weight: 600;
  line-height: 1.4;
}

.markdown-body :deep(h1) { font-size: 1.5em; }
.markdown-body :deep(h2) { font-size: 1.3em; }
.markdown-body :deep(h3) { font-size: 1.15em; }

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  margin: 8px 0;
  padding-left: 24px;
}

.markdown-body :deep(li) {
  margin: 4px 0;
}

.markdown-body :deep(code) {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  color: #e83e8c;
}

.markdown-body :deep(pre) {
  margin: 12px 0;
  border-radius: 8px;
  overflow: hidden;
}

.markdown-body :deep(.code-block) {
  background: #1e1e1e;
  border-radius: 8px;
  overflow: hidden;
  margin: 12px 0;
}

.markdown-body :deep(.code-header) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background: #2d2d2d;
  border-bottom: 1px solid #3d3d3d;
}

.markdown-body :deep(.code-lang) {
  font-size: 12px;
  color: #888;
  font-family: 'Consolas', 'Monaco', monospace;
  text-transform: uppercase;
}

.markdown-body :deep(.copy-btn) {
  background: transparent;
  border: 1px solid #555;
  color: #ccc;
  padding: 4px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}

.markdown-body :deep(.copy-btn:hover) {
  background: #409eff;
  border-color: #409eff;
  color: #fff;
}

.markdown-body :deep(pre code) {
  display: block;
  padding: 16px;
  background: transparent;
  color: #d4d4d4;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  overflow-x: auto;
}

.markdown-body :deep(blockquote) {
  margin: 12px 0;
  padding: 8px 16px;
  border-left: 4px solid #409eff;
  background: #ecf5ff;
  color: #606266;
  border-radius: 0 4px 4px 0;
}

.markdown-body :deep(a) {
  color: #409eff;
  text-decoration: none;
}

.markdown-body :deep(a:hover) {
  text-decoration: underline;
}

.markdown-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 12px 0;
}

.markdown-body :deep(th),
.markdown-body :deep(td) {
  border: 1px solid #e4e7ed;
  padding: 8px 12px;
  text-align: left;
}

.markdown-body :deep(th) {
  background: #f5f7fa;
  font-weight: 600;
}

.markdown-body :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 8px 0;
}

.markdown-body :deep(hr) {
  border: none;
  border-top: 1px solid #e4e7ed;
  margin: 16px 0;
}

/* 加载动画 */
.loading-animation {
  display: flex;
  gap: 6px;
  padding: 8px 0;
}

.dot {
  width: 8px;
  height: 8px;
  background: #409eff;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

/* 光标闪烁 */
.cursor-blink {
  display: inline-block;
  width: 2px;
  height: 16px;
  background: #409eff;
  animation: blink 1s infinite;
  vertical-align: middle;
  margin-left: 2px;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

/* 输入区域 - 增强版 */
.input-area {
  padding: 18px 24px 22px;
  background: #fff;
  border-top: 1px solid #e4e7ed;
}

.input-wrapper {
  max-width: 900px;
  margin: 0 auto;
  background: #fff;
  border: 2px solid #e8ecf0;
  border-radius: 16px;
  padding: 14px 18px;
  transition: all var(--transition-normal);
  position: relative;
}

.input-wrapper::before {
  content: '';
  position: absolute;
  top: -2px;
  left: 20px;
  right: 20px;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--primary-color), transparent);
  opacity: 0;
  transition: opacity var(--transition-normal);
  border-radius: 2px;
}

.input-wrapper:focus-within {
  border-color: var(--primary-color);
  box-shadow:
    0 0 0 3px rgba(64, 158, 255, 0.08),
    0 4px 16px rgba(64, 158, 255, 0.12);
}

.input-wrapper:focus-within::before {
  opacity: 1;
}

.input-main {
  margin-bottom: 8px;
}

.message-input {
  width: 100%;
  border: none;
  outline: none;
  resize: none;
  font-size: 15px;
  line-height: 1.5;
  color: #303133;
  background: transparent;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  min-height: 24px;
  max-height: 200px;
  overflow-y: auto;
}

.message-input::placeholder {
  color: #c0c4cc;
}

.input-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.actions-left {
  display: flex;
  gap: 8px;
}

.actions-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.char-count {
  font-size: 12px;
  color: #909399;
}

.send-btn {
  min-width: 80px;
}

.stop-btn {
  min-width: 70px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .chat-container {
    height: calc(100vh - 64px);
  }

  .session-sidebar {
    position: fixed;
    left: 0;
    top: 64px;
    bottom: 0;
    z-index: 1000;
    box-shadow: 2px 0 12px rgba(0, 0, 0, 0.15);
  }

  .session-sidebar.collapsed {
    transform: translateX(-100%);
  }

  .collapse-btn {
    display: inline-flex !important;
  }

  .chat-header {
    padding: 12px 16px;
  }

  .model-select {
    width: 140px;
  }

  .message-container {
    padding: 16px;
  }

  .bubble {
    max-width: 85%;
  }

  .input-area {
    padding: 12px 16px;
  }

  .quick-questions {
    grid-template-columns: 1fr;
  }

  .session-title {
    font-size: 16px;
  }
}
</style>
