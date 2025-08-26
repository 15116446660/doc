<template>
  <div class="prompt-testing-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><TestTube /></el-icon>
        提示词测试工具
      </h1>
      <p class="page-description">测试和优化AI提示词的效果，提升AI分析的准确性和相关性</p>
    </div>

    <!-- 提示词管理区域 -->
    <el-card class="prompt-management-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>提示词管理</span>
          <el-button type="primary" @click="addPrompt">
            <el-icon><Plus /></el-icon>
            添加提示词
          </el-button>
        </div>
      </template>

      <div class="prompt-list">
        <div 
          v-for="(prompt, index) in prompts" 
          :key="index"
          class="prompt-item"
          :class="{ 'selected': selectedPrompts.includes(index) }"
        >
          <div class="prompt-content">
            <el-checkbox 
              v-model="selectedPrompts" 
              :label="index"
              class="prompt-checkbox"
            />
            <div class="prompt-text">
              <el-input 
                v-model="prompt.text" 
                placeholder="输入提示词..."
                @blur="updatePrompt(index)"
              />
            </div>
            <div class="prompt-meta">
              <el-tag size="small" :type="getPromptTypeColor(prompt.type)">
                {{ getPromptTypeLabel(prompt.type) }}
              </el-tag>
              <span class="prompt-length">{{ prompt.text.length }} 字符</span>
            </div>
          </div>
          
          <div class="prompt-actions">
            <el-dropdown @command="handlePromptAction">
              <el-button size="small" type="text">
                <el-icon><More /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="`edit-${index}`">编辑</el-dropdown-item>
                  <el-dropdown-item :command="`duplicate-${index}`">复制</el-dropdown-item>
                  <el-dropdown-item :command="`delete-${index}`" divided>删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          
          <!-- 测试结果预览 -->
          <div v-if="prompt.testResult" class="prompt-result-preview">
            <div class="result-metrics">
              <span class="metric">
                <el-icon><TrendCharts /></el-icon>
                准确率: {{ prompt.testResult.accuracy }}%
              </span>
              <span class="metric">
                <el-icon><Timer /></el-icon>
                响应时间: {{ prompt.testResult.responseTime }}s
              </span>
              <span class="metric">
                <el-icon><Star /></el-icon>
                评分: {{ prompt.testResult.score }}/10
              </span>
            </div>
          </div>
        </div>
        
        <el-empty v-if="prompts.length === 0" description="暂无提示词，点击添加按钮创建" />
      </div>
    </el-card>

    <!-- 测试配置区域 -->
    <el-card class="test-config-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>测试配置</span>
          <el-button 
            type="primary" 
            @click="runPromptTest" 
            :loading="testing"
            :disabled="selectedPrompts.length === 0"
          >
            <el-icon><Play /></el-icon>
            {{ testing ? '测试中...' : '开始测试' }}
          </el-button>
        </div>
      </template>

      <div class="test-config-content">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="config-section">
              <h4>测试文档</h4>
              <el-input
                v-model="testDocument"
                type="textarea"
                :rows="8"
                placeholder="输入用于测试的文档内容..."
                show-word-limit
                maxlength="3000"
              />
            </div>
          </el-col>
          
          <el-col :span="12">
            <div class="config-section">
              <h4>测试参数</h4>
              <el-form :model="testConfig" label-width="100px">
                <el-form-item label="测试轮数">
                  <el-input-number 
                    v-model="testConfig.rounds" 
                    :min="1" 
                    :max="10"
                  />
                </el-form-item>
                
                <el-form-item label="评估维度">
                  <el-checkbox-group v-model="testConfig.evaluationDimensions">
                    <el-checkbox label="accuracy">准确性</el-checkbox>
                    <el-checkbox label="relevance">相关性</el-checkbox>
                    <el-checkbox label="completeness">完整性</el-checkbox>
                    <el-checkbox label="clarity">清晰度</el-checkbox>
                  </el-checkbox-group>
                </el-form-item>
                
                <el-form-item label="基准答案">
                  <el-input 
                    v-model="testConfig.groundTruth" 
                    type="textarea"
                    :rows="3"
                    placeholder="输入期望的分析结果作为基准..."
                  />
                </el-form-item>
              </el-form>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 测试结果区域 -->
    <el-card v-if="testResults.length > 0" class="test-results-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>测试结果对比</span>
          <div class="result-actions">
            <el-button size="small" @click="exportResults">
              <el-icon><Download /></el-icon>
              导出结果
            </el-button>
            <el-button size="small" @click="generateReport">
              <el-icon><Document /></el-icon>
              生成报告
            </el-button>
          </div>
        </div>
      </template>

      <div class="test-results-content">
        <!-- 结果统计 -->
        <div class="results-summary">
          <el-row :gutter="16">
            <el-col :span="6">
              <div class="summary-item">
                <div class="summary-value">{{ testResults.length }}</div>
                <div class="summary-label">测试提示词</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="summary-item">
                <div class="summary-value">{{ bestPromptIndex + 1 }}</div>
                <div class="summary-label">最佳提示词</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="summary-item">
                <div class="summary-value">{{ averageAccuracy }}%</div>
                <div class="summary-label">平均准确率</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="summary-item">
                <div class="summary-value">{{ averageResponseTime }}s</div>
                <div class="summary-label">平均响应时间</div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 结果列表 -->
        <div class="results-list">
          <div 
            v-for="(result, index) in testResults" 
            :key="index"
            class="result-item"
            :class="{ 'best-result': index === bestPromptIndex }"
          >
            <div class="result-header">
              <div class="result-info">
                <span class="result-rank">#{{ index + 1 }}</span>
                <span class="result-prompt">{{ result.promptText }}</span>
                <el-tag v-if="index === bestPromptIndex" type="success" size="small">
                  <el-icon><Trophy /></el-icon>
                  最佳
                </el-tag>
              </div>
              
              <div class="result-metrics">
                <el-progress 
                  :percentage="result.accuracy" 
                  :color="getAccuracyColor(result.accuracy)"
                  :show-text="false"
                  :stroke-width="6"
                  class="accuracy-progress"
                />
                <span class="metric-text">{{ result.accuracy }}%</span>
              </div>
            </div>
            
            <div class="result-details">
              <el-row :gutter="16">
                <el-col :span="8">
                  <div class="detail-item">
                    <span class="detail-label">响应时间</span>
                    <span class="detail-value">{{ result.responseTime }}s</span>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="detail-item">
                    <span class="detail-label">综合评分</span>
                    <span class="detail-value">{{ result.score }}/10</span>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="detail-item">
                    <span class="detail-label">相关性</span>
                    <span class="detail-value">{{ result.relevance }}%</span>
                  </div>
                </el-col>
              </el-row>
              
              <div class="result-content">
                <h5>AI分析结果</h5>
                <div class="analysis-text">{{ result.analysisResult }}</div>
              </div>
              
              <div class="result-feedback">
                <h5>改进建议</h5>
                <div class="feedback-text">{{ result.feedback }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 提示词编辑对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑提示词" width="600px">
      <el-form v-if="editingPrompt" :model="editingPrompt" label-width="80px">
        <el-form-item label="提示词">
          <el-input 
            v-model="editingPrompt.text" 
            type="textarea" 
            :rows="4"
            placeholder="输入提示词内容..."
          />
        </el-form-item>
        
        <el-form-item label="类型">
          <el-select v-model="editingPrompt.type" placeholder="选择提示词类型">
            <el-option label="内容分析" value="content" />
            <el-option label="结构分析" value="structure" />
            <el-option label="质量评估" value="quality" />
            <el-option label="风险识别" value="risk" />
            <el-option label="合规检查" value="compliance" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="描述">
          <el-input 
            v-model="editingPrompt.description" 
            placeholder="描述这个提示词的用途..."
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePromptEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  TestTube, 
  Plus, 
  More, 
  Play, 
  Download, 
  Document,
  TrendCharts,
  Timer,
  Star,
  Trophy
} from '@element-plus/icons-vue'
import { aiTestApi } from '@/api/ai-analysis'

