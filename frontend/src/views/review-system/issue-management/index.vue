<template>
  <div class="issue-management-container">
    <div class="header">
      <h1>问题清单管理</h1>
    </div>

    <el-table :data="issueStore.filteredIssues.value" v-loading="issueStore.isLoading.value" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="问题标题" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="severity" label="严重程度" />
      <el-table-column prop="category" label="分类" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button size="small" @click="viewIssue(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useIssueStore } from '@/stores/useIssueStore';

const router = useRouter();
const issueStore = useIssueStore();

onMounted(() => {
  // In a real scenario, you might pass a filter here
  issueStore.fetchIssues();
});

const viewIssue = (issue: any) => {
  // This would navigate to a detailed issue view
  console.log('Viewing issue:', issue);
  // router.push(`/review-system/issues/${issue.id}`);
};
</script>

<style scoped>
.issue-management-container {
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>
