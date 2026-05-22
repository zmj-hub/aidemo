# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Build the backend
mvn clean package -DskipTests

# Run the backend (dev profile)
mvn spring-boot:run

# Run a single test class
mvn test -Dtest=ClassName

# Run a single test method
mvn test -Dtest=ClassName#methodName

# Frontend dev server (proxies /api to localhost:8080)
cd frontend && npm run dev

# Frontend production build
cd frontend && npm run build
```

Backend runs on port 8080, frontend dev server on port 5173.
API docs: `http://localhost:8080/swagger-ui.html` (Swagger) or `http://localhost:8080/doc.html` (Knife4j).

Set `PLATFORM_MODEL_MODELSCOPE_API_KEY` env var (or edit `application.yml` `platform.model.modelscope.api-key`) to authenticate with ModelScope API.

## Architecture

Spring Boot 3.3.5 + Vue 3 AI assistant platform using **AgentScope 1.1.0-RC1** (not LangChain4j — the project migrated in commit `daadf06`). Java 21 with virtual threads enabled.

### Backend (`src/main/java/com/enterprise/ai/`)

- **`config/`** — `AgentScopeConfig.java` creates `JsonSession` (file-based session persistence at `./data/sessions/`) and workspace. `ModelRegistryConfig.java` registers ModelScope models into AgentScope's `ModelRegistry` using `OpenAIChatModel` (OpenAI-compatible adapter) pointed at `https://api-inference.modelscope.cn/v1`. `PlatformProperties.java` binds `platform.*` config. `WebConfig.java` handles CORS. `SaTokenConfig.java` sets up Sa-Token (currently all routes are permissive).
- **`controller/`** — REST controllers all returning `Result<T>` wrapper. `AgentController` (sync + SSE streaming chat via `SseEmitter`), `ModelController`, `SessionController`, `RagController`, `MemoryController`, `AuthController`.
- **`service/agent/`** — `AgentService` orchestrates chat using AgentScope's `HarnessAgent` (both `agent.call()` and `agent.stream()`). `AgentFactory` builds/caches `HarnessAgent` instances per session key, wiring a `Toolkit` from the tool names requested. `TraceService` records reasoning traces.
- **`tools/`** — AgentScope `@Tool`-annotated classes: `SystemTools` (time, calculator), `HttpTool`, `MemoryTools` (add/query/clear via ConcurrentHashMap), `RagQueryTool`. Tools are registered by name through `AgentFactory`.
- **`hooks/`** — `TraceHook` implements AgentScope's `Hook` interface, intercepting PreCall/PreReasoning/PreActing/PostCall events for debugging.
- **`service/model/`** — `ChatModelService` for model listing, health checks, and direct model invocation. Models are accessed via `ModelRegistry`.
- **`service/rag/`** — `DocumentService` stores files to `./data/documents/`. `RagService` performs keyword-based matching (not vector search) against document names, then constructs a prompt with matched content for the LLM to answer.
- **`service/session/`** — `SessionService` manages session metadata in a `ConcurrentHashMap` (in-memory only).
- **`service/memory/`** — `MemoryService` accesses AgentScope's `JsonSession` for conversation state persistence.
- **`domain/`** — Entities (User, Session, Document), 17 DTOs, and `ModelProvider` enum.
- **`common/`** — `Result<T>` response wrapper, `BusinessException`, `UserContextHolder` (ThreadLocal), utility classes.
- **`aspect/`** — `LogAspect` logs controller calls with params/response/duration.

Session/document metadata are stored in-memory (ConcurrentHashMap). Agent state persists as JSON files via AgentScope's `JsonSession`. Redis is configured but not actively used by current code. Auth uses Sa-Token with hardcoded users (`admin/admin123` or `admin/123456`); all endpoints are currently open.

### Frontend (`frontend/src/`)

Vue 3 (Composition API) + Element Plus + Pinia + Vue Router 4. Vite 5 build tool.

- `api/` — Axios modules per resource (agent, auth, model, rag, session, memory).
- `stores/` — Pinia stores: `user.js`, `chat.js` (messages + SSE streaming state), `session.js`.
- `views/` — `chat/ChatView.vue` is the main component (~1900 lines): session sidebar, SSE streaming, markdown rendering (markdown-it + highlight.js), typing indicator, cancel support.
- `utils/request.js` — Axios instance with auth token interceptor.

### Docker

`docker-compose.yml` starts Redis 7 + the app. `Dockerfile` uses multi-stage Maven build then JRE 21 alpine. Sets env vars `QWEN_API_KEY`, `DEEPSEEK_API_KEY`, `PINECONE_API_KEY` — but the current code reads `platform.model.modelscope.api-key` from config, so update accordingly if using Docker.
