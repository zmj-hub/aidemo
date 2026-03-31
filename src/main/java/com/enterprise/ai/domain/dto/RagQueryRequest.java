package com.enterprise.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * RAG查询请求DTO
 */
@Data
@Schema(description = "RAG查询请求")
public class RagQueryRequest {

    /**
     * 用户查询问题
     */
    @NotBlank(message = "查询问题不能为空")
    @Schema(description = "用户查询问题", required = true)
    private String query;

    /**
     * Chat模型名称，为空则使用默认模型
     */
    @Schema(description = "Chat模型名称")
    private String chatModel;

    /**
     * Embedding模型名称，为空则使用默认模型
     */
    @Schema(description = "Embedding模型名称")
    private String embeddingModel;

    /**
     * 检索返回的最大文档数，为空则使用默认值
     */
    @Schema(description = "检索返回的最大文档数")
    private Integer maxResults;

    /**
     * 最小相似度阈值，为空则使用默认值
     */
    @Schema(description = "最小相似度阈值")
    private Double minScore;
}
