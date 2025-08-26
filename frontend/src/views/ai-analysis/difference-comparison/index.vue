<template>
  <div class="difference-comparison-page">
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><DocumentCopy /></el-icon>
        文档差异对比
      </h1>
      <p class="page-description">智能对比不同版本文档的差异，识别变更内容和影响分析</p>
    </div>

    <el-card class="comparison-setup-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>对比设置</span>
          <el-button type="primary" @click="runComparison" :loading="comparing">
            <el-icon><MagicStick /></el-icon>
            {{ comparing ? '对比中...' : '开始对比' }}
          </el-button>
        </div>
      </template>

      <div class="comparison-setup">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="document-section">
              <h3>基准文档 (版本A)</h3>
              <el-input
                v-model="documentA"
                type="textarea"
                :rows="12"
                placeholder="请输入基准文档内容..."
                show-word-limit
                maxlength="5000"
              />
              <div class="document-meta">
                <span>字符数: {{ documentA.length }}</span>
                <span>版本: v1.0</span>
              </div>
            </div>
          </el-col>
          
          <el-col :span="12">
            <div class="document-section">
              <h3>对比文档 (版本B)</h3>
              <el-input
                v-model="documentB"
                type="textarea"
                :rows="12"
                placeholder="请输入对比文档内容..."
                show-word-limit
                maxlength="5000"
              />
              <div class="document-meta">
                <span>字符数: {{ documentB.length }}</span>
                <span>版本: v2.0</span>
              </div>
            </div>
          </el-col>
        </el-row>
        
        <div class="comparison-config">
          <el-form :model="comparisonConfig" label-width="100px" inline>
            <el-form-item label="对比粒度">
              <el-select v-model="comparisonConfig.granularity">
                <el-option label="字符级" value="character" />
                <el-option label="词语级" value="word" />
                <el-option label="句子级" value="sentence" />
                <el-option label="段落级" value="paragraph" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="忽略格式">
              <el-switch v-model="comparisonConfig.ignoreFormatting" />
            </el-form-item>
            
            <el-form-item label="忽略空白">
              <el-switch v-model="comparisonConfig.ignoreWhitespace" />
            </el-form-item>
            
            <el-form-item label="智能匹配">
              <el-switch v-model="comparisonConfig.smartMatching" />
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-card>

    <el-card v-if="comparisonResult" class="comparison-result-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>对比结果</span>
          <div class="result-actions">
            <el-button size="small" @click="exportComparison">
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

      <div class="comparison-result">
        <!-- 统计摘要 -->
        <div class="comparison-summary">
          <el-row :gutter="16">
            <el-col :span="6">
              <div class="summary-item added">
                <div class="summary-value">{{ comparisonResult.stats.added }}</div>
                <div class="summary-label">新增内容</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="summary-item deleted">
                <div class="summary-value">{{ comparisonResult.stats.deleted }}</div>
                <div class="summary-label">删除内容</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="summary-item modified">
                <div class="summary-value">{{ comparisonResult.stats.modified }}</div>
                <div class="summary-label">修改内容</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="summary-item similarity">
                <div class="summary-value">{{ comparisonResult.stats.similarity }}%</div>
                <div class="summary-label">相似度</div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 详细对比 -->
        <el-tabs v-model="activeTab" type="border-card">
          <el-tab-pane label="并排对比" name="sidebyside">
            <div class="side-by-side-comparison">
              <el-row :gutter="20">
                <el-col :span="12">
                  <div class="comparison-panel">
                    <h4>版本A (基准)</h4>
                    <div class="comparison-content" v-html="highlightedDocumentA"></div>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="comparison-panel">
                    <h4>版本B (对比)</h4>
                    <div class="comparison-content" v-html="highlightedDocumentB"></div>
                  </div>
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="变更列表" name="changes">
            <div class="changes-list">
              <div v-for="(change, index) in comparisonResult.changes" :key="index" class="change-item">
                <div class="change-header">
                  <el-tag :type="getChangeTypeColor(change.type)" size="small">
                    {{ getChangeTypeLabel(change.type) }}
                  </el-tag>
                  <span class="change-position">位置: {{ change.position }}</span>
                </div>
                
                <div class="change-content">
                  <div v-if="change.type === 'deleted'" class="deleted-content">
                    <strong>删除:</strong> {{ change.oldContent }}
                  </div>
                  <div v-if="change.type === 'added'" class="added-content">
                    <strong>新增:</strong> {{ change.newContent }}
                  </div>
                  <div v-if="change.type === 'modified'" class="modified-content">
                    <div><strong>原内容:</strong> {{ change.oldContent }}</div>
                    <div><strong>新内容:</strong> {{ change.newContent }}</div>
                  </div>
                </div>
                
                <div class="change-impact">
                  <span class="impact-label">影响评估:</span>
                  <el-tag :type="getImpactLevelColor(change.impact)" size="small">
                    {{ getImpactLevelLabel(change.impact) }}
                  </el-tag>
                </div>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="AI分析" name="analysis">
            <div class="ai-analysis-content">
              <div class="analysis-section">
                <h4>变更摘要</h4>
                <div class="analysis-text">{{ comparisonResult.aiAnalysis.summary }}</div>
              </div>
              
              <div class="analysis-section">
                <h4>重要变更</h4>
                <ul class="important-changes">
                  <li v-for="change in comparisonResult.aiAnalysis.importantChanges" :key="change">
                    {{ change }}
                  </li>
                </ul>
              </div>
              
              <div class="analysis-section">
                <h4>影响评估</h4>
                <div class="analysis-text">{{ comparisonResult.aiAnalysis.impactAssessment }}</div>
              </div>
              
              <div class="analysis-section">
                <h4>建议</h4>
                <div class="analysis-text">{{ comparisonResult.aiAnalysis.recommendations }}</div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { DocumentCopy, MagicStick, Download, Document } from '@element-plus/icons-vue'
