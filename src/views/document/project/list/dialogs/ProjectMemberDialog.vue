<template>
  <el-dialog
    :model-value="modelValue"
    title="项目成员管理"
    width="1200px"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:modelValue', $event)"
    @close="handleClose"
  >
    <div
      v-loading="loading"
      class="member-dialog-container"
    >
      <!-- Left Panel: Current Members -->
      <div class="panel current-members-panel">
        <div class="panel-header">
          <h3>
            当前成员 ({{ editingMembers.length }})
          </h3>
          <el-button
            v-if="canClearCurrentMembers"
            type="danger"
            size="small"
            link
            @click="handleClearCurrentMembers"
          >
            清空全部
          </el-button>
        </div>
        <div class="panel-body">
          <el-scrollbar>
            <transition-group
              tag="div"
              name="list"
              class="current-member-grid"
            >
              <div
                v-for="member in sortedEditingMembers"
                :key="member.userId"
                class="member-card"
              >
                <el-avatar
                  :size="48"
                  :src="member.picture"
                >
                  {{ member.userName.substring(0, 1) }}
                </el-avatar>
                <div class="member-card-name">
                  {{ member.userName }}
                </div>
                <div class="member-card-org">
                  {{ member.orgName }}
                </div>
                <div
                  v-if="member.identity !== 'OWNER'"
                  class="remove-action"
                >
                  <el-tooltip
                    content="移除成员"
                    placement="top"
                  >
                    <el-button
                      type="danger"
                      link
                      :icon="Delete"
                      @click="handleLocalRemove(member)"
                    />
                  </el-tooltip>
                </div>
                <div
                  v-if="member.identity === 'OWNER'"
                  class="owner-tag"
                >
                  负责人
                </div>
              </div>
            </transition-group>
            <el-empty
              v-if="!loading && editingMembers.length === 0"
              description="暂无项目成员"
            />
          </el-scrollbar>
        </div>
      </div>
      <!-- Right Panel: Add Members -->
      <div class="panel add-members-panel">
        <div class="panel-header">
          <h3>
            添加新成员
          </h3>
          <div class="add-member-actions">
            <el-button
              type="primary"
              size="small"
              :disabled="selectedUsers.size === 0"
              :icon="Plus"
              @click="handleLocalAdd"
            >
              添加选中 ({{ selectedUsers.size }})
            </el-button>
            <el-button
              v-if="selectedUsers.size > 0"
              size="small"
              :icon="CircleClose"
              @click="handleClearSelection"
            >
              清空
            </el-button>
          </div>
        </div>
        <div class="add-members-filter">
          <el-tree-select
            v-model="filterParams.depId"
            :data="departmentTree"
            placeholder="按部门筛选"
            clearable
            :props="{ label: 'orgName', value: 'id' }"
            check-strictly
            @change="filterParams.depId = $event"
          />
          <el-input
            v-model="filterParams.name"
            placeholder="按姓名搜索"
            clearable
            :prefix-icon="Search"
          />
          <el-input
            v-model="filterParams.position"
            placeholder="按职位搜索"
            clearable
            :prefix-icon="Search"
          />
        </div>
        <div
          v-loading="userLoading"
          class="panel-body"
        >
          <el-scrollbar>
            <div class="user-card-grid">
              <div
                v-for="user in filteredAvailableUsers"
                :key="user.userId"
                class="user-card"
                :class="{ selected: selectedUsers.has(user.userId) }"
                @click="toggleUserSelection(user)"
              >
                <div class="selection-indicator">
                  <el-icon><Select /></el-icon>
                </div>
                <el-avatar :size="48">
                  {{ user.userName.substring(0, 1) }}
                </el-avatar>
                <div class="user-card-name">
                  {{ user.userName }}
                </div>
                <div class="user-card-org">
                  {{ user.orgName }}
                </div>
              </div>
            </div>
            <el-empty
              v-if="!userLoading && filteredAvailableUsers.length === 0"
              description="未找到匹配的用户"
            />
          </el-scrollbar>
        </div>
      </div>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="$emit('update:modelValue', false)">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSaveChanges"> 确认 </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete, Search, Select, Plus, CircleClose } from '@element-plus/icons-vue'
import type { ProjectMember, Department, CompanyUser } from '@/types/document'
import { getProjectMembers, getDepartments, getUsers, saveProjectMembers } from '@/api/project'

const props = defineProps<{
  modelValue: boolean
  projectId: number | string | null
}>()

const emit = defineEmits(['update:modelValue', 'change'])

const loading = ref(false)
const userLoading = ref(false)
const saving = ref(false)

const editingMembers = ref<ProjectMember[]>([])
const allCompanyUsers = ref<CompanyUser[]>([])
const departmentTree = ref<Department[]>([])
const selectedUsers = ref<Set<number>>(new Set())

const filterParams = reactive({ name: '', depId: '', position: '' })

const sortedEditingMembers = computed(() => {
  return [...editingMembers.value].sort((a, b) => {
    if (a.identity === 'OWNER') return -1
    if (b.identity === 'OWNER') return 1
    return a.userName.localeCompare(b.userName)
  })
})

const canClearCurrentMembers = computed(() => {
  return editingMembers.value.some(m => m.identity !== 'OWNER')
})

// Computed property to find users available to be added
const availableUsers = computed(() => {
  const currentMemberIds = new Set(editingMembers.value.map(m => m.userId))
  return allCompanyUsers.value.filter(u => !currentMemberIds.has(u.userId))
})

// Computed property to filter the available users based on text/dep inputs
const filteredAvailableUsers = computed(() => {
  let users = availableUsers.value
  if (filterParams.name) {
    users = users.filter(u => u.userName.includes(filterParams.name))
  }
  if (filterParams.depId) {
    users = users.filter(u => u.orgId === filterParams.depId)
  }
  if (filterParams.position) {
    users = users.filter(u => u.position?.includes(filterParams.position))
  }
  return users
})

