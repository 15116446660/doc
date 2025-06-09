<template>
  <div class="project-detail-container">
    <!-- 项目标题区域 -->
    <div class="project-header">
      <div class="header-left">
        <h1 class="project-title">{{ project?.title || '项目名称' }}</h1>
        <span class="status-tag">进行中</span>
      </div>
      <div class="header-right">
        <el-button type="primary" class="edit-btn" @click="handleEdit">编辑项目</el-button>
        <el-dropdown trigger="click">
          <el-button plain class="more-btn">
            <el-icon><more-filled /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="goToRiskAnalysis">风险分析</el-dropdown-item>
              <el-dropdown-item @click="manageTeam">管理团队</el-dropdown-item>
              <el-dropdown-item @click="exportData">导出数据</el-dropdown-item>
              <el-dropdown-item divided @click="archiveProject">归档项目</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 项目元数据区域 -->
    <div class="project-meta">
      <div class="meta-item">
        <el-icon><office-building /></el-icon>
        <span>{{ project?.client || '未设置客户' }}</span>
      </div>
      <div class="meta-item">
        <el-icon><calendar /></el-icon>
        <span>截止日期: {{ project?.dueDate || '未设置' }}</span>
      </div>
      <div class="meta-item">
        <el-icon><warning /></el-icon>
        <span class="risk-label">{{ project?.risk || '中等风险' }}</span>
      </div>
      <div class="meta-item">
        <el-icon><document /></el-icon>
        <span>{{ project?.documents || 0 }} 文档</span>
      </div>
      <div class="meta-item">
        <el-icon><user /></el-icon>
        <span>{{ project?.teamMembers || 0 }} 团队成员</span>
      </div>
    </div>

    <!-- 标签页导航 -->
    <div class="project-tabs">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="概览" name="overview">
          <div class="tab-content-grid">
            <!-- 项目进度区域 -->
            <div class="content-card progress-card">
              <div class="card-header">
                <h2>项目进度</h2>
                <p class="card-subtitle">总体完成度和关键指标</p>
              </div>
              <div class="card-body">
                <div class="progress-label">总体进度</div>
                <el-progress :percentage="project?.progress || 75" />
                
                <div class="metrics-grid">
                  <div class="metric-card">
                    <div class="metric-icon completed">
                      <el-icon><check /></el-icon>
                    </div>
                    <div class="metric-content">
                      <div class="metric-value">{{ project?.tasksCompleted || 18 }}/{{ project?.tasksTotal || 24 }}</div>
                      <div class="metric-label">已完成任务</div>
                    </div>
                  </div>
                  
                  <div class="metric-card">
                    <div class="metric-icon risks">
                      <el-icon><warning /></el-icon>
                    </div>
                    <div class="metric-content">
                      <div class="metric-value">{{ project?.openRisks || 4 }}</div>
                      <div class="metric-label">风险项</div>
                    </div>
                  </div>
                  
                  <div class="metric-card">
                    <div class="metric-icon days">
                      <el-icon><calendar /></el-icon>
                    </div>
                    <div class="metric-content">
                      <div class="metric-value">{{ project?.daysRemaining || 13 }}</div>
                      <div class="metric-label">剩余天数</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 风险摘要区域 -->
            <div class="content-card risk-card">
              <div class="card-header">
                <h2>风险摘要</h2>
                <p class="card-subtitle">当前风险评估</p>
              </div>
              <div class="card-body">
                <div class="risk-list">
                  <div class="risk-item">
                    <div class="risk-info">
                      <span class="risk-dot high"></span>
                      <span class="risk-name">高风险</span>
                    </div>
                    <span class="risk-count">{{ project?.highRisk || 1 }}</span>
                  </div>
                  
                  <div class="risk-item">
                    <div class="risk-info">
                      <span class="risk-dot medium"></span>
                      <span class="risk-name">中等风险</span>
                    </div>
                    <span class="risk-count">{{ project?.mediumRisk || 3 }}</span>
                  </div>
                  
                  <div class="risk-item">
                    <div class="risk-info">
                      <span class="risk-dot low"></span>
                      <span class="risk-name">低风险</span>
                    </div>
                    <span class="risk-count">{{ project?.lowRisk || 5 }}</span>
                  </div>
                </div>
                
                <el-button link type="warning" class="risk-action" @click="goToRiskAnalysis">
                  <el-icon><warning /></el-icon>
                  查看风险分析
                </el-button>
              </div>
            </div>
            
            <!-- 项目时间线区域 -->
            <div class="content-card timeline-card">
              <div class="card-header">
                <h2>项目时间线</h2>
                <p class="card-subtitle">关键里程碑和截止日期</p>
              </div>
              <div class="card-body">
                <el-timeline>
                  <el-timeline-item
                    v-for="(item, index) in timelineData"
                    :key="index"
                    :type="item.status === 'completed' ? 'success' : 'primary'"
                    :timestamp="item.date"
                    size="large"
                  >
                    <div class="timeline-content">
                      <div class="timeline-title">{{ item.title }}</div>
                      <div class="timeline-status" :class="item.status">{{ getStatusLabel(item.status) }}</div>
                    </div>
                  </el-timeline-item>
                </el-timeline>
              </div>
            </div>
            
            <!-- 最近文档区域 -->
            <div class="content-card documents-card">
              <div class="card-header">
                <h2>最近文档</h2>
                <p class="card-subtitle">最新更新的文件</p>
              </div>
              <div class="card-body">
                <div class="document-list">
                  <div v-for="(doc, index) in recentDocs" :key="index" class="document-item">
                    <el-icon><document /></el-icon>
                    <div class="document-info">
                      <div class="document-title">{{ doc.title }}</div>
                      <div class="document-meta">更新于 {{ doc.updatedTime }} 由 {{ doc.author }}</div>
                    </div>
                  </div>
                </div>
                
                <el-button link type="primary" class="documents-action" @click="viewAllDocuments">
                  <el-icon><document /></el-icon>
                  查看全部文档
                </el-button>
              </div>
            </div>
            
            <!-- 快捷操作区域 -->
            <div class="content-card actions-card">
              <div class="card-header">
                <h2>快捷操作</h2>
              </div>
              <div class="card-body">
                <div class="action-list">
                  <el-button class="action-btn" @click="editDocuments">
                    <el-icon><edit-pen /></el-icon>
                    编辑文档
                  </el-button>
                  
                  <el-button class="action-btn" @click="runRiskAnalysis">
                    <el-icon><warning /></el-icon>
                    风险分析
                  </el-button>
                  
                  <el-button class="action-btn" @click="manageTeam">
                    <el-icon><user /></el-icon>
                    管理团队
                  </el-button>
                  
                  <el-button class="action-btn" @click="viewAnalytics">
                    <el-icon><data-line /></el-icon>
                    查看分析
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="文档" name="documents">
          <div class="tab-placeholder">
            <el-empty description="文档内容开发中...">
              <el-button type="primary">上传文档</el-button>
            </el-empty>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="风险分析" name="risk-analysis">
          <div class="tab-placeholder">
            <el-empty description="风险分析内容开发中...">
              <el-button type="primary">添加风险项</el-button>
            </el-empty>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="团队" name="team">
          <div class="tab-placeholder">
            <el-empty description="团队内容开发中...">
              <el-button type="primary">添加成员</el-button>
            </el-empty>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="时间线" name="timeline">
          <div class="tab-placeholder">
            <el-empty description="时间线内容开发中...">
              <el-button type="primary">添加事件</el-button>
            </el-empty>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="讨论" name="discussions">
          <div class="tab-placeholder">
            <el-empty description="讨论内容开发中...">
              <el-button type="primary">发起讨论</el-button>
            </el-empty>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { 
  OfficeBuilding, Calendar, Document, User, Warning, 
  Check, MoreFilled, EditPen, DataLine
} from '@element-plus/icons-vue'
import dialogInstance from '@/hooks/useDialog'
import { getProjectDetail } from '@/api/project'

