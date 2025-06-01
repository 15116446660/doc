<script setup lang="ts">
import { useAuthStore } from '@/store/auth'

const authStore = useAuthStore()
</script>

<template>
  <div class="home-container">
    <el-card class="welcome-card">
      <template #header>
        <div class="card-header">
          <span>欢迎使用投标文档管理系统</span>
        </div>
      </template>
      <div class="user-info">
        <el-avatar :size="64" :src="authStore.user?.avatar" />
        <div class="user-details">
          <h3>{{ authStore.user?.name }}</h3>
          <p>{{ authStore.user?.department }} - {{ authStore.user?.role }}</p>
        </div>
      </div>
    </el-card>

    <!-- 根据用户角色显示不同的快捷操作卡片 -->
    <div class="quick-actions">
      <el-row :gutter="20">
        <el-col :span="8" v-if="authStore.user?.permissions.includes('project:create')">
          <el-card class="action-card">
            <el-button type="primary" icon="Plus">创建新项目</el-button>
          </el-card>
        </el-col>
        <el-col :span="8" v-if="authStore.user?.permissions.includes('document:edit')">
          <el-card class="action-card">
            <el-button type="success" icon="Document">编辑文档</el-button>
          </el-card>
        </el-col>
        <el-col :span="8" v-if="authStore.user?.permissions.includes('document:review')">
          <el-card class="action-card">
            <el-button type="warning" icon="View">审核文档</el-button>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  padding: 20px;
}

.welcome-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  padding: 20px 0;
}

.user-details {
  margin-left: 20px;
}

.user-details h3 {
  margin: 0 0 10px 0;
}

.user-details p {
  margin: 0;
  color: #909399;
}

.quick-actions {
  margin-top: 20px;
}

.action-card {
  text-align: center;
  padding: 20px;
}

.action-card .el-button {
  width: 100%;
  height: 80px;
  font-size: 16px;
}
</style> 