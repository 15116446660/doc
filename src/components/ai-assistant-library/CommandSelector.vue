<template>
  <el-dropdown trigger="click" @command="handleCommandCommand" popper-class="beautiful-popper">
    <div class="command-selector">
      <el-icon class="command-icon"><component :is="'List'" /></el-icon>
      <span>命令</span>
      <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
    </div>
    <template #dropdown>
      <el-dropdown-menu placement="bottom">
        <div class="panel-title">预设命令</div>
        <div class="command-list-wrapper">
          <el-dropdown-item
            v-for="cmd in commands"
            :key="cmd.id"
            :command="cmd.id"
            class="command-dropdown-item"
          >
            <el-icon class="command-logo"><component :is="cmd.icon || 'Document'" /></el-icon>
            <div class="command-info">
              <div class="command-name">
                {{ cmd.name }}
                <el-tag 
                  v-if="cmd.hasSubCommands" 
                  type="info" 
                  size="small" 
                  class="sub-cmd-tag"
                >有子命令</el-tag>
              </div>
              <div class="command-desc">{{ cmd.description }}</div>
            </div>
          </el-dropdown-item>
        </div>
        <div class="divider"></div>
        <div class="config-item" @click="openCommandConfig">
          <el-icon><Setting /></el-icon>
          <span>命令管理</span>
        </div>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import type { Command } from '@/types/chat';
import { ArrowDown, Setting } from '@element-plus/icons-vue';

defineProps<{
  commands: Command[];
  disabled?: boolean;
}>();

const emit = defineEmits<{
  (e: 'view-sub-commands', commandId: string): void;
  (e: 'open-command-management'): void;
}>();

// 处理命令选择
const handleCommandCommand = (commandId: string) => {
  emit('view-sub-commands', commandId);
};

// 打开命令配置
const openCommandConfig = () => {
  emit('open-command-management');
};
</script>

<style lang="scss" scoped>
.command-selector {
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

  .command-icon {
    width: 18px;
    height: 18px;
    margin-right: 8px;
    color: var(--el-text-color-regular);
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

.command-dropdown-item {
  display: flex;
  align-items: flex-start;
  padding: 10px 12px;
  
  .command-logo {
    font-size: 18px;
    margin-right: 10px;
    margin-top: 3px;
    color: var(--el-color-primary);
  }

  .command-info {
    flex: 1;
  }

  .command-name {
    display: flex;
    align-items: center;
    font-weight: 500;
    font-size: 14px;
    margin-bottom: 4px;
  }

  .command-desc {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin-bottom: 4px;
  }

  .sub-cmd-tag {
    margin-left: 8px;
    font-size: 10px;
    padding: 0 6px;
    height: 18px;
    line-height: 16px;
  }
}

.panel-title {
  padding: 8px 12px;
  font-size: 14px;
  color: var(--el-text-color-secondary);
  border-bottom: 1px solid var(--el-border-color-light);
}

.command-list-wrapper {
  max-height: 400px;
  overflow-y: auto;
}

.divider {
  height: 1px;
  background-color: var(--el-border-color-light);
  margin: 4px 0;
}

.config-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  cursor: pointer;
  color: var(--el-text-color-regular);
  
  &:hover {
    background-color: var(--el-fill-color-light);
  }
  
  .el-icon {
    margin-right: 8px;
  }
}
</style> 