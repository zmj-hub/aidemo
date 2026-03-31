package com.enterprise.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent流式响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent流式响应")
public class AgentStreamResponse {

    /**
     * 响应类型：start（开始）、content（内容）、tool（工具调用）、end（结束）、error（错误）
     */
    @Schema(description = "响应类型", example = "content")
    private String type;

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
     * 工具名称（工具调用类型时返回）
     */
    @Schema(description = "工具名称")
    private String toolName;

    /**
     * 工具参数（工具调用类型时返回）
     */
    @Schema(description = "工具参数")
    private Object toolParams;

    /**
     * 工具执行结果（工具调用类型时返回）
     */
    @Schema(description = "工具执行结果")
    private Object toolResult;

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
}
