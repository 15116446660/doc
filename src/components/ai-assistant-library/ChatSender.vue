<template>
  <div class="chat-sender-container">
    <!-- 顶部工具栏 -->
    <div class="top-toolbar">
      <div class="toolbar-left">
        <ModelSelector
          :current-model-id="currentModelId"
          :models="models"
          :disabled="isGenerating"
          @select-model="handleModelChange"
          @open-config="openModelConfig"
        />
        
        <!-- 命令管理 -->
        <!-- <el-tooltip content="命令管理" placement="top">
          <el-button :icon="Operation" circle @click="handleOpenCommandManagement" />
        </el-tooltip> -->
        <CommandSelector 
          :commands="commands"
          @view-sub-commands="cmd => emit('view-sub-commands', cmd)"
          @open-command-management="emit('open-command-management')"
        />
        
        <!-- 上传附件 -->
        <el-tooltip content="上传附件" placement="top">
          <el-button :icon="Paperclip" circle @click="triggerFileUpload" />
        </el-tooltip>
        <input type="file" ref="fileInputRef" style="display: none" @change="handleFileChange" multiple />
        
        <!-- 全文引用开关 -->
        <el-tooltip content="全文引用" placement="top">
          <el-button 
            :icon="Link" 
            circle 
            :class="{ 'is-active': isFullTextReferenceMode }"
            @click="toggleFullTextReferenceMode" 
          />
        </el-tooltip>
      </div>
      <div class="toolbar-right">
        <!-- 新建会话 -->
        <el-tooltip content="新建会话" placement="top">
          <el-button :icon="Plus" circle @click="handleNewChat" />
        </el-tooltip>
        <!-- 历史会话 -->
        <el-tooltip content="历史会话" placement="top">
          <el-button :icon="List" circle @click="handleOpenHistory" />
        </el-tooltip>
        <!-- 清空当前会话 -->
        <el-tooltip content="清空当前会话" placement="top">
          <el-button :icon="Delete" circle @click="handleClearChat" />
        </el-tooltip>
      </div>
    </div>

    <!-- 输入框容器 -->
    <div class="input-area-container" :class="{ 'is-active': isInputActive }">
      <!-- 已选中文本区域 -->
      <div v-if="selectedText" class="selected-text-wrapper">
        <div class="selected-text-content">
          <div class="label">来自您选择的文本</div>
          <div class="text" :title="selectedText">{{ truncatedText }}</div>
          <el-button class="close-btn" :icon="Close" circle @click="clearSelectedText" />
        </div>
        <div class="quick-commands-bar">
           <el-button 
            v-for="cmd in visibleQuickCommands" 
            :key="cmd.id"
            class="quick-command-btn"
            size="small"
            @click="emit('command', cmd)"
          >
            {{ cmd.name }}
          </el-button>

          <el-dropdown v-if="hiddenQuickCommands.length > 0" trigger="click">
            <el-button class="quick-command-btn" size="small">
              更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item 
                  v-for="cmd in hiddenQuickCommands" 
                  :key="cmd.id"
                  @click="emit('command', cmd)"
                >
                  {{ cmd.name }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 附件预览区域 -->
      <div v-if="attachedFiles.length > 0" class="attachments-preview-wrapper">
        <div 
          v-for="(file, index) in attachedFiles" 
          :key="file.name + index" 
          class="attachment-item"
          :title="file.name"
        >
          <el-icon class="attachment-icon"><Document /></el-icon>
          <span class="attachment-name">{{ file.name }}</span>
          <el-icon class="remove-btn" @click.stop="removeFile(index)"><CircleClose /></el-icon>
        </div>
      </div>

      <!-- 命令 & 子命令面板容器 -->
      <div v-if="showCommandSuggestions" class="command-panels-wrapper">
        <!-- 命令建议面板 -->
        <div class="command-suggestions-panel">
          <div class="panel-title">
            <el-icon><Operation /></el-icon>
            <span>可用命令</span>
          </div>
          <div class="command-list-wrapper">
            <div
              v-for="(cmd, index) in suggestionCommands"
              :key="cmd.id"
              :class="['command-item', { 
                'active': index === activeCommandIndex,
                'selected': cmd.id === selectedCommandId
              }]"
              @click.stop="selectCommand(cmd)"
              @mouseenter="activeCommandIndex = index"
            >
              <div class="command-icon">
                <el-icon><component :is="cmd.icon || 'Operation'" /></el-icon>
              </div>
              <div class="command-info">
                <div class="command-name">{{ cmd.name }}</div>
                <div class="command-desc">{{ cmd.description }}</div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 子命令面板 -->
        <div v-if="selectedCommandId && activeSubCommand" class="sub-command-panel">
          <div class="panel-title">
            <el-icon><component :is="activeSubCommand.icon || 'Operation'" /></el-icon>
            <span>{{ activeSubCommand.name }}</span>
          </div>
          <div v-if="isLoadingSubCommands" class="loading-spinner">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载中...</span>
          </div>
          <div v-else class="sub-command-list">
            <div 
              v-for="subCmd in currentSubCommands" 
              :key="subCmd.id"
              class="sub-command-item"
              @click="selectSubCommand(subCmd)"
            >
              <div class="sub-command-icon">
                <el-icon><component :is="subCmd.icon || 'Operation'" /></el-icon>
              </div>
              <div class="sub-command-info">
                <div class="sub-command-name">{{ subCmd.name }}</div>
                <div class="sub-command-desc">{{ subCmd.description }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 文本输入框 -->
      <el-input
        v-model="inputMessage"
        type="textarea"
        :rows="inputRows"
        placeholder="问任何问题, @ 模型, / 提示"
        resize="none"
        @keydown.enter.exact.prevent="handleSendMessage"
        @keydown="handleKeydown"
        @input="handleInput"
        @focus="isInputActive = true"
        @blur="isInputActive = false"
        :disabled="isGenerating"
        class="main-textarea"
        ref="inputRef"
      />

      <!-- 底部工具栏 -->
      <div class="bottom-toolbar">
        <div class="toolbar-left">
          <el-tooltip content="深度思考" placement="top">
            <el-button 
              class="mode-btn"
              :class="{ 'is-active': isDeepThinkingMode }"
              @click="toggleDeepThinkingMode"
              size="small"
            >
              <el-icon><Cpu /></el-icon>
              思考 (R1)
            </el-button>
          </el-tooltip>
          
          <!-- 命令选择器 -->
          <!-- <CommandSelector 
            :commands="commands"
            @view-sub-commands="cmd => emit('view-sub-commands', cmd)"
            @open-command-management="emit('open-command-management')"
          /> -->
          
          <el-popover
            placement="top-start"
            :width="300"
            trigger="click"
            v-model:visible="isKnowledgeBasePanelVisible"
            popper-class="beautiful-popper"
            :show-arrow="false"
          >
            <template #reference>
              <div class="kb-trigger-wrapper">
                <el-tooltip content="知识库检索" placement="top" v-if="!props.currentKnowledgeBaseId">
                  <el-button 
                    class="mode-btn"
                    :class="{ 'is-active': props.isRAGMode }"
                    @click.stop="handleKnowledgeBaseButtonClick"
                    size="small"
                  >
                    <el-icon><DataLine /></el-icon>
                    搜索
                  </el-button>
                </el-tooltip>

                <div class="selected-kb-display" v-else @click.stop="handleKnowledgeBaseButtonClick">
                   <el-icon class="kb-icon"><component :is="iconMap[selectedKnowledgeBase?.icon || 'Document'] || Document" /></el-icon>
                   <span class="kb-name">{{ selectedKnowledgeBase?.name || props.currentKnowledgeBaseId }}</span>
                   <el-icon class="clear-icon" @click.stop="clearSelectedKnowledgeBase"><CircleClose /></el-icon>
                </div>
              </div>
            </template>
            <div class="knowledge-base-panel">
              <div class="panel-header">选择知识库</div>
              <div class="panel-body">
                <div
                  v-for="kb in knowledgeBases"
                  :key="kb.id"
                  class="kb-item"
                  :class="{ active: props.currentKnowledgeBaseId === kb.id }"
                  @click="selectKnowledgeBase(kb.id)"
                >
                  <el-icon class="kb-icon"><component :is="iconMap[kb.icon] || Document" /></el-icon>
                  <div class="kb-info">
                    <div class="kb-name">{{ kb.name }}</div>
                    <div class="kb-desc">{{ kb.description }}</div>
                  </div>
                </div>
              </div>
            </div>
          </el-popover>
        </div>
        <div class="toolbar-right">
          <el-button
            type="primary"
            circle
            :disabled="!canSend"
            :icon="isGenerating ? VideoPause : Position"
            @click="isGenerating ? emit('stop') : handleSendMessage()"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue';
import { ElMessageBox } from 'element-plus';
import {
  Cpu,
  Paperclip,
  Position,
  VideoPause,
  Delete,
  ArrowDown,
  Plus,
  List,
  Operation,
  Close,
  DataLine,
  Document,
  FolderOpened,
  DataAnalysis,
  CircleClose,
  Loading,
  Link,
} from '@element-plus/icons-vue';
import type { AIModel, SubCommand as SubCommandType, Command as BaseCommand, KnowledgeBase, QuickCommand } from '@/types/chat';
import { usePromptCommands } from '@/components/ai-assistant-library/hooks/usePromptCommands';
import { getSubCommands, getQuickCommands } from '@/api/command';
import { getKnowledgeBases } from '@/api/knowledgeBase';
import ModelSelector from './ModelSelector.vue';
import CommandSelector from './CommandSelector.vue';

// Icon mapping to resolve linter errors and for dynamic rendering
const iconMap: Record<string, any> = {
  Document,
  FolderOpened,
  DataAnalysis,
  CircleClose,
};

// #region --- Props & Emits Definition ---
const props = defineProps<{
  currentModelId: string;
  models: AIModel[];
  isGenerating?: boolean;
  isDeepThinkingMode?: boolean;
  isRAGMode?: boolean;
  isFullTextReferenceMode?: boolean;
  commands: BaseCommand[];
  currentKnowledgeBaseId?: string | null;
}>();

const emit = defineEmits<{
  (e: 'send', message: string, attachments?: Array<any>, commandId?: string): void
  (e: 'stop'): void
  (e: 'modelChange', modelId: string): void
  (e: 'command', command: BaseCommand): void
  (e: 'toggleDeepThinking'): void
  (e: 'toggleRAG'): void
  (e: 'toggleFullTextReference'): void
  (e: 'openModelConfig'): void
  (e: 'selectKnowledgeBase', knowledgeBaseId: string): void
  (e: 'clearSelectedKnowledgeBase'): void
  (e: 'new-chat'): void
  (e: 'open-history'): void
  (e: 'clear-chat'): void
  (e: 'open-command-management'): void
  (e: 'view-sub-commands', commandId: string): void
}>();
// #endregion

// #region --- State Management ---
const inputMessage = ref('');
const fileInputRef = ref<HTMLInputElement | null>(null);
const inputRef = ref<any>(null);
const attachedFiles = ref<File[]>([]);
const isInputActive = ref(false);
const selectedText = ref('');

const { 
  commands, 
  loadCommands,
} = usePromptCommands();

const showCommandSuggestions = ref(false);
const activeCommandIndex = ref(0);
const selectedCommandId = ref<string | null>(null);

const activeSubCommand = ref<BaseCommand | null>(null);
const currentSubCommands = ref<SubCommandType[]>([]);
const isLoadingSubCommands = ref(false);

const isKnowledgeBasePanelVisible = ref(false);
const knowledgeBases = ref<KnowledgeBase[]>([]);

const quickCommands = ref<QuickCommand[]>([]);

const maxVisibleQuickCommands = 5;
// #endregion

// #region --- Computed Properties ---
const canSend = computed(() => {
  return (
    !props.isGenerating && (inputMessage.value.trim().length > 0 || attachedFiles.value.length > 0)
  );
});

const inputRows = computed(() => {
  const lines = inputMessage.value.split('\n').length;
  return Math.min(Math.max(1, lines), 5);
});

const truncatedText = computed(() => {
  const maxLength = 100;
  if (selectedText.value.length > maxLength) {
    return selectedText.value.slice(0, maxLength) + '...';
  }
  return selectedText.value;
});

const visibleQuickCommands = computed(() => quickCommands.value.slice(0, maxVisibleQuickCommands));
const hiddenQuickCommands = computed(() => quickCommands.value.slice(maxVisibleQuickCommands));

const suggestionCommands = computed<Array<BaseCommand>>(() => {
  return commands.value;
});

const selectedKnowledgeBase = computed(() => {
  return knowledgeBases.value.find(kb => kb.id === props.currentKnowledgeBaseId) || null;
});
// #endregion

// #region --- Event Handlers & Methods ---
const handleModelChange = (modelId: string) => emit('modelChange', modelId);
const openModelConfig = () => emit('openModelConfig');
const triggerFileUpload = () => fileInputRef.value?.click();

const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files) {
    attachedFiles.value.push(...Array.from(target.files));
  }
  // 清空，以便再次选择相同文件时仍能触发 change 事件
  target.value = '';
};

