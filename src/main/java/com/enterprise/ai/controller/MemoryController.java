package com.enterprise.ai.controller;

import com.enterprise.ai.common.result.Result;
import com.enterprise.ai.domain.dto.MemoryInfo;
import com.enterprise.ai.domain.dto.MemoryMessageInfo;
import com.enterprise.ai.domain.dto.MemorySearchRequest;
import com.enterprise.ai.domain.dto.MemorySearchResult;
import com.enterprise.ai.service.memory.MemoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "记忆管理模块", description = "会话记忆管理相关接口")
@RestController
@RequestMapping("/memory")
@RequiredArgsConstructor
public class MemoryController {

    private final MemoryService memoryService;

    @Operation(summary = "获取记忆列表")
    @GetMapping
    public Result<List<MemoryInfo>> list() {
        return Result.success(memoryService.getMemoryList());
    }

    @Operation(summary = "获取会话记忆详情")
    @GetMapping("/session/{id}")
    public Result<List<MemoryMessageInfo>> detail(@PathVariable String id) {
        return Result.success(memoryService.getSessionMemory(id));
    }

    @Operation(summary = "搜索记忆")
    @PostMapping("/search")
    public Result<List<MemorySearchResult>> search(@RequestBody MemorySearchRequest request) {
        return Result.success(memoryService.searchMemory(request));
    }

    @Operation(summary = "删除会话记忆")
    @DeleteMapping("/session/{id}")
    public Result<Void> deleteSession(@PathVariable String id) {
        memoryService.deleteSessionMemory(id);
        return Result.success();
    }

    @Operation(summary = "清除会话记忆")
    @PostMapping("/session/{id}/clear")
    public Result<Void> clearSession(@PathVariable String id) {
        memoryService.clearSessionMemory(id);
        return Result.success();
    }

    @Operation(summary = "清除所有记忆")
    @DeleteMapping("/clear-all")
    public Result<Void> clearAll() {
        memoryService.clearAllMemory();
        return Result.success();
    }
}