const route = useRoute()
const router = useRouter()
const activeTab = ref('overview')
const project = ref<any>(null)

// 时间线数据
const timelineData = ref([
  {
    date: '2023/05/15',
    title: '项目启动',
    status: 'completed'
  },
  {
    date: '2023/05/25',
    title: '需求分析',
    status: 'completed'
  },
  {
    date: '2023/06/05',
    title: '技术方案提交',
    status: 'upcoming'
  },
  {
    date: '2023/06/15',
    title: '最终投标提交',
    status: 'upcoming'
  }
])

// 最近文档
const recentDocs = ref([
  {
    title: '技术方案',
    updatedTime: '2小时前',
    author: '陈小明'
  },
  {
    title: '价格表',
    updatedTime: '昨天',
    author: '张经理'
  },
  {
    title: '合规检查表',
    updatedTime: '2天前',
    author: '王工'
  }
])

// 获取项目详情
const fetchProjectDetail = async () => {
  try {
    const projectId = route.params.id
    const res = await getProjectDetail(Number(projectId))
    project.value = {
      ...res,
      progress: 75,
      tasksCompleted: 18,
      tasksTotal: 24,
      openRisks: 4,
      daysRemaining: 13,
      highRisk: 1,
      mediumRisk: 3,
      lowRisk: 5,
      documents: 12,
      teamMembers: 4
    }
  } catch (error) {
    console.error('获取项目详情失败:', error)
    ElMessage.error('获取项目详情失败')
  }
}

