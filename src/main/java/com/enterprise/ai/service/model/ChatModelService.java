package com.enterprise.ai.service.model;

import com.enterprise.ai.common.exception.BusinessException;
import com.enterprise.ai.config.PlatformProperties;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.ModelRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatModelService {

    private final PlatformProperties platformProperties;
    private final Map<String, Boolean> modelHealthCache = new LinkedHashMap<>();

    @PostConstruct
    public void init() {
        var modelscope = platformProperties.getModel().getModelscope();
        for (String modelName : modelscope.getModels()) {
            String modelId = "modelscope:" + modelName;
            modelHealthCache.put(modelId, true);
        }
        log.info("Initialized {} models in health cache", modelHealthCache.size());
    }

    public List<Map<String, Object>> getAvailableModels() {
        List<Map<String, Object>> models = new ArrayList<>();
        for (String modelName : platformProperties.getModel().getModelscope().getModels()) {
            String modelId = "modelscope:" + modelName;
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("code", modelId);
            info.put("name", modelName);
            info.put("provider", "ModelScope");
            info.put("enabled", platformProperties.getModel().getModelscope().isEnabled());
            info.put("healthy", modelHealthCache.getOrDefault(modelId, true));
            info.put("lastCheckTime", null);
            models.add(info);
        }
        return models;
    }

    public boolean isModelAvailable(String modelId) {
        return ModelRegistry.canResolve(modelId);
    }

    public Model resolveModel(String modelId) {
        if (!ModelRegistry.canResolve(modelId)) {
            throw new BusinessException("模型不可用: " + modelId);
        }
        return ModelRegistry.resolve(modelId);
    }

    public Map<String, Object> healthCheck(String modelId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("modelCode", modelId);
        try {
            if (!ModelRegistry.canResolve(modelId)) {
                result.put("healthy", false);
                result.put("error", "模型未注册");
                return result;
            }
            Model model = ModelRegistry.resolve(modelId);
            Msg testMsg = Msg.builder()
                    .role(MsgRole.USER)
                    .textContent("Hello")
                    .build();
            String responseText = model.stream(List.of(testMsg), List.of(), null)
                    .collectList()
                    .block()
                    .stream()
                    .map(r -> {
                        if (!r.getContent().isEmpty() && r.getContent().get(0) instanceof io.agentscope.core.message.TextBlock tb) {
                            return tb.getText();
                        }
                        return "";
                    })
                    .reduce("", (a, b) -> a + b);
            boolean healthy = responseText != null && !responseText.isEmpty();
            result.put("healthy", healthy);
            modelHealthCache.put(modelId, healthy);
        } catch (Exception e) {
            result.put("healthy", false);
            result.put("error", e.getMessage());
            modelHealthCache.put(modelId, false);
        }
        return result;
    }

    public List<Map<String, Object>> batchHealthCheck(List<String> modelIds) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (String modelId : modelIds) {
            results.add(healthCheck(modelId));
        }
        return results;
    }

    public void updateModelHealth(String modelId, boolean healthy) {
        modelHealthCache.put(modelId, healthy);
    }
}
