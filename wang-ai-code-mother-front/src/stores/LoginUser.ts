import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { getCurrentUser } from '@/api/userController'

export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<API.LoginUserVO>({
    userName: '未登录',
  })

  async function fetchLoginUser() {
    const res = await getCurrentUser()
    if (res.data.code === 0 && res.data.data) {
      setLoginUser(res.data.data)
    }
  }

  function setLoginUser(user: API.LoginUserVO) {
    loginUser.value = user
  }
  return {
    loginUser,
    fetchLoginUser,
    setLoginUser,
  }
})
