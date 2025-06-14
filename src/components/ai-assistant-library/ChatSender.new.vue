<template>
  <div class="chat-sender">
    <!-- 输入框区域 -->
    <div class="input-container">
      <!-- 文本输入区域 -->
      <div class="textarea-wrapper">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="inputRows"
          placeholder="输入消息，按Enter发送，Shift+Enter换行..."
          resize="none"
          @keydown.enter.exact.prevent="sendMessage"
          @keydown="handleKeydown"
          ref="inputRef"
          :disabled="isGenerating"
        />
        
        <!-- 命令提示区域 -->
        <div v-if="showCommandSuggestions && subCommands.length > 0" class="command-suggestions">
          <div 
            v-for="(cmd, index) in subCommands" 
            :key="cmd.id"
            :class="['command-item', { active: index === activeCommandIndex }]"
            @click.stop="selectCommand(cmd)"
            @mouseenter="activeCommandIndex = index"
          >
            <div class="command-icon">
              <el-icon><component :is="cmd.icon || 'ChatLineRound'" /></el-icon>
            </div>
            <div class="command-info">
              <div class="command-name">{{ cmd.name }}</div>
              <div class="command-desc">{{ cmd.description || '无描述' }}</div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 发送按钮区域 -->
      <div class="send-actions">
        <!-- 附件按钮 -->
        <el-tooltip content="上传附件" placement="top">
          <el-button
            class="action-btn"
            :disabled="isGenerating"
            @click="triggerFileUpload"
          >
            <el-icon><Paperclip /></el-icon>
          </el-button>
        </el-tooltip>
        
        <!-- 隐藏的文件上传输入 -->
        <input
          type="file"
          ref="fileInputRef"
          style="display: none"
          @change="handleFileChange"
          multiple
        />
        
        <!-- 发送/停止按钮 -->
        <el-button
          type="primary"
          :disabled="!canSend"
          @click="isGenerating ? stopGenerating() : sendMessage()"
        >
          <el-icon v-if="isGenerating"><VideoPause /></el-icon>
          <el-icon v-else><Position /></el-icon>
          {{ isGenerating ? '停止' : '发送' }}
        </el-button>
      </div>
    </div>
    
    <!-- 附件预览区域 -->
    <div v-if="attachments.length > 0" class="attachments-preview">
      <div 
        v-for="(file, index) in attachments" 
        :key="index"
        class="attachment-preview-item"
      >
        <div class="attachment-info">
          <el-icon><Document /></el-icon>
          <span class="attachment-name">{{ file.name }}</span>
          <span class="attachment-size">({{ formatFileSize(file.size) }})</span>
        </div>
        <el-button
          type="text"
          class="remove-attachment"
          @click="removeAttachment(index)"
        >
          <el-icon><Delete /></el-icon>
        </el-button>
      </div>
    </div>
    
    <!-- 底部工具栏 -->
    <div class="toolbar">
      <div class="left-tools">
        <!-- 模型选择器 -->
        <el-dropdown trigger="click" @command="handleModelChange" :disabled="isGenerating">
          <div class="model-selector">
            <el-icon><Connection /></el-icon>
            <span>{{ currentModelName }}</span>
            <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item 
                v-for="model in models" 
                :key="model.id" 
                :command="model.id"
                :class="{ 'is-active': model.id === currentModelId }"
              >
                {{ model.name }}
              </el-dropdown-item>
              <el-dropdown-item divided>
                <el-button type="text" @click.stop="openModelConfig">
                  <el-icon><Setting /></el-icon>
                  模型配置
                </el-button>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
      
      <div class="right-tools">
        <!-- 深度思考模式开关 -->
        <el-tooltip content="深度思考模式" placement="top">
          <el-switch
            v-model="isDeepThinkingMode"
            :disabled="isGenerating"
            @change="toggleDeepThinkingMode"
          />
        </el-tooltip>
        
        <!-- RAG知识库开关 -->
        <el-tooltip content="RAG知识库检索" placement="top">
          <el-switch
            v-model="isRAGMode"
            :disabled="isGenerating"
            @change="toggleRAGMode"
          />
        </el-tooltip>
        
        <!-- 更多菜单 -->
        <el-dropdown trigger="click" @command="handleCommand">
          <el-button type="text">
            <el-icon><MoreFilled /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="new">
                <el-icon><Plus /></el-icon>
                新建会话
              </el-dropdown-item>
              <el-dropdown-item command="history">
                <el-icon><List /></el-icon>
                历史会话
              </el-dropdown-item>
              <el-dropdown-item command="commands">
                <el-icon><Operation /></el-icon>
                命令管理
              </el-dropdown-item>
              <el-dropdown-item command="settings">
                <el-icon><Setting /></el-icon>
                主题和头像设置
              </el-dropdown-item>
              <el-dropdown-item divided command="clear">
                <el-icon><Delete /></el-icon>
                清空当前会话
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue';
import { ElMessageBox } from 'element-plus';
import {
  Paperclip,
  Position,
  VideoPause,
  Document,
  Delete,
  Connection,
  ArrowDown,
  Setting,
  MoreFilled,
  Plus,
  List,
  Operation
} from '@element-plus/icons-vue';
import type { AIModel } from '@/types/chat';
import type { CommandContext, SubCommand } from '@/types/command';
import { useCommands } from '@/hooks/useCommands';

