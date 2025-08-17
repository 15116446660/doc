<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>项目层级结构管理</span>
          <el-button type="primary" :icon="Plus" @click="handleOpenAddDialog(null)">
            添加根节点
          </el-button>
        </div>
      </template>

      <el-tree
        :data="categoryTree"
        :props="treeProps"
        node-key="id"
        default-expand-all
        :expand-on-click-node="false"
      >
        <template #default="{ node, data }">
          <span class="custom-tree-node">
            <span>{{ node.label }} ({{ data.type }})</span>
            <span>
              <el-button type="text" size="small" @click.stop="handleOpenAddDialog(data)">
                添加子节点
              </el-button>
              <el-button type="text" size="small" @click.stop="handleOpenEditDialog(data)">
                编辑
              </el-button>
              <el-popconfirm
                title="确定要删除这个节点吗？"
                @confirm="handleDelete(data.id)"
              >
                <template #reference>
                  <el-button type="text" size="small" style="color: #F56C6C;" @click.stop>
                    删除
                  </el-button>
                </template>
              </el-popconfirm>
            </span>
          </span>
        </template>
      </el-tree>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" @close="handleCloseDialog">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="节点名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入节点名称" />
        </el-form-item>
        <el-form-item label="节点类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择节点类型" :disabled="isEdit">
            <el-option label="部门" value="DEPARTMENT" />
            <el-option label="品类" value="CATEGORY" />
            <el-option label="子品类" value="SUB_CATEGORY" />
          </el-select>
        </el-form-item>
        <el-form-item label="父节点">
          <span>{{ parentNodeName }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue';
import { getCategoryTree, createCategory, updateCategory, deleteCategory, type ProjectCategoryNode, type CreateCategoryPayload, type UpdateCategoryPayload } from '@/api/projectCategory';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';

const categoryTree = ref<ProjectCategoryNode[]>([]);
const treeProps = {
  children: 'children',
  label: 'name',
};

const dialogVisible = ref(false);
const isEdit = ref(false);
const dialogTitle = computed(() => (isEdit.value ? '编辑节点' : '添加新节点'));
const formRef = ref<FormInstance>();

const initialFormData: CreateCategoryPayload & { id?: number } = {
  name: '',
  type: 'DEPARTMENT',
  parentId: null,
};
const formData = ref({ ...initialFormData });
const parentNodeName = ref('无 (根节点)');

const formRules = reactive<FormRules>({
  name: [{ required: true, message: '节点名称不能为空', trigger: 'blur' }],
  type: [{ required: true, message: '节点类型不能为空', trigger: 'change' }],
});

onMounted(() => {
  fetchTreeData();
});

const fetchTreeData = async () => {
  try {
    categoryTree.value = await getCategoryTree();
  } catch (error) {
    ElMessage.error('获取层级数据失败');
  }
};

const handleOpenAddDialog = (parentNode: ProjectCategoryNode | null) => {
  isEdit.value = false;
  formData.value = { ...initialFormData };
  if (parentNode) {
    formData.value.parentId = parentNode.id;
    parentNodeName.value = parentNode.name;
  } else {
    parentNodeName.value = '无 (根节点)';
  }
  dialogVisible.value = true;
};

const handleOpenEditDialog = (node: ProjectCategoryNode) => {
  isEdit.value = true;
  formData.value = {
    id: node.id,
    name: node.name,
    type: node.type, // 类型在编辑时不可更改
    parentId: node.parentId,
  };
  // Find parent name
  // This is a simplified approach. A better way would be a map lookup.
  if (node.parentId) {
     // For now, we don't display parent name on edit to keep it simple
     parentNodeName.value = `ID: ${node.parentId}`;
  } else {
     parentNodeName.value = '无 (根节点)';
  }
  dialogVisible.value = true;
};

const handleCloseDialog = () => {
  formRef.value?.resetFields();
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          const payload: UpdateCategoryPayload = { name: formData.value.name };
          await updateCategory(formData.value.id!, payload);
          ElMessage.success('更新成功');
        } else {
          const payload: CreateCategoryPayload = {
            name: formData.value.name,
            type: formData.value.type,
            parentId: formData.value.parentId,
          };
          await createCategory(payload);
          ElMessage.success('添加成功');
        }
        dialogVisible.value = false;
        fetchTreeData();
      } catch (error) {
        ElMessage.error(isEdit.value ? '更新失败' : '添加失败');
      }
    }
  });
};

const handleDelete = async (id: number) => {
  try {
    await deleteCategory(id);
    ElMessage.success('删除成功');
    fetchTreeData();
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '删除失败');
  }
};
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}
</style>
