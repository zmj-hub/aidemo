<template>
  <Transition name="error-fade">
    <div v-if="error" class="error-boundary">
      <!-- 错误内容卡片 -->
      <div class="error-card" :class="[errorType]">
        <!-- 图标区域 -->
        <div class="error-icon-wrapper">
          <!-- 根据错误类型显示不同图标 -->
          <div class="error-icon">
            <el-icon :size="48" :color="iconColor">
              <component :is="errorIconComponent" />
            </el-icon>
          </div>

          <!-- 装饰背景 -->
          <div class="error-bg-decoration"></div>
        </div>

        <!-- 错误信息区域 -->
        <div class="error-content">
          <h2 class="error-title">{{ errorTitle }}</h2>
          <p class="error-message">{{ errorMessage }}</p>

          <!-- 错误详情（可折叠） -->
          <el-collapse v-if="showDetails && errorDetails" class="error-details">
            <el-collapse-item title="查看错误详情" name="details">
              <pre class="error-details-text">{{ errorDetails }}</pre>
            </el-collapse-item>
          </el-collapse>
        </div>

        <!-- 操作按钮区域 -->
        <div class="error-actions">
          <el-button
            type="primary"
            size="large"
            :loading="retrying"
            @click="handleRetry"
            class="retry-button"
          >
            <template #icon>
              <el-icon><RefreshRight /></el-icon>
            </template>
            {{ retryText }}
          </el-button>

          <el-button
            size="large"
            @click="handleGoHome"
            class="home-button"
          >
            <template #icon>
              <el-icon><HomeFilled /></el-icon>
            </template>
            返回首页
          </el-button>
        </div>

        <!-- 帮助提示 -->
        <p v-if="showHelpText" class="help-text">
          如果问题持续存在，请联系技术支持或稍后重试
        </p>
      </div>
    </div>

    <!-- 正常内容插槽 -->
    <slot v-else />
  </Transition>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  Warning,
  CircleClose,
  Lock,
  Connection,
  RefreshRight,
  HomeFilled
} from '@element-plus/icons-vue'

/**
 * ErrorBoundary - 错误边界组件
 * 用于捕获和展示错误信息，提供重试和导航功能
 *
 * @example
 * <ErrorBoundary
 *   :error="error"
 *   error-type="network"
 *   title="网络连接失败"
 *   message="请检查网络设置后重试"
 *   @retry="handleRetry"
 * />
 */

const props = defineProps({
  /** 错误对象 */
  error: {
    type: [Error, String, Object],
    default: null
  },
  /** 错误标题 */
  errorTitle: {
    type: String,
    default: '出错了'
  },
  /** 错误描述 */
  errorMessage: {
    type: String,
    default: '发生了未知错误，请稍后重试'
  },
  /** 错误类型: network | server | permission | notfound | validation | unknown */
  errorType: {
    type: String,
    default: 'unknown',
    validator: (value) => ['network', 'server', 'permission', 'notfound', 'validation', 'unknown'].includes(value)
  },
  /** 是否显示详情 */
  showDetails: {
    type: Boolean,
    default: false
  },
  /** 错误详情文本 */
  errorDetails: {
    type: String,
    default: ''
  },
  /** 重试按钮文本 */
  retryText: {
    type: String,
    default: '重新尝试'
  },
  /** 是否显示帮助文本 */
  showHelpText: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['retry', 'go-home'])

const router = useRouter()
const retrying = ref(false)

// 错误图标映射
const errorIconMap = {
  network: Connection,
  server: Warning,
  permission: Lock,
  notfound: CircleClose,
  validation: Warning,
  unknown: CircleClose
}

// 错误颜色映射
const errorColorMap = {
  network: '#E6A23C',
  server: '#F56C6C',
  permission: '#909399',
  notfound: '#909399',
  validation: '#E6A23C',
  unknown: '#F56C6C'
}

// 计算属性
const errorIconComponent = computed(() => errorIconMap[props.errorType] || CircleClose)
const iconColor = computed(() => errorColorMap[props.errorType] || '#F56C6C')

// 处理重试
async function handleRetry() {
  retrying.value = true

  try {
    await emit('retry')
  } finally {
    // 延迟重置状态，让用户看到加载效果
    setTimeout(() => {
      retrying.value = false
    }, 500)
  }
}

// 返回首页
function handleGoHome() {
  emit('go-home')
  router.push('/home')
}
</script>

<script>
export default {
  name: 'ErrorBoundary'
}
</script>

<style scoped>
/* ===== 容器样式 ===== */
.error-boundary {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  padding: var(--spacing-8);
}

/* ===== 错误卡片 ===== */
.error-card {
  max-width: 520px;
  width: 100%;
  background: var(--bg-white);
  border-radius: var(--radius-xl);
  padding: var(--spacing-10);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-lighter);
  text-align: center;
  position: relative;
  overflow: hidden;
}

