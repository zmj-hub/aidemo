package com.enterprise.ai.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档实体类
 */
@Data
public class Document implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文档ID
     */
    private String documentId;

    /**
     * 用户ID，用于用户数据隔离
     */
    private Long userId;

    /**
     * 文档名称
     */
    private String documentName;

    /**
     * 文档描述
     */
    private String description;

    /**
     * 文件类型：PDF、WORD、MARKDOWN、TEXT
     */
    private String fileType;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件存储路径
     */
    private String filePath;

    /**
     * 分块数量
     */
    private Integer chunkCount;

    /**
     * 使用的Embedding模型
     */
    private String embeddingModel;

    /**
     * 向量ID列表，用于删除时清理向量存储
     */
    private String embeddingIds;

    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
