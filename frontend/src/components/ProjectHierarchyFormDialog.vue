<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      @submit.prevent
    >
      <!-- 基础信息 -->
      <el-form-item label="名称" prop="name">
        <el-input
          v-model="formData.name"
          placeholder="请输入名称"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="编码" prop="code">
        <el-input
          v-model="formData.code"
          placeholder="请输入编码（可选）"
          maxlength="50"
        />
      </el-form-item>

      <el-form-item label="描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入描述（可选）"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <!-- 部门特有字段 -->
      <template v-if="nodeType === 'department'">
        <el-form-item label="父部门" prop="parentId">
          <el-tree-select
            v-model="formData.parentId"
            :data="departmentOptions"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="请选择父部门（可选）"
            clearable
            check-strictly
          />
        </el-form-item>
      </template>

      <!-- 品类特有字段 -->
      <template v-if="nodeType === 'category'">
        <el-form-item label="所属部门" prop="departmentId">
          <el-tree-select
            v-model="formData.departmentId"
            :data="departmentOptions"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="请选择所属部门"
            check-strictly
          />
        </el-form-item>

        <el-form-item label="图标" prop="icon">
          <el-input
            v-model="formData.icon"
            placeholder="请输入图标名称（可选）"
            maxlength="100"
          />
        </el-form-item>

        <el-form-item label="颜色" prop="color">
          <el-color-picker v-model="formData.color" />
        </el-form-item>
      </template>

      <!-- 子品类特有字段 -->
      <template v-if="nodeType === 'subcategory'">
        <el-form-item label="所属品类" prop="categoryId">
          <el-select
            v-model="formData.categoryId"
            placeholder="请选择所属品类"
            style="width: 100%"
          >
            <el-option
              v-for="category in categoryOptions"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="图标" prop="icon">
          <el-input
            v-model="formData.icon"
            placeholder="请输入图标名称（可选）"
            maxlength="100"
          />
        </el-form-item>

        <el-form-item label="颜色" prop="color">
          <el-color-picker v-model="formData.color" />
        </el-form-item>
      </template>

      <!-- 项目特有字段 -->
      <template v-if="nodeType === 'project'">
        <el-form-item label="所属子品类" prop="subcategoryId">
          <el-select
            v-model="formData.subcategoryId"
            placeholder="请选择所属子品类"
            style="width: 100%"
          >
            <el-option
              v-for="subcategory in subcategoryOptions"
              :key="subcategory.id"
              :label="subcategory.name"
              :value="subcategory.id"
            />
          </el-select>
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="formData.status" style="width: 100%">
                <el-option label="草稿" value="DRAFT" />
                <el-option label="进行中" value="IN_PROGRESS" />
                <el-option label="已完成" value="COMPLETED" />
                <el-option label="已归档" value="ARCHIVED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="formData.priority" style="width: 100%">
                <el-option label="低" value="LOW" />
                <el-option label="中" value="MEDIUM" />
                <el-option label="高" value="HIGH" />
                <el-option label="紧急" value="URGENT" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="预算" prop="budget">
          <el-input-number
            v-model="formData.budget"
            :min="0"
            :precision="2"
            style="width: 100%"
            placeholder="请输入项目预算"
          />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="计划开始" prop="plannedStartTime">
              <el-date-picker
                v-model="formData.plannedStartTime"
                type="datetime"
                placeholder="选择计划开始时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划结束" prop="plannedEndTime">
              <el-date-picker
                v-model="formData.plannedEndTime"
                type="datetime"
                placeholder="选择计划结束时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="标签" prop="tags">
          <el-input
            v-model="formData.tags"
            placeholder="请输入项目标签，多个标签用逗号分隔"
            maxlength="500"
          />
        </el-form-item>
      </template>

      <!-- 负责人 -->
      <el-form-item label="负责人" prop="managerId">
        <el-select
          v-model="formData.managerId"
          placeholder="请选择负责人（可选）"
          style="width: 100%"
          filterable
          remote
          :remote-method="searchUsers"
          :loading="userLoading"
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
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElForm } from 'element-plus'
import type { ProjectHierarchyTreeNode, Department, ProjectCategoryEntity, ProjectSubcategoryEntity } from '@/api/project'
import {
  getDepartmentTree,
  getCategoriesByDepartment,
  getSubcategoriesByCategory,
  createDepartment,
  updateDepartment,
  createProjectCategory,
  updateProjectCategory,
  createProjectSubcategory,
  updateProjectSubcategory,
  createProject,
  updateProject
} from '@/api/project'

