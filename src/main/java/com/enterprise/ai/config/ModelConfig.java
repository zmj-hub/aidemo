package com.enterprise.ai.config;

import com.enterprise.ai.service.model.properties.ModelScopeProperties;
import com.enterprise.ai.service.model.properties.OllamaProperties;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ModelConfig {

    @Autowired
    private ModelScopeProperties modelScopeProperties;

    @Autowired
    private OllamaProperties ollamaProperties;

    @Bean
    public ChatModel qwenTurboModel() {
        if (!modelScopeProperties.getEnabled()) {
            return null;
        }
        return OpenAiChatModel.builder()
                .baseUrl(modelScopeProperties.getBaseUrl())
                .apiKey(modelScopeProperties.getApiKey())
                .modelName("qwen-turbo")
                .timeout(Duration.ofMillis(modelScopeProperties.getTimeout()))
                .maxRetries(modelScopeProperties.getMaxRetries())
                .build();
    }

    @Bean
    public ChatModel qwenMaxModel() {
        if (!modelScopeProperties.getEnabled()) {
            return null;
        }
        return OpenAiChatModel.builder()
                .baseUrl(modelScopeProperties.getBaseUrl())
                .apiKey(modelScopeProperties.getApiKey())
                .modelName("qwen-max")
                .timeout(Duration.ofMillis(modelScopeProperties.getTimeout()))
                .maxRetries(modelScopeProperties.getMaxRetries())
                .build();
    }

    @Bean
    public ChatModel qwenPlusModel() {
        if (!modelScopeProperties.getEnabled()) {
            return null;
        }
        return OpenAiChatModel.builder()
                .baseUrl(modelScopeProperties.getBaseUrl())
                .apiKey(modelScopeProperties.getApiKey())
                .modelName("qwen-plus")
                .timeout(Duration.ofMillis(modelScopeProperties.getTimeout()))
                .maxRetries(modelScopeProperties.getMaxRetries())
                .build();
    }

    @Bean
    public ChatModel qwen2_7bInstructModel() {
        if (!modelScopeProperties.getEnabled()) {
            return null;
        }
        return OpenAiChatModel.builder()
                .baseUrl(modelScopeProperties.getBaseUrl())
                .apiKey(modelScopeProperties.getApiKey())
                .modelName("qwen2-7b-instruct")
                .timeout(Duration.ofMillis(modelScopeProperties.getTimeout()))
                .maxRetries(modelScopeProperties.getMaxRetries())
                .build();
    }

    @Bean
    public ChatModel llama3_8bInstructModel() {
        if (!modelScopeProperties.getEnabled()) {
            return null;
        }
        return OpenAiChatModel.builder()
                .baseUrl(modelScopeProperties.getBaseUrl())
                .apiKey(modelScopeProperties.getApiKey())
                .modelName("llama3-8b-instruct")
                .timeout(Duration.ofMillis(modelScopeProperties.getTimeout()))
                .maxRetries(modelScopeProperties.getMaxRetries())
                .build();
    }

    @Bean
    public ChatModel qwen3_5_397bA17bModel() {
        if (!modelScopeProperties.getEnabled()) {
            return null;
        }
        return OpenAiChatModel.builder()
                .baseUrl(modelScopeProperties.getBaseUrl())
                .apiKey(modelScopeProperties.getApiKey())
                .modelName("Qwen/Qwen3.5-397B-A17B")
                .timeout(Duration.ofMillis(modelScopeProperties.getTimeout()))
                .maxRetries(modelScopeProperties.getMaxRetries())
                .build();
    }

    @Bean
    public ChatModel qwen3_5_122bA10bModel() {
        if (!modelScopeProperties.getEnabled()) {
            return null;
        }
        return OpenAiChatModel.builder()
                .baseUrl(modelScopeProperties.getBaseUrl())
                .apiKey(modelScopeProperties.getApiKey())
                .modelName("Qwen/Qwen3.5-122B-A10B")
                .timeout(Duration.ofMillis(modelScopeProperties.getTimeout()))
                .maxRetries(modelScopeProperties.getMaxRetries())
                .build();
    }

    @Bean
    public OpenAiStreamingChatModel qwenTurboStreamingModel() {
        if (!modelScopeProperties.getEnabled()) {
            return null;
        }
        return OpenAiStreamingChatModel.builder()
                .baseUrl(modelScopeProperties.getBaseUrl())
                .apiKey(modelScopeProperties.getApiKey())
                .modelName("qwen-turbo")
                .timeout(Duration.ofMillis(modelScopeProperties.getTimeout()))
                .build();
    }

    @Bean
    public OpenAiStreamingChatModel qwenMaxStreamingModel() {
        if (!modelScopeProperties.getEnabled()) {
            return null;
        }
        return OpenAiStreamingChatModel.builder()
                .baseUrl(modelScopeProperties.getBaseUrl())
                .apiKey(modelScopeProperties.getApiKey())
                .modelName("qwen-max")
                .timeout(Duration.ofMillis(modelScopeProperties.getTimeout()))
                .build();
    }

    @Bean
    public OpenAiStreamingChatModel qwenPlusStreamingModel() {
        if (!modelScopeProperties.getEnabled()) {
            return null;
        }
        return OpenAiStreamingChatModel.builder()
                .baseUrl(modelScopeProperties.getBaseUrl())
                .apiKey(modelScopeProperties.getApiKey())
                .modelName("qwen-plus")
                .timeout(Duration.ofMillis(modelScopeProperties.getTimeout()))
                .build();
    }

    @Bean
    public OpenAiStreamingChatModel qwen2_7bInstructStreamingModel() {
        if (!modelScopeProperties.getEnabled()) {
            return null;
        }
        return OpenAiStreamingChatModel.builder()
                .baseUrl(modelScopeProperties.getBaseUrl())
                .apiKey(modelScopeProperties.getApiKey())
                .modelName("qwen2-7b-instruct")
                .timeout(Duration.ofMillis(modelScopeProperties.getTimeout()))
                .build();
    }

    @Bean
    public OpenAiStreamingChatModel qwen3_5_397bA17bStreamingModel() {
        if (!modelScopeProperties.getEnabled()) {
            return null;
        }
        return OpenAiStreamingChatModel.builder()
                .baseUrl(modelScopeProperties.getBaseUrl())
                .apiKey(modelScopeProperties.getApiKey())
                .modelName("Qwen/Qwen3.5-397B-A17B")
                .timeout(Duration.ofMillis(modelScopeProperties.getTimeout()))
                .build();
    }

    @Bean
    public OpenAiStreamingChatModel qwen3_5_122bA10bStreamingModel() {
        if (!modelScopeProperties.getEnabled()) {
            return null;
        }
        return OpenAiStreamingChatModel.builder()
                .baseUrl(modelScopeProperties.getBaseUrl())
                .apiKey(modelScopeProperties.getApiKey())
                .modelName("Qwen/Qwen3.5-122B-A10B")
                .timeout(Duration.ofMillis(modelScopeProperties.getTimeout()))
                .build();
    }

    @Bean
    public OpenAiTokenCountEstimator modelScopeTokenizer() {
        // OpenAiTokenCountEstimator 需要提供模型名称参数
        return new OpenAiTokenCountEstimator("gpt-3.5-turbo");
    }
}
