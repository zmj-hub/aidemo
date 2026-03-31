package com.enterprise.ai.service.agent.tools;

/**
 * 工具注册器接口
 * 通过SPI机制实现工具的自动发现和注册
 */
public interface ToolRegistry {

    /**
     * 注册工具
     * 
     * @param toolManager 工具管理器
     */
    void registerTools(ToolManager toolManager);
}
