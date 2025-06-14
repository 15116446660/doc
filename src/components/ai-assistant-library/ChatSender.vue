<template>
  <div class="chat-sender-container">
    <!-- 顶部工具栏 -->
    <div class="top-toolbar">
      <div class="toolbar-left">
        <!-- 模型选择器 -->
        <el-dropdown trigger="click" @command="handleModelChange" :disabled="isGenerating" popper-class="beautiful-popper">
          <div class="model-selector">
            <img v-if="currentModelLogo" :src="currentModelLogo" class="model-logo" alt="logo" />
            <el-icon v-else class="model-logo-default"><Cpu /></el-icon>
            <span>{{ currentModelName }}</span>
            <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <div class="panel-title">选择模型</div>
              <div class="model-list-wrapper">
                <el-dropdown-item
                  v-for="model in models"
                  :key="model.id"
                  :command="model.id"
                  :class="{ 'is-active': model.id === currentModelId }"
                  class="model-dropdown-item"
                >
                  <img v-if="model.logo" :src="model.logo" class="model-logo" alt="logo" />
                  <el-icon v-else class="model-logo-default"><Cpu /></el-icon>
                  <span>{{ model.name }}</span>
                </el-dropdown-item>
              </div>
              <el-dropdown-item divided @click.stop="openModelConfig">
                <el-icon><Setting /></el-icon>
                <span>模型配置</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        
        <!-- 命令管理 -->
        <el-tooltip content="命令管理" placement="top">
          <el-button :icon="Operation" circle @click="handleCommand('commands')" />
        </el-tooltip>
        
        <!-- 上传附件 -->
        <el-tooltip content="上传附件" placement="top">
          <el-button :icon="Paperclip" circle @click="triggerFileUpload" />
        </el-tooltip>
        <input type="file" ref="fileInputRef" style="display: none" @change="handleFileChange" multiple />
      </div>
      <div class="toolbar-right">
        <!-- 新建会话 -->
        <el-tooltip content="新建会话" placement="top">
          <el-button :icon="Plus" circle @click="handleCommand('new')" />
        </el-tooltip>
        <!-- 历史会话 -->
        <el-tooltip content="历史会话" placement="top">
          <el-button :icon="List" circle @click="handleCommand('history')" />
        </el-tooltip>
        <!-- 清空当前会话 -->
        <el-tooltip content="清空当前会话" placement="top">
          <el-button :icon="Delete" circle @click="handleCommand('clear')" />
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
            @click="executeQuickCommand(cmd)"
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
                  @click="executeQuickCommand(cmd)"
                >
                  {{ cmd.name }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
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
        @keydown.enter.exact.prevent="sendMessage"
        @keydown="handleKeydown"
        @input="handleInput"
        @focus="isInputActive = true"
        @blur="isInputActive = false"
        :disabled="isGenerating"
        class="main-textarea"
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
                <el-tooltip content="知识库检索" placement="top" v-if="!selectedKnowledgeBaseId">
                  <el-button 
                    class="mode-btn"
                    :class="{ 'is-active': isRAGMode }"
                    @click.stop="handleKnowledgeBaseButtonClick"
                    size="small"
                  >
                    <el-icon><DataLine /></el-icon>
                    搜索
                  </el-button>
                </el-tooltip>

                <div class="selected-kb-display" v-else-if="selectedKnowledgeBase" @click.stop="handleKnowledgeBaseButtonClick">
                   <el-icon class="kb-icon"><component :is="iconMap[selectedKnowledgeBase.icon] || Document" /></el-icon>
                   <span class="kb-name">{{ selectedKnowledgeBase.name }}</span>
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
                  :class="{ active: selectedKnowledgeBaseId === kb.id }"
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
            @click="isGenerating ? stopGenerating() : sendMessage()"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { ElMessageBox } from 'element-plus';
import {
  Cpu,
  Paperclip,
  Position,
  VideoPause,
  Delete,
  ArrowDown,
  Setting,
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
} from '@element-plus/icons-vue';
import type { AIModel, SubCommand as SubCommandType, Command as BaseCommand, KnowledgeBase, QuickCommand } from '@/types/chat';
import { usePromptCommands } from '@/components/ai-assistant-library/hooks/usePromptCommands';
import { getSubCommands, getQuickCommands } from '@/api/command';
import { getKnowledgeBases } from '@/api/knowledgeBase';

// Icon mapping to resolve linter errors and for dynamic rendering
const iconMap: Record<string, any> = {
  Document,
  FolderOpened,
  DataAnalysis,
  CircleClose,
};

// #region --- Props & Emits Definition ---
const props = withDefaults(
  defineProps<{
    isGenerating?: boolean;
    isDeepThinkingMode?: boolean;
    isRAGMode?: boolean;
    currentModelId?: string;
    models?: AIModel[];
  }>(),
  {
    isGenerating: false,
    isDeepThinkingMode: false,
    isRAGMode: false,
    currentModelId: 'gpt-4',
    models: () => [
      { id: 'gpt-4-mini', name: 'GPT-4 mini', logo: 'https://cdn.jsdelivr.net/gh/walkxcode/dashboard-icons/png/openai.png' },
      { id: 'gpt-4', name: 'GPT-4', logo: 'https://cdn.jsdelivr.net/gh/walkxcode/dashboard-icons/png/openai.png' },
      { id: 'claude-3', name: 'Claude 3', logo: 'https://cdn.jsdelivr.net/gh/walkxcode/dashboard-icons/png/anthropic.png' },
      { id: 'gemini-pro', name: 'Gemini Pro', logo: 'https://cdn.jsdelivr.net/gh/walkxcode/dashboard-icons/png/google-gemini.png' },
      { id: 'custom-model', name: '自定义模型' },
    ],
  }
);

const emit = defineEmits<{
  (e: 'send', content: string, attachments: File[], context?: { selectedText?: string }): void;
  (e: 'stop'): void;
  (e: 'modelChange', modelId: string): void;
  (e: 'command', command: string): void;
  (e: 'toggleDeepThinking'): void;
  (e: 'toggleRAG'): void;
  (e: 'openModelConfig'): void;
  (e: 'clearConversation'): void;
}>();
// #endregion

// #region --- State Management ---
const inputMessage = ref('');
const fileInputRef = ref<HTMLInputElement | null>(null);
const attachments = ref<File[]>([]);
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
const selectedKnowledgeBaseId = ref<string | null>(null);

const quickCommands = ref<QuickCommand[]>([]);

const maxVisibleQuickCommands = 5;
// #endregion

// #region --- Computed Properties ---
const currentModelName = computed(() => {
  const model = props.models?.find(m => m.id === props.currentModelId);
  return model ? model.name : '选择模型';
});

const currentModelLogo = computed(() => {
  const model = props.models?.find(m => m.id === props.currentModelId);
  return model ? model.logo : null;
});

const canSend = computed(() => {
  return (
    !props.isGenerating && (inputMessage.value.trim().length > 0 || attachments.value.length > 0)
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
  return knowledgeBases.value.find(kb => kb.id === selectedKnowledgeBaseId.value) || null;
});
// #endregion

// #region --- Event Handlers & Methods ---
const handleModelChange = (modelId: string) => emit('modelChange', modelId);
const openModelConfig = () => emit('openModelConfig');
const triggerFileUpload = () => fileInputRef.value?.click();
const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files) {
    attachments.value.push(...Array.from(target.files));
  }
};

