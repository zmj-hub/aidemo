<template>
  <el-container class="main-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="logo-container">
        <img src="@/assets/logo.svg" alt="Logo" class="logo" v-if="!isCollapse" />
        <h1 v-if="!isCollapse">AI Platform</h1>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        router
        background-color="#001529"
        text-color="#ffffffa6"
        active-text-color="#1890ff"
      >
        <el-menu-item index="/home">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>

        <el-menu-item index="/chat">
          <el-icon><ChatDotRound /></el-icon>
          <span>AI对话</span>
        </el-menu-item>

        <el-menu-item index="/rag">
          <el-icon><Document /></el-icon>
          <span>文档管理</span>
        </el-menu-item>

        <el-menu-item index="/model">
          <el-icon><Cpu /></el-icon>
          <span>模型管理</span>
        </el-menu-item>

        <el-menu-item index="/memory">
          <el-icon><Collection /></el-icon>
          <span>记忆管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon
            class="collapse-btn"
            @click="toggleCollapse"
          >
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.meta.title !== '首页'">
              {{ $route.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <!-- 主题切换按钮 -->
          <el-tooltip :content="isDarkMode ? '切换至明亮模式' : '切换至暗黑模式'" placement="bottom" :show-after="500">
            <el-icon class="header-icon theme-toggle" @click="toggleTheme">
              <Moon v-if="!isDarkMode" />
              <Sunny v-else />
            </el-icon>
          </el-tooltip>

          <!-- 全屏切换按钮（可选） -->
          <el-tooltip content="全屏" placement="bottom" :show-after="500">
            <el-icon class="header-icon" @click="toggleFullscreen">
              <FullScreen />
            </el-icon>
          </el-tooltip>

          <!-- 用户信息下拉菜单 -->
          <el-dropdown trigger="click" @command="handleCommand" class="user-dropdown">
            <span class="user-info">
              <!-- 用户头像 - 支持自定义头像或首字母显示 -->
              <el-avatar 
                :size="36" 
                :style="{ backgroundColor: userStore.avatarColor }"
                class="user-avatar"
              >
                <!-- 如果有自定义头像URL则显示图片，否则显示首字母 -->
                <img 
                  v-if="userStore.avatar" 
                  :src="userStore.avatar" 
                  alt="avatar"
                  @error="handleAvatarError"
                />
                <span v-else class="avatar-text">{{ userStore.userInitial }}</span>
              </el-avatar>
              
              <!-- 用户名称 -->
              <span class="username">{{ userStore.nickname }}</span>
              
              <!-- 下拉箭头 -->
              <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
            </span>
            
            <template #dropdown>
              <el-dropdown-menu class="user-dropdown-menu">
                <!-- 用户信息卡片 -->
                <div class="dropdown-user-card">
                  <el-avatar 
                    :size="48" 
                    :style="{ backgroundColor: userStore.avatarColor }"
                    class="card-avatar"
                  >
                    <img 
                      v-if="userStore.avatar" 
                      :src="userStore.avatar" 
                      alt="avatar"
                    />
                    <span v-else>{{ userStore.userInitial }}</span>
                  </el-avatar>
                  <div class="card-info">
                    <div class="card-name">{{ userStore.nickname }}</div>
                    <div class="card-email" v-if="userStore.email">
                      {{ userStore.email }}
                    </div>
                    <div class="card-role" v-if="userStore.roles?.length">
                      <el-tag size="small" type="info">{{ userStore.roles[0] }}</el-tag>
                    </div>
                  </div>
                </div>

                <el-dropdown-item divided command="profile">
                  <el-icon><UserFilled /></el-icon>
                  <span>个人信息</span>
                </el-dropdown-item>

                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  <span>账户设置</span>
                </el-dropdown-item>

                <el-dropdown-item divided command="logout" class="logout-item">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区域 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component, route }">
          <transition :name="transitionName" mode="out-in" @before-leave="onBeforeLeave" @enter="onEnter">
            <div :key="route.path" class="page-wrapper">
              <component :is="Component" />
            </div>
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  HomeFilled,
  ChatDotRound,
  Document,
  Cpu,
  Collection,
  Fold,
  Expand,
  FullScreen,
  ArrowDown,
  UserFilled,
  Setting,
  SwitchButton,
  Sunny,
  Moon
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 侧边栏折叠状态
const isCollapse = ref(false)

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// ===== 路由过渡动画系统 =====

// 记录路由深度，用于判断前进/后退
const routeDepth = ref({})

// 过渡名称
const transitionName = ref('page-transition')

// 监听路由变化，计算过渡方向
watch(() => route.path, (to) => {
  const from = routeDepth.value.current || '/home'

  // 简单的深度比较逻辑（可根据实际路由配置优化）
  const toDepth = to.split('/').length
  const fromDepth = from.split('/').length

  transitionName.value = toDepth >= fromDepth ? 'page-slide-forward' : 'page-slide-backward'

  // 更新当前路由深度
  routeDepth.value = { current: to }
}, { immediate: true })

// 页面离开前的处理
function onBeforeLeave(el) {
  el.style.position = 'absolute'
  el.style.width = '100%'
}

// 页面进入时的处理
function onEnter(el) {
  // 可以在这里添加页面入场时的额外处理
  el.style.position = 'relative'
}

// ===== 暗黑模式切换 =====

// 检查系统暗黑模式偏好
const prefersDark = window.matchMedia('(prefers-color-scheme: dark)')
const isDarkMode = ref(localStorage.getItem('theme') === 'dark' ||
  (!localStorage.getItem('theme') && prefersDark.matches))

// 初始化主题
function initTheme() {
  if (isDarkMode.value) {
    document.documentElement.setAttribute('data-theme', 'dark')
  }
}

// 切换主题
function toggleTheme() {
  isDarkMode.value = !isDarkMode.value

  if (isDarkMode.value) {
    document.documentElement.setAttribute('data-theme', 'dark')
    localStorage.setItem('theme', 'dark')
    ElMessage.success('已切换至暗黑模式')
  } else {
    document.documentElement.removeAttribute('data-theme')
    localStorage.setItem('theme', 'light')
    ElMessage.success('已切换至明亮模式')
  }
}

// 监听系统主题变化
prefersDark.addEventListener('change', (e) => {
  if (!localStorage.getItem('theme')) {
    isDarkMode.value = e.matches
    initTheme()
  }
})

// 初始化主题
initTheme()

/**
 * 切换侧边栏展开/折叠状态
 */
function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

/**
 * 切换全屏模式
 */
function toggleFullscreen() {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen().catch((err) => {
      console.warn('进入全屏失败:', err)
    })
  } else {
    document.exitFullscreen()
  }
}

