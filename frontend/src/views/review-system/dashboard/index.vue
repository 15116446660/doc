<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="16">
        <MonitoringBoard />
      </el-col>
      <el-col :span="8">
        <QuickActions />
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <TaskStatistics />
      </el-col>
      <el-col :span="12">
        <ExpertWorkload />
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import MonitoringBoard from './components/MonitoringBoard.vue';
import QuickActions from './components/QuickActions.vue';
import TaskStatistics from './components/TaskStatistics.vue';
import ExpertWorkload from './components/ExpertWorkload.vue';
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}
</style>
