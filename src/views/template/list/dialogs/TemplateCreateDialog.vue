<template>
  <el-dialog
    :title="initialData?.id ? '编辑模板' : '新建模板'"
    v-model="visible"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="80px"
      class="template-form"
    >
      <el-form-item label="名称" prop="title">
        <el-input v-model="formData.title" placeholder="请输入模板名称" />
      </el-form-item>

      <el-form-item label="分类" prop="categoryName">
        <el-select
          v-model="formData.categoryName"
          placeholder="请选择分类"
          class="w-full"
        >
          <el-option
            v-for="item in categoryOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-select
          v-model="formData.status"
          placeholder="请选择状态"
          class="w-full"
        >
          <el-option
            v-for="item in statusOptions"
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
          class="w-full"
          filterable
          remote
          :remote-method="handleSearchUser"
          :loading="userSearchLoading"
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

      <el-form-item label="版本" prop="version">
        <el-input v-model="formData.version" placeholder="请输入版本号" />
      </el-form-item>

      <el-form-item label="描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="4"
          placeholder="请输入模板描述"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { Template } from '@/api/template'
import { getTemplateCategoryOptions, getTemplateStatusOptions, searchUsers } from '@/api/template'
import type { OptionItem } from '@/components/BaseList/types'

const props = defineProps<{
  initialData?: Partial<Template>
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'submit', data: Template): void
  (e: 'error', error: Error): void
}>()

// 表单实例
const formRef = ref<FormInstance>()
const visible = ref(false)
const submitting = ref(false)

// 表单数据
const formData = reactive<Partial<Template>>({
  title: '',
  categoryName: '',
  status: '',
  owner: '',
  version: '1.0.0',
  description: ''
})

// 表单校验规则
const rules = reactive<FormRules>({
  title: [
    { required: true, message: '请输入模板名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  categoryName: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ],
  owner: [
    { required: true, message: '请选择负责人', trigger: 'change' }
  ],
  version: [
    { required: true, message: '请输入版本号', trigger: 'blur' },
    { pattern: /^\d+\.\d+\.\d+$/, message: '版本号格式为 x.y.z', trigger: 'blur' }
  ]
})

// 选项数据
const categoryOptions = ref<OptionItem[]>([])
const statusOptions = ref<OptionItem[]>([])
const userOptions = ref<OptionItem[]>([])
const userSearchLoading = ref(false)

// 初始化选项数据
const initOptions = async () => {
  try {
    const [categories, statuses] = await Promise.all([
      getTemplateCategoryOptions(),
      getTemplateStatusOptions()
    ])
    categoryOptions.value = categories
    statusOptions.value = statuses
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
      userOptions.value = users
    } catch (error) {
      console.error('Failed to search users:', error)
    } finally {
      userSearchLoading.value = false
    }
  } else {
    userOptions.value = []
  }
}

// 初始化表单数据
const initFormData = () => {
  if (props.initialData) {
    Object.assign(formData, props.initialData)
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    emit('submit', formData as Template)
    handleClose()
  } catch (error) {
    emit('error', error as Error)
  } finally {
    submitting.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  formRef.value?.resetFields()
  visible.value = false
  emit('update:modelValue', false)
}

// 监听初始数据变化
watch(() => props.initialData, () => {
  if (props.initialData) {
    initFormData()
    visible.value = true
  }
}, { immediate: true })

// 组件挂载时初始化选项数据
onMounted(() => {
  initOptions()
})
</script>

<style lang="scss" scoped>
.template-form {
  max-height: 60vh;
  overflow-y: auto;
  padding: 0 20px;
}

.user-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-select) {
  width: 100%;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style> 