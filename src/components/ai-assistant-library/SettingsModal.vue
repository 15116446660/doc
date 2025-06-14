<template>
  <el-dialog
    title="设置"
    :modelValue="visible"
    @update:modelValue="(val: boolean) => emit('update:visible', val)"
    width="500px"
    :before-close="handleClose"
    append-to-body
  >
    <div class="settings-container">
      <!-- 主题设置 -->
      <div class="setting-section">
        <h3 class="section-title">主题设置</h3>
        <el-radio-group v-model="localTheme" class="theme-options">
          <el-radio-button label="light">
            <el-icon><Sunny /></el-icon>
            亮色
          </el-radio-button>
          <el-radio-button label="dark">
            <el-icon><Moon /></el-icon>
            暗色
          </el-radio-button>
          <el-radio-button label="auto">
            <el-icon><Monitor /></el-icon>
            自动
          </el-radio-button>
        </el-radio-group>
        <div class="setting-description">
          自动模式将根据系统设置自动切换亮色/暗色主题
        </div>
      </div>
      
      <!-- 头像设置 -->
      <div class="setting-section">
        <h3 class="section-title">头像设置</h3>
        
        <!-- 用户头像 -->
        <div class="avatar-setting">
          <div class="avatar-label">用户头像</div>
          <div class="avatar-container">
            <el-avatar :src="localUserAvatar" :size="60" />
            <div class="avatar-actions">
              <el-button type="primary" size="small" @click="openUserAvatarUpload">
                <el-icon><Upload /></el-icon>
                上传
              </el-button>
              <el-button type="default" size="small" @click="resetUserAvatar">
                <el-icon><RefreshRight /></el-icon>
                重置
              </el-button>
            </div>
          </div>
        </div>
        
        <!-- 助手头像 -->
        <div class="avatar-setting">
          <div class="avatar-label">助手头像</div>
          <div class="avatar-container">
            <el-avatar :src="localAssistantAvatar" :size="60" />
            <div class="avatar-actions">
              <el-button type="primary" size="small" @click="openAssistantAvatarUpload">
                <el-icon><Upload /></el-icon>
                上传
              </el-button>
              <el-button type="default" size="small" @click="resetAssistantAvatar">
                <el-icon><RefreshRight /></el-icon>
                重置
              </el-button>
            </div>
          </div>
        </div>
        
        <!-- 隐藏的文件上传输入 -->
        <input
          type="file"
          ref="userAvatarInputRef"
          style="display: none"
          accept="image/*"
          @change="handleUserAvatarChange"
        />
        <input
          type="file"
          ref="assistantAvatarInputRef"
          style="display: none"
          accept="image/*"
          @change="handleAssistantAvatarChange"
        />
      </div>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Sunny, Moon, Monitor, Upload, RefreshRight } from '@element-plus/icons-vue'

// 默认头像
const DEFAULT_USER_AVATAR = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
const DEFAULT_ASSISTANT_AVATAR = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 组件属性
const props = defineProps<{
  visible: boolean
  theme?: 'light' | 'dark' | 'auto'
  userAvatar?: string
  assistantAvatar?: string
}>()

// 组件事件
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'save', settings: { theme: 'light' | 'dark' | 'auto', userAvatar: string, assistantAvatar: string }): void
  (e: 'close'): void
}>()

// 本地状态
const localTheme = ref<'light' | 'dark' | 'auto'>(props.theme || 'auto')
const localUserAvatar = ref<string>(props.userAvatar || DEFAULT_USER_AVATAR)
const localAssistantAvatar = ref<string>(props.assistantAvatar || DEFAULT_ASSISTANT_AVATAR)

// 文件输入引用
const userAvatarInputRef = ref<HTMLInputElement | null>(null)
const assistantAvatarInputRef = ref<HTMLInputElement | null>(null)

// 监听属性变化
watch(() => props.theme, (newTheme) => {
  if (newTheme) {
    localTheme.value = newTheme
  }
})

watch(() => props.userAvatar, (newAvatar) => {
  if (newAvatar) {
    localUserAvatar.value = newAvatar
  }
})

watch(() => props.assistantAvatar, (newAvatar) => {
  if (newAvatar) {
    localAssistantAvatar.value = newAvatar
  }
})

// 打开用户头像上传
const openUserAvatarUpload = () => {
  if (userAvatarInputRef.value) {
    userAvatarInputRef.value.click()
  }
}

// 打开助手头像上传
const openAssistantAvatarUpload = () => {
  if (assistantAvatarInputRef.value) {
    assistantAvatarInputRef.value.click()
  }
}

// 处理用户头像变更
const handleUserAvatarChange = (e: Event) => {
  const input = e.target as HTMLInputElement
  if (input.files && input.files.length > 0) {
    const file = input.files[0]
    
    // 验证文件类型
    if (!file.type.startsWith('image/')) {
      ElMessage.error('请选择图片文件')
      return
    }
    
    // 创建URL
    const imageUrl = URL.createObjectURL(file)
    localUserAvatar.value = imageUrl
    
    // 重置文件输入
    input.value = ''
  }
}

// 处理助手头像变更
const handleAssistantAvatarChange = (e: Event) => {
  const input = e.target as HTMLInputElement
  if (input.files && input.files.length > 0) {
    const file = input.files[0]
    
    // 验证文件类型
    if (!file.type.startsWith('image/')) {
      ElMessage.error('请选择图片文件')
      return
    }
    
    // 创建URL
    const imageUrl = URL.createObjectURL(file)
    localAssistantAvatar.value = imageUrl
    
    // 重置文件输入
    input.value = ''
  }
}

// 重置用户头像
const resetUserAvatar = () => {
  localUserAvatar.value = DEFAULT_USER_AVATAR
}

// 重置助手头像
const resetAssistantAvatar = () => {
  localAssistantAvatar.value = DEFAULT_ASSISTANT_AVATAR
}

// 关闭对话框
const handleClose = () => {
  emit('update:visible', false)
  emit('close')
}

// 保存设置
const handleSave = () => {
  emit('save', {
    theme: localTheme.value,
    userAvatar: localUserAvatar.value,
    assistantAvatar: localAssistantAvatar.value
  })
  emit('update:visible', false)
}
</script>

<style scoped>
.settings-container {
  padding: 0 10px;
}

.setting-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 16px;
  color: var(--el-text-color-primary);
}

.theme-options {
  display: flex;
  justify-content: space-between;
  width: 100%;
}

.setting-description {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 8px;
}

.avatar-setting {
  margin-bottom: 16px;
}

.avatar-label {
  margin-bottom: 8px;
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.avatar-container {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar-actions {
  display: flex;
  gap: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 