const removeFile = (index: number) => {
  attachedFiles.value.splice(index, 1);
};

const handleSendMessage = () => {
  if (!canSend.value) return;

  let messageToSend = inputMessage.value;
  
  // 检查是否是命令格式（以/开头，后面跟着命令名称和可能的空格及参数）
  const commandRegex = /^\/(\S+)(?:\s+(.*))?$/;
  const match = messageToSend.match(commandRegex);
  
  if (match) {
    const commandName = match[1];
    const commandInput = match[2] || '';
    console.log(`检测到命令: /${commandName}, 参数: "${commandInput}"`);
    
    // 查找匹配的命令
    const command = props.commands.find(cmd => cmd.name === commandName);
    
    if (command) {
      console.log(`找到命令: ${command.name}, 提示词: ${command.prompt}`);
      
      // 如果命令有子命令，则通过emit('command')处理
      if (command.hasSubCommands) {
        console.log(`命令 ${command.name} 有子命令，通过command事件处理`);
        emit('command', command);
        
        // 清理工作
        inputMessage.value = '';
        attachedFiles.value = [];
        clearSelectedText();
        return;
      }
      
      // 替换命令为提示词
      if (command.prompt) {
        messageToSend = command.prompt.replace(/{input}/g, commandInput);
        console.log(`替换后的消息: ${messageToSend}`);
      }
      
      // 发送消息时附带命令ID
      emit('send', messageToSend, attachedFiles.value, command.id);
    } else {
      console.warn(`未找到命令: ${commandName}`);
      // 未找到命令时，仍然发送原始消息
      emit('send', messageToSend, attachedFiles.value);
    }
  } else {
    // 不是命令格式，直接发送
    emit('send', messageToSend, attachedFiles.value);
  }

  // 清理工作
  inputMessage.value = '';
  attachedFiles.value = [];
  clearSelectedText();
};

