<template>
  <el-card>
    <template #header>权限矩阵 (当前用户)</template>
    <p>当前用户角色: {{ authStore.userRole.value }}</p>
    <p>拥有的权限点:</p>
    <el-table :data="permissionsData" border style="width: 100%">
      <el-table-column prop="permission" label="权限点" />
    </el-table>
  </el-card>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import { useAuthStore } from '@/stores/useAuthStore';

const authStore = useAuthStore();

const permissionsData = computed(() => {
    return authStore.permissions.value.map(p => ({ permission: p }));
});
</script>
