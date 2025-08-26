<template>
  <div class="department-management">
    <div class="page-header">
      <div class="header-left">
        <h2>部门管理</h2>
        <p class="header-desc">管理组织架构中的部门信息</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建部门
        </el-button>
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <div class="page-content">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchForm.name"
          placeholder="搜索部门名称"
          style="width: 200px"
          clearable
          @input="handleSearch"
        />
        <el-select
          v-model="searchForm.enabled"
          placeholder="状态"
          style="width: 120px; margin-left: 10px"
          clearable
          @change="handleSearch"
        >
          <el-option label="启用" :value="true" />
          <el-option label="禁用" :value="false" />
        </el-select>
        <el-button type="primary" @click="handleSearch" style="margin-left: 10px">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button @click="handleResetSearch">重置</el-button>
      </div>

      <!-- 部门树表格 -->
      <div class="table-container">
        <el-table
          v-loading="loading"
          :data="tableData"
          row-key="id"
          :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
          :default-expand-all="false"
          stripe
          border
        >
          <el-table-column prop="name" label="部门名称" min-width="200">
            <template #default="{ row }">
              <div class="department-name">
                <el-icon class="dept-icon">
                  <OfficeBuilding />
                </el-icon>
                <span>{{ row.name }}</span>
                <el-tag v-if="row.code" size="small" type="info" style="margin-left: 8px">
                  {{ row.code }}
                </el-tag>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />

          <el-table-column prop="managerName" label="负责人" width="120">
            <template #default="{ row }">
              <span>{{ row.managerName || '-' }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="level" label="层级" width="80" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="getLevelTagType(row.level)">
                L{{ row.level }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="sortOrder" label="排序" width="80" align="center" />

          <el-table-column prop="enabled" label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.enabled"
                @change="handleToggleEnabled(row)"
                :loading="row.toggleLoading"
              />
            </template>
          </el-table-column>

          <el-table-column prop="createdAt" label="创建时间" width="160">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>

          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="text" size="small" @click="handleAddChild(row)">
                <el-icon><Plus /></el-icon>
                新建子部门
              </el-button>
              <el-button type="text" size="small" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button
                type="text"
                size="small"
                @click="handleDelete(row)"
                :disabled="row.children && row.children.length > 0"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        @submit.prevent
      >
        <el-form-item label="部门名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入部门名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="部门编码" prop="code">
          <el-input
            v-model="formData.code"
            placeholder="请输入部门编码（可选）"
            maxlength="50"
          />
        </el-form-item>

        <el-form-item label="部门描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入部门描述（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="父部门" prop="parentId">
          <el-tree-select
            v-model="formData.parentId"
            :data="departmentOptions"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="请选择父部门（可选）"
            clearable
            check-strictly
            :disabled="isEdit && formData.id === formData.parentId"
          />
        </el-form-item>

        <el-form-item label="负责人" prop="managerId">
          <el-select
            v-model="formData.managerId"
            placeholder="请选择负责人（可选）"
            style="width: 100%"
            filterable
            remote
            :remote-method="searchUsers"
            :loading="userLoading"
            clearable
          >
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="user.name"
              :value="user.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="排序号" prop="sortOrder">
          <el-input-number
            v-model="formData.sortOrder"
            :min="0"
            style="width: 100%"
            placeholder="排序号，数字越小越靠前"
          />
        </el-form-item>

        <el-form-item label="状态" prop="enabled">
          <el-switch
            v-model="formData.enabled"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleDialogClose">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
            {{ isEdit ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElForm } from 'element-plus'
import {
  Plus,
  Refresh,
  Search,
  Edit,
  Delete,
  OfficeBuilding
} from '@element-plus/icons-vue'
import type { Department } from '@/api/project'
import {
  getDepartmentTree,
  createDepartment,
  updateDepartment,
  deleteDepartment,
  moveDepartment
} from '@/api/project'

// Refs
const formRef = ref<InstanceType<typeof ElForm>>()

// Reactive data
const loading = ref(false)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const userLoading = ref(false)
const tableData = ref<Department[]>([])
const departmentOptions = ref<Department[]>([])
const userOptions = ref<any[]>([])

const searchForm = reactive({
  name: '',
  enabled: undefined as boolean | undefined
})

const formData = reactive({
  id: undefined as number | undefined,
  name: '',
  code: '',
  description: '',
  parentId: undefined as number | undefined,
  managerId: undefined as number | undefined,
  sortOrder: 0,
  enabled: true
})

// 计算属性
const isEdit = computed(() => !!formData.id)
const dialogTitle = computed(() => isEdit.value ? '编辑部门' : '创建部门')

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入部门名称', trigger: 'blur' },
    { min: 1, max: 100, message: '部门名称长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  code: [
    { max: 50, message: '部门编码长度不能超过 50 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '部门描述长度不能超过 500 个字符', trigger: 'blur' }
  ],
  sortOrder: [
    { type: 'number', min: 0, message: '排序号不能小于 0', trigger: 'blur' }
  ]
}

// 生命周期
onMounted(() => {
  loadData()
})

// 方法
const loadData = async () => {
  try {
    loading.value = true
    const response = await getDepartmentTree()
    tableData.value = response.data || []
    departmentOptions.value = response.data || []
  } catch (error) {
    console.error('加载部门数据失败:', error)
    ElMessage.error('加载部门数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  // 实现搜索逻辑
  loadData()
}

const handleResetSearch = () => {
  searchForm.name = ''
  searchForm.enabled = undefined
  loadData()
}

const handleRefresh = () => {
  loadData()
}

const handleCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const handleAddChild = (row: Department) => {
  resetForm()
  formData.parentId = row.id
  dialogVisible.value = true
}

const handleEdit = (row: Department) => {
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    code: row.code || '',
    description: row.description || '',
    parentId: row.parentId,
    managerId: row.managerId,
    sortOrder: row.sortOrder || 0,
    enabled: row.enabled !== false
  })
  dialogVisible.value = true
}

const handleDelete = async (row: Department) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除部门"${row.name}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteDepartment(row.id)
    ElMessage.success('删除成功')
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleToggleEnabled = async (row: Department) => {
  try {
    row.toggleLoading = true
    await updateDepartment(row.id, { enabled: row.enabled })
    ElMessage.success(row.enabled ? '启用成功' : '禁用成功')
  } catch (error) {
    console.error('状态切换失败:', error)
    ElMessage.error('状态切换失败')
    row.enabled = !row.enabled // 恢复原状态
  } finally {
    row.toggleLoading = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitLoading.value = true

    if (isEdit.value) {
      await updateDepartment(formData.id!, formData)
      ElMessage.success('更新成功')
    } else {
      await createDepartment(formData)
      ElMessage.success('创建成功')
    }

    dialogVisible.value = false
    await loadData()
  } catch (error) {
    if (error !== false) {
      console.error('提交失败:', error)
      ElMessage.error('提交失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
  dialogVisible.value = false
}

const resetForm = () => {
  Object.assign(formData, {
    id: undefined,
    name: '',
    code: '',
    description: '',
    parentId: undefined,
    managerId: undefined,
    sortOrder: 0,
    enabled: true
  })
}

const searchUsers = async (query: string) => {
  if (!query) {
    userOptions.value = []
    return
  }

  try {
    userLoading.value = true
    // 这里应该调用用户搜索API
    // const response = await searchUsers({ name: query })
    // userOptions.value = response.data || []
    
    // 模拟数据
    userOptions.value = [
      { id: 1, name: '张三' },
      { id: 2, name: '李四' },
      { id: 3, name: '王五' }
    ].filter(user => user.name.includes(query))
  } catch (error) {
    console.error('搜索用户失败:', error)
  } finally {
    userLoading.value = false
  }
}

const getLevelTagType = (level: number) => {
  switch (level) {
    case 1:
      return 'primary'
    case 2:
      return 'success'
    case 3:
      return 'warning'
    default:
      return 'info'
  }
}

const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}
</script>

<style scoped>
.department-management {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid var(--el-border-color);
}

.header-left h2 {
  margin: 0 0 4px 0;
  font-size: 20px;
  font-weight: 600;
}

.header-desc {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.header-right {
  display: flex;
  gap: 8px;
}

.page-content {
  flex: 1;
  padding: 16px 24px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.search-bar {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.table-container {
  flex: 1;
  overflow: auto;
}

.department-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dept-icon {
  color: var(--el-color-primary);
  font-size: 16px;
}

.dialog-footer {
  text-align: right;
}
</style>