// 提示词接口
interface PromptItem {
  text: string
  type: string
  description?: string
  testResult?: {
    accuracy: number
    responseTime: number
    score: number
    relevance: number
  }
}

// 测试结果接口
interface TestResult {
  promptText: string
  accuracy: number
  responseTime: number
  score: number
  relevance: number
  analysisResult: string
  feedback: string
}

// 响应式数据
const testing = ref(false)
const editDialogVisible = ref(false)
const editingPrompt = ref<PromptItem | null>(null)
const editingIndex = ref(-1)

const prompts = ref<PromptItem[]>([
  {
    text: '请详细分析文档的合规性，重点关注是否符合相关法规和标准要求',
    type: 'compliance',
    description: '合规性分析提示词'
  },
  {
    text: '请检查文档的完整性，识别缺失的关键信息和必要章节',
    type: 'content',
    description: '完整性检查提示词'
  },
  {
    text: '请评估文档中的潜在风险点，并提供相应的风险控制建议',
    type: 'risk',
    description: '风险评估提示词'
  }
])

const selectedPrompts = ref<number[]>([])

const testDocument = ref(`项目技术方案

1. 系统架构设计
本系统采用微服务架构，包含用户管理、文档处理、审核流程等核心模块。

2. 技术选型
- 前端：Vue3 + TypeScript + Element Plus
- 后端：Spring Boot + MySQL + Redis
- 部署：Docker + Kubernetes

3. 安全要求
系统需要满足等保三级要求，包括数据加密、访问控制、审计日志等。

4. 性能指标
- 响应时间：< 2秒
- 并发用户：1000+
- 可用性：99.9%`)

