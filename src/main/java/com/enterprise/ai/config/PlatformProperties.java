package com.enterprise.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "platform")
public class PlatformProperties {

    private ModelConfig model = new ModelConfig();
    private WorkspaceConfig workspace = new WorkspaceConfig();
    private SessionConfig session = new SessionConfig();
    private RagConfig rag = new RagConfig();

    @Data
    public static class ModelConfig {
        private ModelScopeConfig modelscope = new ModelScopeConfig();

        @Data
        public static class ModelScopeConfig {
            private boolean enabled = true;
            private String apiKey;
            private String baseUrl = "https://api-inference.modelscope.cn/v1";
            private long timeout = 60000;
            private int maxRetries = 3;
            private List<String> models = new ArrayList<>();
        }
    }

    @Data
    public static class WorkspaceConfig {
        private String path = "./.agentscope/workspace";
        private boolean autoInit = true;
    }

    @Data
    public static class SessionConfig {
        private String path = "./data/sessions";
    }

    @Data
    public static class RagConfig {
        private String documentStoragePath = "./data/documents";
        private int chunkSize = 1000;
        private int chunkOverlap = 200;
        private int maxResults = 5;
        private double minScore = 0.7;
        private String defaultChatModel = "qwen-turbo";
    }
}
