<template>
  <el-card>
    <template #header>节点编辑器</template>
    <el-form label-position="top">
      <el-form-item label="节点名称">
        <el-input placeholder="步骤名称"></el-input>
      </el-form-item>
      <el-form-item label="SLA (小时)">
        <el-input-number :min="1" />
      </el-form-item>
      <el-form-item label="所需角色">
        <el-select placeholder="选择角色"></el-select>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script lang="ts" setup>
// Placeholder
</script>
