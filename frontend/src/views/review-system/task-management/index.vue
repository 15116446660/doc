<template>
  <div class="task-management-container">
    <div class="header">
      <h1>评审任务管理</h1>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        创建任务
      </el-button>
    </div>

    <el-table :data="reviewStore.filteredTasks.value" v-loading="reviewStore.isLoading.value" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="taskName" label="任务名称" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="priority" label="优先级" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button size="small" @click="viewTask(row)">查看</el-button>
          <el-button size="small" type="primary" @click="editTask(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <TaskCreateDialog
      v-model="showCreateDialog"
      @task-created="handleTaskCreated"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { useReviewStore } from '@/stores/useReviewStore';
import TaskCreateDialog from './components/TaskCreateDialog.vue';

const router = useRouter();
const reviewStore = useReviewStore();
const showCreateDialog = ref(false);

onMounted(() => {
  reviewStore.fetchTasks();
});

const handleTaskCreated = async (taskData: any) => {
  const { success, error } = await reviewStore.createTask(taskData);
  if (success) {
    ElMessage.success('任务创建成功');
    showCreateDialog.value = false;
    reviewStore.fetchTasks(); // Refresh the list
  } else {
    ElMessage.error(`创建失败: ${error}`);
  }
};

const viewTask = (task: any) => {
  router.push(`/review-system/workspace/${task.id}`);
};

const editTask = (task: any) => {
  // Logic to edit task
  console.log('Editing task:', task);
};
</script>

<style scoped>
.task-management-container {
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>