// 获取状态标签文本
const getStatusLabel = (status: string) => {
  switch (status) {
    case 'completed':
      return '已完成'
    case 'upcoming':
      return '即将开始'
    case 'in-progress':
      return '进行中'
    default:
      return '未开始'
  }
}

// 编辑项目
const handleEdit = () => {
  if (!project.value) return
  dialogInstance.open('projectCreate', 
    { initialData: project.value }, 
    {
      submit: () => fetchProjectDetail(),
      error: (err: Error) => ElMessage.error(err.message)
    }
  )
}

// 前往风险分析
const goToRiskAnalysis = () => {
  activeTab.value = 'risk-analysis'
}

// 管理团队
const manageTeam = () => {
  activeTab.value = 'team'
}

// 导出数据
const exportData = () => {
  ElMessage.success('导出数据功能即将上线')
}

// 归档项目
const archiveProject = () => {
  ElMessageBox.confirm(
    '归档后的项目将移至归档列表，确定要归档该项目吗？',
    '归档确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(() => {
      ElMessage.success('项目已归档')
    })
    .catch(() => {})
}

// 编辑文档
const editDocuments = () => {
  activeTab.value = 'documents'
}

// 运行风险分析
const runRiskAnalysis = () => {
  activeTab.value = 'risk-analysis'
}

// 查看分析
const viewAnalytics = () => {
  ElMessage.success('分析功能即将上线')
}

// 查看所有文档
const viewAllDocuments = () => {
  activeTab.value = 'documents'
}

onMounted(() => {
  fetchProjectDetail()
})
</script>