const testConfig = reactive({
  rounds: 3,
  evaluationDimensions: ['accuracy', 'relevance', 'completeness'],
  groundTruth: '系统采用微服务架构，技术栈现代化，安全性符合等保三级，性能指标明确'
})

const testResults = ref<TestResult[]>([])

// 计算属性
const bestPromptIndex = computed(() => {
  if (testResults.value.length === 0) return -1
  let bestIndex = 0
  let bestScore = testResults.value[0].score
  
  testResults.value.forEach((result, index) => {
    if (result.score > bestScore) {
      bestScore = result.score
      bestIndex = index
    }
  })
  
  return bestIndex
})

const averageAccuracy = computed(() => {
  if (testResults.value.length === 0) return 0
  const total = testResults.value.reduce((sum, result) => sum + result.accuracy, 0)
  return Math.round(total / testResults.value.length)
})

const averageResponseTime = computed(() => {
  if (testResults.value.length === 0) return 0
  const total = testResults.value.reduce((sum, result) => sum + result.responseTime, 0)
  return (total / testResults.value.length).toFixed(1)
})

// 方法
const addPrompt = () => {
  prompts.value.push({
    text: '',
    type: 'content',
    description: ''
  })
}

const updatePrompt = (index: number) => {
  // 提示词更新后清除测试结果
  if (prompts.value[index].testResult) {
    delete prompts.value[index].testResult
  }
}

const handlePromptAction = (command: string) => {
  const [action, indexStr] = command.split('-')
  const index = parseInt(indexStr)
  
  switch (action) {
    case 'edit':
      editPrompt(index)
      break
    case 'duplicate':
      duplicatePrompt(index)
      break
    case 'delete':
      deletePrompt(index)
      break
  }
}

const editPrompt = (index: number) => {
  editingIndex.value = index
  editingPrompt.value = { ...prompts.value[index] }
  editDialogVisible.value = true
}

const duplicatePrompt = (index: number) => {
  const original = prompts.value[index]
  prompts.value.push({
    ...original,
    text: original.text + ' (副本)',
    testResult: undefined
  })
  ElMessage.success('提示词已复制')
}

const deletePrompt = async (index: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个提示词吗？', '确认删除', {
      type: 'warning'
    })
    
    prompts.value.splice(index, 1)
    // 更新选中状态
    selectedPrompts.value = selectedPrompts.value
      .filter(i => i !== index)
      .map(i => i > index ? i - 1 : i)
    
    ElMessage.success('提示词已删除')
  } catch {
    // 用户取消
  }
}

