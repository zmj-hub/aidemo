<template>
  <div class="empty-state" :class="[size, { 'has-action': !!actionText }]">
    <!-- 图标区域 -->
    <div class="empty-icon-wrapper" :style="{ '--icon-color': iconColor }">
      <!-- 自定义图标插槽 -->
      <slot name="icon">
        <el-icon :size="iconSize" :color="iconColor || '#c0c4cc'">
          <component :is="iconComponent" />
        </el-icon>
      </slot>

      <!-- 装饰性背景圆 -->
      <div class="icon-bg-circle"></div>
      <div class="icon-bg-pulse"></div>
    </div>

    <!-- 文字区域 -->
    <div class="empty-content">
      <!-- 标题 -->
      <h3 class="empty-title">{{ title }}</h3>

      <!-- 描述文字（支持插槽） -->
      <p v-if="description || $slots.description" class="empty-description">
        <slot name="description">{{ description }}</slot>
      </p>

      <!-- 额外内容插槽 -->
      <slot name="extra" />

      <!-- 操作按钮 -->
      <el-button
        v-if="actionText"
        type="primary"
        size="large"
        class="action-button"
        @click="$emit('action')"
        :loading="actionLoading"
      >
        {{ actionText }}
      </el-button>
    </div>

    <!-- 底部装饰（可选） -->
    <div v-if="$slots.footer" class="empty-footer">
      <slot name="footer" />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import {
  ChatDotRound,
  Document,
  Monitor,
  Notebook,
  FolderOpened,
  Warning,
  InfoFilled,
  Search,
  Connection,
  DataAnalysis
} from '@element-plus/icons-vue'

/**
 * EmptyState - 空状态组件
 * 用于展示数据为空时的友好提示界面
 *
 * @example
 * <EmptyState
 *   icon="chat"
 *   title="开始对话"
 *   description="创建您的第一个会话"
 *   action-text="新建会话"
 *   @action="handleCreate"
 * />
 */

const props = defineProps({
  /** 图标类型: chat | document | model | memory | folder | warning | info | search | connection | analysis */
  icon: {
    type: String,
    default: 'info'
  },
  /** 自定义图标颜色 */
  iconColor: {
    type: String,
    default: ''
  },
  /** 标题文本 */
  title: {
    type: String,
    required: true
  },
  /** 描述文本 */
  description: {
    type: String,
    default: ''
  },
  /** 操作按钮文本 */
  actionText: {
    type: String,
    default: ''
  },
  /** 操作按钮加载状态 */
  actionLoading: {
    type: Boolean,
    default: false
  },
  /** 尺寸: small | medium | large */
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  }
})

defineEmits(['action'])

// 图标组件映射
const iconMap = {
  chat: ChatDotRound,
  document: Document,
  model: Monitor,
  memory: Notebook,
  folder: FolderOpened,
  warning: Warning,
  info: InfoFilled,
  search: Search,
  connection: Connection,
  analysis: DataAnalysis
}

// 计算图标组件
const iconComponent = computed(() => iconMap[props.icon] || InfoFilled)

// 根据尺寸计算图标大小
const iconSize = computed(() => {
  const sizes = { small: 48, medium: 64, large: 80 }
  return sizes[props.size] || 64
})
</script>

<script>
export default {
  name: 'EmptyState'
}
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-10) var(--spacing-6);
  text-align: center;
  min-height: 300px;
}

/* ===== 尺寸变体 ===== */
.empty-state.size-small {
  padding: var(--spacing-6) var(--spacing-4);
  min-height: 200px;
}

.empty-state.size-large {
  padding: var(--spacing-12) var(--spacing-8);
  min-height: 400px;
}

/* ===== 图标区域 ===== */
.empty-icon-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
  margin-bottom: var(--spacing-6);
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-state.size-small .empty-icon-wrapper {
  width: 80px;
  height: 80px;
  margin-bottom: var(--spacing-4);
}

.empty-state.size-large .empty-icon-wrapper {
  width: 160px;
  height: 160px;
  margin-bottom: var(--spacing-8);
}

/* 背景装饰圆 */
.icon-bg-circle {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-light), transparent);
  opacity: 0.5;
  animation: float 4s ease-in-out infinite;
}

.icon-bg-pulse {
  position: absolute;
  top: -10%;
  left: -10%;
  right: -10%;
  bottom: -10%;
  border-radius: 50%;
  border: 2px solid var(--primary-color);
  opacity: 0;
  animation: pulse-ring 2s ease-out infinite;
}

@keyframes pulse-ring {
  0% {
    transform: scale(0.8);
    opacity: 0.6;
  }
  100% {
    transform: scale(1.2);
    opacity: 0;
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) scale(1);
  }
  50% {
    transform: translateY(-8px) scale(1.02);
  }
}

/* 确保图标在最上层 */
.empty-icon-wrapper :deep(.el-icon) {
  position: relative;
  z-index: 2;
}

/* ===== 内容区域 ===== */
.empty-content {
  max-width: 400px;
}

.empty-state.size-small .empty-content {
  max-width: 280px;
}

.empty-title {
  font-size: var(--font-size-xl);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-2);
  letter-spacing: -0.3px;
}

.empty-state.size-small .empty-title {
  font-size: var(--font-size-lg);
}

.empty-state.size-large .empty-title {
  font-size: var(--font-size-2xl);
}

.empty-description {
  font-size: var(--font-size-base);
  color: var(--text-secondary);
  line-height: var(--line-height-relaxed);
  margin-bottom: var(--spacing-5);
}

.empty-state.size-small .empty-description {
  font-size: var(--font-size-sm);
  margin-bottom: var(--spacing-3);
}

/* ===== 操作按钮 ===== */
.action-button {
  min-width: 140px;
  font-weight: 500;
  letter-spacing: 0.5px;
  transition: all var(--transition-normal);
}

.action-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.35);
}

.action-button:active {
  transform: translateY(0);
}

/* ===== 底部区域 ===== */
.empty-footer {
  margin-top: var(--spacing-8);
  padding-top: var(--spacing-6);
  border-top: 1px dashed var(--border-lighter);
  width: 100%;
  max-width: 400px;
}

/* ===== 动画入场效果 ===== */
.empty-state {
  animation: empty-enter 0.5s ease-out;
}

@keyframes empty-enter {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ===== 响应式适配 ===== */
@media (max-width: 768px) {
  .empty-state {
    padding: var(--spacing-8) var(--spacing-4);
    min-height: 250px;
  }

  .empty-icon-wrapper {
    width: 96px;
    height: 96px;
  }

  .empty-content {
    max-width: 100%;
  }

  .empty-title {
    font-size: var(--font-size-lg);
  }

  .action-button {
    width: 100%;
  }

  .empty-footer {
    max-width: 100%;
  }
}
</style>
