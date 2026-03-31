package com.enterprise.ai.service.agent.tools.system;

import com.enterprise.ai.common.utils.DateUtils;
import com.enterprise.ai.service.agent.tools.BaseTool;
import com.enterprise.ai.service.agent.tools.ToolResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 时间工具
 * 提供获取当前时间、日期格式化等功能
 */
@Slf4j
@Component
public class TimeTool extends BaseTool {

    /**
     * 工具名称
     */
    private static final String TOOL_NAME = "time";

    /**
     * 工具描述
     */
    private static final String TOOL_DESCRIPTION = "获取当前时间和日期，支持各种日期格式化";

    /**
     * 参数定义
     */
    private static final Map<String, String> PARAMS;

    static {
        PARAMS = new HashMap<>();
        PARAMS.put("pattern", "日期时间格式模式，如：yyyy-MM-dd HH:mm:ss（可选）");
        PARAMS.put("timezone", "时区，如：Asia/Shanghai（可选，默认使用系统时区）");
    }

    public TimeTool() {
        super(TOOL_NAME, TOOL_DESCRIPTION, PARAMS);
    }

    @Override
    protected ToolResult doExecute(Map<String, Object> params) {
        String pattern = getParamAsString(params, "pattern", DateUtils.DEFAULT_PATTERN);
        
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", System.currentTimeMillis());
        result.put("formattedTime", DateUtils.nowStr(pattern));
        result.put("date", DateUtils.nowStr(DateUtils.DATE_PATTERN));
        result.put("time", DateUtils.nowStr(DateUtils.TIME_PATTERN));
        result.put("year", java.time.LocalDate.now().getYear());
        result.put("month", java.time.LocalDate.now().getMonthValue());
        result.put("day", java.time.LocalDate.now().getDayOfMonth());
        result.put("hour", java.time.LocalTime.now().getHour());
        result.put("minute", java.time.LocalTime.now().getMinute());
        result.put("second", java.time.LocalTime.now().getSecond());
        result.put("dayOfWeek", java.time.LocalDate.now().getDayOfWeek().toString());
        
        return ToolResult.success(result);
    }
}
