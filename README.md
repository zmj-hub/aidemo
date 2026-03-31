# AI 企业级应用平台

一个基于 Spring Boot 3.x 和 LangChain4j 构建的企业级 AI 应用平台，支持多种 AI 模型集成、Agent 对话、RAG 文档问答等功能。

## 功能特性

- 多种 AI 模型支持（ModelScope、Ollama）
- Agent 智能对话（同步、流式）
- RAG 文档智能问答
- 会话管理
- 用户认证（基于 Sa-Token）
- Swagger API 文档
- Redis 缓存支持

## 环境准备

### 本地开发环境

- **JDK 21**：推荐使用 Eclipse Temurin 或 OpenJDK 21
- **Maven 3.9+**：用于项目构建
- **Redis 7+**：用于缓存和会话存储

### Docker 环境

- **Docker 20.10+**
- **Docker Compose 2.0+**

## 启动步骤

### 方式一：本地启动

#### 1. 克隆项目

```bash
git clone <repository-url>
cd aidemo
```

#### 2. 配置环境变量

在项目根目录创建 `.env` 文件，配置 API 密钥：

```env
MODELSCOPE_API_KEY=your-modelscope-api-key
PINECONE_API_KEY=your-pinecone-api-key
PINECONE_ENVIRONMENT=your-pinecone-environment
```

#### 3. 启动 Redis

```bash
# 使用 Docker 启动 Redis
docker run -d -p 6379:6379 --name redis redis:7-alpine redis-server --appendonly yes
```

#### 4. 编译并运行项目

```bash
# 编译项目
mvn clean package -DskipTests

# 运行项目
java -jar target/ai-platform-1.0.0-SNAPSHOT.jar
```

或使用 Maven 直接运行：

```bash
mvn spring-boot:run
```

#### 5. 访问应用

- 应用地址：http://localhost:8080
- Swagger API 文档：http://localhost:8080/swagger-ui.html
- 健康检查：http://localhost:8080/actuator/health

---

### 方式二：Docker 启动

#### 1. 配置环境变量

在项目根目录创建 `.env` 文件：

```env
MODELSCOPE_API_KEY=your-modelscope-api-key
PINECONE_API_KEY=your-pinecone-api-key
PINECONE_ENVIRONMENT=your-pinecone-environment
```

#### 2. 构建并启动服务

```bash
# 构建并启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f app

# 查看所有服务状态
docker-compose ps
```

#### 3. 停止服务

```bash
# 停止服务但保留数据
docker-compose down

# 停止服务并删除数据卷
docker-compose down -v
```

#### 4. 访问应用

- 应用地址：http://localhost:8080
- Swagger API 文档：http://localhost:8080/swagger-ui.html
- Redis：localhost:6379

## API 密钥获取方式

### 1. ModelScope（魔搭社区）API 密钥

1. 访问 ModelScope：https://www.modelscope.cn/
2. 注册/登录账号
3. 进入个人中心 -> API管理页面
4. 创建新的 API Key（AccessToken）
5. 在 `application.yml` 中配置：
   ```yaml
   model:
     modelscope:
       enabled: true
       api-key: ${MODELSCOPE_API_KEY:your-api-key}
       base-url: https://api-inference.modelscope.cn/v1
   ```

**支持的模型**：
- qwen-turbo（通义千问Turbo）
- qwen-max（通义千问Max）
- qwen-plus（通义千问Plus）
- qwen2-7b-instruct（Qwen2-7B）
- Qwen/Qwen3.5-397B-A17B（Qwen3.5-397B）
- Qwen/Qwen3.5-122B-A10B（Qwen3.5-122B）
- llama3-8b-instruct（Llama3-8B）

## 接口调用示例

### 1. 用户登录

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
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

### 2. 获取模型列表

```bash
curl -X GET http://localhost:8080/model/list \
  -H "Authorization: your-token-here"
```

### 3. 同步聊天

```bash
curl -X POST http://localhost:8080/model/chat \
  -H "Content-Type: application/json" \
  -H "Authorization: your-token-here" \
  -d '{
    "modelCode": "qwen-turbo",
    "messages": [
      {
        "role": "user",
        "content": "你好，请介绍一下自己"
      }
    ]
  }'
```

### 4. Agent 同步聊天

```bash
curl -X POST http://localhost:8080/agent/chat \
  -H "Content-Type: application/json" \
  -H "Authorization: your-token-here" \
  -d '{
    "modelCode": "qwen-turbo",
    "query": "今天天气怎么样？",
    "sessionId": 1,
    "debugMode": false
  }'
```

