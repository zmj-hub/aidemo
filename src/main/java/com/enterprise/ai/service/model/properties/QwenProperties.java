package com.enterprise.ai.service.model.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 通义千问模型配置属性类
 * 用于读取配置文件中qwen相关配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "model.qwen")
public class QwenProperties {

    /**
     * 是否启用通义千问模型
     */
    private Boolean enabled = false;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * API基础URL
     */
    private String baseUrl = "https://dashscope.aliyuncs.com";

    /**
     * 超时时间（毫秒）
     */
    private Integer timeout = 60000;

    /**
     * 最大重试次数
     */
    private Integer maxRetries = 3;
}
