<template>
  <div class="long-text-interaction-page">
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><ChatLineRound /></el-icon>
        长文本交互
      </h1>
      <p class="page-description">处理长文档的智能交互和分析，提供摘要、问答和深度分析功能</p>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="document-card" shadow="hover">
          <template #header>
            <span>文档内容</span>
          </template>
          
          <div class="document-content">
            <el-input
              v-model="documentContent"
              type="textarea"
              :rows="20"
              placeholder="请输入长文档内容..."
              show-word-limit
              maxlength="10000"
            />
            
            <div class="document-stats">
              <span>字符数: {{ documentContent.length }}</span>
              <span>预计阅读时间: {{ estimatedReadTime }} 分钟</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card class="interaction-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>智能交互</span>
              <el-button-group>
                <el-button 
                  size="small" 
                  :type="interactionMode === 'summary' ? 'primary' : ''"
                  @click="setInteractionMode('summary')"
                >
                  摘要
                </el-button>
                <el-button 
                  size="small" 
                  :type="interactionMode === 'qa' ? 'primary' : ''"
                  @click="setInteractionMode('qa')"
                >
                  问答
                </el-button>
                <el-button 
                  size="small" 
                  :type="interactionMode === 'analysis' ? 'primary' : ''"
                  @click="setInteractionMode('analysis')"
                >
                  分析
                </el-button>
              </el-button-group>
            </div>
          </template>
          
          <div class="interaction-content">
            <!-- 摘要模式 -->
            <div v-if="interactionMode === 'summary'" class="summary-mode">
              <div class="mode-header">
                <h4>文档摘要生成</h4>
                <el-button type="primary" @click="generateSummary" :loading="processing">
                  <el-icon><MagicStick /></el-icon>
                  生成摘要
                </el-button>
              </div>
              
              <div class="summary-config">
                <el-form :model="summaryConfig" label-width="80px" size="small">
                  <el-form-item label="摘要长度">
                    <el-select v-model="summaryConfig.length">
                      <el-option label="简短" value="short" />
                      <el-option label="中等" value="medium" />
                      <el-option label="详细" value="detailed" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="摘要类型">
                    <el-select v-model="summaryConfig.type">
                      <el-option label="关键点" value="keypoints" />
                      <el-option label="结构化" value="structured" />
                      <el-option label="执行摘要" value="executive" />
                    </el-select>
                  </el-form-item>
                </el-form>
              </div>
              
              <div v-if="summaryResult" class="summary-result">
                <h5>摘要结果</h5>
                <div class="result-text">{{ summaryResult }}</div>
              </div>
            </div>
            
            <!-- 问答模式 -->
            <div v-if="interactionMode === 'qa'" class="qa-mode">
              <div class="mode-header">
                <h4>智能问答</h4>
              </div>
              
              <div class="qa-input">
                <el-input
                  v-model="currentQuestion"
                  placeholder="请输入您的问题..."
                  @keyup.enter="askQuestion"
                >
                  <template #append>
                    <el-button @click="askQuestion" :loading="processing">
                      <el-icon><Search /></el-icon>
                    </el-button>
                  </template>
                </el-input>
              </div>
              
              <div class="qa-history">
                <div v-for="(qa, index) in qaHistory" :key="index" class="qa-item">
                  <div class="question">
                    <el-icon><QuestionFilled /></el-icon>
                    {{ qa.question }}
                  </div>
                  <div class="answer">
                    <el-icon><ChatDotRound /></el-icon>
                    {{ qa.answer }}
                  </div>
                </div>
                
                <el-empty v-if="qaHistory.length === 0" description="暂无问答记录" />
              </div>
            </div>
            
            <!-- 分析模式 -->
            <div v-if="interactionMode === 'analysis'" class="analysis-mode">
              <div class="mode-header">
                <h4>深度分析</h4>
                <el-button type="primary" @click="performAnalysis" :loading="processing">
                  <el-icon><TrendCharts /></el-icon>
                  开始分析
                </el-button>
              </div>
              
              <div class="analysis-config">
                <el-form :model="analysisConfig" label-width="80px" size="small">
                  <el-form-item label="分析维度">
                    <el-checkbox-group v-model="analysisConfig.dimensions">
                      <el-checkbox label="structure">结构分析</el-checkbox>
                      <el-checkbox label="content">内容分析</el-checkbox>
                      <el-checkbox label="sentiment">情感分析</el-checkbox>
                      <el-checkbox label="keywords">关键词提取</el-checkbox>
                    </el-checkbox-group>
                  </el-form-item>
                </el-form>
              </div>
              
              <div v-if="analysisResult" class="analysis-result">
                <el-tabs v-model="activeAnalysisTab" type="card">
                  <el-tab-pane 
                    v-for="(result, key) in analysisResult" 
                    :key="key"
                    :label="getAnalysisDimensionLabel(key)" 
                    :name="key"
                  >
                    <div class="analysis-content">{{ result }}</div>
                  </el-tab-pane>
                </el-tabs>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  ChatLineRound, 
  MagicStick, 
  Search, 
  QuestionFilled, 
  ChatDotRound, 
  TrendCharts 
} from '@element-plus/icons-vue'
import { aiTestApi } from '@/api/ai-analysis'

