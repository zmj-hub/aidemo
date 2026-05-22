package com.enterprise.ai.config;

import io.agentscope.core.model.ExecutionConfig;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.ModelRegistry;
import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.core.model.GenerateOptions;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ModelRegistryConfig {

    private final PlatformProperties platformProperties;

    @PostConstruct
    public void registerModelScopeModels() {
        var modelscope = platformProperties.getModel().getModelscope();
        if (!modelscope.isEnabled()) {
            log.info("ModelScope model registration is disabled");
            return;
        }

        String apiKey = modelscope.getApiKey();
        String baseUrl = modelscope.getBaseUrl();
        long timeout = modelscope.getTimeout();

        // 注册 ModelScope 模型工厂（前缀 "modelscope:"）
        ModelRegistry.registerFactory("modelscope:.+", id -> {
            String modelName = id.substring("modelscope:".length());
            log.debug("Creating ModelScope model: {} via {}", modelName, baseUrl);
            return OpenAIChatModel.builder()
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .baseUrl(baseUrl)
                    .generateOptions(GenerateOptions.builder()
                            .maxTokens(4096)
                            .temperature(0.7)
                            .executionConfig(executionConfig())
                            .build())
                    .build();
        });

        // 为配置中的所有模型预注册命名实例
        for (String modelName : modelscope.getModels()) {
            String modelId = "modelscope:" + modelName;
            if (!ModelRegistry.canResolve(modelId)) {
                Model model = OpenAIChatModel.builder()
                        .apiKey(apiKey)
                        .modelName(modelName)
                        .baseUrl(baseUrl)
                        .generateOptions(GenerateOptions.builder()
                                .maxTokens(4096)
                                .temperature(0.7)
                                .executionConfig(executionConfig())
                                .build())
                        .build();
                ModelRegistry.register(modelId, model);
            }
        }

        log.info("Registered {} ModelScope models via {}", modelscope.getModels().size(), baseUrl);
    }

    private ExecutionConfig executionConfig() {
        return ExecutionConfig.builder()
                .maxAttempts(3)
                .initialBackoff(Duration.ofSeconds(2))
                .maxBackoff(Duration.ofSeconds(30))
                .backoffMultiplier(2.0)
                .build();
    }
}