// Props
interface Props {
  modelValue: boolean
  nodeType: string
  parentNode?: ProjectHierarchyTreeNode | null
  editData?: ProjectHierarchyTreeNode | null
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

// Refs
const formRef = ref<InstanceType<typeof ElForm>>()

// Reactive data
const submitLoading = ref(false)
const userLoading = ref(false)
const departmentOptions = ref<Department[]>([])
const categoryOptions = ref<ProjectCategoryEntity[]>([])
const subcategoryOptions = ref<ProjectSubcategoryEntity[]>([])
const userOptions = ref<any[]>([])

const formData = reactive({
  name: '',
  code: '',
  description: '',
  parentId: undefined as number | undefined,
  departmentId: undefined as number | undefined,
  categoryId: undefined as number | undefined,
  subcategoryId: undefined as number | undefined,
  managerId: undefined as number | undefined,
  sortOrder: 0,
  enabled: true,
  icon: '',
  color: '',
  status: 'DRAFT',
  priority: 'MEDIUM',
  budget: undefined as number | undefined,
  plannedStartTime: undefined as Date | undefined,
  plannedEndTime: undefined as Date | undefined,
  tags: ''
})

// 计算属性
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const isEdit = computed(() => !!props.editData)

const dialogTitle = computed(() => {
  const typeText = getNodeTypeText(props.nodeType)
  return isEdit.value ? `编辑${typeText}` : `创建${typeText}`
})

// 表单验证规则
const formRules = computed(() => {
  const rules: any = {
    name: [
      { required: true, message: '请输入名称', trigger: 'blur' },
      { min: 1, max: 100, message: '名称长度在 1 到 100 个字符', trigger: 'blur' }
    ],
    code: [
      { max: 50, message: '编码长度不能超过 50 个字符', trigger: 'blur' }
    ],
    description: [
      { max: 500, message: '描述长度不能超过 500 个字符', trigger: 'blur' }
    ],
    sortOrder: [
      { type: 'number', min: 0, message: '排序号不能小于 0', trigger: 'blur' }
    ]
  }

  // 根据节点类型添加特定验证规则
  switch (props.nodeType) {
    case 'category':
      rules.departmentId = [
        { required: true, message: '请选择所属部门', trigger: 'change' }
      ]
      break
    case 'subcategory':
      rules.categoryId = [
        { required: true, message: '请选择所属品类', trigger: 'change' }
      ]
      break
    case 'project':
      rules.subcategoryId = [
        { required: true, message: '请选择所属子品类', trigger: 'change' }
      ]
      break
  }

  return rules
})

// 监听器
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    initForm()
    loadOptions()
  }
})

watch(() => formData.departmentId, (newVal) => {
  if (newVal && props.nodeType === 'subcategory') {
    loadCategoryOptions(newVal)
  }
})

watch(() => formData.categoryId, (newVal) => {
  if (newVal && props.nodeType === 'project') {
    loadSubcategoryOptions(newVal)
  }
})

// 生命周期
onMounted(() => {
  if (props.modelValue) {
    initForm()
    loadOptions()
  }
})

