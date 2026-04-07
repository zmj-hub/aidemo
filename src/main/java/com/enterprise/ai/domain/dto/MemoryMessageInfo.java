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
@Schema(description = "记忆消息信息")
public class MemoryMessageInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "消息类型", example = "USER")
    private String type;

    @Schema(description = "消息内容", example = "你好，帮我写一段代码")
    private String content;

    @Schema(description = "时间戳", example = "2024-01-01T12:00:00")
    private LocalDateTime timestamp;
}