/* 错误类型边框装饰 */
.error-card.network {
  border-top: 4px solid #E6A23C;
}

.error-card.server {
  border-top: 4px solid #F56C6C;
}

.error-card.permission {
  border-top: 4px solid #909399;
}

.error-card.notfound {
  border-top: 4px solid #909399;
}

.error-card.validation {
  border-top: 4px solid #E6A23C;
}

.error-card.unknown {
  border-top: 4px solid #F56C6C;
}

/* ===== 图标区域 ===== */
.error-icon-wrapper {
  position: relative;
  margin-bottom: var(--spacing-8);
}

.error-icon {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  background: var(--bg-color);
  position: relative;
  z-index: 2;
  animation: error-bounce-in 0.6s ease-out;
}

@keyframes error-bounce-in {
  0% {
    opacity: 0;
    transform: scale(0.3);
  }
  50% {
    opacity: 1;
    transform: scale(1.1);
  }
  70% {
    transform: scale(0.9);
  }
  100% {
    transform: scale(1);
  }
}

/* 装饰背景圆环 */
.error-bg-decoration {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 140px;
  height: 140px;
  border-radius: 50%;
  background: radial-gradient(circle, currentColor 0%, transparent 70%);
  opacity: 0.08;
  animation: error-pulse 3s ease-in-out infinite;
}

@keyframes error-pulse {
  0%, 100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.08;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.15);
    opacity: 0.12;
  }
}

/* ===== 内容区域 ===== */
.error-content {
  margin-bottom: var(--spacing-8);
}

.error-title {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-3);
  letter-spacing: -0.5px;
}

.error-message {
  font-size: var(--font-size-base);
  color: var(--text-secondary);
  line-height: var(--line-height-relaxed);
  max-width: 400px;
  margin: 0 auto;
}

/* ===== 错误详情 ===== */
.error-details {
  margin-top: var(--spacing-5);
  text-align: left;
}

.error-details :deep(.el-collapse-item__header) {
  font-size: var(--font-size-sm);
  color: var(--info-color);
}

.error-details-text {
  background: #f8f9fa;
  border-radius: var(--radius-md);
  padding: var(--spacing-4);
  font-family: var(--font-mono);
  font-size: var(--font-size-xs);
  color: #666;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 200px;
  overflow-y: auto;
  line-height: 1.6;
}

/* ===== 操作按钮 ===== */
.error-actions {
  display: flex;
  justify-content: center;
  gap: var(--spacing-4);
  margin-bottom: var(--spacing-6);
}

.retry-button {
  min-width: 140px;
  font-weight: 500;
  transition: all var(--transition-normal);
}

.retry-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.35);
}

.home-button {
  min-width: 120px;
}

/* ===== 帮助文本 ===== */
.help-text {
  font-size: var(--font-size-sm);
  color: var(--text-placeholder);
}

/* ===== 过渡动画 ===== */
.error-fade-enter-active {
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}
.error-fade-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 1, 1);
}
.error-fade-enter-from {
  opacity: 0;
  transform: scale(0.95);
}
.error-fade-leave-to {
  opacity: 0;
  transform: scale(1.02);
}

/* ===== 响应式适配 ===== */
@media (max-width: 768px) {
  .error-boundary {
    min-height: 300px;
    padding: var(--spacing-4);
  }

  .error-card {
    padding: var(--spacing-6);
  }

  .error-icon {
    width: 72px;
    height: 72px;
  }

  .error-icon :deep(.el-icon) {
    font-size: 36px !important;
  }

  .error-bg-decoration {
    width: 110px;
    height: 110px;
  }

  .error-title {
    font-size: var(--font-size-xl);
  }

  .error-message {
    font-size: var(--font-size-sm);
  }

  .error-actions {
    flex-direction: column;
  }

  .error-actions .el-button {
    width: 100%;
  }
}
</style>
