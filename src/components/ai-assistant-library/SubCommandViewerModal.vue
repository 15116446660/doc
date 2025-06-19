<template>
  <el-dialog
    :title="`子命令列表 - ${parentCommand?.name || ''}`"
    v-model="dialogVisible"
    width="650px"
    @close="closeDialog"
  >
    <div class="sub-command-viewer-content">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      
      <div v-else-if="subCommands.length === 0" class="empty-container">
        <el-empty description="暂无子命令" />
      </div>
      
      <div v-else class="sub-commands-list">
        <el-collapse v-model="activeNames">
          <el-collapse-item 
            v-for="cmd in subCommands" 
            :key="cmd.id"
            :name="cmd.id"
          >
            <template #title>
              <div class="sub-command-header">
                <el-icon v-if="cmd.icon">
                  <component :is="cmd.icon || 'Document'" />
                </el-icon>
                <span class="sub-command-title">{{ cmd.name }}</span>
              </div>
            </template>
            
            <div class="sub-command-details">
              <div class="sub-command-section">
                <div class="sub-command-label">描述：</div>
                <div class="sub-command-value">{{ cmd.description || '无描述' }}</div>
              </div>
              
              <div class="sub-command-section">
                <div class="sub-command-label">模板：</div>
                <div class="sub-command-value template">
                  <pre>{{ cmd.template }}</pre>
                </div>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
    </div>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="refreshSubCommands" type="primary" plain :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button @click="closeDialog">关闭</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import type { SubCommand, Command } from '@/types/chat'
import { usePromptCommands } from './hooks/usePromptCommands'

// 定义组件属性
const props = defineProps<{
  parentCommandId: string
  parentCommand?: Command | null
}>()

// 定义事件
const emit = defineEmits<{
  (e: 'close'): void
}>()

// 使用emit函数示例（确保lint通过）
function closeDialog() {
  emit('close')
}

// 状态
const dialogVisible = ref(true)
const loading = ref(false)
const subCommands = ref<SubCommand[]>([])
const activeNames = ref<string[]>([])

// 工具函数
const { fetchSubCommands } = usePromptCommands()

// 载入子命令
async function loadSubCommands() {
  if (!props.parentCommandId) return
  
  loading.value = true
  try {
    // 尝试从父组件传入的参数中获取子命令
    if (props.parentCommand?.subCommands && props.parentCommand.subCommands.length > 0) {
      // 如果父命令有预设的子命令（系统命令），直接使用
      subCommands.value = [...props.parentCommand.subCommands]
    } else {
      // 否则从API获取子命令
      subCommands.value = await fetchSubCommands(props.parentCommandId)
    }
    
    // 默认展开第一个子命令
    if (subCommands.value.length > 0) {
      activeNames.value = [subCommands.value[0].id]
    }
  } catch (error) {
    console.error('加载子命令失败:', error)
    ElMessage.error('加载子命令失败')
  } finally {
    loading.value = false
  }
}

// 刷新子命令列表
async function refreshSubCommands() {
  await loadSubCommands()
}

// 组件挂载时加载子命令
onMounted(() => {
  loadSubCommands()
})
</script>

<style scoped>
.sub-command-viewer-content {
  min-height: 300px;
  max-height: 500px;
  overflow-y: auto;
}

.loading-container {
  padding: 20px;
}

.empty-container {
  padding: 40px 0;
}

.sub-commands-list {
  width: 100%;
}

.sub-command-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sub-command-title {
  font-weight: 500;
}

.sub-command-details {
  padding: 10px 0;
}

.sub-command-section {
  margin-bottom: 15px;
}

.sub-command-label {
  font-weight: 500;
  margin-bottom: 5px;
  color: var(--el-text-color-secondary);
}

.sub-command-value {
  color: var(--el-text-color-primary);
}

.sub-command-value.template {
  background-color: var(--el-fill-color-light);
  border-radius: 4px;
  padding: 10px;
  overflow-x: auto;
}

.sub-command-value.template pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}
</style> 