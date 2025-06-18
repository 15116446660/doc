<template>
  <div class="chat-bubble-list" ref="chatListRef">
    <!-- 欢迎区域 -->
    <div v-if="messages.length === 0" class="chat-welcome">
      <h1 class="chat-title">你好，我是景智文档助手</h1>
      <p class="chat-subtitle">我今天能帮你什么？</p>
      
      <!-- 快捷命令按钮区 -->
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
          <el-avatar :size="36" :src="message.role === 'user' ? userAvatar : assistantAvatar" />
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
            <div v-if="message.role === 'user'">
              <!-- 编辑模式 -->
              <div v-if="message.status === 'editing'" class="edit-message-container">
                <el-input
                  type="textarea"
                  v-model="editingContent"
                  :rows="calculateRows(editingContent)"
                  placeholder="编辑消息..."
                  resize="none"
                  autofocus
                  :autosize="{ minRows: 1, maxRows: 10 }"
                />
                <div class="edit-actions">
                  <el-button size="small" @click="cancelEdit(message)">取消</el-button>
                  <el-button size="small" type="primary" @click="saveEdit(message)">发送</el-button>
                </div>
              </div>
              
              <!-- 显示模式 -->
              <div v-else class="user-message">
                {{ message.content }}
                <span v-if="message.edited" class="edited-badge">(已编辑)</span>
              </div>
            </div>
            
            <!-- AI消息 -->
            <template v-else>
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
                :content="message.content || fullMarkdownExample" 
                :thinking="message.thinking"
              />
            </template>
          </template>
          
          <!-- 附件列表 -->
          <div v-if="message.attachments && message.attachments.length > 0" class="attachments-container">
            <div 
              v-for="attachment in message.attachments" 
              :key="attachment.id" 
              class="attachment-item"
              @click="handleAttachmentClick(attachment)"
            >
              <!-- 图片附件 -->
              <div v-if="isImageAttachment(attachment)" class="image-attachment">
                <img :src="attachment.thumbnail || attachment.url" :alt="attachment.name" />
              </div>
              
              <!-- 其他类型附件 -->
              <div v-else class="file-attachment">
                <el-icon><Document /></el-icon>
                <span>{{ attachment.name }}</span>
              </div>
            </div>
          </div>
          
          <!-- 消息底部操作栏 -->
          <div class="message-actions" v-if="message.role === 'assistant' && message.status === 'completed'">
            <el-button 
              size="small" 
              type="text" 
              @click="copyMessage(message)"
              :title="'复制内容'"
            >
              <el-icon><DocumentCopy /></el-icon>
            </el-button>
            
            <el-button 
              size="small" 
              type="text" 
              @click="$emit('regenerate', message)"
              v-if="index === messages.length - 1"
              :title="'重新生成'"
            >
              <el-icon><RefreshRight /></el-icon>
            </el-button>
            
            <div class="feedback-buttons">
              <el-button 
                size="small" 
                type="text" 
                @click="$emit('feedback', message.id, 'like')"
                :class="{ active: message.feedback === 'like' }"
                :title="'有帮助'"
              >
                <el-icon><Star /></el-icon>
              </el-button>
              
              <el-button 
                size="small" 
                type="text" 
                @click="$emit('feedback', message.id, 'dislike')"
                :class="{ active: message.feedback === 'dislike' }"
                :title="'没帮助'"
              >
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
          </div>
          
          <!-- 用户消息操作栏 -->
          <div class="message-actions" v-if="message.role === 'user' && message.status !== 'editing'">
            <el-button 
              size="small" 
              type="text" 
              @click="copyMessage(message)"
              :title="'复制内容'"
            >
              <el-icon><DocumentCopy /></el-icon>
            </el-button>
            
            <el-button 
              size="small" 
              type="text" 
              @click="startEdit(message)"
              :title="'编辑消息'"
            >
              <el-icon><Edit /></el-icon>
            </el-button>
          </div>
          
          <!-- 停止生成按钮 -->
          <div class="stop-button" v-if="message.role === 'assistant' && message.status === 'generating'">
            <el-button 
              size="small" 
              @click="$emit('stop')"
              :title="'停止生成'"
            >
              停止生成
            </el-button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Document, 
  DocumentCopy, 
  RefreshRight, 
  Star,
  Close,
  WarningFilled,
  Edit
} from '@element-plus/icons-vue'
import MarkdownMessage from './MarkdownMessage.vue'
import type { Message, Attachment, Command } from '@/types/chat'

// 定义组件属性
const props = defineProps<{
  messages: Message[]
  userAvatar?: string
  assistantAvatar?: string
  quickCommands?: Command[]
  autoScroll?: boolean
}>()

