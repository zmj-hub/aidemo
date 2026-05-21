package com.enterprise.ai.service.agent;

import cn.dev33.satoken.stp.StpUtil;
import com.enterprise.ai.common.exception.BusinessException;
import com.enterprise.ai.domain.dto.AgentChatRequest;
import com.enterprise.ai.domain.dto.AgentChatResponse;
import com.enterprise.ai.domain.dto.AgentStreamResponse;
import com.enterprise.ai.service.model.ChatModelService;
import io.agentscope.core.agent.EventType;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.agent.StreamOptions;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.session.Session;
import io.agentscope.core.state.SimpleSessionKey;
import io.agentscope.harness.agent.HarnessAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentFactory agentFactory;
    private final ChatModelService chatModelService;
    private final Session jsonSession;

    private Long getCurrentUserId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            return 1L;
        }
    }

    public AgentChatResponse chat(AgentChatRequest request) {
        long startTime = System.currentTimeMillis();
        String traceId = UUID.randomUUID().toString();

        try {
            String modelId = resolveModelId(request.getModelCode());
            if (!chatModelService.isModelAvailable(modelId)) {
                throw new BusinessException("模型不可用: " + modelId);
            }

            HarnessAgent agent = agentFactory.createAgent(
                    request.getSessionId(), modelId,
                    request.getSystemPrompt(), request.getToolNames());

            Msg userMsg = Msg.builder()
                    .role(MsgRole.USER)
                    .textContent(request.getMessage())
                    .build();

            RuntimeContext ctx = RuntimeContext.builder()
                    .sessionId(request.getSessionId())
                    .userId(String.valueOf(getCurrentUserId()))
                    .build();

            Msg response = agent.call(userMsg, ctx).block();
            String responseText = response != null ? response.getTextContent() : "";

            agent.saveTo(jsonSession, SimpleSessionKey.of(request.getSessionId()));

            long duration = System.currentTimeMillis() - startTime;

            AgentChatResponse result = AgentChatResponse.builder()
                    .content(responseText)
                    .traceId(traceId)
                    .sessionId(request.getSessionId())
                    .success(true)
                    .duration(duration)
                    .build();

            if (Boolean.TRUE.equals(request.getDebugMode())) {
                Map<String, Object> traceInfo = new LinkedHashMap<>();
                traceInfo.put("traceId", traceId);
                traceInfo.put("duration", duration);
                result.setTraceRecord(traceInfo);
            }

            return result;

        } catch (Exception e) {
            log.error("Agent同步聊天失败", e);
            long duration = System.currentTimeMillis() - startTime;
            return AgentChatResponse.builder()
                    .sessionId(request.getSessionId())
                    .success(false)
                    .errorMessage(e.getMessage())
                    .duration(duration)
                    .build();
        }
    }

    public Flux<AgentStreamResponse> streamChat(AgentChatRequest request) {
        Sinks.Many<AgentStreamResponse> sink = Sinks.many().multicast().onBackpressureBuffer();
        String traceId = UUID.randomUUID().toString();

        try {
            String modelId = resolveModelId(request.getModelCode());
            if (!chatModelService.isModelAvailable(modelId)) {
                throw new BusinessException("模型不可用: " + modelId);
            }

            HarnessAgent agent = agentFactory.createAgent(
                    request.getSessionId(), modelId,
                    request.getSystemPrompt(), request.getToolNames());

            Msg userMsg = Msg.builder()
                    .role(MsgRole.USER)
                    .textContent(request.getMessage())
                    .build();

            RuntimeContext ctx = RuntimeContext.builder()
                    .sessionId(request.getSessionId())
                    .userId(String.valueOf(getCurrentUserId()))
                    .build();

            sink.tryEmitNext(AgentStreamResponse.builder()
                    .type("start")
                    .traceId(traceId)
                    .success(true)
                    .build());

            agent.stream(List.of(userMsg),
                            StreamOptions.builder()
                                    .eventTypes(EventType.REASONING, EventType.TOOL_RESULT)
                                    .incremental(false)
                                    .build(),
                            ctx)
                    .doOnNext(event -> {
                        String type = mapEventType(event.getType());
                        String content = event.getMessage() != null
                                ? event.getMessage().getTextContent()
                                : "";
                        sink.tryEmitNext(AgentStreamResponse.builder()
                                .type(type)
                                .content(content)
                                .traceId(traceId)
                                .success(true)
                                .build());
                    })
                    .doOnComplete(() -> {
                        agent.saveTo(jsonSession, SimpleSessionKey.of(request.getSessionId()));
                        sink.tryEmitNext(AgentStreamResponse.builder()
                                .type("end")
                                .content("")
                                .traceId(traceId)
                                .success(true)
                                .build());
                        sink.tryEmitComplete();
                    })
                    .doOnError(error -> {
                        log.error("Agent流式聊天失败", error);
                        sink.tryEmitNext(AgentStreamResponse.builder()
                                .type("error")
                                .errorMessage(error.getMessage())
                                .success(false)
                                .build());
                        sink.tryEmitComplete();
                    })
                    .subscribe();

        } catch (Exception e) {
            log.error("Agent流式聊天启动失败", e);
            sink.tryEmitNext(AgentStreamResponse.builder()
                    .type("error")
                    .errorMessage(e.getMessage())
                    .success(false)
                    .build());
            sink.tryEmitComplete();
        }

        return sink.asFlux();
    }

    private String resolveModelId(String modelCode) {
        if (modelCode != null && modelCode.contains(":")) {
            return modelCode;
        }
        return "modelscope:" + modelCode;
    }

    private String mapEventType(EventType eventType) {
        return switch (eventType) {
            case REASONING -> "content";
            case TOOL_RESULT -> "tool";
            case HINT -> "content";
            case SUMMARY -> "content";
            case AGENT_RESULT -> "end";
            default -> "content";
        };
    }
}
