<template>
  <div class="chat-bubble-list" ref="chatListRef">
    <!-- 欢迎区域 -->
    <div v-if="messages.length === 0" class="chat-welcome">
      <h1 class="chat-title">你好，我是景智文档助手</h1>
      <p class="chat-subtitle">我可以帮助你创建、编辑和优化各类文档，提供专业的写作建议和内容生成</p>
      
      <!-- 快捷命令按钮区 -->
      <div class="command-section">
        <div class="command-section-title">快捷命令</div>
      <div class="chat-header-actions">
        <div 
          v-for="cmd in quickCommands" 
          :key="cmd.id" 
          class="quick-command-btn"
          @click="handleQuickCommand(cmd)"
        >
          <el-icon><component :is="cmd.icon || 'ChatLineRound'" /></el-icon>
          <span>{{ cmd.name }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 消息列表 -->
    <template v-else>
      <div 
        v-for="(message, index) in messages" 
        :key="message.id"
        :class="[
          'chat-bubble', 
          `chat-bubble-${message.role}`,
          { 'thinking': message.status === 'thinking' },
          { 'generating': message.status === 'generating' },
          { 'error': message.status === 'error' }
        ]"
      >
        <!-- 用户头像 -->
        <div class="chat-avatar" :class="{ 'user-avatar': message.role === 'user' }">
          <el-avatar v-if="message.role === 'user'" :size="36" :src="userAvatar" />
          <AIModelLogo 
            v-else-if="message.modelInfo && message.role === 'assistant'"
            :model="message.modelInfo"
            size="medium"
            class="assistant-model-logo"
          />
          <AIModelLogo 
            v-else-if="currentModel && message.role === 'assistant' && !message.modelInfo"
            :model="currentModel"
            size="medium"
            class="assistant-model-logo"
          />
          <el-avatar v-else :size="36" :src="assistantAvatar" />
        </div>
        
        <!-- 命令标识区域 -->
        <div v-if="message.commandId && message.role === 'user'" class="command-badge">
          <el-tag size="small" type="info">{{ message.commandName || '命令' }}</el-tag>
        </div>
        
        <!-- 消息内容 -->
        <div class="chat-content">
          <!-- 错误消息 -->
          <div v-if="message.status === 'error'" class="chat-error">
            <el-icon><WarningFilled /></el-icon>
            <span>{{ message.error || '生成回复时出错' }}</span>
          </div>
          
          <!-- 正常消息内容 -->
          <template v-else>
            <!-- 用户消息 -->
            <UserMessageBubble 
              v-if="message.role === 'user'"
              :message="message"
              v-model:editing-content="editingContent"
              @save-edit="saveEdit"
              @cancel-edit="cancelEdit"
              :calculate-rows="calculateRows"
            />
            
            <!-- AI消息 -->
            <AssistantMessageBubble
              v-else
              :message="message"
              @sub-command-click="handleSubCommandClick"
              :find-parent-command="findParentCommand"
              />
          </template>
          
          <!-- 附件列表 -->
          <AttachmentList 
            v-if="message.attachments && message.attachments.length > 0" 
            :attachments="message.attachments || []"
            @attachment-click="handleAttachmentClick"
          />
              </div>
              
        <!-- 消息底部操作栏 - 移到外部，排除AI命令类型消息 -->
        <div 
          v-if="!isCommandMessage(message)" 
          class="message-actions-wrapper"
          :class="[
            message.role === 'user' ? 'user-actions' : 'assistant-actions'
          ]"
            >
          <MessageActions
            :role="message.role"
            :status="message.status"
            :feedback="message.feedback"
            :is-latest-message="index === messages.length - 1"
            @copy="copyMessage(message)"
            @regenerate="$emit('regenerate', message)"
            @edit="startEdit(message)"
            @feedback="(value: 'like' | 'dislike') => $emit('feedback', message.id, value)"
          />
          </div>
          
          <!-- 停止生成按钮 -->
        <div class="stop-button-container" v-if="message.role === 'assistant' && message.status === 'generating'">
          <button 
            class="stop-button"
              @click="$emit('stop')"
              :title="'停止生成'"
            >
            <span class="stop-icon"></span>
            <span class="stop-text">停止生成</span>
          </button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { WarningFilled } from '@element-plus/icons-vue'