import { aiTestApi } from '@/api/ai-analysis'

const comparing = ref(false)
const activeTab = ref('sidebyside')

const documentA = ref(`第一章 项目概述

1.1 项目背景
本项目旨在建设一套智能化的文档管理系统，主要包括文档编辑、审核、版本控制等功能。

1.2 项目目标
通过本项目的实施，预期达到以下目标：
- 提升文档处理效率50%以上
- 降低人工审核成本30%
- 实现文档智能化管理

第二章 技术要求

2.1 系统架构要求
系统应采用微服务架构，支持水平扩展。

2.2 功能要求
1. 文档编辑功能
2. 审核流程
3. 版本控制`)

const documentB = ref(`第一章 项目概述

1.1 项目背景
本项目旨在建设一套基于AI技术的智能化文档管理系统，主要包括文档编辑、智能审核、版本控制、AI分析等功能。

1.2 项目目标
通过本项目的实施，预期达到以下目标：
- 提升文档处理效率60%以上
- 降低人工审核成本40%
- 实现文档智能化管理和分析
- 提供7×24小时智能服务

第二章 技术要求

2.1 系统架构要求
系统应采用微服务架构，支持水平扩展，具备高可用性和容错能力。

2.2 功能要求
1. 智能文档编辑功能
2. AI智能审核流程
3. 完整版本控制
4. 智能数据分析`)

const comparisonConfig = reactive({
  granularity: 'sentence',
  ignoreFormatting: false,
  ignoreWhitespace: true,
  smartMatching: true
})

const comparisonResult = ref<any>(null)

