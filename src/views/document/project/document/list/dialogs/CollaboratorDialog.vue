<template>
  <el-dialog
    :model-value="modelValue"
    :title="dialogTitle"
    width="1000px"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:modelValue', $event)"
    @open="handleOpen"
    @close="handleClose"
  >
    <div v-loading="loading" class="dialog-content-wrapper">
      <transition name="fade-mode" mode="out-in">
        <!-- VIEW MODE -->
        <div v-if="mode === 'view'" class="view-mode-content">
          <el-empty v-if="viewCollaborators.length === 0" description="当前文档没有协作者" />
          <transition-group v-else tag="div" name="card-fade" class="collaborator-cards-grid">
            <el-card v-for="user in viewCollaborators" :key="user.userId" shadow="hover" class="collaborator-card">
              <div class="card-content">
                <div class="card-header">
                  <div class="user-info">
                    <el-avatar :size="48" :src="user.picture">{{ user.userName.substring(0, 1) }}</el-avatar>
                    <div class="user-details">
                      <div class="user-name">{{ user.userName }}</div>
                      <div class="user-org">{{ user.orgName }} / {{ user.position }}</div>
                    </div>
                  </div>
                  <el-tag effect="light" round>{{ user.identity }}</el-tag>
                </div>
                <div class="permissions-display">
                  <div
                    v-for="perm in permissionIcons"
                    :key="perm.key"
                    class="permission-icon"
                    :class="{ active: user[perm.key] === 1 }"
                  >
                    <el-tooltip :content="perm.label" placement="top">
                      <el-icon><component :is="perm.icon" /></el-icon>
                    </el-tooltip>
                  </div>
                </div>
              </div>
            </el-card>
          </transition-group>
        </div>

        <!-- EDIT MODE -->
        <div v-else-if="mode === 'edit'" class="edit-mode-content">
          <el-table :data="editingCollaborators" style="width: 100%" height="50vh" border stripe>
            <el-table-column label="成员" width="220">
              <template #default="{ row }">
                <div class="user-info">
                  <el-avatar :size="40" :src="row.picture">{{ row.userName.substring(0, 1) }}</el-avatar>
                  <div class="user-details">
                    <div class="user-name">{{ row.userName }}</div>
                    <div class="user-org">{{ row.orgName }} / {{ row.position }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="协作者" width="80" align="center">
              <template #default="{ row }">
                <el-switch
                  v-model="row.selected"
                  :active-value="1"
                  :inactive-value="0"
                  @change="handleSelectionChange(row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="角色" width="140">
              <template #default="{ row }">
                <el-select v-model="row.identity" placeholder="请选择角色" :disabled="row.selected === 0">
                  <el-option label="主管" value="DIRECTOR"></el-option>
                  <el-option label="协作者" value="COLLABORATOR"></el-option>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="权限">
              <template #default="{ row }">
                <div class="permission-group">
                  <el-checkbox
                    v-for="perm in permissionIcons"
                    :key="perm.key"
                    v-model="row[perm.key]"
                    :true-value="1"
                    :false-value="0"
                    :disabled="row.selected === 0"
                    :label="perm.label"
                  />
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </transition>
    </div>
    <template #footer>
      <div v-if="mode === 'view'">
        <el-button @click="$emit('update:modelValue', false)">关闭</el-button>
        <el-button type="primary" @click="switchToEditMode">管理协作人员</el-button>
      </div>
      <div v-if="mode === 'edit'">
        <el-button @click="handleCancelEdit">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSaveChanges">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Download,
  Edit,
  Upload,
  Delete,
  Printer,
  ChatDotSquare,
  CopyDocument
} from '@element-plus/icons-vue'
import type { DocumentCollaborator } from '@/types/document'
import { getDocumentCollaborators, updateDocumentCollaborators } from '@/api/document'
import { cloneDeep } from 'lodash-es'

type PermissionKey = 'down' | 'edit' | 'upload' | 'del' | 'print' | 'comment' | 'copy'

const props = defineProps<{
  modelValue: boolean
  documentId: string | null
}>()

const emit = defineEmits(['update:modelValue', 'change'])

const mode = ref<'view' | 'edit'>('view')
const loading = ref(false)
const saving = ref(false)
const allCollaborators = ref<DocumentCollaborator[]>([])
const editingCollaborators = ref<DocumentCollaborator[]>([])

