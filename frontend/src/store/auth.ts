import { defineStore } from 'pinia'
import { login as apiLogin, logout as apiLogout, getCurrentUser, type LoginRequest, type UserInfo } from '@/api/auth'

interface AuthState {
  isAuthenticated: boolean;
  user: UserInfo | null;
  token: string | null;
  refreshToken: string | null;
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    isAuthenticated: false,
    user: null,
    token: null,
    refreshToken: null
  }),
  actions: {
    async login(username: string, password: string): Promise<void> {
      try {
        const loginRequest: LoginRequest = {
          usernameOrEmail: username,
          password: password,
          rememberMe: false
        }

        const response = await apiLogin(loginRequest)

        // 设置认证状态
        this.isAuthenticated = true
        this.user = response.userInfo
        this.token = response.accessToken
        this.refreshToken = response.refreshToken

        // 存储到localStorage
        localStorage.setItem('token', response.accessToken)
        localStorage.setItem('refreshToken', response.refreshToken)
        localStorage.setItem('user', JSON.stringify(response.userInfo))

        console.log('登录成功:', response.userInfo)
      } catch (error: any) {
        // 清除认证状态
        this.isAuthenticated = false
        this.user = null
        this.token = null
        this.refreshToken = null

        console.error('登录失败:', error)
        throw error
      }
    },

    // 初始化认证状态
    initializeAuth() {
      const token = localStorage.getItem('token')
      const refreshToken = localStorage.getItem('refreshToken')
      const user = localStorage.getItem('user')

      if (token && user) {
        try {
          this.token = token
          this.refreshToken = refreshToken
          this.user = JSON.parse(user)
          this.isAuthenticated = true
          console.log('认证状态已从localStorage初始化')
        } catch (e) {
          console.error('解析localStorage中的用户信息失败', e)
          this.logout()
        }
      }
    },

    // 登出
    async logout() {
      try {
        // 调用后端登出接口
        await apiLogout()
      } catch (error) {
        console.error('登出接口调用失败:', error)
      } finally {
        // 清除本地状态
        this.isAuthenticated = false
        this.user = null
        this.token = null
        this.refreshToken = null
        localStorage.removeItem('token')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('user')
        console.log('已登出')
      }
    },

    // 获取当前用户信息
    async fetchCurrentUser() {
      try {
        const userInfo = await getCurrentUser()
        this.user = userInfo
        localStorage.setItem('user', JSON.stringify(userInfo))
        return userInfo
      } catch (error) {
        console.error('获取用户信息失败:', error)
        throw error
      }
    }
  }
}); 