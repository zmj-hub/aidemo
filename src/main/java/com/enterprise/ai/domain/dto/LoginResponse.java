package com.enterprise.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 访问令牌
     */
    @Schema(description = "访问令牌，后续请求需在Header中携带此Token")
    private String token;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "admin")
    private String username;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称", example = "管理员")
    private String nickname;

    /**
     * 用户头像
     */
    @Schema(description = "用户头像URL")
    private String avatar;
}
