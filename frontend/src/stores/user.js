/**
 * 用户状态管理 Store
 * 
 * 功能：
 * - 管理用户Token和认证状态
 * - 存储和获取用户信息
 * - 登录/登出操作
 * - 持久化存储到localStorage
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi, getCurrentUser } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // ===========================
  // 状态定义
  // ===========================

  /** 用户访问令牌 */
  const token = ref(localStorage.getItem('token') || '')

  /** 用户详细信息对象 */
  const userInfo = ref(null)

  /** 用户角色列表 */
  const roles = ref([])

  // ===========================
  // 计算属性
  // ===========================

  /**
   * 用户是否已登录（通过判断token是否存在）
   */
  const isLoggedIn = computed(() => !!token.value)

  /**
   * 获取用户名
   */
  const username = computed(() => userInfo.value?.username || '')

  /**
   * 获取用户昵称/显示名称
   */
  const nickname = computed(() => userInfo.value?.nickname || userInfo.value?.username || '用户')

  /**
   * 获取用户头像URL
   */
  const avatar = computed(() => userInfo.value?.avatar || '')

  /**
   * 获取用户邮箱
   */
  const email = computed(() => userInfo.value?.email || '')

  /**
   * 获取用户首字母（用于默认头像显示）
   */
  const userInitial = computed(() => {
    if (nickname.value) {
      return nickname.value.charAt(0).toUpperCase()
    }
    if (username.value) {
      return username.value.charAt(0).toUpperCase()
    }
    return 'U'
  })

  /**
   * 根据用户名生成头像背景色（基于字符串哈希的稳定颜色）
   */
  const avatarColor = computed(() => {
    if (!username.value) return '#667eea'

    // 使用简单的哈希算法根据用户名生成颜色
    let hash = 0
    for (let i = 0; i < username.value.length; i++) {
      hash = username.value.charCodeAt(i) + ((hash << 5) - hash)
    }

    // 预定义的专业配色方案
    const colors = [
      '#667eea', '#764ba2', '#f093fb', '#f5576c',
      '#4facfe', '#00f2fe', '#43e97b', '#38f9d7',
      '#fa709a', '#fee140', '#a8edea', '#fed6e3',
      '#ff9a9e', '#fecfef', '#667eea', '#764ba2'
    ]

    return colors[Math.abs(hash) % colors.length]
  })

  // ===========================
  // Actions - 操作方法
  // ===========================

  /**
   * 设置用户Token
   * @param {string} newToken - 新的访问令牌
   */
  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  /**
   * 设置用户信息
   * @param {Object} user - 用户信息对象
   */
  function setUser(user) {
    userInfo.value = user
    if (user.roles) {
      roles.value = user.roles
    }
    localStorage.setItem('userInfo', JSON.stringify(user))
  }

  /**
   * 用户登录操作
   * @param {Object} loginForm - 登录表单数据 { username, password }
   * @returns {Promise<Object>} API响应数据
   */
  async function login(loginForm) {
    try {
      const res = await loginApi(loginForm)
      
      // 处理API返回的数据结构
      const data = res.data || res
      
      // 存储Token
      if (data.token) {
        setToken(data.token)
      }
      
      // 存储用户信息
      if (data.user) {
        setUser(data.user)
      } else if (data.username || data.nickname) {
        setUser(data)
      }
      
      return res
    } catch (error) {
      console.error('[UserStore] 登录失败:', error)
      throw error
    }
  }

  /**
   * 获取当前用户信息
   * 从服务器拉取最新的用户数据并更新本地状态
   * @returns {Promise<Object>} 用户信息对象
   */
  async function fetchUserInfo() {
    try {
      const res = await getCurrentUser()
      const userData = res.data || res
      setUser(userData)
      return userData
    } catch (error) {
      console.error('[UserStore] 获取用户信息失败:', error)
      throw error
    }
  }

  /**
   * 用户登出操作
   * 清除所有用户相关状态和本地存储
   * @returns {Promise<void>}
   */
  async function logout() {
    try {
      // 尝试调用后端登出接口（即使失败也要清除本地状态）
      await logoutApi()
    } catch (error) {
      console.warn('[UserStore] 调用登出API失败:', error)
    } finally {
      // 无论API调用是否成功，都清除本地状态
      resetState()
    }
  }

  /**
   * 重置所有用户状态
   * 用于登出或清除无效数据时使用
   */
  function resetState() {
    token.value = ''
    userInfo.value = null
    roles.value = []
    
    // 清除localStorage中的用户相关数据
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('username')
    localStorage.removeItem('remember')
  }

  /**
   * 从localStorage初始化用户信息
   * 在Store创建时自动调用，用于刷新页面后恢复状态
   */
  function initFromStorage() {
    // 恢复用户信息
    const storedUserInfo = localStorage.getItem('userInfo')
    if (storedUserInfo) {
      try {
        const parsedInfo = JSON.parse(storedUserInfo)
        userInfo.value = parsedInfo
        if (parsedInfo.roles) {
          roles.value = parsedInfo.roles
        }
      } catch (error) {
        console.warn('[UserStore] 解析存储的用户信息失败:', error)
        userInfo.value = null
      }
    }
  }

  // ===========================
  // 初始化
  // ===========================

  // Store创建时从localStorage恢复用户信息
  initFromStorage()

  // ===========================
  // 导出公共接口
  // ===========================

  return {
    // 状态
    token,
    userInfo,
    roles,
    
    // 计算属性
    isLoggedIn,
    username,
    nickname,
    avatar,
    email,
    userInitial,
    avatarColor,
    
    // 方法
    setToken,
    setUser,
    login,
    fetchUserInfo,
    logout,
    resetState
  }
})
