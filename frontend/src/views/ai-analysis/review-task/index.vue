<template>
  <div class="review-task-page">
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><Document /></el-icon>
        AI评审任务
      </h1>
      <p class="page-description">创建和管理AI驱动的文档评审任务，实现智能化评审流程</p>
    </div>

    <!-- 任务创建/编辑 -->
    <el-card class="task-form-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>{{ isEditing ? '编辑评审任务' : '创建评审任务' }}</span>
          <el-button type="primary" @click="saveTask" :loading="saving">
            <el-icon><Check /></el-icon>
            {{ saving ? '保存中...' : '保存任务' }}
          </el-button>
        </div>
      </template>

      <div class="task-form">
        <el-form :model="taskForm" :rules="taskRules" ref="taskFormRef" label-width="120px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="任务名称" prop="taskName">
                <el-input v-model="taskForm.taskName" placeholder="输入评审任务名称" />
              </el-form-item>
              
              <el-form-item label="文档ID" prop="documentId">
                <el-input-number v-model="taskForm.documentId" :min="1" placeholder="文档ID" />
              </el-form-item>
              
              <el-form-item label="项目ID" prop="projectId">
                <el-input-number v-model="taskForm.projectId" :min="1" placeholder="项目ID" />
              </el-form-item>
              
              <el-form-item label="优先级" prop="priority">
                <el-select v-model="taskForm.priority" placeholder="选择优先级">
                  <el-option label="低" value="LOW" />
                  <el-option label="中" value="MEDIUM" />
                  <el-option label="高" value="HIGH" />
                  <el-option label="紧急" value="URGENT" />
                </el-select>
              </el-form-item>
            </el-col>
            
            <el-col :span="12">
              <el-form-item label="截止时间" prop="deadline">
                <el-date-picker
                  v-model="taskForm.deadline"
                  type="datetime"
                  placeholder="选择截止时间"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                />
              </el-form-item>
              
              <el-form-item label="启用AI分析">
                <el-switch v-model="taskForm.aiAnalysisEnabled" />
              </el-form-item>
              
              <el-form-item label="AI分析类型" v-if="taskForm.aiAnalysisEnabled">
                <el-checkbox-group v-model="taskForm.aiAnalysisTypes">
                  <el-checkbox label="CONTENT_REVERSE_INSERTION">内容反向插入</el-checkbox>
                  <el-checkbox label="INTELLIGENT_SLICE_TAGGING">智能切片标记</el-checkbox>
                  <el-checkbox label="PROMPT_TESTING">提示词测试</el-checkbox>
                  <el-checkbox label="CITATION_SOURCE_MARKING">引用来源标记</el-checkbox>
                  <el-checkbox label="SMART_FORMATTING">智能格式化</el-checkbox>
                  <el-checkbox label="LONG_TEXT_INTERACTION">长文本交互</el-checkbox>
                  <el-checkbox label="DOCUMENT_DIFFERENCE_COMPARISON">文档差异对比</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="任务描述" prop="taskDescription">
            <el-input
              v-model="taskForm.taskDescription"
              type="textarea"
              :rows="4"
              placeholder="描述评审任务的具体要求和目标..."
            />
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- AI分析控制面板 -->
    <el-card v-if="currentTask && currentTask.aiAnalysisEnabled" class="ai-control-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>AI分析控制</span>
          <div class="control-actions">
            <el-button 
              type="success" 
              @click="startAIAnalysis" 
              :loading="aiAnalyzing"
              :disabled="currentTask.aiAnalysisStatus === 'RUNNING'"
            >
              <el-icon><Play /></el-icon>
              {{ aiAnalyzing ? '分析中...' : '启动AI分析' }}
            </el-button>
            
            <el-button 
              type="warning" 
              @click="stopAIAnalysis"
              :disabled="currentTask.aiAnalysisStatus !== 'RUNNING'"
            >
              <el-icon><VideoPause /></el-icon>
              停止分析
            </el-button>
          </div>
        </div>
      </template>

      <div class="ai-control-content">
        <!-- AI分析状态 -->
        <div class="analysis-status">
          <el-row :gutter="16">
            <el-col :span="6">
              <div class="status-item">
                <div class="status-label">分析状态</div>
                <div class="status-value">
                  <el-tag :type="getAnalysisStatusType(currentTask.aiAnalysisStatus)">
                    {{ getAnalysisStatusLabel(currentTask.aiAnalysisStatus) }}
                  </el-tag>
                </div>
              </div>
            </el-col>
            
            <el-col :span="6">
              <div class="status-item">
                <div class="status-label">分析进度</div>
                <div class="status-value">
                  <el-progress 
                    :percentage="currentTask.aiAnalysisProgress || 0" 
                    :color="getProgressColor(currentTask.aiAnalysisProgress || 0)"
                  />
                </div>
              </div>
            </el-col>
            
            <el-col :span="6">
              <div class="status-item">
                <div class="status-label">已完成功能</div>
                <div class="status-value">{{ completedAnalysisCount }}/{{ totalAnalysisCount }}</div>
              </div>
            </el-col>
            
            <el-col :span="6">
              <div class="status-item">
                <div class="status-label">预计剩余时间</div>
                <div class="status-value">{{ estimatedTimeRemaining }}</div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- AI分析结果 -->
        <div v-if="aiAnalysisResults.length > 0" class="analysis-results">
          <h4>分析结果</h4>
          <el-tabs v-model="activeResultTab" type="card">
            <el-tab-pane 
              v-for="result in aiAnalysisResults" 
              :key="result.analysisType"
              :label="getAnalysisTypeLabel(result.analysisType)" 
              :name="result.analysisType"
            >
              <div class="result-content">
                <div class="result-header">
                  <h5>{{ result.analysisTitle }}</h5>
                  <div class="result-meta">
                    <el-tag :type="getRiskLevelType(result.riskLevel)" size="small">
                      {{ getRiskLevelLabel(result.riskLevel) }}
                    </el-tag>
                    <span class="confidence-score">
                      置信度: {{ (result.confidenceScore * 100).toFixed(1) }}%
                    </span>
                  </div>
                </div>
                
                <div class="result-summary">
                  <strong>摘要:</strong> {{ result.analysisSummary }}
                </div>
                
                <div class="result-detail">
                  <strong>详细分析:</strong>
                  <div class="analysis-text" v-html="formatAnalysisContent(result.analysisContent)"></div>
                </div>
                
                <div v-if="result.suggestions" class="result-suggestions">
                  <strong>建议:</strong>
                  <div class="suggestions-text">{{ result.suggestions }}</div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </el-card>

    <!-- 任务历史 -->
    <el-card class="task-history-card" shadow="hover">
      <template #header>
        <span>任务历史</span>
      </template>
      
      <div class="task-history">
        <el-timeline>
          <el-timeline-item
            v-for="(event, index) in taskHistory"
            :key="index"
            :timestamp="formatTime(event.timestamp)"
            :type="getEventType(event.type)"
          >
            <div class="history-event">
              <div class="event-title">{{ event.title }}</div>
              <div class="event-description">{{ event.description }}</div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Document, 
  Check, 
  Play, 
  VideoPause 
} from '@element-plus/icons-vue'
import { reviewTaskApi, aiTestApi, AIAnalysisTypeLabels, RiskLevelLabels, RiskLevelColors } from '@/api/ai-analysis'

