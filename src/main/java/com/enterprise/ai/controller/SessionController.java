package com.enterprise.ai.controller;

import com.enterprise.ai.common.result.Result;
import com.enterprise.ai.domain.dto.SessionCreateRequest;
import com.enterprise.ai.domain.dto.SessionInfo;
import com.enterprise.ai.domain.dto.SessionUpdateRequest;
import com.enterprise.ai.service.session.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "会话管理模块", description = "会话CRUD相关接口")
@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @Operation(summary = "创建新会话")
    @PostMapping
    public Result<SessionInfo> create(@Valid @RequestBody SessionCreateRequest request) {
        return Result.success(sessionService.createSession(request));
    }

    @Operation(summary = "获取会话列表")
    @GetMapping
    public Result<List<SessionInfo>> list(
            @RequestParam(required = false, defaultValue = "false") boolean includeArchived) {
        return Result.success(sessionService.getSessionList(includeArchived));
    }

    @Operation(summary = "获取会话详情")
    @GetMapping("/{id}")
    public Result<SessionInfo> detail(@PathVariable String id) {
        return Result.success(sessionService.getSessionById(id));
    }

    @Operation(summary = "更新会话")
    @PutMapping("/{id}")
    public Result<SessionInfo> update(@PathVariable String id, @Valid @RequestBody SessionUpdateRequest request) {
        return Result.success(sessionService.updateSession(id, request));
    }

    @Operation(summary = "归档会话")
    @PostMapping("/{id}/archive")
    public Result<Void> archive(@PathVariable String id) {
        sessionService.archiveSession(id);
        return Result.success();
    }

    @Operation(summary = "取消归档")
    @PostMapping("/{id}/unarchive")
    public Result<Void> unarchive(@PathVariable String id) {
        sessionService.unarchiveSession(id);
        return Result.success();
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        sessionService.deleteSession(id);
        return Result.success();
    }
}
