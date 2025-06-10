<template>
  <base-tree-select
    ref="treeRef"
    tree-title="项目分类"
    add-button-text="新增分类"
    search-placeholder="搜索分类..."
    add-dialog-title="新增分类"
    edit-dialog-title="编辑分类"
    :is-lazy="true"
    :key-map="{
      id: 'id',
      label: 'name',
      code: '',
      children: 'children',
      parentId: 'parentId'
    }"
    :form-fields="{
      name: {
        label: '分类名称',
        placeholder: '请输入分类名称',
        maxLength: 50
      },
      sort: {
        label: '排序',
        placeholder: '请输入排序值',
        maxLength: 5
      }
    }"
    :form-rules="formRules"
    :load-tree-data="loadProjectTypeTree"
    :load-children="loadChildren"
    :create-node="createProjectType"
    :update-node="updateProjectType"
    :delete-node="deleteProjectType"
    :show-node-code="false"
    @select="handleTypeSelect"
    @created="handleCreated"
    @updated="handleUpdated"
    @deleted="handleDeleted"
  />
</template>

<script setup lang="ts">
import { BaseTreeSelect } from '@/components'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormRules } from 'element-plus'
import type { ProjectType } from '@/types/document'

const emit = defineEmits<{
  (e: 'select', type: ProjectType | null): void
  (e: 'change'): void
}>()

// 表单验证规则
const formRules: FormRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  sort: [
    { pattern: /^\d+$/, message: '排序值必须为数字', trigger: 'blur' }
  ]
}

// 模拟数据
const mockProjectTypes: ProjectType[] = [
  {
    id: '1',
    name: '工程项目',
    children: [
      {
        id: '11',
        name: '市政工程',
        parentId: '1',
        sort: 1
      },
      {
        id: '12',
        name: '房建工程',
        parentId: '1',
        sort: 2
      }
    ],
    sort: 1
  },
  {
    id: '2',
    name: '产品项目',
    children: [
      {
        id: '21',
        name: '软件产品',
        parentId: '2',
        sort: 1
      },
      {
        id: '22',
        name: '硬件产品',
        parentId: '2',
        sort: 2
      }
    ],
    sort: 2
  }
]

// 加载项目分类树
const loadProjectTypeTree = async () => {
  // TODO: 实际项目中应该调用API
  // 例如: return await getProjectTypeTree()
  
  // 这里使用模拟数据
  return mockProjectTypes
}

// 加载子节点
const loadChildren = async (node: any) => {
  const projectNode = node as ProjectType
  return projectNode.children || []
}

// 创建项目分类
const createProjectType = async (data: Partial<ProjectType>) => {
  // TODO: 实际项目中应该调用API
  // 例如: return await createProjectType(data)
  
  // 这里模拟API调用
  console.log('创建项目分类:', data)
  
  // 模拟成功响应
  return {
    ...data,
    id: `new-${Date.now()}`,
    createTime: new Date().toISOString(),
    updateTime: new Date().toISOString()
  } as ProjectType
}

// 更新项目分类
const updateProjectType = async (id: string | number, data: Partial<ProjectType>) => {
  // TODO: 实际项目中应该调用API
  // 例如: return await updateProjectType(id, data)
  
  // 这里模拟API调用
  console.log('更新项目分类:', id, data)
  
  // 模拟成功响应
  return {
    ...data,
    id,
    updateTime: new Date().toISOString()
  } as ProjectType
}

// 删除项目分类
const deleteProjectType = async (id: string | number) => {
  // TODO: 实际项目中应该调用API
  // 例如: return await deleteProjectType(id)
  
  // 这里模拟API调用
  console.log('删除项目分类:', id)
  
  // 模拟成功响应
  return true
}

// 处理分类选择
const handleTypeSelect = (node: any) => {
  emit('select', node as ProjectType | null)
}

// 处理创建成功
const handleCreated = () => {
  ElMessage.success('创建成功')
  emit('change')
}

// 处理更新成功
const handleUpdated = () => {
  ElMessage.success('更新成功')
  emit('change')
}

// 处理删除成功
const handleDeleted = () => {
  ElMessage.success('删除成功')
  emit('change')
}

// 树形组件实例
const treeRef = ref<InstanceType<typeof BaseTreeSelect>>()

// 暴露的方法
defineExpose({
  refreshTree: () => {
    treeRef.value?.refreshTreeData()
  }
})
</script> 