const savePromptEdit = () => {
  if (editingPrompt.value && editingIndex.value >= 0) {
    prompts.value[editingIndex.value] = { ...editingPrompt.value }
    editDialogVisible.value = false
    ElMessage.success('提示词已更新')
  }
}

const runPromptTest = async () => {
  if (selectedPrompts.value.length === 0) {
    ElMessage.warning('请选择要测试的提示词')
    return
  }
  
  if (!testDocument.value.trim()) {
    ElMessage.warning('请输入测试文档')
    return
  }

  testing.value = true
  testResults.value = []
  
  try {
    // 调用AI测试接口
    await aiTestApi.testPromptTesting()
    
    // 模拟生成测试结果
    for (const promptIndex of selectedPrompts.value) {
      const prompt = prompts.value[promptIndex]
      const result = await simulatePromptTest(prompt)
      testResults.value.push(result)
      
      // 更新提示词的测试结果
      prompts.value[promptIndex].testResult = {
        accuracy: result.accuracy,
        responseTime: result.responseTime,
        score: result.score,
        relevance: result.relevance
      }
    }
    
    ElMessage.success('提示词测试完成')
  } catch (error) {
    ElMessage.error('测试失败，请重试')
    console.error('Prompt test failed:', error)
  } finally {
    testing.value = false
  }
}

const simulatePromptTest = async (prompt: PromptItem): Promise<TestResult> => {
  // 模拟测试延迟
  await new Promise(resolve => setTimeout(resolve, 1000 + Math.random() * 2000))
  
  // 根据提示词类型生成不同的测试结果
  let baseAccuracy = 70
  let baseScore = 6
  
  if (prompt.text.includes('详细') || prompt.text.includes('重点')) {
    baseAccuracy += 10
    baseScore += 1
  }
  
  if (prompt.text.includes('合规') || prompt.text.includes('标准')) {
    baseAccuracy += 15
    baseScore += 1.5
  }
  
  if (prompt.text.includes('风险') || prompt.text.includes('评估')) {
    baseAccuracy += 8
    baseScore += 0.8
  }
  
  const accuracy = Math.min(95, baseAccuracy + Math.random() * 10)
  const responseTime = 1.2 + Math.random() * 2.8
  const score = Math.min(10, baseScore + Math.random() * 2)
  const relevance = Math.min(95, accuracy + Math.random() * 10 - 5)
  
  return {
    promptText: prompt.text,
    accuracy: Math.round(accuracy),
    responseTime: parseFloat(responseTime.toFixed(1)),
    score: parseFloat(score.toFixed(1)),
    relevance: Math.round(relevance),
    analysisResult: generateMockAnalysis(prompt.type),
    feedback: generateMockFeedback(accuracy)
  }
}

const generateMockAnalysis = (type: string): string => {
  const analyses: Record<string, string> = {
    compliance: '文档基本符合相关法规要求，但在数据保护条款方面需要进一步完善。建议补充GDPR相关内容。',
    content: '文档结构完整，包含了主要的技术方案内容。缺少详细的测试计划和部署方案章节。',
    risk: '识别出3个主要风险点：技术选型风险、安全合规风险、性能达标风险。建议制定相应的风险缓解措施。',
    structure: '文档结构清晰，逻辑层次分明。建议在每个章节添加小结，提升可读性。',
    quality: '文档质量良好，内容详实。部分技术细节描述不够具体，建议补充实现细节。'
  }
  
  return analyses[type] || '分析完成，文档整体质量良好，建议进一步优化细节内容。'
}

const generateMockFeedback = (accuracy: number): string => {
  if (accuracy >= 90) {
    return '提示词表现优秀，能够准确识别关键信息，建议保持当前表述方式。'
  } else if (accuracy >= 80) {
    return '提示词表现良好，可以考虑增加更具体的分析维度来提升准确性。'
  } else if (accuracy >= 70) {
    return '提示词表现一般，建议优化表述方式，增加更明确的指导性语言。'
  } else {
    return '提示词需要优化，建议重新设计，明确分析目标和期望输出格式。'
  }
}