const processing = ref(false)
const interactionMode = ref('summary')
const activeAnalysisTab = ref('structure')

const documentContent = ref(`第一章 项目概述

1.1 项目背景
随着数字化转型的深入推进，企业对文档管理系统的需求日益增长。传统的文档管理方式已无法满足现代企业的高效协作需求，亟需一套智能化、自动化的文档处理解决方案。

本项目旨在构建一个基于人工智能技术的智能文档管理系统，通过集成先进的自然语言处理、机器学习等技术，为用户提供文档编辑、审核、版本控制、智能分析等全方位服务。

1.2 项目目标
通过本项目的实施，预期达到以下目标：
- 提升文档处理效率50%以上
- 降低人工审核成本30%
- 实现文档智能化管理和分析
- 建立标准化的文档处理流程
- 提供7×24小时的智能服务支持

第二章 技术架构

2.1 系统架构设计
系统采用微服务架构，主要包含以下核心模块：
- 用户管理服务：负责用户认证、权限管理
- 文档处理服务：提供文档编辑、格式转换功能
- AI分析服务：集成多种AI算法，提供智能分析能力
- 审核流程服务：支持多级审核，可配置审核规则
- 存储服务：提供文档存储、版本管理功能

2.2 技术选型
前端技术栈：Vue3 + TypeScript + Element Plus
后端技术栈：Spring Boot + MySQL + Redis + Elasticsearch
AI技术栈：Python + TensorFlow + BERT + GPT
部署技术栈：Docker + Kubernetes + Jenkins

第三章 功能规格

3.1 核心功能
系统提供以下核心功能：
1. 智能文档编辑：支持富文本编辑，实时协作
2. AI内容分析：自动识别文档结构，提取关键信息
3. 智能审核：基于规则引擎的自动审核
4. 版本控制：完整的版本历史和回滚功能
5. 权限管理：细粒度的权限控制机制
6. 数据分析：提供文档使用情况统计和分析报告

3.2 性能要求
- 系统响应时间：不超过2秒
- 并发用户数：支持1000+并发用户
- 系统可用性：99.9%以上
- 数据安全：符合等保三级要求`)

const currentQuestion = ref('')
const summaryResult = ref('')
const qaHistory = ref<Array<{question: string, answer: string}>>([])
const analysisResult = ref<Record<string, string>>({})

const summaryConfig = reactive({
  length: 'medium',
  type: 'structured'
})

const analysisConfig = reactive({
  dimensions: ['structure', 'content', 'keywords']
})

const estimatedReadTime = computed(() => {
  const wordsPerMinute = 200
  const wordCount = documentContent.value.length / 2 // 估算中文字数
  return Math.ceil(wordCount / wordsPerMinute)
})

const setInteractionMode = (mode: string) => {
  interactionMode.value = mode
}

const generateSummary = async () => {
  if (!documentContent.value.trim()) {
    ElMessage.warning('请输入文档内容')
    return
  }

  processing.value = true
  
  try {
    await aiTestApi.testLongTextInteraction()
    
    // 模拟生成摘要
    summaryResult.value = `本文档描述了一个智能文档管理系统项目。

主要内容包括：
1. 项目背景：企业数字化转型需求，传统文档管理方式的局限性
2. 项目目标：提升效率50%，降低成本30%，实现智能化管理
3. 技术架构：采用微服务架构，包含用户管理、文档处理、AI分析等核心模块
4. 技术选型：Vue3+Spring Boot+AI技术栈的现代化解决方案
5. 功能规格：涵盖智能编辑、AI分析、审核流程等核心功能
6. 性能要求：2秒响应时间，1000+并发，99.9%可用性

该项目旨在通过AI技术提升文档处理的智能化水平，为企业提供高效的文档管理解决方案。`
    
    ElMessage.success('摘要生成完成')
  } catch (error) {
    ElMessage.error('摘要生成失败')
  } finally {
    processing.value = false
  }
}

const askQuestion = async () => {
  if (!currentQuestion.value.trim()) {
    ElMessage.warning('请输入问题')
    return
  }
  
  if (!documentContent.value.trim()) {
    ElMessage.warning('请先输入文档内容')
    return
  }

  processing.value = true
  
  try {
    await aiTestApi.testLongTextInteraction()
    
    // 模拟生成回答
    const answer = generateMockAnswer(currentQuestion.value)
    
    qaHistory.value.unshift({
      question: currentQuestion.value,
      answer
    })
    
    currentQuestion.value = ''
    ElMessage.success('问题回答完成')
  } catch (error) {
    ElMessage.error('问答失败')
  } finally {
    processing.value = false
  }
}

