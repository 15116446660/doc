<script setup lang="ts">
import { ref, watch, computed, onMounted } from 'vue'
import { ElDialog, ElMessage } from 'element-plus'
import { Plus, Close, Search } from '@element-plus/icons-vue'
import type { User } from '@/types/user'
import type { ProjectMember } from '@/api/project'
// import { getUserList } from '@/api/user'
// import { getProjectMembers, updateProjectMembers } from '@/api/project'

// FAKE API calls until the issue with edit_file is resolved
async function getUserList(params: any): Promise<any> {
  console.log('Faking getUserList with params:', params)
  const allUsers: User[] = Array.from({ length: 100 }).map((_, i) => ({
    id: i + 1,
    name: `All-User-${i + 1}`,
    account: `user${i+1}`,
    orgName: `Org ${Math.floor(i / 10)}`,
    position: 'Developer',
  }));
  const { current = 1, size = 20, keyword = '' } = params
  const filtered = keyword ? allUsers.filter(u => u.name.includes(keyword)) : allUsers
  const start = (current - 1) * size
  const records = filtered.slice(start, start + size)
  return Promise.resolve({ data: { records, total: filtered.length } })
}

async function getProjectMembers(projectId: string | number): Promise<any> {
  console.log('Faking getProjectMembers for projectId:', projectId)
  const members: ProjectMember[] = [
    { userId: 1, userName: 'Current-Member-1', identity: 'OWNER', orgName: 'Org 1', picture: '', position: 'Manager' },
    { userId: 2, userName: 'Current-Member-2', identity: 'COLLABORATOR', orgName: 'Org 2', picture: '', position: 'Developer' },
  ]
  return Promise.resolve({ data: members })
}

async function updateProjectMembers(projectId: string | number, members: ProjectMember[]): Promise<any> {
  console.log('Faking updateProjectMembers for projectId:', projectId, members)
  return Promise.resolve()
}
// END FAKE API calls

interface Props {
  modelValue: boolean
  projectId: string | number
}

const props = defineProps<Props>()
const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: val => emit('update:modelValue', val),
})

const loading = ref(false)
const allUsers = ref<User[]>([])
const projectMembers = ref<ProjectMember[]>([])
const allUsersPage = ref(1)
const allUsersPageSize = 20
const allUsersTotal = ref(0)
const allUsersLoading = ref(false)
const allUsersSearch = ref('')

const memberRoles = [
  { label: '项目所有者', value: 'OWNER' },
  { label: '编辑者', value: 'EDITOR' },
  { label: '协作者', value: 'COLLABORATOR' },
  { label: '仅查看', value: 'VIEWER' },
]

watch(visible, (val) => {
  if (val) {
    loadProjectMembers()
    resetAllUsers()
    loadAllUsers()
  }
})

function isMember(user: User) {
  return projectMembers.value.some(m => m.userId === user.id)
}

async function loadProjectMembers() {
  loading.value = true
  try {
    const res = await getProjectMembers(props.projectId)
    projectMembers.value = res.data || []
  }
  catch (e) {
    ElMessage.error('加载项目成员失败')
  }
  finally {
    loading.value = false
  }
}

async function loadAllUsers() {
  if (allUsersLoading.value) return
  allUsersLoading.value = true
  try {
    const res = await getUserList({
      current: allUsersPage.value,
      size: allUsersPageSize,
      keyword: allUsersSearch.value,
    })
    allUsers.value.push(...res.data.records)
    allUsersTotal.value = res.data.total
    allUsersPage.value++
  }
  catch (e) {
    ElMessage.error('加载用户列表失败')
  }
  finally {
    allUsersLoading.value = false
  }
}

function handleAllUsersSearch() {
  resetAllUsers()
  loadAllUsers()
}

function resetAllUsers() {
  allUsers.value = []
  allUsersPage.value = 1
  allUsersTotal.value = 0
}

function handleAddMember(user: User) {
  if (!isMember(user)) {
    projectMembers.value.push({
      userId: user.id,
      userName: user.name,
      orgName: user.orgName || '',
      position: user.position || '',
      identity: 'COLLABORATOR',
      picture: user.avatar || '',
    })
  }
}