const loadInitialData = async () => {
  if (!props.projectId) return
  loading.value = true
  userLoading.value = true
  try {
    const [membersRes, usersRes, departmentsRes] = await Promise.all([
      getProjectMembers(props.projectId),
      getUsers({}), // Fetch all users
      departmentTree.value.length === 0 ? getDepartments() : Promise.resolve(departmentTree.value)
    ])
    editingMembers.value = membersRes || []
    allCompanyUsers.value = usersRes.records || []
    departmentTree.value = departmentsRes || []
  } catch (error) {
    console.error('Failed to load initial data:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
    userLoading.value = false
  }
}

const toggleUserSelection = (user: CompanyUser) => {
  if (selectedUsers.value.has(user.userId)) {
    selectedUsers.value.delete(user.userId)
  } else {
    selectedUsers.value.add(user.userId)
  }
}

const handleClearSelection = () => {
  selectedUsers.value.clear()
}

const handleLocalAdd = () => {
  const usersToAdd = allCompanyUsers.value.filter(u => selectedUsers.value.has(u.userId))
  const newMembers: ProjectMember[] = usersToAdd.map(u => ({
    userId: u.userId,
    identity: 'COLLABORATOR',
    userName: u.userName,
    orgName: u.orgName || '未指定',
    picture: '',
    position: u.position || '新成员'
  }))
  editingMembers.value.push(...newMembers)
  selectedUsers.value.clear()
}

const handleLocalRemove = (member: ProjectMember) => {
  const index = editingMembers.value.findIndex(m => m.userId === member.userId)
  if (index > -1) {
    editingMembers.value.splice(index, 1)
  }
}

const handleSaveChanges = async () => {
  if (!props.projectId) return
  saving.value = true
  try {
    const finalUserIds = editingMembers.value.map(m => m.userId)
    await saveProjectMembers(props.projectId, finalUserIds)
    ElMessage.success('成员保存成功')
    emit('update:modelValue', false)
    emit('change')
  } catch (error) {
    console.error('Failed to save members:', error)
    ElMessage.error('成员保存失败')
  } finally {
    saving.value = false
  }
}

const resetState = () => {
  editingMembers.value = []
  allCompanyUsers.value = []
  selectedUsers.value.clear()
  filterParams.name = ''
  filterParams.depId = ''
  filterParams.position = ''
}

const handleClose = () => {
  resetState()
}

const handleClearCurrentMembers = () => {
  const owner = editingMembers.value.find(m => m.identity === 'OWNER')
  editingMembers.value = owner ? [owner] : []
}

watch(
  () => props.modelValue,
  (isVisible) => {
    if (isVisible) {
      loadInitialData()
    }
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">
:deep(.el-dialog__body) {
  padding-top: 10px;
  padding-bottom: 10px;
}

.member-dialog-container {
  display: flex;
  gap: 24px;
  height: 60vh;
  background-color: #f7f8fa;
  padding: 16px;
  border-radius: 8px;
}

.panel {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;
  background-color: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.panel-header {
  padding: 12px 16px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
  h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
  }
  .add-member-actions {
    display: flex;
    gap: 8px;
  }
}

.panel-body {
  flex-grow: 1;
  overflow: hidden;
  position: relative;
  padding: 8px;
}

// Base card style
.member-card,
.user-card {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease-in-out;
  position: relative;
  background-color: #fff;

  .member-card-name,
  .user-card-name {
    margin-top: 10px;
    font-size: 14px;
    font-weight: 500;
    text-align: center;
    word-break: break-all;
  }

  .member-card-org,
  .user-card-org {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }
}

// Grid layout for cards
.current-member-grid,
.user-card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
  gap: 16px;
  padding: 8px;
}

// Hover effects for cards
.member-card:hover,
.user-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

// Specific styles for current member cards
.member-card {
  .remove-action {
    position: absolute;
    top: 4px;
    right: 4px;
    opacity: 0;
    transition: opacity 0.2s ease-in-out;
  }

  &:hover .remove-action {
    opacity: 1;
  }

  .owner-tag {
    position: absolute;
    top: -1px;
    left: -1px;
    background: linear-gradient(145deg, #ffc107, #f5ab1b);
    color: white;
    font-size: 10px;
    padding: 3px 6px;
    border-radius: 6px 0 6px 0;
    font-weight: 600;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  }
}

// Specific styles for user selection cards
.user-card {
  cursor: pointer;

  &.selected {
    border-color: var(--el-color-primary);
    background-color: #f2f8ff;
    box-shadow: 0 0 0 2px var(--el-color-primary-light-7);
  }

  .selection-indicator {
    position: absolute;
    top: 5px;
    right: 5px;
    color: var(--el-color-primary);
    font-size: 18px;
    opacity: 0;
    transform: scale(0.5);
    transition: all 0.2s ease-in-out;
  }

  &.selected .selection-indicator {
    opacity: 1;
    transform: scale(1);
  }
}

.add-members-panel {
  .add-members-filter {
    display: flex;
    gap: 10px;
    padding: 12px 16px;
    border-bottom: 1px solid #e4e7ed;
    flex-shrink: 0;
    background-color: #fafbfc;

    .el-tree-select,
    .el-input {
      flex: 1;
    }
  }
}

// Transition styles
.list-move, /* apply transition to moving elements */
.list-enter-active,
.list-leave-active {
  transition: all 0.5s ease;
}

.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateY(30px);
}

/* ensure leaving items are taken out of layout flow so that moving
   animations can be calculated correctly. */
.list-leave-active {
  position: absolute;
}
</style> 