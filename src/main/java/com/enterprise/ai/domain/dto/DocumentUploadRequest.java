package com.enterprise.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 文档上传请求DTO
 */
@Data
@Schema(description = "文档上传请求")
public class DocumentUploadRequest {

    /**
     * 文档名称
     */
    @NotBlank(message = "文档名称不能为空")
    @Schema(description = "文档名称", required = true)
    private String documentName;

    /**
     * 文档描述
     */
    @Schema(description = "文档描述")
    private String description;

    /**
     * Embedding模型名称，为空则使用默认模型
     */
    @Schema(description = "Embedding模型名称")
    private String embeddingModel;
}
