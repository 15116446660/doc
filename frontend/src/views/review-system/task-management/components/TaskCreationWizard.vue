<template>
  <el-steps :active="active" finish-status="success">
    <el-step title="基础信息" />
    <el-step title="上传文档" />
    <el-step title="时间设置" />
    <el-step title="完成" />
  </el-steps>

  <div class="wizard-content">
    <!-- Content for each step will go here -->
    <p>当前步骤: {{ active + 1 }}</p>
  </div>

  <el-button style="margin-top: 12px" @click="next">下一步</el-button>
</template>

<script lang="ts" setup>
import { ref } from 'vue'

const active = ref(0)

const next = () => {
  if (active.value++ > 3) active.value = 0
}
</script>

<style scoped>
.wizard-content {
    margin-top: 20px;
    padding: 20px;
    border: 1px solid #ebeef5;
    border-radius: 4px;
}
</style>
