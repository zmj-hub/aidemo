package com.enterprise.ai.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
public class HttpTool {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Tool(name = "http_request", description = "发送HTTP请求，支持GET/POST等方法")
    public String httpRequest(
            @ToolParam(name = "url", description = "请求URL") String url,
            @ToolParam(name = "method", description = "请求方法: GET, POST, PUT, DELETE，默认GET") String method,
            @ToolParam(name = "body", description = "请求体（JSON格式，仅POST/PUT使用）") String body) {
        try {
            String httpMethod = method != null ? method.toUpperCase() : "GET";
            var requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30));

            if ("POST".equals(httpMethod) || "PUT".equals(httpMethod)) {
                requestBuilder.method(httpMethod,
                        HttpRequest.BodyPublishers.ofString(body != null ? body : ""));
            } else {
                requestBuilder.GET();
            }

            HttpResponse<String> response = httpClient.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString());

            return String.format("HTTP %s %s → %d\n%s",
                    httpMethod, url, response.statusCode(),
                    response.body().length() > 2000
                            ? response.body().substring(0, 2000) + "..."
                            : response.body());
        } catch (Exception e) {
            return "HTTP请求失败: " + e.getMessage();
        }
    }
}
