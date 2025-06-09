<template>
  <el-card class="template-card" :body-style="{ padding: '0' }" @click="$emit('view', template)">
    <!-- 文档封面区域 -->
    <div class="document-cover">
      <!-- 文档图标 -->
      <div class="document-icon-container">
        <div class="document-icon">
          <el-icon :size="24"><Document /></el-icon>
        </div>
      </div>
      
      <!-- 中心图标 -->
      <div class="center-icon">
        <div class="starburst">
          <div v-for="n in 8" :key="n" class="ray"></div>
        </div>
        <el-icon :size="16"><Picture /></el-icon>
      </div>
      
      <!-- 下载量和收藏 -->
      <div class="header-actions">
        <div class="download-count">
          <el-icon :size="14"><Download /></el-icon>
          {{ template.downloads || 156 }}
        </div>
        <div class="favorite">
          <el-icon :size="20" :color="template.favorite ? '#F7BA2A' : '#909399'"><Star /></el-icon>
        </div>
      </div>
    </div>

    <!-- 文档信息区域 -->
    <div class="card-content">
      <!-- 标题和描述 -->
      <div class="document-info">
        <h3 class="title">{{ template.title || '产品需求文档模版' }}</h3>
        <p class="description">{{ template.description || '标准的产品需求文档模版，包含完整的需求分析框架' }}</p>
      </div>

      <!-- 评分 -->
      <div class="rating">
        <el-rate
          v-model="template.rating"
          disabled
          allow-half
          :size="16"
          :colors="['#F7BA2A', '#F7BA2A', '#F7BA2A']"
        />
        <span class="rating-value">({{ template.rating || 4.8 }})</span>
      </div>

      <!-- 作者和版本 -->
      <div class="author-info">
        <div class="author">
          <el-avatar :size="24" class="avatar">
            {{ template?.owner?.charAt(0) || '张' }}
          </el-avatar>
          <span class="author-name">{{ template.owner || '张小明' }}</span>
        </div>
        <div class="version">v{{ template.version || '2.1' }}</div>
      </div>

      <!-- 更新时间 -->
      <div class="update-time">
        <el-icon :size="14"><Clock /></el-icon>
        {{ formatDate(template.updateTime) || '2024-01-20' }}
      </div>

      <!-- 底部标签 -->
      <div class="tags">
        <span class="tag">需求</span>
        <span class="tag">产品</span>
        <span class="tag">文档</span>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { Document, Star, Download, Clock, Picture } from '@element-plus/icons-vue'
import type { Template } from '@/api/template'

defineProps<{
  template: Template
}>()

defineEmits<{
  (e: 'edit', template: Template): void
  (e: 'copy', template: Template): void
  (e: 'delete', template: Template): void
  (e: 'view', template: Template): void
}>()

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).replace(/\//g, '-')
}
</script>

<style lang="scss" scoped>
.template-card {
  height: auto;
  min-height: 380px;
  max-width: 280px;
  width: 100%;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: none;

  :deep(.el-card__body) {
    padding: 0;
  }

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);

    .document-cover {
      .document-icon {
        transform: scale(1.05);
        box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
      }
      
      .starburst {
        transform: rotate(45deg);
      }
    }
  }
}

.document-cover {
  height: 140px;
  background: #F5F7FA;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  position: relative;
  overflow: hidden;
}

.document-icon-container {
  position: absolute;
  top: 16px;
  left: 16px;
  z-index: 2;
}

.document-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #EBF3FF;
  border-radius: 12px;
  transition: all 0.3s ease;

  :deep(.el-icon) {
    color: #409EFF;
    transition: all 0.3s ease;
  }
}

.center-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: white;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.05);
  z-index: 1;
  
  .el-icon {
    color: #C0C4CC;
  }
  
  .starburst {
    position: absolute;
    width: 100%;
    height: 100%;
    transition: transform 0.5s ease;
    
    .ray {
      position: absolute;
      top: 50%;
      left: 50%;
      width: 60px;
      height: 1px;
      background: linear-gradient(to right, rgba(0,0,0,0.03), transparent);
      transform-origin: 0 0;
      
      @for $i from 0 through 7 {
        &:nth-child(#{$i + 1}) {
          transform: rotate(#{$i * 45}deg) translateX(-50%);
        }
      }
    }
  }
}

.header-actions {
  position: absolute;
  top: 16px;
  right: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  z-index: 2;
}

.download-count {
  height: 28px;
  display: flex;
  align-items: center;
  gap: 4px;
  background-color: rgba(0, 0, 0, 0.2);
  color: white;
  padding: 6px 12px;
  border-radius: 14px;
  font-size: 13px;
  transition: all 0.3s ease;

  &:hover {
    background-color: rgba(0, 0, 0, 0.25);
  }

  .el-icon {
    transition: transform 0.3s ease;
  }

  &:hover .el-icon {
    transform: translateY(-1px);
  }
}

.card-content {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background-color: #fff;
}

.document-info {
  margin-bottom: 12px;
  
  .title {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    line-height: 24px;
  }

  .description {
    margin: 8px 0 0;
    font-size: 13px;
    color: #909399;
    line-height: 20px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
}

.rating {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;

  :deep(.el-rate__icon) {
    margin-right: 2px;
    font-size: 14px;
  }

  .rating-value {
    color: #909399;
    font-size: 13px;
  }
}

.author-info {
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .author {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .avatar {
    background-color: #F2F3F5;
    color: #606266;
    font-size: 12px;
    border: 2px solid #fff;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  }

  .author-name {
    font-size: 13px;
    color: #606266;
  }

  .version {
    font-size: 12px;
    color: #909399;
    background-color: #F2F3F5;
    padding: 2px 8px;
    border-radius: 12px;
    font-weight: 500;
  }
}

.update-time {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 12px;

  .el-icon {
    opacity: 0.8;
  }
}

.tags {
  display: flex;
  gap: 12px;

  .tag {
    color: #909399;
    font-size: 12px;
    background-color: #F7F8FA;
    padding: 2px 8px;
    border-radius: 4px;
    transition: all 0.3s ease;

    &:hover {
      background-color: #F2F3F5;
      color: #606266;
    }
  }
}
</style> 