package com.enterprise.ai.service;

import cn.hutool.crypto.digest.BCrypt;
import com.enterprise.ai.common.exception.BusinessException;
import com.enterprise.ai.domain.entity.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务类
 * 提供用户查询、登录验证等功能
 */
@Service
public class UserService {

    /**
     * 模拟用户数据库
     */
    private static final Map<String, User> USER_DB = new HashMap<>();

    static {
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPassword(BCrypt.hashpw("123456"));
        admin.setNickname("系统管理员");
        admin.setEmail("admin@example.com");
        admin.setPhone("13800138000");
        admin.setAvatar("https://example.com/avatar/admin.jpg");
        admin.setStatus(1);
        admin.setCreateTime(LocalDateTime.now());
        admin.setUpdateTime(LocalDateTime.now());
        USER_DB.put("admin", admin);

        User user = new User();
        user.setId(2L);
        user.setUsername("user");
        user.setPassword(BCrypt.hashpw("123456"));
        user.setNickname("普通用户");
        user.setEmail("user@example.com");
        user.setPhone("13800138001");
        user.setAvatar("https://example.com/avatar/user.jpg");
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        USER_DB.put("user", user);
    }

    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    public User getUserByUsername(String username) {
        return USER_DB.get(username);
    }

    /**
     * 根据用户ID查询用户
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    public User getUserById(Long userId) {
        return USER_DB.values().stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 验证用户名和密码
     * 
     * @param username 用户名
     * @param password 密码
     * @return 用户信息
     */
    public User validateUser(String username, String password) {
        User user = getUserByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("用户已被禁用");
        }
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        return user;
    }
}
