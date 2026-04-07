package com.enterprise.ai.service.model;

import cn.dev33.satoken.stp.StpUtil;
import com.enterprise.ai.common.exception.BusinessException;
import com.enterprise.ai.domain.dto.ChatRequest;
import com.enterprise.ai.domain.dto.ChatResponse;
import com.enterprise.ai.domain.dto.ModelInfo;
import com.enterprise.ai.domain.enums.ModelProvider;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 统一模型调用服务
 * 提供同步/流式调用接口，统一异常处理、Token统计
 */
@Slf4j
@Service
public class ChatModelService {

    @Autowired
    private ModelFactory modelFactory;

    @Autowired
    @Qualifier("modelScopeTokenizer")
    private OpenAiTokenizer tokenizer;

    /**
     * 角色与可用模型的映射关系
     */
    private static final Map<String, Set<String>> ROLE_MODEL_MAP = new ConcurrentHashMap<>();

    static {
        ROLE_MODEL_MAP.put("admin", Set.of(
            "Qwen/Qwen3.5-397B-A17B", "Qwen/Qwen3.5-122B-A10B",
                "qwen-turbo", "qwen-max", "qwen-plus",
                "qwen2-7b-instruct"
        ));
        ROLE_MODEL_MAP.put("user", Set.of(
               "Qwen/Qwen3.5-397B-A17B", "Qwen/Qwen3.5-122B-A10B",   "qwen-turbo", "qwen2-7b-instruct", "ollama-llama2"
        ));
        ROLE_MODEL_MAP.put("guest", Set.of(
                "Qwen/Qwen3.5-397B-A17B", "Qwen/Qwen3.5-122B-A10B",  "ollama-llama2"
        ));
    }

