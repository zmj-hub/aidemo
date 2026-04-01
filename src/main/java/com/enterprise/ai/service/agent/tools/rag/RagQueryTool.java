package com.enterprise.ai.service.agent.tools.rag;

import com.enterprise.ai.common.utils.UserContextHolder;
import com.enterprise.ai.domain.dto.RagQueryRequest;
import com.enterprise.ai.domain.dto.RagQueryResponse;
import com.enterprise.ai.service.agent.tools.BaseTool;
import com.enterprise.ai.service.agent.tools.ToolResult;
import com.enterprise.ai.service.rag.RagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * RAG知识库查询工具
 * 提供基于知识库的问答功能
 */
@Slf4j
@Component
public class RagQueryTool extends BaseTool {

    /**
     * 工具名称
     */
    private static final String TOOL_NAME = "rag_query";

    /**
     * 工具描述
     */
    private static final String TOOL_DESCRIPTION = "查询知识库中的相关文档，基于RAG技术回答问题";

    /**
     * 参数定义
     */
    private static final Map<String, String> PARAMS;

    static {
        PARAMS = new HashMap<>();
        PARAMS.put("query", "查询问题（必需）");
        PARAMS.put("chatModel", "使用的对话模型（可选）");
        PARAMS.put("embeddingModel", "使用的Embedding模型（可选）");
        PARAMS.put("maxResults", "返回的最大结果数（可选）");
        PARAMS.put("minScore", "最小相似度阈值，0-1之间（可选）");
    }

    @Autowired
    private RagService ragService;

    public RagQueryTool() {
        super(TOOL_NAME, TOOL_DESCRIPTION, PARAMS);
    }

    @Override
    protected ToolResult doExecute(Map<String, Object> params) {
        String query = getParamAsString(params, "query", null);
        if (query == null || query.isBlank()) {
            return ToolResult.failure("必须提供查询问题（query）");
        }

        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            log.warn("用户未登录，RAG查询可能受限制");
        }

        try {
            RagQueryRequest request = new RagQueryRequest();
            request.setQuery(query);
            request.setChatModel(getParamAsString(params, "chatModel", null));
            request.setEmbeddingModel(getParamAsString(params, "embeddingModel", null));
            request.setMaxResults(getParamAsInteger(params, "maxResults", null));
            request.setMinScore(getParamAsDouble(params, "minScore", null));

            RagQueryResponse response = ragService.query(request);

            Map<String, Object> result = new HashMap<>();
            result.put("answer", response.getAnswer());
            result.put("chatModel", response.getChatModel());
            result.put("embeddingModel", response.getEmbeddingModel());
            result.put("sources", response.getSources());
            result.put("sourceCount", response.getSources() != null ? response.getSources().size() : 0);

            return ToolResult.success(result);
        } catch (Exception e) {
            log.error("RAG查询失败: query={}", query, e);
            return ToolResult.failure("RAG查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取Double类型参数
     * 
     * @param params 参数Map
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    private Double getParamAsDouble(Map<String, Object> params, String key, Double defaultValue) {
        Object value = params.get(key);
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof Float) {
            return ((Float) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}
