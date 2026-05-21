# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Build the backend
mvn clean package -DskipTests

# Run the backend (dev profile)
mvn spring-boot:run

# Run frontend dev server
cd frontend && npm run dev
```

The backend runs on port 8080, the frontend dev server on port 5173 (proxies `/api` to 8080).

Application uses the `dev` profile by default. Set `DASHSCOPE_API_KEY` environment variable to authenticate with ModelScope API.

## Architecture

This is a Spring Boot 3.3.5 + Vue 3 AI assistant platform using LangChain4j 1.12.2.

### Backend (`src/main/java/com/enterprise/ai/`)

Standard layered architecture:

- **`config/`** — Bean definitions. `ModelConfig.java` defines all `ChatModel`/`StreamingChatModel` beans (ModelScope-compatible, accessed via `https://api-inference.modelscope.cn/v1`). `RagConfig.java` wires `EmbeddingModel` and `InMemoryEmbeddingStore`.
- **`controller/`** — REST controllers: `AgentController` (sync + SSE streaming chat), `ModelController` (model list, health check), `SessionController`, `RagController`, `MemoryController`, `AuthController`. All return `Result<T>` wrapper.
- **`service/agent/`** — Core agent logic. `AgentService` orchestrates chat using LangChain4j `AiServices`. `AgentFactory` caches agent instances by key. `TraceService` records reasoning traces in memory.
- **`service/agent/memory/`** — `RedisShortTermMemory` implements LangChain4j `ChatMemoryStore` backed by Redis. `MemorySummarizer` compresses long conversations via LLM.
- **`service/agent/tools/`** — Plugin tool system: tools implement `Tool` interface and are registered through `ToolManager` (supports SPI via `ToolRegistry`). Includes `TimeTool`, `CalculatorTool`, `MemoryAddTool`, `MemoryQueryTool`, `RagQueryTool`, `HttpTool`.
- **`service/model/`** — `ChatModelService` for direct model invocation with role-based permissions. `ModelFactory` maintains a singleton pool of models.
- **`service/rag/`** — RAG pipeline: embed query → vector search → build context → answer with sources.
- **`domain/`** — Entities (User, Session, Document), DTOs, and enums (`ModelProvider` lists all registered models).
- **`common/`** — `Result<T>` response wrapper, `BusinessException`, utility classes, `UserContextHolder` (ThreadLocal-based user context).
- **`aspect/`** — `LogAspect` logs all controller calls with params/response/duration.

Configuration properties classes are suffixed with `Properties` (e.g., `RagProperties`, `MemoryProperties`, `ModelScopeProperties`) and use `@ConfigurationProperties`.

Session and document metadata are stored in-memory (ConcurrentHashMap). Chat memory persists in Redis. RAG vector store uses LangChain4j's `InMemoryEmbeddingStore`. Auth uses Sa-Token with hardcoded users (currently all endpoints are open).

### Frontend (`frontend/src/`)

Vue 3 + Element Plus + Pinia + Vue Router. See `frontend/package.json` for dependencies. API modules live in `api/`, state in `stores/`, views in `views/`. The `request.js` Axios instance handles auth tokens via interceptors.

### Docker

`docker-compose.yml` starts Redis 7 + the app. The `Dockerfile` uses multi-stage Maven build then JRE 21 alpine.
