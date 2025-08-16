<template>
  <div class="project-container">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">项目分类</h2>
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
        placeholder="搜索分类..."
        prefix-icon="Search"
        clearable
        class="search-input"
      />
    </div>

    <div class="category-list">
      <div v-for="(category, index) in categories" :key="index" class="category-item">
        <div class="category-icon" :style="{ backgroundColor: category.color }">
          <el-icon><component :is="category.icon" /></el-icon>
        </div>
        <div class="category-content">
          <div class="category-header">
            <h3 class="category-title">{{ category.name }}</h3>
            <span class="category-count">{{ category.count }}</span>
          </div>
          <p class="category-desc">{{ category.description }}</p>
        </div>
        <div class="category-action">
          <el-icon><ArrowRight /></el-icon>
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
  ArrowRight
} from '@element-plus/icons-vue'

const searchQuery = ref('')

const categories = ref([
  {
    name: '产品设计',
    icon: 'Document',
    color: '#6366F1',
    count: 12,
    description: '产品相关的设计文档'
  },
  {
    name: '技术开发',
    icon: 'Setting',
    color: '#10B981',
    count: 18,
    description: '技术开发相关文档'
  },
  {
    name: '市场营销',
    icon: 'Promotion',
    color: '#F59E0B',
    count: 6,
    description: '市场推广和营销文档'
  }
])
</script>

<style scoped>
.project-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
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

.category-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.category-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  background-color: #fff;
  border: 1px solid #e5e7eb;
  transition: all 0.3s;
  cursor: pointer;
}

.category-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.category-icon {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  color: white;
}

.category-content {
  flex: 1;
}

.category-header {
  display: flex;
  align-items: center;
  margin-bottom: 4px;
}

.category-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.category-count {
  margin-left: 8px;
  background-color: #e5e7eb;
  color: #4b5563;
  font-size: 12px;
  padding: 0 8px;
  height: 20px;
  line-height: 20px;
  border-radius: 10px;
  font-weight: 500;
}

.category-desc {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.category-action {
  color: #6b7280;
}
</style>