// 定义组件属性
const props = withDefaults(defineProps<{
  isGenerating?: boolean
  isDeepThinkingMode?: boolean
  isRAGMode?: boolean
  currentModelId?: string
  models?: AIModel[]
}>(), {
  isGenerating: false,
  isDeepThinkingMode: false,
  isRAGMode: false,
  currentModelId: '',
  models: () => []
});

// 定义事件
const emit = defineEmits<{
  (e: 'send', content: string, attachments: File[]): void
  (e: 'stop'): void
  (e: 'modelChange', modelId: string): void
  (e: 'command', command: string): void
  (e: 'toggleDeepThinking'): void
  (e: 'toggleRAG'): void
  (e: 'openModelConfig'): void
}>();

// 使用命令系统
const {
  commands,
  subCommands,
  loading: commandsLoading,
  error: commandsError,
  fetchSubCommands,
  executeCommand,
  clearActiveCommand
} = useCommands();

// 输入消息
const inputMessage = ref('');
// 输入框引用
const inputRef = ref<any>(null);
// 文件输入引用
const fileInputRef = ref<HTMLInputElement | null>(null);
// 附件列表
const attachments = ref<File[]>([]);
// 是否显示命令提示
const showCommandSuggestions = ref(false);
// 当前激活的命令索引
const activeCommandIndex = ref(0);
// 深度思考模式
const isDeepThinkingMode = ref(props.isDeepThinkingMode || false);
// RAG模式
const isRAGMode = ref(props.isRAGMode || false);

// 计算属性：输入框行数
const inputRows = computed(() => {
  const lines = (inputMessage.value.match(/\n/g) || []).length + 1;
  return Math.min(Math.max(lines, 1), 5);
});

// 计算属性：是否可以发送消息
const canSend = computed(() => {
  return !props.isGenerating && (inputMessage.value.trim() !== '' || attachments.value.length > 0);
});

// 计算属性：当前模型名称
const currentModelName = computed(() => {
  const model = props.models.find(m => m.id === props.currentModelId);
  return model ? model.name : '默认模型';
});

// 发送消息
const sendMessage = async () => {
  if (!canSend.value) return;
  
  // 检查是否是命令
  const trimmedMessage = inputMessage.value.trim();
  if (trimmedMessage.startsWith('/') && subCommands.value.length > 0 && activeCommandIndex.value >= 0) {
    const selectedCommand = subCommands.value[activeCommandIndex.value];
    if (selectedCommand) {
      const context: CommandContext = {
        userId: 'current-user', // 这里应该从用户状态获取
        input: trimmedMessage.substring(selectedCommand.name.length + 2).trim(),
        modelId: props.currentModelId
      };
      
      const result = await executeCommand(selectedCommand.id, context);
      if (result.success) {
        emit('command', selectedCommand.id);
        resetInput();
      } else {
        ElMessageBox.alert(result.message || '命令执行失败', '错误', {
          type: 'error'
        });
      }
      return;
    }
  }
  
  // 普通消息发送
  emit('send', inputMessage.value, [...attachments.value]);
  resetInput();
};

