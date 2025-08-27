<template>
  <el-card class="issue-card">
    <template #header>
      <div class="card-header">
        <span>{{ issue.title }}</span>
        <el-tag :type="statusType">{{ issue.status }}</el-tag>
      </div>
    </template>
    <p>严重程度: {{ issue.severity }}</p>
    <p>分类: {{ issue.category }}</p>
  </el-card>
</template>

<script lang="ts" setup>
import { computed } from 'vue'

const props = defineProps<{
  issue: {
    title: string;
    status: string;
    severity: string;
    category: string;
  }
}>();

const statusType = computed(() => {
    if (props.issue.status === '开放') return 'danger'
    if (props.issue.status === '已解决') return 'success'
    return 'info'
})
</script>

<style scoped>
.issue-card {
  margin-bottom: 10px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