const toggleDeepThinkingMode = () => emit('toggleDeepThinking');

const toggleFullTextReferenceMode = () => emit('toggleFullTextReference');

const handleKnowledgeBaseButtonClick = () => {
  isKnowledgeBasePanelVisible.value = true;
};

const selectKnowledgeBase = (kbId: string) => {
  emit('selectKnowledgeBase', kbId);
  isKnowledgeBasePanelVisible.value = false;
};

const clearSelectedKnowledgeBase = () => {
  emit('clearSelectedKnowledgeBase');
};

const clearSelectedText = () => {
  selectedText.value = '';
};

const handleInput = (value: string) => {
  const trimmedValue = value.trim();
  
  // 检查是否是已经选择了命令并输入了空格的情况
  const commandPlusSpacePattern = /^\/\S+\s+/;
  if (commandPlusSpacePattern.test(value)) {
    // 已选择命令且有空格，不显示命令面板
    showCommandSuggestions.value = false;
    return;
  }
  
  if (trimmedValue.startsWith('/')) {
    const exactMatch = suggestionCommands.value.find(c => `/${c.name}` === trimmedValue);
    if (exactMatch) {
      selectCommand(exactMatch);
    } else {
      showCommandSuggestions.value = true;
      selectedCommandId.value = null;
      activeSubCommand.value = null;
    }
  } else {
    showCommandSuggestions.value = false;
    selectedCommandId.value = null;
    activeSubCommand.value = null;
  }
};

