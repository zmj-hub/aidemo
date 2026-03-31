package com.enterprise.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "模型信息")
public class ModelInfo {

    /**
     * 模型编码
     */
    @Schema(description = "模型编码")
    private String code;

    /**
     * 模型名称
     */
    @Schema(description = "模型名称")
    private String name;

    /**
     * 模型提供商
     */
    @Schema(description = "模型提供商")
    private String provider;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用")
    private Boolean enabled;

    /**
     * 是否健康
     */
    @Schema(description = "是否健康")
    private Boolean healthy;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

    /**
     * 最后健康检查时间
     */
    @Schema(description = "最后健康检查时间")
    private Long lastCheckTime;
}