/**
 * 处理用户下拉菜单命令
 * @param {string} command - 菜单命令标识
 */
function handleCommand(command) {
  switch (command) {
    case 'profile':
      // TODO: 跳转到个人中心页面
      ElMessage.info('个人信息功能开发中')
      break
    case 'settings':
      // TODO: 跳转到设置页面
      ElMessage.info('账户设置功能开发中')
      break
    case 'logout':
      handleLogout()
      break
    default:
      break
  }
}

/**
 * 处理退出登录操作
 * 显示确认对话框后执行登出流程
 */
async function handleLogout() {
  try {
    // 显示二次确认对话框
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )

    // 执行登出操作
    await userStore.logout()
    
    ElMessage.success('已成功退出登录')
    
    // 跳转到登录页
    router.push('/login')
  } catch (error) {
    // 用户取消操作或登出API调用失败
    if (error === 'cancel') {
      return
    }
    console.error('退出登录失败:', error)
    // 即使API失败也强制跳转到登录页
    router.push('/login')
  }
}

/**
 * 处理用户头像加载失败事件
 * 当自定义头像URL无法加载时触发
 */
function handleAvatarError() {
  // 可以在这里处理头像加载失败的逻辑
  // 例如：清除无效的头像URL，让组件回退到显示首字母
  console.warn('用户头像加载失败')
}
</script>

<style scoped>
/* ===========================
   主布局样式
   =========================== */

.main-layout {
  height: 100vh;
}

/* ===========================
   侧边栏样式
   =========================== */

.sidebar {
  background-color: #001529;
  transition: width 0.3s;
  overflow-x: hidden;
}

.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  border-bottom: 1px solid #ffffff1a;
}

.logo {
  height: 32px;
  margin-right: 8px;
}

.logo-container h1 {
  color: #fff;
  font-size: 18px;
  margin: 0;
  white-space: nowrap;
}

.el-menu {
  border-right: none;
}

/* ===========================
   顶部导航栏样式
   =========================== */

.header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #666;
  transition: color 0.3s ease;
}

