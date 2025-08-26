<template>
  <div class="content-insertion-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><EditPen /></el-icon>
        AI内容反向插入
      </h1>
      <p class="page-description">基于文档上下文智能推荐内容插入建议，提升文档完整性和专业性</p>
    </div>

    <!-- 操作区域 -->
    <el-card class="operation-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>内容插入分析</span>
          <el-button type="primary" @click="runAnalysis" :loading="analyzing">
            <el-icon><MagicStick /></el-icon>
            {{ analyzing ? '分析中...' : '开始分析' }}
          </el-button>
        </div>
      </template>

      <div class="operation-content">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="input-section">
              <h3>文档内容</h3>
              <el-input
                v-model="documentContent"
                type="textarea"
                :rows="12"
                placeholder="请输入或粘贴文档内容..."
                show-word-limit
                maxlength="5000"
              />
            </div>
          </el-col>
          
          <el-col :span="12">
            <div class="config-section">
              <h3>分析配置</h3>
              
              <el-form :model="analysisConfig" label-width="120px">
                <el-form-item label="目标位置">
                  <el-input 
                    v-model="analysisConfig.targetPosition" 
                    placeholder="例如：第3段后、章节2.1前"
                  />
                </el-form-item>
                
                <el-form-item label="上下文信息">
                  <el-input 
                    v-model="analysisConfig.context" 
                    type="textarea"
                    :rows="3"
                    placeholder="提供相关的上下文信息..."
                  />
                </el-form-item>
                
                <el-form-item label="插入类型">
                  <el-select v-model="analysisConfig.insertionType" placeholder="选择插入类型">
                    <el-option label="法规条款" value="regulation" />
                    <el-option label="技术标准" value="technical" />
                    <el-option label="风险评估" value="risk" />
                    <el-option label="案例引用" value="case" />
                    <el-option label="数据支撑" value="data" />
                    <el-option label="其他" value="other" />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="分析深度">
                  <el-radio-group v-model="analysisConfig.analysisDepth">
                    <el-radio label="basic">基础分析</el-radio>
                    <el-radio label="detailed">详细分析</el-radio>
                    <el-radio label="comprehensive">全面分析</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-form>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 分析结果 -->
    <el-card v-if="analysisResult" class="result-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>分析结果</span>
          <div class="result-actions">
            <el-tag :type="getConfidenceType(analysisResult.confidenceScore)">
              置信度: {{ (analysisResult.confidenceScore * 100).toFixed(1) }}%
            </el-tag>
            <el-button size="small" @click="exportResult">
              <el-icon><Download /></el-icon>
              导出结果
            </el-button>
          </div>
        </div>
      </template>

      <div class="result-content">
        <el-tabs v-model="activeTab" type="border-card">
          <el-tab-pane label="插入建议" name="suggestions">
            <div class="suggestions-content">
              <div class="analysis-summary">
                <h4>分析摘要</h4>
                <p>{{ analysisResult.analysisSummary }}</p>
              </div>
              
              <div class="analysis-detail">
                <h4>详细建议</h4>
                <div class="content-display" v-html="formatAnalysisContent(analysisResult.analysisContent)"></div>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="风险评估" name="risk">
            <div class="risk-content">
              <div class="risk-level">
                <span class="risk-label">风险等级：</span>
                <el-tag :type="getRiskLevelType(analysisResult.riskLevel)" size="large">
                  {{ getRiskLevelLabel(analysisResult.riskLevel) }}
                </el-tag>
              </div>
              
              <div class="risk-suggestions" v-if="analysisResult.suggestions">
                <h4>风险建议</h4>
                <div class="content-display" v-html="formatAnalysisContent(analysisResult.suggestions)"></div>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="关键词" name="keywords">
            <div class="keywords-content">
              <div class="keywords-list" v-if="analysisResult.keywords">
                <el-tag 
                  v-for="keyword in parseKeywords(analysisResult.keywords)" 
                  :key="keyword"
                  class="keyword-tag"
                  effect="plain"
                >
                  {{ keyword }}
                </el-tag>
              </div>
              <div v-else class="no-keywords">
                <el-empty description="暂无关键词信息" />
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="元数据" name="metadata">
            <div class="metadata-content">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="分析类型">
                  {{ getAnalysisTypeLabel(analysisResult.analysisType) }}
                </el-descriptions-item>
                <el-descriptions-item label="处理时间">
                  {{ analysisResult.processingTimeMs ? `${analysisResult.processingTimeMs}ms` : '未知' }}
                </el-descriptions-item>
                <el-descriptions-item label="创建时间">
                  {{ formatTime(analysisResult.createdAt) }}
                </el-descriptions-item>
                <el-descriptions-item label="更新时间">
                  {{ formatTime(analysisResult.updatedAt) }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>

    <!-- 历史记录 -->
    <el-card class="history-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>分析历史</span>
          <el-button size="small" @click="clearHistory">
            <el-icon><Delete /></el-icon>
            清空历史
          </el-button>
        </div>
      </template>

      <div class="history-content">
        <el-timeline v-if="analysisHistory.length > 0">
          <el-timeline-item
            v-for="(item, index) in analysisHistory"
            :key="index"
            :timestamp="formatTime(item.timestamp)"
            placement="top"
          >
            <div class="history-item">
              <div class="history-title">{{ item.title }}</div>
              <div class="history-summary">{{ item.summary }}</div>
              <div class="history-actions">
                <el-button size="small" type="text" @click="viewHistoryDetail(item)">
                  查看详情
                </el-button>
                <el-button size="small" type="text" @click="rerunAnalysis(item)">
                  重新分析
                </el-button>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
        
        <el-empty v-else description="暂无分析历史" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  EditPen, 
  MagicStick, 
  Download, 
  Delete 
} from '@element-plus/icons-vue'
import { aiTestApi, type AIAnalysisResult, AIAnalysisTypeLabels, RiskLevelLabels, RiskLevelColors } from '@/api/ai-analysis'

