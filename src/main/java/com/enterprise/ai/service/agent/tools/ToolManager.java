package com.enterprise.ai.service.agent.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具管理器
 * 负责工具的注册、发现、获取和管理
 */
@Slf4j
@Component
public class ToolManager {

    /**
     * 工具存储池
     */
    private final Map<String, Tool> toolPool = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private List<Tool> springManagedTools;

    /**
     * 初始化工具管理器
     */
    @PostConstruct
    public void init() {
        log.info("开始初始化工具管理器...");
        
        if (springManagedTools != null) {
            for (Tool tool : springManagedTools) {
                registerTool(tool);
            }
        }
        
        loadSpiTools();
        
        log.info("工具管理器初始化完成，共注册{}个工具", toolPool.size());
    }

    /**
     * 通过SPI加载工具
     */
    private void loadSpiTools() {
        try {
            ServiceLoader<ToolRegistry> serviceLoader = ServiceLoader.load(ToolRegistry.class);
            for (ToolRegistry registry : serviceLoader) {
                log.info("发现工具注册器: {}", registry.getClass().getName());
                registry.registerTools(this);
            }
        } catch (Exception e) {
            log.warn("SPI工具加载失败", e);
        }
    }

    /**
     * 注册工具
     * 
     * @param tool 工具实例
     */
    public void registerTool(Tool tool) {
        if (tool == null) {
            log.warn("尝试注册空工具");
            return;
        }
        
        String toolName = tool.getName();
        if (toolName == null || toolName.isBlank()) {
            log.warn("工具名称不能为空，跳过注册: {}", tool.getClass().getName());
            return;
        }
        
        if (toolPool.containsKey(toolName)) {
            log.warn("工具已存在，将被覆盖: {}", toolName);
        }
        
        toolPool.put(toolName, tool);
        log.info("工具注册成功: {}", toolName);
    }

    /**
     * 注销工具
     * 
     * @param toolName 工具名称
     */
    public void unregisterTool(String toolName) {
        Tool removed = toolPool.remove(toolName);
        if (removed != null) {
            log.info("工具注销成功: {}", toolName);
        } else {
            log.warn("工具不存在，无法注销: {}", toolName);
        }
    }

    /**
     * 获取工具
     * 
     * @param toolName 工具名称
     * @return 工具实例，如果不存在返回null
     */
    public Tool getTool(String toolName) {
        Tool tool = toolPool.get(toolName);
        if (tool != null && !tool.isAvailable()) {
            log.warn("工具不可用: {}", toolName);
            return null;
        }
        return tool;
    }

    /**
     * 获取所有可用工具
     * 
     * @return 工具列表
     */
    public List<Tool> getAllTools() {
        return toolPool.values().stream()
                .filter(Tool::isAvailable)
                .toList();
    }

    /**
     * 获取所有工具名称
     * 
     * @return 工具名称列表
     */
    public Set<String> getAllToolNames() {
        return toolPool.keySet();
    }

    /**
     * 检查工具是否存在且可用
     * 
     * @param toolName 工具名称
     * @return 是否存在且可用
     */
    public boolean hasTool(String toolName) {
        Tool tool = toolPool.get(toolName);
        return tool != null && tool.isAvailable();
    }

    /**
     * 根据工具名称列表获取工具
     * 
     * @param toolNames 工具名称列表
     * @return 工具列表
     */
    public List<Tool> getTools(Collection<String> toolNames) {
        if (toolNames == null || toolNames.isEmpty()) {
            return new ArrayList<>();
        }
        return toolNames.stream()
                .map(this::getTool)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 清空所有工具
     */
    public void clear() {
        toolPool.clear();
        log.info("已清空所有工具");
    }
}
