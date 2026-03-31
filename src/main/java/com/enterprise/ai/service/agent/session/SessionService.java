package com.enterprise.ai.service.agent.session;

import cn.hutool.core.util.IdUtil;
import com.enterprise.ai.common.exception.BusinessException;
import com.enterprise.ai.common.utils.UserContextHolder;
import com.enterprise.ai.domain.dto.SessionCreateRequest;
import com.enterprise.ai.domain.dto.SessionInfo;
import com.enterprise.ai.domain.dto.SessionUpdateRequest;
import com.enterprise.ai.domain.entity.Session;
import com.enterprise.ai.service.agent.memory.RedisShortTermMemory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 会话服务类
 * 提供会话创建、归档、删除、列表查询、自动标题生成等功能
 */
@Service
public class SessionService {

    /**
     * 模拟会话数据库
     */
    private static final Map<Long, Session> SESSION_DB = new ConcurrentHashMap<>();

    /**
     * 会话ID生成器
     */
    private static long sessionIdGenerator = 1;

    @Autowired
    private RedisShortTermMemory redisShortTermMemory;

    /**
     * 创建新会话
     * 
     * @param request 会话创建请求
     * @return 会话信息
     */
    public SessionInfo createSession(SessionCreateRequest request) {
        Long userId = UserContextHolder.getUserId();
        
        Session session = new Session();
        session.setId(generateSessionId());
        session.setUserId(userId);
        session.setModelCode(request.getModelCode());
        session.setArchived(false);
        
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            session.setTitle(request.getTitle());
        } else {
            session.setTitle(generateDefaultTitle());
        }
        
        LocalDateTime now = LocalDateTime.now();
        session.setCreateTime(now);
        session.setUpdateTime(now);
        session.setLastMessageTime(now);
        
        SESSION_DB.put(session.getId(), session);
        
        return convertToSessionInfo(session);
    }

    /**
     * 获取当前用户的所有会话列表
     * 
     * @param includeArchived 是否包含已归档的会话
     * @return 会话信息列表
     */
    public List<SessionInfo> getSessionList(boolean includeArchived) {
        Long userId = UserContextHolder.getUserId();
        
        return SESSION_DB.values().stream()
                .filter(session -> session.getUserId().equals(userId))
                .filter(session -> includeArchived || !session.getArchived())
                .sorted(Comparator.comparing(Session::getLastMessageTime).reversed())
                .map(this::convertToSessionInfo)
                .collect(Collectors.toList());
    }

    /**
     * 获取指定会话的详细信息
     * 
     * @param sessionId 会话ID
     * @return 会话信息
     */
    public SessionInfo getSessionById(Long sessionId) {
        Session session = getSessionAndValidate(sessionId);
        return convertToSessionInfo(session);
    }

    /**
     * 更新会话信息
     * 
     * @param sessionId 会话ID
     * @param request 会话更新请求
     * @return 更新后的会话信息
     */
    public SessionInfo updateSession(Long sessionId, SessionUpdateRequest request) {
        Session session = getSessionAndValidate(sessionId);
        
        session.setTitle(request.getTitle());
        session.setUpdateTime(LocalDateTime.now());
        
        SESSION_DB.put(sessionId, session);
        
        return convertToSessionInfo(session);
    }

    /**
     * 归档会话
     * 
     * @param sessionId 会话ID
     */
    public void archiveSession(Long sessionId) {
        Session session = getSessionAndValidate(sessionId);
        
        session.setArchived(true);
        session.setUpdateTime(LocalDateTime.now());
        
        SESSION_DB.put(sessionId, session);
    }

    /**
     * 取消归档会话
     * 
     * @param sessionId 会话ID
     */
    public void unarchiveSession(Long sessionId) {
        Session session = getSessionAndValidate(sessionId);
        
        session.setArchived(false);
        session.setUpdateTime(LocalDateTime.now());
        
        SESSION_DB.put(sessionId, session);
    }

    /**
     * 删除会话
     * 
     * @param sessionId 会话ID
     */
    public void deleteSession(Long sessionId) {
        getSessionAndValidate(sessionId);
        
        SESSION_DB.remove(sessionId);
        
        redisShortTermMemory.deleteMessages(sessionId);
    }

    /**
     * 自动生成会话标题
     * 
     * @param sessionId 会话ID
     * @param firstMessage 第一条消息内容
     * @return 生成的标题
     */
    public String generateSessionTitle(Long sessionId, String firstMessage) {
        String title;
        
        if (firstMessage != null && !firstMessage.isBlank()) {
            title = firstMessage.length() > 50 ? firstMessage.substring(0, 50) + "..." : firstMessage;
        } else {
            title = generateDefaultTitle();
        }
        
        Session session = SESSION_DB.get(sessionId);
        if (session != null) {
            session.setTitle(title);
            session.setUpdateTime(LocalDateTime.now());
            SESSION_DB.put(sessionId, session);
        }
        
        return title;
    }

    /**
     * 更新会话的最后消息时间
     * 
     * @param sessionId 会话ID
     */
    public void updateLastMessageTime(Long sessionId) {
        Session session = SESSION_DB.get(sessionId);
        if (session != null) {
            session.setLastMessageTime(LocalDateTime.now());
            session.setUpdateTime(LocalDateTime.now());
            SESSION_DB.put(sessionId, session);
        }
    }

    /**
     * 获取会话实体并验证权限
     * 
     * @param sessionId 会话ID
     * @return 会话实体
     */
    private Session getSessionAndValidate(Long sessionId) {
        Long userId = UserContextHolder.getUserId();
        
        Session session = SESSION_DB.get(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException("无权访问该会话");
        }
        
        return session;
    }

    /**
     * 生成默认会话标题
     * 
     * @return 默认标题
     */
    private String generateDefaultTitle() {
        return "新对话 " + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    /**
     * 生成会话ID
     * 
     * @return 会话ID
     */
    private synchronized Long generateSessionId() {
        return sessionIdGenerator++;
    }

    /**
     * 将Session实体转换为SessionInfo DTO
     * 
     * @param session Session实体
     * @return SessionInfo DTO
     */
    private SessionInfo convertToSessionInfo(Session session) {
        return SessionInfo.builder()
                .id(session.getId())
                .title(session.getTitle())
                .modelCode(session.getModelCode())
                .userId(session.getUserId())
                .archived(session.getArchived())
                .lastMessageTime(session.getLastMessageTime())
                .createTime(session.getCreateTime())
                .updateTime(session.getUpdateTime())
                .build();
    }
}
