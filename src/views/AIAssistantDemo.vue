<template>
  <div class="ai-assistant-demo">
    <div class="demo-container">
      <Chat 
        :theme="theme"
        :userAvatar="userAvatar"
        :assistantAvatar="assistantAvatar"
        @themeChange="handleThemeChange"
        @avatarChange="handleAvatarChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Chat } from '@/components/ai-assistant-library'

// 状态
const theme = ref<'light' | 'dark' | 'auto'>('auto')
const userAvatar = ref<string>('https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png')
const assistantAvatar = ref<string>('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png')

// 选择头像
function selectAvatar() {
  // 这里可以实现头像选择逻辑，例如打开文件选择器
  // 简化处理，直接切换几个预设头像
  const avatars = [
    'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
    'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png'
  ]
  
  // 找到当前头像的索引，切换到下一个
  const currentIndex = avatars.indexOf(userAvatar.value)
  const nextIndex = (currentIndex + 1) % avatars.length
  userAvatar.value = avatars[nextIndex]
  
  ElMessage.success('头像已更换')
}

// 处理主题变更
function handleThemeChange(newTheme: 'light' | 'dark' | 'auto') {
  theme.value = newTheme
}

// 处理头像变更
function handleAvatarChange(type: 'user' | 'assistant', url: string) {
  if (type === 'user') {
    userAvatar.value = url
  } else {
    assistantAvatar.value = url
  }
}
</script>

<style scoped>
.ai-assistant-demo {
  padding: 0px;
  height: 100%;
}

.demo-container {
  height: 100%;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  overflow: hidden;
  margin: 10px 0;
}

.demo-controls {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
}

.avatar-selector {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 10px;
}
</style> 