const performAnalysis = async () => {
  if (!documentContent.value.trim()) {
    ElMessage.warning('请输入文档内容')
    return
  }
  
  if (analysisConfig.dimensions.length === 0) {
    ElMessage.warning('请选择分析维度')
    return
  }

  processing.value = true
  
  try {
    await aiTestApi.testLongTextInteraction()
    
    // 模拟生成分析结果
    analysisResult.value = {}
    
    if (analysisConfig.dimensions.includes('structure')) {
      analysisResult.value.structure = '文档结构清晰，分为三个主要章节：项目概述、技术架构、功能规格。每个章节都有明确的子节，逻辑层次分明。建议在每个章节末尾添加小结。'
    }
    
    if (analysisConfig.dimensions.includes('content')) {
      analysisResult.value.content = '文档内容详实，涵盖了项目的各个方面。技术选型现代化，目标明确可量化。建议补充风险评估和项目时间计划部分。'
    }
    
    if (analysisConfig.dimensions.includes('sentiment')) {
      analysisResult.value.sentiment = '文档整体语调积极正面，体现了对项目成功的信心。用词专业规范，适合技术文档的表达风格。'
    }
    
    if (analysisConfig.dimensions.includes('keywords')) {
      analysisResult.value.keywords = '关键词：智能文档管理、人工智能、微服务架构、Vue3、Spring Boot、文档处理、AI分析、审核流程、版本控制、权限管理'
    }
    
    activeAnalysisTab.value = analysisConfig.dimensions[0]
    ElMessage.success('分析完成')
  } catch (error) {
    ElMessage.error('分析失败')
  } finally {
    processing.value = false
  }
}

const generateMockAnswer = (question: string): string => {
  const lowerQuestion = question.toLowerCase()
  
  if (lowerQuestion.includes('技术') || lowerQuestion.includes('架构')) {
    return '根据文档内容，系统采用微服务架构，技术栈包括前端Vue3+TypeScript+Element Plus，后端Spring Boot+MySQL+Redis，AI技术使用Python+TensorFlow+BERT+GPT，部署采用Docker+Kubernetes+Jenkins。'
  } else if (lowerQuestion.includes('目标') || lowerQuestion.includes('效果')) {
    return '项目目标是提升文档处理效率50%以上，降低人工审核成本30%，实现文档智能化管理和分析，建立标准化的文档处理流程，提供7×24小时的智能服务支持。'
  } else if (lowerQuestion.includes('功能') || lowerQuestion.includes('特性')) {
    return '系统核心功能包括：智能文档编辑、AI内容分析、智能审核、版本控制、权限管理、数据分析等。支持富文本编辑、实时协作、自动审核、完整版本历史等特性。'
  } else if (lowerQuestion.includes('性能') || lowerQuestion.includes('要求')) {
    return '性能要求：系统响应时间不超过2秒，支持1000+并发用户，系统可用性99.9%以上，数据安全符合等保三级要求。'
  } else {
    return '根据文档内容，这是一个基于AI技术的智能文档管理系统项目，旨在通过先进技术提升企业文档处理效率和智能化水平。如需了解具体细节，请提出更具体的问题。'
  }
}

const getAnalysisDimensionLabel = (dimension: string): string => {
  const labels: Record<string, string> = {
    structure: '结构分析',
    content: '内容分析',
    sentiment: '情感分析',
    keywords: '关键词'
  }
  return labels[dimension] || dimension
}
</script>

<style scoped>
.long-text-interaction-page {
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

.document-card,
.interaction-card {
  height: calc(100vh - 200px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.document-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.document-stats {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  font-size: 12px;
  color: #909399;
}

.interaction-content {
  height: calc(100% - 20px);
  overflow-y: auto;
}

.mode-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.mode-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.summary-config,
.analysis-config {
  margin-bottom: 16px;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.summary-result,
.analysis-result {
  margin-top: 16px;
}

.summary-result h5 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.result-text {
  line-height: 1.6;
  color: #606266;
  background-color: #fff;
  padding: 16px;
  border-radius: 6px;
  border-left: 4px solid #409EFF;
}

.qa-input {
  margin-bottom: 16px;
}

.qa-history {
  max-height: 400px;
  overflow-y: auto;
}

.qa-item {
  margin-bottom: 16px;
  padding: 12px;
  background-color: #fff;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}

.question {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 8px;
  font-weight: 600;
  color: #409EFF;
}

.answer {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  color: #606266;
  line-height: 1.5;
}

.analysis-content {
  line-height: 1.6;
  color: #606266;
  background-color: #f8f9fa;
  padding: 16px;
  border-radius: 6px;
  border-left: 4px solid #409EFF;
}
</style>
