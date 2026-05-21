package com.enterprise.ai.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
public class SystemTools {

    @Tool(name = "get_current_time", description = "获取当前时间，支持指定时区")
    public String getTime(
            @ToolParam(name = "timezone", description = "时区，如Asia/Shanghai、America/New_York，默认Asia/Shanghai") String timezone) {
        try {
            ZoneId zone = timezone != null && !timezone.isEmpty()
                    ? ZoneId.of(timezone)
                    : ZoneId.of("Asia/Shanghai");
            LocalDateTime now = LocalDateTime.now(zone);
            return "当前时间: " + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    + " (时区: " + zone + ")";
        } catch (Exception e) {
            return "时间获取失败: " + e.getMessage();
        }
    }

    @Tool(name = "calculator", description = "执行数学计算，支持add/subtract/multiply/divide/power/sqrt操作")
    public String calculate(
            @ToolParam(name = "operation", description = "运算类型: add, subtract, multiply, divide, power, sqrt") String operation,
            @ToolParam(name = "a", description = "第一个操作数") double a,
            @ToolParam(name = "b", description = "第二个操作数（sqrt操作不需要）") Double b) {
        try {
            double result = switch (operation.toLowerCase()) {
                case "add" -> a + (b != null ? b : 0);
                case "subtract" -> a - (b != null ? b : 0);
                case "multiply" -> a * (b != null ? b : 1);
                case "divide" -> {
                    if (b == null || b == 0) throw new ArithmeticException("除数不能为零");
                    yield a / b;
                }
                case "power" -> Math.pow(a, b != null ? b : 1);
                case "sqrt" -> Math.sqrt(a);
                default -> throw new IllegalArgumentException("不支持的运算: " + operation);
            };
            return String.format("计算结果: %s(%s, %s) = %s", operation, a, b, result);
        } catch (Exception e) {
            return "计算失败: " + e.getMessage();
        }
    }
}
