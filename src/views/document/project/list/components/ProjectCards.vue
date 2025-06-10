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
              <div class="project-code">{{ project.projectCode }}</div>
            </div>
            <div class="project-status">
              <el-tag :type="getStatusType(project.status)">
                {{ project.status }}
              </el-tag>
            </div>
          </div>

          <div class="card-content">
            <div class="info-item">
              <span class="label">项目类型：</span>
              <span class="value">{{ project.type }}</span>
            </div>
            <div class="info-item">
              <span class="label">所属部门：</span>
              <span class="value">{{ project.department }}</span>
            </div>
            <div class="info-item">
              <span class="label">负责人：</span>
              <div class="user-info">
                <el-avatar :size="24" :src="project.ownerAvatar">
                  {{ project.owner?.charAt(0) }}
                </el-avatar>
                <span>{{ project.owner }}</span>
              </div>
            </div>
            <div class="info-item">
              <span class="label">开始时间：</span>
              <span class="value">{{ project.startDate }}</span>
            </div>
            <div class="info-item">
              <span class="label">结束时间：</span>
              <span class="value">{{ project.endDate }}</span>
            </div>
          </div>

          <div class="card-footer">
            <div class="footer-info">
              <span class="create-time">创建时间：{{ project.createTime }}</span>
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
    '未开始': 'info',
    '进行中': 'success',
    '已完成': '',
    '已终止': 'danger'
  }
  return typeMap[status] || 'info'
}

// 处理卡片点击
const handleCardClick = (project: Project) => {
  handleViewDocuments(project)
}

// 处理查看文档
const handleViewDocuments = (project: Project) => {
  router.push({
    name: 'document-list',
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