<template>
  <base-tree-select
    tree-title="模板分类"
    add-button-text="新增分类"
    search-placeholder="搜索分类..."
    add-dialog-title="新增分类"
    edit-dialog-title="编辑分类"
    :is-lazy="true"
    :show-refresh-button="true"
    :key-map="{
      id: 'id',
      label: 'typeName',
      code: 'typeCode',
      children: 'children',
      parentId: 'parentId'
    }"
    :form-fields="{
      typeCode: {
        label: '分类编码',
        placeholder: '请输入分类编码',
        maxLength: 20
      },
      typeName: {
        label: '分类名称',
        placeholder: '请输入分类名称',
        maxLength: 50
      }
    }"
    :form-rules="formRules"
    :load-tree-data="loadTemplateTypeTree"
    :load-children="loadChildren"
    :create-node="createTemplateTypeWrapper"
    :update-node="updateTemplateTypeWrapper"
    :delete-node="deleteTemplateTypeWrapper"
    @select="handleSelect"
    @created="handleCreated"
    @updated="handleUpdated"
    @deleted="handleDeleted"
  />
</template>

<script setup lang="ts">
import { BaseTreeSelect } from '@/components'
import type { FormRules } from 'element-plus'
import { getTemplateTypeTree, createTemplateType, updateTemplateType, deleteTemplateType } from '@/api/template'
import type { TemplateType } from '@/api/template'

// Type assertions for the API functions to match BaseTreeSelect props
const createTemplateTypeWrapper = (data: any) => createTemplateType(data as Partial<TemplateType>)
const updateTemplateTypeWrapper = (id: string | number, data: any) => updateTemplateType(Number(id), data as Partial<TemplateType>)
const deleteTemplateTypeWrapper = (id: string | number) => deleteTemplateType(Number(id))

const emit = defineEmits<{
  (e: 'select', type: TemplateType | null): void
  (e: 'change'): void
}>()

// 表单验证规则
const formRules: FormRules = {
  typeCode: [
    { required: true, message: '请输入分类编码', trigger: 'blur' },
    { pattern: /^[A-Z0-9]+$/, message: '分类编码只能包含大写字母和数字', trigger: 'blur' }
  ],
  typeName: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

// 加载模板分类树
const loadTemplateTypeTree = async () => {
  return await getTemplateTypeTree()
}

// 加载子节点
const loadChildren = async (node: any) => {
  const templateNode = node as TemplateType
  return templateNode.children || []
}

// 处理节点选择
const handleSelect = (node: any) => {
  emit('select', node as TemplateType | null)
}

// 处理创建成功
const handleCreated = () => {
  emit('change')
}

// 处理更新成功
const handleUpdated = () => {
  emit('change')
}

// 处理删除成功
const handleDeleted = () => {
  emit('change')
}
</script> 