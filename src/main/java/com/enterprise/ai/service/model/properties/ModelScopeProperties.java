package com.enterprise.ai.service.model.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ModelScope模型配置属性类
 * 用于读取配置文件中modelscope相关配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "model.modelscope")
public class ModelScopeProperties {

    /**
     * 是否启用ModelScope模型
     */
    private Boolean enabled = false;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * API基础URL
     */
    private String baseUrl = "https://api-inference.modelscope.cn/v1";

    /**
     * 超时时间（毫秒）
     */
    private Integer timeout = 60000;

    /**
     * 最大重试次数
     */
    private Integer maxRetries = 3;

    /**
     * 支持的模型列表
     */
    private List<String> models;
}