const route = useRoute()
const taskFormRef = ref()

const saving = ref(false)
const aiAnalyzing = ref(false)
const isEditing = ref(false)
const activeResultTab = ref('')

const taskForm = reactive({
  taskName: '',
  taskDescription: '',
  documentId: 1,
  projectId: 1,
  priority: 'MEDIUM',
  deadline: '',
  aiAnalysisEnabled: true,
  aiAnalysisTypes: ['CONTENT_REVERSE_INSERTION', 'INTELLIGENT_SLICE_TAGGING']
})

const taskRules = {
  taskName: [
    { required: true, message: '请输入任务名称', trigger: 'blur' }
  ],
  documentId: [
    { required: true, message: '请输入文档ID', trigger: 'blur' }
  ],
  projectId: [
    { required: true, message: '请输入项目ID', trigger: 'blur' }
  ]
}

const currentTask = ref<any>(null)
const aiAnalysisResults = ref<any[]>([])
const taskHistory = ref([
  {
    type: 'created',
    title: '任务创建',
    description: '评审任务已创建',
    timestamp: new Date().toISOString()
  }
])

const completedAnalysisCount = computed(() => {
  return aiAnalysisResults.value.length
})

const totalAnalysisCount = computed(() => {
  return taskForm.aiAnalysisTypes.length
})

