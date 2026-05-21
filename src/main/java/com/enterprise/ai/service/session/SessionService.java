package com.enterprise.ai.service.session;

import cn.dev33.satoken.stp.StpUtil;
import com.enterprise.ai.config.PlatformProperties;
import com.enterprise.ai.domain.dto.SessionCreateRequest;
import com.enterprise.ai.domain.dto.SessionInfo;
import com.enterprise.ai.domain.dto.SessionUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final PlatformProperties platformProperties;
    private final Map<String, SessionMeta> sessionMetaStore = new ConcurrentHashMap<>();

    public SessionInfo createSession(SessionCreateRequest request) {
        String sessionId = UUID.randomUUID().toString();
        String title = request.getTitle() != null ? request.getTitle()
                : "新对话 " + LocalDateTime.now().toString().substring(0, 16);

        SessionMeta meta = new SessionMeta();
        meta.id = sessionId;
        meta.title = title;
        meta.modelCode = request.getModelCode();
        meta.userId = getCurrentUserId();
        meta.archived = false;
        meta.lastMessageTime = LocalDateTime.now();
        meta.createTime = LocalDateTime.now();
        meta.updateTime = LocalDateTime.now();

        sessionMetaStore.put(sessionId, meta);
        ensureSessionDir(sessionId);

        log.debug("Created session: {} ({})", sessionId, title);
        return toSessionInfo(meta);
    }

    public List<SessionInfo> getSessionList(boolean includeArchived) {
        return sessionMetaStore.values().stream()
                .filter(m -> m.userId.equals(getCurrentUserId()))
                .filter(m -> includeArchived || !m.archived)
                .sorted(Comparator.comparing((SessionMeta m) -> m.lastMessageTime).reversed())
                .map(this::toSessionInfo)
                .toList();
    }

    public SessionInfo getSessionById(String sessionId) {
        SessionMeta meta = getSessionMeta(sessionId);
        return toSessionInfo(meta);
    }

    public SessionInfo updateSession(String sessionId, SessionUpdateRequest request) {
        SessionMeta meta = getSessionMeta(sessionId);
        if (request.getTitle() != null) {
            meta.title = request.getTitle();
        }
        meta.updateTime = LocalDateTime.now();
        return toSessionInfo(meta);
    }

    public void archiveSession(String sessionId) {
        SessionMeta meta = getSessionMeta(sessionId);
        meta.archived = true;
        meta.updateTime = LocalDateTime.now();
    }

    public void unarchiveSession(String sessionId) {
        SessionMeta meta = getSessionMeta(sessionId);
        meta.archived = false;
        meta.updateTime = LocalDateTime.now();
    }

    public void deleteSession(String sessionId) {
        getSessionMeta(sessionId);
        sessionMetaStore.remove(sessionId);
    }

    public void updateLastMessageTime(String sessionId) {
        SessionMeta meta = sessionMetaStore.get(sessionId);
        if (meta != null) {
            meta.lastMessageTime = LocalDateTime.now();
            meta.updateTime = LocalDateTime.now();
        }
    }

    private SessionMeta getSessionMeta(String sessionId) {
        SessionMeta meta = sessionMetaStore.get(sessionId);
        if (meta == null) {
            throw new RuntimeException("会话不存在: " + sessionId);
        }
        return meta;
    }

    private Long getCurrentUserId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            return 1L;
        }
    }

    private SessionInfo toSessionInfo(SessionMeta meta) {
        return SessionInfo.builder()
                .id(meta.id.hashCode() & 0x7FFFFFFFL) // 兼容旧版Long型ID
                .title(meta.title)
                .modelCode(meta.modelCode)
                .userId(meta.userId)
                .archived(meta.archived)
                .lastMessageTime(meta.lastMessageTime)
                .createTime(meta.createTime)
                .updateTime(meta.updateTime)
                .build();
    }

    private void ensureSessionDir(String sessionId) {
        try {
            Path dir = Path.of(platformProperties.getSession().getPath(), sessionId);
            Files.createDirectories(dir);
        } catch (IOException e) {
            log.warn("Failed to create session dir: {}", e.getMessage());
        }
    }

    private static class SessionMeta {
        String id;
        String title;
        String modelCode;
        Long userId;
        Boolean archived;
        LocalDateTime lastMessageTime;
        LocalDateTime createTime;
        LocalDateTime updateTime;
    }
}
