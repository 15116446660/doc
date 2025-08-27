<template>
  <div>
    <h3>流程模板选择</h3>
    <el-select v-model="selectedWorkflow" placeholder="请选择评审流程模板">
      <el-option
        v-for="item in workflows"
        :key="item.value"
        :label="item.label"
        :value="item.value"
      />
    </el-select>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'

const selectedWorkflow = ref('')
const workflows = ref([
  { value: 'wf-001', label: '标准合同评审流程' },
  { value: 'wf-002', label: '技术文档三级评审流程' },
  { value: 'wf-003', label: '财务报表快速审核' },
])
</script>
