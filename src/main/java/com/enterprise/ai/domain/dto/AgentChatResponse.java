package com.enterprise.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Agent聊天响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent聊天响应")
public class AgentChatResponse {

    /**
     * 响应内容
     */
    @Schema(description = "响应内容")
    private String content;

    /**
     * 追踪ID
     */
    @Schema(description = "追踪ID")
    private String traceId;

    /**
     * 会话ID
     */
    @Schema(description = "会话ID")
    private String sessionId;

    /**
     * 是否成功
     */
    @Schema(description = "是否成功")
    private Boolean success;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 请求耗时（毫秒）
     */
    @Schema(description = "请求耗时（毫秒）")
    private Long duration;

    /**
     * 输入token数
     */
    @Schema(description = "输入token数")
    private Integer promptTokens;

    /**
     * 输出token数
     */
    @Schema(description = "输出token数")
    private Integer completionTokens;

    /**
     * 总token数
     */
    @Schema(description = "总token数")
    private Integer totalTokens;

    /**
     * 工具调用记录（调试模式返回）
     */
    @Schema(description = "工具调用记录")
    private List<Map<String, Object>> toolCalls;

    /**
     * 完整追踪信息（调试模式返回）
     */
    @Schema(description = "完整追踪信息")
    private Map<String, Object> traceRecord;
}
