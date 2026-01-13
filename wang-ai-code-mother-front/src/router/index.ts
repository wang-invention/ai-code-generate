import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import type { MenuProps } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/LoginUser'

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
    path: '/user/login',
    name: '用户登录',
    component: () => import('../views/user/UserLoginPage.vue')
  },
  {
    path: '/user/register',
    name: '用户注册',
    component: () => import('../views/user/UserRegisterPage.vue')
  },
  {
    path: '/user/profile',
    name: '个人中心',
    component: () => import('../views/user/UserProfilePage.vue')
  },
  {
    path: '/user/profile/edit',
    name: '编辑资料',
    component: () => import('../views/user/UserEditProfilePage.vue')
  },
  {
    path: '/admin/userManage',
    name: '用户管理',
    component: () => import('../views/admin/UserManagePage.vue')
  },
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
  const loginUserStore = useLoginUserStore()

  // 如果用户已登录但访问登录/注册页面，跳转到首页
  if ((to.path === '/user/login' || to.path === '/user/register') && loginUserStore.isLogin) {
    next('/')
    return
  }

  next()
})

export default router
