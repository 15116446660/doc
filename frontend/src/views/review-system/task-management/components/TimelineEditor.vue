<template>
  <div>
    <h3>评审时间设置</h3>
    <el-form label-width="120px">
      <el-form-item label="开始日期">
        <el-date-picker type="date" placeholder="选择日期" v-model="startDate"></el-date-picker>
      </el-form-item>
      <el-form-item label="截止日期">
        <el-date-picker type="date" placeholder="选择日期" v-model="endDate"></el-date-picker>
      </el-form-item>
    </el-form>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'

const startDate = ref('')
const endDate = ref('')
</script>
