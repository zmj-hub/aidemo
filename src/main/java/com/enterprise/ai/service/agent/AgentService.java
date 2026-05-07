package com.enterprise.ai.service.agent;

import cn.dev33.satoken.stp.StpUtil;
import com.enterprise.ai.common.exception.BusinessException;
import com.enterprise.ai.config.MemoryProperties;
import com.enterprise.ai.domain.dto.AgentChatRequest;
import com.enterprise.ai.domain.dto.AgentChatResponse;
import com.enterprise.ai.domain.dto.AgentStreamResponse;
import com.enterprise.ai.service.agent.memory.MemorySummarizer;
import com.enterprise.ai.service.agent.memory.RedisShortTermMemory;
import com.enterprise.ai.service.agent.tools.ToolManager;
import com.enterprise.ai.service.model.ModelFactory;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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

    @Autowired
    private RedisShortTermMemory redisShortTermMemory;

    @Autowired
    private MemoryProperties memoryProperties;

    @Autowired
    private MemorySummarizer memorySummarizer;

    /**
     * 获取当前用户ID，如果未登录则返回默认用户ID
     */
    private Long getCurrentUserId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            // 未登录时返回默认用户ID
            return 1L;
        }
    }

    public interface Agent {
        String chat(String userMessage);
    }

    public AgentChatResponse chat(AgentChatRequest request) {
        Long userId = getCurrentUserId();
        String traceId = null;
        long startTime = System.currentTimeMillis();

        try {
            traceId = traceService.startTrace(request.getSessionId(), userId, "enhanced-agent");
            traceService.setUserInput(traceId, request.getMessage());

            ChatModel chatModel = modelFactory.getChatModel(request.getModelCode());
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

    public Flux<AgentStreamResponse> streamChat(AgentChatRequest request) {
        Long userId = getCurrentUserId();
        String traceId = null;
        Sinks.Many<AgentStreamResponse> sink = Sinks.many().multicast().onBackpressureBuffer();

        try {
            traceId = traceService.startTrace(request.getSessionId(), userId, "enhanced-agent");
            traceService.setUserInput(traceId, request.getMessage());

            sink.tryEmitNext(AgentStreamResponse.builder()
                    .type("start")
                    .traceId(traceId)
                    .success(true)
                    .build());

            StreamingChatModel streamingModel = modelFactory.getStreamingModel(request.getModelCode());
            if (streamingModel == null) {
                throw new BusinessException("流式模型不可用: " + request.getModelCode());
            }

            final String finalTraceId = traceId;
            StringBuilder fullResponse = new StringBuilder();

            streamingModel.chat(request.getMessage(), new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponse) {
                    fullResponse.append(partialResponse);
                    sink.tryEmitNext(AgentStreamResponse.builder()
                            .type("content")
                            .content(partialResponse)
                            .traceId(finalTraceId)
                            .success(true)
                            .build());
                }

                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
                    traceService.setAgentOutput(finalTraceId, fullResponse.toString());
                    sink.tryEmitNext(AgentStreamResponse.builder()
                            .type("end")
                            .content(fullResponse.toString())
                            .traceId(finalTraceId)
                            .success(true)
                            .build());
                    traceService.endTrace(finalTraceId, true, null);
                    sink.tryEmitComplete();
                }

                @Override
                public void onError(Throwable error) {
                    log.error("Agent流式聊天失败", error);
                    traceService.endTrace(finalTraceId, false, error.getMessage());
                    sink.tryEmitNext(AgentStreamResponse.builder()
                            .type("error")
                            .errorMessage(error.getMessage())
                            .success(false)
                            .build());
                    sink.tryEmitComplete();
                }
            });

        } catch (Exception e) {
            log.error("Agent流式聊天启动失败", e);
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

        return sink.asFlux()
                .doOnCancel(() -> log.debug("流式响应被取消"))
                .timeout(Duration.ofMinutes(5));
    }

    private AgentConfig buildAgentConfig(AgentChatRequest request, Long userId) {
        String systemPrompt = request.getSystemPrompt() != null ? request.getSystemPrompt() : "你是一个有帮助的AI助手。";
        Integer defaultWindowSize = memoryProperties.getWindowSize() != null ? memoryProperties.getWindowSize() : 10;

        AgentConfig.AgentConfigBuilder builder = AgentConfig.builder()
                .agentName("enhanced-agent")
                .modelCode(request.getModelCode())
                .sessionId(request.getSessionId())
                .userId(userId)
                .systemPrompt(systemPrompt)
                .memoryStrategy(request.getMemoryStrategy() != null ? request.getMemoryStrategy() : AgentConfig.MemoryStrategy.SHORT_TERM)
                .memoryWindowSize(defaultWindowSize)
                .enableTracing(true);

        if (request.getToolNames() != null && !request.getToolNames().isEmpty()) {
            builder.tools(toolManager.getTools(request.getToolNames()));
        } else {
            builder.tools(toolManager.getAllTools());
        }

        return builder.build();
    }
}
