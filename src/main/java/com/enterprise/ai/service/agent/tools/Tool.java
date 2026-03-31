package com.enterprise.ai.service.agent.tools;

import java.util.Map;

/**
 * Agent工具接口
 * 所有Agent工具都需要实现此接口，提供工具的基本信息和执行功能
 */
public interface Tool {

    /**
     * 获取工具名称
     * 
     * @return 工具唯一标识名称
     */
    String getName();

    /**
     * 获取工具描述
     * 
     * @return 工具功能描述
     */
    String getDescription();

    /**
     * 获取工具参数定义
     * 
     * @return 参数定义Map，key为参数名，value为参数描述
     */
    Map<String, String> getParams();

    /**
     * 执行工具
     * 
     * @param params 工具参数
     * @return 执行结果
     */
    ToolResult execute(Map<String, Object> params);

    /**
     * 检查工具是否可用
     * 
     * @return 工具可用性状态
     */
    default boolean isAvailable() {
        return true;
    }
}
