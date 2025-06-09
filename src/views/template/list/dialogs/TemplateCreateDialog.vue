<template>
  <el-dialog
    :title="initialData?.id ? '编辑模板' : '新建模板'"
    v-model="visible"
    width="700px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
      class="template-form"
    >
      <el-form-item prop="templateFile">
        <template #label>
          <div class="custom-label">
            <span>模板文件</span>
            <el-tooltip
              v-if="initialData?.id"
              content="更新时上传文件，会覆盖原来的版本升级为当前上传的版本"
              placement="top"
            >
              <el-icon class="label-help-icon"><QuestionFilled /></el-icon>
            </el-tooltip>
          </div>
        </template>
        <el-upload
          class="template-file-uploader"
          action="#"
          :auto-upload="false"
          :limit="1"
          :on-change="handleFileChange"
        >
          <template #trigger>
            <el-button type="primary">选择文件</el-button>
          </template>
          <template #tip>
            <div class="el-upload__tip">
              {{ formData.templateFile || '请上传模板文件' }}
            </div>
          </template>
        </el-upload>
      </el-form-item>

      <el-form-item prop="categoryName">
        <template #label>
          <div class="custom-label">
            <span>模板类型</span>
          </div>
        </template>
        <el-select
          v-model="formData.categoryName"
          placeholder="请选择模板类型"
          class="w-full"
          :disabled="!!initialData?.id"
        >
          <el-option
            v-for="item in templateTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item prop="templateCode">
        <template #label>
          <div class="custom-label">
            <span>模板编号</span>
          </div>
        </template>
        <el-input 
          v-model="formData.templateCode" 
          placeholder="请输入模板编号" 
          :disabled="!!initialData?.id"
        />
      </el-form-item>

      <el-form-item label="模板名称" prop="title">
        <el-input 
          v-model="formData.title" 
          placeholder="请输入模板名称" 
          maxlength="80" 
          show-word-limit 
        />
      </el-form-item>

      <el-form-item label="模板别名" prop="alias">
        <el-input 
          v-model="formData.alias" 
          placeholder="请输入模板别名（可选）" 
          maxlength="80" 
          show-word-limit 
        />
      </el-form-item>

      <el-form-item label="适用范围" prop="applicableScope">
        <el-input 
          v-model="formData.applicableScope" 
          placeholder="请输入适用范围（可选）" 
          maxlength="80" 
          show-word-limit 
        />
      </el-form-item>

      <el-form-item prop="suffixCode">
        <template #label>
          <div class="custom-label">
            <span>尾缀编码</span>
            <el-tooltip
              content="后续生成文件编码时，会做为文件编码的一部分，没有可以不填"
              placement="top"
            >
              <el-icon class="label-help-icon"><QuestionFilled /></el-icon>
            </el-tooltip>
          </div>
        </template>
        <el-input 
          v-model="formData.suffixCode" 
          placeholder="请输入尾缀编码（可选）" 
          maxlength="80" 
          show-word-limit 
        />
      </el-form-item>

      <el-form-item label="标准类型" prop="standardType">
        <el-select
          v-model="formData.standardType"
          placeholder="请选择标准类型"
          class="w-full"
        >
          <el-option
            v-for="item in standardTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="制修订内容" prop="revisionContent">
        <el-input
          v-model="formData.revisionContent"
          type="textarea"
          :rows="4"
          placeholder="请输入制修订内容"
        />
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
        >
          <el-option
            v-for="item in ownerOptions"
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
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { QuestionFilled } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { Template } from '@/api/template'
import { 
  getTemplateTypeOptions, 
  getTemplateStatusOptions, 
  getOwnerOptions 
} from '@/api/template'
import type { OptionItem } from '@/components/BaseList/types'

const props = withDefaults(defineProps<{
  modelValue: boolean
  initialData?: Partial<Template>
}>(), {
  modelValue: false
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'submit', data: Template): void
  (e: 'error', error: Error): void
}>()

// 表单实例
const formRef = ref<FormInstance>()
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})
const submitting = ref(false)

// 表单数据
const formData = reactive<Partial<Template>>({
  title: '',
  categoryName: '',
  status: '',
  owner: '',
  version: '1.0.0',
  description: '',
  templateFile: '',
  templateCode: '',
  applicableScope: '',
  suffixCode: '',
  standardType: '',
  revisionContent: '',
  alias: ''
})

// 表单校验规则
const rules = reactive<FormRules>({
  title: [
    { required: true, message: '请输入模板名称', trigger: 'blur' },
    { max: 80, message: '长度不能超过80个字符', trigger: 'blur' }
  ],
  categoryName: [
    { required: true, message: '请选择模板类型', trigger: 'change' }
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
  ],
  templateFile: [
    { required: true, message: '请上传模板文件', trigger: 'change' }
  ],
  templateCode: [
    { required: true, message: '请输入模板编号', trigger: 'blur' }
  ],
  standardType: [
    { required: true, message: '请选择标准类型', trigger: 'change' }
  ],
  alias: [
    { max: 80, message: '长度不能超过80个字符', trigger: 'blur' }
  ],
  applicableScope: [
    { max: 80, message: '长度不能超过80个字符', trigger: 'blur' }
  ],
  suffixCode: [
    { max: 80, message: '长度不能超过80个字符', trigger: 'blur' }
  ]
})

// 选项数据
const templateTypeOptions = ref<OptionItem[]>([])
const statusOptions = ref<OptionItem[]>([])
const ownerOptions = ref<OptionItem[]>([])
const standardTypeOptions = ref<OptionItem[]>([
  { label: '行业标准模板', value: '行业标准模板' },
  { label: '企业标准模板', value: '企业标准模板' },
  { label: '自定义模板', value: '自定义模板' }
])

// 初始化选项数据
const initOptions = async () => {
  try {
    const [types, statuses, owners] = await Promise.all([
      getTemplateTypeOptions(),
      getTemplateStatusOptions(),
      getOwnerOptions()
    ])
    templateTypeOptions.value = types
    statusOptions.value = statuses
    ownerOptions.value = owners
  } catch (error) {
    console.error('Failed to load options:', error)
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

// 处理文件上传
const handleFileChange = (file: any) => {
  if (file && file.raw) {
    formData.templateFile = file.name
  }
}
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

.custom-label {
  display: flex;
  align-items: center;
  gap: 4px;
  
  .label-help-icon {
    color: var(--el-color-info);
    cursor: pointer;
    font-size: 14px;
    
    &:hover {
      color: var(--el-color-primary);
    }
  }
}

.template-file-uploader {
  width: 100%;
  
  :deep(.el-upload__tip) {
    color: var(--el-text-color-secondary);
    font-size: 12px;
    margin-top: 8px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 100%;
  }
}

.suffix-code-container {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .suffix-code-help {
    color: var(--el-color-info);
    cursor: pointer;
    font-size: 16px;
    
    &:hover {
      color: var(--el-color-primary);
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 