<template>
  <div class="project-detail-root">
    <!-- 顶部项目信息 -->
    <div class="project-header">
      <div class="header-main">
        <h1 class="project-title">
          {{ project?.title || '项目名称' }}
          <el-tag :type="getStatusType(project?.status)" class="status-tag">{{ project?.status || '进行中' }}</el-tag>
        </h1>
        <div class="project-meta">
          <span><el-icon><office-building /></el-icon> {{ project?.client || '归属单位' }}</span>
          <span><el-icon><calendar /></el-icon> 截止：{{ project?.dueDate || '-' }}</span>
          <span>
            <el-tag :type="getRiskType(project?.risk)" size="small">{{ project?.risk || '风险' }}</el-tag>
          </span>
          <span><el-icon><document /></el-icon> {{ project?.documents || 0 }} 个文档</span>
          <span><el-icon><user /></el-icon> {{ project?.members?.length || 0 }} 位成员</span>
        </div>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleEdit">编辑项目</el-button>
        <el-dropdown>
          <el-button icon="el-icon-more" />
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="goToRisk">风险分析</el-dropdown-item>
              <el-dropdown-item @click="goToTeam">团队管理</el-dropdown-item>
              <el-dropdown-item>导出数据</el-dropdown-item>
              <el-dropdown-item divided>归档项目</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- Tab导航 -->
    <el-tabs v-model="activeTab" class="project-tabs">
      <el-tab-pane label="概览" name="overview">
        <div class="overview-grid">
          <!-- 进度 -->
          <div class="overview-card progress-card">
            <div class="card-title">项目进度</div>
            <div class="progress-bar">
              <span>整体进度</span>
              <el-progress :percentage="project?.progress || 0" />
            </div>
            <div class="progress-metrics">
              <div class="metric">
                <el-icon><finished /></el-icon>
                <div>
                  <div class="metric-value">{{ project?.tasksCompleted || 0 }}/{{ project?.tasksTotal || 0 }}</div>
                  <div class="metric-label">已完成任务</div>
                </div>
              </div>
              <div class="metric">
                <el-icon><warning /></el-icon>
                <div>
                  <div class="metric-value">{{ project?.openRisks || 0 }}</div>
                  <div class="metric-label">未解决风险</div>
                </div>
              </div>
              <div class="metric">
                <el-icon><timer /></el-icon>
                <div>
                  <div class="metric-value">{{ project?.daysRemaining || 0 }}</div>
                  <div class="metric-label">剩余天数</div>
                </div>
              </div>
            </div>
          </div>
          <!-- 风险摘要 -->
          <div class="overview-card risk-card">
            <div class="card-title">风险摘要</div>
            <div class="risk-list">
              <div class="risk-item high">高风险 <span>{{ project?.riskHigh || 0 }}</span></div>
              <div class="risk-item medium">中风险 <span>{{ project?.riskMedium || 0 }}</span></div>
              <div class="risk-item low">低风险 <span>{{ project?.riskLow || 0 }}</span></div>
            </div>
            <el-button type="warning" link @click="goToRisk">查看风险分析</el-button>
          </div>
          <!-- 时间线 -->
          <div class="overview-card timeline-card">
            <div class="card-title">项目时间线</div>
            <el-timeline>
              <el-timeline-item
                v-for="(item, idx) in project?.timeline || []"
                :key="idx"
                :timestamp="item.date"
                :type="item.status === 'Completed' ? 'success' : 'info'"
              >
                <div>
                  <div class="timeline-title">{{ item.title }}</div>
                  <div class="timeline-status" :class="item.status">
                    {{ item.status === 'Completed' ? '已完成' : '待完成' }}
                  </div>
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>
          <!-- 最近文档 -->
          <div class="overview-card docs-card">
            <div class="card-title">最近文档</div>
            <ul class="doc-list">
              <li v-for="doc in project?.recentDocs || []" :key="doc.id">
                <el-icon><document /></el-icon>
                <span class="doc-title">{{ doc.title }}</span>
                <span class="doc-meta">{{ doc.updatedAt }} · {{ doc.author }}</span>
              </li>
            </ul>
            <el-button link @click="goToDocs">查看全部文档</el-button>
          </div>
          <!-- 快捷操作 -->
          <div class="overview-card quick-card">
            <div class="card-title">快捷操作</div>
            <el-button text @click="goToDocs"><el-icon><edit /></el-icon> 编辑文档</el-button>
            <el-button text @click="goToRisk"><el-icon><warning /></el-icon> 风险分析</el-button>
            <el-button text @click="goToTeam"><el-icon><user /></el-icon> 团队管理</el-button>
            <el-button text><el-icon><data-analysis /></el-icon> 查看分析</el-button>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="文档" name="documents"></el-tab-pane>
      <el-tab-pane label="风险分析" name="risk"></el-tab-pane>
      <el-tab-pane label="团队" name="team"></el-tab-pane>
      <el-tab-pane label="时间线" name="timeline"></el-tab-pane>
      <el-tab-pane label="讨论" name="discussions"></el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { OfficeBuilding, Calendar, Document, User, Finished, Warning, Timer, Edit, DataAnalysis } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import dialogInstance from '@/hooks/useDialog'
import { getProjectDetail } from '@/api/project'
import type { Project } from '@/api/project'

const route = useRoute()
const router = useRouter()
const activeTab = ref('overview')
const project = ref<any>(null)

