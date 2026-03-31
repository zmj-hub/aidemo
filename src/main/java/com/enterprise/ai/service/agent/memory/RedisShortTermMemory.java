package com.enterprise.ai.service.agent.memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Redis短期记忆服务
 * 负责会话级别的窗口记忆存储，使用Redis作为存储介质
 */
@Component
public class RedisShortTermMemory implements ChatMemoryStore {

    /**
     * Redis中记忆数据的key前缀
     */
    private static final String MEMORY_KEY_PREFIX = "ai:session:memory:";

    /**
     * 默认记忆过期时间（24小时）
     */
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    /**
     * 默认窗口大小（最近20条消息）
     */
    private static final int DEFAULT_WINDOW_SIZE = 20;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取指定会话的记忆消息列表
     * 
     * @param memoryId 记忆ID（通常使用会话ID）
     * @return 聊天消息列表
     */
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String key = buildKey(memoryId);
        @SuppressWarnings("unchecked")
        List<ChatMessage> messages = (List<ChatMessage>) redisTemplate.opsForValue().get(key);
        return messages != null ? messages : new ArrayList<>();
    }

    /**
     * 更新指定会话的记忆消息列表
     * 会自动进行窗口大小限制
     * 
     * @param memoryId 记忆ID（通常使用会话ID）
     * @param messages 聊天消息列表
     */
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String key = buildKey(memoryId);
        
        List<ChatMessage> windowMessages;
        if (messages.size() > DEFAULT_WINDOW_SIZE) {
            windowMessages = messages.subList(messages.size() - DEFAULT_WINDOW_SIZE, messages.size());
        } else {
            windowMessages = new ArrayList<>(messages);
        }
        
        redisTemplate.opsForValue().set(key, windowMessages, DEFAULT_TTL);
    }

    /**
     * 删除指定会话的记忆
     * 
     * @param memoryId 记忆ID（通常使用会话ID）
     */
    @Override
    public void deleteMessages(Object memoryId) {
        String key = buildKey(memoryId);
        redisTemplate.delete(key);
    }

    /**
     * 添加单条消息到记忆中
     * 
     * @param memoryId 记忆ID（通常使用会话ID）
     * @param message 聊天消息
     */
    public void addMessage(Object memoryId, ChatMessage message) {
        List<ChatMessage> messages = getMessages(memoryId);
        messages.add(message);
        updateMessages(memoryId, messages);
    }

    /**
     * 清空指定会话的记忆
     * 
     * @param memoryId 记忆ID（通常使用会话ID）
     */
    public void clear(Object memoryId) {
        deleteMessages(memoryId);
    }

    /**
     * 构建Redis key
     * 
     * @param memoryId 记忆ID
     * @return Redis key
     */
    private String buildKey(Object memoryId) {
        return MEMORY_KEY_PREFIX + memoryId;
    }
}
