package com.enterprise.ai.domain.dto;

import com.enterprise.ai.service.agent.AgentConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * Agent聊天请求DTO
 */
@Data
@Schema(description = "Agent聊天请求参数")
public class AgentChatRequest {

    /**
     * 会话ID
     */
    @NotBlank(message = "会话ID不能为空")
    @Schema(description = "会话ID", example = "session-123")
    private String sessionId;

    /**
     * 模型编码
     */
    @NotBlank(message = "模型编码不能为空")
    @Schema(description = "模型编码", example = "qwen-turbo")
    private String modelCode;

    /**
     * 用户消息
     */
    @NotBlank(message = "用户消息不能为空")
    @Schema(description = "用户消息", example = "你好，请介绍一下自己")
    private String message;

    /**
     * 系统提示词
     */
    @Schema(description = "系统提示词")
    private String systemPrompt;

    /**
     * 工具名称列表（可选，不传则使用所有可用工具）
     */
    @Schema(description = "工具名称列表")
    private List<String> toolNames;

    /**
     * 是否启用调试模式（返回完整思考链）
     */
    @Schema(description = "是否启用调试模式", example = "false")
    private Boolean debugMode = false;

    /**
     * 温度参数
     */
    @Schema(description = "温度参数", example = "0.7")
    private Double temperature = 0.7;

    /**
     * 最大生成token数
     */
    @Schema(description = "最大生成token数", example = "2000")
    private Integer maxTokens = 2000;

    /**
     * 记忆策略
     */
    @Schema(description = "记忆策略: SHORT_TERM(短期记忆), LONG_TERM(长期记忆), HYBRID(混合记忆), NONE(无记忆)", example = "SHORT_TERM")
    private AgentConfig.MemoryStrategy memoryStrategy = AgentConfig.MemoryStrategy.SHORT_TERM;
}
