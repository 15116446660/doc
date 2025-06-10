<template>
  <el-dialog
    :title="initialData?.id ? '编辑项目' : '新建项目'"
    v-model="dialogVisible"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      class="project-form"
    >
      <el-form-item label="项目名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入项目名称" />
      </el-form-item>

      <el-form-item label="项目编号" prop="projectCode">
        <el-input v-model="formData.projectCode" placeholder="请输入项目编号" />
      </el-form-item>

      <el-form-item label="项目类型" prop="type">
        <el-select v-model="formData.type" placeholder="请选择项目类型" class="w-full">
          <el-option
            v-for="item in projectTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="所属部门" prop="department">
        <el-select v-model="formData.department" placeholder="请选择所属部门" class="w-full">
          <el-option
            v-for="item in departmentOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="负责人" prop="owner">
        <el-select
          v-model="formData.owner"
          placeholder="请选择负责人"
          filterable
          remote
          :remote-method="handleSearchUser"
          :loading="userSearchLoading"
          class="w-full"
        >
          <el-option
            v-for="item in userOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          >
            <div class="user-option">
              <el-avatar :size="24">{{ item.label.charAt(0) }}</el-avatar>
              <span>{{ item.label }}</span>
            </div>
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="项目时间" required>
        <el-col :span="11">
          <el-form-item prop="startDate">
            <el-date-picker
              v-model="formData.startDate"
              type="date"
              placeholder="开始日期"
              class="w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :span="2" class="text-center">
          <span class="text-gray-400">至</span>
        </el-col>
        <el-col :span="11">
          <el-form-item prop="endDate">
            <el-date-picker
              v-model="formData.endDate"
              type="date"
              placeholder="结束日期"
              class="w-full"
            />
          </el-form-item>
        </el-col>
      </el-form-item>

      <el-form-item label="项目描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入项目描述"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ initialData?.id ? '保存' : '创建' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import type { FormInstance } from 'element-plus'
import type { Project } from '@/types/document'
import { ElMessage } from 'element-plus'
import { searchUsers } from '@/api/user'
import { getProjectTypeOptions, getDepartmentOptions } from '@/api/document'

const props = defineProps<{
  modelValue: boolean
  initialData?: Partial<Project>
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'submit', data: Project): void
  (e: 'error', error: Error): void
}>()

// 表单实例
const formRef = ref<FormInstance>()

// 对话框可见性
const dialogVisible = ref(props.modelValue)

// 监听对话框可见性
watch(() => props.modelValue, (val) => {
  dialogVisible.value = val
})

// 监听内部对话框可见性
watch(() => dialogVisible.value, (val) => {
  emit('update:modelValue', val)
})

// 表单数据
const formData = reactive<Partial<Project>>({
  name: '',
  projectCode: '',
  type: '',
  department: '',
  owner: '',
  startDate: '',
  endDate: '',
  description: '',
  status: '未开始'
})

// 表单校验规则
const formRules = {
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  projectCode: [
    { required: true, message: '请输入项目编号', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择项目类型', trigger: 'change' }
  ],
  department: [
    { required: true, message: '请选择所属部门', trigger: 'change' }
  ],
  owner: [
    { required: true, message: '请选择负责人', trigger: 'change' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' }
  ]
}

// 项目类型选项
const projectTypeOptions = ref<{ label: string; value: string }[]>([])
// 部门选项
const departmentOptions = ref<{ label: string; value: string }[]>([])
// 用户选项
const userOptions = ref<{ label: string; value: string }[]>([])
// 用户搜索加载状态
const userSearchLoading = ref(false)

// 初始化选项数据
const initOptions = async () => {
  try {
    const [types, departments] = await Promise.all([
      getProjectTypeOptions(),
      getDepartmentOptions()
    ])
    projectTypeOptions.value = types
    departmentOptions.value = departments
  } catch (error) {
    console.error('Failed to load options:', error)
  }
}

// 搜索用户
const handleSearchUser = async (query: string) => {
  if (query) {
    userSearchLoading.value = true
    try {
      const users = await searchUsers(query)
      userOptions.value = users.map(user => ({
        label: user.name,
        value: user.id
      }))
    } catch (error) {
      console.error('Failed to search users:', error)
    } finally {
      userSearchLoading.value = false
    }
  } else {
    userOptions.value = []
  }
}

// 提交状态
const submitting = ref(false)

// 处理提交
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    // 发射提交事件
    emit('submit', formData as Project)
    
    // 关闭对话框
    dialogVisible.value = false
  } catch (error) {
    emit('error', error as Error)
  } finally {
    submitting.value = false
  }
}

// 处理关闭
const handleClose = () => {
  formRef.value?.resetFields()
  Object.assign(formData, {
    name: '',
    projectCode: '',
    type: '',
    department: '',
    owner: '',
    startDate: '',
    endDate: '',
    description: '',
    status: '未开始'
  })
}

// 监听初始数据变化
watch(() => props.initialData, (val) => {
  if (val) {
    Object.assign(formData, val)
  }
}, { immediate: true })

// 初始化
initOptions()
</script>

<style lang="scss" scoped>
.project-form {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 16px;
}

.user-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.w-full {
  width: 100%;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-form-item__content) {
  flex-wrap: nowrap;
}
</style> 