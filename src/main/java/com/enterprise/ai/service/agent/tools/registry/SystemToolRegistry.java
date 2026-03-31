package com.enterprise.ai.service.agent.tools.registry;

import com.enterprise.ai.service.agent.tools.ToolManager;
import com.enterprise.ai.service.agent.tools.ToolRegistry;
import com.enterprise.ai.service.agent.tools.system.CalculatorTool;
import com.enterprise.ai.service.agent.tools.system.TimeTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 系统工具注册器
 * 通过SPI机制注册系统内置工具
 */
@Slf4j
@Component
public class SystemToolRegistry implements ToolRegistry {

    @Override
    public void registerTools(ToolManager toolManager) {
        log.info("开始注册系统工具...");
        
        toolManager.registerTool(new TimeTool());
        toolManager.registerTool(new CalculatorTool());
        
        log.info("系统工具注册完成");
    }
}
