package com.enterprise.ai.config;

import com.enterprise.ai.service.model.properties.ModelScopeProperties;
import com.enterprise.ai.service.model.properties.OllamaProperties;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
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
    public ChatLanguageModel qwenTurboModel() {
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
    public ChatLanguageModel qwenMaxModel() {
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
    public ChatLanguageModel qwenPlusModel() {
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
    public ChatLanguageModel qwen2_7bInstructModel() {
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
    public ChatLanguageModel llama3_8bInstructModel() {
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
    public ChatLanguageModel qwen3_5_397bA17bModel() {
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
    public ChatLanguageModel qwen3_5_122bA10bModel() {
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
    public OpenAiTokenizer modelScopeTokenizer() {
        return new OpenAiTokenizer();
    }
}
