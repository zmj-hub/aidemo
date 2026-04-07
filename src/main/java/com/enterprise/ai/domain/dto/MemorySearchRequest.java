package com.enterprise.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "记忆搜索请求")
public class MemorySearchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "搜索关键词", example = "Java")
    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;

    @Schema(description = "是否搜索所有会话记忆", example = "false")
    private Boolean searchAll;
}
