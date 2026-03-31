package com.enterprise.ai.service.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 推理追踪记录实体
 * 用于记录Agent推理过程中的完整链路信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraceRecord {

    /**
     * 追踪ID
     */
    private String traceId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * Agent名称
     */
    private String agentName;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 执行耗时（毫秒）
     */
    private Long durationMs;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 用户输入
     */
    private String userInput;

    /**
     * Agent输出
     */
    private String agentOutput;

    /**
     * 工具调用记录
     */
    @Builder.Default
    private List<ToolCallRecord> toolCalls = new ArrayList<>();

    /**
     * 扩展信息
     */
    private Map<String, Object> extra;

    /**
     * 创建新的追踪记录
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param agentName Agent名称
     * @return 追踪记录
     */
    public static TraceRecord create(String sessionId, Long userId, String agentName) {
        return TraceRecord.builder()
                .traceId(UUID.randomUUID().toString())
                .sessionId(sessionId)
                .userId(userId)
                .agentName(agentName)
                .startTime(LocalDateTime.now())
                .success(true)
                .build();
    }

    /**
     * 添加工具调用记录
     * 
     * @param toolCall 工具调用记录
     */
    public void addToolCall(ToolCallRecord toolCall) {
        if (this.toolCalls == null) {
            this.toolCalls = new ArrayList<>();
        }
        this.toolCalls.add(toolCall);
    }

    /**
     * 结束追踪
     * 
     * @param success 是否成功
     * @param errorMessage 错误信息
     */
    public void finish(boolean success, String errorMessage) {
        this.endTime = LocalDateTime.now();
        this.success = success;
        this.errorMessage = errorMessage;
        if (this.startTime != null && this.endTime != null) {
            this.durationMs = java.time.Duration.between(this.startTime, this.endTime).toMillis();
        }
    }

    /**
     * 工具调用记录
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolCallRecord {

        /**
         * 调用ID
         */
        private String callId;

        /**
         * 工具名称
         */
        private String toolName;

        /**
         * 工具参数
         */
        private Map<String, Object> params;

        /**
         * 开始时间
         */
        private LocalDateTime startTime;

        /**
         * 结束时间
         */
        private LocalDateTime endTime;

        /**
         * 执行耗时（毫秒）
         */
        private Long durationMs;

        /**
         * 是否成功
         */
        private boolean success;

        /**
         * 错误信息
         */
        private String errorMessage;

        /**
         * 执行结果
         */
        private Object result;

        /**
         * 创建工具调用记录
         * 
         * @param toolName 工具名称
         * @param params 工具参数
         * @return 工具调用记录
         */
        public static ToolCallRecord create(String toolName, Map<String, Object> params) {
            return ToolCallRecord.builder()
                    .callId(UUID.randomUUID().toString())
                    .toolName(toolName)
                    .params(params)
                    .startTime(LocalDateTime.now())
                    .success(true)
                    .build();
        }

        /**
         * 完成工具调用
         * 
         * @param success 是否成功
         * @param errorMessage 错误信息
         * @param result 执行结果
         */
        public void complete(boolean success, String errorMessage, Object result) {
            this.endTime = LocalDateTime.now();
            this.success = success;
            this.errorMessage = errorMessage;
            this.result = result;
            if (this.startTime != null && this.endTime != null) {
                this.durationMs = java.time.Duration.between(this.startTime, this.endTime).toMillis();
            }
        }
    }
}
