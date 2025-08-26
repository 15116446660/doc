<template>
  <div class="category-management">
    <div class="page-header">
      <div class="header-left">
        <h2>品类管理</h2>
        <p class="header-desc">管理项目品类信息</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建品类
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
        <el-select
          v-model="searchForm.departmentId"
          placeholder="选择部门"
          style="width: 200px"
          clearable
          @change="handleSearch"
        >
          <el-option
            v-for="dept in departmentOptions"
            :key="dept.id"
            :label="dept.name"
            :value="dept.id"
          />
        </el-select>
        <el-input
          v-model="searchForm.name"
          placeholder="搜索品类名称"
          style="width: 200px; margin-left: 10px"
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

      <!-- 品类表格 -->
      <div class="table-container">
        <el-table
          v-loading="loading"
          :data="tableData"
          stripe
          border
        >
          <el-table-column prop="name" label="品类名称" min-width="200">
            <template #default="{ row }">
              <div class="category-name">
                <el-icon class="category-icon" :style="{ color: row.color || '#409EFF' }">
                  <Collection />
                </el-icon>
                <span>{{ row.name }}</span>
                <el-tag v-if="row.code" size="small" type="info" style="margin-left: 8px">
                  {{ row.code }}
                </el-tag>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="departmentName" label="所属部门" width="150" />

          <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />

          <el-table-column prop="managerName" label="负责人" width="120">
            <template #default="{ row }">
              <span>{{ row.managerName || '-' }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="icon" label="图标" width="80" align="center">
            <template #default="{ row }">
              <el-icon v-if="row.icon" :style="{ color: row.color }">
                <component :is="row.icon" />
              </el-icon>
              <span v-else>-</span>
            </template>
          </el-table-column>

          <el-table-column prop="color" label="颜色" width="80" align="center">
            <template #default="{ row }">
              <div
                v-if="row.color"
                class="color-block"
                :style="{ backgroundColor: row.color }"
              ></div>
              <span v-else>-</span>
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

          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button type="text" size="small" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button type="text" size="small" @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
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
        <el-form-item label="品类名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入品类名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="品类编码" prop="code">
          <el-input
            v-model="formData.code"
            placeholder="请输入品类编码（可选）"
            maxlength="50"
          />
        </el-form-item>

        <el-form-item label="所属部门" prop="departmentId">
          <el-select
            v-model="formData.departmentId"
            placeholder="请选择所属部门"
            style="width: 100%"
          >
            <el-option
              v-for="dept in departmentOptions"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="品类描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入品类描述（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="图标" prop="icon">
              <el-input
                v-model="formData.icon"
                placeholder="图标名称（可选）"
                maxlength="100"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="颜色" prop="color">
              <el-color-picker v-model="formData.color" />
            </el-form-item>
          </el-col>
        </el-row>

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
  Collection
} from '@element-plus/icons-vue'
import type { ProjectCategoryEntity, Department } from '@/api/project'
import {
  getCategoriesByDepartment,
  getDepartmentTree,
  createProjectCategory,
  updateProjectCategory,
  deleteProjectCategory
} from '@/api/project'

// Refs
const formRef = ref<InstanceType<typeof ElForm>>()

// Reactive data
const loading = ref(false)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const userLoading = ref(false)
const tableData = ref<ProjectCategoryEntity[]>([])
const departmentOptions = ref<Department[]>([])
const userOptions = ref<any[]>([])

const searchForm = reactive({
  departmentId: undefined as number | undefined,
  name: '',
  enabled: undefined as boolean | undefined
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const formData = reactive({
  id: undefined as number | undefined,
  name: '',
  code: '',
  description: '',
  departmentId: undefined as number | undefined,
  managerId: undefined as number | undefined,
  sortOrder: 0,
  enabled: true,
  icon: '',
  color: ''
})

// 计算属性
const isEdit = computed(() => !!formData.id)
const dialogTitle = computed(() => isEdit.value ? '编辑品类' : '创建品类')

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入品类名称', trigger: 'blur' },
    { min: 1, max: 100, message: '品类名称长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  departmentId: [
    { required: true, message: '请选择所属部门', trigger: 'change' }
  ],
  code: [
    { max: 50, message: '品类编码长度不能超过 50 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '品类描述长度不能超过 500 个字符', trigger: 'blur' }
  ],
  sortOrder: [
    { type: 'number', min: 0, message: '排序号不能小于 0', trigger: 'blur' }
  ]
}

// 生命周期
onMounted(() => {
  loadDepartments()
  loadData()
})

// 方法
const loadDepartments = async () => {
  try {
    const response = await getDepartmentTree()
    departmentOptions.value = flattenDepartments(response.data || [])
  } catch (error) {
    console.error('加载部门数据失败:', error)
  }
}

const flattenDepartments = (departments: Department[]): Department[] => {
  const result: Department[] = []
  const flatten = (depts: Department[], level = 0) => {
    depts.forEach(dept => {
      result.push({
        ...dept,
        name: '　'.repeat(level) + dept.name
      })
      if (dept.children && dept.children.length > 0) {
        flatten(dept.children, level + 1)
      }
    })
  }
  flatten(departments)
  return result
}

const loadData = async () => {
  try {
    loading.value = true
    // 这里应该调用分页查询API
    // const response = await getCategoriesWithPagination(searchForm, pagination)
    // tableData.value = response.data.records || []
    // pagination.total = response.data.total || 0
    
    // 模拟数据
    tableData.value = []
    pagination.total = 0
  } catch (error) {
    console.error('加载品类数据失败:', error)
    ElMessage.error('加载品类数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleResetSearch = () => {
  searchForm.departmentId = undefined
  searchForm.name = ''
  searchForm.enabled = undefined
  pagination.page = 1
  loadData()
}

const handleRefresh = () => {
  loadData()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadData()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadData()
}

const handleCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: ProjectCategoryEntity) => {
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    code: row.code || '',
    description: row.description || '',
    departmentId: row.departmentId,
    managerId: row.managerId,
    sortOrder: row.sortOrder || 0,
    enabled: row.enabled !== false,
    icon: row.icon || '',
    color: row.color || ''
  })
  dialogVisible.value = true
}

const handleDelete = async (row: ProjectCategoryEntity) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除品类"${row.name}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteProjectCategory(row.id)
    ElMessage.success('删除成功')
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleToggleEnabled = async (row: ProjectCategoryEntity) => {
  try {
    row.toggleLoading = true
    await updateProjectCategory(row.id, { enabled: row.enabled })
    ElMessage.success(row.enabled ? '启用成功' : '禁用成功')
  } catch (error) {
    console.error('状态切换失败:', error)
    ElMessage.error('状态切换失败')
    row.enabled = !row.enabled
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
      await updateProjectCategory(formData.id!, formData)
      ElMessage.success('更新成功')
    } else {
      await createProjectCategory(formData)
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
    departmentId: undefined,
    managerId: undefined,
    sortOrder: 0,
    enabled: true,
    icon: '',
    color: ''
  })
}

const searchUsers = async (query: string) => {
  if (!query) {
    userOptions.value = []
    return
  }

  try {
    userLoading.value = true
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

const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}
</script>

<style scoped>
.category-management {
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

.category-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.category-icon {
  font-size: 16px;
}

.color-block {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  border: 1px solid var(--el-border-color);
}

.pagination-container {
  margin-top: 16px;
  text-align: right;
}

.dialog-footer {
  text-align: right;
}
</style>
