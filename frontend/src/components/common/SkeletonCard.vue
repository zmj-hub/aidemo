<template>
  <div class="skeleton-card" :class="[variant, { 'is-loading': loading }]">
    <!-- 头部骨架 -->
    <div v-if="showHeader" class="skeleton-header">
      <el-skeleton :rows="0" animated>
        <template #template>
          <div class="skeleton-header-content">
            <el-skeleton-item variant="circle" style="width: 40px; height: 40px;" />
            <div class="skeleton-header-text">
              <el-skeleton-item variant="h3" style="width: 40%; margin-bottom: 8px;" />
              <el-skeleton-item variant="text" style="width: 60%;" />
            </div>
          </div>
        </template>
      </el-skeleton>
    </div>

    <!-- 内容骨架 -->
    <div class="skeleton-body">
      <el-skeleton :rows="rows" animated>
        <template #template>
          <div class="skeleton-content">
            <!-- 标题行 -->
            <el-skeleton-item
              variant="h3"
              :style="{ width: titleWidth, marginBottom: '16px' }"
            />
            <!-- 内容行 -->
            <el-skeleton-item
              v-for="(width, index) in contentRows"
              :key="index"
              variant="text"
              :style="{ width, marginBottom: index < contentRows.length - 1 ? '12px' : '0' }"
            />
          </div>
        </template>
      </el-skeleton>
    </div>

    <!-- 底部操作区骨架 -->
    <div v-if="showFooter" class="skeleton-footer">
      <el-skeleton :rows="0" animated>
        <template #template>
          <div class="skeleton-footer-content">
            <el-skeleton-item variant="text" style="width: 80px;" />
            <el-skeleton-item variant="button" style="width: 88px; height: 32px; border-radius: 6px;" />
          </div>
        </template>
      </el-skeleton>
    </div>

    <!-- 脉冲动画覆盖层 -->
    <div v-if="loading" class="skeleton-overlay">
      <div class="pulse-ring"></div>
    </div>
  </div>
</template>

<script setup>
/**
 * SkeletonCard - 骨架屏卡片组件
 * 用于数据加载时显示占位内容
 *
 * @example
 * <SkeletonCard :loading="true" variant="card" :rows="5" show-header show-footer />
 */
defineProps({
  /** 是否显示加载状态 */
  loading: {
    type: Boolean,
    default: true
  },
  /** 卡片变体类型: card(卡片) | list(列表) | table(表格) | statistic(统计) */
  variant: {
    type: String,
    default: 'card',
    validator: (value) => ['card', 'list', 'table', 'statistic'].includes(value)
  },
  /** 骨架行数 */
  rows: {
    type: Number,
    default: 5
  },
  /** 是否显示头部骨架 */
  showHeader: {
    type: Boolean,
    default: false
  },
  /** 是否显示底部操作区骨架 */
  showFooter: {
    type: Boolean,
    default: false
  },
  /** 标题宽度百分比 */
  titleWidth: {
    type: String,
    default: '60%'
  }
})

// 根据行数生成不同宽度的内容行
const contentRows = computed(() => {
  const widths = []
  for (let i = 0; i < props.rows; i++) {
    // 模拟真实文本长度分布：首尾短，中间长
    if (i === 0 || i === props.rows - 1) {
      widths.push('100%')
    } else if (i === Math.floor(props.rows / 2)) {
      widths.push('80%')
    } else {
      widths.push(`${85 + Math.random() * 15}%`)
    }
  }
  return widths
})
</script>

<script>
import { computed } from 'vue'
export default {
  name: 'SkeletonCard'
}
</script>

<style scoped>
.skeleton-card {
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: var(--spacing-6);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-lighter);
  position: relative;
  overflow: hidden;
  min-height: 200px;
}

.skeleton-card.is-loading {
  pointer-events: none;
}

/* ===== 变体样式 ===== */

/* 卡片变体 */
.skeleton-card.variant-card {
  /* 默认样式 */
}

/* 列表变体 */
.skeleton-card.variant-list {
  padding: var(--spacing-4);
}

.skeleton-card.variant-list .skeleton-body {
  display: flex;
  align-items: center;
  gap: var(--spacing-4);
}

.skeleton-card.variant-list .skeleton-body .skeleton-content {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
  width: 100%;
}

/* 表格变体 */
.skeleton-card.variant-table {
  padding: 0;
}

.skeleton-card.variant-table .skeleton-body {
  padding: var(--spacing-4);
}

.skeleton-card.variant-table .skeleton-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: var(--spacing-4);
}

.skeleton-card.variant-table .skeleton-content > :deep(.el-skeleton__item) {
  margin-bottom: 0 !important;
}

/* 统计变体 */
.skeleton-card.variant-statistic {
  background: linear-gradient(135deg, var(--primary-light), #fff);
  text-align: center;
}

.skeleton-card.variant-statistic .skeleton-body .skeleton-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-3);
}

.skeleton-card.variant-statistic .skeleton-body .skeleton-content > :deep(.el-skeleton__item:first-child) {
  width: 120px !important;
  height: 48px !important;
  border-radius: var(--radius-lg) !important;
}

.skeleton-card.variant-statistic .skeleton-body .skeleton-content > :deep(.el-skeleton__item:nth-child(2)) {
  width: 80px !important;
  height: 32px !important;
  margin-bottom: 8px !important;
}

/* ===== 头部区域 ===== */
.skeleton-header {
  margin-bottom: var(--spacing-5);
  padding-bottom: var(--spacing-4);
  border-bottom: 1px solid var(--border-extra-light);
}

.skeleton-header-content {
  display: flex;
  align-items: center;
  gap: var(--spacing-4);
}

.skeleton-header-text {
  flex: 1;
}

/* ===== 底部区域 ===== */
.skeleton-footer {
  margin-top: var(--spacing-5);
  padding-top: var(--spacing-4);
  border-top: 1px solid var(--border-extra-light);
}

.skeleton-footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* ===== 脉冲覆盖层 ===== */
.skeleton-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  overflow: hidden;
}

.pulse-ring {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(255, 255, 255, 0.03) 20%,
    rgba(255, 255, 255, 0.08) 50%,
    rgba(255, 255, 255, 0.03) 80%,
    transparent 100%
  );
  animation: skeleton-pulse 2s ease-in-out infinite;
}

@keyframes skeleton-pulse {
  0%, 100% {
    transform: translateX(-30%) rotate(0deg);
  }
  50% {
    transform: translateX(10%) rotate(2deg);
  }
}

/* ===== Element Plus 骨架屏覆盖 ===== */
.skeleton-card :deep(.el-skeleton) {
  width: 100%;
}

.skeleton-card :deep(.el-skeleton__item) {
  border-radius: var(--radius-sm) !important;
  background: linear-gradient(
    90deg,
    var(--bg-color) 25%,
    #f0f2f5 37%,
    var(--bg-color) 63%
  );
  background-size: 400% 100%;
  animation: skeleton-shimmer 1.5s ease-in-out infinite;
}

@keyframes skeleton-shimmer {
  0% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

/* ===== 响应式适配 ===== */
@media (max-width: 768px) {
  .skeleton-card {
    padding: var(--spacing-4);
    min-height: 150px;
  }

  .skeleton-card.variant-list .skeleton-body .skeleton-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .skeleton-card.variant-table .skeleton-content {
    grid-template-columns: 1fr;
  }
}
</style>
