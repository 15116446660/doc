<template>
  <div class="project-cards-container" v-loading="loading">
    <div class="project-cards">
      <div v-for="project in projects" :key="project.id" class="project-card">
        <div class="card-header">
          <div class="project-status" :style="{ color: project.statusColor }">
            <span class="status-dot" :style="{ backgroundColor: project.statusColor }"></span>
            {{ project.status }}
          </div>
          <div v-if="project.priority" class="project-priority" :style="{ color: project.priorityColor }">
            {{ project.priority }}
          </div>
        </div>
        <h3 class="project-title">{{ project.title }}</h3>
        <p class="project-desc">{{ project.description }}</p>
        <div class="project-progress">
          <div class="progress-info">
            <span>进度</span>
            <span>{{ project.progress }}%</span>
          </div>
          <el-progress 
            :percentage="project.progress" 
            :stroke-width="6" 
            :color="project.progressColor"
          />
        </div>
        <div class="project-meta">
          <div class="meta-item">
            <el-icon><Calendar /></el-icon>
            <span>{{ project.dueDate }}</span>
          </div>
          <div class="meta-item">
            <el-icon><User /></el-icon>
            <span>{{ project.client }}</span>
          </div>
        </div>
        <div class="project-risk">
          <span class="risk-label">风险等级:</span>
          <el-tag
            :type="getRiskType(project.risk)"
            effect="light"
            size="small"
            class="risk-tag"
          >
            {{ project.risk }}
          </el-tag>
        </div>
        <div class="project-footer">
          <div class="project-members">
            <el-avatar v-for="(member, i) in project.members" :key="i" :size="28" :src="member" />
          </div>
          <div class="project-tags">
            <el-tag v-for="(tag, i) in project.tags" :key="i" size="small">{{ tag }}</el-tag>
          </div>
        </div>
        <div class="card-actions">
          <el-button link type="primary" size="small">查看</el-button>
          <el-button link type="primary" size="small">编辑</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineProps } from 'vue'
import type { Project } from '@/api/project'
import { Calendar, User } from '@element-plus/icons-vue'

defineProps<{
  projects: Project[]
  loading: boolean
}>()

// 获取风险等级对应的类型
const getRiskType = (risk: string) => {
  switch (risk) {
    case '低':
      return 'success'
    case '中':
      return 'warning'
    case '高':
      return 'danger'
    case '严重':
      return 'danger'
    default:
      return 'info'
  }
}
</script>

<style scoped>
.project-cards-container {
  width: 100%;
  height: 100%;
  overflow: auto;
  position: relative;
}

.project-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  padding-bottom: 20px;
}

.project-card {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
  position: relative;
}

.project-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.project-status {
  display: flex;
  align-items: center;
  font-size: 12px;
  font-weight: 500;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
}

.project-priority {
  font-size: 12px;
  font-weight: 500;
}

.project-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 8px 0;
}

.project-desc {
  font-size: 14px;
  color: #6B7280;
  margin: 0 0 16px 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.project-progress {
  margin-bottom: 16px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #6B7280;
  margin-bottom: 4px;
}

.project-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #6B7280;
}

.project-risk {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 12px;
}

.risk-label {
  color: #6B7280;
}

.project-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.project-members {
  display: flex;
}

.project-members .el-avatar {
  margin-right: -8px;
  border: 2px solid white;
}

.project-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.el-tag {
  background-color: #EEF2FF;
  color: #6366F1;
  border: none;
}

.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style> 