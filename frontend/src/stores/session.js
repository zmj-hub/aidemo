import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  getSessionList,
  createSession,
  updateSession,
  deleteSession,
  renameSession,
  archiveSessionApi,
  unarchiveSessionApi
} from '@/api/session'

export const useSessionStore = defineStore('session', () => {
  // 状态
  const sessions = ref([])
  const currentSessionId = ref(null)
  const loading = ref(false)
  const showArchived = ref(false)

  // 计算属性 - 当前选中的会话
  const currentSession = computed(() =>
    sessions.value.find(s => s.id === currentSessionId.value)
  )

  const sessionCount = computed(() => sessions.value.length)

  // 计算属性 - 活跃会话（非归档状态，后端 archived=false）
  const activeSessions = computed(() => {
    return sessions.value
      .filter(s => !s.archived)
      .sort((a, b) => new Date(b.lastMessageTime || b.updateTime) - new Date(a.lastMessageTime || a.updateTime))
  })

  // 计算属性 - 已归档会话（后端 archived=true）
  const archivedSessions = computed(() => {
    return sessions.value
      .filter(s => s.archived)
      .sort((a, b) => new Date(b.updateTime || b.lastMessageTime) - new Date(a.updateTime || b.lastMessageTime))
  })

  // Actions

  /**
   * 获取会话列表
   * @param {Object} params - 查询参数
   * @param {boolean} params.includeArchived - 是否包含归档会话
   */
  async function fetchSessions(params = {}) {
    loading.value = true
    try {
      const res = await getSessionList({ includeArchived: params.includeArchived ?? false })
      // 后端返回 Result<List<SessionInfo>>，axios拦截器取了 response.data
      // res = { code: 200, msg: "...", data: [SessionInfo, ...] }
      const list = Array.isArray(res.data) ? res.data : []
      sessions.value = list.map(s => ({
        ...s,
        name: s.title || s.name || '未命名会话',
        status: s.archived ? 'ARCHIVED' : 'ACTIVE'
      }))

      if (!currentSessionId.value && activeSessions.value.length > 0) {
        currentSessionId.value = activeSessions.value[0].id
      }

      return res
    } catch (error) {
      console.error('获取会话列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建新会话
   * @param {Object} data - 会话数据
   */
  async function createNewSession(data = {}) {
    try {
      // 后端 SessionCreateRequest 需要 title(可选) + modelCode(必填)
      const sessionData = {
        title: data.title || data.name || '新会话',
        modelCode: data.modelCode || 'qwen-turbo'
      }
      const res = await createSession(sessionData)

      // 后端返回 Result<SessionInfo>，axios拦截器取了 response.data
      // res.data = { id, title, modelCode, userId, archived, lastMessageTime, createTime, updateTime }
      const rawData = res.data || {}
      const newSession = {
        ...rawData,
        name: rawData.title || data.title || data.name || '新会话',
        status: rawData.archived ? 'ARCHIVED' : 'ACTIVE'
      }

      sessions.value.unshift(newSession)
      currentSessionId.value = newSession.id

      return newSession
    } catch (error) {
      console.error('创建会话失败:', error)
      throw error
    }
  }

  /**
   * 添加会话（兼容旧接口）
   */
  async function addSession(data) {
    return createNewSession(data)
  }

  /**
   * 编辑会话信息
   */
  async function editSession(sessionId, data) {
    try {
      const res = await updateSession(sessionId, data)
      const index = sessions.value.findIndex(s => s.id === sessionId)
      if (index !== -1) {
        sessions.value[index] = { ...sessions.value[index], ...res.data, ...data }
      }
      return res
    } catch (error) {
      console.error('编辑会话失败:', error)
      throw error
    }
  }

  /**
   * 删除会话
   */
  async function removeSession(sessionId) {
    try {
      await deleteSession(sessionId)
      sessions.value = sessions.value.filter(s => s.id !== sessionId)

      if (currentSessionId.value === sessionId) {
        const firstActive = activeSessions.value[0]
        currentSessionId.value = firstActive ? firstActive.id : null
      }

      return true
    } catch (error) {
      console.error('删除会话失败:', error)
      throw error
    }
  }

  /**
   * 重命名会话
   */
  async function rename(sessionId, name) {
    try {
      const res = await renameSession(sessionId, name)
      const session = sessions.value.find(s => s.id === sessionId)
      if (session) {
        session.name = name
        session.title = name
      }
      return res
    } catch (error) {
      console.error('重命名会话失败:', error)
      throw error
    }
  }

  /**
   * 更新会话标题
   */
  async function updateSessionTitle(sessionId, newTitle) {
    return rename(sessionId, newTitle)
  }

  /**
   * 归档会话
   */
  async function archiveSession(sessionId) {
    try {
      await archiveSessionApi(sessionId)
      const session = sessions.value.find(s => s.id === sessionId)
      if (session) {
        session.archived = true
        session.status = 'ARCHIVED'

        if (currentSessionId.value === sessionId) {
          const firstActive = activeSessions.value[0]
          currentSessionId.value = firstActive ? firstActive.id : null
        }
      }
      return true
    } catch (error) {
      console.error('归档会话失败:', error)
      throw error
    }
  }

  /**
   * 恢复已归档的会话
   */
  async function unarchiveSession(sessionId) {
    try {
      await unarchiveSessionApi(sessionId)
      const session = sessions.value.find(s => s.id === sessionId)
      if (session) {
        session.archived = false
        session.status = 'ACTIVE'
        delete session.archivedAt
      }
      return true
    } catch (error) {
      console.error('恢复会话失败:', error)
      throw error
    }
  }

  /**
   * 设置当前选中的会话
   */
  function setCurrentSession(sessionId) {
    currentSessionId.value = sessionId
  }

  /**
   * 选择会话（带验证）
   */
  function selectSession(sessionId) {
    const session = sessions.value.find(s => s.id === sessionId)
    if (session && !session.archived) {
      currentSessionId.value = sessionId
      return true
    } else if (session && session.archived) {
      console.warn('该会话已归档，请先恢复')
      return false
    }
    return false
  }

  /**
   * 切换显示归档会话
   */
  function toggleShowArchived() {
    showArchived.value = !showArchived.value
  }

  return {
    // 状态
    sessions,
    currentSessionId,
    loading,
    showArchived,

    // 计算属性
    currentSession,
    sessionCount,
    activeSessions,
    archivedSessions,

    // 方法
    fetchSessions,
    createNewSession,
    addSession,
    editSession,
    removeSession,
    rename,
    updateSessionTitle,
    archiveSession,
    unarchiveSession,
    setCurrentSession,
    selectSession,
    toggleShowArchived
  }
})
