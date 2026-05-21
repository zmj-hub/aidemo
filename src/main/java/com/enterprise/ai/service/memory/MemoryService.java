package com.enterprise.ai.service.memory;

import cn.dev33.satoken.stp.StpUtil;
import com.enterprise.ai.domain.dto.MemoryInfo;
import com.enterprise.ai.domain.dto.MemoryMessageInfo;
import com.enterprise.ai.domain.dto.MemorySearchRequest;
import com.enterprise.ai.domain.dto.MemorySearchResult;
import io.agentscope.core.session.JsonSession;
import io.agentscope.core.session.Session;
import io.agentscope.core.state.SimpleSessionKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemoryService {

    private final Session jsonSession;

    public List<MemoryInfo> getMemoryList() {
        List<MemoryInfo> result = new ArrayList<>();
        try {
            Path sessionPath = Path.of("./data/sessions");
            if (Files.exists(sessionPath)) {
                try (Stream<Path> dirs = Files.list(sessionPath).filter(Files::isDirectory)) {
                    dirs.forEach(dir -> {
                        String sessionId = dir.getFileName().toString();
                        MemoryInfo info = MemoryInfo.builder()
                                .sessionId((long) sessionId.hashCode())
                                .sessionTitle("会话-" + sessionId.substring(0, 8))
                                .messageCount(0)
                                .createTime(LocalDateTime.now())
                                .updateTime(LocalDateTime.now())
                                .build();
                        result.add(info);
                    });
                }
            }
        } catch (Exception e) {
            log.warn("Error listing memory: {}", e.getMessage());
        }
        return result;
    }

    public List<MemoryMessageInfo> getSessionMemory(String sessionId) {
        return List.of(); // HarnessAgent管理内部记忆，通过Session恢复
    }

    public List<MemorySearchResult> searchMemory(MemorySearchRequest request) {
        return List.of();
    }

    public void deleteSessionMemory(String sessionId) {
        try {
            jsonSession.delete(SimpleSessionKey.of(sessionId));
        } catch (Exception e) {
            log.warn("Failed to delete session memory: {}", e.getMessage());
        }
    }

    public void clearSessionMemory(String sessionId) {
        deleteSessionMemory(sessionId);
    }

    public void clearAllMemory() {
        try {
            Path sessionPath = Path.of("./data/sessions");
            if (Files.exists(sessionPath)) {
                try (Stream<Path> dirs = Files.list(sessionPath).filter(Files::isDirectory)) {
                    dirs.forEach(dir -> {
                        try {
                            Files.list(dir).forEach(f -> {
                                try { Files.delete(f); } catch (Exception ignored) {}
                            });
                            Files.deleteIfExists(dir);
                        } catch (Exception ignored) {}
                    });
                }
            }
        } catch (Exception e) {
            log.warn("Error clearing all memory: {}", e.getMessage());
        }
    }

    private Long getCurrentUserId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            return 1L;
        }
    }
}
