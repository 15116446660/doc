<template>
  <div class="project-container">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">项目列表</h2>
      </div>
      <div class="header-right">
        <el-button plain class="action-btn">
          <el-icon><RefreshRight /></el-icon>
        </el-button>
        <el-button plain class="action-btn">
          <el-icon><MoreFilled /></el-icon>
        </el-button>
        <el-button type="primary" class="add-btn">
          <el-icon><Plus /></el-icon>
          添加
        </el-button>
      </div>
    </div>

    <div class="search-section">
      <el-input
        v-model="searchQuery"
        placeholder="搜索项目..."
        prefix-icon="Search"
        clearable
        class="search-input"
      />
    </div>

    <div class="project-cards">
      <div v-for="(project, index) in projects" :key="index" class="project-card">
        <div class="card-header">
          <div class="project-status" :class="project.statusClass">
            <span class="status-dot"></span>
            {{ project.status }}
          </div>
          <div v-if="project.priority" class="project-priority" :class="project.priorityClass">
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
            <span>{{ project.date }}</span>
          </div>
          <div class="meta-item">
            <el-icon><Money /></el-icon>
            <span>¥{{ project.budget }}万</span>
          </div>
        </div>
        <div class="project-footer">
          <div class="project-members">
            <el-avatar v-for="(member, i) in project.members" :key="i" :size="28" :src="member" />
          </div>
          <div class="project-tags">
            <el-tag v-for="(tag, i) in project.tags" :key="i" size="small">{{ tag }}</el-tag>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { 
  RefreshRight, 
  MoreFilled, 
  Plus, 
  Calendar,
  Money
} from '@element-plus/icons-vue'

const searchQuery = ref('')

const projects = ref([
  {
    title: '移动端APP设计',
    description: '新一代移动应用的UI/UX设计项目，包含完整的用户体验流程设计',
    status: '进行中',
    statusClass: 'status-in-progress',
    priority: '高优先级',
    priorityClass: 'priority-high',
    progress: 75,
    progressColor: '#60A5FA',
    date: '2024-03-30',
    budget: 115,
    members: [
      'https://placeholder.pics/svg/30/DEDEDE/555555/U',
    ],
    tags: ['UI设计', '移动端', '用户体验'],
    documents: 24
  },
  {
    title: '后台管理系统',
    description: '企业级后台管理系统开发，包含权限管理、数据分析等功能',
    status: '进行中',
    statusClass: 'status-in-progress',
    priority: '中优先级',
    priorityClass: 'priority-medium',
    progress: 45,
    progressColor: '#60A5FA',
    date: '2024-04-15',
    budget: 85,
    members: [
      'https://placeholder.pics/svg/30/DEDEDE/555555/U',
      'https://placeholder.pics/svg/30/DEDEDE/555555/U',
    ],
    tags: ['后台', '管理系统'],
    documents: 18
  }
])
</script>

<style scoped>
.project-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.header-right {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 8px;
  height: 36px;
  width: 36px;
}

.add-btn {
  background: linear-gradient(135deg, #6366F1 0%, #8B5CF6 100%);
  border: none;
  display: flex;
  align-items: center;
  gap: 4px;
}

.search-section {
  margin-bottom: 20px;
}

.search-input {
  width: 100%;
}

.project-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.project-card {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
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

.status-in-progress {
  color: #60A5FA;
}

.status-in-progress .status-dot {
  background-color: #60A5FA;
}

.project-priority {
  font-size: 12px;
  font-weight: 500;
}

.priority-high {
  color: #F43F5E;
}

.priority-medium {
  color: #F59E0B;
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
  margin-bottom: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #6B7280;
}

.project-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
}

.el-tag {
  background-color: #EEF2FF;
  color: #6366F1;
  border: none;
}
</style>