import UserMessageBubble from './message-bubbles/UserMessageBubble.vue'
import AssistantMessageBubble from './message-bubbles/AssistantMessageBubble.vue'
import AttachmentList from './message-bubbles/AttachmentList.vue'
import MessageActions from './message-bubbles/MessageActions.vue'
import AIModelLogo from './AIModelLogo.vue'
import type { Message, Attachment, Command, SubCommand, AIModel } from '@/types/chat'

// 定义组件属性
const props = defineProps<{
  messages: Message[]
  userAvatar?: string
  assistantAvatar?: string
  quickCommands?: Command[]
  autoScroll?: boolean
  currentModel?: AIModel
}>()

// 定义事件
const emit = defineEmits<{
  (e: 'regenerate', message: Message): void
  (e: 'stop'): void
  (e: 'feedback', messageId: string, feedback: 'like' | 'dislike'): void
  (e: 'command', command: Command): void
  (e: 'subCommand', parentCommand: Command, subCommand: SubCommand, input?: string): void
  (e: 'startEditing', messageId: string): void
  (e: 'cancelEditing', messageId: string): void
  (e: 'saveEditing', messageId: string, content: string): void
}>()

// 设置默认值
const userAvatar = props.userAvatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
const assistantAvatar = props.assistantAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 聊天列表DOM引用
const chatListRef = ref<HTMLElement | null>(null)

// 编辑消息相关状态
const editingContent = ref<string>('')
const editingMessageId = ref<string | null>(null)
const disableAutoScroll = ref<boolean>(false)

// 计算文本区域的行数
const calculateRows = (text: string): number => {
  if (!text) return 1;
  const lines = text.split('\n').length;
  // 每行平均字符数
  const charsPerLine = 50;
  // 计算额外的换行
  const extraLines = Math.floor(text.length / charsPerLine);
  return Math.min(Math.max(1, lines + extraLines), 10); // 最少1行，最多10行
}

// 开始编辑消息
const startEdit = (message: Message) => {
  try {
    editingMessageId.value = message.id;
    editingContent.value = typeof message.content === 'string' ? message.content : '';
    
    // 设置全局禁止滚动标记
    disableAutoScroll.value = true;
    emit('startEditing', message.id);
  } catch (error) {
    console.error('Error in startEdit:', error, message);
    ElMessage.error('编辑消息时出错');
  }
}

// 取消编辑
const cancelEdit = (message: Message) => {
  try {
    if (!message || typeof message !== 'object') {
      console.error('Invalid message object:', message);
      return;
    }
    
    editingMessageId.value = null;
    editingContent.value = '';
    
    // 设置禁止自动滚动标记
    disableAutoScroll.value = true;
    
    if (message && message.id) {
      emit('cancelEditing', message.id);
    } else {
      console.error('Message id is missing:', message);
    }
  } catch (error) {
    console.error('Error in cancelEdit:', error, message);
  }
}

// 保存编辑
const saveEdit = (message: Message) => {
  try {
    if (!message || typeof message !== 'object') {
      console.error('Invalid message object:', message);
      return;
    }
    
    if (editingContent.value.trim() === '') {
      ElMessage.warning('消息内容不能为空');
      return;
    }
    
    if (message && message.id) {
      // 设置禁止自动滚动标记
      disableAutoScroll.value = true;
      emit('saveEditing', message.id, editingContent.value);
      editingMessageId.value = null;
      editingContent.value = '';
    } else {
      console.error('Message id is missing:', message);
    }
  } catch (error) {
    console.error('Error in saveEdit:', error, message);
  }
}

