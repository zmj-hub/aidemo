package com.enterprise.ai.service.model;

import com.enterprise.ai.domain.enums.ModelProvider;
import com.enterprise.ai.service.model.properties.ModelScopeProperties;
import com.enterprise.ai.service.model.properties.OllamaProperties;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模型工厂类
 * 基于ConcurrentHashMap实现模型单例池，启动时校验配置可用性，仅注册有效模型
 */
@Slf4j
@Component
public class ModelFactory {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ModelScopeProperties modelScopeProperties;

    @Autowired
    private OllamaProperties ollamaProperties;

    /**
     * 同步模型单例池
     */
    private final Map<String, ChatModel> chatModelPool = new ConcurrentHashMap<>();

    /**
     * 流式模型单例池
     */
    private final Map<String, StreamingChatModel> streamingModelPool = new ConcurrentHashMap<>();

    /**
     * 健康模型集合
     */
    private final Set<String> healthyModels = ConcurrentHashMap.newKeySet();

    /**
     * 初始化模型工厂，注册所有有效模型
     */
    @PostConstruct
    public void init() {
        log.info("开始初始化模型工厂...");
        registerModelScopeModels();
        registerOllamaModels();
        log.info("模型工厂初始化完成，已注册{}个同步模型，{}个流式模型", 
                chatModelPool.size(), streamingModelPool.size());
    }

    /**
     * 注册ModelScope模型
     */
    private void registerModelScopeModels() {
        if (!modelScopeProperties.getEnabled()) {
            log.info("ModelScope模型未启用，跳过注册");
            return;
        }
        if (modelScopeProperties.getApiKey() == null || modelScopeProperties.getApiKey().isBlank()) {
            log.warn("ModelScope API密钥未配置，跳过注册");
            return;
        }
        try {
            // 注册 qwen-turbo 模型
            ChatModel qwenTurbo = applicationContext.getBean("qwenTurboModel", ChatModel.class);
            if (qwenTurbo != null) {
                chatModelPool.put(ModelProvider.QWEN_TURBO.getCode(), qwenTurbo);
                healthyModels.add(ModelProvider.QWEN_TURBO.getCode());
                log.info("注册ModelScope qwen-turbo模型成功");
            }
            // 注册 qwen-max 模型
            ChatModel qwenMax = applicationContext.getBean("qwenMaxModel", ChatModel.class);
            if (qwenMax != null) {
                chatModelPool.put(ModelProvider.QWEN_MAX.getCode(), qwenMax);
                healthyModels.add(ModelProvider.QWEN_MAX.getCode());
                log.info("注册ModelScope qwen-max模型成功");
            }
            // 注册 qwen-plus 模型
            ChatModel qwenPlus = applicationContext.getBean("qwenPlusModel", ChatModel.class);
            if (qwenPlus != null) {
                chatModelPool.put(ModelProvider.QWEN_PLUS.getCode(), qwenPlus);
                healthyModels.add(ModelProvider.QWEN_PLUS.getCode());
                log.info("注册ModelScope qwen-plus模型成功");
            }
            // 注册 qwen2-7b-instruct 模型
            ChatModel qwen2_7bInstruct = applicationContext.getBean("qwen2_7bInstructModel", ChatModel.class);
            if (qwen2_7bInstruct != null) {
                chatModelPool.put(ModelProvider.QWEN2_7B_INSTRUCT.getCode(), qwen2_7bInstruct);
                healthyModels.add(ModelProvider.QWEN2_7B_INSTRUCT.getCode());
                log.info("注册ModelScope qwen2-7b-instruct模型成功");
            }
            // 注册 llama3-8b-instruct 模型
            ChatModel llama3_8bInstruct = applicationContext.getBean("llama3_8bInstructModel", ChatModel.class);
            if (llama3_8bInstruct != null) {
                chatModelPool.put(ModelProvider.LLAMA3_8B_INSTRUCT.getCode(), llama3_8bInstruct);
                healthyModels.add(ModelProvider.LLAMA3_8B_INSTRUCT.getCode());
                log.info("注册ModelScope llama3-8b-instruct模型成功");
            }
            // 注册 Qwen/Qwen3.5-397B-A17B 模型
            ChatModel qwen3_5_397bA17b = applicationContext.getBean("qwen3_5_397bA17bModel", ChatModel.class);
            if (qwen3_5_397bA17b != null) {
                chatModelPool.put(ModelProvider.QWEN3_5_397B_A17B.getCode(), qwen3_5_397bA17b);
                healthyModels.add(ModelProvider.QWEN3_5_397B_A17B.getCode());
                log.info("注册ModelScope Qwen/Qwen3.5-397B-A17B模型成功");
            }
            // 注册 Qwen/Qwen3.5-122B-A10B 模型
            ChatModel qwen3_5_122bA10b = applicationContext.getBean("qwen3_5_122bA10bModel", ChatModel.class);
            if (qwen3_5_122bA10b != null) {
                chatModelPool.put(ModelProvider.QWEN3_5_122B_A10B.getCode(), qwen3_5_122bA10b);
                healthyModels.add(ModelProvider.QWEN3_5_122B_A10B.getCode());
                log.info("注册ModelScope Qwen/Qwen3.5-122B-A10B模型成功");
            }
            // 注册 qwen-turbo 流式模型
            OpenAiStreamingChatModel qwenTurboStreaming = applicationContext.getBean("qwenTurboStreamingModel", OpenAiStreamingChatModel.class);
            if (qwenTurboStreaming != null) {
                streamingModelPool.put(ModelProvider.QWEN_TURBO.getCode(), qwenTurboStreaming);
                log.info("注册ModelScope qwen-turbo流式模型成功");
            }
            // 注册 qwen-max 流式模型
            OpenAiStreamingChatModel qwenMaxStreaming = applicationContext.getBean("qwenMaxStreamingModel", OpenAiStreamingChatModel.class);
            if (qwenMaxStreaming != null) {
                streamingModelPool.put(ModelProvider.QWEN_MAX.getCode(), qwenMaxStreaming);
                log.info("注册ModelScope qwen-max流式模型成功");
            }
            // 注册 qwen-plus 流式模型
            OpenAiStreamingChatModel qwenPlusStreaming = applicationContext.getBean("qwenPlusStreamingModel", OpenAiStreamingChatModel.class);
            if (qwenPlusStreaming != null) {
                streamingModelPool.put(ModelProvider.QWEN_PLUS.getCode(), qwenPlusStreaming);
                log.info("注册ModelScope qwen-plus流式模型成功");
            }
            // 注册 qwen2-7b-instruct 流式模型
            OpenAiStreamingChatModel qwen2_7bInstructStreaming = applicationContext.getBean("qwen2_7bInstructStreamingModel", OpenAiStreamingChatModel.class);
            if (qwen2_7bInstructStreaming != null) {
                streamingModelPool.put(ModelProvider.QWEN2_7B_INSTRUCT.getCode(), qwen2_7bInstructStreaming);
                log.info("注册ModelScope qwen2-7b-instruct流式模型成功");
            }
            // 注册 Qwen/Qwen3.5-397B-A17B 流式模型
            OpenAiStreamingChatModel qwen3_5_397bA17bStreaming = applicationContext.getBean("qwen3_5_397bA17bStreamingModel", OpenAiStreamingChatModel.class);
            if (qwen3_5_397bA17bStreaming != null) {
                streamingModelPool.put(ModelProvider.QWEN3_5_397B_A17B.getCode(), qwen3_5_397bA17bStreaming);
                log.info("注册ModelScope Qwen/Qwen3.5-397B-A17B流式模型成功");
            }
            // 注册 Qwen/Qwen3.5-122B-A10B 流式模型
            OpenAiStreamingChatModel qwen3_5_122bA10bStreaming = applicationContext.getBean("qwen3_5_122bA10bStreamingModel", OpenAiStreamingChatModel.class);
            if (qwen3_5_122bA10bStreaming != null) {
                streamingModelPool.put(ModelProvider.QWEN3_5_122B_A10B.getCode(), qwen3_5_122bA10bStreaming);
                log.info("注册ModelScope Qwen/Qwen3.5-122B-A10B流式模型成功");
            }
        } catch (Exception e) {
            log.error("注册ModelScope模型失败", e);
        }
    }

