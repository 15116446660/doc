import { ref, computed, readonly } from 'vue'
import { useLocalStorage } from './composables/useLocalStorage'

// Placeholder Types and API
// These should be moved to appropriate files in src/types and src/api later
interface User {
  id: number;
  name: string;
  role: string;
}

interface LoginCredentials {
  username?: string;
  password?: string;
}

const authApi = {
  login: async (credentials: LoginCredentials) => {
    console.log('Mock login with', credentials);
    await new Promise(resolve => setTimeout(resolve, 500));
    const userData = { id: 1, name: 'Test User', role: 'ADMIN' };
    const authToken = 'mock-jwt-token';
    const userPermissions = ['review:dashboard:view', 'review:task:view', 'review:task:create', 'review:issue:view'];
    return { data: { user: userData, token: authToken, permissions: userPermissions } };
  },
  logout: async () => {
    console.log('Mock logout');
    await new Promise(resolve => setTimeout(resolve, 100));
    return;
  }
};
// End Placeholder

const user = ref<User | null>(null)
const token = ref<string>('')
const permissions = ref<string[]>([])
const isLoading = ref(false)

const { value: persistedToken, setValue: setPersistedToken } = useLocalStorage('auth_token', '')
const { value: persistedUser, setValue: setPersistedUser } = useLocalStorage('auth_user', null)

export function useAuthStore() {
  // 计算属性
  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const userRole = computed(() => user.value?.role || '')
  const hasPermission = computed(() => (permission: string) => permissions.value.includes(permission))

  // 初始化状态
  const initializeAuth = () => {
    if (persistedToken.value) {
      token.value = persistedToken.value
    }
    if (persistedUser.value) {
      user.value = persistedUser.value
    }
  }

  // 登录
  const login = async (credentials: LoginCredentials) => {
    isLoading.value = true
    try {
      const response = await authApi.login(credentials)
      const { user: userData, token: authToken, permissions: userPermissions } = response.data

      user.value = userData
      token.value = authToken
      permissions.value = userPermissions

      // 持久化到本地存储
      setPersistedToken(authToken)
      setPersistedUser(userData)

      return { success: true }
    } catch (error: any) {
      return { success: false, error: error.message }
    } finally {
      isLoading.value = false
    }
  }

  // 登出
  const logout = async () => {
    try {
      await authApi.logout()
    } finally {
      user.value = null
      token.value = ''
      permissions.value = []
      setPersistedToken('')
      setPersistedUser(null)
    }
  }

  // 更新用户信息
  const updateUser = (userData: Partial<User>) => {
    if (user.value) {
      user.value = { ...user.value, ...userData }
      setPersistedUser(user.value)
    }
  }

  return {
    // 状态
    user: readonly(user),
    token: readonly(token),
    permissions: readonly(permissions),
    isLoading: readonly(isLoading),

    // 计算属性
    isAuthenticated,
    userRole,
    hasPermission,

    // 方法
    initializeAuth,
    login,
    logout,
    updateUser
  }
}