// 停止生成
const stopGenerating = () => {
  emit('stop');
};

// 重置输入状态
const resetInput = () => {
  inputMessage.value = '';
  attachments.value = [];
  showCommandSuggestions.value = false;
  clearActiveCommand();
  
  // 聚焦输入框
  nextTick(() => {
    if (inputRef.value) {
      inputRef.value.focus();
    }
  });
};

// 处理键盘事件
const handleKeydown = (e: KeyboardEvent) => {
  // 如果显示命令提示，处理上下键选择
  if (showCommandSuggestions.value && subCommands.value.length > 0) {
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      activeCommandIndex.value = (activeCommandIndex.value + 1) % subCommands.value.length;
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      activeCommandIndex.value = (activeCommandIndex.value - 1 + subCommands.value.length) % subCommands.value.length;
    } else if (e.key === 'Tab' || e.key === 'Enter') {
      e.preventDefault();
      if (activeCommandIndex.value >= 0 && activeCommandIndex.value < subCommands.value.length) {
        selectCommand(subCommands.value[activeCommandIndex.value]);
      }
    } else if (e.key === 'Escape') {
      e.preventDefault();
      showCommandSuggestions.value = false;
      clearActiveCommand();
    }
  } else {
    // 正常输入模式下的快捷键
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  }
};

// 触发文件上传
const triggerFileUpload = () => {
  if (fileInputRef.value) {
    fileInputRef.value.click();
  }
};

// 处理文件选择
const handleFileChange = (e: Event) => {
  const input = e.target as HTMLInputElement;
  if (input.files && input.files.length > 0) {
    // 添加到附件列表
    for (let i = 0; i < input.files.length; i++) {
      attachments.value.push(input.files[i]);
    }
    
    // 重置文件输入，以便可以再次选择相同的文件
    input.value = '';
  }
};

// 移除附件
const removeAttachment = (index: number) => {
  attachments.value.splice(index, 1);
};

// 格式化文件大小
const formatFileSize = (bytes: number): string => {
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
};

// 处理模型切换
const handleModelChange = (modelId: string) => {
  emit('modelChange', modelId);
};

// 处理菜单命令
const handleCommand = (command: string) => {
  if (command === 'clear') {
    ElMessageBox.confirm('确定要清空当前会话吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      emit('command', 'clear');
    }).catch(() => {});
  } else {
    emit('command', command);
  }
};

// 打开模型配置
const openModelConfig = () => {
  emit('openModelConfig');
};

// 切换深度思考模式
const toggleDeepThinkingMode = () => {
  emit('toggleDeepThinking');
};

// 切换RAG模式
const toggleRAGMode = () => {
  emit('toggleRAG');
};

// 选择命令
const selectCommand = (command: SubCommand) => {
  inputMessage.value = `/${command.name} `;
  showCommandSuggestions.value = false;
  
  // 聚焦输入框并将光标移到末尾
  nextTick(() => {
    if (inputRef.value) {
      inputRef.value.focus();
      
      const textarea = inputRef.value.$el.querySelector('textarea');
      if (textarea) {
        textarea.selectionStart = textarea.selectionEnd = inputMessage.value.length;
      }
    }
  });
};

