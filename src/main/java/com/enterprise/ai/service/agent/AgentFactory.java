package com.enterprise.ai.service.agent;

import com.enterprise.ai.common.exception.BusinessException;
import com.enterprise.ai.service.agent.memory.RedisShortTermMemory;
import com.enterprise.ai.service.agent.tools.Tool;
import com.enterprise.ai.service.agent.tools.ToolManager;
import com.enterprise.ai.service.model.ModelFactory;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent工厂类
 * 支持动态创建Agent实例，可按模型、记忆策略、工具集灵活配置
 */
@Slf4j
@Component
public class AgentFactory {

    @Autowired
    private ModelFactory modelFactory;

    @Autowired
    private ToolManager toolManager;

    @Autowired
    private TraceService traceService;

    @Autowired
    private RedisShortTermMemory redisShortTermMemory;

    /**
     * Agent实例缓存池
     */
    private final Map<String, Object> agentPool = new ConcurrentHashMap<>();

    /**
     * 创建Agent实例
     * 
     * @param config Agent配置
     * @return Agent实例
     */
    public <T> T createAgent(AgentConfig config, Class<T> agentInterface) {
        validateConfig(config);
        
        String agentKey = buildAgentKey(config);
        
        @SuppressWarnings("unchecked")
        T cachedAgent = (T) agentPool.get(agentKey);
        if (cachedAgent != null) {
            log.info("返回缓存的Agent实例: {}", agentKey);
            return cachedAgent;
        }
        
        T agent = buildAgent(config, agentInterface);
        agentPool.put(agentKey, agent);
        log.info("创建新的Agent实例: {}", agentKey);
        
        return agent;
    }

    /**
     * 创建默认Agent
     * 
     * @param modelCode 模型编码
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return Agent实例
     */
    public <T> T createDefaultAgent(String modelCode, String sessionId, Long userId, Class<T> agentInterface) {
        AgentConfig config = AgentConfig.builder()
                .agentName("default-agent")
                .modelCode(modelCode)
                .sessionId(sessionId)
                .userId(userId)
                .systemPrompt("你是一个有帮助的AI助手。")
                .memoryStrategy(AgentConfig.MemoryStrategy.SHORT_TERM)
                .memoryWindowSize(10)
                .tools(toolManager.getAllTools())
                .enableTracing(true)
                .build();
        
        return createAgent(config, agentInterface);
    }

    /**
     * 创建带指定工具的Agent
     * 
     * @param modelCode 模型编码
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param toolNames 工具名称列表
     * @param agentInterface Agent接口类
     * @return Agent实例
     */
    public <T> T createAgentWithTools(String modelCode, String sessionId, Long userId, 
                                       List<String> toolNames, Class<T> agentInterface) {
        List<Tool> tools = toolManager.getTools(toolNames);
        
        AgentConfig config = AgentConfig.builder()
                .agentName("custom-agent")
                .modelCode(modelCode)
                .sessionId(sessionId)
                .userId(userId)
                .systemPrompt("你是一个有帮助的AI助手。")
                .memoryStrategy(AgentConfig.MemoryStrategy.SHORT_TERM)
                .memoryWindowSize(10)
                .tools(tools)
                .enableTracing(true)
                .build();
        
        return createAgent(config, agentInterface);
    }

    /**
     * 销毁Agent实例
     * 
     * @param config Agent配置
     */
    public void destroyAgent(AgentConfig config) {
        String agentKey = buildAgentKey(config);
        agentPool.remove(agentKey);
        log.info("销毁Agent实例: {}", agentKey);
    }

    /**
     * 销毁会话的所有Agent
     * 
     * @param sessionId 会话ID
     */
    public void destroyAgentsBySession(String sessionId) {
        Set<String> keysToRemove = agentPool.keySet().stream()
                .filter(key -> key.contains(sessionId))
                .collect(java.util.stream.Collectors.toSet());
        
        for (String key : keysToRemove) {
            agentPool.remove(key);
        }
        log.info("销毁会话的所有Agent: sessionId={}, count={}", sessionId, keysToRemove.size());
    }

