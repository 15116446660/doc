<template>
  <el-dialog
    v-model="internalVisible"
    title="文档协作人员管理"
    width="1000px"
    top="10vh"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div
      v-loading="loading"
      class="collaborator-dialog-content"
    >
      <el-table
        :data="collaborators"
        border
        stripe
        height="60vh"
        row-key="userId"
      >
        <el-table-column
          label="协作者"
          width="200"
          fixed
        >
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar
                :size="32"
                :src="row.picture"
              >{{ row.userName.charAt(0) }}</el-avatar>
              <div class="user-details">
                <div class="user-name">{{ row.userName }}</div>
                <div class="user-org">{{ row.orgName }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column
          label="是否协作"
          width="100"
          align="center"
        >
          <template #default="{ row }">
            <el-switch
              v-model="row.selected"
              :active-value="1"
              :inactive-value="0"
              @change="() => handleSelectionChange(row)"
            />
          </template>
        </el-table-column>

        <el-table-column
          label="角色"
          width="140"
          align="center"
        >
          <template #default="{ row }">
            <el-select
              v-model="row.identity"
              placeholder="选择角色"
              :disabled="row.selected === 0"
              clearable
              size="small"
            >
              <el-option
                label="负责人"
                value="DIRECTOR"
              />
              <el-option
                label="编辑者"
                value="EDITOR"
              />
              <el-option
                label="查看者"
                value="VIEWER"
              />
            </el-select>
          </template>
        </el-table-column>

        <el-table-column
          label="操作权限"
          min-width="400"
        >
          <template #default="{ row }">
            <div class="permissions-group">
              <el-checkbox
                v-for="perm in permissionOptions"
                :key="perm.key"
                v-model="row[perm.key]"
                :label="perm.label"
                :disabled="row.selected === 0"
                :true-value="1"
                :false-value="0"
                size="small"
              />
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="internalVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="saving"
          @click="handleSaveChanges"
        >
          保存
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getDocumentCollaborators, updateDocumentCollaborators } from '@/api/document'
import type { DocumentCollaborator } from '@/types/document'
import { cloneDeep } from 'lodash-es'

type PermissionKey = 'down' | 'edit' | 'upload' | 'del' | 'print' | 'comment' | 'copy'

const props = defineProps<{
  modelValue: boolean
  documentId: string | null
}>()

const emit = defineEmits(['update:modelValue', 'save-success'])

const internalVisible = ref(props.modelValue)
const loading = ref(false)
const saving = ref(false)
const collaborators = ref<DocumentCollaborator[]>([])

const permissionOptions: { key: PermissionKey; label: string }[] = [
  { key: 'edit', label: '编辑' },
  { key: 'down', label: '下载' },
  { key: 'print', label: '打印' },
  { key: 'comment', label: '评论' },
  { key: 'copy', label: '复制' },
  { key: 'upload', label: '上传' },
  { key: 'del', label: '删除' }
]

const fetchData = async () => {
  if (!props.documentId) return
  loading.value = true
  try {
    const data = await getDocumentCollaborators(props.documentId)
    collaborators.value = cloneDeep(data) // Use cloneDeep to avoid modifying original data
  } catch (error) {
    console.error('Failed to fetch collaborators:', error)
    ElMessage.error('获取协作人员失败')
  } finally {
    loading.value = false
  }
}

const handleSelectionChange = (row: DocumentCollaborator) => {
  if (row.selected === 0) {
    // If deselected, clear all permissions and role
    row.identity = null
    permissionOptions.forEach(perm => {
      row[perm.key] = 0
    })
  } else {
    // If selected, default to a role if none is set
    if (!row.identity) {
      row.identity = 'VIEWER'
    }
  }
}

const handleSaveChanges = async () => {
  if (!props.documentId) return
  saving.value = true
  try {
    await updateDocumentCollaborators(props.documentId, collaborators.value)
    ElMessage.success('协作人员信息保存成功')
    emit('save-success')
    internalVisible.value = false
  } catch (error) {
    console.error('Failed to save collaborators:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleClose = () => {
  emit('update:modelValue', false)
}

watch(() => props.modelValue, (val) => {
  internalVisible.value = val
  if (val) {
    fetchData()
  } else {
    collaborators.value = [] // Clear data on close
  }
}, {
    immediate: true
})
</script>

<style scoped lang="scss">
.collaborator-dialog-content {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.user-org {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.permissions-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 