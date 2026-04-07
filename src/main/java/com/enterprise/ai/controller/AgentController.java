package com.enterprise.ai.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.enterprise.ai.common.result.Result;
import com.enterprise.ai.domain.dto.AgentChatRequest;
import com.enterprise.ai.domain.dto.AgentChatResponse;
import com.enterprise.ai.domain.dto.AgentStreamResponse;
import com.enterprise.ai.service.agent.AgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Agent管理模块", description = "Agent对话相关接口 - 提供智能Agent对话功能，支持同步和流式两种模式")
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
    @Operation(
        summary = "Agent同步聊天", 
        description = "使用Agent进行同步对话，支持工具调用、记忆管理、推理追踪。开启debugMode可返回完整思考链和工具调用记录",
        operationId = "agentChat"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "对话成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Result.class),
                examples = @ExampleObject(value = "{\"code\":200,\"message\":\"操作成功\",\"data\":{\"content\":\"你好！我是AI助手\",\"traceId\":\"trace-123\",\"sessionId\":\"session-123\",\"success\":true,\"duration\":1500}}")
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "未登录",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "服务器内部错误",
            content = @Content(mediaType = "application/json")
        )
    })
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
    @Operation(
        summary = "Agent流式聊天（SSE）", 
        description = "使用Agent进行SSE流式对话，实时返回响应内容。支持工具调用、记忆管理。流式响应包含start、content、end、error四种类型的消息",
        operationId = "agentStreamChat"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "流式响应",
            content = @Content(
                mediaType = "text/event-stream",
                examples = @ExampleObject(value = "data: {\"type\":\"start\",\"traceId\":\"trace-123\",\"success\":true}\n\ndata: {\"type\":\"content\",\"content\":\"你\",\"traceId\":\"trace-123\",\"success\":true}\n\ndata: {\"type\":\"end\",\"content\":\"你好！\",\"traceId\":\"trace-123\",\"success\":true}")
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "未登录",
            content = @Content(mediaType = "application/json")
        )
    })
    @SaCheckLogin
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AgentStreamResponse> streamChat(@Valid @RequestBody AgentChatRequest request) {
        return agentService.streamChat(request);
    }
}
