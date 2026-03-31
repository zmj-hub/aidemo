package com.enterprise.ai.service.agent;

import com.enterprise.ai.service.agent.memory.RedisShortTermMemory;
import com.enterprise.ai.service.agent.tools.Tool;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent配置类
 * 用于配置Agent的各种参数，包括模型、记忆策略、工具集等
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentConfig {

    /**
     * Agent名称
     */
    private String agentName;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 使用的聊天模型
     */
    private ChatLanguageModel chatModel;

    /**
     * 模型编码
     */
    private String modelCode;

    /**
     * 记忆策略类型
     */
    @Builder.Default
    private MemoryStrategy memoryStrategy = MemoryStrategy.SHORT_TERM;

    /**
     * 记忆窗口大小
     */
    @Builder.Default
    private Integer memoryWindowSize = 10;

    /**
     * 工具列表
     */
    @Builder.Default
    private List<Tool> tools = new ArrayList<>();

    /**
     * 是否启用追踪
     */
    @Builder.Default
    private boolean enableTracing = true;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 记忆策略枚举
     */
    public enum MemoryStrategy {
        /**
         * 短期记忆（窗口记忆）
         */
        SHORT_TERM,
        /**
         * 长期记忆（向量记忆）
         */
        LONG_TERM,
        /**
         * 混合记忆（短期+长期）
         */
        HYBRID,
        /**
         * 无记忆
         */
        NONE
    }
}