    /**
     * 清空所有Agent缓存
     */
    public void clearAllAgents() {
        agentPool.clear();
        log.info("清空所有Agent缓存");
    }

    /**
     * 构建Agent唯一键
     * 
     * @param config Agent配置
     * @return 唯一键
     */
    private String buildAgentKey(AgentConfig config) {
        return String.format("%s:%s:%s:%s", 
                config.getAgentName(), 
                config.getModelCode(), 
                config.getSessionId(),
                config.getMemoryStrategy());
    }

    /**
     * 校验配置
     * 
     * @param config Agent配置
     */
    private void validateConfig(AgentConfig config) {
        if (config.getAgentName() == null || config.getAgentName().isBlank()) {
            throw new BusinessException("Agent名称不能为空");
        }
        
        if (config.getModelCode() == null || config.getModelCode().isBlank()) {
            throw new BusinessException("模型编码不能为空");
        }
        
        if (!modelFactory.isModelAvailable(config.getModelCode())) {
            throw new BusinessException("模型不可用: " + config.getModelCode());
        }
    }

    /**
     * 构建Agent实例
     * 
     * @param config Agent配置
     * @param agentInterface Agent接口
     * @return Agent实例
     */
    private <T> T buildAgent(AgentConfig config, Class<T> agentInterface) {
        ChatLanguageModel chatModel = modelFactory.getChatModel(config.getModelCode());
        if (chatModel == null) {
            throw new BusinessException("获取模型失败: " + config.getModelCode());
        }
        
        return AiServices.create(agentInterface, chatModel);
    }

    /**
     * 构建记忆
     * 
     * @param config Agent配置
     * @return 记忆实例
     */
    private ChatMemory buildChatMemory(AgentConfig config) {
        switch (config.getMemoryStrategy()) {
            case SHORT_TERM:
                if (config.getSessionId() != null) {
                    return MessageWindowChatMemory.builder()
                            .id(config.getSessionId())
                            .chatMemoryStore(redisShortTermMemory)
                            .maxMessages(config.getMemoryWindowSize())
                            .build();
                } else {
                    return MessageWindowChatMemory.withMaxMessages(config.getMemoryWindowSize());
                }
            case NONE:
                return null;
            case LONG_TERM:
            case HYBRID:
            default:
                log.warn("暂不支持的记忆策略: {}, 使用短期记忆替代", config.getMemoryStrategy());
                return MessageWindowChatMemory.withMaxMessages(config.getMemoryWindowSize());
        }
    }

    /**
     * 将自定义工具转换为langchain4j工具
     * 
     * @param tools 自定义工具列表
     * @return langchain4j工具列表
     */
    private List<Object> convertToLangchain4jTools(List<Tool> tools) {
        List<Object> result = new ArrayList<>();
        for (Tool tool : tools) {
            result.add(new Langchain4jToolAdapter(tool, traceService));
        }
        return result;
    }

    /**
     * Langchain4j工具适配器
     */
    private static class Langchain4jToolAdapter {
        private final Tool tool;
        private final TraceService traceService;
        private String currentTraceId;

        public Langchain4jToolAdapter(Tool tool, TraceService traceService) {
            this.tool = tool;
            this.traceService = traceService;
        }

        public void setTraceId(String traceId) {
            this.currentTraceId = traceId;
        }

        public String execute(String toolName, Map<String, Object> params) {
            String callId = null;
            if (currentTraceId != null) {
                callId = traceService.startToolCall(currentTraceId, toolName, params);
            }
            
            com.enterprise.ai.service.agent.tools.ToolResult result = tool.execute(params);
            
            if (currentTraceId != null && callId != null) {
                traceService.endToolCall(currentTraceId, callId, result.isSuccess(), 
                        result.getErrorMessage(), result.getData());
            }
            
            if (result.isSuccess()) {
                return result.getData() != null ? result.getData().toString() : "执行成功";
            } else {
                return "执行失败: " + result.getErrorMessage();
            }
        }
    }
}
