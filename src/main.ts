import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { createPinia } from 'pinia'
import { useAuthStore } from '@/store/auth' // 导入 auth store
import './api/request' // 导入请求配置

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)

// 初始化 auth store
const authStore = useAuthStore()
authStore.initializeAuth()

app.use(router)
app.use(ElementPlus)

// Register all Element Plus icons
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
