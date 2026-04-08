package com.enterprise.ai.service.agent.tools.memory;

import com.enterprise.ai.common.utils.DateUtils;
import com.enterprise.ai.service.agent.memory.RedisShortTermMemory;
import com.enterprise.ai.service.agent.tools.BaseTool;
import com.enterprise.ai.service.agent.tools.ToolResult;
import dev.langchain4j.data.message.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 记忆查询工具
 * 用于从记忆中查询历史内容
 */
@Slf4j
@Component
public class MemoryQueryTool extends BaseTool {

    /**
     * 工具名称
     */
    private static final String TOOL_NAME = "memory_query";

    /**
     * 工具描述
     */
    private static final String TOOL_DESCRIPTION = "从记忆中查询历史内容，支持分页和过滤";

    /**
     * 参数定义
     */
    private static final Map<String, String> PARAMS;

    static {
        PARAMS = new HashMap<>();
        PARAMS.put("memoryId", "记忆ID，通常使用会话ID（必需）");
        PARAMS.put("limit", "返回的最大消息数（可选，默认全部）");
        PARAMS.put("type", "消息类型过滤：user, ai, system（可选）");
    }

    @Autowired
    private RedisShortTermMemory memoryService;

    public MemoryQueryTool() {
        super(TOOL_NAME, TOOL_DESCRIPTION, PARAMS);
    }

    @Override
    protected ToolResult doExecute(Map<String, Object> params) {
        String memoryId = getParamAsString(params, "memoryId", null);
        if (memoryId == null || memoryId.isBlank()) {
            return ToolResult.failure("必须提供记忆ID（memoryId）");
        }

        Integer limit = getParamAsInteger(params, "limit", null);
        String type = getParamAsString(params, "type", null);

        try {
            List<ChatMessage> messages = memoryService.getMessages(memoryId);
            
            List<Map<String, Object>> filteredMessages = new ArrayList<>();
            for (ChatMessage message : messages) {
                if (type != null && !type.isBlank()) {
                    String messageType = getMessageType(message);
                    if (!type.equalsIgnoreCase(messageType)) {
                        continue;
                    }
                }

                Map<String, Object> messageInfo = new HashMap<>();
                messageInfo.put("type", getMessageType(message));
                messageInfo.put("content", getMessageContent(message));
                filteredMessages.add(messageInfo);
            }

            if (limit != null && limit > 0 && filteredMessages.size() > limit) {
                filteredMessages = filteredMessages.subList(filteredMessages.size() - limit, filteredMessages.size());
            }

            Map<String, Object> result = new HashMap<>();
            result.put("memoryId", memoryId);
            result.put("totalCount", messages.size());
            result.put("returnedCount", filteredMessages.size());
            result.put("messages", filteredMessages);
            result.put("queryTime", DateUtils.nowStr());

            return ToolResult.success(result);
        } catch (Exception e) {
            log.error("查询记忆失败: memoryId={}", memoryId, e);
            return ToolResult.failure("查询记忆失败: " + e.getMessage());
        }
    }

    /**
     * 获取消息类型
     * 
     * @param message 聊天消息
     * @return 消息类型字符串
     */
    private String getMessageType(ChatMessage message) {
        if (message instanceof dev.langchain4j.data.message.UserMessage) {
            return "user";
        } else if (message instanceof dev.langchain4j.data.message.AiMessage) {
            return "ai";
        } else if (message instanceof dev.langchain4j.data.message.SystemMessage) {
            return "system";
        }
        return "unknown";
    }

    /**
     * 获取消息内容
     * 
     * @param message 聊天消息
     * @return 消息内容字符串
     */
    private String getMessageContent(ChatMessage message) {
        if (message instanceof dev.langchain4j.data.message.UserMessage userMessage) {
            return userMessage.singleText();
        } else if (message instanceof dev.langchain4j.data.message.AiMessage aiMessage) {
            return aiMessage.text();
        } else if (message instanceof dev.langchain4j.data.message.SystemMessage systemMessage) {
            return systemMessage.text();
        }
        return "";
    }
}
