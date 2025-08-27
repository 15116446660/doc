<template>
  <div class="settings-container">
    <h1>系统配置管理</h1>
    <el-row :gutter="20">
        <el-col :span="16">
            <PermissionMatrix />
        </el-col>
        <el-col :span="8">
            <RoleEditor />
        </el-col>
    </el-row>
     <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
            <IntegrationSettings />
        </el-col>
        <el-col :span="12">
            <AuditLog />
        </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import PermissionMatrix from './components/PermissionMatrix.vue';
import RoleEditor from './components/RoleEditor.vue';
import IntegrationSettings from './components/IntegrationSettings.vue';
import AuditLog from './components/AuditLog.vue';
</script>

<style scoped>
.settings-container {
  padding: 20px;
}
</style>
