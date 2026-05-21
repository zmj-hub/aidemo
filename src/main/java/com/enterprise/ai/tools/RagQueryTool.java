package com.enterprise.ai.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RagQueryTool {

    private final RagQueryHandler handler;

    @Tool(name = "rag_query", description = "从知识库中检索相关文档并回答问题")
    public String query(
            @ToolParam(name = "query", description = "要查询的问题") String query) {
        try {
            return handler.handleQuery(query);
        } catch (Exception e) {
            return "RAG查询失败: " + e.getMessage();
        }
    }

    @FunctionalInterface
    public interface RagQueryHandler {
        String handleQuery(String query);
    }
}