.collapse-btn:hover {
  color: var(--primary-color, #1890ff);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 头部图标按钮 */
.header-icon {
  font-size: 18px;
  cursor: pointer;
  color: #666;
  padding: 8px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.header-icon:hover {
  color: var(--primary-color, #1890ff);
  background-color: rgba(24, 144, 255, 0.06);
}

/* ===========================
   用户下拉区域样式
   =========================== */

.user-dropdown {
  margin-left: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 10px;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background-color 0.3s ease;
}

.user-info:hover {
  background-color: rgba(0, 0, 0, 0.04);
}

/* 用户头像 */
.user-avatar {
  flex-shrink: 0;
  font-weight: 600;
  font-size: 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: transform 0.3s ease;
}

.user-info:hover .user-avatar {
  transform: scale(1.05);
}

.avatar-text {
  color: #fff;
  font-size: 14px;
  font-weight: 600;
}

/* 用户名 */
.username {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 下拉箭头 */
.dropdown-arrow {
  font-size: 12px;
  color: #999;
  transition: transform 0.3s ease;
}

.user-info:hover .dropdown-arrow {
  color: #666;
}

/* ===========================
   下拉菜单样式覆盖
   =========================== */

:deep(.user-dropdown-menu) {
  width: 280px;
  padding: 12px 0;
  border-radius: 12px !important;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12) !important;
  overflow: visible;
}

/* 用户信息卡片 */
.dropdown-user-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
  margin-bottom: 4px;
}

.card-avatar {
  flex-shrink: 0;
  font-weight: 600;
  font-size: 18px;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.18);
}

.card-info {
  flex: 1;
  min-width: 0;
}

.card-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-email {
  font-size: 12px;
  color: #999;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-role {
  display: inline-block;
}

/* 下拉菜单项样式 */
:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  font-size: 14px;
  color: #666;
  transition: all 0.2s ease;
}

:deep(.el-dropdown-menu__item .el-icon) {
  font-size: 16px;
  color: #999;
  transition: color 0.2s ease;
}

:deep(.el-dropdown-menu__item:hover) {
  background-color: rgba(102, 126, 234, 0.08);
  color: #667eea;
}

:deep(.el-dropdown-menu__item:hover .el-icon) {
  color: #667eea;
}

/* 退出登录特殊样式 */
:deep(.logout-item) {
  color: #f56c6c !important;
}

:deep(.logout-item .el-icon) {
  color: #f56c6c !important;
}

:deep(.logout-item:hover) {
  background-color: rgba(245, 108, 108, 0.08) !important;
  color: #f56c6c !important;
}

/* ===========================
   主内容区样式
   =========================== */

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

/* ===========================
   路由切换动画
   =========================== */

/* 基础页面过渡 */
.page-transition-enter-active {
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}
.page-transition-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 1, 1);
}
.page-transition-enter-from {
  opacity: 0;
  filter: blur(4px);
  transform: translateY(16px) scale(0.98);
}
.page-transition-leave-to {
  opacity: 0;
  filter: blur(2px);
  transform: translateY(-8px) scale(1.01);
}

/* 前进过渡（从右到左） */
.page-slide-forward-enter-active {
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}
.page-slide-forward-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 1, 1);
}
.page-slide-forward-enter-from {
  opacity: 0;
  transform: translateX(30px) scale(0.98);
}
.page-slide-forward-leave-to {
  opacity: 0;
  transform: translateX(-20px) scale(1.01);
}

/* 后退过渡（从左到右） */
.page-slide-backward-enter-active {
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}
.page-slide-backward-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 1, 1);
}
.page-slide-backward-enter-from {
  opacity: 0;
  transform: translateX(-30px) scale(0.98);
}
.page-slide-backward-leave-to {
  opacity: 0;
  transform: translateX(20px) scale(1.01);
}

/* 页面包装器 - 确保过渡期间布局稳定 */
.page-wrapper {
  min-height: 100%;
  position: relative;
}

/* ===== 主题切换按钮样式 ===== */

.theme-toggle {
  position: relative;
}

.theme-toggle:hover {
  color: #f6a821 !important;
  background-color: rgba(246, 168, 33, 0.08) !important;
  animation: theme-icon-rotate 0.5s ease-in-out;
}

@keyframes theme-icon-rotate {
  0% { transform: rotate(0deg) scale(1); }
  50% { transform: rotate(180deg) scale(1.1); }
  100% { transform: rotate(360deg) scale(1); }
}

/* 暗黑模式下的特殊调整 */
[data-theme="dark"] .header {
  background: var(--bg-white);
  border-bottom: 1px solid var(--border-color);
}

[data-theme="dark"] .sidebar {
  background: #000;
}

[data-theme="dark"] .main-content {
  background: var(--bg-page);
}
</style>
