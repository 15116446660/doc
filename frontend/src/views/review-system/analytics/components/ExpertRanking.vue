<template>
  <el-card>
    <template #header>专家排名</template>
    <el-table :data="ranking" style="width: 100%">
      <el-table-column prop="rank" label="排名" />
      <el-table-column prop="name" label="专家" />
      <el-table-column prop="score" label="贡献分" />
    </el-table>
  </el-card>
</template>

<script lang="ts" setup>
import { ref } from 'vue'

const ranking = ref([
  { rank: 1, name: '张专家', score: 98 },
  { rank: 2, name: '李专家', score: 95 },
]);
</script>