// 判断附件是否为图片
const isImageAttachment = (attachment: Attachment): boolean => {
  return attachment.type.startsWith('image/') || 
    attachment.name.match(/\.(jpg|jpeg|png|gif|webp|svg)$/i) !== null
}

// 复制消息内容
const copyMessage = (message: Message) => {
  navigator.clipboard.writeText(message.content).then(() => {
    ElMessage.success('内容已复制到剪贴板')
  }).catch(err => {
    console.error('复制失败:', err)
    ElMessage.error('复制失败')
  })
}

// 处理附件点击
const handleAttachmentClick = (attachment: Attachment) => {
  // 如果是图片，可以打开预览
  if (isImageAttachment(attachment)) {
    window.open(attachment.url, '_blank')
  } else {
    // 其他类型文件，直接下载
    const link = document.createElement('a')
    link.href = attachment.url
    link.download = attachment.name
    link.target = '_blank'
    link.click()
  }
}

// 处理快捷命令点击
const handleQuickCommand = (command: Command) => {
  emit('command', command)
}

// 处理子命令点击
const handleSubCommandClick = (parentCommand: Command, subCommand: SubCommand) => {
  emit('subCommand', parentCommand, subCommand)
}

// 查找父命令
const findParentCommand = (commandId?: string): Command => {
  if (!commandId) {
    return {} as Command // Return empty command as fallback
  }
  
  // Try to find the command in quickCommands
  const command = props.quickCommands?.find(cmd => cmd.id === commandId)
  
  // If not found, return a basic object with the ID
  return command || { id: commandId } as Command
}

// 判断是否为命令类型的消息 - AI回复中包含子命令的消息
const isCommandMessage = (message: Message): boolean => {
  return message.role === 'assistant' && 
         Array.isArray(message.subCommands) && 
         message.subCommands.length > 0
}

// 滚动到底部
const scrollToBottom = () => {
  if (!chatListRef.value) return
  
  nextTick(() => {
    if (chatListRef.value) {
      chatListRef.value.scrollTop = chatListRef.value.scrollHeight
    }
  })
}

// 暴露给父组件的滚动方法
const forceScrollToBottom = () => {
  setTimeout(() => {
    scrollToBottom();
  }, 100); // 短暂延时确保DOM已更新
}

// 监听消息列表变化，自动滚动到底部
watch(() => props.messages, (messages, oldMessages) => {
  // 调试：打印assistant消息的modelInfo
  messages.forEach(msg => {
    if (msg.role === "assistant") {
      console.log("Assistant message modelInfo:", msg.modelInfo);
      }
    });
    
  try {
    // 如果设置了禁止自动滚动标记，则取消标记并直接返回
    if (disableAutoScroll.value) {
      disableAutoScroll.value = false;
      return;
    }
    
    // 只有在以下情况滚动到底部：
    // 1. 消息数量增加了（新消息）
    // 2. 最后一条消息的内容变化了（流式生成）
    const shouldScroll = props.autoScroll !== false && (
      !oldMessages || 
      messages.length > oldMessages.length ||
      (messages.length > 0 && oldMessages && oldMessages.length > 0 && 
       messages[messages.length - 1].content !== oldMessages[oldMessages.length - 1].content)
    );
    
    if (shouldScroll) {
      scrollToBottom();
    }
  } catch (error) {
    console.error('Error in messages watcher:', error);
  }
}, { deep: true })

// 组件挂载后滚动到底部
onMounted(() => {
  scrollToBottom()
})

// 暴露方法给父组件
defineExpose({
  scrollToBottom: forceScrollToBottom
})
</script>

<style scoped>
.chat-bubble-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  overflow-y: auto;
  height: 100%;
}

/* 欢迎区域样式 */
.chat-welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px 40px;
  text-align: center;
  max-width: 900px;
  margin: 0 auto;
}

.chat-title {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 16px;
  color: var(--el-color-primary);
  letter-spacing: -0.5px;
  position: relative;
  transition: all 0.3s ease;
}

.chat-title::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: var(--el-color-primary);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.chat-welcome:hover .chat-title::after {
  width: 100px;
}

