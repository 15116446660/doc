<template>
  <div>
    <h2>权限管理</h2>
    <PermissionMatrix />
    <RoleEditor />
  </div>
</template>

<script setup lang="ts">
import PermissionMatrix from './components/PermissionMatrix.vue';
import RoleEditor from './components/RoleEditor.vue';
</script>
