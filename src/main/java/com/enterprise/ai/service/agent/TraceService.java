package com.enterprise.ai.service.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 推理链路追踪服务
 * 用于记录和管理Agent推理过程的完整链路信息，支持链路查询和分析
 */
@Slf4j
@Service
public class TraceService {

    /**
     * 追踪记录存储（实际项目中建议使用数据库存储）
     */
    private final Map<String, TraceRecord> traceStore = new ConcurrentHashMap<>();

    /**
     * 会话的追踪记录索引
     */
    private final Map<String, List<String>> sessionTraceIndex = new ConcurrentHashMap<>();

    /**
     * 用户的追踪记录索引
     */
    private final Map<Long, List<String>> userTraceIndex = new ConcurrentHashMap<>();

    /**
     * 开始追踪
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param agentName Agent名称
     * @return 追踪ID
     */
    public String startTrace(String sessionId, Long userId, String agentName) {
        TraceRecord traceRecord = TraceRecord.create(sessionId, userId, agentName);
        traceStore.put(traceRecord.getTraceId(), traceRecord);
        
        sessionTraceIndex.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(traceRecord.getTraceId());
        
        if (userId != null) {
            userTraceIndex.computeIfAbsent(userId, k -> new ArrayList<>()).add(traceRecord.getTraceId());
        }
        
        log.info("开始追踪: traceId={}, sessionId={}, userId={}, agentName={}", 
                traceRecord.getTraceId(), sessionId, userId, agentName);
        return traceRecord.getTraceId();
    }

    /**
     * 设置用户输入
     * 
     * @param traceId 追踪ID
     * @param userInput 用户输入
     */
    public void setUserInput(String traceId, String userInput) {
        TraceRecord traceRecord = traceStore.get(traceId);
        if (traceRecord != null) {
            traceRecord.setUserInput(userInput);
        }
    }

    /**
     * 设置Agent输出
     * 
     * @param traceId 追踪ID
     * @param agentOutput Agent输出
     */
    public void setAgentOutput(String traceId, String agentOutput) {
        TraceRecord traceRecord = traceStore.get(traceId);
        if (traceRecord != null) {
            traceRecord.setAgentOutput(agentOutput);
        }
    }

    /**
     * 记录工具调用开始
     * 
     * @param traceId 追踪ID
     * @param toolName 工具名称
     * @param params 工具参数
     * @return 工具调用ID
     */
    public String startToolCall(String traceId, String toolName, Map<String, Object> params) {
        TraceRecord traceRecord = traceStore.get(traceId);
        if (traceRecord != null) {
            TraceRecord.ToolCallRecord toolCall = TraceRecord.ToolCallRecord.create(toolName, params);
            traceRecord.addToolCall(toolCall);
            log.debug("工具调用开始: traceId={}, toolName={}, callId={}", traceId, toolName, toolCall.getCallId());
            return toolCall.getCallId();
        }
        return null;
    }

    /**
     * 记录工具调用结束
     * 
     * @param traceId 追踪ID
     * @param callId 工具调用ID
     * @param success 是否成功
     * @param errorMessage 错误信息
     * @param result 执行结果
     */
    public void endToolCall(String traceId, String callId, boolean success, String errorMessage, Object result) {
        TraceRecord traceRecord = traceStore.get(traceId);
        if (traceRecord != null && traceRecord.getToolCalls() != null) {
            for (TraceRecord.ToolCallRecord toolCall : traceRecord.getToolCalls()) {
                if (toolCall.getCallId().equals(callId)) {
                    toolCall.complete(success, errorMessage, result);
                    log.debug("工具调用结束: traceId={}, callId={}, success={}", traceId, callId, success);
                    break;
                }
            }
        }
    }

    /**
     * 结束追踪
     * 
     * @param traceId 追踪ID
     * @param success 是否成功
     * @param errorMessage 错误信息
     */
    public void endTrace(String traceId, boolean success, String errorMessage) {
        TraceRecord traceRecord = traceStore.get(traceId);
        if (traceRecord != null) {
            traceRecord.finish(success, errorMessage);
            log.info("结束追踪: traceId={}, success={}, duration={}ms", 
                    traceId, success, traceRecord.getDurationMs());
        }
    }

    /**
     * 根据追踪ID获取追踪记录
     * 
     * @param traceId 追踪ID
     * @return 追踪记录
     */
    public TraceRecord getTrace(String traceId) {
        return traceStore.get(traceId);
    }

    /**
     * 获取会话的所有追踪记录
     * 
     * @param sessionId 会话ID
     * @return 追踪记录列表
     */
    public List<TraceRecord> getTracesBySession(String sessionId) {
        List<String> traceIds = sessionTraceIndex.get(sessionId);
        if (traceIds == null) {
            return new ArrayList<>();
        }
        return traceIds.stream()
                .map(traceStore::get)
                .filter(t -> t != null)
                .toList();
    }

    /**
     * 获取用户的所有追踪记录
     * 
     * @param userId 用户ID
     * @return 追踪记录列表
     */
    public List<TraceRecord> getTracesByUser(Long userId) {
        List<String> traceIds = userTraceIndex.get(userId);
        if (traceIds == null) {
            return new ArrayList<>();
        }
        return traceIds.stream()
                .map(traceStore::get)
                .filter(t -> t != null)
                .toList();
    }

    /**
     * 清除指定会话的追踪记录
     * 
     * @param sessionId 会话ID
     */
    public void clearSessionTraces(String sessionId) {
        List<String> traceIds = sessionTraceIndex.remove(sessionId);
        if (traceIds != null) {
            for (String traceId : traceIds) {
                TraceRecord traceRecord = traceStore.remove(traceId);
                if (traceRecord != null && traceRecord.getUserId() != null) {
                    List<String> userTraces = userTraceIndex.get(traceRecord.getUserId());
                    if (userTraces != null) {
                        userTraces.remove(traceId);
                    }
                }
            }
        }
        log.info("清除会话追踪记录: sessionId={}", sessionId);
    }

    /**
     * 添加扩展信息
     * 
     * @param traceId 追踪ID
     * @param key 扩展信息键
     * @param value 扩展信息值
     */
    public void addExtra(String traceId, String key, Object value) {
        TraceRecord traceRecord = traceStore.get(traceId);
        if (traceRecord != null) {
            if (traceRecord.getExtra() == null) {
                traceRecord.setExtra(new ConcurrentHashMap<>());
            }
            traceRecord.getExtra().put(key, value);
        }
    }
}
