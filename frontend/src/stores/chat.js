import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useChatStore = defineStore('chat', () => {
  // 消息列表（按会话ID分组）
  const messagesMap = ref({})

  // 加载状态（按会话ID分组）
  const loadingMap = ref({})

  // 流式输出状态（按会话ID分组）
  const streamingMap = ref({})

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

  // 停止生成
  async function stopGeneration(sessionId) {
    try {
      if (abortControllerMap.value[sessionId]) {
        abortControllerMap.value[sessionId].abort()
        delete abortControllerMap.value[sessionId]
      }
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
    stopGeneration,
    setAbortController
  }
})
