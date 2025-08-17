import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { useAuth } from '@/hooks/useAuth' // 导入 useAuth hook
import './api/request' // 导入请求配置

// 浏览器兼容性检查
function checkBrowserCompatibility() {
  const ua = navigator.userAgent
  let isSupported = true
  let browserInfo = ''

  // 检查Chrome版本
  if (ua.indexOf('Chrome') > -1) {
    const match = ua.match(/Chrome\/(\d+)/)
    if (match) {
      const version = parseInt(match[1])
      browserInfo = `Chrome ${version}`
      if (version < 72) {
        isSupported = false
      }
    }
  }

  // 检查关键JavaScript特性
  const features = [
    () => typeof Promise !== 'undefined',
    () => typeof Symbol !== 'undefined',
    () => typeof Map !== 'undefined',
    () => typeof Set !== 'undefined',
    () => {
      try {
        new Function('() => {}')
        return true
      } catch (e) {
        return false
      }
    }
  ]

  const unsupportedFeatures = features.filter(test => !test())

  if (!isSupported || unsupportedFeatures.length > 0) {
    console.warn(`浏览器兼容性警告: ${browserInfo}`)
    console.warn('检测到浏览器可能不支持某些现代JavaScript特性')

    // 显示兼容性提示
    const compatibilityWarning = document.createElement('div')
    compatibilityWarning.innerHTML = `
      <div style="
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        background: #fff3cd;
        border-bottom: 1px solid #ffeaa7;
        padding: 10px;
        text-align: center;
        z-index: 9999;
        font-family: Arial, sans-serif;
      ">
        <strong>浏览器兼容性提示:</strong>
        检测到您使用的是 ${browserInfo}，可能无法完全支持本应用的所有功能。
        <a href="/browser-check.html" target="_blank" style="color: #007bff; margin-left: 10px;">查看详细信息</a>
        <button onclick="this.parentElement.parentElement.remove()" style="
          margin-left: 10px;
          background: none;
          border: 1px solid #ffc107;
          padding: 2px 8px;
          cursor: pointer;
        ">关闭</button>
      </div>
    `
    document.body.appendChild(compatibilityWarning)
  }

  return isSupported
}

// 错误处理
function setupErrorHandling(app: any) {
  // Vue错误处理
  app.config.errorHandler = (err: any, vm: any, info: string) => {
    console.error('Vue Error:', err)
    console.error('Error Info:', info)

    // 如果是关键错误，显示降级页面
    if (err.message && (
      err.message.includes('Cannot read property') ||
      err.message.includes('is not a function') ||
      err.message.includes('Unexpected token')
    )) {
      showFallbackUI(err.message)
    }
  }

  // 全局JavaScript错误处理
  window.addEventListener('error', (event) => {
    console.error('Global Error:', event.error)
    if (event.error && event.error.message) {
      showFallbackUI(event.error.message)
    }
  })

  // Promise错误处理
  window.addEventListener('unhandledrejection', (event) => {
    console.error('Unhandled Promise Rejection:', event.reason)
    event.preventDefault()
  })
}

// 显示降级UI
function showFallbackUI(errorMessage: string) {
  const fallbackDiv = document.createElement('div')
  fallbackDiv.innerHTML = `
    <div style="
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: white;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      z-index: 10000;
      font-family: Arial, sans-serif;
    ">
      <h2 style="color: #dc3545;">应用加载失败</h2>
      <p style="margin: 20px; text-align: center; max-width: 600px;">
        抱歉，应用在您的浏览器中无法正常运行。这可能是由于浏览器版本过低或不支持某些现代Web特性导致的。
      </p>
      <div style="margin: 20px;">
        <button onclick="location.reload()" style="
          background: #007bff;
          color: white;
          border: none;
          padding: 10px 20px;
          margin: 5px;
          border-radius: 4px;
          cursor: pointer;
        ">重新加载</button>
        <a href="/browser-check.html" style="
          background: #28a745;
          color: white;
          text-decoration: none;
          padding: 10px 20px;
          margin: 5px;
          border-radius: 4px;
          display: inline-block;
        ">检查浏览器兼容性</a>
      </div>
      <details style="margin-top: 20px; max-width: 600px;">
        <summary style="cursor: pointer;">技术详情</summary>
        <pre style="background: #f8f9fa; padding: 10px; margin-top: 10px; overflow: auto;">
错误信息: ${errorMessage}
浏览器: ${navigator.userAgent}
        </pre>
      </details>
    </div>
  `
  document.body.appendChild(fallbackDiv)
}

try {
  // 检查浏览器兼容性
  checkBrowserCompatibility()

  const app = createApp(App)

  // 设置错误处理
  setupErrorHandling(app)

  // 初始化认证状态
  // 这必须在 router initalization 之前，以便路由守卫可以访问正确的状态
  const { initializeAuth } = useAuth()
  initializeAuth()

  app.use(router)
  app.use(ElementPlus)

  // Register all Element Plus icons
  for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
  }

  app.mount('#app')
} catch (error) {
  console.error('应用初始化失败:', error)
  showFallbackUI(error instanceof Error ? error.message : '未知错误')
}
