<template>
  <div class="login-container">
    <!-- 背景装饰元素 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <!-- 品牌区域 -->
      <div class="login-header">
        <div class="logo-wrapper">
          <el-icon :size="48" color="#667eea"><MagicStick /></el-icon>
        </div>
        <h1 class="title">AI Enterprise Platform</h1>
        <p class="subtitle">企业级智能对话平台</p>
      </div>

      <!-- 分隔线 -->
      <el-divider />

      <!-- 登录表单 -->
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <!-- 用户名输入框 -->
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <!-- 密码输入框 -->
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <!-- 记住我和忘记密码 -->
        <div class="form-options">
          <el-checkbox v-model="loginForm.remember" class="remember-checkbox">
            记住我
          </el-checkbox>
          <el-link type="primary" :underline="false" class="forgot-link">
            忘记密码？
          </el-link>
        </div>

        <!-- 登录按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            <template #loading>
              <el-icon class="is-loading"><Loading /></el-icon>
              登录中...
            </template>
            {{ loading ? '' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 底部注册链接 -->
      <div class="login-footer">
        <span>还没有账号？</span>
        <el-link type="primary" :underline="false" @click="handleRegister">
          立即注册
        </el-link>
      </div>
    </div>

    <!-- 版权信息 -->
    <div class="copyright">
      &copy; 2026 AI Enterprise Platform. All rights reserved.
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock, Loading, MagicStick } from '@element-plus/icons-vue'
import { login as loginApi } from '@/api/auth'

// 路由和状态管理
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 表单引用和加载状态
const loginFormRef = ref(null)
const loading = ref(false)

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度在 6 到 30 个字符', trigger: 'blur' }
  ]
}

/**
 * 处理登录操作
 * 包含表单验证、API调用、Token存储和路由跳转
 */
async function handleLogin() {
  if (!loginFormRef.value) return

  try {
    // 表单验证
    await loginFormRef.value.validate()
  } catch (error) {
    // 验证失败，不继续执行
    return
  }

  loading.value = true
  try {
    // 调用登录API
    const res = await loginApi({
      username: loginForm.username,
      password: loginForm.password
    })

    // 存储Token和用户信息到Pinia Store
    userStore.setToken(res.data.token)
    userStore.setUser(res.data)

    // 处理"记住我"功能 - 将用户名存储到localStorage
    if (loginForm.remember) {
      localStorage.setItem('username', loginForm.username)
      localStorage.setItem('remember', 'true')
    } else {
      localStorage.removeItem('username')
      localStorage.removeItem('remember')
    }

    ElMessage.success('登录成功！欢迎回来')

    // 跳转到之前的页面或首页
    const redirect = route.query.redirect || '/home'
    router.push(redirect)
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error(error.response?.data?.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

/**
 * 处理注册跳转（预留功能）
 */
function handleRegister() {
  ElMessage.info('注册功能即将开放，敬请期待')
}

// 页面加载时检查是否保存了用户名
onMounted(() => {
  const savedRemember = localStorage.getItem('remember')
  if (savedRemember === 'true') {
    const savedUsername = localStorage.getItem('username')
    if (savedUsername) {
      loginForm.username = savedUsername
      loginForm.remember = true
    }
  }
})
</script>

<style scoped>
/* ===========================
   登录页面样式
   =========================== */

.login-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  /* 深色渐变背景 - 增强版 */
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
}

/* 背景装饰圆圈 - 增强动画 */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  overflow: hidden;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
  animation: float 6s ease-in-out infinite;
  backdrop-filter: blur(4px);
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  right: -100px;
  animation-delay: 0s;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: -50px;
  left: -50px;
  animation-delay: 2s;
}

.circle-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  left: 10%;
  animation-delay: 4s;
}

/* 新增装饰元素 */
.circle-4 {
  width: 80px;
  height: 80px;
  top: 20%;
  right: 20%;
  animation-delay: 1s;
  opacity: 0.6;
}

.circle-5 {
  width: 120px;
  height: 120px;
  bottom: 25%;
  right: 10%;
  animation-delay: 3s;
  opacity: 0.5;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) scale(1) rotate(0deg);
  }
  33% {
    transform: translateY(-15px) scale(1.05) rotate(5deg);
  }
  66% {
    transform: translateY(8px) scale(0.97) rotate(-3deg);
  }
}

/* 登录卡片 - 增强版 */
.login-card {
  width: 440px;
  padding: 44px 40px;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 20px;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.12),
    0 2px 8px rgba(0, 0, 0, 0.08),
    0 0 0 1px rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(12px);
  z-index: 10;
  animation: slideUp 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  overflow: hidden;
}

/* 卡片顶部装饰线 */
.login-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60%;
  height: 3px;
  background: linear-gradient(90deg, transparent, var(--primary-color), transparent);
  opacity: 0.6;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(40px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 品牌区域 */
.login-header {
  text-align: center;
  margin-bottom: 24px;
}

.logo-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.logo-wrapper:hover {
  transform: translateY(-3px) rotate(5deg);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

.title {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary, #333333);
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
}

.subtitle {
  font-size: 14px;
  color: var(--text-secondary, #999999);
  margin: 0;
}

/* Element Plus 分隔线覆盖 */
:deep(.el-divider) {
  margin: 0 0 28px 0;
  border-color: #f0f0f0;
}

/* 表单样式 */
.login-form {
  width: 100%;
}

/* 输入框聚焦动画效果 */
:deep(.el-input__wrapper) {
  border-radius: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.25), 0 0 0 1px #667eea inset !important;
}

:deep(.el-input__prefix .el-icon) {
  font-size: 16px;
  color: #909399;
  transition: color 0.3s ease;
}

:deep(.el-input__wrapper.is-focus .el-input__prefix .el-icon) {
  color: #667eea;
}

/* 表单选项行（记住我 + 忘记密码） */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.remember-checkbox {
  --el-checkbox-text-color: var(--text-regular, #666666);
}

.forgot-link {
  font-size: 14px;
}

/* 登录按钮 */
.login-button {
  width: 100%;
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  /* 使用CSS变量或直接指定主色调 */
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  letter-spacing: 2px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.35);
}

.login-button:hover {
  background: linear-gradient(135deg, #5a6fd6 0%, #6a4296 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(102, 126, 234, 0.45);
}

.login-button:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.35);
}

/* 底部注册链接 */
.login-footer {
  text-align: center;
  margin-top: 20px;
  color: var(--text-secondary, #999999);
  font-size: 14px;
}

.login-footer span {
  margin-right: 6px;
}

/* 版权信息 */
.copyright {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
  text-align: center;
  z-index: 10;
}

/* ===========================
   响应式设计
   =========================== */

@media (max-width: 480px) {
  .login-card {
    width: 90%;
    padding: 32px 24px;
    margin: 16px;
  }

  .title {
    font-size: 22px;
  }

  .logo-wrapper {
    width: 64px;
    height: 64px;
  }

  .logo-wrapper :deep(.el-icon) {
    font-size: 36px !important;
  }
}

@media (max-width: 360px) {
  .login-card {
    width: 95%;
    padding: 24px 20px;
  }

  .title {
    font-size: 20px;
  }
}
</style>
