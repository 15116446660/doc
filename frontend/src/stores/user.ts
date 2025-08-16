import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
  state: () => ({
    userId: 'current-user', // 默认用户ID，实际应用中应该从登录状态获取
    username: '',
    avatar: '',
    token: ''
  }),

  getters: {
    isLoggedIn: (state) => !!state.token
  },

  actions: {
    setUser(user: { userId: string; username: string; avatar?: string; token: string }) {
      this.userId = user.userId;
      this.username = user.username;
      this.avatar = user.avatar || '';
      this.token = user.token;
    },

    clearUser() {
      this.userId = '';
      this.username = '';
      this.avatar = '';
      this.token = '';
    }
  }
}); 
 
 
 
 
 
 
 