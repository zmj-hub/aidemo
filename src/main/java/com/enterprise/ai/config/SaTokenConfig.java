package com.enterprise.ai.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token配置类
 * 负责配置Sa-Token的拦截器和路由规则
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 注册Sa-Token拦截器
     * 
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 暂时不限制任何接口访问
        registry.addInterceptor(new SaInterceptor(handle -> {
            SaRouter.match("/**")
                    .check(r -> { /* 暂时无需登录 */ });
        })).addPathPatterns("/**");
        
        /* 原有逻辑，暂时注释
        registry.addInterceptor(new SaInterceptor(handle -> {
            SaRouter.match("/**")
                    .notMatch("/auth/login", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/webjars/**", "/doc.html")
                    .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
        
        // 确保Swagger相关路径都能正常访问
        registry.addInterceptor(new SaInterceptor(handle -> {
            SaRouter.match("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/webjars/**", "/doc.html")
                    .check(r -> { });
        })).addPathPatterns("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/webjars/**", "/doc.html");
        */
    }
}
