import axios from 'axios'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/LoginUser'

const request = axios.create({
  baseURL: 'http://localhost:5173/api',
  timeout: 60000,
  withCredentials: true,
})

request.interceptors.request.use(
  function (config) {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  function (error) {
    return Promise.reject(error)
  },
)

request.interceptors.response.use(
  function (response) {
    const { data } = response
    if (data.code === 40100) {
      if (
        !response.request.responseURL.includes('user/get/login') &&
        !window.location.pathname.includes('/user/login')
      ) {
        message.warning('请先登录')
        const loginUserStore = useLoginUserStore()
        loginUserStore.clearLoginUser()
        window.location.href = `/user/login?redirect=${window.location.href}`
      }
    }
    return response
  },
  function (error) {
    if (error.response?.status === 401) {
      const loginUserStore = useLoginUserStore()
      loginUserStore.clearLoginUser()
      message.warning('登录已过期，请重新登录')
      if (!window.location.pathname.includes('/user/login')) {
        window.location.href = `/user/login?redirect=${window.location.href}`
      }
    }
    return Promise.reject(error)
  },
)

export default request