// 定义事件
const emit = defineEmits<{
  (e: 'regenerate', message: Message): void
  (e: 'stop'): void
  (e: 'feedback', messageId: string, feedback: 'like' | 'dislike'): void
  (e: 'command', commandId: string): void
  (e: 'startEditing', messageId: string): void
  (e: 'cancelEditing', messageId: string): void
  (e: 'saveEditing', messageId: string, content: string): void
}>()

const fullMarkdownExample = `
# 全场景Markdown响应示例

这是一个模拟的AI回复，用于测试Markdown渲染的各种场景。

## 1. 文本格式

- **粗体文本**: **这是一个加粗的词语**
- *斜体文本*: *这是一个倾斜的词语*
- ***粗斜体文本***: ***这是一个既加粗又倾斜的词语***
- ~~删除线文本~~: ~~这是一个带删除线的词语~~

## 2. 列表

### 无序列表
- 列表项 A
  - 嵌套列表项 A1
  - 嵌套列表项 A2
- 列表项 B
- 列表项 C

### 有序列表
1. 第一项
2. 第二项
   1. 嵌套第一项
   2. 嵌套第二项
3. 第三项

## 3. 代码块

这里是一个JavaScript代码块的示例，带有语法高亮：

\`\`\`javascript
function greet(name) {
  // 返回一个问候字符串
  return \`Hello, \${name}! This is a test of code block rendering.\`;
}

console.log(greet('Developer'));
\`\`\`

## 4. 表格

| 表头 1 | 表头 2 | 表头 3 |
| :--- | :---: | ---: |
| 左对齐 | 居中对齐 | 右对齐 |
| 单元格 | 单元格 | 单元格 |
| 单元格 | 单元格 | 单元格 |

## 5. 引用块

> "这是一个引用块。它通常用于引用他人的话语或突出显示特定段落。"
> > 这是一个嵌套的引用块。

## 6. 其他元素

---

这是一个水平分割线，用于分隔内容。

链接示例: [访问 Element Plus 官网](https://element-plus.org)
`;

// 设置默认值
const userAvatar = props.userAvatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
const assistantAvatar = props.assistantAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 聊天列表DOM引用
const chatListRef = ref<HTMLElement | null>(null)

// 编辑消息相关状态
const editingContent = ref<string>('')
const editingMessageId = ref<string | null>(null)

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
  editingMessageId.value = message.id
  editingContent.value = message.content
  emit('startEditing', message.id)
}

// 取消编辑
const cancelEdit = (message: Message) => {
  editingMessageId.value = null
  editingContent.value = ''
  emit('cancelEditing', message.id)
}

// 保存编辑
const saveEdit = (message: Message) => {
  if (editingContent.value.trim() === '') {
    ElMessage.warning('消息内容不能为空')
    return
  }
  
  emit('saveEditing', message.id, editingContent.value)
  editingMessageId.value = null
  editingContent.value = ''
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
  emit('command', command.id)
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
watch(() => props.messages, () => {
  if (props.autoScroll !== false) {
    scrollToBottom()
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
  padding: 40px 20px;
  text-align: center;
}

.chat-title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 12px;
  background: linear-gradient(90deg, #1a73e8, #8e44ad, #e74c3c, #f39c12, #2ecc71, #3498db);
  background-size: 400% 400%;
  color: transparent;
  -webkit-background-clip: text;
  background-clip: text;
  animation: gradient 10s ease infinite;
}

.chat-subtitle {
  font-size: 18px;
  color: #666;
  margin-bottom: 32px;
  background: linear-gradient(90deg, #1a73e8, #8e44ad, #e74c3c, #f39c12, #2ecc71, #3498db);
  background-size: 400% 400%;
  color: transparent;
  -webkit-background-clip: text;
  background-clip: text;
  animation: gradient 10s ease infinite;
  animation-delay: 0.5s;
}

@keyframes gradient {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.chat-header-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 16px;
  max-width: 800px;
}

.quick-command-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  background-color: #f5f7fa;
  cursor: pointer;
  transition: all 0.3s;
  width: 120px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.quick-command-btn:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.quick-command-btn .el-icon {
  font-size: 24px;
  margin-bottom: 8px;
  color: #409eff;
}

.quick-command-btn span {
  font-size: 14px;
  color: #333;
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

/* 消息操作按钮 */
.message-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
  opacity: 0;
  transition: opacity 0.2s;
}

.chat-content:hover .message-actions {
  opacity: 1;
}

.feedback-buttons {
  display: flex;
  gap: 4px;
}

.feedback-buttons .el-button.active {
  color: #409eff;
}

/* 停止生成按钮 */
.stop-button {
  display: flex;
  justify-content: center;
  margin-top: 8px;
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
}
</style> 