package com.enterprise.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 会话创建请求DTO
 * 用于创建新的会话
 */
@Data
@Schema(description = "会话创建请求参数")
public class SessionCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话标题（可选，不提供则自动生成）
     */
    @Schema(description = "会话标题", example = "关于Java编程的讨论")
    private String title;

    /**
     * 模型编码
     */
    @NotBlank(message = "模型编码不能为空")
    @Schema(description = "模型编码", example = "qwen-turbo", required = true)
    private String modelCode;
}
