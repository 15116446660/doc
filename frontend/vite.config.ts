import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { viteMockServe } from 'vite-plugin-mock'
import legacy from '@vitejs/plugin-legacy'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    viteMockServe({
      mockPath: 'src/mock',
      enable: true,
      logger: true,
    }),
    // 添加legacy插件以支持旧版浏览器
    legacy({
      targets: ['chrome >= 72', 'firefox >= 60', 'safari >= 12'],
      additionalLegacyPolyfills: ['regenerator-runtime/runtime'],
      renderLegacyChunks: true,
      polyfills: [
        'es.symbol',
        'es.array.filter',
        'es.promise',
        'es.promise.finally',
        'es/map',
        'es/set',
        'es.array.for-each',
        'es.object.define-properties',
        'es.object.define-property',
        'es.object.get-own-property-descriptor',
        'es.object.get-own-property-descriptors',
        'es.object.keys',
        'es.object.to-string',
        'web.dom-collections.for-each',
        'esnext.global-this',
        'esnext.string.match-all'
      ]
    }),
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  build: {
    // 设置构建目标以支持较老的浏览器
    target: ['es2015', 'chrome63'],
    // 启用CSS代码分割
    cssCodeSplit: true,
    // 生成sourcemap便于调试
    sourcemap: true,
  },
  // 开发服务器配置
  server: {
    // 确保开发环境也使用兼容的构建
    force: true,
  },
  // 优化配置
  optimizeDeps: {
    // 预构建依赖以提高兼容性
    include: ['element-plus', 'vue', 'vue-router', 'pinia'],
  },
})