// 方法
const initForm = () => {
  if (props.editData) {
    // 编辑模式，填充现有数据
    Object.assign(formData, {
      name: props.editData.name || '',
      code: props.editData.code || '',
      description: props.editData.description || '',
      parentId: props.editData.parentId,
      departmentId: props.editData.parentId, // 对于品类，parentId就是departmentId
      categoryId: props.editData.parentId, // 对于子品类，parentId就是categoryId
      subcategoryId: props.editData.parentId, // 对于项目，parentId就是subcategoryId
      managerId: props.editData.managerId,
      sortOrder: props.editData.sortOrder || 0,
      enabled: props.editData.enabled !== false,
      icon: props.editData.icon || '',
      color: props.editData.color || '',
      status: props.editData.projectStatus || 'DRAFT',
      priority: props.editData.projectPriority || 'MEDIUM',
      budget: props.editData.projectBudget,
      plannedStartTime: props.editData.plannedStartTime ? new Date(props.editData.plannedStartTime) : undefined,
      plannedEndTime: props.editData.plannedEndTime ? new Date(props.editData.plannedEndTime) : undefined,
      tags: props.editData.tags || ''
    })
  } else {
    // 创建模式，重置表单
    Object.assign(formData, {
      name: '',
      code: '',
      description: '',
      parentId: props.parentNode?.id,
      departmentId: props.parentNode?.id,
      categoryId: props.parentNode?.id,
      subcategoryId: props.parentNode?.id,
      managerId: undefined,
      sortOrder: 0,
      enabled: true,
      icon: '',
      color: '',
      status: 'DRAFT',
      priority: 'MEDIUM',
      budget: undefined,
      plannedStartTime: undefined,
      plannedEndTime: undefined,
      tags: ''
    })
  }
}

const loadOptions = async () => {
  try {
    // 加载部门选项
    const deptResponse = await getDepartmentTree()
    departmentOptions.value = deptResponse.data || []

    // 根据节点类型加载相应选项
    if (props.nodeType === 'subcategory' && formData.departmentId) {
      await loadCategoryOptions(formData.departmentId)
    } else if (props.nodeType === 'project' && formData.categoryId) {
      await loadSubcategoryOptions(formData.categoryId)
    }
  } catch (error) {
    console.error('加载选项失败:', error)
  }
}

const loadCategoryOptions = async (departmentId: number) => {
  try {
    const response = await getCategoriesByDepartment(departmentId)
    categoryOptions.value = response.data || []
  } catch (error) {
    console.error('加载品类选项失败:', error)
  }
}

const loadSubcategoryOptions = async (categoryId: number) => {
  try {
    const response = await getSubcategoriesByCategory(categoryId)
    subcategoryOptions.value = response.data || []
  } catch (error) {
    console.error('加载子品类选项失败:', error)
  }
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

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitLoading.value = true

    const submitData = { ...formData }
    
    // 根据节点类型调用相应的API
    if (isEdit.value) {
      await updateNode(props.editData!.id, submitData)
    } else {
      await createNode(submitData)
    }

    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    emit('success')
  } catch (error) {
    if (error !== false) { // 表单验证失败时error为false
      console.error('提交失败:', error)
      ElMessage.error('提交失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const createNode = async (data: any) => {
  switch (props.nodeType) {
    case 'department':
      await createDepartment(data)
      break
    case 'category':
      await createProjectCategory(data)
      break
    case 'subcategory':
      await createProjectSubcategory(data)
      break
    case 'project':
      await createProject(data)
      break
  }
}

const updateNode = async (id: number, data: any) => {
  switch (props.nodeType) {
    case 'department':
      await updateDepartment(id, data)
      break
    case 'category':
      await updateProjectCategory(id, data)
      break
    case 'subcategory':
      await updateProjectSubcategory(id, data)
      break
    case 'project':
      await updateProject(id, data)
      break
  }
}

const handleClose = () => {
  formRef.value?.resetFields()
  emit('update:modelValue', false)
}

const getNodeTypeText = (nodeType: string) => {
  switch (nodeType) {
    case 'department':
      return '部门'
    case 'category':
      return '品类'
    case 'subcategory':
      return '子品类'
    case 'project':
      return '项目'
    default:
      return '节点'
  }
}
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}
</style>
