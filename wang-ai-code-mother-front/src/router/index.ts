import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import type { MenuProps } from 'ant-design-vue'

// 菜单项配置
export const menuItems: MenuProps['items'] = [
  {
    key: 'home',
    label: '首页',
    path: '/'
  },
  {
    key: 'courses',
    label: '课程',
    path: '/courses'
  },
  {
    key: 'articles',
    label: '文章',
    path: '/articles'
  },
  {
    key: 'community',
    label: '社区',
    path: '/community'
  }
]

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/HomeView.vue')
  },
  {
    path: '/courses',
    name: 'Courses',
    component: () => import('../views/CoursesView.vue')
  },
  {
    path: '/articles',
    name: 'Articles',
    component: () => import('../views/ArticlesView.vue')
  },
  {
    path: '/community',
    name: 'Community',
    component: () => import('../views/CommunityView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach((to, from, next) => {
  next()
})

export default router
