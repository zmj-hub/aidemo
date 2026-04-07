package com.enterprise.ai.service.agent.memory;

import com.enterprise.ai.config.MemoryProperties;
import com.enterprise.ai.service.model.ModelFactory;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 对话自动摘要服务
 * 负责将长对话历史压缩为简洁的摘要，用于长期记忆存储
 */
@Slf4j
@Component
public class MemorySummarizer {

    /**
     * 摘要提示词
     */
    private static final String SUMMARY_PROMPT = """
            请将以下对话历史总结为一个简洁的摘要，保留关键信息：
            - 主要讨论的话题
            - 用户的核心问题和需求
            - 重要的结论和决策
            - 需要记住的用户偏好
            
            请用中文回答，保持摘要简洁明了，不超过500字。
            """;

    @Autowired
    private VectorLongTermMemory vectorLongTermMemory;

    @Autowired
    private MemoryProperties memoryProperties;

    @Autowired
    private ModelFactory modelFactory;

    /**
     * 检查是否需要进行摘要
     * 
     * @param messages 对话消息列表
     * @return 是否需要摘要
     */
    public boolean shouldSummarize(List<ChatMessage> messages) {
        int threshold = memoryProperties.getSummaryThreshold() != null ? memoryProperties.getSummaryThreshold() : 10;
        return messages.size() >= threshold;
    }

    /**
     * 生成对话摘要
     * 
     * @param messages 对话消息列表
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 生成的摘要文本
     */
    public String generateSummary(List<ChatMessage> messages, Long userId, String sessionId) {
        String conversationText = formatMessagesForSummary(messages);
        
        String summary = callModelForSummary(conversationText);
        
        storeSummaryInLongTermMemory(summary, userId, sessionId);
        
        return summary;
    }

    /**
     * 获取压缩后的记忆（保留最近几条消息 + 之前的摘要）
     * 
     * @param messages 完整对话消息列表
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 压缩后的消息列表
     */
    public List<ChatMessage> getCompressedMemory(
            List<ChatMessage> messages,
            Long userId,
            String sessionId) {
        
        int keepRecentCount = memoryProperties.getKeepRecentCount() != null 
            ? memoryProperties.getKeepRecentCount() 
            : 3;
        
        if (messages.size() <= keepRecentCount) {
            return messages;
        }
        
        String summary = generateSummary(
            messages.subList(0, messages.size() - keepRecentCount),
            userId,
            sessionId
        );
        
        List<ChatMessage> recentMessages = messages.subList(messages.size() - keepRecentCount, messages.size());
        
        SystemMessage summaryMessage = SystemMessage.from("对话摘要：\n" + summary);
        
        List<ChatMessage> result = new ArrayList<>();
        result.add(summaryMessage);
        result.addAll(recentMessages);
        return result;
    }

    /**
     * 格式化对话消息用于摘要生成
     * 
     * @param messages 对话消息列表
     * @return 格式化后的对话文本
     */
    private String formatMessagesForSummary(List<ChatMessage> messages) {
        return messages.stream()
                .map(msg -> {
                    String role = msg.getClass().getSimpleName().replace("Message", "");
                    String content;
                    if (msg instanceof UserMessage) {
                        content = ((UserMessage) msg).singleText();
                    } else if (msg instanceof SystemMessage) {
                        content = ((SystemMessage) msg).text();
                    } else if (msg instanceof dev.langchain4j.data.message.AiMessage) {
                        content = ((dev.langchain4j.data.message.AiMessage) msg).text();
                    } else {
                        content = msg.toString();
                    }
                    return String.format("[%s]: %s", role, content);
                })
                .collect(Collectors.joining("\n"));
    }

    /**
     * 调用模型生成摘要
     * 
     * @param conversationText 对话文本
     * @return 摘要文本
     */
    private String callModelForSummary(String conversationText) {
        try {
            String modelCode = memoryProperties.getSummaryModelCode() != null 
                ? memoryProperties.getSummaryModelCode() 
                : "qwen-turbo";
            
            ChatModel model = modelFactory.getChatModel(modelCode);
            if (model == null) {
                log.warn("摘要模型 {} 不可用，尝试使用第一个可用模型", modelCode);
                model = findFirstAvailableModel();
            }
            
            if (model == null) {
                throw new RuntimeException("没有可用的模型来生成摘要");
            }
            
            String prompt = SUMMARY_PROMPT + "\n\n对话内容：\n" + conversationText;
            return model.chat(prompt);
        } catch (Exception e) {
            log.error("生成摘要失败", e);
            return "对话摘要生成失败，保留原始对话内容。";
        }
    }

    /**
     * 查找第一个可用的聊天模型
     * 
     * @return 可用的聊天模型，没有则返回null
     */
    private ChatModel findFirstAvailableModel() {
        for (String modelCode : modelFactory.getAvailableModels()) {
            ChatModel model = modelFactory.getChatModel(modelCode);
            if (model != null) {
                return model;
            }
        }
        return null;
    }

    /**
     * 将摘要存储到长期记忆中
     * 
     * @param summary 摘要文本
     * @param userId 用户ID
     * @param sessionId 会话ID
     */
    private void storeSummaryInLongTermMemory(String summary, Long userId, String sessionId) {
        vectorLongTermMemory.addMemory(
            userId,
            summary,
            Map.of(
                "type", "summary",
                "sessionId", sessionId,
                "timestamp", System.currentTimeMillis()
            )
        );
    }
}
