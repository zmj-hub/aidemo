import request from '@/utils/request'

export function sendMessage(data) {
  return request.post('/agent/chat', data)
}

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
      let errorMsg = `请求失败 (${response.status})`
      try {
        const errorBody = await response.text()
        if (errorBody) {
          const errorJson = JSON.parse(errorBody)
          errorMsg = errorJson.message || errorJson.error || errorMsg
        }
      } catch {
        // 无法解析错误响应体
      }
      throw new Error(errorMsg)
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
        const trimmed = line.trim()
        if (!trimmed || !trimmed.startsWith('data:')) continue

        const data = trimmed.slice(5).trim()

        if (data === '[DONE]') {
          if (onDone) onDone()
          continue
        }

        try {
          const parsed = JSON.parse(data)
          if (onMessage) onMessage(parsed)
        } catch (e) {
          console.error('解析SSE数据失败:', e)
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

export function stopChat(sessionId) {
  return request.post(`/agent/stop/${sessionId}`)
}