    /**
     * 注册Ollama模型
     */
    private void registerOllamaModels() {
        if (!ollamaProperties.getEnabled()) {
            log.info("Ollama模型未启用，跳过注册");
            return;
        }
        log.info("Ollama模型配置已启用，基础URL: {}", ollamaProperties.getBaseUrl());
        for (String model : ollamaProperties.getModels()) {
            String modelCode = "ollama-" + model;
            healthyModels.add(modelCode);
            log.info("注册Ollama {} 模型成功", model);
        }
    }

    /**
     * 获取同步模型
     * 
     * @param modelCode 模型编码
     * @return 同步模型，如果不存在返回null
     */
    public ChatModel getChatModel(String modelCode) {
        return chatModelPool.get(modelCode);
    }

    /**
     * 获取流式模型
     * 
     * @param modelCode 模型编码
     * @return 流式模型，如果不存在返回null
     */
    public StreamingChatModel getStreamingModel(String modelCode) {
        return streamingModelPool.get(modelCode);
    }

    /**
     * 检查模型是否可用
     * 
     * @param modelCode 模型编码
     * @return 是否可用
     */
    public boolean isModelAvailable(String modelCode) {
        return healthyModels.contains(modelCode);
    }

    /**
     * 获取所有可用模型编码
     * 
     * @return 可用模型编码集合
     */
    public Set<String> getAvailableModels() {
        return healthyModels;
    }

    /**
     * 更新模型健康状态
     * 
     * @param modelCode 模型编码
     * @param healthy 是否健康
     */
    public void updateModelHealth(String modelCode, boolean healthy) {
        if (healthy) {
            healthyModels.add(modelCode);
        } else {
            healthyModels.remove(modelCode);
        }
    }
}