const estimatedTimeRemaining = computed(() => {
  if (!currentTask.value || currentTask.value.aiAnalysisStatus !== 'RUNNING') {
    return '未知'
  }
  
  const progress = currentTask.value.aiAnalysisProgress || 0
  if (progress === 0) return '计算中...'
  
  const remainingProgress = 100 - progress
  const estimatedMinutes = Math.ceil(remainingProgress / 10) // 假设每10%需要1分钟
  
  return `约 ${estimatedMinutes} 分钟`
})

const saveTask = async () => {
  if (!taskFormRef.value) return
  
  try {
    await taskFormRef.value.validate()
    
    saving.value = true
    
    if (isEditing.value) {
      // 更新任务逻辑
      ElMessage.success('任务更新成功')
    } else {
      // 创建任务
      const result = await reviewTaskApi.createTask(taskForm)
      currentTask.value = result
      isEditing.value = true
      
      taskHistory.value.unshift({
        type: 'created',
        title: '任务创建成功',
        description: `任务"${taskForm.taskName}"已成功创建`,
        timestamp: new Date().toISOString()
      })
      
      ElMessage.success('任务创建成功')
    }
  } catch (error) {
    ElMessage.error('保存失败，请检查输入信息')
  } finally {
    saving.value = false
  }
}

const startAIAnalysis = async () => {
  if (!currentTask.value) {
    ElMessage.warning('请先保存任务')
    return
  }

  aiAnalyzing.value = true
  
  try {
    // 启动AI分析
    await reviewTaskApi.startAIAnalysis(currentTask.value.id)
    
    currentTask.value.aiAnalysisStatus = 'RUNNING'
    currentTask.value.aiAnalysisProgress = 0
    
    // 模拟分析进度
    simulateAnalysisProgress()
    
    taskHistory.value.unshift({
      type: 'ai_started',
      title: 'AI分析启动',
      description: '已启动AI智能分析，正在处理中...',
      timestamp: new Date().toISOString()
    })
    
    ElMessage.success('AI分析已启动')
  } catch (error) {
    ElMessage.error('启动AI分析失败')
  } finally {
    aiAnalyzing.value = false
  }
}

const stopAIAnalysis = async () => {
  if (!currentTask.value) return

  try {
    await reviewTaskApi.stopAIAnalysis(currentTask.value.id)
    
    currentTask.value.aiAnalysisStatus = 'STOPPED'
    
    taskHistory.value.unshift({
      type: 'ai_stopped',
      title: 'AI分析停止',
      description: '用户手动停止了AI分析',
      timestamp: new Date().toISOString()
    })
    
    ElMessage.success('AI分析已停止')
  } catch (error) {
    ElMessage.error('停止AI分析失败')
  }
}

const simulateAnalysisProgress = () => {
  const interval = setInterval(() => {
    if (!currentTask.value || currentTask.value.aiAnalysisStatus !== 'RUNNING') {
      clearInterval(interval)
      return
    }
    
    currentTask.value.aiAnalysisProgress += Math.random() * 15
    
    if (currentTask.value.aiAnalysisProgress >= 100) {
      currentTask.value.aiAnalysisProgress = 100
      currentTask.value.aiAnalysisStatus = 'COMPLETED'
      
      // 模拟生成分析结果
      generateMockAnalysisResults()
      
      taskHistory.value.unshift({
        type: 'ai_completed',
        title: 'AI分析完成',
        description: `AI分析已完成，共生成 ${aiAnalysisResults.value.length} 项分析结果`,
        timestamp: new Date().toISOString()
      })
      
      ElMessage.success('AI分析完成')
      clearInterval(interval)
    }
  }, 2000)
}

