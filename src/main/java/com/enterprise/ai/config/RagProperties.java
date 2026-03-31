package com.enterprise.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * RAG配置属性类
 * 用于从配置文件中读取RAG相关配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "rag")
public class RagProperties {

    /**
     * 文档存储路径
     */
    private String documentStoragePath = "./data/documents";

    /**
     * 分块大小（字符数）
     */
    private Integer chunkSize = 1000;

    /**
     * 分块重叠大小（字符数）
     */
    private Integer chunkOverlap = 200;

    /**
     * 检索返回的最大文档数
     */
    private Integer maxResults = 5;

    /**
     * 最小相似度阈值
     */
    private Double minScore = 0.7;

    /**
     * 默认Embedding模型名称
     */
    private String defaultEmbeddingModel = "all-minilm-l6-v2";

    /**
     * 默认Chat模型名称
     */
    private String defaultChatModel = "qwen-turbo";

    /**
     * 向量存储类型：memory-内存存储，milvus-Milvus向量数据库，pinecone-Pinecone向量数据库，chroma-Chroma向量数据库
     */
    private String vectorStoreType = "memory";

    /**
     * Milvus配置
     */
    private MilvusConfig milvus = new MilvusConfig();

    /**
     * Pinecone配置
     */
    private PineconeConfig pinecone = new PineconeConfig();

    /**
     * Chroma配置
     */
    private ChromaConfig chroma = new ChromaConfig();

    /**
     * Milvus配置内部类
     */
    @Data
    public static class MilvusConfig {
        private String host = "localhost";
        private Integer port = 19530;
        private String collectionName = "rag_documents";
        private Integer dimension = 384;
    }

    /**
     * Pinecone配置内部类
     */
    @Data
    public static class PineconeConfig {
        private String apiKey;
        private String environment;
        private String indexName = "rag-documents";
    }

    /**
     * Chroma配置内部类
     */
    @Data
    public static class ChromaConfig {
        private String baseUrl = "http://localhost:8000";
        private String collectionName = "rag_documents";
    }
}
