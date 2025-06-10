<template>
  <div class="project-cards">
    <el-row :gutter="16">
      <el-col
        v-for="project in projects"
        :key="project.id"
        :xs="24"
        :sm="12"
        :md="8"
        :lg="6"
        :xl="4"
      >
        <el-card
          class="project-card"
          :body-style="{ padding: '0px' }"
          @click="handleCardClick(project)"
        >
          <div class="card-header">
            <div class="project-info">
              <div class="project-title" :title="project.name">
                {{ project.name }}
              </div>
              <div class="project-code">{{ project.projectNum }}</div>
            </div>
            <div class="project-status">
              <el-tag :type="getStatusType(project.status)">
                {{ project.status }}
              </el-tag>
            </div>
          </div>

          <div class="card-content">
            <div class="info-item">
              <span class="label">优先级：</span>
              <el-tag size="small" :type="getPriorityType(project.priority)">
                {{ project.priority }}
              </el-tag>
            </div>
            <div class="info-item">
              <span class="label">负责人：</span>
              <div class="user-info">
                <el-avatar :size="24" :src="project.directorHeadImg">
                  {{ project.directorName?.charAt(0) }}
                </el-avatar>
                <span>{{ project.directorName }}</span>
              </div>
            </div>
            <div class="info-item">
              <span class="label">文档数：</span>
              <span class="value badge">{{ project.documentCount }}</span>
            </div>
            <div class="info-item">
              <span class="label">成员数：</span>
              <span class="value badge">{{ project.userCount }}</span>
            </div>
            <div class="info-item">
              <span class="label">开始时间：</span>
              <span class="value">{{ project.startTime }}</span>
            </div>
            <div class="info-item">
              <span class="label">结束时间：</span>
              <span class="value">{{ project.endTime }}</span>
            </div>
          </div>

          <div class="card-footer">
            <div class="footer-info">
              <span class="create-time">创建：{{ project.creator || '无' }} | {{ project.createTime }}</span>
            </div>
            <div class="footer-actions">
              <el-button
                type="primary"
                link
                @click.stop="handleViewDocuments(project)"
              >
                查看文档
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import type { Project } from '@/types/document'

defineProps<{
  projects: Project[]
}>()

const router = useRouter()

// 获取状态类型
const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'SURVEY': 'info',     // 调研中
    'ONGOING': 'success', // 进行中
    'COMPLETED': '',      // 已完成
    'PAUSED': 'warning',  // 已暂停
    'CANCELLED': 'danger' // 已取消
  }
  return typeMap[status] || 'info'
}

// 获取优先级类型
const getPriorityType = (priority: string) => {
  const typeMap: Record<string, string> = {
    'P0': 'danger',  // 最高
    'P1': 'warning', // 高
    'P2': 'success', // 中
    'P3': 'info'     // 低
  }
  return typeMap[priority] || 'info'
}

// 处理卡片点击
const handleCardClick = (project: Project) => {
  handleViewDocuments(project)
}

// 处理查看文档
const handleViewDocuments = (project: Project) => {
  router.push({
    name: 'DocumentList',
    params: {
      projectId: project.id
    },
    query: {
      projectName: project.name
    }
  })
}
</script>

<style lang="scss" scoped>
// 定义mixins
@mixin text-overflow {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.project-cards {
  .project-card {
    height: 100%;
    cursor: pointer;
    transition: transform 0.3s;

    &:hover {
      transform: translateY(-4px);
    }

    .card-header {
      padding: 16px;
      border-bottom: 1px solid var(--el-border-color-lighter);
      background-color: var(--el-bg-color-overlay);

      .project-info {
        margin-bottom: 12px;

        .project-title {
          font-size: 16px;
          font-weight: 500;
          color: var(--el-text-color-primary);
          margin-bottom: 4px;
          @include text-overflow;
        }

        .project-code {
          font-size: 13px;
          color: var(--el-text-color-secondary);
        }
      }
    }

    .card-content {
      padding: 16px;

      .info-item {
        display: flex;
        align-items: center;
        margin-bottom: 8px;
        font-size: 13px;

        &:last-child {
          margin-bottom: 0;
        }

        .label {
          color: var(--el-text-color-regular);
          margin-right: 8px;
          flex-shrink: 0;
        }

        .value {
          color: var(--el-text-color-primary);
          @include text-overflow;
          
          &.badge {
            background-color: var(--el-color-primary-light-8);
            color: var(--el-color-primary);
            border-radius: 12px;
            padding: 2px 8px;
            font-size: 12px;
            display: inline-block;
            font-weight: 500;
          }
        }

        .user-info {
          display: flex;
          align-items: center;
          gap: 8px;
          color: var(--el-text-color-primary);
        }
      }
    }

    .card-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      border-top: 1px solid var(--el-border-color-lighter);
      background-color: var(--el-fill-color-light);

      .footer-info {
        .create-time {
          font-size: 12px;
          color: var(--el-text-color-secondary);
        }
      }
    }
  }
}
</style> 