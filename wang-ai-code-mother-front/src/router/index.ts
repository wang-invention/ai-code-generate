import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import type { MenuProps } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/LoginUser'

const menuItems: MenuProps['items'] = [
  {
    key: 'home',
    label: '首页',
    path: '/'
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
  },
  {
    key: 'user-manage',
    label: '用户管理',
    path: '/admin/userManage'
  },
  {
    key: 'app-manage',
    label: '应用管理',
    path: '/admin/appManage'
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
    path: '/admin/appManage',
    name: '应用管理',
    component: () => import('../views/admin/AppManagePage.vue')
  },
  {
    path: '/app/chat/:id',
    name: '应用对话',
    component: () => import('../views/app/AppChatPage.vue')
  },
  {
    path: '/app/edit/:id',
    name: '编辑应用',
    component: () => import('../views/app/AppEditPage.vue')
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

  if ((to.path === '/user/login' || to.path === '/user/register') && loginUserStore.isLogin) {
    next('/')
    return
  }

  const requireAuthPages = ['/user/profile', '/user/profile/edit', '/admin/userManage', '/admin/appManage']

  if (requireAuthPages.includes(to.path) && !loginUserStore.isLogin) {
    next(`/user/login?redirect=${encodeURIComponent(to.fullPath)}`)
    return
  }

  if (to.path === '/admin/appManage' && loginUserStore.loginUser.userRole !== 'admin') {
    message.error('您没有权限访问此页面')
    next('/')
    return
  }

  next()
})

export default router
export { menuItems }
