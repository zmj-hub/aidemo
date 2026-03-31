package com.enterprise.ai.common.utils;

import cn.dev33.satoken.stp.StpUtil;
import com.enterprise.ai.domain.entity.User;
import com.enterprise.ai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用户上下文工具类
 * 用于在当前线程中保存和获取用户信息，实现用户级数据隔离
 */
@Component
public class UserContextHolder {

    private static UserService userService;

    /**
     * 注入UserService
     * 
     * @param userService 用户服务
     */
    @Autowired
    public void setUserService(UserService userService) {
        UserContextHolder.userService = userService;
    }

    /**
     * 存储当前线程的用户ID
     */
    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    /**
     * 存储当前线程的用户信息
     */
    private static final ThreadLocal<User> USER_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     * 
     * @param userId 用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    /**
     * 获取当前用户ID
     * 
     * @return 用户ID
     */
    public static Long getUserId() {
        Long userId = USER_ID_HOLDER.get();
        if (userId == null) {
            try {
                userId = StpUtil.getLoginIdAsLong();
                USER_ID_HOLDER.set(userId);
            } catch (Exception e) {
                return null;
            }
        }
        return userId;
    }

    /**
     * 设置当前用户信息
     * 
     * @param user 用户信息
     */
    public static void setUser(User user) {
        USER_HOLDER.set(user);
        if (user != null) {
            USER_ID_HOLDER.set(user.getId());
        }
    }

    /**
     * 获取当前用户信息
     * 
     * @return 用户信息
     */
    public static User getUser() {
        User user = USER_HOLDER.get();
        if (user == null) {
            Long userId = getUserId();
            if (userId != null && userService != null) {
                user = userService.getUserById(userId);
                USER_HOLDER.set(user);
            }
        }
        return user;
    }

    /**
     * 清除当前线程的用户上下文信息
     * 防止内存泄漏
     */
    public static void clear() {
        USER_ID_HOLDER.remove();
        USER_HOLDER.remove();
    }
}
