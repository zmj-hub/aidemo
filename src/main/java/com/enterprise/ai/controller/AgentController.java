package com.enterprise.ai.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.enterprise.ai.common.result.Result;
import com.enterprise.ai.domain.dto.AgentChatRequest;
import com.enterprise.ai.domain.dto.AgentChatResponse;
import com.enterprise.ai.domain.dto.AgentStreamResponse;
import com.enterprise.ai.service.agent.AgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * Agent控制器
 * 提供Agent同步对话、SSE流式对话接口，支持调试模式返回完整思考链
 */
@Tag(name = "Agent管理模块", description = "Agent对话相关接口")
@RestController
@RequestMapping("/agent")
public class AgentController {

    @Autowired
    private AgentService agentService;

    /**
     * 同步Agent聊天接口
     *
     * @param request 聊天请求
     * @return 聊天响应
     */
    @Operation(summary = "Agent同步聊天", description = "使用Agent进行同步对话，支持调试模式返回完整思考链")
    @SaCheckLogin
    @PostMapping("/chat")
    public Result<AgentChatResponse> chat(@Valid @RequestBody AgentChatRequest request) {
        AgentChatResponse response = agentService.chat(request);
        if (response.getSuccess()) {
            return Result.success(response);
        } else {
            return Result.error(response.getErrorMessage());
        }
    }

    /**
     * SSE流式Agent聊天接口
     *
     * @param request 聊天请求
     * @return SSE流式响应
     */
    @Operation(summary = "Agent流式聊天（SSE）", description = "使用Agent进行SSE流式对话，支持调试模式返回完整思考链")
    @SaCheckLogin
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AgentStreamResponse> streamChat(@Valid @RequestBody AgentChatRequest request) {
        return agentService.streamChat(request);
    }
}
