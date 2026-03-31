package com.enterprise.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "聊天响应")
public class ChatResponse {

    /**
     * 模型编码
     */
    @Schema(description = "模型编码")
    private String modelCode;

    /**
     * 响应内容
     */
    @Schema(description = "响应内容")
    private String content;

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
     * 请求耗时（毫秒）
     */
    @Schema(description = "请求耗时（毫秒）")
    private Long duration;

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
