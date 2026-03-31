package com.enterprise.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文档信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文档信息")
public class DocumentInfo {

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
     * 文档描述
     */
    @Schema(description = "文档描述")
    private String description;

    /**
     * 文件类型
     */
    @Schema(description = "文件类型")
    private String fileType;

    /**
     * 文件大小（字节）
     */
    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    /**
     * 分块数量
     */
    @Schema(description = "分块数量")
    private Integer chunkCount;

    /**
     * 使用的Embedding模型
     */
    @Schema(description = "使用的Embedding模型")
    private String embeddingModel;

    /**
     * 上传时间
     */
    @Schema(description = "上传时间")
    private LocalDateTime uploadTime;
}
