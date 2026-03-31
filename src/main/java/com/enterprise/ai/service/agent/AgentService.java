package com.enterprise.ai.service.agent;

import cn.dev33.satoken.stp.StpUtil;
import com.enterprise.ai.common.exception.BusinessException;
import com.enterprise.ai.domain.dto.AgentChatRequest;
import com.enterprise.ai.domain.dto.AgentChatResponse;
import com.enterprise.ai.domain.dto.AgentStreamResponse;
import com.enterprise.ai.service.agent.tools.ToolManager;
import com.enterprise.ai.service.model.ModelFactory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;

/**
 * Agent服务类
 * 提供Agent同步对话和SSE流式对话功能，集成记忆管理、会话管理、工具管理、推理追踪
 */
@Slf4j
@Service
public class AgentService {

    @Autowired
    private AgentFactory agentFactory;

    @Autowired
    private TraceService traceService;

    @Autowired
    private ToolManager toolManager;

    @Autowired
    private ModelFactory modelFactory;

    /**
     * Agent服务接口
     */
    public interface Agent {
        String chat(String userMessage);
    }

    /**
     * 同步Agent聊天
     *
     * @param request 聊天请求
     * @return 聊天响应
     */
    public AgentChatResponse chat(AgentChatRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        String traceId = null;
        long startTime = System.currentTimeMillis();

        try {
            traceId = traceService.startTrace(request.getSessionId(), userId, "enhanced-agent");
            traceService.setUserInput(traceId, request.getMessage());

            ChatLanguageModel chatModel = modelFactory.getChatModel(request.getModelCode());
            if (chatModel == null) {
                throw new BusinessException("获取模型失败: " + request.getModelCode());
            }

            AgentConfig config = buildAgentConfig(request, userId);
            Agent agent = agentFactory.createAgent(config, Agent.class);

            String response = agent.chat(request.getMessage());
            traceService.setAgentOutput(traceId, response);

            TraceRecord traceRecord = traceService.getTrace(traceId);
            long duration = System.currentTimeMillis() - startTime;

            AgentChatResponse result = AgentChatResponse.builder()
                    .content(response)
                    .traceId(traceId)
                    .sessionId(request.getSessionId())
                    .success(true)
                    .duration(duration)
                    .build();

            if (request.getDebugMode()) {
                result.setToolCalls(traceRecord != null ? traceRecord.getToolCalls() : null);
                result.setTraceRecord(traceRecord);
            }

            traceService.endTrace(traceId, true, null);

            return result;

        } catch (Exception e) {
            log.error("Agent同步聊天失败", e);
            if (traceId != null) {
                traceService.endTrace(traceId, false, e.getMessage());
            }
            return AgentChatResponse.builder()
                    .sessionId(request.getSessionId())
                    .success(false)
                    .errorMessage(e.getMessage())
                    .duration(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    /**
     * 流式Agent聊天（SSE）
     *
     * @param request 聊天请求
     * @return Flux流式响应
     */
    public Flux<AgentStreamResponse> streamChat(AgentChatRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        Sinks.Many<AgentStreamResponse> sink = Sinks.many().multicast().onBackpressureBuffer();

        new Thread(() -> {
            String traceId = null;
            try {
                traceId = traceService.startTrace(request.getSessionId(), userId, "enhanced-agent");
                traceService.setUserInput(traceId, request.getMessage());

                sink.tryEmitNext(AgentStreamResponse.builder()
                        .type("start")
                        .traceId(traceId)
                        .success(true)
                        .build());

                AgentConfig config = buildAgentConfig(request, userId);

                StringBuilder fullResponse = new StringBuilder();

                // 使用同步模型进行演示
                ChatLanguageModel chatModel = modelFactory.getChatModel(request.getModelCode());
                if (chatModel == null) {
                    throw new BusinessException("获取模型失败: " + request.getModelCode());
                }

                String response = chatModel.generate(request.getMessage());
                fullResponse.append(response);

                sink.tryEmitNext(AgentStreamResponse.builder()
                        .type("content")
                        .content(response)
                        .traceId(traceId)
                        .success(true)
                        .build());

                traceService.setAgentOutput(traceId, fullResponse.toString());
                sink.tryEmitNext(AgentStreamResponse.builder()
                        .type("end")
                        .content(fullResponse.toString())
                        .traceId(traceId)
                        .success(true)
                        .build());

                traceService.endTrace(traceId, true, null);
                sink.tryEmitComplete();

                Thread.sleep(100);

            } catch (Exception e) {
                log.error("Agent流式聊天失败", e);
                if (traceId != null) {
                    traceService.endTrace(traceId, false, e.getMessage());
                }
                sink.tryEmitNext(AgentStreamResponse.builder()
                        .type("error")
                        .errorMessage(e.getMessage())
                        .success(false)
                        .build());
                sink.tryEmitComplete();
            }
        }).start();

        return sink.asFlux()
                .doOnCancel(() -> log.debug("流式响应被取消"))
                .timeout(Duration.ofMinutes(5));
    }

    /**
     * 构建Agent配置
     *
     * @param request 聊天请求
     * @param userId  用户ID
     * @return Agent配置
     */
    private AgentConfig buildAgentConfig(AgentChatRequest request, Long userId) {
        String systemPrompt = request.getSystemPrompt() != null ? request.getSystemPrompt() : "你是一个有帮助的AI助手。";

        AgentConfig.AgentConfigBuilder builder = AgentConfig.builder()
                .agentName("enhanced-agent")
                .modelCode(request.getModelCode())
                .sessionId(request.getSessionId())
                .userId(userId)
                .systemPrompt(systemPrompt)
                .memoryStrategy(AgentConfig.MemoryStrategy.SHORT_TERM)
                .memoryWindowSize(10)
                .enableTracing(true);

        if (request.getToolNames() != null && !request.getToolNames().isEmpty()) {
            builder.tools(toolManager.getTools(request.getToolNames()));
        } else {
            builder.tools(toolManager.getAllTools());
        }

        return builder.build();
    }
}
