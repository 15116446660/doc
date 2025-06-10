<template>
  <el-dialog
    :title="initialData?.id ? '编辑文档' : '新建文档'"
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
      class="document-form"
    >
      <el-form-item label="文档名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入文档名称" />
      </el-form-item>

      <el-form-item label="文档编号" prop="documentCode">
        <el-input v-model="formData.documentCode" placeholder="请输入文档编号" />
      </el-form-item>

      <el-form-item label="文档类型" prop="type">
        <el-select v-model="formData.type" placeholder="请选择文档类型" class="w-full">
          <el-option
            v-for="item in documentTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="模板" prop="templateId">
        <el-select v-model="formData.templateId" placeholder="请选择模板" class="w-full" clearable>
          <el-option
            v-for="item in templateOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="自动填充" prop="autoFill">
        <el-switch v-model="formData.autoFill" />
      </el-form-item>

      <el-form-item label="负责人" prop="directorId">
        <el-select v-model="formData.directorId" placeholder="请选择负责人" class="w-full" filterable>
          <el-option
            v-for="item in directorOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="文档格式" prop="format">
        <el-select v-model="formData.format" placeholder="请选择文档格式" class="w-full">
          <el-option
            v-for="item in formatOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="文档内容" prop="content">
        <el-upload
          class="document-upload"
          drag
          :action="uploadAction"
          :headers="uploadHeaders"
          :data="uploadData"
          :before-upload="handleBeforeUpload"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持 docx, pdf, xlsx 等格式，单个文件不超过10MB
            </div>
          </template>
        </el-upload>
      </el-form-item>

      <el-form-item label="文档描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入文档描述"
        />
      </el-form-item>

      <el-form-item label="标签" prop="tags">
        <el-select
          v-model="formData.tags"
          multiple
          filterable
          allow-create
          default-first-option
          placeholder="请选择或输入标签"
          class="w-full"
        >
          <el-option
            v-for="item in tagOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
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
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import type { Document, OptionItem } from '@/types/document'
import {
  getDocumentTypeOptions,
  getDocumentTagOptions,
  getTemplateOptions,
  getDirectorOptions
} from '@/api/document'

const props = defineProps<{
  modelValue: boolean
  initialData?: Partial<Document>
  projectId: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'submit', data: Document): void
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
const formData = reactive<Partial<Document>>({
  name: '',
  documentCode: '',
  type: '',
  format: '',
  content: '',
  description: '',
  tags: [],
  status: '草稿',
  version: '1.0.0',
  autoFill: false
})

// 表单校验规则
const formRules = {
  name: [
    { required: true, message: '请输入文档名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  documentCode: [
    { required: true, message: '请输入文档编号', trigger: 'blur' }
  ],
  templateId: [
    { required: true, message: '请选择模板', trigger: 'change' }
  ],
  directorId: [
    { required: true, message: '请选择负责人', trigger: 'change' }
  ],
  type: [
    { required: true, message: '请选择文档类型', trigger: 'change' }
  ],
  format: [
    { required: true, message: '请选择文档格式', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请上传文档内容', trigger: 'change' }
  ]
}

// 文档类型选项
const documentTypeOptions = ref<OptionItem[]>([])
const templateOptions = ref<OptionItem[]>([])
const directorOptions = ref<OptionItem[]>([])
// 文档格式选项
const formatOptions = ref([
  { label: 'Word文档', value: 'docx' },
  { label: 'PDF文档', value: 'pdf' },
  { label: 'Excel表格', value: 'xlsx' },
  { label: '图片', value: 'image' }
])
// 标签选项
const tagOptions = ref<OptionItem[]>([])

// 上传配置
const uploadAction = '/api/document/upload'
const uploadHeaders = {
  // TODO: 添加认证头
}
const uploadData = {
  projectId: props.projectId
}

// 上传前检查
const handleBeforeUpload = (file: File) => {
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('上传文件大小不能超过 10MB!')
    return false
  }
  return true
}

// 上传成功
const handleUploadSuccess = (response: any) => {
  formData.content = response.url
  ElMessage.success('上传成功')
}

// 上传失败
const handleUploadError = () => {
  ElMessage.error('上传失败')
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
    emit('submit', {
      ...formData,
      projectId: props.projectId
    } as Document)
    
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
    documentCode: '',
    type: '',
    format: '',
    content: '',
    description: '',
    tags: [],
    status: '草稿',
    version: '1.0.0',
    autoFill: false
  })
}

// 监听初始数据变化
watch(
  () => props.initialData,
  (val) => {
    if (val) {
      Object.assign(formData, val)
    }
  },
  { immediate: true, deep: true }
)

// 初始化选项数据
const initOptions = async () => {
  try {
    const [types, tags, templates, directors] = await Promise.all([
      getDocumentTypeOptions(),
      getDocumentTagOptions(),
      getTemplateOptions(),
      getDirectorOptions()
    ])

    documentTypeOptions.value = (types as any)
    tagOptions.value = (tags as any)
    templateOptions.value = (templates as any)
    directorOptions.value = (directors as any).map((d: any) => ({
      label: d.name,
      value: d.id
    }))
  } catch (error) {
    console.error('Failed to load options:', error)
  }
}

// 初始化
initOptions()
</script>

<style lang="scss" scoped>
.document-form {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 16px;
}

.document-upload {
  width: 100%;
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

:deep(.el-upload) {
  width: 100%;
}

:deep(.el-upload-dragger) {
  width: 100%;
}
</style> 