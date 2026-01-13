import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { getCurrentUser } from '@/api/userController'

export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<API.LoginUserVO>({
    userName: '未登录',
  })

  const isLogin = computed(() => {
    return !!loginUser.value.id
  })

  function setLoginUser(user: API.LoginUserVO) {
    loginUser.value = user
    localStorage.setItem('loginUser', JSON.stringify(user))
  }

  function loadLoginUserFromStorage() {
    const savedUser = localStorage.getItem('loginUser')
    if (savedUser) {
      try {
        loginUser.value = JSON.parse(savedUser)
      } catch (e) {
        console.error('Failed to parse saved user:', e)
        localStorage.removeItem('loginUser')
      }
    }
  }

  async function fetchLoginUser() {
    try {
      const res = await getCurrentUser()
      if (res.data.code === 0 && res.data.data) {
        setLoginUser(res.data.data)
      }
    } catch (error) {
      console.error('Failed to fetch login user:', error)
    }
  }

  function clearLoginUser() {
    loginUser.value = {
      userName: '未登录',
    }
    localStorage.removeItem('loginUser')
    localStorage.removeItem('token')
  }

  return {
    loginUser,
    isLogin,
    fetchLoginUser,
    setLoginUser,
    loadLoginUserFromStorage,
    clearLoginUser,
  }
})