const handleKeydown = (e: KeyboardEvent) => {
  const text = inputMessage.value.trim();
  if (e.key === '/' && text === '') {
    showCommandSuggestions.value = true;
  }

  if (showCommandSuggestions.value && suggestionCommands.value.length > 0) {
    switch (e.key) {
      case 'ArrowDown':
        e.preventDefault();
        activeCommandIndex.value = (activeCommandIndex.value + 1) % suggestionCommands.value.length;
        break;
      case 'ArrowUp':
        e.preventDefault();
        activeCommandIndex.value = (activeCommandIndex.value - 1 + suggestionCommands.value.length) % suggestionCommands.value.length;
        break;
      case 'Enter':
      case 'Tab':
        if (text.startsWith('/')) {
          e.preventDefault();
          selectCommand(suggestionCommands.value[activeCommandIndex.value]);
        }
        break;
      case 'Escape':
        e.preventDefault();
        showCommandSuggestions.value = false;
        selectedCommandId.value = null;
        activeSubCommand.value = null;
        break;
    }
  }
};

// 聚焦输入框并将光标定位到最后
const focusInputAndSetCursorToEnd = () => {
  // 使用nextTick确保DOM已更新
  nextTick(() => {
    if (inputRef.value) {
      try {
        // 先尝试调用Element Plus组件的focus方法
        inputRef.value.focus();
      } catch (e) {
        console.log('直接调用focus方法失败，将通过DOM元素聚焦');
      }
      
      // 使用Element Plus的方法获取真实的DOM元素
      const textarea = inputRef.value.$el.querySelector('textarea');
      if (textarea) {
        // 聚焦输入框
        textarea.focus();
        // 将光标定位到最后
        const length = inputMessage.value.length;
        textarea.setSelectionRange(length, length);
        
        // 确保文本区域确实获得了焦点
        if (document.activeElement !== textarea) {
          setTimeout(() => {
            textarea.focus();
            textarea.setSelectionRange(length, length);
          }, 50);
        }
      }
    }
  });
};

