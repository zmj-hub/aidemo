package com.enterprise.ai.service.agent.tools.system;

import com.enterprise.ai.service.agent.tools.BaseTool;
import com.enterprise.ai.service.agent.tools.ToolResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 计算器工具
 * 提供基本的数学运算功能
 */
@Slf4j
@Component
public class CalculatorTool extends BaseTool {

    /**
     * 工具名称
     */
    private static final String TOOL_NAME = "calculator";

    /**
     * 工具描述
     */
    private static final String TOOL_DESCRIPTION = "执行基本的数学运算，包括加、减、乘、除、取模、幂运算等";

    /**
     * 参数定义
     */
    private static final Map<String, String> PARAMS;

    static {
        PARAMS = new HashMap<>();
        PARAMS.put("operation", "运算类型：add(加), subtract(减), multiply(乘), divide(除), mod(取模), power(幂), sqrt(开方)");
        PARAMS.put("a", "第一个操作数（必需）");
        PARAMS.put("b", "第二个操作数（除开方外必需）");
    }

    public CalculatorTool() {
        super(TOOL_NAME, TOOL_DESCRIPTION, PARAMS);
    }

    @Override
    protected ToolResult doExecute(Map<String, Object> params) {
        String operation = getParamAsString(params, "operation", null);
        if (operation == null || operation.isBlank()) {
            return ToolResult.failure("必须指定运算类型（operation）");
        }

        Double a = getParamAsDouble(params, "a", null);
        if (a == null) {
            return ToolResult.failure("必须提供第一个操作数（a）");
        }

        Double b = getParamAsDouble(params, "b", null);
        Double result;

        try {
            switch (operation.toLowerCase()) {
                case "add":
                    if (b == null) return ToolResult.failure("加法需要第二个操作数（b）");
                    result = a + b;
                    break;
                case "subtract":
                    if (b == null) return ToolResult.failure("减法需要第二个操作数（b）");
                    result = a - b;
                    break;
                case "multiply":
                    if (b == null) return ToolResult.failure("乘法需要第二个操作数（b）");
                    result = a * b;
                    break;
                case "divide":
                    if (b == null) return ToolResult.failure("除法需要第二个操作数（b）");
                    if (b == 0) return ToolResult.failure("除数不能为零");
                    result = a / b;
                    break;
                case "mod":
                    if (b == null) return ToolResult.failure("取模需要第二个操作数（b）");
                    if (b == 0) return ToolResult.failure("模数不能为零");
                    result = a % b;
                    break;
                case "power":
                    if (b == null) return ToolResult.failure("幂运算需要第二个操作数（b）");
                    result = Math.pow(a, b);
                    break;
                case "sqrt":
                    if (a < 0) return ToolResult.failure("负数不能开平方根");
                    result = Math.sqrt(a);
                    break;
                default:
                    return ToolResult.failure("不支持的运算类型: " + operation);
            }

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("operation", operation);
            resultMap.put("a", a);
            resultMap.put("b", b);
            resultMap.put("result", result);
            resultMap.put("expression", buildExpression(operation, a, b, result));

            return ToolResult.success(resultMap);
        } catch (Exception e) {
            return ToolResult.failure("运算执行失败: " + e.getMessage());
        }
    }

    /**
     * 获取Double类型参数
     * 
     * @param params 参数Map
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    private Double getParamAsDouble(Map<String, Object> params, String key, Double defaultValue) {
        Object value = params.get(key);
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else if (value instanceof Float) {
            return ((Float) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * 构建运算表达式字符串
     * 
     * @param operation 运算类型
     * @param a 第一个操作数
     * @param b 第二个操作数
     * @param result 结果
     * @return 表达式字符串
     */
    private String buildExpression(String operation, Double a, Double b, Double result) {
        String op;
        switch (operation.toLowerCase()) {
            case "add": op = "+"; break;
            case "subtract": op = "-"; break;
            case "multiply": op = "×"; break;
            case "divide": op = "÷"; break;
            case "mod": op = "%"; break;
            case "power": op = "^"; break;
            case "sqrt": return String.format("√%.2f = %.2f", a, result);
            default: op = "?";
        }
        return String.format("%.2f %s %.2f = %.2f", a, op, b, result);
    }
}
