package com.enterprise.ai.config;

import io.agentscope.core.session.JsonSession;
import io.agentscope.core.session.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AgentScopeConfig {

    private final PlatformProperties platformProperties;

    @Bean
    public Path workspacePath() {
        Path path = Path.of(platformProperties.getWorkspace().getPath());
        if (platformProperties.getWorkspace().isAutoInit()) {
            try {
                Files.createDirectories(path);
                // 初始化 AGENTS.md（如果不存在）
                Path agentsMd = path.resolve("AGENTS.md");
                if (!Files.exists(agentsMd)) {
                    Files.writeString(agentsMd, """
                            # AI 智能助手

                            你是一个企业级AI智能助手，具备以下能力：
                            - 使用工具获取实时信息（时间、计算、HTTP请求等）
                            - 通过RAG检索知识库中的文档
                            - 管理会话记忆和上下文

                            ## 行为准则
                            - 回答简洁准确，必要时使用列表
                            - 对于不确定的信息，明确说明而不是猜测
                            - 主动使用可用工具来提供更好的回答
                            """);
                    log.info("Created AGENTS.md in workspace: {}", agentsMd);
                }
            } catch (IOException e) {
                log.warn("Failed to create workspace directory: {}", e.getMessage());
            }
        }
        return path;
    }

    @Bean
    public Path sessionPath() {
        Path path = Path.of(platformProperties.getSession().getPath());
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            log.warn("Failed to create session directory: {}", e.getMessage());
        }
        return path;
    }

    @Bean
    public Session jsonSession() {
        return new JsonSession(sessionPath());
    }
}
