package com.enterprise.ai.service.agent.tools.memory;

import com.enterprise.ai.service.agent.memory.RedisShortTermMemory;
import com.enterprise.ai.service.agent.tools.BaseTool;
import com.enterprise.ai.service.agent.tools.ToolResult;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 记忆添加工具
 * 用于向记忆中添加新的内容
 */
@Slf4j
@Component
public class MemoryAddTool extends BaseTool {

    /**
     * 工具名称
     */
    private static final String TOOL_NAME = "memory_add";

    /**
     * 工具描述
     */
    private static final String TOOL_DESCRIPTION = "向记忆中添加新的内容，支持短期记忆";

    /**
     * 参数定义
     */
    private static final Map<String, String> PARAMS;

    static {
        PARAMS = new HashMap<>();
        PARAMS.put("memoryId", "记忆ID，通常使用会话ID（必需）");
        PARAMS.put("content", "要添加的内容（必需）");
        PARAMS.put("type", "内容类型：user, ai, system（默认user）");
    }

    @Autowired
    private RedisShortTermMemory memoryService;

    public MemoryAddTool() {
        super(TOOL_NAME, TOOL_DESCRIPTION, PARAMS);
    }

    @Override
    protected ToolResult doExecute(Map<String, Object> params) {
        String memoryId = getParamAsString(params, "memoryId", null);
        if (memoryId == null || memoryId.isBlank()) {
            return ToolResult.failure("必须提供记忆ID（memoryId）");
        }

        String content = getParamAsString(params, "content", null);
        if (content == null || content.isBlank()) {
            return ToolResult.failure("必须提供要添加的内容（content）");
        }

        String type = getParamAsString(params, "type", "user");

        try {
            ChatMessage message;
            switch (type.toLowerCase()) {
                case "ai":
                    message = dev.langchain4j.data.message.AiMessage.from(content);
                    break;
                case "system":
                    message = dev.langchain4j.data.message.SystemMessage.from(content);
                    break;
                case "user":
                default:
                    message = UserMessage.from(content);
                    break;
            }

            memoryService.addMessage(memoryId, message);

            Map<String, Object> result = new HashMap<>();
            result.put("memoryId", memoryId);
            result.put("content", content);
            result.put("type", type);
            result.put("status", "success");

            return ToolResult.success(result);
        } catch (Exception e) {
            log.error("添加记忆失败: memoryId={}", memoryId, e);
            return ToolResult.failure("添加记忆失败: " + e.getMessage());
        }
    }
}