const selectCommand = async (cmd: BaseCommand) => {
  activeCommandIndex.value = suggestionCommands.value.findIndex(c => c.id === cmd.id);

  if (!cmd.hasSubCommands) {
    inputMessage.value = `/${cmd.name} `;
    showCommandSuggestions.value = false;
    selectedCommandId.value = null;
    activeSubCommand.value = null;
    // 聚焦输入框并将光标定位到最后
    focusInputAndSetCursorToEnd();
    return;
  }

  if (selectedCommandId.value === cmd.id) {
    return;
  }

  selectedCommandId.value = cmd.id;
  activeSubCommand.value = cmd;
  currentSubCommands.value = [];
  isLoadingSubCommands.value = true;
  
  if (cmd.subCommands && cmd.subCommands.length > 0) {
    currentSubCommands.value = cmd.subCommands;
    isLoadingSubCommands.value = false;
  } else {
    try {
      currentSubCommands.value = await getSubCommands(cmd.id);
    } catch (error) {
      console.error('获取子命令失败:', error);
      ElMessageBox.alert('获取子命令失败，请稍后再试。', '错误', { type: 'error' });
      selectedCommandId.value = null;
      activeSubCommand.value = null;
    } finally {
      isLoadingSubCommands.value = false;
    }
  }
};

