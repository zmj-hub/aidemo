<template>
  <div class="home-view">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <h1>欢迎使用 AI Enterprise Platform</h1>
        <p>企业级人工智能平台，集成对话、文档管理、模型配置和记忆系统</p>
      </div>
    </div>

    <!-- 功能卡片 -->
    <el-row :gutter="24" class="feature-cards">
      <el-col :xs="24" :sm="12" :md="6" v-for="(feature, index) in features" :key="feature.title">
        <el-card shadow="hover" class="feature-card card-animate" :style="{ animationDelay: `${index * 100}ms` }" @click="navigateTo(feature.path)">
          <div class="card-icon" :style="{ backgroundColor: feature.color }">
            <el-icon :size="32" color="#fff">
              <component :is="feature.icon" />
            </el-icon>
          </div>
          <h3>{{ feature.title }}</h3>
          <p>{{ feature.description }}</p>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快速统计 -->
    <el-row :gutter="24" class="quick-stats">
      <el-col :xs="24" :sm="8">
        <el-card class="stat-card stat-card-animate" style="--delay: 0ms;">
          <div class="stat-header">
            <span>今日对话次数</span>
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <div class="stat-value count-up" :data-target="quickStats.chatsToday">{{ quickStats.chatsToday }}</div>
          <div class="stat-trend up">
            <el-icon><Top /></el-icon>
            较昨日 +12%
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card class="stat-card stat-card-animate" style="--delay: 150ms;">
          <div class="stat-header">
            <span>活跃会话数</span>
            <el-icon><Connection /></el-icon>
          </div>
          <div class="stat-value count-up" :data-target="quickStats.activeSessions">{{ quickStats.activeSessions }}</div>
          <div class="stat-trend up">
            <el-icon><Top /></el-icon>
            较昨日 +5%
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card class="stat-card stat-card-animate" style="--delay: 300ms;">
          <div class="stat-header">
            <span>知识库文档</span>
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-value count-up" :data-target="quickStats.documents">{{ quickStats.documents }}</div>
          <div class="stat-trend">
            <el-icon><Minus /></el-icon>
            无变化
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近活动 -->
    <el-card class="activity-card">
      <template #header>
        <div class="activity-header">
          <span>最近活动</span>
          <el-button link type="primary">查看全部</el-button>
        </div>
      </template>

      <el-timeline>
        <el-timeline-item
          v-for="activity in recentActivities"
          :key="activity.id"
          :timestamp="formatDate(activity.time)"
          :type="activity.type"
          placement="top"
        >
          <div class="activity-content">
            <strong>{{ activity.title }}</strong>
            <p>{{ activity.description }}</p>
          </div>
        </el-timeline-item>
      </el-timeline>
    </el-card>

    <!-- 快速开始指南 -->
    <el-card class="guide-card">
      <template #header>
        <span>快速开始</span>
      </template>

      <el-steps :active="1" finish-status="success" simple style="margin-top: 20px;">
        <el-step title="配置模型" description="添加 AI 模型 API" />
        <el-step title="上传文档" description="构建知识库" />
        <el-step title="开始对话" description="与 AI 交互" />
        <el-step title="查看记忆" description="管理系统记忆" />
      </el-steps>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { formatDate } from '@/utils/index'

const router = useRouter()

// 功能特性
const features = ref([
  {
    title: 'AI 对话',
    description: '与智能助手进行自然语言交互，支持流式输出和多轮对话',
    icon: 'ChatDotRound',
    path: '/chat',
    color: '#1890ff'
  },
  {
    title: '文档管理',
    description: '上传和管理 RAG 知识库文档，增强 AI 回答准确性',
    icon: 'Document',
    path: '/rag',
    color: '#52c41a'
  },
  {
    title: '模型管理',
    description: '配置和管理多种 AI 模型，灵活切换不同服务提供商',
    icon: 'Cpu',
    path: '/model',
    color: '#faad14'
  },
  {
    title: '记忆管理',
    description: '查看和管理 AI 长期记忆数据，保持对话连贯性',
    icon: 'Collection',
    path: '/memory',
    color: '#722ed1'
  }
])

// 快速统计数据
const quickStats = ref({
  chatsToday: 156,
  activeSessions: 23,
  documents: 48
})

// 最近活动
const recentActivities = ref([
  {
    id: 1,
    title: '新建会话',
    description: '用户创建了新的对话会话 "产品咨询"',
    time: new Date(Date.now() - 300000).toISOString(),
    type: 'primary'
  },
  {
    id: 2,
    title: '上传文档',
    description: '上传了 "技术规格说明.pdf" 到产品知识库',
    time: new Date(Date.now() - 3600000).toISOString(),
    type: 'success'
  },
  {
    id: 3,
    title: '配置模型',
    description: '新增 GPT-4 Turbo 模型配置并完成连接测试',
    time: new Date(Date.now() - 7200000).toISOString(),
    type: 'warning'
  },
  {
    id: 4,
    title: '导出记忆',
    description: '导出了本月的记忆数据报告',
    time: new Date(Date.now() - 86400000).toISOString(),
    type: 'info'
  }
])

// 导航到指定页面
function navigateTo(path) {
  router.push(path)
}