.chat-subtitle {
  font-size: 20px;
  font-weight: 400;
  margin: 24px 0 40px;
  color: var(--el-text-color-secondary);
  max-width: 600px;
  line-height: 1.5;
  opacity: 0.9;
}

.command-section {
  width: 100%;
  max-width: 800px;
  margin-top: 10px;
}

.command-section-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 16px;
  color: var(--el-text-color-secondary);
  text-align: center;
  position: relative;
  display: inline-block;
  left: 50%;
  transform: translateX(-50%);
  padding: 0 20px;
}

.command-section-title::before,
.command-section-title::after {
  content: "";
  position: absolute;
  top: 50%;
  width: 30px;
  height: 1px;
  background-color: var(--el-border-color);
}

.command-section-title::before {
  right: 100%;
}

.command-section-title::after {
  left: 100%;
}

.chat-header-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 14px;
  max-width: 800px;
  margin-top: 20px;
  animation: fadeInUp 0.8s ease-out forwards;
  opacity: 1;
}

@keyframes fadeInUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.quick-command-btn {
  display: flex;
  align-items: center;
  padding: 14px 18px;
  border-radius: 10px;
  background-color: var(--el-bg-color-overlay);
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: 1px solid var(--el-border-color-light);
  width: auto;
  min-width: 160px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
  position: relative;
  overflow: hidden;
}

.quick-command-btn:hover {
  transform: translateY(-2px);
  border-color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
  box-shadow: 0 4px 12px rgba(var(--el-color-primary-rgb), 0.15);
}

.quick-command-btn .el-icon {
  font-size: 22px;
  margin-right: 12px;
  color: var(--el-color-primary);
  transition: transform 0.25s ease;
}

.quick-command-btn:hover .el-icon {
  transform: scale(1.1);
  color: var(--el-color-primary);
}

.quick-command-btn span {
  font-size: 15px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  transition: color 0.25s ease;
  text-align: left;
  margin-top: 0;
}

.quick-command-btn:hover span {
  color: var(--el-color-primary);
}

/* 消息气泡样式 */
.chat-bubble {
  display: flex;
  margin-bottom: 24px;
  position: relative;
}

.chat-bubble-user {
  flex-direction: row-reverse;
}

.chat-avatar {
  margin: 0 12px;
  position: relative;
}

