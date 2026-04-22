import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getChatHistory, stopChat } from '@/api/agent'

export const useChatStore = defineStore('chat', () => {
  // 消息列表（按会话ID分组）
  const messagesMap = ref({})

  // 加载状态（按会话ID分组）
  const loadingMap = ref({})

  // 流式输出状态（按会话ID分组）
  const streamingMap = ref({})

  // 当前正在流式输出的消息内容
  const currentReplyMap = ref({})

  // AbortController 用于取消请求
  const abortControllerMap = ref({})

  // 获取当前会话的消息
  function getMessages(sessionId) {
    if (!messagesMap.value[sessionId]) {
      messagesMap.value[sessionId] = []
    }
    return messagesMap.value[sessionId]
  }

  // 获取当前会话的加载状态
  function isLoading(sessionId) {
    return loadingMap.value[sessionId] || false
  }

  // 获取当前会话的流式状态
  function isStreaming(sessionId) {
    return streamingMap.value[sessionId] || false
  }

  // 添加消息
  function addMessage(sessionId, message) {
    const messages = getMessages(sessionId)
    messages.push({
      id: Date.now() + Math.random(),
      timestamp: new Date().toISOString(),
      ...message
    })
    return messages[messages.length - 1]
  }

  // 更新消息（用于流式追加）
  function updateMessage(messageId, updates) {
    for (const sessionId in messagesMap.value) {
      const msg = messagesMap.value[sessionId].find(m => m.id === messageId)
      if (msg) {
        Object.assign(msg, updates)
        return msg
      }
    }
    return null
  }

  // 根据ID获取消息
  function getMessageById(messageId) {
    for (const sessionId in messagesMap.value) {
      const msg = messagesMap.value[sessionId].find(m => m.id === messageId)
      if (msg) return msg
    }
    return null
  }

  // 清空会话消息
  function clearMessages(sessionId) {
    if (sessionId) {
      messagesMap.value[sessionId] = []
    } else {
      messagesMap.value = {}
    }
  }

  // 删除单个会话的所有消息
  function deleteSessionMessages(sessionId) {
    delete messagesMap.value[sessionId]
    delete loadingMap.value[sessionId]
    delete streamingMap.value[sessionId]
    delete currentReplyMap.value[sessionId]
    delete abortControllerMap.value[sessionId]
  }

  // 设置加载状态
  function setLoading(sessionId, status) {
    loadingMap.value[sessionId] = status
  }

  // 设置流式状态
  function setStreaming(sessionId, status) {
    streamingMap.value[sessionId] = status
  }

  // 更新当前回复内容
  function updateCurrentReply(sessionId, content) {
    currentReplyMap.value[sessionId] = content
  }

  // 获取历史记录
  async function fetchHistory(sessionId, params) {
    try {
      setLoading(sessionId, true)
      const res = await getChatHistory(sessionId, params)
      messagesMap.value[sessionId] = res.list || res.data || []
      return res
    } catch (error) {
      throw error
    } finally {
      setLoading(sessionId, false)
    }
  }

  // 停止生成
  async function stopGeneration(sessionId) {
    try {
      // 取消当前的 fetch 请求
      if (abortControllerMap.value[sessionId]) {
        abortControllerMap.value[sessionId].abort()
        delete abortControllerMap.value[sessionId]
      }

      // 调用后端停止接口
      await stopChat(sessionId)

      // 重置状态
      setStreaming(sessionId, false)
      setLoading(sessionId, false)
    } catch (error) {
      throw error
    }
  }

  // 保存 AbortController
  function setAbortController(sessionId, controller) {
    abortControllerMap.value[sessionId] = controller
  }

  return {
    messagesMap,
    loadingMap,
    streamingMap,
    currentReplyMap,
    getMessages,
    isLoading,
    isStreaming,
    addMessage,
    updateMessage,
    getMessageById,
    clearMessages,
    deleteSessionMessages,
    setLoading,
    setStreaming,
    updateCurrentReply,
    fetchHistory,
    stopGeneration,
    setAbortController
  }
})
