package com.enterprise.ai.controller;

import com.enterprise.ai.common.result.Result;
import com.enterprise.ai.domain.dto.ChatRequest;
import com.enterprise.ai.domain.dto.ChatResponse;
import com.enterprise.ai.service.model.ChatModelService;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.model.Model;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "模型管理模块", description = "AI模型管理相关接口")
@RestController
@RequestMapping("/model")
@RequiredArgsConstructor
public class ModelController {

    private final ChatModelService chatModelService;

    @Operation(summary = "获取可用模型列表")
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        return Result.success(chatModelService.getAvailableModels());
    }

    @Operation(summary = "直接调用模型（非Agent模式）")
    @PostMapping("/chat")
    public Result<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            Model model = chatModelService.resolveModel(request.getModelCode());
            String message = request.getMessage();
            if (message == null && request.getMessages() != null) {
                message = request.getMessages().stream()
                        .filter(m -> "user".equals(m.getRole()))
                        .map(ChatRequest.ChatMessage::getContent)
                        .reduce((a, b) -> b)
                        .orElse("Hello");
            }

            Msg userMsg = Msg.builder()
                    .role(MsgRole.USER)
                    .textContent(message != null ? message : "Hello")
                    .build();

            String responseText = model.stream(List.of(userMsg), List.of(), null)
                    .collectList()
                    .block()
                    .stream()
                    .map(r -> {
                        if (!r.getContent().isEmpty() && r.getContent().get(0) instanceof io.agentscope.core.message.TextBlock tb) {
                            return tb.getText();
                        }
                        return "";
                    })
                    .reduce("", (a, b) -> a + b);
            long duration = System.currentTimeMillis() - startTime;

            ChatResponse result = ChatResponse.builder()
                    .modelCode(request.getModelCode())
                    .content(responseText)
                    .duration(duration)
                    .success(true)
                    .build();
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "模型健康检查（单个）")
    @GetMapping("/health/{modelCode}")
    public Result<Map<String, Object>> health(@PathVariable String modelCode) {
        return Result.success(chatModelService.healthCheck(modelCode));
    }

    @Operation(summary = "批量模型健康检查")
    @PostMapping("/health/batch")
    public Result<List<Map<String, Object>>> healthBatch(@RequestBody Map<String, List<String>> request) {
        List<String> modelCodes = request.getOrDefault("modelCodes", List.of());
        return Result.success(chatModelService.batchHealthCheck(modelCodes));
    }
}