### 5. 上传文档（RAG）

```bash
curl -X POST http://localhost:8080/rag/documents \
  -H "Authorization: your-token-here" \
  -F "file=@/path/to/your/document.pdf" \
  -F "title=文档标题" \
  -F "description=文档描述"
```

### 6. RAG 问答

```bash
curl -X POST http://localhost:8080/rag/query \
  -H "Content-Type: application/json" \
  -H "Authorization: your-token-here" \
  -d '{
    "query": "文档中提到的主要内容是什么？",
    "modelCode": "qwen-turbo",
    "maxResults": 5
  }'
```

### 7. 创建会话

```bash
curl -X POST http://localhost:8080/sessions \
  -H "Content-Type: application/json" \
  -H "Authorization: your-token-here" \
  -d '{
    "title": "我的新会话",
    "modelCode": "qwen-turbo"
  }'
```

### 8. 用户登出

```bash
curl -X POST http://localhost:8080/auth/logout \
  -H "Authorization: your-token-here"
```

## 常见问题排查

### 1. 应用启动失败，连接 Redis 超时

**问题**：`Unable to connect to Redis`

**解决方案**：
- 确认 Redis 服务已启动：`docker ps | grep redis`
- 检查 Redis 端口是否被占用：`netstat -ano | findstr 6379`
- 确认 `application.yml` 中的 Redis 配置正确
- 如果使用 Docker Compose，确认服务依赖关系正确

### 2. API 调用返回 401 Unauthorized

**问题**：接口调用返回未授权

**解决方案**：
- 确认已先调用登录接口获取 token
- 检查请求头中是否正确携带 `Authorization: your-token`
- 确认 token 未过期（默认有效期 30 天）

### 3. 模型调用失败

**问题**：`Model not available` 或 API 调用错误

**解决方案**：
- 确认已在 `application.yml` 中启用对应模型（`enabled: true`）
- 检查 API Key 是否正确配置
- 确认 API Key 有足够的额度
- 检查网络连接是否可以访问模型服务商的 API

### 4. Docker 构建失败

**问题**：Docker build 过程中出错

**解决方案**：
- 确认 Docker 服务正常运行
- 检查网络连接（构建过程需要下载依赖）
- 尝试清理 Docker 缓存：`docker builder prune`
- 查看详细构建日志：`docker-compose build --no-cache`

### 5. 上传文档失败

**问题**：RAG 文档上传失败

**解决方案**：
- 确认文档格式支持（PDF、Word、TXT 等）
- 检查文档大小是否超限
- 确认 `./data/documents` 目录有写入权限
- 查看应用日志获取详细错误信息

### 6. 内存不足（OOM）

**问题**：应用启动或运行时 OutOfMemoryError

**解决方案**：
- 调整 JVM 内存参数：`-Xmx1g -Xms512m`
- 如果使用 Docker，调整容器内存限制
- 减少同时加载的模型数量

## 项目结构

```
aidemo/
├── src/
│   ├── main/
│   │   ├── java/com/enterprise/ai/
│   │   │   ├── aspect/              # AOP 切面
│   │   │   ├── common/              # 公共模块
│   │   │   │   ├── constants/       # 常量定义
│   │   │   │   ├── exception/       # 异常处理
│   │   │   │   ├── result/          # 统一响应
│   │   │   │   └── utils/           # 工具类
│   │   │   ├── config/              # 配置类
│   │   │   ├── controller/          # 控制器
│   │   │   ├── domain/              # 领域模型
│   │   │   │   ├── dto/             # 数据传输对象
│   │   │   │   ├── entity/          # 实体类
│   │   │   │   └── enums/           # 枚举
│   │   │   └── service/             # 服务层
│   │   └── resources/
│   │       ├── application.yml      # 主配置文件
│   │       └── application-dev.yml  # 开发环境配置
├── Dockerfile                        # Docker 镜像构建文件
├── docker-compose.yml                # Docker Compose 配置
├── pom.xml                           # Maven 配置
└── README.md                         # 项目文档
```

## 技术栈

- **后端框架**：Spring Boot 3.3.5
- **JDK**：OpenJDK 21
- **构建工具**：Maven 3.9+
- **AI 框架**：LangChain4j 0.34.0
- **缓存**：Redis 7+
- **认证**：Sa-Token 1.37.0
- **API 文档**：SpringDoc OpenAPI 2.5.0
- **工具库**：Hutool 5.8.29, Lombok 1.18.34

## 许可证

MIT License
