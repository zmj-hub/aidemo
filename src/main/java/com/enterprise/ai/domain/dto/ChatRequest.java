package com.enterprise.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 聊天请求DTO
 */
@Data
@Schema(description = "聊天请求参数")
public class ChatRequest {

    /**
     * 模型编码
     */
    @NotBlank(message = "模型编码不能为空")
    @Schema(description = "模型编码", example = "qwen-turbo")
    private String modelCode;

    /**
     * 用户消息
     */
    @NotBlank(message = "用户消息不能为空")
    @Schema(description = "用户消息", example = "你好，请介绍一下自己")
    private String message;

    /**
     * 历史对话消息列表
     */
    @Schema(description = "历史对话消息列表")
    private List<ChatMessage> messages;

    /**
     * 温度参数，控制随机性
     */
    @Schema(description = "温度参数", example = "0.7")
    private Double temperature = 0.7;

    /**
     * 最大生成token数
     */
    @Schema(description = "最大生成token数", example = "2000")
    private Integer maxTokens = 2000;

    /**
     * 是否流式输出
     */
    @Schema(description = "是否流式输出", example = "false")
    private Boolean stream = false;

    /**
     * 聊天消息内部类
     */
    @Data
    @Schema(description = "聊天消息")
    public static class ChatMessage {

        /**
         * 角色：user/assistant/system
         */
        @NotBlank(message = "角色不能为空")
        @Schema(description = "角色", example = "user")
        private String role;

        /**
         * 消息内容
         */
        @NotBlank(message = "消息内容不能为空")
        @Schema(description = "消息内容")
        private String content;
    }
}
