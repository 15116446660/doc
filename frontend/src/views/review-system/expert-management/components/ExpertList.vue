<template>
  <div>
    <h3>专家列表</h3>
    <el-table :data="experts" v-loading="loading" style="width: 100%">
      <el-table-column prop="userName" label="姓名" />
      <el-table-column prop="skillTags" label="专业领域">
        <template #default="{ row }">
            <el-tag v-for="tag in row.skillTags" :key="tag" style="margin-right: 5px;">{{ tag }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="isAvailable" label="状态">
        <template #default="{ row }">
            <el-tag :type="row.isAvailable ? 'success' : 'danger'">
                {{ row.isAvailable ? '空闲' : '忙碌' }}
            </el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script lang="ts" setup>
defineProps<{
  experts: any[];
  loading: boolean;
}>();
</script>
