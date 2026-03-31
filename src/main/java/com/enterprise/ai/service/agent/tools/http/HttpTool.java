package com.enterprise.ai.service.agent.tools.http;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import com.enterprise.ai.service.agent.tools.BaseTool;
import com.enterprise.ai.service.agent.tools.ToolResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP请求工具
 * 支持GET、POST、PUT、DELETE等HTTP请求
 */
@Slf4j
@Component
public class HttpTool extends BaseTool {

    /**
     * 工具名称
     */
    private static final String TOOL_NAME = "http";

    /**
     * 工具描述
     */
    private static final String TOOL_DESCRIPTION = "发送HTTP请求，支持GET、POST、PUT、DELETE等方法";

    /**
     * 参数定义
     */
    private static final Map<String, String> PARAMS;

    static {
        PARAMS = new HashMap<>();
        PARAMS.put("url", "请求的URL地址（必需）");
        PARAMS.put("method", "HTTP方法：GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS（默认GET）");
        PARAMS.put("headers", "请求头，JSON格式（可选）");
        PARAMS.put("body", "请求体内容（POST/PUT等方法使用）");
        PARAMS.put("timeout", "请求超时时间（毫秒，默认30000）");
        PARAMS.put("followRedirects", "是否跟随重定向（true/false，默认true）");
    }

    public HttpTool() {
        super(TOOL_NAME, TOOL_DESCRIPTION, PARAMS);
    }

    @Override
    protected ToolResult doExecute(Map<String, Object> params) {
        String url = getParamAsString(params, "url", null);
        if (url == null || url.isBlank()) {
            return ToolResult.failure("必须提供请求URL");
        }

        String methodStr = getParamAsString(params, "method", "GET");
        Method method;
        try {
            method = Method.valueOf(methodStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ToolResult.failure("不支持的HTTP方法: " + methodStr);
        }

        Integer timeout = getParamAsInteger(params, "timeout", 30000);
        Boolean followRedirects = getParamAsBoolean(params, "followRedirects", true);
        String body = getParamAsString(params, "body", null);
        Map<String, String> headers = parseHeaders(getParamAsString(params, "headers", null));

        try {
            HttpRequest request = HttpRequest.of(url)
                    .method(method)
                    .timeout(timeout)
                    .setFollowRedirects(followRedirects);

            if (headers != null && !headers.isEmpty()) {
                headers.forEach(request::header);
            }

            if (body != null && !body.isBlank()) {
                request.body(body);
            }

            HttpResponse response = request.execute();

            Map<String, Object> result = new HashMap<>();
            result.put("url", url);
            result.put("method", method.name());
            result.put("statusCode", response.getStatus());
            result.put("statusText", response.body().length() > 1000 ? response.body().substring(0, 1000) + "..." : response.body());
            result.put("body", response.body());
            result.put("headers", response.headers());
            result.put("isOk", response.isOk());

            return ToolResult.success(result);
        } catch (Exception e) {
            log.error("HTTP请求失败: url={}, method={}", url, method, e);
            return ToolResult.failure("HTTP请求失败: " + e.getMessage());
        }
    }

    /**
     * 解析请求头
     * 
     * @param headersJson 请求头JSON字符串
     * @return 请求头Map
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> parseHeaders(String headersJson) {
        if (headersJson == null || headersJson.isBlank()) {
            return new HashMap<>();
        }
        try {
            return com.enterprise.ai.common.utils.JsonUtils.fromJson(headersJson, Map.class);
        } catch (Exception e) {
            log.warn("解析请求头失败，使用空请求头: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}