function handleRemoveMember(userId: number) {
  projectMembers.value = projectMembers.value.filter(m => m.userId !== userId)
}

async function handleSave() {
  loading.value = true
  try {
    await updateProjectMembers(props.projectId, projectMembers.value)
    ElMessage.success('成员更新成功')
    emit('success')
    visible.value = false
  }
  catch (e) {
    ElMessage.error('保存失败')
  }
  finally {
    loading.value = false
  }
}

</script>

<template>
  <el-dialog v-model="visible" title="项目成员管理" width="800px" :close-on-click-modal="false">
    <div v-loading="loading" class="member-manager">
      <div class="panel available-members">
        <div class="panel-header">
          <span>公司成员</span>
          <el-input v-model="allUsersSearch" placeholder="搜索成员" :prefix-icon="Search" @change="handleAllUsersSearch" clearable />
        </div>
        <div class="panel-body">
          <el-scrollbar>
            <ul v-infinite-scroll="loadAllUsers" :infinite-scroll-disabled="allUsers.length >= allUsersTotal">
              <li v-for="user in allUsers" :key="user.id" class="user-item">
                <el-avatar :size="32" :src="user.avatar">{{ user.name[0] }}</el-avatar>
                <div class="user-info">
                  <span class="name">{{ user.name }}</span>
                  <span class="org">{{ user.orgName }} / {{ user.position }}</span>
                </div>
                <el-button
                  type="primary"
                  circle
                  :icon="Plus"
                  :disabled="isMember(user)"
                  @click="handleAddMember(user)"
                />
              </li>
            </ul>
            <p v-if="allUsersLoading" class="list-loading">加载中...</p>
            <p v-if="!allUsersLoading && allUsers.length >= allUsersTotal && allUsers.length > 0" class="list-loading">没有更多了</p>
            <el-empty v-if="!allUsersLoading && allUsers.length === 0" description="未找到用户" />
          </el-scrollbar>
        </div>
      </div>

      <div class="panel project-members">
        <div class="panel-header">
          <span>项目成员 ({{ projectMembers.length }})</span>
        </div>
        <div class="panel-body">
          <el-scrollbar>
            <ul>
              <li v-for="member in projectMembers" :key="member.userId" class="user-item">
                 <el-avatar :size="32" :src="member.picture">{{ member.userName[0] }}</el-avatar>
                <div class="user-info">
                  <span class="name">{{ member.userName }}</span>
                  <span class="org">{{ member.orgName }} / {{ member.position }}</span>
                </div>
                <el-select v-model="member.identity" placeholder="角色" class="role-select">
                  <el-option v-for="role in memberRoles" :key="role.value" :label="role.label" :value="role.value" />
                </el-select>
                <el-button
                  type="danger"
                  circle
                  :icon="Close"
                  @click="handleRemoveMember(member.userId)"
                />
              </li>
            </ul>
            <el-empty v-if="projectMembers.length === 0" description="请从左侧添加项目成员" />
          </el-scrollbar>
        </div>
      </div>
    </div>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleSave" :loading="loading">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped lang="scss">
.member-manager {
  display: flex;
  gap: 20px;
  height: 500px;
  border-top: 1px solid var(--el-border-color-light);
  border-bottom: 1px solid var(--el-border-color-light);
  padding: 10px 0;
}

.panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  font-weight: bold;

  .el-input {
    width: 180px;
  }
}

.panel-body {
  flex: 1;
  overflow: hidden;
  ul {
    padding: 0;
    margin: 0;
    list-style: none;
  }
  .list-loading {
    text-align: center;
    color: #999;
    font-size: 14px;
    padding: 10px 0;
  }
}

.user-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  gap: 12px;
  &:hover {
    background-color: var(--el-fill-color-light);
  }
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  .name {
    font-size: 14px;
  }
  .org {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}

.role-select {
  width: 120px;
  margin: 0 10px;
}
</style> 