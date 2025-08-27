<template>
  <el-card v-if="expert">
    <template #header>
      专家基本信息
    </template>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="姓名">{{ expert.userName }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="expert.isAvailable ? 'success' : 'danger'">
            {{ expert.isAvailable ? '空闲' : '忙碌' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="专业领域">
        <el-tag v-for="tag in expert.skillTags" :key="tag" style="margin-right: 5px;">{{ tag }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="当前工作量">{{ expert.currentWorkload }} / {{ expert.maxConcurrentReviews }}</el-descriptions-item>
    </el-descriptions>
  </el-card>
</template>

<script lang="ts" setup>
defineProps<{
  expert: any; // In a real app, this would be a specific Expert type
}>();
</script>