const selectSubCommand = (subCmd: SubCommandType) => {
  // 二级命令选择后，只显示"/二级命令名称 "，不需要显示一级命令名称
  inputMessage.value = `/${subCmd.name} `;
  showCommandSuggestions.value = false;
  activeSubCommand.value = null;
  selectedCommandId.value = null;
  // 聚焦输入框并将光标定位到最后
  focusInputAndSetCursorToEnd();
};

const handleMouseUp = () => {
  const text = window.getSelection()?.toString().trim() ?? '';
  if (text && text.length > 10) {
    selectedText.value = text;
  }
};

const handleNewChat = () => {
  emit('new-chat');
};

const handleOpenHistory = () => {
  emit('open-history');
};

const handleClearChat = () => {
  emit('clear-chat');
};

// #endregion

// #region --- Lifecycle Hooks ---
onMounted(() => {
  loadCommands();

  getKnowledgeBases().then(data => {
    knowledgeBases.value = data;
  });

  getQuickCommands().then(data => {
    quickCommands.value = data;
  });

  document.addEventListener('mouseup', handleMouseUp);
});

onUnmounted(() => {
  document.removeEventListener('mouseup', handleMouseUp);
});
// #endregion
</script>

<style lang="scss" scoped>
.chat-sender-container {
  padding: 8px;
  background-color: var(--el-bg-color-page);
}

.top-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding: 0 4px;

  .toolbar-left, .toolbar-right {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .model-selector {
    display: flex;
    align-items: center;
    padding: 5px 12px;
    border-radius: 8px;
    cursor: pointer;
    background-color: var(--el-bg-color);
    border: 1px solid var(--el-border-color);
    transition: all 0.3s ease;
    height: 32px;
    
    &:hover {
      border-color: var(--el-color-primary);
      background-color: var(--el-color-primary-light-9);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    }

    .model-logo, .model-logo-default {
      width: 18px;
      height: 18px;
      margin-right: 8px;
      border-radius: 4px;
      object-fit: contain;
    }

    .model-logo-default {
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: var(--el-fill-color-light);
      color: var(--el-text-color-secondary);
    }

    span {
      font-size: 14px;
      color: var(--el-text-color-regular);
      font-weight: 500;
    }

    .dropdown-icon {
      margin-left: 8px;
      margin-right: 0;
      font-size: 12px;
      color: var(--el-text-color-secondary);
      transition: transform 0.3s ease;
    }

    &:hover .dropdown-icon {
      transform: rotate(180deg);
      color: var(--el-color-primary);
    }
  }

  .el-button--circle {
    width: 32px;
    height: 32px;
  }
}

.el-button.is-active {
  color: var(--el-color-primary);
  border-color: var(--el-color-primary-light-3);
  background-color: var(--el-color-primary-light-9);
}

.input-area-container {
  position: relative;
  background-color: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 12px;
  transition: border-color 0.2s, box-shadow 0.2s;
  padding: 8px 12px;

  &.is-active {
    border-color: var(--el-color-primary);
    box-shadow: 0 0 0 1px var(--el-color-primary);
  }
}

