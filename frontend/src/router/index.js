import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/home',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/HomeView.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('@/views/chat/ChatView.vue'),
        meta: { title: 'AI对话', icon: 'ChatDotRound' }
      },
      {
        path: 'rag',
        name: 'RAG',
        component: () => import('@/views/rag/RagView.vue'),
        meta: { title: '文档管理', icon: 'Document' }
      },
      {
        path: 'model',
        name: 'Model',
        component: () => import('@/views/model/ModelView.vue'),
        meta: { title: '模型管理', icon: 'Cpu' }
      },
      {
        path: 'memory',
        name: 'Memory',
        component: () => import('@/views/memory/MemoryView.vue'),
        meta: { title: '记忆管理', icon: 'Collection' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/home/HomeView.vue'),
    meta: { title: '404' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 登录验证
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title
    ? `${to.meta.title} - AI Enterprise Platform`
    : 'AI Enterprise Platform'

  const userStore = useUserStore()
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth !== false)

  if (requiresAuth && !userStore.isLoggedIn) {
    // 需要登录但未登录，跳转到登录页
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.name === 'Login' && userStore.isLoggedIn) {
    // 已登录用户访问登录页，跳转到首页
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router