const permissionIcons: { key: PermissionKey; label: string; icon: any }[] = [
  { key: 'down', label: '下载', icon: Download },
  { key: 'edit', label: '编辑', icon: Edit },
  { key: 'upload', label: '上传', icon: Upload },
  { key: 'del', label: '删除', icon: Delete },
  { key: 'print', label: '打印', icon: Printer },
  { key: 'comment', label: '评论', icon: ChatDotSquare },
  { key: 'copy', label: '复制', icon: CopyDocument }
]

const dialogTitle = computed(() => (mode.value === 'view' ? '文档协作者' : '管理文档协作者'))
const viewCollaborators = computed(() => allCollaborators.value.filter((c) => c.selected === 1))

const loadCollaborators = async () => {
  if (!props.documentId) return
  loading.value = true
  try {
    allCollaborators.value = await getDocumentCollaborators(props.documentId)
  } catch (error) {
    console.error('Failed to load collaborators:', error)
    ElMessage.error('加载协作者列表失败')
  } finally {
    loading.value = false
  }
}

const switchToEditMode = () => {
  editingCollaborators.value = cloneDeep(allCollaborators.value)
  mode.value = 'edit'
}

const handleCancelEdit = () => {
  mode.value = 'view'
  editingCollaborators.value = []
}

const handleSelectionChange = (row: DocumentCollaborator) => {
  if (row.selected === 0) {
    row.identity = null
    permissionIcons.forEach(p => {
      const key = p.key as keyof DocumentCollaborator
      if (typeof row[key] === 'number') {
        ;(row[key] as any) = 0
      }
    })
  } else {
    row.identity = 'COLLABORATOR'
    row.down = 1
    row.edit = 1
    row.copy = 1
  }
}

const handleSaveChanges = async () => {
  if (!props.documentId) return
  saving.value = true
  try {
    await updateDocumentCollaborators(props.documentId, editingCollaborators.value)
    ElMessage.success('协作者保存成功')
    await loadCollaborators()
    mode.value = 'view'
    emit('change')
  } catch (error) {
    console.error('Failed to save collaborators:', error)
    ElMessage.error('协作者保存失败')
  } finally {
    saving.value = false
  }
}

const handleOpen = () => {
  mode.value = 'view'
  loadCollaborators()
}

const handleClose = () => {
  mode.value = 'view'
  allCollaborators.value = []
  editingCollaborators.value = []
}
</script>

<style scoped lang="scss">
// Transitions
.fade-mode-enter-active,
.fade-mode-leave-active {
  transition: opacity 0.3s ease;
}
.fade-mode-enter-from,
.fade-mode-leave-to {
  opacity: 0;
}

.card-fade-enter-active,
.card-fade-leave-active {
  transition: all 0.5s ease;
}
.card-fade-enter-from,
.card-fade-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
.card-fade-move {
  transition: transform 0.5s ease;
}


// Main content wrapper
.dialog-content-wrapper {
  background: rgba(0,0,0,0.02);
  border-radius: 8px;
  padding: 24px;
  min-height: 400px;
}

.view-mode-content {
  min-height: 300px;
}

.collaborator-cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.collaborator-card {
  border-radius: 12px;
  border: 1px solid transparent;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.9), rgba(245, 245, 255, 0.8));
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  overflow: hidden;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.08);
    border-color: var(--el-color-primary-light-7);
  }
  
  .card-content {
    padding: 8px;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;

  .el-tag {
    font-weight: 600;
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.user-name {
  font-weight: 600;
  font-size: 16px;
}
.user-org {
  font-size: 12px;
  color: #888;
}

.permissions-display {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(40px, 1fr));
  gap: 12px;
  padding-top: 16px;
  margin-top: 12px;
  border-top: 1px solid var(--el-border-color-lighter);
}
.permission-icon {
  font-size: 20px;
  color: var(--el-text-color-disabled);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &.active {
    color: var(--el-color-primary);
    text-shadow: 0 0 10px var(--el-color-primary-light-5);
  }
}

.permission-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  .el-checkbox {
    min-width: 80px;
    margin-right: 10px;
  }
}

:deep(.el-dialog__header) {
  padding-bottom: 20px;
  .el-dialog__title {
    font-size: 20px;
    font-weight: 600;
  }
}

:deep(.el-table) {
  .el-table__header-wrapper th {
    background-color: var(--el-fill-color-light) !important;
  }
}
</style>