const getPromptTypeLabel = (type: string): string => {
  const labels: Record<string, string> = {
    content: '内容分析',
    structure: '结构分析',
    quality: '质量评估',
    risk: '风险识别',
    compliance: '合规检查'
  }
  return labels[type] || type
}

const getPromptTypeColor = (type: string): string => {
  const colors: Record<string, string> = {
    content: 'primary',
    structure: 'success',
    quality: 'warning',
    risk: 'danger',
    compliance: 'info'
  }
  return colors[type] || 'default'
}

const getAccuracyColor = (accuracy: number): string => {
  if (accuracy >= 90) return '#67c23a'
  if (accuracy >= 80) return '#e6a23c'
  if (accuracy >= 70) return '#f56c6c'
  return '#909399'
}

const exportResults = () => {
  const data = {
    testConfig,
    prompts: prompts.value.filter((_, index) => selectedPrompts.value.includes(index)),
    results: testResults.value,
    summary: {
      totalPrompts: testResults.value.length,
      bestPrompt: bestPromptIndex.value + 1,
      averageAccuracy: averageAccuracy.value,
      averageResponseTime: averageResponseTime.value
    },
    timestamp: new Date().toISOString()
  }
  
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `prompt-test-results-${Date.now()}.json`
  a.click()
  URL.revokeObjectURL(url)
  
  ElMessage.success('测试结果已导出')
}

const generateReport = () => {
  ElMessage.info('报告生成功能开发中...')
}
</script>

<style scoped>
.prompt-testing-page {
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

.prompt-management-card,
.test-config-card,
.test-results-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-actions {
  display: flex;
  gap: 8px;
}

.prompt-list {
  padding: 16px 0;
}

.prompt-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  margin-bottom: 12px;
  background-color: #fff;
  transition: all 0.3s ease;
}

.prompt-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.prompt-item.selected {
  border-color: #409EFF;
  background-color: #f0f9ff;
}

.prompt-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.prompt-checkbox {
  margin: 0;
}

.prompt-text {
  flex: 1;
}

.prompt-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.prompt-length {
  color: #909399;
}

.prompt-actions {
  flex-shrink: 0;
}

.prompt-result-preview {
  margin-top: 12px;
  padding: 8px 12px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.result-metrics {
  display: flex;
  gap: 16px;
  font-size: 12px;
}

.metric {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
}

.test-config-content {
  padding: 16px 0;
}

.config-section h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.test-results-content {
  padding: 16px 0;
}

.results-summary {
  margin-bottom: 24px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.summary-item {
  text-align: center;
}

.summary-value {
  font-size: 24px;
  font-weight: 600;
  color: #409EFF;
  margin-bottom: 4px;
}

.summary-label {
  font-size: 14px;
  color: #606266;
}

.results-list {
  margin-bottom: 24px;
}

.result-item {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  margin-bottom: 16px;
  background-color: #fff;
  overflow: hidden;
}

.result-item.best-result {
  border-color: #67c23a;
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.2);
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #ebeef5;
}

.result-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.result-rank {
  font-weight: 600;
  color: #409EFF;
  font-size: 18px;
}

.result-prompt {
  color: #303133;
  font-weight: 500;
  flex: 1;
}

.result-metrics {
  display: flex;
  align-items: center;
  gap: 8px;
}

.accuracy-progress {
  width: 100px;
}

.metric-text {
  font-weight: 600;
  color: #303133;
}

.result-details {
  padding: 16px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.detail-label {
  color: #606266;
  font-size: 14px;
}

.detail-value {
  color: #303133;
  font-weight: 600;
}

.result-content,
.result-feedback {
  margin-top: 16px;
}

.result-content h5,
.result-feedback h5 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.analysis-text,
.feedback-text {
  line-height: 1.6;
  color: #606266;
  background-color: #f8f9fa;
  padding: 12px;
  border-radius: 4px;
  border-left: 4px solid #409EFF;
}

@media (max-width: 768px) {
  .prompt-testing-page {
    padding: 16px;
  }
  
  .prompt-item {
    flex-direction: column;
    gap: 12px;
  }
  
  .result-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .result-metrics {
    align-self: stretch;
    justify-content: space-between;
  }
}
</style>
