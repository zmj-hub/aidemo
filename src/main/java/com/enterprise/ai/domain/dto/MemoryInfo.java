package com.enterprise.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "记忆信息")
public class MemoryInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "记忆ID（会话ID）", example = "1")
    private Long sessionId;

    @Schema(description = "会话标题", example = "关于Java编程的讨论")
    private String sessionTitle;

    @Schema(description = "消息数量", example = "10")
    private Integer messageCount;

    @Schema(description = "创建时间", example = "2024-01-01T10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "最后更新时间", example = "2024-01-01T12:00:00")
    private LocalDateTime updateTime;
}
