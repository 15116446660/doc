<template>
  <div class="project-type-tree">
    <div class="tree-header">
      <div class="title">项目分类</div>
      <div class="actions">
        <el-button
          type="primary"
          link
          :icon="Plus"
          @click="handleAddType"
        >
          新增分类
        </el-button>
      </div>
    </div>
    
    <el-tree
      ref="treeRef"
      :data="treeData"
      :props="defaultProps"
      :highlight-current="true"
      node-key="id"
      default-expand-all
      @node-click="handleNodeClick"
    >
      <template #default="{ node, data }">
        <div class="custom-tree-node">
          <span class="node-label">{{ node.label }}</span>
          <span class="node-actions">
            <el-button
              type="primary"
              link
              :icon="Edit"
              @click.stop="handleEditType(data)"
            />
            <el-button
              type="danger"
              link
              :icon="Delete"
              @click.stop="handleDeleteType(data)"
            />
          </span>
        </div>
      </template>
    </el-tree>

    <!-- 分类编辑对话框 -->
    <el-dialog
      :title="typeForm.id ? '编辑分类' : '新增分类'"
      v-model="dialogVisible"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="typeForm"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="typeForm.name" placeholder="请输入分类名称" />
        </el-form-item>

        <el-form-item label="上级分类" prop="parentId">
          <el-tree-select
            v-model="typeForm.parentId"
            :data="treeData"
            :props="defaultProps"
            :render-after-expand="false"
            placeholder="请选择上级分类"
            class="w-full"
          />
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="typeForm.sort"
            :min="0"
            :max="999"
            placeholder="请输入排序值"
            class="w-full"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import type { ProjectType } from '@/types/document'

// 树形数据
const treeData = ref<ProjectType[]>([
  {
    id: '1',
    name: '工程项目',
    children: [
      {
        id: '1-1',
        name: '市政工程',
        parentId: '1'
      },
      {
        id: '1-2',
        name: '房建工程',
        parentId: '1'
      }
    ]
  },
  {
    id: '2',
    name: '产品项目',
    children: [
      {
        id: '2-1',
        name: '软件产品',
        parentId: '2'
      },
      {
        id: '2-2',
        name: '硬件产品',
        parentId: '2'
      }
    ]
  }
])

// 树形配置
const defaultProps = {
  children: 'children',
  label: 'name'
}

// 树形实例
const treeRef = ref()

// 对话框控制
const dialogVisible = ref(false)

// 表单实例
const formRef = ref<FormInstance>()

// 表单数据
const typeForm = ref<Partial<ProjectType>>({
  name: '',
  parentId: null,
  sort: 0
})

// 表单校验规则
const formRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ]
}

// 处理节点点击
const handleNodeClick = (data: ProjectType) => {
  // 发射选择事件
  emit('select', data)
}

// 处理添加分类
const handleAddType = () => {
  typeForm.value = {
    name: '',
    parentId: '',
    sort: 0
  }
  dialogVisible.value = true
}

// 处理编辑分类
const handleEditType = (data: ProjectType) => {
  typeForm.value = { ...data }
  dialogVisible.value = true
}

// 处理删除分类
const handleDeleteType = async (data: ProjectType) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除分类"${data.name}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // TODO: 调用删除API
    console.log('Delete type:', data)
    
    ElMessage.success('删除成功')
  } catch {
    // 用户取消删除
  }
}

// 处理提交
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    // TODO: 调用保存API
    console.log('Save type:', typeForm.value)
    
    ElMessage.success('保存成功')
    dialogVisible.value = false
  } catch {
    // 表单校验失败
  }
}

// 定义事件
const emit = defineEmits<{
  (e: 'select', type: ProjectType): void
}>()
</script>

<style lang="scss" scoped>
.project-type-tree {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: var(--el-bg-color);

  .tree-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px;
    border-bottom: 1px solid var(--el-border-color-lighter);

    .title {
      font-size: 14px;
      font-weight: 500;
      color: var(--el-text-color-primary);
    }
  }

  .el-tree {
    flex: 1;
    padding: 12px;
    overflow-y: auto;
  }
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;

  .node-label {
    color: var(--el-text-color-regular);
  }

  .node-actions {
    display: none;
  }

  &:hover {
    .node-actions {
      display: flex;
      gap: 4px;
    }
  }
}

.w-full {
  width: 100%;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style> 