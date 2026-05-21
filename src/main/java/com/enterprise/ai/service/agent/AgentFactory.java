package com.enterprise.ai.service.agent;

import com.enterprise.ai.tools.HttpTool;
import com.enterprise.ai.tools.MemoryTools;
import com.enterprise.ai.tools.SystemTools;
import io.agentscope.core.model.ModelRegistry;
import io.agentscope.core.session.Session;
import io.agentscope.core.state.SimpleSessionKey;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.harness.agent.HarnessAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgentFactory {

    private final Path workspacePath;
    private final Session jsonSession;
    private final Map<String, HarnessAgent> agentCache = new ConcurrentHashMap<>();

    public HarnessAgent createAgent(String sessionId, String modelId, String systemPrompt,
                                     List<String> toolNames) {
        String cacheKey = sessionId + ":" + modelId;
        agentCache.remove(cacheKey);

        Toolkit toolkit = buildToolkit(toolNames);

        HarnessAgent agent = HarnessAgent.builder()
                .name("agent-" + sessionId)
                .model(modelId)
                .sysPrompt(systemPrompt != null ? systemPrompt : "你是一个有帮助的AI助手。")
                .toolkit(toolkit)
                .workspace(workspacePath)
                .build();

        try {
            agent.loadIfExists(jsonSession, SimpleSessionKey.of(sessionId));
        } catch (Exception e) {
            log.debug("No existing session state for: {}", sessionId);
        }

        agentCache.put(cacheKey, agent);
        log.debug("Created agent for session={}, model={}", sessionId, modelId);
        return agent;
    }

    public HarnessAgent getAgent(String sessionId, String modelId) {
        return agentCache.get(sessionId + ":" + modelId);
    }

    public void removeAgent(String sessionId, String modelId) {
        agentCache.remove(sessionId + ":" + modelId);
    }

    public void clearAll() {
        agentCache.clear();
    }

    private Toolkit buildToolkit(List<String> toolNames) {
        Toolkit toolkit = new Toolkit();
        boolean allTools = toolNames == null || toolNames.isEmpty();

        if (allTools || toolNames.contains("time") || toolNames.contains("get_current_time")) {
            toolkit.registerTool(new SystemTools());
        }
        if (allTools || toolNames.contains("http") || toolNames.contains("http_request")) {
            toolkit.registerTool(new HttpTool());
        }
        if (allTools || toolNames.contains("memory_add") || toolNames.contains("memory_get")) {
            toolkit.registerTool(new MemoryTools());
        }

        return toolkit;
    }
}
