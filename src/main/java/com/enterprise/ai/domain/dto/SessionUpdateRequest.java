package com.enterprise.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 会话更新请求DTO
 * 用于更新会话信息
 */
@Data
@Schema(description = "会话更新请求参数")
public class SessionUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话标题
     */
    @NotBlank(message = "会话标题不能为空")
    @Schema(description = "会话标题", example = "更新后的会话标题", required = true)
    private String title;
}
