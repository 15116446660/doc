<template>
  <el-card>
    <template #header>问题编辑器</template>
    <el-form label-position="top">
      <el-form-item label="问题标题">
        <el-input placeholder="请输入问题标题"></el-input>
      </el-form-item>
      <el-form-item label="问题描述">
        <el-input type="textarea" placeholder="请描述问题"></el-input>
      </el-form-item>
       <el-form-item label="严重程度">
        <el-select placeholder="选择严重程度">
            <el-option label="高" value="HIGH"></el-option>
            <el-option label="中" value="MEDIUM"></el-option>
            <el-option label="低" value="LOW"></el-option>
        </el-select>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script lang="ts" setup>
// Placeholder
</script>
