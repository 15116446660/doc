<template>
  <div>
    <!-- 模型信息显示 -->
    <div v-if="message.modelInfo" class="model-info">
      <AIModelLogo 
        v-if="message.modelInfo"
        :model="message.modelInfo"
        size="small"
        class="model-info-logo"
      />
      <span class="model-info-name">{{ message.modelInfo.name }}</span>
    </div>
    
    <!-- 生成中状态 -->
    <div v-if="message.status === 'generating' || message.status === 'thinking'" class="generating-indicator">
      <div v-if="message.status === 'thinking'" class="thinking-text">思考中...</div>
      <div v-else class="generating-text">生成中...</div>
      <div class="typing-animation">
        <span></span>
        <span></span>
        <span></span>
      </div>
    </div>
    
    <!-- 已完成的AI消息 -->
    <MarkdownMessage 
      :content="message.content" 
      :thinking="message.thinking"
    />
    
    <!-- 子命令按钮区域 -->
    <div 
      v-if="message.subCommands && message.subCommands.length > 0" 
      class="sub-commands-container"
    >
      <div class="sub-commands-list">
        <div 
          v-for="subCmd in message.subCommands" 
          :key="subCmd.id"
          class="sub-command-button"
          @click="handleSubCommandClick(subCmd)"
        >
          <div class="sub-command-icon">
            <el-icon><component :is="subCmd.icon || 'Document'" /></el-icon>
          </div>
          <div class="sub-command-info">
            <div class="sub-command-name">{{ subCmd.name }}</div>
            <div class="sub-command-description" v-if="subCmd.description">
              {{ subCmd.description }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import MarkdownMessage from '../MarkdownMessage.vue'
import AIModelLogo from '../AIModelLogo.vue'
import type { Message, SubCommand, Command } from '@/types/chat'

// 定义组件属性
const props = defineProps<{
  message: Message
  findParentCommand: (commandId?: string) => Command
}>()

// 定义事件
const emit = defineEmits<{
  (e: 'sub-command-click', parentCommand: Command, subCommand: SubCommand): void
}>()

// 处理子命令点击
const handleSubCommandClick = (subCommand: SubCommand) => {
  const parentCommand = props.findParentCommand(props.message.commandId)
  emit('sub-command-click', parentCommand, subCommand)
}
</script>

<style scoped>
/* 模型信息样式 */
.model-info {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  padding: 4px 8px;
  background-color: var(--el-fill-color-lighter);
  border-radius: 4px;
  font-size: 12px;
}

.model-info-logo {
  margin-right: 6px;
  width: 16px;
  height: 16px;
}

.model-info-name {
  color: var(--el-text-color-secondary);
  font-weight: 500;
}

/* 生成中动画 */
.generating-indicator {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.thinking-text, .generating-text {
  margin-right: 8px;
  font-style: italic;
  color: #909399;
}

.typing-animation {
  display: inline-flex;
  align-items: center;
}

.typing-animation span {
  height: 8px;
  width: 8px;
  margin: 0 2px;
  background-color: #909399;
  border-radius: 50%;
  display: inline-block;
  animation: typing 1.4s infinite ease-in-out both;
}

.typing-animation span:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-animation span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typing {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

/* Add styles for sub-commands */
.sub-commands-container {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--el-border-color-light);
}

.sub-commands-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sub-command-button {
  display: flex;
  align-items: center;
  padding: 10px;
  border-radius: 6px;
  background-color: var(--el-fill-color-lighter);
  cursor: pointer;
  transition: all 0.2s;
}

.sub-command-button:hover {
  background-color: var(--el-fill-color-light);
  transform: translateY(-2px);
}

.sub-command-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 4px;
  background-color: var(--el-color-primary-light-8);
  color: var(--el-color-primary);
  margin-right: 10px;
}

.sub-command-info {
  flex: 1;
}

.sub-command-name {
  font-weight: 500;
  margin-bottom: 2px;
}

.sub-command-description {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.sub-command-button:hover {
  background-color: var(--el-fill-color-light);
  transform: translateY(-2px);
}

.sub-command-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 4px;
  background-color: var(--el-color-primary-light-8);
  color: var(--el-color-primary);
  margin-right: 10px;
}

.sub-command-info {
  flex: 1;
}

.sub-command-name {
  font-weight: 500;
  margin-bottom: 2px;
}

.sub-command-description {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style> 