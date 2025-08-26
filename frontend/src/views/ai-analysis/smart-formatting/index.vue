<template>
  <div class="smart-formatting-page">
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><MagicStick /></el-icon>
        AI智能格式化
      </h1>
      <p class="page-description">智能优化文档格式和结构，提升文档的专业性和可读性</p>
    </div>

    <el-card class="operation-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>格式化分析</span>
          <el-button type="primary" @click="runAnalysis" :loading="analyzing">
            <el-icon><Refresh /></el-icon>
            {{ analyzing ? '分析中...' : '开始分析' }}
          </el-button>
        </div>
      </template>

      <div class="operation-content">
        <el-row :gutter="20">
          <el-col :span="16">
            <div class="input-section">
              <h3>文档内容</h3>
              <el-input
                v-model="documentContent"
                type="textarea"
                :rows="12"
                placeholder="请输入需要格式化的文档内容..."
                show-word-limit
                maxlength="5000"
              />
            </div>
          </el-col>
          
          <el-col :span="8">
            <div class="config-section">
              <h3>格式化配置</h3>
              <el-form :model="formatConfig" label-width="100px">
                <el-form-item label="格式规则">
                  <el-select v-model="formatConfig.formatRules">
                    <el-option label="标准格式" value="standard" />
                    <el-option label="学术格式" value="academic" />
                    <el-option label="商务格式" value="business" />
                    <el-option label="技术文档" value="technical" />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="优化级别">
                  <el-radio-group v-model="formatConfig.optimizationLevel">
                    <el-radio label="basic">基础优化</el-radio>
                    <el-radio label="advanced">高级优化</el-radio>
                    <el-radio label="professional">专业优化</el-radio>
                  </el-radio-group>
                </el-form-item>
                
                <el-form-item label="检查项目">
                  <el-checkbox-group v-model="formatConfig.checkItems">
                    <el-checkbox label="heading">标题格式</el-checkbox>
                    <el-checkbox label="paragraph">段落格式</el-checkbox>
                    <el-checkbox label="list">列表格式</el-checkbox>
                    <el-checkbox label="table">表格格式</el-checkbox>
                  </el-checkbox-group>
                </el-form-item>
              </el-form>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <el-card v-if="analysisResult" class="result-card" shadow="hover">
      <template #header>
        <span>格式化分析结果</span>
      </template>
      
      <div class="result-content">
        <el-tabs v-model="activeTab" type="border-card">
          <el-tab-pane label="问题检测" name="issues">
            <div class="issues-list">
              <div v-for="(issue, index) in mockIssues" :key="index" class="issue-item">
                <div class="issue-header">
                  <el-tag :type="getIssueSeverityType(issue.severity)">
                    {{ getIssueSeverityLabel(issue.severity) }}
                  </el-tag>
                  <span class="issue-type">{{ issue.type }}</span>
                </div>
                <div class="issue-description">{{ issue.description }}</div>
                <div class="issue-suggestion">建议: {{ issue.suggestion }}</div>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="格式化预览" name="preview">
            <div class="preview-content">
              <div class="analysis-text" v-html="formatAnalysisContent(analysisResult)"></div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="优化建议" name="suggestions">
            <div class="suggestions-content">
              <div class="suggestion-category" v-for="(category, key) in mockSuggestionCategories" :key="key">
                <h4>{{ category.title }}</h4>
                <ul>
                  <li v-for="suggestion in category.items" :key="suggestion">{{ suggestion }}</li>
                </ul>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick, Refresh } from '@element-plus/icons-vue'
import { aiTestApi } from '@/api/ai-analysis'

const analyzing = ref(false)
const activeTab = ref('issues')
const analysisResult = ref('')

const documentContent = ref(`第一章项目概述
1.1项目背景
本项目旨在建设智能化文档管理系统。

1.2项目目标
- 提升效率
-降低成本
- 智能化管理

第二章技术要求
2.1系统架构
系统采用微服务架构。

2.2功能要求
1)文档编辑功能
2)审核流程
3)版本控制`)

const formatConfig = reactive({
  formatRules: 'standard',
  optimizationLevel: 'advanced',
  checkItems: ['heading', 'paragraph', 'list']
})

const mockIssues = [
  {
    type: '标题格式',
    severity: 'high',
    description: '标题层级格式不统一，缺少适当的间距',
    suggestion: '使用标准的标题样式，添加适当的上下间距'
  },
  {
    type: '列表格式',
    severity: 'medium',
    description: '列表项格式不一致，混用了不同的标记符号',
    suggestion: '统一使用相同的列表标记符号，保持格式一致'
  },
  {
    type: '段落格式',
    severity: 'low',
    description: '段落间距不规范，部分段落缺少空行',
    suggestion: '在段落之间添加适当的空行，提升可读性'
  }
]

const mockSuggestionCategories = {
  structure: {
    title: '结构优化',
    items: [
      '建议在每个章节前添加概述',
      '为长段落添加小标题进行分割',
      '在文档末尾添加总结部分'
    ]
  },
  format: {
    title: '格式优化',
    items: [
      '统一标题字体和大小',
      '规范化列表项的缩进',
      '调整段落行距为1.5倍'
    ]
  },
  content: {
    title: '内容优化',
    items: [
      '为技术术语添加解释',
      '补充必要的图表说明',
      '添加交叉引用和页码'
    ]
  }
}

const runAnalysis = async () => {
  if (!documentContent.value.trim()) {
    ElMessage.warning('请输入文档内容')
    return
  }

  analyzing.value = true
  
  try {
    const result = await aiTestApi.testSmartFormatting()
    analysisResult.value = result
    ElMessage.success('格式化分析完成')
  } catch (error) {
    ElMessage.error('分析失败，请重试')
  } finally {
    analyzing.value = false
  }
}

const getIssueSeverityType = (severity: string) => {
  const types: Record<string, string> = {
    high: 'danger',
    medium: 'warning',
    low: 'info'
  }
  return types[severity] || 'info'
}

const getIssueSeverityLabel = (severity: string) => {
  const labels: Record<string, string> = {
    high: '严重',
    medium: '中等',
    low: '轻微'
  }
  return labels[severity] || severity
}

const formatAnalysisContent = (content: string) => {
  return content.replace(/\n/g, '<br>')
}
</script>

<style scoped>
.smart-formatting-page {
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
.result-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.issues-list {
  padding: 16px;
}

.issue-item {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 12px;
  background-color: #fff;
}

.issue-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.issue-type {
  font-size: 12px;
  color: #909399;
}

.issue-description {
  color: #303133;
  margin-bottom: 8px;
  line-height: 1.5;
}

.issue-suggestion {
  color: #606266;
  font-size: 14px;
  font-style: italic;
}

.preview-content,
.suggestions-content {
  padding: 16px;
}

.analysis-text {
  line-height: 1.6;
  color: #606266;
  background-color: #f8f9fa;
  padding: 16px;
  border-radius: 6px;
  border-left: 4px solid #409EFF;
}

.suggestion-category {
  margin-bottom: 24px;
}

.suggestion-category h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.suggestion-category ul {
  margin: 0;
  padding-left: 20px;
}

.suggestion-category li {
  margin-bottom: 8px;
  line-height: 1.5;
  color: #606266;
}
</style>
