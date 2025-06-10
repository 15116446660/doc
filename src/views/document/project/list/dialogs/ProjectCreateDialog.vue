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

      <el-form-item label="项目图号" prop="projectNum">
        <el-input v-model="formData.projectNum" placeholder="请输入项目图号" />
      </el-form-item>

      <el-form-item label="优先级" prop="priority">
        <el-select v-model="formData.priority" placeholder="请选择优先级" class="w-full">
          <el-option label="P0 - 最高" value="P0" />
          <el-option label="P1 - 高" value="P1" />
          <el-option label="P2 - 中" value="P2" />
          <el-option label="P3 - 低" value="P3" />
        </el-select>
      </el-form-item>

      <el-form-item label="项目状态" prop="status">
        <el-select v-model="formData.status" placeholder="请选择项目状态" class="w-full">
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="负责人" prop="director">
        <el-select
          v-model="formData.director"
          placeholder="请选择负责人"
          class="w-full"
          @change="handleDirectorChange"
        >
          <el-option
            v-for="item in directorOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          >
            <div class="user-option">
              <el-avatar :size="24" :src="item.headImg">
                {{ item.name?.charAt(0) }}
              </el-avatar>
              <span>{{ item.name }}</span>
            </div>
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="项目时间" required>
        <el-col :span="11">
          <el-form-item prop="startTime">
            <el-date-picker
              v-model="formData.startTime"
              type="date"
              placeholder="开始日期"
              value-format="YYYY-MM-DD"
              class="w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :span="2" class="text-center">
          <span class="text-gray-400">至</span>
        </el-col>
        <el-col :span="11">
          <el-form-item prop="endTime">
            <el-date-picker
              v-model="formData.endTime"
              type="date"
              placeholder="结束日期"
              value-format="YYYY-MM-DD"
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
import { getProjectStatusOptions, getDirectorOptions } from '@/api/document'

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
  projectNum: '',
  priority: 'P2',
  status: 'SURVEY',
  startTime: '',
  endTime: '',
  description: '',
  director: '',
  directorName: '',
  directorHeadImg: '',
  documentCount: 0,
  userCount: 0
})

// 表单校验规则
const formRules = {
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  projectNum: [
    { required: true, message: '请输入项目图号', trigger: 'blur' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择项目状态', trigger: 'change' }
  ],
  director: [
    { required: true, message: '请选择负责人', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束日期', trigger: 'change' }
  ]
}

// 状态选项
const statusOptions = ref<{ label: string; value: string }[]>([])
// 负责人选项
const directorOptions = ref<{ id: string; name: string; headImg?: string }[]>([])

// 提交状态
const submitting = ref(false)

// 初始化选项数据
const initOptions = async () => {
  try {
    const [status, directors] = await Promise.all([
      getProjectStatusOptions(),
      getDirectorOptions()
    ])
    statusOptions.value = status
    directorOptions.value = directors
  } catch (error) {
    console.error('Failed to load options:', error)
  }
}

// 处理负责人选择变化
const handleDirectorChange = (value: string) => {
  const selected = directorOptions.value.find(item => item.id === value)
  if (selected) {
    formData.directorName = selected.name
    formData.directorHeadImg = selected.headImg
  }
}

// 处理提交
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    // 生成创建时间
    if (!formData.createTime) {
      formData.createTime = new Date().toISOString().replace('T', ' ').substring(0, 19)
    }
    
    // 设置默认值
    if (formData.documentCount === undefined) {
      formData.documentCount = 0
    }
    
    if (formData.userCount === undefined) {
      formData.userCount = 1
    }
    
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
    projectNum: '',
    priority: 'P2',
    status: 'SURVEY',
    startTime: '',
    endTime: '',
    description: '',
    directorName: '',
    director: '',
    directorHeadImg: '',
    documentCount: 0,
    userCount: 0
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