package com.enterprise.ai.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.enterprise.ai.common.result.Result;
import com.enterprise.ai.domain.dto.LoginRequest;
import com.enterprise.ai.domain.dto.LoginResponse;
import com.enterprise.ai.domain.entity.User;
import com.enterprise.ai.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 提供登录、登出等认证相关接口
 */
@Tag(name = "认证模块", description = "用户认证相关接口")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录接口
     * 
     * @param loginRequest 登录请求参数
     * @return 登录结果，包含访问令牌
     */
    @Operation(summary = "用户登录", description = "根据用户名和密码进行登录认证")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        User user = userService.validateUser(loginRequest.getUsername(), loginRequest.getPassword());
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();
        LoginResponse response = new LoginResponse(token, user.getUsername(), user.getNickname(), user.getAvatar());
        return Result.success("登录成功", response);
    }

    /**
     * 用户登出接口
     * 
     * @return 登出结果
     */
    @Operation(summary = "用户登出", description = "退出当前登录状态")
    @SaCheckLogin
    @PostMapping("/logout")
    public Result<String> logout() {
        StpUtil.logout();
        return Result.success("登出成功");
    }

    /**
     * 获取当前登录用户信息接口
     * 
     * @return 当前用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @SaCheckLogin
    @GetMapping("/current")
    public Result<User> getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getUserById(userId);
        return Result.success(user);
    }

    /**
     * 示例接口：演示@SaCheckLogin注解的使用方式
     * 需要登录后才能访问
     * 
     * @return 测试消息
     */
    @Operation(summary = "需要登录的接口示例", description = "演示@SaCheckLogin注解的使用方式")
    @SaCheckLogin
    @GetMapping("/protected")
    public Result<String> protectedResource() {
        return Result.success("这是需要登录才能访问的受保护资源");
    }
}
