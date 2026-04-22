import request from '@/utils/request'

// 发送消息给Agent（同步模式）
export function sendMessage(data) {
  return request.post('/agent/chat', data)
}

// 获取对话历史
export function getChatHistory(sessionId, params) {
  return request.get(`/agent/history/${sessionId}`, { params })
}

// 流式对话（SSE - 使用原生fetch实现）
export async function streamChat(data, onMessage, onError, onDone, signal) {
  const token = localStorage.getItem('token')

  try {
    const response = await fetch('/api/agent/chat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        message: data.message,
        sessionId: data.sessionId,
        modelCode: data.modelCode,
        stream: true
      }),
      signal
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()

      if (done) {
        if (onDone) onDone()
        break
      }

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const data = line.slice(6).trim()

          if (data === '[DONE]') {
            if (onDone) onDone()
            continue
          }

          try {
            const parsed = JSON.parse(data)
            if (onMessage) onMessage(parsed)
          } catch (e) {
            console.error('解析SSE数据失败:', e)
            if (onError) onError(e)
          }
        }
      }
    }
  } catch (error) {
    if (error.name === 'AbortError') {
      console.log('请求被取消')
    } else {
      console.error('流式请求失败:', error)
      if (onError) onError(error)
    }
  }
}

// 终止当前对话
export function stopChat(sessionId) {
  return request.post(`/agent/stop/${sessionId}`)
}