.assistant-model-logo {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.chat-content {
  max-width: 80%;
  border-radius: 12px;
  padding: 12px 16px;
  position: relative;
}

.chat-bubble-assistant .chat-content {
  background-color: #f5f7fa;
  border: 1px solid #ebeef5;
}

.chat-bubble-user .chat-content {
  background-color: #ecf5ff;
  border: 1px solid #d9ecff;
}

/* 命令标识 */
.command-badge {
  position: absolute;
  top: -20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1;
  white-space: nowrap;
}

.command-badge .el-tag {
  font-size: 12px;
  padding: 0 8px;
  height: 20px;
  line-height: 18px;
  border-radius: 10px;
  background-color: #f4f4f5;
  border-color: #e9e9eb;
  color: #909399;
}

.chat-bubble-user .command-badge {
  left: auto;
  right: 50px;
}

/* 用户消息样式 */
.user-message {
  white-space: pre-wrap;
  word-break: break-word;
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

/* 错误消息样式 */
.chat-error {
  color: #f56c6c;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 附件样式 */
.attachments-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.attachment-item {
  cursor: pointer;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid #ebeef5;
}

.image-attachment {
  width: 100px;
  height: 100px;
}

.image-attachment img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.file-attachment {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background-color: #f5f7fa;
}

.file-attachment .el-icon {
  margin-right: 8px;
  font-size: 18px;
  color: #909399;
}

.file-attachment span {
  font-size: 12px;
  color: #606266;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 消息操作按钮 - 修改为外部显示，自适应位置 */
.message-actions-wrapper {
  position: absolute;
  z-index: 1;
  transition: opacity 0.2s;
  opacity: 0;
}

/* 助手消息操作按钮位置 */
.assistant-actions {
  bottom: -30px;
  left: 50%;
  transform: translateX(-50%);
}

/* 用户消息操作按钮位置 */
.user-actions {
  bottom: -30px;
  right: 10%;
}

.chat-bubble:hover .message-actions-wrapper {
  opacity: 1;
}

.message-actions {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
}

.feedback-buttons {
  display: flex;
  gap: 4px;
}

.feedback-buttons .el-button.active {
  color: #409eff;
}

/* 停止生成按钮 */
.stop-button-container {
  display: flex;
  justify-content: center;
  margin-top: 12px;
  position: relative;
}

.stop-button {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 6px 14px;
  background-color: var(--el-color-danger-light-9);
  border: 1px solid var(--el-color-danger-light-5);
  color: var(--el-color-danger);
  border-radius: 18px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 6px rgba(245, 108, 108, 0.1);
  position: relative;
  overflow: hidden;
}

.stop-button:hover {
  background-color: var(--el-color-danger-light-8);
  border-color: var(--el-color-danger-light-3);
  transform: translateY(-1px);
  box-shadow: 0 3px 8px rgba(245, 108, 108, 0.15);
}

.stop-button:active {
  transform: translateY(0);
  box-shadow: 0 1px 3px rgba(245, 108, 108, 0.1);
}

.stop-button::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 5px;
  height: 5px;
  background: rgba(245, 108, 108, 0.3);
  opacity: 0;
  border-radius: 100%;
  transform: scale(1) translate(-50%, -50%);
  transform-origin: 50% 50%;
}

.stop-button:active::after {
  animation: ripple 0.6s ease-out;
}

.stop-icon {
  display: inline-block;
  width: 12px;
  height: 12px;
  background-color: var(--el-color-danger);
  border-radius: 2px;
  margin-right: 6px;
}

.stop-text {
  line-height: 1;
}

/* 编辑消息样式 */
.edit-message-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.edit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.edited-badge {
  font-size: 12px;
  color: #909399;
  margin-left: 6px;
  font-style: italic;
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .chat-bubble-assistant .chat-content {
    background-color: #2d2d2d;
    border: 1px solid #444;
  }
  
  .chat-bubble-user .chat-content {
    background-color: #213d5b;
    border: 1px solid #345d82;
  }
  
  .quick-command-btn {
    background-color: #2d2d2d;
  }
  
  .quick-command-btn span {
    color: #ddd;
  }
  
  .chat-subtitle {
    color: #aaa;
  }
  
  .file-attachment {
    background-color: #2d2d2d;
  }
  
  .file-attachment span {
    color: #ddd;
  }
  
  .command-badge .el-tag {
    background-color: #2d2d2d;
    border-color: #444;
    color: #a8abb2;
  }
  
  /* 深色模式下的停止按钮 */
  .stop-button {
    background-color: rgba(245, 108, 108, 0.1);
    border-color: rgba(245, 108, 108, 0.3);
    color: #ff6b6b;
  }
  
  .stop-button:hover {
    background-color: rgba(245, 108, 108, 0.2);
    border-color: rgba(245, 108, 108, 0.4);
  }
  
  .stop-icon {
    background-color: #ff6b6b;
  }
  
  /* Dark mode message actions are now handled by the MessageActions component */
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

.command-section {
  width: 100%;
  max-width: 900px;
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: fadeInUp 0.8s ease-out forwards;
  animation-delay: 0.3s;
  opacity: 0;
}

.command-section-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--el-text-color-secondary);
  margin-bottom: 16px;
  text-align: center;
  position: relative;
  display: inline-block;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.command-section-title::before,
.command-section-title::after {
  content: '';
  position: absolute;
  top: 50%;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--el-border-color-light), transparent);
  width: 40px;
}

.command-section-title::before {
  right: 100%;
  margin-right: 15px;
}

.command-section-title::after {
  left: 100%;
  margin-left: 15px;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style> 