.selected-text-wrapper {
  background-color: var(--el-bg-color-page);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  margin-bottom: 8px;
  animation: fadeIn 0.3s ease-out;

  .selected-text-content {
    padding: 8px 12px;
    position: relative;
    .label {
      font-size: 12px;
      color: var(--el-text-color-secondary);
      margin-bottom: 4px;
    }
    .text {
      font-size: 14px;
      color: var(--el-text-color-primary);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .close-btn {
      position: absolute;
      top: 4px;
      right: 4px;
      width: 20px;
      height: 20px;
    }
  }

  .quick-commands-bar {
    border-top: 1px solid var(--el-border-color-lighter);
    padding: 8px 12px;
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .quick-command-btn {
    background-color: var(--el-bg-color);
    border-color: var(--el-border-color);
    color: var(--el-text-color-regular);
    
    &:hover {
      background-color: var(--el-color-primary-light-9);
      border-color: var(--el-color-primary-light-5);
      color: var(--el-color-primary);
    }
  }
}

.attachments-preview-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 0 4px 8px;
  animation: fadeIn 0.3s ease-out;
}

.attachment-item {
  display: flex;
  align-items: center;
  background-color: var(--el-bg-color-page);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  padding: 4px 8px;
  font-size: 13px;
  max-width: 200px;
  
  .attachment-icon {
    margin-right: 6px;
    color: var(--el-text-color-secondary);
  }

  .attachment-name {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    color: var(--el-text-color-regular);
  }

  .remove-btn {
    margin-left: 8px;
    cursor: pointer;
    color: var(--el-text-color-placeholder);
    transition: color 0.2s;
    
    &:hover {
      color: var(--el-color-danger);
    }
  }
}

.main-textarea {
  :deep(.el-textarea__inner) {
    box-shadow: none;
    background-color: transparent;
    padding: 6px 0;
    border: none;
    font-size: 15px;
  }
}

.bottom-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;

  .toolbar-left {
    display: flex;
    gap: 8px;
  }
  
  .mode-btn {
    border-radius: 16px;
    background-color: var(--el-bg-color-page);
    border: 1px solid var(--el-border-color-lighter);

    &.is-active {
      background-color: var(--el-color-primary-light-9);
      color: var(--el-color-primary);
      border-color: var(--el-color-primary-light-8);
    }

    .el-icon {
      margin-right: 4px;
    }
  }
}

.command-panels-wrapper {
  position: absolute;
  bottom: calc(100% + 4px);
  left: 0;
  display: flex;
  gap: 4px;
  z-index: 10;
}

.command-suggestions-panel {
  width: 320px;
  flex-shrink: 0;
  background: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-extra-light);
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  max-height: 300px;
  display: flex;
  flex-direction: column;

  .command-list-wrapper {
    overflow-y: auto;
    padding: 4px;
  }

  .command-item {
    display: flex;
    align-items: center;
    padding: 8px 12px;
    border-radius: 6px;
    cursor: pointer;
    transition: background-color 0.2s;

    &.active, &:hover {
      background-color: var(--el-fill-color-light);
    }

    &.selected {
      background-color: var(--el-color-primary-light-9);
      .command-name, .command-icon .el-icon {
         color: var(--el-color-primary);
      }
    }
    
    .command-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 32px;
      height: 32px;
      background-color: var(--el-color-primary-light-9);
      color: var(--el-color-primary);
      border-radius: 4px;
      margin-right: 12px;
    }

    .command-info {
      .command-name {
        font-weight: 500;
        color: var(--el-text-color-primary);
      }
      .command-desc {
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }
    }
  }
}

.panel-title {
  padding: 8px 12px;
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-secondary);
  flex-shrink: 0;
}

:deep(.el-dropdown-menu) {
  padding: 0 !important;
}

.model-list-wrapper {
  max-height: 250px;
  overflow-y: auto;
  padding: 6px;
}

.divider {
  margin: 4px 0;
  height: 1px;
  background-color: var(--el-border-color-lighter);
}

.config-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  font-size: 14px;
  color: var(--el-text-color-regular);
  cursor: pointer;
  transition: background-color 0.3s;
  
  &:hover {
    background-color: var(--el-dropdown-menuItem-hover-fill);
    color: var(--el-dropdown-menuItem-hover-color);
  }
  
  .el-icon {
    margin-right: 8px;
    font-size: 16px;
  }
}

.kb-trigger-wrapper {
  display: inline-block;
}

