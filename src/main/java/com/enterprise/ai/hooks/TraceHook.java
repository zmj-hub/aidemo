package com.enterprise.ai.hooks;

import io.agentscope.core.hook.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class TraceHook implements Hook {

    private final Map<String, TraceInfo> traces = new ConcurrentHashMap<>();

    @Override
    public <T extends HookEvent> Mono<T> onEvent(T event) {
        if (event instanceof PreCallEvent) {
            TraceInfo trace = new TraceInfo();
            trace.traceId = UUID.randomUUID().toString();
            trace.startTime = System.currentTimeMillis();
            traces.put(trace.traceId, trace);
        } else if (event instanceof PreReasoningEvent) {
            TraceInfo trace = findCurrentTrace();
            if (trace != null) {
                trace.reasoningRounds++;
            }
        } else if (event instanceof PreActingEvent e) {
            TraceInfo trace = findCurrentTrace();
            if (trace != null) {
                trace.toolCalls.add(new ToolCallRecord("tool", System.currentTimeMillis()));
            }
        } else if (event instanceof PostCallEvent) {
            TraceInfo trace = findCurrentTrace();
            if (trace != null) {
                trace.endTime = System.currentTimeMillis();
                trace.success = true;
            }
        } else if (event instanceof ErrorEvent) {
            TraceInfo trace = findCurrentTrace();
            if (trace != null) {
                trace.success = false;
                trace.errorMessage = event.toString();
            }
        }
        return Mono.just(event);
    }

    @Override
    public int priority() {
        return 0;
    }

    private TraceInfo findCurrentTrace() {
        return traces.values().stream()
                .filter(t -> t.endTime == 0)
                .findFirst()
                .orElse(null);
    }

    public TraceInfo getCurrentTrace() {
        return findCurrentTrace();
    }

    public void clear() {
        traces.clear();
    }

    public static class TraceInfo {
        public String traceId;
        public long startTime;
        public long endTime;
        public boolean success;
        public String errorMessage;
        public int reasoningRounds;
        public final List<ToolCallRecord> toolCalls = new CopyOnWriteArrayList<>();
    }

    public record ToolCallRecord(String toolName, long timestamp) {}
}
