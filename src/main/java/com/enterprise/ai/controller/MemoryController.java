package com.enterprise.ai.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.enterprise.ai.common.result.Result;
import com.enterprise.ai.domain.dto.*;
import com.enterprise.ai.service.agent.MemoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "记忆模块", description = "会话记忆管理相关接口 - 提供会话记忆的查看、搜索、删除等功能")
@RestController
@RequestMapping("/memory")
// @SaCheckLogin // 暂时注释，允许无需登录访问
public class MemoryController {

    @Autowired
    private MemoryService memoryService;

    @Operation(
        summary = "获取所有会话记忆列表", 
        description = "获取当前用户所有会话的记忆概览信息",
        operationId = "getMemoryList"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "获取成功",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"code\":200,\"message\":\"操作成功\",\"data\":[{\"sessionId\":1,\"title\":\"会话标题\",\"messageCount\":10,\"lastMessageTime\":\"2024-01-01T00:00:00\"}]}")
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "未登录",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping
    public Result<List<MemoryInfo>> getMemoryList() {
        List<MemoryInfo> memoryList = memoryService.getMemoryList();
        return Result.success(memoryList);
    }

    @Operation(
        summary = "获取会话记忆详情", 
        description = "根据会话ID获取该会话的所有记忆消息",
        operationId = "getSessionMemory"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "获取成功",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"code\":200,\"message\":\"操作成功\",\"data\":[{\"role\":\"user\",\"content\":\"你好\",\"timestamp\":\"2024-01-01T00:00:00\"}]}")
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "未登录",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/session/{sessionId}")
    public Result<List<MemoryMessageInfo>> getSessionMemory(
            @Parameter(description = "会话ID", required = true, example = "1")
            @PathVariable Long sessionId) {
        List<MemoryMessageInfo> memoryMessages = memoryService.getSessionMemory(sessionId);
        return Result.success(memoryMessages);
    }

    @Operation(
        summary = "搜索记忆", 
        description = "在当前用户的会话记忆中搜索关键词，支持语义搜索",
        operationId = "searchMemory"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "搜索成功",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"code\":200,\"message\":\"操作成功\",\"data\":[{\"sessionId\":1,\"messageId\":\"相关消息内容\",\"similarity\":0.95}]}")
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "未登录",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/search")
    public Result<List<MemorySearchResult>> searchMemory(
            @Valid @RequestBody MemorySearchRequest request) {
        List<MemorySearchResult> searchResults = memoryService.searchMemory(request);
        return Result.success(searchResults);
    }

    @Operation(
        summary = "删除会话记忆", 
        description = "删除指定会话的所有记忆数据",
        operationId = "deleteSessionMemory"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "删除成功",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"code\":200,\"message\":\"会话记忆删除成功\",\"data\":null}")
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "未登录",
            content = @Content(mediaType = "application/json")
        )
    })
    @DeleteMapping("/session/{sessionId}")
    public Result<String> deleteSessionMemory(
            @Parameter(description = "会话ID", required = true, example = "1")
            @PathVariable Long sessionId) {
        memoryService.deleteSessionMemory(sessionId);
        return Result.success("会话记忆删除成功");
    }

    @Operation(
        summary = "清空会话记忆", 
        description = "清空指定会话的所有记忆数据（等同于删除）",
        operationId = "clearSessionMemory"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "清空成功",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"code\":200,\"message\":\"会话记忆清空成功\",\"data\":null}")
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "未登录",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/session/{sessionId}/clear")
    public Result<String> clearSessionMemory(
            @Parameter(description = "会话ID", required = true, example = "1")
            @PathVariable Long sessionId) {
        memoryService.clearSessionMemory(sessionId);
        return Result.success("会话记忆清空成功");
    }

    @Operation(
        summary = "清空所有记忆", 
        description = "清空当前用户所有会话的记忆数据，此操作不可恢复",
        operationId = "clearAllMemory"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "清空成功",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"code\":200,\"message\":\"所有会话记忆清空成功\",\"data\":null}")
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "未登录",
            content = @Content(mediaType = "application/json")
        )
    })
    @DeleteMapping("/clear-all")
    public Result<String> clearAllMemory() {
        memoryService.clearAllMemory();
        return Result.success("所有会话记忆清空成功");
    }
}