// 监听输入变化，处理命令提示
watch(inputMessage, async (newValue) => {
  if (newValue.startsWith('/')) {
    const commandText = newValue.substring(1).toLowerCase().trim();
    
    // 获取命令列表
    if (commandText === '') {
      showCommandSuggestions.value = true;
      activeCommandIndex.value = 0;
      return;
    }
    
    // 查找匹配的命令
    const matchedCommand = commands.value.find(cmd => 
      cmd.name.toLowerCase().startsWith(commandText) ||
      cmd.description.toLowerCase().includes(commandText)
    );
    
    if (matchedCommand) {
      // 获取子命令
      const context: CommandContext = {
        userId: 'current-user', // 这里应该从用户状态获取
        input: commandText
      };
      
      await fetchSubCommands(matchedCommand.id, context);
      showCommandSuggestions.value = subCommands.value.length > 0;
      activeCommandIndex.value = 0;
    } else {
      showCommandSuggestions.value = false;
      clearActiveCommand();
    }
  } else {
    showCommandSuggestions.value = false;
    clearActiveCommand();
  }
});

// 同步外部属性变化
watch(() => props.isDeepThinkingMode, (newValue) => {
  isDeepThinkingMode.value = newValue || false;
});

watch(() => props.isRAGMode, (newValue) => {
  isRAGMode.value = newValue || false;
});

// 添加点击外部关闭命令面板的处理函数
const handleClickOutside = (event: MouseEvent) => {
  if (showCommandSuggestions.value) {
    const commandPanel = document.querySelector('.command-suggestions');
    if (commandPanel && !commandPanel.contains(event.target as Node)) {
      showCommandSuggestions.value = false;
      clearActiveCommand();
    }
  }
};

// 组件挂载后聚焦输入框
onMounted(() => {
  nextTick(() => {
    if (inputRef.value) {
      inputRef.value.focus();
    }
  });
  
  // 添加点击事件监听
  document.addEventListener('click', handleClickOutside);
});

// 组件卸载时移除事件监听
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
});
</script>

<style scoped>
.chat-sender {
  width: 100%;
  background-color: #fff;
  border-top: 1px solid #ebeef5;
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-container {
  display: flex;
  gap: 12px;
  width: 100%;
}

.textarea-wrapper {
  flex: 1;
  position: relative;
}

.send-actions {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.action-btn {
  padding: 9px;
  height: 40px;
  width: 40px;
}

.attachments-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.attachment-preview-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 8px;
  background-color: #f5f7fa;
  border-radius: 4px;
  border: 1px solid #ebeef5;
  max-width: 250px;
}

.attachment-info {
  display: flex;
  align-items: center;
  gap: 4px;
  overflow: hidden;
}

.attachment-name {
  font-size: 12px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 150px;
}

.attachment-size {
  font-size: 12px;
  color: #909399;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.left-tools, .right-tools {
  display: flex;
  align-items: center;
  gap: 12px;
}

.model-selector {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.model-selector:hover {
  background-color: #f5f7fa;
}

.dropdown-icon {
  margin-left: 4px;
  font-size: 12px;
}

.command-suggestions {
  position: absolute;
  bottom: 100%;
  left: 0;
  right: 0;
  background-color: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  max-height: 300px;
  overflow-y: auto;
  z-index: 10;
  margin-bottom: 4px;
}

.command-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.command-item:hover,
.command-item.active {
  background-color: #f5f7fa;
}

.command-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 4px;
  background-color: var(--el-color-primary-light-8);
  margin-right: 12px;
  color: var(--el-color-primary);
}

.command-info {
  flex: 1;
  min-width: 0;
}

.command-name {
  font-weight: 500;
  margin-bottom: 2px;
}

.command-desc {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .chat-sender {
    background-color: #1e1e1e;
    border-top: 1px solid #333;
  }
  
  .attachment-preview-item {
    background-color: #2d2d2d;
    border: 1px solid #444;
  }
  
  .attachment-name {
    color: #ddd;
  }
  
  .attachment-size {
    color: #aaa;
  }
  
  .model-selector:hover {
    background-color: #2d2d2d;
  }
  
  .command-suggestions {
    background-color: #1e1e1e;
    border: 1px solid #444;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.3);
  }
  
  .command-item:hover,
  .command-item.active {
    background-color: #2d2d2d;
  }
}
</style> 