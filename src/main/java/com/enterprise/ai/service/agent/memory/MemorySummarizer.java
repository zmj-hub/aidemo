package com.enterprise.ai.service.agent.memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
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
@Component
public class MemorySummarizer {

    /**
     * 默认触发摘要的消息数量阈值
     */
    private static final int DEFAULT_SUMMARY_THRESHOLD = 10;

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

    /**
     * 检查是否需要进行摘要
     * 
     * @param messages 对话消息列表
     * @return 是否需要摘要
     */
    public boolean shouldSummarize(List<ChatMessage> messages) {
        return messages.size() >= DEFAULT_SUMMARY_THRESHOLD;
    }

    /**
     * 生成对话摘要
     * 
     * @param messages 对话消息列表
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 生成的摘要文本
     */
    public String generateSummary(List<ChatMessage> messages, Long userId, Long sessionId) {
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
     * @param keepRecentCount 保留的最近消息数量
     * @return 压缩后的消息列表
     */
    public List<ChatMessage> getCompressedMemory(
            List<ChatMessage> messages,
            Long userId,
            Long sessionId,
            int keepRecentCount) {
        
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
                    String content = msg.text();
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
            ChatLanguageModel model = createSimpleModel();
            String prompt = SUMMARY_PROMPT + "\n\n对话内容：\n" + conversationText;
            return model.generate(prompt);
        } catch (Exception e) {
            return "对话摘要生成失败，保留原始对话内容。";
        }
    }

    /**
     * 创建一个简单的模型用于摘要生成
     * 
     * @return ChatLanguageModel实例
     */
    private ChatLanguageModel createSimpleModel() {
        return OpenAiChatModel.builder()
                .apiKey("dummy-key")
                .baseUrl("http://localhost:8080")
                .modelName("gpt-3.5-turbo")
                .build();
    }

    /**
     * 将摘要存储到长期记忆中
     * 
     * @param summary 摘要文本
     * @param userId 用户ID
     * @param sessionId 会话ID
     */
    private void storeSummaryInLongTermMemory(String summary, Long userId, Long sessionId) {
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
