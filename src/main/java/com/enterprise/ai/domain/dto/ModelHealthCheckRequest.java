package com.enterprise.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 模型健康检查请求DTO
 */
@Data
@Schema(description = "模型健康检查请求")
public class ModelHealthCheckRequest {

    /**
     * 模型编码列表，为空时检查所有模型
     */
    @Schema(description = "模型编码列表")
    private List<String> modelCodes;

    /**
     * 健康检查超时时间（毫秒）
     */
    @Schema(description = "超时时间（毫秒）", example = "5000")
    private Integer timeout = 5000;
}
