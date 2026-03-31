package com.enterprise.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RAG查询响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "RAG查询响应")
public class RagQueryResponse {

    /**
     * AI生成的回答
     */
    @Schema(description = "AI生成的回答")
    private String answer;

    /**
     * 使用的Chat模型
     */
    @Schema(description = "使用的Chat模型")
    private String chatModel;

    /**
     * 使用的Embedding模型
     */
    @Schema(description = "使用的Embedding模型")
    private String embeddingModel;

    /**
     * 溯源文档列表
     */
    @Schema(description = "溯源文档列表")
    private List<SourceDocument> sources;

    /**
     * 溯源文档内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "溯源文档")
    public static class SourceDocument {

        /**
         * 文档ID
         */
        @Schema(description = "文档ID")
        private String documentId;

        /**
         * 文档名称
         */
        @Schema(description = "文档名称")
        private String documentName;

        /**
         * 相关内容片段
         */
        @Schema(description = "相关内容片段")
        private String content;

        /**
         * 相似度分数
         */
        @Schema(description = "相似度分数")
        private Double score;
    }
}
