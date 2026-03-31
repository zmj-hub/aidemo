package com.enterprise.ai.domain.enums;

import lombok.Getter;

/**
 * 模型提供方枚举
 * 定义系统支持的所有AI模型
 */
@Getter
public enum ModelProvider {

    QWEN_TURBO("qwen-turbo", "通义千问Turbo", "modelscope"),
    QWEN_MAX("qwen-max", "通义千问Max", "modelscope"),
    QWEN_PLUS("qwen-plus", "通义千问Plus", "modelscope"),
    QWEN2_7B_INSTRUCT("qwen2-7b-instruct", "Qwen2-7B-Instruct", "modelscope"),
    QWEN3_5_397B_A17B("Qwen/Qwen3.5-397B-A17B", "Qwen3.5-397B-A17B", "modelscope"),
    QWEN3_5_122B_A10B("Qwen/Qwen3.5-122B-A10B", "Qwen3.5-122B-A10B", "modelscope"),
    LLAMA3_8B_INSTRUCT("llama3-8b-instruct", "Llama3-8B-Instruct", "modelscope"),
    OLLAMA_LLAMA2("ollama-llama2", "Ollama Llama2", "ollama"),
    OLLAMA_LLAMA3("ollama-llama3", "Ollama Llama3", "ollama"),
    OLLAMA_MISTRAL("ollama-mistral", "Ollama Mistral", "ollama");

    /**
     * 模型编码
     */
    private final String code;

    /**
     * 模型名称
     */
    private final String name;

    /**
     * 模型提供商
     */
    private final String provider;

    ModelProvider(String code, String name, String provider) {
        this.code = code;
        this.name = name;
        this.provider = provider;
    }

    /**
     * 根据模型编码获取枚举
     * 
     * @param code 模型编码
     * @return 模型枚举，如果不存在返回null
     */
    public static ModelProvider fromCode(String code) {
        for (ModelProvider provider : values()) {
            if (provider.getCode().equals(code)) {
                return provider;
            }
        }
        return null;
    }
}
