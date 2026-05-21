package com.enterprise.ai.domain.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum ModelProvider {
    QWEN_TURBO("modelscope:qwen-turbo", "Qwen-Turbo", "ModelScope"),
    QWEN_MAX("modelscope:Qwen/Qwen3.5-122B-A10B", "Qwen-Max", "ModelScope"),
    QWEN_PLUS("modelscope:qwen-plus", "Qwen-Plus", "ModelScope"),
    QWEN2_7B_INSTRUCT("modelscope:qwen2-7b-instruct", "Qwen2-7B", "ModelScope"),
    LLAMA3_8B_INSTRUCT("modelscope:llama3-8b-instruct", "Llama3-8B", "ModelScope"),
    QWEN3_5_397B_A17B("modelscope:Qwen/Qwen3.5-397B-A17B", "Qwen3.5-397B", "ModelScope"),
    QWEN3_5_122B_A10B("modelscope:Qwen/Qwen3.5-122B-A10B", "Qwen3.5-122B", "ModelScope");

    private final String code;
    private final String name;
    private final String provider;

    ModelProvider(String code, String name, String provider) {
        this.code = code;
        this.name = name;
        this.provider = provider;
    }

    public static List<String> getAllCodes() {
        return Arrays.stream(values())
                .map(ModelProvider::getCode)
                .collect(Collectors.toList());
    }

    public static ModelProvider fromCode(String code) {
        return Arrays.stream(values())
                .filter(p -> p.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