// 响应式数据
const analyzing = ref(false)
const activeTab = ref('suggestions')
const documentContent = ref(`项目概述：
本项目旨在建设一套智能化的文档管理系统，主要包括文档编辑、审核、版本控制等功能。

技术要求：
1. 系统应支持多种文档格式
2. 具备实时协作编辑能力
3. 提供完善的权限管理机制

质量标准：
系统应满足国家相关标准和行业规范要求。`)

const analysisConfig = reactive({
  targetPosition: '第2段后',
  context: '这是一个技术项目的招标文档，需要补充更多技术细节',
  insertionType: 'technical',
  analysisDepth: 'detailed'
})

const analysisResult = ref<AIAnalysisResult | null>(null)
const analysisHistory = ref<any[]>([])

// 方法
const runAnalysis = async () => {
  if (!documentContent.value.trim()) {
    ElMessage.warning('请输入文档内容')
    return
  }

  analyzing.value = true
  
  try {
    // 模拟调用AI分析接口
    const result = await aiTestApi.testContentInsertion()
    
    // 构造分析结果
    analysisResult.value = {
      analysisType: 'CONTENT_REVERSE_INSERTION',
      analysisTitle: 'AI内容反向插入分析',
      analysisContent: result,
      analysisSummary: '基于文档上下文分析，识别出3个可优化的内容插入点',
      confidenceScore: 0.85,
      riskLevel: 'LOW',
      suggestions: '建议在技术要求部分补充具体的技术指标和验收标准，以提高文档的完整性和可操作性。',
      keywords: '技术要求,质量标准,文档管理,智能化系统',
      processingTimeMs: Math.floor(Math.random() * 2000) + 1000,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    }
    
    // 添加到历史记录
    analysisHistory.value.unshift({
      title: '内容插入分析',
      summary: analysisResult.value.analysisSummary,
      timestamp: new Date().toISOString(),
      config: { ...analysisConfig },
      content: documentContent.value,
      result: analysisResult.value
    })
    
    ElMessage.success('分析完成')
  } catch (error) {
    ElMessage.error('分析失败，请重试')
    console.error('Analysis failed:', error)
  } finally {
    analyzing.value = false
  }
}

