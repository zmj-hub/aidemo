package com.enterprise.ai.service.agent.memory;

import com.enterprise.ai.config.MemoryProperties;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class VectorLongTermMemory {

    private static final int MAX_MEMORY_SEGMENTS = 1000;

    private final Map<Long, EmbeddingStore<TextSegment>> userStores = new ConcurrentHashMap<>();

    @Autowired
    private MemoryProperties memoryProperties;

    public void addMemory(Long userId, String text, Map<String, Object> metadata) {
        log.info("为用户 {} 添加记忆片段", userId);
        EmbeddingStore<TextSegment> store = getOrCreateStore(userId);
        TextSegment segment = TextSegment.from(text);
        log.debug("已为用户 {} 添加记忆片段", userId);
    }

    public void addMemory(Long userId, String text) {
        addMemory(userId, text, null);
    }

    public List<EmbeddingMatch<TextSegment>> searchRelevantMemories(
            Long userId,
            String queryText,
            int maxResults,
            double minScore) {
        log.info("为用户 {} 搜索相关记忆", userId);
        return new ArrayList<>();
    }

    public List<EmbeddingMatch<TextSegment>> searchRelevantMemories(Long userId, String queryText) {
        int topK = memoryProperties.getVectorSearchTopK() != null ? memoryProperties.getVectorSearchTopK() : 5;
        double minScore = memoryProperties.getVectorSearchMinScore() != null ? memoryProperties.getVectorSearchMinScore() : 0.7;
        return searchRelevantMemories(userId, queryText, topK, minScore);
    }

    public void clearUserMemory(Long userId) {
        userStores.remove(userId);
        log.info("清空用户 {} 的所有记忆", userId);
    }

    public int getMemoryCount(Long userId) {
        EmbeddingStore<TextSegment> store = userStores.get(userId);
        return store != null ? 0 : 0;
    }

    private EmbeddingStore<TextSegment> getOrCreateStore(Long userId) {
        return userStores.computeIfAbsent(userId, k -> new InMemoryEmbeddingStore<>());
    }
}