<style lang="scss" scoped>
.project-detail-container {
  padding: 12px;
//   max-width: 1200px;
  margin: 0 auto;
  
  // 项目标题区域
  .project-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    .header-left {
      display: flex;
      align-items: center;
      
      .project-title {
        font-size: 24px;
        font-weight: 600;
        margin: 0;
        margin-right: 12px;
      }
      
      .status-tag {
        background-color: #3b82f6;
        color: white;
        padding: 4px 12px;
        border-radius: 4px;
        font-size: 14px;
      }
    }
    
    .header-right {
      display: flex;
      gap: 8px;
      
      .edit-btn {
        font-weight: 500;
      }
      
      .more-btn {
        padding: 8px;
      }
    }
  }
  
  // 项目元数据区域
  .project-meta {
    display: flex;
    gap: 24px;
    margin-bottom: 24px;
    
    .meta-item {
      display: flex;
      align-items: center;
      gap: 6px;
      color: #6b7280;
      font-size: 14px;
      
      .el-icon {
        color: #6b7280;
      }
      
      .risk-label {
        color: #f59e0b;
      }
    }
  }
  
  // 标签页导航
  .project-tabs {
    // background-color: white;
    // border-radius: 8px;
    // box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    
    :deep(.el-tabs__header) {
      margin-bottom: 0;
      padding: 0 16px;
    }
    
    :deep(.el-tabs__nav-wrap) {
      padding: 8px 0;
    }
    
    :deep(.el-tabs__content) {
      padding: 20px;
    }
    
    .tab-placeholder {
      padding: 40px;
      display: flex;
      justify-content: center;
      align-items: center;
    }
    
    .tab-content-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      grid-template-rows: auto auto;
      gap: 20px;
      
      // 项目进度卡片
      .progress-card {
        grid-column: 1;
        grid-row: 1;
        
        .progress-label {
          margin-bottom: 8px;
          color: #6b7280;
          font-size: 14px;
        }
        
        .metrics-grid {
          display: grid;
          grid-template-columns: repeat(3, 1fr);
          gap: 16px;
          margin-top: 24px;
          
          .metric-card {
            display: flex;
            align-items: center;
            gap: 12px;
            
            .metric-icon {
              width: 40px;
              height: 40px;
              border-radius: 8px;
              display: flex;
              align-items: center;
              justify-content: center;
              
              &.completed {
                background-color: #10b981;
                color: white;
              }
              
              &.risks {
                background-color: #f59e0b;
                color: white;
              }
              
              &.days {
                background-color: #3b82f6;
                color: white;
              }
            }
            
            .metric-content {
              .metric-value {
                font-size: 18px;
                font-weight: 600;
                color: #111827;
              }
              
              .metric-label {
                font-size: 12px;
                color: #6b7280;
              }
            }
          }
        }
      }
      
      // 风险摘要卡片
      .risk-card {
        grid-column: 2;
        grid-row: 1;
        
        .risk-list {
          margin-bottom: 24px;
          
          .risk-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 12px;
            
            .risk-info {
              display: flex;
              align-items: center;
              gap: 8px;
              
              .risk-dot {
                width: 12px;
                height: 12px;
                border-radius: 50%;
                
                &.high {
                  background-color: #ef4444;
                }
                
                &.medium {
                  background-color: #f59e0b;
                }
                
                &.low {
                  background-color: #10b981;
                }
              }
              
              .risk-name {
                font-size: 14px;
                color: #374151;
              }
            }
            
            .risk-count {
              font-weight: 600;
              font-size: 16px;
              color: #111827;
            }
          }
        }
        
        .risk-action {
          color: #f59e0b;
          font-weight: 500;
        }
      }
      
      // 项目时间线卡片
      .timeline-card {
        grid-column: 1;
        grid-row: 2;
        
        :deep(.el-timeline) {
          padding-left: 0;
        }
        
        :deep(.el-timeline-item__node) {
          background-color: #3b82f6;
        }
        
        :deep(.el-timeline-item__node--success) {
          background-color: #10b981;
        }
        
        :deep(.el-timeline-item__tail) {
          border-left-color: #e5e7eb;
        }
        
        :deep(.el-timeline-item__timestamp) {
          color: #6b7280;
          font-size: 12px;
          margin-top: 0;
        }
        
        .timeline-content {
          .timeline-title {
            font-weight: 500;
            color: #111827;
            margin-bottom: 2px;
          }
          
          .timeline-status {
            font-size: 12px;
            
            &.completed {
              color: #10b981;
            }
            
            &.upcoming {
              color: #3b82f6;
            }
          }
        }
      }
      
      // 最近文档卡片
      .documents-card {
        grid-column: 2;
        grid-row: 2;
        
        .document-list {
          margin-bottom: 24px;
          
          .document-item {
            display: flex;
            gap: 12px;
            padding: 8px 0;
            border-bottom: 1px solid #e5e7eb;
            
            &:last-child {
              border-bottom: none;
            }
            
            .el-icon {
              color: #3b82f6;
            }
            
            .document-info {
              .document-title {
                font-weight: 500;
                color: #111827;
                margin-bottom: 2px;
              }
              
              .document-meta {
                font-size: 12px;
                color: #6b7280;
              }
            }
          }
        }
        
        .documents-action {
          color: #3b82f6;
          font-weight: 500;
        }
      }
      
      // 快捷操作卡片
      .actions-card {
        grid-column: 1 / 3;
        grid-row: 3;
        
        .action-list {
          display: flex;
          justify-content: space-between;
          gap: 16px;
          
          .action-btn {
            flex: 1;
            justify-content: flex-start;
            font-weight: 500;
            padding: 12px 16px;
            border-radius: 8px;
            border: 1px solid #e5e7eb;
            
            .el-icon {
              margin-right: 8px;
            }
          }
        }
      }
    }
  }
  
  // 共用卡片样式
  .content-card {
    background-color: white;
    border-radius: 8px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    padding: 16px;
    
    .card-header {
      margin-bottom: 16px;
      
      h2 {
        font-size: 16px;
        font-weight: 600;
        color: #111827;
        margin: 0 0 4px 0;
      }
      
      .card-subtitle {
        color: #6b7280;
        font-size: 14px;
        margin: 0;
      }
    }
  }
}

@media (max-width: 768px) {
  .project-detail-container {
    .project-meta {
      flex-wrap: wrap;
      row-gap: 12px;
    }
    
    .project-tabs {
      .tab-content-grid {
        grid-template-columns: 1fr;
        
        .progress-card,
        .risk-card,
        .timeline-card,
        .documents-card,
        .actions-card {
          grid-column: 1;
        }
        
        .risk-card {
          grid-row: 2;
        }
        
        .timeline-card {
          grid-row: 3;
        }
        
        .documents-card {
          grid-row: 4;
        }
        
        .actions-card {
          grid-row: 5;
          
          .action-list {
            flex-wrap: wrap;
            
            .action-btn {
              flex: 1 0 45%;
            }
          }
        }
      }
    }
  }
}
</style> 