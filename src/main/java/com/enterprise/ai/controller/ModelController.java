package com.enterprise.ai.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.enterprise.ai.common.result.Result;
import com.enterprise.ai.domain.dto.ChatRequest;
import com.enterprise.ai.domain.dto.ChatResponse;
import com.enterprise.ai.domain.dto.ModelInfo;
import com.enterprise.ai.domain.dto.ModelHealthCheckRequest;
import com.enterprise.ai.service.model.ChatModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 模型控制器
 * 提供模型列表查询、单模型/批量健康检查接口
 */
@Tag(name = "模型管理模块", description = "模型管理相关接口")
@RestController
@RequestMapping("/model")
public class ModelController {

    @Autowired
    private ChatModelService chatModelService;

    /**
     * 获取当前用户可用的模型列表
     * 
     * @return 模型信息列表
     */
    @Operation(summary = "获取可用模型列表", description = "获取当前登录用户可用的模型列表")
    @SaCheckLogin
    @GetMapping("/list")
    public Result<List<ModelInfo>> getModelList() {
        List<ModelInfo> models = chatModelService.getAvailableModels();
        return Result.success(models);
    }

    /**
     * 同步聊天接口
     * 
     * @param request 聊天请求
     * @return 聊天响应
     */
    @Operation(summary = "同步聊天", description = "使用指定模型进行同步聊天")
    @SaCheckLogin
    @PostMapping("/chat")
    public Result<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatModelService.chat(request);
        if (response.getSuccess()) {
            return Result.success(response);
        } else {
            return Result.error(response.getErrorMessage());
        }
    }

    /**
     * 单个模型健康检查
     * 
     * @param modelCode 模型编码
     * @return 健康检查结果
     */
    @Operation(summary = "单个模型健康检查", description = "检查指定模型是否健康")
    @SaCheckLogin
    @GetMapping("/health/{modelCode}")
    public Result<Boolean> healthCheck(@PathVariable String modelCode) {
        boolean healthy = chatModelService.healthCheck(modelCode);
        return Result.success(healthy);
    }

    /**
     * 批量模型健康检查
     * 
     * @param request 健康检查请求
     * @return 健康检查结果Map
     */
    @Operation(summary = "批量模型健康检查", description = "批量检查多个模型的健康状态")
    @SaCheckLogin
    @PostMapping("/health/batch")
    public Result<Map<String, Boolean>> batchHealthCheck(@Valid @RequestBody ModelHealthCheckRequest request) {
        Map<String, Boolean> result = chatModelService.batchHealthCheck(request.getModelCodes());
        return Result.success(result);
    }
}