// ===== 数字递增动画 =====
function animateCountUp(element, target, duration = 1500) {
  const start = 0
  const startTime = performance.now()

  function update(currentTime) {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)

    // 使用 easeOutExpo 缓动函数
    const easeProgress = progress === 1 ? 1 : 1 - Math.pow(2, -10 * progress)
    const current = Math.floor(start + (target - start) * easeProgress)

    element.textContent = current.toLocaleString()

    if (progress < 1) {
      requestAnimationFrame(update)
    }
  }

  requestAnimationFrame(update)
}

// 初始化数字动画
function initCountUpAnimations() {
  nextTick(() => {
    const countElements = document.querySelectorAll('.count-up')
    countElements.forEach((el) => {
      const target = parseInt(el.dataset.target) || 0

      // 使用 IntersectionObserver 实现滚动触发动画
      const observer = new IntersectionObserver(
        (entries) => {
          entries.forEach((entry) => {
            if (entry.isIntersecting) {
              animateCountUp(el, target)
              observer.unobserve(el)
            }
          })
        },
        { threshold: 0.5 }
      )

      observer.observe(el)
    })
  })
}

onMounted(() => {
  initCountUpAnimations()
})
</script>

<style scoped>
.home-view {
  padding: 24px;
}

/* ===== 欢迎区域 - 增强版 ===== */
.welcome-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  border-radius: 16px;
  padding: 44px 40px;
  margin-bottom: 28px;
  color: #fff;
  position: relative;
  overflow: hidden;
  animation: welcome-glow 3s ease-in-out infinite alternate;
}

/* 欢迎区域装饰 */
.welcome-section::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
  animation: welcome-float 8s ease-in-out infinite;
}

@keyframes welcome-glow {
  from { box-shadow: 0 4px 24px rgba(102, 126, 234, 0.25); }
  to { box-shadow: 0 6px 32px rgba(118, 75, 162, 0.35); }
}

@keyframes welcome-float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-30px, 20px) scale(1.1); }
}

.welcome-content h1 {
  font-size: 30px;
  margin-bottom: 12px;
  font-weight: 700;
  letter-spacing: -0.5px;
  position: relative;
  z-index: 1;
}

.welcome-content p {
  font-size: 16px;
  opacity: 0.92;
  position: relative;
  z-index: 1;
}

/* ===== 功能卡片 - 增强版 ===== */
.feature-cards {
  margin-bottom: 28px;
}

.feature-card {
  cursor: pointer;
  transition: all var(--transition-slow) cubic-bezier(0.34, 1.56, 0.64, 1);
  text-align: center;
  padding: 28px 20px;
  border-radius: 16px !important;
  position: relative;
  overflow: hidden;
}

.feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, transparent, currentColor, transparent);
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.feature-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.15);
}

.feature-card:hover::before {
  opacity: 0.5;
}

.card-icon {
  width: 72px;
  height: 72px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 18px;
  transition: all var(--transition-normal);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.feature-card:hover .card-icon {
  transform: scale(1.1) rotate(-5deg);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.feature-card h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 10px;
  font-weight: 600;
  transition: color var(--transition-fast);
}

.feature-card:hover h3 {
  color: var(--primary-color);
}

.feature-card p {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  transition: color var(--transition-fast);
}

.feature-card:hover p {
  color: #555;
}

/* 卡片入场动画 */
.card-animate {
  opacity: 0;
  animation: card-enter 0.6s ease-out forwards;
  animation-delay: var(--delay, 0ms);
}

@keyframes card-enter {
  from {
    opacity: 0;
    transform: translateY(24px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* ===== 统计卡片 - 增强版 ===== */
.quick-stats {
  margin-bottom: 28px;
}

.stat-card {
  padding: 24px;
  border-radius: 14px !important;
  position: relative;
  overflow: hidden;
  transition: all var(--transition-slow) !important;
  border: 1px solid transparent !important;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.03), transparent);
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1) !important;
  border-color: var(--border-light) !important;
}

.stat-card:hover::before {
  opacity: 1;
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #666;
  font-size: 14px;
  margin-bottom: 14px;
  font-weight: 500;
}

.stat-value {
  font-size: 38px;
  font-weight: 800;
  color: #333;
  margin-bottom: 8px;
  letter-spacing: -1px;
  line-height: 1.2;
  /* 数字字体优化 */
  font-variant-numeric: tabular-nums;
}

/* 数字动画类 */
.count-up {
  display: inline-block;
  min-width: 60px;
}

.stat-trend {
  font-size: 13px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-trend.up {
  color: #52c41a;
  font-weight: 500;
}

/* 统计卡片入场动画 */
.stat-card-animate {
  opacity: 0;
  animation: stat-enter 0.5s ease-out forwards;
  animation-delay: var(--delay, 0ms);
}

@keyframes stat-enter {
  from {
    opacity: 0;
    transform: translateY(16px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.activity-card {
  margin-bottom: 24px;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-content strong {
  color: #333;
}

.activity-content p {
  color: #666;
  margin: 4px 0 0;
  font-size: 14px;
}

.guide-card {
  margin-bottom: 24px;
}
</style>