const sendMessage = () => {
  if (!canSend.value) return;
  const commandToSend = inputMessage.value;

  if (commandToSend.startsWith('/')) {
    emit('send', commandToSend, attachments.value);
  } else {
    const context = selectedText.value ? { selectedText: selectedText.value } : undefined;
    emit('send', inputMessage.value, attachments.value, context);
  }

  inputMessage.value = '';
  attachments.value = [];
  clearSelectedText();
};

const stopGenerating = () => emit('stop');

const handleCommand = (command: string) => {
  if (command === 'clear') {
    ElMessageBox.confirm('确定要清空当前会话的所有消息吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
      .then(() => {
        emit('clearConversation');
      })
      .catch(() => {});
  } else {
    emit('command', command);
  }
};

const toggleDeepThinkingMode = () => emit('toggleDeepThinking');

const handleKnowledgeBaseButtonClick = () => {
  isKnowledgeBasePanelVisible.value = true;
};

const selectKnowledgeBase = (kbId: string) => {
  selectedKnowledgeBaseId.value = kbId;
  isKnowledgeBasePanelVisible.value = false;
  if (!props.isRAGMode) {
    emit('toggleRAG');
  }
};

const clearSelectedKnowledgeBase = () => {
  selectedKnowledgeBaseId.value = null;
  if (props.isRAGMode) {
    emit('toggleRAG');
  }
};

const clearSelectedText = () => {
  selectedText.value = '';
};

const executeQuickCommand = (command: QuickCommand) => {
  const finalPrompt = command.prompt.replace('{selectedText}', selectedText.value);
  const context = { selectedText: selectedText.value };
  emit('send', finalPrompt, [], context);
  clearSelectedText();
};

const handleInput = (value: string) => {
  const trimmedValue = value.trim();
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

const selectCommand = async (cmd: BaseCommand) => {
  activeCommandIndex.value = suggestionCommands.value.findIndex(c => c.id === cmd.id);

  if (!cmd.hasSubCommands) {
    inputMessage.value = `/${cmd.name} `;
    showCommandSuggestions.value = false;
    selectedCommandId.value = null;
    activeSubCommand.value = null;
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
  if (activeSubCommand.value) {
    inputMessage.value = `/${activeSubCommand.value.name} ${subCmd.name} `;
  }
  showCommandSuggestions.value = false;
  activeSubCommand.value = null;
  selectedCommandId.value = null;
};

const handleMouseUp = () => {
  const text = window.getSelection()?.toString().trim() ?? '';
  if (text && text.length > 10) {
    selectedText.value = text;
  }
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
    padding: 4px 8px;
    border-radius: 6px;
    cursor: pointer;
    transition: background-color 0.2s;
    &:hover {
      background-color: var(--el-fill-color-light);
    }

    .el-icon {
      margin-right: 6px;
    }
    .dropdown-icon {
      margin-left: 6px;
      margin-right: 0;
    }
  }

  .el-button--circle {
    width: 32px;
    height: 32px;
  }
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
  gap: 8px;

  .model-logo {
    width: 20px;
    height: 20px;
    object-fit: contain;
  }

  .model-logo-default {
    width: 20px;
    height: 20px;
    font-size: 20px;
  }

  &.is-active,
  &.is-active:hover,
  &.is-active:focus {
    background-color: var(--el-color-primary-light-8) !important;
    color: var(--el-color-primary) !important;
    font-weight: 500;
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