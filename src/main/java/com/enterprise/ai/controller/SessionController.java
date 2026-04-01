package com.enterprise.ai.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.enterprise.ai.common.result.Result;
import com.enterprise.ai.domain.dto.SessionCreateRequest;
import com.enterprise.ai.domain.dto.SessionInfo;
import com.enterprise.ai.domain.dto.SessionUpdateRequest;
import com.enterprise.ai.service.agent.session.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会话控制器
 * 提供会话管理相关接口，包括创建、查询、更新、归档、删除等
 */
@Tag(name = "会话模块", description = "会话管理相关接口")
@RestController
@RequestMapping("/sessions")
@SaCheckLogin
public class SessionController {

    @Autowired
    private SessionService sessionService;

    /**
     * 创建新会话接口
     * 
     * @param request 会话创建请求参数
     * @return 创建的会话信息
     */
    @Operation(summary = "创建会话", description = "创建一个新的AI会话")
    @PostMapping
    public Result<SessionInfo> createSession(@Valid @RequestBody SessionCreateRequest request) {
        SessionInfo sessionInfo = sessionService.createSession(request);
        return Result.success("会话创建成功", sessionInfo);
    }

    /**
     * 获取会话列表接口
     * 
     * @param includeArchived 是否包含已归档的会话，默认为false
     * @return 会话信息列表
     */
    @Operation(summary = "获取会话列表", description = "获取当前用户的所有会话列表")
    @GetMapping
    public Result<List<SessionInfo>> getSessionList(
            @Parameter(description = "是否包含已归档的会话")
            @RequestParam(defaultValue = "false") boolean includeArchived) {
        List<SessionInfo> sessionList = sessionService.getSessionList(includeArchived);
        return Result.success(sessionList);
    }

    /**
     * 获取会话详情接口
     * 
     * @param sessionId 会话ID
     * @return 会话详细信息
     */
    @Operation(summary = "获取会话详情", description = "根据会话ID获取会话的详细信息")
    @GetMapping("/{sessionId}")
    public Result<SessionInfo> getSessionById(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long sessionId) {
        SessionInfo sessionInfo = sessionService.getSessionById(sessionId);
        return Result.success(sessionInfo);
    }

    /**
     * 更新会话接口
     * 
     * @param sessionId 会话ID
     * @param request 会话更新请求参数
     * @return 更新后的会话信息
     */
    @Operation(summary = "更新会话", description = "更新会话的信息，如标题等")
    @PutMapping("/{sessionId}")
    public Result<SessionInfo> updateSession(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long sessionId,
            @Valid @RequestBody SessionUpdateRequest request) {
        SessionInfo sessionInfo = sessionService.updateSession(sessionId, request);
        return Result.success("会话更新成功", sessionInfo);
    }

    /**
     * 归档会话接口
     * 
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @Operation(summary = "归档会话", description = "将会话归档，归档后的会话默认不显示在列表中")
    @PostMapping("/{sessionId}/archive")
    public Result<String> archiveSession(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long sessionId) {
        sessionService.archiveSession(sessionId);
        return Result.success("会话归档成功");
    }

    /**
     * 取消归档会话接口
     * 
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @Operation(summary = "取消归档会话", description = "取消会话的归档状态，使其恢复到正常列表中")
    @PostMapping("/{sessionId}/unarchive")
    public Result<String> unarchiveSession(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long sessionId) {
        sessionService.unarchiveSession(sessionId);
        return Result.success("会话取消归档成功");
    }

    /**
     * 删除会话接口
     * 
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @Operation(summary = "删除会话", description = "永久删除会话及其相关数据")
    @DeleteMapping("/{sessionId}")
    public Result<String> deleteSession(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long sessionId) {
        sessionService.deleteSession(sessionId);
        return Result.success("会话删除成功");
    }
}