const highlightedDocumentA = computed(() => {
  if (!comparisonResult.value) return documentA.value.replace(/\n/g, '<br>')
  
  let highlighted = documentA.value
  // 简单的高亮处理
  highlighted = highlighted.replace(/文档编辑/g, '<span class="deleted">文档编辑</span>')
  highlighted = highlighted.replace(/审核流程/g, '<span class="deleted">审核流程</span>')
  highlighted = highlighted.replace(/50%/g, '<span class="modified">50%</span>')
  highlighted = highlighted.replace(/30%/g, '<span class="modified">30%</span>')
  
  return highlighted.replace(/\n/g, '<br>')
})

const highlightedDocumentB = computed(() => {
  if (!comparisonResult.value) return documentB.value.replace(/\n/g, '<br>')
  
  let highlighted = documentB.value
  // 简单的高亮处理
  highlighted = highlighted.replace(/基于AI技术的/g, '<span class="added">基于AI技术的</span>')
  highlighted = highlighted.replace(/智能文档编辑/g, '<span class="added">智能文档编辑</span>')
  highlighted = highlighted.replace(/AI智能审核/g, '<span class="added">AI智能审核</span>')
  highlighted = highlighted.replace(/60%/g, '<span class="modified">60%</span>')
  highlighted = highlighted.replace(/40%/g, '<span class="modified">40%</span>')
  highlighted = highlighted.replace(/提供7×24小时智能服务/g, '<span class="added">提供7×24小时智能服务</span>')
  
  return highlighted.replace(/\n/g, '<br>')
})

const runComparison = async () => {
  if (!documentA.value.trim() || !documentB.value.trim()) {
    ElMessage.warning('请输入两个文档的内容')
    return
  }

  comparing.value = true
  
  try {
    await aiTestApi.testDifferenceComparison()
    
    // 模拟生成对比结果
    comparisonResult.value = {
      stats: {
        added: 8,
        deleted: 3,
        modified: 6,
        similarity: 78
      },
      changes: [
        {
          type: 'added',
          position: '第1段',
          newContent: '基于AI技术的',
          impact: 'medium'
        },
        {
          type: 'added',
          position: '第1段',
          newContent: 'AI分析',
          impact: 'high'
        },
        {
          type: 'modified',
          position: '第2段',
          oldContent: '提升文档处理效率50%以上',
          newContent: '提升文档处理效率60%以上',
          impact: 'medium'
        },
        {
          type: 'modified',
          position: '第2段',
          oldContent: '降低人工审核成本30%',
          newContent: '降低人工审核成本40%',
          impact: 'medium'
        },
        {
          type: 'added',
          position: '第2段',
          newContent: '提供7×24小时智能服务',
          impact: 'high'
        },
        {
          type: 'modified',
          position: '第4段',
          oldContent: '系统应采用微服务架构，支持水平扩展。',
          newContent: '系统应采用微服务架构，支持水平扩展，具备高可用性和容错能力。',
          impact: 'high'
        }
      ],
      aiAnalysis: {
        summary: '版本B相比版本A主要增强了AI功能，提升了性能目标，并增加了高可用性要求。整体变更体现了系统向智能化方向的升级。',
        importantChanges: [
          '增加了AI技术相关功能描述',
          '提升了效率和成本节约目标',
          '新增了7×24小时服务要求',
          '强化了系统架构的可靠性要求'
        ],
        impactAssessment: '这些变更对系统设计和实施有重要影响，需要重新评估技术方案和资源投入。AI功能的增加将显著提升系统复杂度。',
        recommendations: '建议：1) 制定详细的AI功能实施计划；2) 评估高可用性架构的成本；3) 确保团队具备相应的技术能力；4) 调整项目时间计划和预算。'
      }
    }
    
    ElMessage.success('文档对比完成')
  } catch (error) {
    ElMessage.error('对比失败，请重试')
  } finally {
    comparing.value = false
  }
}

const getChangeTypeColor = (type: string) => {
  const colors: Record<string, string> = {
    added: 'success',
    deleted: 'danger',
    modified: 'warning'
  }
  return colors[type] || 'info'
}

const getChangeTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    added: '新增',
    deleted: '删除',
    modified: '修改'
  }
  return labels[type] || type
}

const getImpactLevelColor = (level: string) => {
  const colors: Record<string, string> = {
    low: 'info',
    medium: 'warning',
    high: 'danger'
  }
  return colors[level] || 'info'
}

const getImpactLevelLabel = (level: string) => {
  const labels: Record<string, string> = {
    low: '低影响',
    medium: '中等影响',
    high: '高影响'
  }
  return labels[level] || level
}

const exportComparison = () => {
  const data = {
    config: comparisonConfig,
    documents: {
      versionA: documentA.value,
      versionB: documentB.value
    },
    result: comparisonResult.value,
    timestamp: new Date().toISOString()
  }
  
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `document-comparison-${Date.now()}.json`
  a.click()
  URL.revokeObjectURL(url)
  
  ElMessage.success('对比结果已导出')
}

const generateReport = () => {
  ElMessage.info('报告生成功能开发中...')
}
</script>

<style scoped>
.difference-comparison-page {
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

.comparison-setup-card,
.comparison-result-card {
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

.comparison-setup {
  padding: 16px 0;
}

.document-section h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.document-meta {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.comparison-config {
  margin-top: 20px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.comparison-summary {
  margin-bottom: 24px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.summary-item {
  text-align: center;
  padding: 12px;
  border-radius: 6px;
}

.summary-item.added {
  background-color: #f0f9ff;
  border: 1px solid #67c23a;
}

.summary-item.deleted {
  background-color: #fef0f0;
  border: 1px solid #f56c6c;
}

.summary-item.modified {
  background-color: #fdf6ec;
  border: 1px solid #e6a23c;
}

.summary-item.similarity {
  background-color: #f4f4f5;
  border: 1px solid #909399;
}

.summary-value {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 4px;
}

.summary-label {
  font-size: 14px;
  color: #606266;
}

.side-by-side-comparison {
  padding: 16px 0;
}

.comparison-panel h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  text-align: center;
  padding: 8px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.comparison-content {
  line-height: 1.8;
  color: #303133;
  background-color: #fff;
  padding: 16px;
  border-radius: 6px;
  border: 1px solid #ebeef5;
  min-height: 400px;
}

.comparison-content :deep(.added) {
  background-color: #f0f9ff;
  color: #67c23a;
  padding: 2px 4px;
  border-radius: 3px;
}

.comparison-content :deep(.deleted) {
  background-color: #fef0f0;
  color: #f56c6c;
  text-decoration: line-through;
  padding: 2px 4px;
  border-radius: 3px;
}

.comparison-content :deep(.modified) {
  background-color: #fdf6ec;
  color: #e6a23c;
  padding: 2px 4px;
  border-radius: 3px;
}

.changes-list {
  padding: 16px 0;
}

.change-item {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 12px;
  background-color: #fff;
}

.change-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.change-position {
  font-size: 12px;
  color: #909399;
}

.change-content {
  margin-bottom: 12px;
  line-height: 1.5;
}

.deleted-content {
  color: #f56c6c;
}

.added-content {
  color: #67c23a;
}

.modified-content {
  color: #e6a23c;
}

.modified-content div {
  margin-bottom: 4px;
}

.change-impact {
  display: flex;
  align-items: center;
  gap: 8px;
}

.impact-label {
  font-size: 12px;
  color: #909399;
}

.ai-analysis-content {
  padding: 16px;
}

.analysis-section {
  margin-bottom: 24px;
}

.analysis-section h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.analysis-text {
  line-height: 1.6;
  color: #606266;
  background-color: #f8f9fa;
  padding: 16px;
  border-radius: 6px;
  border-left: 4px solid #409EFF;
}

.important-changes {
  margin: 0;
  padding-left: 20px;
}

.important-changes li {
  margin-bottom: 8px;
  line-height: 1.5;
  color: #606266;
}
</style>
