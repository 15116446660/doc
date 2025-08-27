<template>
  <div class="workspace-container">
    <div v-if="reviewStore.isLoading.value || issueStore.isLoading.value" class="loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>Loading...</span>
    </div>

    <div v-else-if="reviewStore.currentTask.value" class="workspace-content">
      <div class="header">
        <h1>{{ reviewStore.currentTask.value.taskName }}</h1>
        <el-tag>{{ reviewStore.currentTask.value.status }}</el-tag>
      </div>

      <el-tabs type="border-card">
        <el-tab-pane label="文档视图">
          <div class="placeholder-panel">文档查看器将在此处 (Document Viewer Here)</div>
        </el-tab-pane>
        <el-tab-pane label="问题清单">
          <el-table :data="issueStore.filteredIssues.value" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="title" label="问题标题" />
            <el-table-column prop="status" label="状态" />
            <el-table-column prop="severity" label="严重程度" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="批注列表">
          <div class="placeholder-panel">批注列表将在此处 (Annotation Panel Here)</div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <div v-else class="empty-state">
      <p>无法加载评审任务，请检查任务ID是否正确。</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import { useReviewStore } from '@/stores/useReviewStore';
import { useIssueStore } from '@/stores/useIssueStore';
import { Loading } from '@element-plus/icons-vue';

const route = useRoute();
const reviewStore = useReviewStore();
const issueStore = useIssueStore();

const taskId = computed(() => Number(route.params.taskId));

onMounted(async () => {
  if (taskId.value) {
    await reviewStore.fetchTaskDetail(taskId.value);

    issueStore.updateFilter({ reviewTaskId: taskId.value });
    await issueStore.fetchIssues();
  }
});
</script>

<style scoped>
.workspace-container {
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.placeholder-panel {
  padding: 40px;
  text-align: center;
  color: #909399;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
}
.loading, .empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
  color: #909399;
}
</style>
