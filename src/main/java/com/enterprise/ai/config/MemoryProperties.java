package com.enterprise.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "memory")
public class MemoryProperties {

    /**
     * 默认记忆窗口大小（最近N条消息）
     */
    private Integer windowSize = 10;

    /**
     * Redis中记忆数据的key前缀
     */
    private String keyPrefix = "ai:session:memory:";

    /**
     * 记忆过期时间（小时）
     */
    private Integer ttlHours = 24;

    /**
     * 触发摘要的消息数量阈值
     */
    private Integer summaryThreshold = 10;

    /**
     * 摘要时保留的最近消息数量
     */
    private Integer keepRecentCount = 3;

    /**
     * 用于摘要生成的默认模型编码
     */
    private String summaryModelCode = "qwen-turbo";

    /**
     * 向量检索：返回的最大结果数（top-k）
     */
    private Integer vectorSearchTopK = 5;

    /**
     * 向量检索：最小相似度分数阈值（0-1之间）
     */
    private Double vectorSearchMinScore = 0.7;

    /**
     * 是否启用记忆检索功能
     */
    private Boolean enableMemoryRetrieval = true;
}
