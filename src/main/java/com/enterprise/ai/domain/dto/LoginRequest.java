package com.enterprise.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录请求DTO
 */
@Data
@Schema(description = "登录请求参数")
public class LoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "admin", required = true)
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456", required = true)
    private String password;
}