    /**
     * 同步聊天调用
     * 
     * @param request 聊天请求
     * @return 聊天响应
     */
    public ChatResponse chat(ChatRequest request) {
        long startTime = System.currentTimeMillis();
        String modelCode = request.getModelCode();
        
        try {
            checkModelPermission(modelCode);
            
            if (!modelFactory.isModelAvailable(modelCode)) {
                throw new BusinessException("模型不可用: " + modelCode);
            }
            
            ChatLanguageModel model = modelFactory.getChatModel(modelCode);
            if (model == null) {
                throw new BusinessException("模型未找到: " + modelCode);
            }
            
            List<ChatMessage> messages = buildChatMessages(request);
            int promptTokens = estimateTokenCount(messages);
            
            Response<AiMessage> response = model.chat(messages);
            String content = response.content().text();
            
            int completionTokens = estimateTokenCount(content);
            int totalTokens = promptTokens + completionTokens;
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("模型调用成功, 模型: {}, 耗时: {}ms, Token数: {}/{}", 
                    modelCode, duration, promptTokens, completionTokens);
            
            return ChatResponse.builder()
                    .modelCode(modelCode)
                    .content(content)
                    .promptTokens(promptTokens)
                    .completionTokens(completionTokens)
                    .totalTokens(totalTokens)
                    .duration(duration)
                    .success(true)
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("模型调用失败, 模型: {}", modelCode, e);
            long duration = System.currentTimeMillis() - startTime;
            return ChatResponse.builder()
                    .modelCode(modelCode)
                    .duration(duration)
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * 检查模型权限
     * 
     * @param modelCode 模型编码
     */
    private void checkModelPermission(String modelCode) {
        List<String> roles = StpUtil.getRoleList();
        if (roles.isEmpty()) {
            roles = List.of("guest");
        }
        
        boolean hasPermission = false;
        for (String role : roles) {
            Set<String> allowedModels = ROLE_MODEL_MAP.get(role);
            if (allowedModels != null && allowedModels.contains(modelCode)) {
                hasPermission = true;
                break;
            }
        }
        
        if (!hasPermission) {
            throw new BusinessException("没有权限使用该模型: " + modelCode);
        }
    }

    /**
     * 构建聊天消息列表
     * 
     * @param request 聊天请求
     * @return 聊天消息列表
     */
    private List<ChatMessage> buildChatMessages(ChatRequest request) {
        List<ChatMessage> messages = new ArrayList<>();
        
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            for (ChatRequest.ChatMessage msg : request.getMessages()) {
                if ("user".equals(msg.getRole())) {
                    messages.add(UserMessage.from(msg.getContent()));
                } else if ("assistant".equals(msg.getRole())) {
                    messages.add(AiMessage.from(msg.getContent()));
                }
            }
        }
        
        messages.add(UserMessage.from(request.getMessage()));
        return messages;
    }

    /**
     * 估算Token数量
     * 
     * @param messages 消息列表
     * @return Token数量
     */
    private int estimateTokenCount(List<ChatMessage> messages) {
        try {
            return tokenizer.estimateTokenCountInMessages(messages);
        } catch (Exception e) {
            StringBuilder text = new StringBuilder();
            for (ChatMessage msg : messages) {
                text.append(msg.toString()).append(" ");
            }
            return estimateTokenCount(text.toString());
        }
    }

    /**
     * 估算Token数量
     * 
     * @param text 文本
     * @return Token数量
     */
    private int estimateTokenCount(String text) {
        try {
            return tokenizer.estimateTokenCountInText(text);
        } catch (Exception e) {
            return text.length() / 4;
        }
    }

    /**
     * 获取当前用户可用的模型列表
     * 
     * @return 模型信息列表
     */
    public List<ModelInfo> getAvailableModels() {
        List<String> roles = StpUtil.getRoleList();
        if (roles.isEmpty()) {
            roles = List.of("guest");
        }
        
        Set<String> availableModels = modelFactory.getAvailableModels();
        List<ModelInfo> result = new ArrayList<>();
        
        Set<String> allowedModels = new java.util.HashSet<>();
        for (String role : roles) {
            Set<String> roleModels = ROLE_MODEL_MAP.get(role);
            if (roleModels != null) {
                allowedModels.addAll(roleModels);
            }
        }
        
        for (String modelCode : allowedModels) {
            if (availableModels.contains(modelCode)) {
                ModelProvider provider = ModelProvider.fromCode(modelCode);
                if (provider != null) {
                    result.add(ModelInfo.builder()
                            .code(provider.getCode())
                            .name(provider.getName())
                            .provider(provider.getProvider())
                            .enabled(true)
                            .healthy(true)
                            .lastCheckTime(System.currentTimeMillis())
                            .build());
                }
            }
        }
        
        return result;
    }

    /**
     * 健康检查单个模型
     * 
     * @param modelCode 模型编码
     * @return 是否健康
     */
    public boolean healthCheck(String modelCode) {
        try {
            ChatLanguageModel model = modelFactory.getChatModel(modelCode);
            if (model == null) {
                modelFactory.updateModelHealth(modelCode, false);
                return false;
            }
            
            String response = model.chat("你好");
            boolean healthy = response != null && !response.isEmpty();
            modelFactory.updateModelHealth(modelCode, healthy);
            return healthy;
        } catch (Exception e) {
            log.warn("模型健康检查失败: {}", modelCode, e);
            modelFactory.updateModelHealth(modelCode, false);
            return false;
        }
    }

    /**
     * 批量健康检查
     * 
     * @param modelCodes 模型编码列表，为空则检查所有可用模型
     * @return 模型健康状态Map
     */
    public Map<String, Boolean> batchHealthCheck(List<String> modelCodes) {
        if (modelCodes == null || modelCodes.isEmpty()) {
            modelCodes = new ArrayList<>(modelFactory.getAvailableModels());
        }
        
        Map<String, Boolean> result = new HashMap<>();
        for (String modelCode : modelCodes) {
            result.put(modelCode, healthCheck(modelCode));
        }
        return result;
    }
}
