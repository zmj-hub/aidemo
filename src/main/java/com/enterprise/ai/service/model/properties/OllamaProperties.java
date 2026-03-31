package com.enterprise.ai.service.model.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Ollama模型配置属性类
 * 用于读取配置文件中ollama相关配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "model.ollama")
public class OllamaProperties {

    /**
     * 是否启用Ollama模型
     */
    private Boolean enabled = false;

    /**
     * Ollama服务基础URL
     */
    private String baseUrl = "http://localhost:11434";

    /**
     * 超时时间（毫秒）
     */
    private Integer timeout = 60000;

    /**
     * 最大重试次数
     */
    private Integer maxRetries = 3;

    /**
     * 可用模型列表
     */
    private String[] models = {"llama2", "llama3", "mistral"};
}
