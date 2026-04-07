package com.enterprise.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "记忆搜索结果")
public class MemorySearchResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会话ID", example = "1")
    private Long sessionId;

    @Schema(description = "会话标题", example = "关于Java编程的讨论")
    private String sessionTitle;

    @Schema(description = "匹配的消息列表")
    private List<MemoryMessageInfo> matchedMessages;

    @Schema(description = "匹配数量", example = "3")
    private Integer matchCount;
}
