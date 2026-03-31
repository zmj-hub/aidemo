package com.enterprise.ai.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话实体类
 * 用于存储用户与AI的会话信息
 */
@Data
public class Session implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    private Long id;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 模型编码
     */
    private String modelCode;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 是否已归档
     */
    private Boolean archived;

    /**
     * 最后消息时间
     */
    private LocalDateTime lastMessageTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
