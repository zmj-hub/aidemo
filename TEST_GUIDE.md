# 企业级AI应用平台 - 测试说明文档

## 目录
1. [测试概述](#测试概述)
2. [环境准备](#环境准备)
3. [单元测试](#单元测试)
4. [集成测试](#集成测试)
5. [API测试](#api测试)
6. [性能测试](#性能测试)
7. [测试报告](#测试报告)

---

## 测试概述

本文档提供企业级AI应用平台的测试指南，包括单元测试、集成测试、API测试和性能测试的详细说明。

### 测试目标
- 确保所有API接口正常工作
- 验证各功能模块的正确性
- 检查系统的稳定性和性能
- 保证代码质量和可维护性

---

## 环境准备

### 测试环境要求
- JDK 17+
- Maven 3.6+
- Redis 6.0+ (测试用)
- 测试数据库 (H2或MySQL)

### 配置测试环境

创建 `src/test/resources/application-test.yml`：

```yaml
spring:
  profiles:
    active: test
  redis:
    host: localhost
    port: 6379
    database: 1
  h2:
    console:
      enabled: true

ai:
  models:
    qwen:
      api-key: test-api-key
      enabled: false
    deepseek:
      api-key: test-api-key
      enabled: false
    ollama:
      base-url: http://localhost:11434
      enabled: false
```

---

## 单元测试

### 运行单元测试

```bash
# 运行所有单元测试
mvn test

# 运行特定测试类
mvn test -Dtest=AgentServiceTest

# 运行特定测试方法
mvn test -Dtest=AgentServiceTest#testChat
```

### 编写单元测试示例

#### Service层测试

```java
package com.enterprise.ai.service.agent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class AgentServiceTest {

    @Mock
    private AgentFactory agentFactory;

    @Mock
    private TraceService traceService;

    @InjectMocks
    private AgentService agentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testChat_Success() {
        // 准备测试数据
        AgentChatRequest request = new AgentChatRequest();
        request.setSessionId("test-session");
        request.setModelCode("qwen-turbo");
        request.setMessage("你好");

        // Mock行为
        when(traceService.startTrace(any(), any(), any())).thenReturn("trace-123");

        // 执行测试
        AgentChatResponse response = agentService.chat(request);

        // 验证结果
        assertNotNull(response);
        assertEquals("test-session", response.getSessionId());
        verify(traceService, times(1)).startTrace(any(), any(), any());
    }
}
```

#### Controller层测试

```java
package com.enterprise.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void testLogin_Success() throws Exception {
        // 准备测试数据
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("123456");

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("admin");
        mockUser.setNickname("管理员");

        when(userService.validateUser("admin", "123456")).thenReturn(mockUser);

        // 执行测试并验证
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("admin"));
    }
}
```

---

## 集成测试

### 运行集成测试

```bash
# 运行集成测试
mvn verify

# 只运行集成测试
mvn failsafe:integration-test
```

### 集成测试示例

```java
package com.enterprise.ai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AiApplicationIntegrationTest {

    @Autowired
    private AgentService agentService;

    @Autowired
    private SessionService sessionService;

    @Test
    void testCompleteWorkflow() {
        // 1. 创建会话
        SessionCreateRequest createRequest = new SessionCreateRequest();
        createRequest.setTitle("测试会话");
        createRequest.setModelCode("qwen-turbo");
        
        SessionInfo session = sessionService.createSession(createRequest);
        assertNotNull(session);
        assertNotNull(session.getId());

        // 2. Agent对话
        AgentChatRequest chatRequest = new AgentChatRequest();
        chatRequest.setSessionId(String.valueOf(session.getId()));
        chatRequest.setModelCode("qwen-turbo");
        chatRequest.setMessage("测试消息");
        
        AgentChatResponse response = agentService.chat(chatRequest);
        assertNotNull(response);
    }
}
```

---

## API测试

### 使用Swagger UI测试

1. 启动应用：`mvn spring-boot:run`
2. 访问：http://localhost:8080/swagger-ui.html
3. 点击右上角 "Authorize"，输入登录Token
4. 选择要测试的API，点击 "Try it out"
5. 输入请求参数，点击 "Execute"

### 使用Postman测试

#### 导入Postman集合

1. 访问 http://localhost:8080/v3/api-docs 获取OpenAPI规范
2. 在Postman中点击 "Import" → "Link"
3. 粘贴API文档URL并导入

#### 测试流程示例

**1. 登录获取Token**
```
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

**2. 创建会话**
```
POST /sessions
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "测试会话",
  "modelCode": "qwen-turbo"
}
```

**3. Agent对话**
```
POST /agent/chat
Authorization: Bearer {token}
Content-Type: application/json

{
  "sessionId": "1",
  "modelCode": "qwen-turbo",
  "message": "你好"
}
```

### 使用curl测试

创建测试脚本 `test-api.sh`：

```bash
#!/bin/bash

BASE_URL="http://localhost:8080"

echo "=== 1. 登录 ==="
LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo "Token: $TOKEN"

echo -e "\n=== 2. 获取模型列表 ==="
curl -X GET $BASE_URL/model/list \
  -H "Authorization: Bearer $TOKEN"

echo -e "\n=== 3. 创建会话 ==="
SESSION_RESPONSE=$(curl -s -X POST $BASE_URL/sessions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"API测试会话","modelCode":"qwen-turbo"}')

SESSION_ID=$(echo $SESSION_RESPONSE | grep -o '"id":[0-9]*' | cut -d':' -f2)
echo "Session ID: $SESSION_ID"

echo -e "\n=== 4. Agent对话 ==="
curl -X POST $BASE_URL/agent/chat \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"sessionId\":\"$SESSION_ID\",\"modelCode\":\"qwen-turbo\",\"message\":\"你好，请介绍一下自己\"}"
```

---

## 性能测试

### 使用JMeter进行性能测试

#### 测试计划示例

1. 创建测试计划
2. 添加线程组（100个线程，持续60秒）
3. 添加HTTP请求默认值
4. 添加HTTP请求（登录、对话等）
5. 添加监听器（查看结果树、汇总报告）

#### 测试场景

| 场景 | 并发用户 | 持续时间 | 目标TPS |
|------|----------|----------|---------|
| 登录测试 | 50 | 30s | 100 |
| Agent对话 | 20 | 60s | 50 |
| RAG查询 | 10 | 60s | 30 |

### 性能基准

| 指标 | 目标值 |
|------|--------|
| API响应时间（P95） | < 500ms |
| API响应时间（P99） | < 1000ms |
| 吞吐量 | > 100 req/s |
| 错误率 | < 0.1% |

---

## 测试报告

### 生成测试覆盖率报告

```bash
# 使用JaCoCo生成覆盖率报告
mvn clean test jacoco:report

# 查看报告
# 打开 target/site/jacoco/index.html
```

### 代码质量检查

```bash
# 使用SonarQube分析
mvn sonar:sonar \
  -Dsonar.projectKey=aidemo \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=your-token
```

### 测试检查清单

- [ ] 所有单元测试通过
- [ ] 所有集成测试通过
- [ ] API功能测试通过
- [ ] 性能测试达标
- [ ] 代码覆盖率 > 80%
- [ ] 无严重安全漏洞
- [ ] 文档完整更新

---

## 常见问题

### Q1: 测试时如何模拟外部API调用？

A: 使用Mockito或WireMock模拟外部API调用，避免依赖外部服务。

### Q2: 如何并行运行测试以提高速度？

A: 在pom.xml中配置maven-surefire-plugin：
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <parallel>methods</parallel>
        <threadCount>4</threadCount>
    </configuration>
</plugin>
```

### Q3: 如何在CI/CD中集成测试？

A: 使用GitHub Actions、GitLab CI或Jenkins配置测试流水线，每次提交自动运行测试。

---

## 附录

### 测试数据

#### 测试用户
| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 管理员 |
| test | test123 | 普通用户 |

#### 测试文档
- 位置：`src/test/resources/test-docs/`
- 格式：PDF、TXT、DOCX

### 参考文档
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Documentation](https://assertj.github.io/doc/)