const getStatusType = (status?: string) => {
  const typeMap: Record<string, string> = {
    '进行中': 'primary',
    '已完成': 'success',
    '待审核': 'warning',
    '已暂停': 'info'
  }
  return typeMap[status || ''] || 'info'
}
const getRiskType = (risk?: string) => {
  const typeMap: Record<string, string> = {
    '低': 'success',
    '中': 'warning',
    '高': 'danger'
  }
  return typeMap[risk || ''] || 'info'
}

const handleEdit = () => {
  if (!project.value) return
  dialogInstance.open('projectCreate', 
    { initialData: project.value }, 
    {
      submit: () => fetchProjectDetail(),
      error: (error: Error) => ElMessage.error(error.message)
    }
  )
}
const goToRisk = () => router.push(`/project/detail/${project.value?.id}?tab=risk`)
const goToTeam = () => router.push(`/project/detail/${project.value?.id}?tab=team`)
const goToDocs = () => router.push(`/project/detail/${project.value?.id}?tab=documents`)

const fetchProjectDetail = async () => {
  const projectId = route.params.id
  // 实际开发请替换为真实接口
  const res = await getProjectDetail(Number(projectId))
  // 这里建议后端返回的数据结构尽量贴合页面展示
  project.value = {
    ...res.data,
    // mock数据补充
    tasksCompleted: 18,
    tasksTotal: 24,
    openRisks: 4,
    daysRemaining: 13,
    riskHigh: 1,
    riskMedium: 3,
    riskLow: 5,
    timeline: [
      { date: 'May 15, 2025', title: 'Project Started', status: 'Completed' },
      { date: 'May 25, 2025', title: 'Requirements Analysis', status: 'Completed' },
      { date: 'Jun 5, 2025', title: 'Technical Proposal Submission', status: 'Upcoming' },
      { date: 'Jun 15, 2025', title: 'Final Bid Submission', status: 'Upcoming' }
    ],
    recentDocs: [
      { id: 1, title: 'Technical Proposal', updatedAt: '2 hours ago', author: 'Sarah Chen' },
      { id: 2, title: 'Pricing Sheet', updatedAt: 'Yesterday', author: 'Michael Johnson' },
      { id: 3, title: 'Compliance Checklist', updatedAt: '2 days ago', author: 'Emily Wong' }
    ]
  }
}

onMounted(() => {
  fetchProjectDetail()
})
</script>

<style lang="scss" scoped>
.project-detail-root {
  padding: 15px;
  background: #fafbfc;
  min-height: 100vh;
}
.project-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  .header-main {
    flex: 1;
    .project-title {
      font-size: 2rem;
      font-weight: 700;
      margin-bottom: 8px;
      display: flex;
      align-items: center;
      gap: 12px;
    }
    .status-tag {
      font-size: 1rem;
      margin-left: 8px;
    }
    .project-meta {
      display: flex;
      gap: 24px;
      color: #888;
      font-size: 15px;
      margin-top: 8px;
      span {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }
  .header-actions {
    display: flex;
    gap: 8px;
    align-items: center;
  }
}
.project-tabs {
  background: #fff;
  border-radius: 8px;
  padding: 0 0 24px 0;
  box-shadow: 0 2px 8px #0001;
}
.overview-grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  grid-template-rows: auto auto;
  gap: 24px;
  margin: 24px 0 0 0;
  .overview-card {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 1px 4px #0001;
    padding: 24px;
    min-height: 180px;
    .card-title {
      font-weight: 600;
      font-size: 1.1rem;
      margin-bottom: 16px;
    }
  }
  .progress-card {
    grid-column: 1/2;
    grid-row: 1/2;
    .progress-bar {
      margin-bottom: 16px;
      span {
        font-size: 14px;
        color: #888;
      }
    }
    .progress-metrics {
      display: flex;
      gap: 32px;
      .metric {
        display: flex;
        align-items: center;
        gap: 10px;
        .metric-value {
          font-size: 1.3rem;
          font-weight: 600;
        }
        .metric-label {
          font-size: 13px;
          color: #888;
        }
      }
    }
  }
  .risk-card {
    grid-column: 2/3;
    grid-row: 1/2;
    .risk-list {
      margin-bottom: 16px;
      .risk-item {
        display: flex;
        justify-content: space-between;
        font-size: 15px;
        margin-bottom: 6px;
        &.high { color: #f56c6c; }
        &.medium { color: #e6a23c; }
        &.low { color: #67c23a; }
      }
    }
  }
  .timeline-card {
    grid-column: 1/2;
    grid-row: 2/3;
    .timeline-title {
      font-weight: 600;
    }
    .timeline-status {
      font-size: 13px;
      &.Completed { color: #67c23a; }
      &.Upcoming { color: #409eff; }
    }
  }
  .docs-card {
    grid-column: 2/3;
    grid-row: 2/3;
    .doc-list {
      list-style: none;
      padding: 0;
      margin: 0 0 12px 0;
      li {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 15px;
        margin-bottom: 6px;
        .doc-title { font-weight: 500; }
        .doc-meta { color: #888; font-size: 13px; margin-left: 8px; }
      }
    }
  }
  .quick-card {
    grid-column: 3/4;
    grid-row: 1/3;
    display: flex;
    flex-direction: column;
    gap: 12px;
    .el-button {
      justify-content: flex-start;
      font-size: 15px;
    }
  }
}
</style> 