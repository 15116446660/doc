<template>
  <el-card>
    <template #header>角色编辑器</template>
    <el-form label-position="top">
      <el-form-item label="角色名称">
        <el-input :model-value="authStore.userRole.value" placeholder="当前用户角色" disabled></el-input>
      </el-form-item>
      <el-form-item label="权限">
        <el-checkbox-group>
            <el-checkbox v-for="p in authStore.permissions.value" :key="p" :label="p" disabled />
        </el-checkbox-group>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script lang="ts" setup>
import { useAuthStore } from '@/stores/useAuthStore';

const authStore = useAuthStore();
</script>
