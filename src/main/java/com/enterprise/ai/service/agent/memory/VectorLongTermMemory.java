package com.enterprise.ai.service.agent.memory;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.allminilm.l6.v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 向量数据库长期记忆服务
 * 负责用户级别的长期记忆存储和检索，使用向量相似度搜索
 */
@Component
public class VectorLongTermMemory {

    /**
     * 每个用户的最大记忆片段数量
     */
    private static final int MAX_MEMORY_SEGMENTS = 1000;

    /**
     * 检索时返回的最大匹配数
     */
    private static final int DEFAULT_MAX_RESULTS = 5;

    /**
     * 最小相似度分数
     */
    private static final double DEFAULT_MIN_SCORE = 0.7;

    /**
     * 用户ID到EmbeddingStore的映射
     */
    private final Map<Long, EmbeddingStore<TextSegment>> userStores = new ConcurrentHashMap<>();

    /**
     * 嵌入模型
     */
    private EmbeddingModel embeddingModel;

    @PostConstruct
    public void init() {
        // 暂时注释掉嵌入模型的初始化，让应用程序能够启动
        // embeddingModel = new AllMiniLmL6V2EmbeddingModel();
    }

    /**
     * 添加记忆片段到用户的长期记忆中
     * 
     * @param userId 用户ID
     * @param text 记忆文本内容
     * @param metadata 元数据（可选）
     */
    public void addMemory(Long userId, String text, Map<String, Object> metadata) {
        EmbeddingStore<TextSegment> store = getOrCreateStore(userId);
        
        TextSegment segment;
        if (metadata != null && !metadata.isEmpty()) {
            segment = TextSegment.from(text, metadata);
        } else {
            segment = TextSegment.from(text);
        }
        
        Embedding embedding = embeddingModel.embed(segment).content();
        store.add(embedding, segment);
        
        trimStoreIfNeeded(store);
    }

    /**
     * 添加记忆片段到用户的长期记忆中（不带元数据）
     * 
     * @param userId 用户ID
     * @param text 记忆文本内容
     */
    public void addMemory(Long userId, String text) {
        addMemory(userId, text, null);
    }

    /**
     * 从用户的长期记忆中搜索相关的记忆片段
     * 
     * @param userId 用户ID
     * @param queryText 查询文本
     * @param maxResults 最大返回结果数
     * @param minScore 最小相似度分数
     * @return 匹配的记忆片段列表
     */
    public List<EmbeddingMatch<TextSegment>> searchRelevantMemories(
            Long userId,
            String queryText,
            int maxResults,
            double minScore) {
        EmbeddingStore<TextSegment> store = getOrCreateStore(userId);
        
        Embedding queryEmbedding = embeddingModel.embed(queryText).content();
        
        return store.findRelevant(queryEmbedding, maxResults, minScore);
    }

    /**
     * 从用户的长期记忆中搜索相关的记忆片段（使用默认参数）
     * 
     * @param userId 用户ID
     * @param queryText 查询文本
     * @return 匹配的记忆片段列表
     */
    public List<EmbeddingMatch<TextSegment>> searchRelevantMemories(Long userId, String queryText) {
        return searchRelevantMemories(userId, queryText, DEFAULT_MAX_RESULTS, DEFAULT_MIN_SCORE);
    }

    /**
     * 清空用户的所有长期记忆
     * 
     * @param userId 用户ID
     */
    public void clearUserMemory(Long userId) {
        userStores.remove(userId);
    }

    /**
     * 获取用户的记忆片段数量
     * 
     * @param userId 用户ID
     * @return 记忆片段数量
     */
    public int getMemoryCount(Long userId) {
        EmbeddingStore<TextSegment> store = userStores.get(userId);
        if (store instanceof InMemoryEmbeddingStore) {
            return ((InMemoryEmbeddingStore<TextSegment>) store).size();
        }
        return 0;
    }

    /**
     * 获取或创建用户的EmbeddingStore
     * 
     * @param userId 用户ID
     * @return EmbeddingStore实例
     */
    private EmbeddingStore<TextSegment> getOrCreateStore(Long userId) {
        return userStores.computeIfAbsent(userId, k -> new InMemoryEmbeddingStore<>());
    }

    /**
     * 如果记忆片段数量超过限制，则进行裁剪
     * 
     * @param store EmbeddingStore实例
     */
    private void trimStoreIfNeeded(EmbeddingStore<TextSegment> store) {
        if (store instanceof InMemoryEmbeddingStore) {
            InMemoryEmbeddingStore<TextSegment> inMemoryStore = (InMemoryEmbeddingStore<TextSegment>) store;
            while (inMemoryStore.size() > MAX_MEMORY_SEGMENTS) {
                inMemoryStore.remove(0);
            }
        }
    }
}
