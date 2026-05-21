package com.enterprise.ai.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class MemoryTools {

    private final Map<String, StringBuilder> memoryStore = new ConcurrentHashMap<>();

    @Tool(name = "memory_add", description = "将信息添加到记忆存储中")
    public String memoryAdd(
            @ToolParam(name = "memoryId", description = "记忆存储ID") String memoryId,
            @ToolParam(name = "content", description = "要存储的内容") String content) {
        memoryStore.computeIfAbsent(memoryId, k -> new StringBuilder())
                .append(content).append("\n");
        return "已添加到记忆 [" + memoryId + "]: " + content;
    }

    @Tool(name = "memory_get", description = "从记忆存储中获取信息")
    public String memoryGet(
            @ToolParam(name = "memoryId", description = "记忆存储ID") String memoryId) {
        StringBuilder sb = memoryStore.get(memoryId);
        if (sb == null || sb.isEmpty()) {
            return "记忆 [" + memoryId + "] 为空";
        }
        return "记忆 [" + memoryId + "]:\n" + sb.toString();
    }

    @Tool(name = "memory_clear", description = "清除指定记忆存储")
    public String memoryClear(
            @ToolParam(name = "memoryId", description = "要清除的记忆存储ID") String memoryId) {
        memoryStore.remove(memoryId);
        return "已清除记忆 [" + memoryId + "]";
    }
}