.selected-kb-display {
  display: flex;
  align-items: center;
  gap: 6px;
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  border: 1px solid var(--el-color-primary-light-8);
  border-radius: 16px;
  padding: 0 10px;
  height: 24px;
  font-size: 12px;
  cursor: pointer;

  .kb-name {
    font-weight: 500;
  }

  .clear-icon {
    color: var(--el-text-color-secondary);
    transition: color 0.2s;
    &:hover {
      color: var(--el-color-primary);
    }
  }
}

.knowledge-base-panel {
  .panel-header {
    font-weight: 500;
    padding-bottom: 8px;
    margin-bottom: 8px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .panel-body {
    max-height: 200px;
    overflow-y: auto;
  }

  .kb-item {
    display: flex;
    align-items: center;
    padding: 8px;
    border-radius: 6px;
    cursor: pointer;
    transition: background-color 0.2s;

    &:hover {
      background-color: var(--el-fill-color-light);
    }

    &.active {
      background-color: var(--el-color-primary-light-9);
      .kb-name, .kb-icon {
        color: var(--el-color-primary);
      }
    }
  }

  .kb-icon {
    font-size: 18px;
    margin-right: 10px;
    color: var(--el-text-color-secondary);
  }

  .kb-info {
    .kb-name {
      font-weight: 500;
      font-size: 14px;
    }
    .kb-desc {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }
}

.command-panel {
  .panel-header {
    font-weight: 500;
    padding-bottom: 8px;
    margin-bottom: 8px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .panel-body {
    max-height: 300px;
    overflow-y: auto;
  }

  .command-panel-item {
    display: flex;
    align-items: flex-start;
    padding: 10px;
    border-radius: 6px;
    cursor: pointer;
    transition: background-color 0.2s;
    margin-bottom: 6px;

    &:hover {
      background-color: var(--el-fill-color-light);
    }
  }

  .cmd-icon {
    font-size: 18px;
    margin-right: 10px;
    margin-top: 3px;
    color: var(--el-color-primary);
  }

  .cmd-info {
    flex: 1;
    position: relative;
  }

  .cmd-name {
    font-weight: 500;
    font-size: 14px;
    margin-bottom: 4px;
  }

  .cmd-desc {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin-bottom: 4px;
  }

  .sub-cmd-tag {
    margin-top: 4px;
    font-size: 10px;
    padding: 0 6px;
    height: 18px;
    line-height: 16px;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.model-dropdown-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  
  .model-logo, .model-logo-default {
    width: 18px;
    height: 18px;
    margin-right: 8px;
    border-radius: 4px;
    object-fit: contain;
  }

  .model-logo-default {
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: var(--el-fill-color-light);
    color: var(--el-text-color-secondary);
  }
}

.sub-command-panel {
  width: 320px;
  flex-shrink: 0;
  background: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-extra-light);
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  max-height: 300px;
  padding: 8px;
  display: flex;
  flex-direction: column;

  .loading-spinner {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 8px;
    padding: 20px;
    color: var(--el-text-color-secondary);
  }

  .sub-command-list {
    max-height: 250px;
    overflow-y: auto;
  }

  .sub-command-item {
    display: flex;
    align-items: center;
    padding: 8px 12px;
    border-radius: 6px;
    cursor: pointer;
    transition: background-color 0.2s;

    &:hover {
      background-color: var(--el-fill-color-light);
    }
  }

  .sub-command-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    background-color: var(--el-color-primary-light-9);
    color: var(--el-color-primary);
    border-radius: 4px;
    margin-right: 12px;
  }

  .sub-command-info {
    .sub-command-name {
      font-weight: 500;
    }
    .sub-command-desc {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }
}

.command-suggestions-panel .panel-title,
.sub-command-panel .panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  margin-bottom: 8px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}
</style>

<style lang="scss">
.beautiful-popper {
  border-radius: 12px !important;
  border: 1px solid var(--el-border-color-extra-light) !important;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12) !important;
}
</style>