const getConfidenceType = (score: number) => {
  if (score >= 0.8) return 'success'
  if (score >= 0.6) return 'warning'
  return 'danger'
}

const getRiskLevelType = (level: string) => {
  return RiskLevelColors[level as keyof typeof RiskLevelColors] || 'info'
}

const getRiskLevelLabel = (level: string) => {
  return RiskLevelLabels[level as keyof typeof RiskLevelLabels] || level
}

const getAnalysisTypeLabel = (type: string) => {
  return AIAnalysisTypeLabels[type as keyof typeof AIAnalysisTypeLabels] || type
}

const formatAnalysisContent = (content: string) => {
  if (!content) return ''

  // 简单的格式化：将换行符转换为<br>，将数字列表格式化
  return content
    .replace(/\n/g, '<br>')
    .replace(/(\d+\.\s)/g, '<strong>$1</strong>')
    .replace(/【([^】]+)】/g, '<span class="highlight">【$1】</span>')
}

const parseKeywords = (keywords: string) => {
  if (!keywords) return []
  return keywords.split(',').map(k => k.trim()).filter(k => k)
}

const formatTime = (timeStr?: string) => {
  if (!timeStr) return '未知'
  return new Date(timeStr).toLocaleString()
}

const exportResult = () => {
  if (!analysisResult.value) return
  
  const data = {
    title: analysisResult.value.analysisTitle,
    content: analysisResult.value.analysisContent,
    summary: analysisResult.value.analysisSummary,
    confidence: analysisResult.value.confidenceScore,
    riskLevel: analysisResult.value.riskLevel,
    timestamp: new Date().toISOString()
  }
  
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `ai-content-insertion-${Date.now()}.json`
  a.click()
  URL.revokeObjectURL(url)
  
  ElMessage.success('结果已导出')
}

const clearHistory = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有分析历史吗？', '确认操作', {
      type: 'warning'
    })
    
    analysisHistory.value = []
    ElMessage.success('历史记录已清空')
  } catch {
    // 用户取消
  }
}

const viewHistoryDetail = (item: any) => {
  analysisResult.value = item.result
  activeTab.value = 'suggestions'
  ElMessage.info('已加载历史分析结果')
}

const rerunAnalysis = (item: any) => {
  documentContent.value = item.content
  Object.assign(analysisConfig, item.config)
  runAnalysis()
}
</script>

<style scoped>
.content-insertion-page {
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

.operation-card,
.result-card,
.history-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.operation-content {
  padding: 16px 0;
}

.input-section h3,
.config-section h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.result-content {
  padding: 16px 0;
}

.suggestions-content,
.risk-content,
.keywords-content,
.metadata-content {
  padding: 16px;
}

.analysis-summary,
.analysis-detail {
  margin-bottom: 24px;
}

.analysis-summary h4,
.analysis-detail h4,
.risk-suggestions h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.content-display {
  line-height: 1.6;
  color: #606266;
  background-color: #f8f9fa;
  padding: 16px;
  border-radius: 6px;
  border-left: 4px solid #409EFF;
}

.content-display :deep(.highlight) {
  background-color: #fff3cd;
  padding: 2px 4px;
  border-radius: 3px;
  color: #856404;
}

.risk-level {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.risk-label {
  font-weight: 600;
  color: #303133;
}

.keywords-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.keyword-tag {
  margin: 0;
}

.no-keywords {
  text-align: center;
  padding: 40px 0;
}

.history-content {
  padding: 16px 0;
}

.history-item {
  background-color: #f8f9fa;
  padding: 16px;
  border-radius: 6px;
  border-left: 4px solid #409EFF;
}

.history-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.history-summary {
  color: #606266;
  margin-bottom: 12px;
  line-height: 1.5;
}

.history-actions {
  display: flex;
  gap: 12px;
}

@media (max-width: 768px) {
  .content-insertion-page {
    padding: 16px;
  }
  
  .result-actions {
    flex-direction: column;
    gap: 8px;
  }
}
</style>