const generateMockAnalysisResults = () => {
  aiAnalysisResults.value = taskForm.aiAnalysisTypes.map(type => ({
    analysisType: type,
    analysisTitle: AIAnalysisTypeLabels[type as keyof typeof AIAnalysisTypeLabels],
    analysisContent: `这是${AIAnalysisTypeLabels[type as keyof typeof AIAnalysisTypeLabels]}的详细分析结果。通过AI算法分析，识别出了多个关键点和改进建议。`,
    analysisSummary: `${AIAnalysisTypeLabels[type as keyof typeof AIAnalysisTypeLabels]}分析完成，发现3个重要发现点`,
    confidenceScore: 0.8 + Math.random() * 0.15,
    riskLevel: ['LOW', 'MEDIUM', 'HIGH'][Math.floor(Math.random() * 3)],
    suggestions: '建议根据分析结果进行相应的优化和改进',
    createdAt: new Date().toISOString()
  }))
  
  if (aiAnalysisResults.value.length > 0) {
    activeResultTab.value = aiAnalysisResults.value[0].analysisType
  }
}

const getAnalysisStatusType = (status: string) => {
  const types: Record<string, string> = {
    PENDING: 'info',
    RUNNING: 'warning',
    COMPLETED: 'success',
    FAILED: 'danger',
    STOPPED: 'info'
  }
  return types[status] || 'info'
}

const getAnalysisStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    PENDING: '待开始',
    RUNNING: '分析中',
    COMPLETED: '已完成',
    FAILED: '失败',
    STOPPED: '已停止'
  }
  return labels[status] || status
}

const getProgressColor = (progress: number) => {
  if (progress < 30) return '#f56c6c'
  if (progress < 70) return '#e6a23c'
  return '#67c23a'
}

const getAnalysisTypeLabel = (type: string) => {
  return AIAnalysisTypeLabels[type as keyof typeof AIAnalysisTypeLabels] || type
}

const getRiskLevelType = (level: string) => {
  return RiskLevelColors[level as keyof typeof RiskLevelColors] || 'info'
}

const getRiskLevelLabel = (level: string) => {
  return RiskLevelLabels[level as keyof typeof RiskLevelLabels] || level
}

const getEventType = (type: string) => {
  const types: Record<string, string> = {
    created: 'primary',
    ai_started: 'success',
    ai_completed: 'success',
    ai_stopped: 'warning',
    ai_failed: 'danger'
  }
  return types[type] || 'primary'
}

const formatAnalysisContent = (content: string) => {
  return content.replace(/\n/g, '<br>')
}

const formatTime = (timeStr: string) => {
  return new Date(timeStr).toLocaleString()
}

onMounted(() => {
  const taskId = route.params.id
  if (taskId && taskId !== 'new') {
    // 加载现有任务
    isEditing.value = true
    // 这里应该调用API加载任务数据
  }
})
</script>

<style scoped>
.review-task-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 24px;
  text-align: center;
}

.page-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.page-description {
  color: #606266;
  font-size: 14px;
  margin: 0;
}

.task-form-card,
.ai-control-card,
.task-history-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.control-actions {
  display: flex;
  gap: 8px;
}

.task-form {
  padding: 16px 0;
}

.ai-control-content {
  padding: 16px 0;
}

.analysis-status {
  margin-bottom: 24px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.status-item {
  text-align: center;
}

.status-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.status-value {
  font-weight: 600;
  color: #303133;
}

.analysis-results h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.result-content {
  padding: 16px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.result-header h5 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.result-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.confidence-score {
  font-size: 12px;
  color: #909399;
}

.result-summary,
.result-detail,
.result-suggestions {
  margin-bottom: 16px;
  line-height: 1.6;
}

.analysis-text,
.suggestions-text {
  margin-top: 8px;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 6px;
  border-left: 4px solid #409EFF;
  color: #606266;
}

.task-history {
  padding: 16px 0;
}

.history-event {
  background-color: #f8f9fa;
  padding: 12px;
  border-radius: 6px;
  border-left: 4px solid #409EFF;
}

.event-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.event-description {
  color: #606266;
  font-size: 14px;
}
</style>
