package com.enterprise.ai.service.agent.tools;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 工具执行结果封装类
 * 用于封装工具执行的结果信息，包括成功状态、数据、错误信息等
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolResult {

    /**
     * 执行是否成功
     */
    private boolean success;

    /**
     * 执行结果数据
     */
    private Object data;

    /**
     * 错误信息（失败时）
     */
    private String errorMessage;

    /**
     * 执行时间
     */
    private LocalDateTime executeTime;

    /**
     * 执行耗时（毫秒）
     */
    private Long executionTimeMs;

    /**
     * 扩展信息
     */
    private Map<String, Object> extra;

    /**
     * 创建成功结果
     * 
     * @param data 结果数据
     * @return 成功的工具结果
     */
    public static ToolResult success(Object data) {
        return ToolResult.builder()
                .success(true)
                .data(data)
                .executeTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败结果
     * 
     * @param errorMessage 错误信息
     * @return 失败的工具结果
     */
    public static ToolResult failure(String errorMessage) {
        return ToolResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .executeTime(LocalDateTime.now())
                .build();
    }

    /**
     * 设置执行耗时
     * 
     * @param executionTimeMs 执行耗时（毫秒）
     * @return 当前结果对象
     */
    public ToolResult withExecutionTime(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
        return this;
    }

    /**
     * 添加扩展信息
     * 
     * @param key 扩展信息键
     * @param value 扩展信息值
     * @return 当前结果对象
     */
    public ToolResult addExtra(String key, Object value) {
        if (this.extra == null) {
            this.extra = new java.util.HashMap<>();
        }
        this.extra.put(key, value);
        return this;
    }
}
