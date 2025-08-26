<template>
  <div class="ai-analysis-dashboard">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><MagicStick /></el-icon>
        AI智能分析概览
      </h1>
      <p class="page-description">基于阿里云千问3的7大核心AI功能，为您的文档提供智能化分析服务</p>
    </div>

    <!-- AI服务状态卡片 -->
    <el-card class="status-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>AI服务状态</span>
          <el-button type="primary" size="small" @click="refreshStatus" :loading="statusLoading">
            <el-icon><Refresh /></el-icon>
            刷新状态
          </el-button>
        </div>
      </template>
      
      <div class="status-content">
        <div class="status-item">
          <div class="status-label">服务状态</div>
          <div class="status-value">
            <el-tag :type="aiStatus.available ? 'success' : 'warning'" size="large">
              <el-icon><Connection /></el-icon>
              {{ aiStatus.available ? '已连接' : '模拟模式' }}
            </el-tag>
          </div>
        </div>
        
        <div class="status-item">
          <div class="status-label">服务描述</div>
          <div class="status-value">{{ aiStatus.service }}</div>
        </div>
        
        <div class="status-item">
          <div class="status-label">AI模型</div>
          <div class="status-value">{{ aiConfig.model || 'qwen-max' }}</div>
        </div>
        
        <div class="status-item">
          <div class="status-label">最后更新</div>
          <div class="status-value">{{ formatTime(aiStatus.timestamp) }}</div>
        </div>
      </div>
      
      <div class="status-actions">
        <el-button type="primary" @click="testConnection" :loading="testLoading">
          <el-icon><ChatDotRound /></el-icon>
          测试连接
        </el-button>
        <el-button @click="viewConfig">
          <el-icon><Setting /></el-icon>
          查看配置
        </el-button>
      </div>
    </el-card>

    <!-- AI功能卡片网格 -->
    <div class="features-grid">
      <el-card 
        v-for="feature in aiFeatures" 
        :key="feature.key"
        class="feature-card" 
        shadow="hover"
        @click="navigateToFeature(feature.key)"
      >
        <div class="feature-content">
          <div class="feature-icon">
            <el-icon :size="32" :color="feature.color">
              <component :is="feature.icon" />
            </el-icon>
          </div>
          
          <div class="feature-info">
            <h3 class="feature-title">{{ feature.title }}</h3>
            <p class="feature-description">{{ feature.description }}</p>
          </div>
          
          <div class="feature-actions">
            <el-button 
              type="primary" 
              size="small" 
              @click.stop="testFeature(feature.key)"
              :loading="testingFeatures[feature.key]"
            >
              <el-icon><Play /></el-icon>
              快速测试
            </el-button>
          </div>
        </div>
        
        <div class="feature-stats">
          <div class="stat-item">
            <span class="stat-label">成功率</span>
            <span class="stat-value">{{ feature.successRate }}%</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">平均耗时</span>
            <span class="stat-value">{{ feature.avgTime }}s</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 快速操作区域 -->
    <el-card class="quick-actions-card" shadow="hover">
      <template #header>
        <span>快速操作</span>
      </template>
      
      <div class="quick-actions">
        <el-button type="primary" size="large" @click="createReviewTask">
          <el-icon><Plus /></el-icon>
          创建AI评审任务
        </el-button>
        
        <el-button type="success" size="large" @click="runFullAnalysis" :loading="fullAnalysisLoading">
          <el-icon><MagicStick /></el-icon>
          运行完整AI分析
        </el-button>
        
        <el-button size="large" @click="viewRecentTasks">
          <el-icon><Document /></el-icon>
          查看最近任务
        </el-button>
      </div>
    </el-card>

    <!-- 配置对话框 -->
    <el-dialog v-model="configDialogVisible" title="AI服务配置" width="600px">
      <div class="config-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="服务类型">{{ aiConfig.service_type }}</el-descriptions-item>
          <el-descriptions-item label="AI模型">{{ aiConfig.model }}</el-descriptions-item>
          <el-descriptions-item label="服务状态">
            <el-tag :type="aiConfig.enabled ? 'success' : 'danger'">
              {{ aiConfig.enabled ? '已启用' : '未启用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="支持功能">
            <el-tag v-for="feature in aiConfig.features" :key="feature" class="feature-tag">
              {{ feature }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  MagicStick, 
  Refresh, 
  Connection, 
  ChatDotRound, 
  Setting, 
  Play, 
  Plus, 
  Document,
  EditPen,
  Scissors,
  TestTube,
  Link,
  MagicStick as FormatIcon,
  ChatLineRound,
  DocumentCopy
} from '@element-plus/icons-vue'
import { aiStatusApi, aiTestApi, type AIServiceStatus, type AIServiceConfig } from '@/api/ai-analysis'

const router = useRouter()

// 响应式数据
const statusLoading = ref(false)
const testLoading = ref(false)
const fullAnalysisLoading = ref(false)
const configDialogVisible = ref(false)

const aiStatus = reactive<AIServiceStatus>({
  available: false,
  service: '',
  timestamp: 0
})

const aiConfig = reactive<AIServiceConfig>({
  enabled: false,
  service_type: '',
  model: '',
  features: []
})

const testingFeatures = reactive<Record<string, boolean>>({})

// AI功能配置
const aiFeatures = [
  {
    key: 'content-insertion',
    title: 'AI内容反向插入',
    description: '基于文档上下文智能推荐内容插入建议',
    icon: EditPen,
    color: '#409EFF',
    successRate: 94,
    avgTime: 1.2
  },
  {
    key: 'slice-tagging',
    title: '智能切片标记',
    description: '将文档智能分割并标记重要片段',
    icon: Scissors,
    color: '#67C23A',
    successRate: 92,
    avgTime: 0.8
  },
  {
    key: 'prompt-testing',
    title: '提示词测试工具',
    description: '测试和优化AI提示词的效果',
    icon: TestTube,
    color: '#E6A23C',
    successRate: 89,
    avgTime: 1.5
  },
  {
    key: 'citation-marking',
    title: '引用来源标记',
    description: '智能识别和标记文档中的引用来源',
    icon: Link,
    color: '#F56C6C',
    successRate: 87,
    avgTime: 2.1
  },
  {
    key: 'smart-formatting',
    title: 'AI智能格式化',
    description: '智能优化文档格式和结构',
    icon: FormatIcon,
    color: '#909399',
    successRate: 91,
    avgTime: 1.8
  },
  {
    key: 'long-text-interaction',
    title: '长文本交互',
    description: '处理长文本的智能交互和分析',
    icon: ChatLineRound,
    color: '#606266',
    successRate: 88,
    avgTime: 3.2
  },
  {
    key: 'difference-comparison',
    title: '文档差异对比',
    description: '智能对比不同版本文档的差异',
    icon: DocumentCopy,
    color: '#303133',
    successRate: 85,
    avgTime: 2.8
  }
]

// 方法
const refreshStatus = async () => {
  statusLoading.value = true
  try {
    const [status, config] = await Promise.all([
      aiStatusApi.getStatus(),
      aiStatusApi.getConfig()
    ])
    
    Object.assign(aiStatus, status)
    Object.assign(aiConfig, config)
    
    ElMessage.success('状态刷新成功')
  } catch (error) {
    ElMessage.error('获取状态失败')
    console.error('Failed to refresh status:', error)
  } finally {
    statusLoading.value = false
  }
}

const testConnection = async () => {
  testLoading.value = true
  try {
    const result = await aiStatusApi.testConnection()
    ElMessage.success('连接测试成功')
    console.log('Test result:', result)
  } catch (error) {
    ElMessage.error('连接测试失败')
    console.error('Connection test failed:', error)
  } finally {
    testLoading.value = false
  }
}

const viewConfig = () => {
  configDialogVisible.value = true
}

const navigateToFeature = (featureKey: string) => {
  router.push(`/ai-analysis/${featureKey}`)
}

const testFeature = async (featureKey: string) => {
  testingFeatures[featureKey] = true
  
  try {
    let result: string
    
    switch (featureKey) {
      case 'content-insertion':
        result = await aiTestApi.testContentInsertion()
        break
      case 'slice-tagging':
        result = await aiTestApi.testSliceTagging()
        break
      case 'prompt-testing':
        result = await aiTestApi.testPromptTesting()
        break
      case 'citation-marking':
        result = await aiTestApi.testCitationMarking()
        break
      case 'smart-formatting':
        result = await aiTestApi.testSmartFormatting()
        break
      case 'long-text-interaction':
        result = await aiTestApi.testLongTextInteraction()
        break
      case 'difference-comparison':
        result = await aiTestApi.testDifferenceComparison()
        break
      default:
        throw new Error('Unknown feature')
    }
    
    ElMessage.success('测试完成')
    console.log('Test result:', result)
  } catch (error) {
    ElMessage.error('测试失败')
    console.error('Feature test failed:', error)
  } finally {
    testingFeatures[featureKey] = false
  }
}

const createReviewTask = () => {
  ElMessage.info('创建评审任务功能开发中...')
}

const runFullAnalysis = async () => {
  fullAnalysisLoading.value = true
  try {
    const result = await aiTestApi.testFullAnalysis()
    ElMessage.success('完整AI分析完成')
    console.log('Full analysis result:', result)
  } catch (error) {
    ElMessage.error('完整AI分析失败')
    console.error('Full analysis failed:', error)
  } finally {
    fullAnalysisLoading.value = false
  }
}

const viewRecentTasks = () => {
  ElMessage.info('查看最近任务功能开发中...')
}

const formatTime = (timestamp: number) => {
  if (!timestamp) return '未知'
  return new Date(timestamp).toLocaleString()
}

// 生命周期
onMounted(() => {
  refreshStatus()
})
</script>

<style scoped>
.ai-analysis-dashboard {
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
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.page-description {
  color: #606266;
  font-size: 16px;
  margin: 0;
}

.status-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.status-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.status-label {
  font-size: 14px;
  color: #909399;
  font-weight: 500;
}

.status-value {
  font-size: 16px;
  color: #303133;
  font-weight: 600;
}

.status-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.feature-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.feature-content {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}

.feature-icon {
  flex-shrink: 0;
  padding: 12px;
  border-radius: 8px;
  background-color: #f5f7fa;
}

.feature-info {
  flex: 1;
}

.feature-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.feature-description {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  margin: 0;
}

.feature-actions {
  flex-shrink: 0;
}

.feature-stats {
  display: flex;
  justify-content: space-between;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #409EFF;
}

.quick-actions-card {
  margin-bottom: 24px;
}

.quick-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
}

.config-content {
  padding: 16px 0;
}

.feature-tag {
  margin-right: 8px;
  margin-bottom: 4px;
}

@media (max-width: 768px) {
  .ai-analysis-dashboard {
    padding: 16px;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .quick-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .status-content {
    grid-template-columns: 1fr;
  }
}
</style>
