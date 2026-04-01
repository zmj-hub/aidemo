package com.enterprise.ai.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RAG配置类
 * 负责配置RAG相关的Bean，包括Embedding模型、向量存储等
 */
@Configuration
public class RagConfig {

    @Autowired
    private RagProperties ragProperties;

    /**
     * 创建默认的Embedding模型（AllMiniLmL6V2）
     * 
     * @return AllMiniLmL6V2 Embedding模型
     */
    @Bean
    public EmbeddingModel allMiniLmEmbeddingModel() {
        // 暂时返回null，避免DJL库的权限问题
        return null;
    }

    /**
     * 创建内存向量存储
     * 
     * @return 内存向量存储
     */
    @Bean
    public EmbeddingStore<TextSegment> inMemoryEmbeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    /**
     * 根据配置创建向量存储
     * 
     * @return 向量存储实例
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        // 只使用内存向量存储
        return inMemoryEmbeddingStore();
    }

    /**
     * 根据模型名称获取Embedding模型
     * 
     * @param modelName 模型名称
     * @return Embedding模型
     */
    public EmbeddingModel getEmbeddingModel(String modelName) {
        if (modelName == null || modelName.isEmpty()) {
            modelName = ragProperties.getDefaultEmbeddingModel();
        }

        switch (modelName.toLowerCase()) {
            case "all-minilm-l6-v2":
                return allMiniLmEmbeddingModel();
            case "text-embedding-ada-002":
            case "text-embedding-3-small":
            case "text-embedding-3-large":
                return createOpenAiEmbeddingModel(modelName);
            default:
                return allMiniLmEmbeddingModel();
        }
    }

    /**
     * 创建OpenAI Embedding模型
     * 
     * @param modelName 模型名称
     * @return OpenAI Embedding模型
     */
    private EmbeddingModel createOpenAiEmbeddingModel(String modelName) {
        return OpenAiEmbeddingModel.builder()
                .modelName(modelName)
                .build();
    }
}
