# 企业级AI应用平台 - Demo演示指南

## 目录
1. [概述](#概述)
2. [环境准备](#环境准备)
3. [快速开始](#快速开始)
4. [功能演示](#功能演示)
5. [API使用示例](#api使用示例)
6. [常见问题](#常见问题)

---

## 概述

企业级AI应用平台是一个功能完整的AI应用开发框架，提供以下核心功能：

- **智能Agent对话**：支持同步和流式对话，集成工具调用、记忆管理
- **多模型支持**：接入通义千问、DeepSeek、Ollama等多种模型
- **RAG文档问答**：文档上传、向量化存储、智能问答与溯源
- **会话管理**：会话创建、查询、更新、归档、删除
- **记忆管理**：会话记忆查看、搜索、删除

---

## 环境准备

### 系统要求
- JDK 17+
- Maven 3.6+
- Redis 6.0+
- Docker (可选，用于快速部署)

### 配置文件

修改 `src/main/resources/application-dev.yml` 配置文件：

```yaml
spring:
  redis:
    host: localhost
    port: 6379

ai:
  models:
    qwen:
      api-key: your-api-key
    deepseek:
      api-key: your-api-key
    ollama:
      base-url: http://localhost:11434
```

### 启动应用

```bash
# 使用Maven启动
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 或使用Docker
docker-compose up -d
```

应用启动后，访问：
- Swagger UI文档：http://localhost:8080/swagger-ui.html
- API文档JSON：http://localhost:8080/v3/api-docs

---

## 快速开始

### 1. 用户登录

首先需要登录获取访问Token。系统默认测试用户：
- 用户名：admin
- 密码：123456

使用Swagger UI或curl登录：

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

响应示例：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "your-token-here",
    "username": "admin",
    "nickname": "管理员",
    "avatar": ""
  }
}
```

### 2. 配置Swagger认证

在Swagger UI右上角点击 "Authorize" 按钮，输入：
```
Bearer your-token-here
```

### 3. 开始对话

获取可用模型列表并开始对话！

---

## 功能演示

### 一、认证模块

#### 1.1 用户登录

**接口：** `POST /auth/login`

**请求示例：**
```json
{
  "username": "admin",
  "password": "123456"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "admin",
    "nickname": "管理员",
    "avatar": ""
  }
}
```

#### 1.2 获取当前用户信息

**接口：** `GET /auth/current`

**需要认证：** 是

#### 1.3 用户登出

**接口：** `POST /auth/logout`

**需要认证：** 是

---

### 二、会话管理

#### 2.1 创建会话

**接口：** `POST /sessions`

**请求示例：**
```json
{
  "title": "我的第一个会话",
  "modelCode": "qwen-turbo"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "会话创建成功",
  "data": {
    "id": 1,
    "title": "我的第一个会话",
    "modelCode": "qwen-turbo",
    "userId": 1,
    "archived": false,
    "createTime": "2024-01-01T10:00:00"
  }
}
```

#### 2.2 获取会话列表

**接口：** `GET /sessions?includeArchived=false`

#### 2.3 更新会话

**接口：** `PUT /sessions/{sessionId}`

**请求示例：**
```json
{
  "title": "更新后的会话标题"
}
```

#### 2.4 归档/取消归档会话

**接口：** 
- `POST /sessions/{sessionId}/archive` - 归档
- `POST /sessions/{sessionId}/unarchive` - 取消归档

#### 2.5 删除会话

**接口：** `DELETE /sessions/{sessionId}`

---

### 三、Agent对话

#### 3.1 同步Agent对话

**接口：** `POST /agent/chat`

**请求示例：**
```json
{
  "sessionId": "1",
  "modelCode": "qwen-turbo",
  "message": "你好，请介绍一下自己",
  "systemPrompt": "你是一个专业的AI助手",
  "debugMode": false,
  "temperature": 0.7,
  "maxTokens": 2000,
  "memoryStrategy": "SHORT_TERM"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": "你好！我是AI助手，很高兴为您服务。",
    "traceId": "trace-abc123",
    "sessionId": "1",
    "success": true,
    "duration": 1500
  }
}
```

#### 3.2 流式Agent对话（SSE）

**接口：** `POST /agent/chat/stream`

**流式响应示例：**
```
data: {"type":"start","traceId":"trace-abc123","success":true}

data: {"type":"content","content":"你","traceId":"trace-abc123","success":true}

data: {"type":"content","content":"好","traceId":"trace-abc123","success":true}

data: {"type":"end","content":"你好！","traceId":"trace-abc123","success":true}
```

#### 3.3 调试模式

设置 `debugMode: true` 可获取完整思考链和工具调用记录：

```json
{
  "sessionId": "1",
  "modelCode": "qwen-turbo",
  "message": "计算 23 + 45",
  "debugMode": true
}
```

---

### 四、模型管理

#### 4.1 获取可用模型列表

**接口：** `GET /model/list`

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "code": "qwen-turbo",
      "name": "通义千问-Turbo",
      "provider": "QWEN",
      "enabled": true,
      "healthy": true,
      "description": "通义千问快速响应模型"
    },
    {
      "code": "deepseek-chat",
      "name": "DeepSeek-Chat",
      "provider": "DEEPSEEK",
      "enabled": true,
      "healthy": true,
      "description": "DeepSeek对话模型"
    }
  ]
}
```

#### 4.2 直接模型对话

**接口：** `POST /model/chat`

**请求示例：**
```json
{
  "modelCode": "qwen-turbo",
  "message": "你好",
  "temperature": 0.7,
  "maxTokens": 2000
}
```

#### 4.3 健康检查

**接口：** 
- `GET /model/health/{modelCode}` - 单个模型
- `POST /model/health/batch` - 批量模型

---

### 五、RAG文档问答

#### 5.1 上传文档

**接口：** `POST /rag/documents` (multipart/form-data)

**参数：**
- `file`: 文档文件（PDF、TXT、DOCX等）
- `documentName`: 文档名称
- `description`: 文档描述（可选）
- `embeddingModel`: Embedding模型（可选）

**curl示例：**
```bash
curl -X POST http://localhost:8080/rag/documents \
  -H "Authorization: Bearer your-token" \
  -F "file=@/path/to/document.pdf" \
  -F "documentName=产品说明书" \
  -F "description=这是产品使用说明书"
```

#### 5.2 获取文档列表

**接口：** `GET /rag/documents`

#### 5.3 RAG问答

**接口：** `POST /rag/query`

**请求示例：**
```json
{
  "query": "这个产品如何使用？",
  "chatModel": "qwen-turbo",
  "maxResults": 5,
  "minScore": 0.7
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "answer": "根据文档内容，该产品的使用步骤如下...",
    "chatModel": "qwen-turbo",
    "sources": [
      {
        "documentId": "doc-123",
        "documentName": "产品说明书",
        "content": "第一步：打开电源开关...",
        "score": 0.95
      }
    ]
  }
}
```

#### 5.4 删除文档

**接口：** `DELETE /rag/documents/{documentId}`

---

### 六、记忆管理

#### 6.1 获取所有会话记忆列表

**接口：** `GET /memory`

#### 6.2 获取会话记忆详情

**接口：** `GET /memory/session/{sessionId}`

#### 6.3 搜索记忆

**接口：** `POST /memory/search`

**请求示例：**
```json
{
  "keyword": "Java",
  "limit": 10
}
```

#### 6.4 删除会话记忆

**接口：** 
- `DELETE /memory/session/{sessionId}`
- `POST /memory/session/{sessionId}/clear`

#### 6.5 清空所有记忆

**接口：** `DELETE /memory/clear-all`

---

## API使用示例

### Python示例

```python
import requests
import json

BASE_URL = "http://localhost:8080"

# 1. 登录
def login(username, password):
    response = requests.post(
        f"{BASE_URL}/auth/login",
        json={"username": username, "password": password}
    )
    return response.json()["data"]["token"]

# 2. 创建会话
def create_session(token, title, model_code):
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.post(
        f"{BASE_URL}/sessions",
        headers=headers,
        json={"title": title, "modelCode": model_code}
    )
    return response.json()["data"]

# 3. Agent对话
def agent_chat(token, session_id, model_code, message):
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.post(
        f"{BASE_URL}/agent/chat",
        headers=headers,
        json={
            "sessionId": str(session_id),
            "modelCode": model_code,
            "message": message
        }
    )
    return response.json()["data"]

# 使用示例
if __name__ == "__main__":
    token = login("admin", "123456")
    session = create_session(token, "测试会话", "qwen-turbo")
    response = agent_chat(token, session["id"], "qwen-turbo", "你好")
    print(response["content"])
```

### JavaScript示例

```javascript
const BASE_URL = "http://localhost:8080";

// 登录
async function login(username, password) {
    const response = await fetch(`${BASE_URL}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password })
    });
    const data = await response.json();
    return data.data.token;
}

