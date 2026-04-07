package com.enterprise.ai.service.agent;

import cn.dev33.satoken.stp.StpUtil;
import com.enterprise.ai.common.exception.BusinessException;
import com.enterprise.ai.domain.dto.*;
import com.enterprise.ai.domain.entity.Session;
import com.enterprise.ai.service.agent.memory.RedisShortTermMemory;
import com.enterprise.ai.service.agent.session.SessionService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MemoryService {

    @Autowired
    private RedisShortTermMemory redisShortTermMemory;

    @Autowired
    private SessionService sessionService;

    public List<MemoryInfo> getMemoryList() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<SessionInfo> sessionList = sessionService.getSessionList(true);
        
        return sessionList.stream()
                .map(session -> {
                    List<ChatMessage> messages = redisShortTermMemory.getMessages(session.getId());
                    return MemoryInfo.builder()
                            .sessionId(session.getId())
                            .sessionTitle(session.getTitle())
                            .messageCount(messages.size())
                            .createTime(session.getCreateTime())
                            .updateTime(session.getUpdateTime())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<MemoryMessageInfo> getSessionMemory(Long sessionId) {
        validateSessionOwnership(sessionId);
        
        List<ChatMessage> messages = redisShortTermMemory.getMessages(sessionId);
        return convertToMemoryMessageInfos(messages);
    }

    public List<MemorySearchResult> searchMemory(MemorySearchRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        String keyword = request.getKeyword().toLowerCase();
        List<SessionInfo> sessionList = (request.getSearchAll() != null && request.getSearchAll()) 
                ? sessionService.getSessionList(true) 
                : sessionService.getSessionList(false);
        
        List<MemorySearchResult> results = new ArrayList<>();
        
        for (SessionInfo session : sessionList) {
            List<ChatMessage> messages = redisShortTermMemory.getMessages(session.getId());
            List<MemoryMessageInfo> matchedMessages = new ArrayList<>();
            
            for (ChatMessage message : messages) {
                String content = getMessageContent(message);
                if (content != null && content.toLowerCase().contains(keyword)) {
                    matchedMessages.add(convertToMemoryMessageInfo(message));
                }
            }
            
            if (!matchedMessages.isEmpty()) {
                results.add(MemorySearchResult.builder()
                        .sessionId(session.getId())
                        .sessionTitle(session.getTitle())
                        .matchedMessages(matchedMessages)
                        .matchCount(matchedMessages.size())
                        .build());
            }
        }
        
        return results;
    }

    public void deleteSessionMemory(Long sessionId) {
        validateSessionOwnership(sessionId);
        redisShortTermMemory.deleteMessages(sessionId);
        log.info("用户 {} 删除了会话 {} 的记忆", StpUtil.getLoginIdAsLong(), sessionId);
    }

    public void clearSessionMemory(Long sessionId) {
        validateSessionOwnership(sessionId);
        redisShortTermMemory.clear(sessionId);
        log.info("用户 {} 清空了会话 {} 的记忆", StpUtil.getLoginIdAsLong(), sessionId);
    }

    public void clearAllMemory() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<SessionInfo> sessionList = sessionService.getSessionList(true);
        
        for (SessionInfo session : sessionList) {
            redisShortTermMemory.deleteMessages(session.getId());
        }
        
        log.info("用户 {} 清空了所有会话的记忆", userId);
    }

    private void validateSessionOwnership(Long sessionId) {
        Long userId = StpUtil.getLoginIdAsLong();
        SessionInfo session = sessionService.getSessionById(sessionId);
        
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException("无权访问该会话的记忆");
        }
    }

    private List<MemoryMessageInfo> convertToMemoryMessageInfos(List<ChatMessage> messages) {
        return messages.stream()
                .map(this::convertToMemoryMessageInfo)
                .collect(Collectors.toList());
    }

    private MemoryMessageInfo convertToMemoryMessageInfo(ChatMessage message) {
        String type = "UNKNOWN";
        String content = getMessageContent(message);
        
        if (message instanceof UserMessage) {
            type = "USER";
        } else if (message instanceof AiMessage) {
            type = "AI";
        } else if (message instanceof SystemMessage) {
            type = "SYSTEM";
        }
        
        return MemoryMessageInfo.builder()
                .type(type)
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private String getMessageContent(ChatMessage message) {
        if (message instanceof UserMessage) {
            return ((UserMessage) message).singleText();
        } else if (message instanceof AiMessage) {
            return ((AiMessage) message).text();
        } else if (message instanceof SystemMessage) {
            return ((SystemMessage) message).text();
        }
        return null;
    }
}
