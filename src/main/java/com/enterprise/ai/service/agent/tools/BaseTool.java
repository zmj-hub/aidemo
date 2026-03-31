package com.enterprise.ai.service.agent.tools;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 工具基类
 * 提供工具的通用功能，包括参数校验、日志记录等
 */
@Slf4j
public abstract class BaseTool implements Tool {

    /**
     * 工具名称
     */
    protected final String name;

    /**
     * 工具描述
     */
    protected final String description;

    /**
     * 参数定义
     */
    protected final Map<String, String> params;

    /**
     * 构造函数
     * 
     * @param name 工具名称
     * @param description 工具描述
     * @param params 参数定义
     */
    protected BaseTool(String name, String description, Map<String, String> params) {
        this.name = name;
        this.description = description;
        this.params = params;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }

    @Override
    public ToolResult execute(Map<String, Object> params) {
        long startTime = System.currentTimeMillis();
        try {
            log.info("开始执行工具: {}, 参数: {}", name, params);
            
            validateParams(params);
            
            ToolResult result = doExecute(params);
            result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            
            log.info("工具执行成功: {}, 耗时: {}ms", name, result.getExecutionTimeMs());
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("工具执行失败: {}, 耗时: {}ms, 错误: {}", name, executionTime, e.getMessage(), e);
            return ToolResult.failure(e.getMessage())
                    .withExecutionTime(executionTime);
        }
    }

    /**
     * 执行工具的具体逻辑
     * 子类需要实现此方法
     * 
     * @param params 工具参数
     * @return 执行结果
     */
    protected abstract ToolResult doExecute(Map<String, Object> params);

    /**
     * 校验参数
     * 默认实现，子类可以覆盖
     * 
     * @param params 工具参数
     */
    protected void validateParams(Map<String, Object> params) {
        if (params == null) {
            params = new java.util.HashMap<>();
        }
        
        for (Map.Entry<String, String> entry : this.params.entrySet()) {
            String paramName = entry.getKey();
            if (!params.containsKey(paramName) || params.get(paramName) == null) {
                log.warn("参数校验警告: 缺少参数 {}, 描述: {}", paramName, entry.getValue());
            }
        }
    }

    /**
     * 获取字符串参数
     * 
     * @param params 参数Map
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    protected String getParamAsString(Map<String, Object> params, String key, String defaultValue) {
        Object value = params.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * 获取整数参数
     * 
     * @param params 参数Map
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    protected Integer getParamAsInteger(Map<String, Object> params, String key, Integer defaultValue) {
        Object value = params.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * 获取长整数参数
     * 
     * @param params 参数Map
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    protected Long getParamAsLong(Map<String, Object> params, String key, Long defaultValue) {
        Object value = params.get(key);
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * 获取布尔参数
     * 
     * @param params 参数Map
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    protected Boolean getParamAsBoolean(Map<String, Object> params, String key, Boolean defaultValue) {
        Object value = params.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }
}