// Agent对话
async function agentChat(token, sessionId, modelCode, message) {
    const response = await fetch(`${BASE_URL}/agent/chat`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({
            sessionId: String(sessionId),
            modelCode,
            message
        })
    });
    const data = await response.json();
    return data.data;
}

// 使用示例
(async () => {
    const token = await login("admin", "123456");
    const response = await agentChat(token, "1", "qwen-turbo", "你好");
    console.log(response.content);
})();
```

### Java示例

```java
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class AiClient {
    private static final String BASE_URL = "http://localhost:8080";
    private final RestTemplate restTemplate = new RestTemplate();

    public String login(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String requestBody = String.format(
            "{\"username\":\"%s\",\"password\":\"%s\"}", 
            username, password
        );
        
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
            BASE_URL + "/auth/login", entity, Map.class
        );
        
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        return (String) data.get("token");
    }

    public Map<String, Object> agentChat(String token, String sessionId, 
                                         String modelCode, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("sessionId", sessionId);
        requestBody.put("modelCode", modelCode);
        requestBody.put("message", message);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
            BASE_URL + "/agent/chat", entity, Map.class
        );
        
        return (Map<String, Object>) response.getBody().get("data");
    }
}
```

---

## 常见问题

### Q1: 如何配置多个模型提供商？

A: 在 `application.yml` 中配置多个模型提供商的API Key即可。系统会自动加载所有可用模型。

### Q2: 支持哪些文档格式？

A: 目前支持PDF、TXT、DOCX、MD等常见文档格式。

### Q3: 流式对话如何实现？

A: 使用SSE（Server-Sent Events）协议，前端需要使用EventSource或类似库来处理流式响应。

### Q4: 如何自定义Agent的工具？

A: 实现 `Tool` 接口并注册到 `ToolRegistry` 中，Agent会自动发现并使用。

### Q5: 记忆数据存储在哪里？

A: 短期记忆存储在Redis中，长期记忆存储在向量数据库中。

---

## 技术支持

如有问题，请联系开发团队或查看项目README.md文档。
