import { defineStore } from 'pinia'
import { authApi } from '../api'

function readUser() {
  try {
    return JSON.parse(localStorage.getItem('user') || 'null')
  } catch {
    return null
  }
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: readUser()
  }),

  getters: {
    isLogin: state => !!state.token,
    isAdmin: state => state.user?.role === 1,
    nickname: state => state.user?.nickname || state.user?.username || ''
  },

  actions: {
    async login(form) {
      const res = await authApi.login(form)
      this.token = res.data.token
      this.user = res.data.user
      localStorage.setItem('token', this.token)
      localStorage.setItem('user', JSON.stringify(this.user))
    },

    /** 刷新当前用户信息 */
    async fetchMe() {
      if (!this.token) return
      const res = await authApi.me()
      this.user = res.data
      localStorage.setItem('user', JSON.stringify(this.user))
    },

    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
