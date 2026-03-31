package com.enterprise.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话信息DTO
 * 用于返回会话的详细信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "会话信息")
public class SessionInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    @Schema(description = "会话ID", example = "1")
    private Long id;

    /**
     * 会话标题
     */
    @Schema(description = "会话标题", example = "关于Java编程的讨论")
    private String title;

    /**
     * 模型编码
     */
    @Schema(description = "模型编码", example = "qwen-turbo")
    private String modelCode;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    /**
     * 是否已归档
     */
    @Schema(description = "是否已归档", example = "false")
    private Boolean archived;

    /**
     * 最后消息时间
     */
    @Schema(description = "最后消息时间", example = "2024-01-01T12:00:00")
    private LocalDateTime lastMessageTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-01-01T10:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2024-01-01T12:00:00")
    private LocalDateTime updateTime;
}
