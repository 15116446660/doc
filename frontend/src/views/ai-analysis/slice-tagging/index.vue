<template>
  <div class="slice-tagging-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><Scissors /></el-icon>
        智能切片标记
      </h1>
      <p class="page-description">将文档智能分割为语义片段，并标记重要内容，提升文档结构化程度</p>
    </div>

    <!-- 操作区域 -->
    <el-card class="operation-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>文档切片分析</span>
          <el-button type="primary" @click="runSliceAnalysis" :loading="analyzing">
            <el-icon><MagicStick /></el-icon>
            {{ analyzing ? '分析中...' : '开始切片' }}
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
                :rows="15"
                placeholder="请输入或粘贴文档内容..."
                show-word-limit
                maxlength="10000"
              />
            </div>
          </el-col>
          
          <el-col :span="8">
            <div class="config-section">
              <h3>切片配置</h3>
              
              <el-form :model="sliceConfig" label-width="100px">
                <el-form-item label="切片策略">
                  <el-select v-model="sliceConfig.strategy" placeholder="选择切片策略">
                    <el-option label="语义切片" value="semantic" />
                    <el-option label="段落切片" value="paragraph" />
                    <el-option label="章节切片" value="section" />
                    <el-option label="智能切片" value="intelligent" />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="最小长度">
                  <el-input-number 
                    v-model="sliceConfig.minLength" 
                    :min="10" 
                    :max="500"
                    placeholder="字符数"
                  />
                </el-form-item>
                
                <el-form-item label="最大长度">
                  <el-input-number 
                    v-model="sliceConfig.maxLength" 
                    :min="100" 
                    :max="2000"
                    placeholder="字符数"
                  />
                </el-form-item>
                
                <el-form-item label="重要性阈值">
                  <el-slider 
                    v-model="sliceConfig.importanceThreshold" 
                    :min="0" 
                    :max="1" 
                    :step="0.1"
                    show-input
                  />
                </el-form-item>
                
                <el-form-item label="标记类型">
                  <el-checkbox-group v-model="sliceConfig.markingTypes">
                    <el-checkbox label="important">重要内容</el-checkbox>
                    <el-checkbox label="technical">技术要求</el-checkbox>
                    <el-checkbox label="legal">法规条款</el-checkbox>
                    <el-checkbox label="risk">风险点</el-checkbox>
                  </el-checkbox-group>
                </el-form-item>
              </el-form>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 切片结果 -->
    <el-card v-if="sliceResults.length > 0" class="result-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>切片结果 ({{ sliceResults.length }} 个片段)</span>
          <div class="result-actions">
            <el-button size="small" @click="exportSlices">
              <el-icon><Download /></el-icon>
              导出切片
            </el-button>
            <el-button size="small" @click="togglePreview">
              <el-icon><View /></el-icon>
              {{ showPreview ? '隐藏预览' : '显示预览' }}
            </el-button>
          </div>
        </div>
      </template>

      <div class="result-content">
        <!-- 切片统计 -->
        <div class="slice-stats">
          <el-row :gutter="16">
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-value">{{ sliceResults.length }}</div>
                <div class="stat-label">总片段数</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-value">{{ importantSlices.length }}</div>
                <div class="stat-label">重要片段</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-value">{{ averageLength }}</div>
                <div class="stat-label">平均长度</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-value">{{ coverageRate }}%</div>
                <div class="stat-label">覆盖率</div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 切片列表 -->
        <div class="slice-list">
          <div 
            v-for="(slice, index) in sliceResults" 
            :key="index"
            class="slice-item"
            :class="{ 'important': slice.importance >= sliceConfig.importanceThreshold }"
          >
            <div class="slice-header">
              <div class="slice-info">
                <span class="slice-index">片段 {{ index + 1 }}</span>
                <el-tag 
                  :type="getImportanceType(slice.importance)" 
                  size="small"
                  class="importance-tag"
                >
                  重要性: {{ (slice.importance * 100).toFixed(0) }}%
                </el-tag>
                <el-tag 
                  v-for="tag in slice.tags" 
                  :key="tag"
                  size="small"
                  effect="plain"
                  class="slice-tag"
                >
                  {{ getTagLabel(tag) }}
                </el-tag>
              </div>
              
              <div class="slice-actions">
                <el-button size="small" type="text" @click="editSlice(index)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button size="small" type="text" @click="deleteSlice(index)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </div>
            
            <div class="slice-content">
              <div class="slice-text">{{ slice.content }}</div>
              <div class="slice-meta">
                <span>长度: {{ slice.content.length }} 字符</span>
                <span>位置: {{ slice.startPos }} - {{ slice.endPos }}</span>
                <span v-if="slice.keywords">关键词: {{ slice.keywords }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 预览模式 -->
        <div v-if="showPreview" class="preview-section">
          <h4>文档预览（带标记）</h4>
          <div class="preview-content" v-html="generatePreview()"></div>
        </div>
      </div>
    </el-card>

    <!-- 切片编辑对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑切片" width="600px">
      <el-form v-if="editingSlice" :model="editingSlice" label-width="80px">
        <el-form-item label="内容">
          <el-input 
            v-model="editingSlice.content" 
            type="textarea" 
            :rows="6"
          />
        </el-form-item>
        
        <el-form-item label="重要性">
          <el-slider 
            v-model="editingSlice.importance" 
            :min="0" 
            :max="1" 
            :step="0.1"
            show-input
          />
        </el-form-item>
        
        <el-form-item label="标签">
          <el-checkbox-group v-model="editingSlice.tags">
            <el-checkbox label="important">重要内容</el-checkbox>
            <el-checkbox label="technical">技术要求</el-checkbox>
            <el-checkbox label="legal">法规条款</el-checkbox>
            <el-checkbox label="risk">风险点</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="关键词">
          <el-input 
            v-model="editingSlice.keywords" 
            placeholder="用逗号分隔多个关键词"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSliceEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Scissors, 
  MagicStick, 
  Download, 
  View, 
  Edit, 
  Delete 
} from '@element-plus/icons-vue'
import { aiTestApi } from '@/api/ai-analysis'

// 切片数据接口
interface SliceItem {
  content: string
  importance: number
  tags: string[]
  keywords?: string
  startPos: number
  endPos: number
}

// 响应式数据
const analyzing = ref(false)
const showPreview = ref(false)
const editDialogVisible = ref(false)
const editingSlice = ref<SliceItem | null>(null)
const editingIndex = ref(-1)

const documentContent = ref(`第一章 项目概述

1.1 项目背景
本项目旨在建设一套智能化的文档管理系统，主要包括文档编辑、审核、版本控制等功能。系统将采用先进的人工智能技术，为用户提供高效、便捷的文档处理体验。

1.2 项目目标
通过本项目的实施，预期达到以下目标：
- 提升文档处理效率50%以上
- 降低人工审核成本30%
- 实现文档智能化管理

第二章 技术要求

2.1 系统架构要求
系统应采用微服务架构，支持水平扩展，具备高可用性和容错能力。

2.2 功能要求
1. 文档编辑功能：支持多种文档格式，提供富文本编辑器
2. 审核流程：支持多级审核，可配置审核规则
3. 版本控制：完整的版本历史记录和回滚功能
4. 权限管理：细粒度的权限控制机制

2.3 性能要求
- 系统响应时间不超过2秒
- 支持1000并发用户
- 数据备份和恢复机制完善

第三章 质量标准

系统应满足国家相关标准和行业规范要求，通过相关质量认证。`)

const sliceConfig = reactive({
  strategy: 'semantic',
  minLength: 50,
  maxLength: 500,
  importanceThreshold: 0.6,
  markingTypes: ['important', 'technical']
})

const sliceResults = ref<SliceItem[]>([])

// 计算属性
const importantSlices = computed(() => 
  sliceResults.value.filter(slice => slice.importance >= sliceConfig.importanceThreshold)
)

const averageLength = computed(() => {
  if (sliceResults.value.length === 0) return 0
  const total = sliceResults.value.reduce((sum, slice) => sum + slice.content.length, 0)
  return Math.round(total / sliceResults.value.length)
})

const coverageRate = computed(() => {
  if (!documentContent.value || sliceResults.value.length === 0) return 0
  const totalSliceLength = sliceResults.value.reduce((sum, slice) => sum + slice.content.length, 0)
  return Math.round((totalSliceLength / documentContent.value.length) * 100)
})

// 方法
const runSliceAnalysis = async () => {
  if (!documentContent.value.trim()) {
    ElMessage.warning('请输入文档内容')
    return
  }

  analyzing.value = true
  
  try {
    // 调用AI切片分析接口
    await aiTestApi.testSliceTagging()
    
    // 模拟生成切片结果
    generateMockSlices()
    
    ElMessage.success('切片分析完成')
  } catch (error) {
    ElMessage.error('切片分析失败，请重试')
    console.error('Slice analysis failed:', error)
  } finally {
    analyzing.value = false
  }
}

const generateMockSlices = () => {
  const content = documentContent.value
  const paragraphs = content.split('\n\n').filter(p => p.trim())
  
  sliceResults.value = []
  let currentPos = 0
  
  paragraphs.forEach((paragraph, index) => {
    const trimmed = paragraph.trim()
    if (trimmed.length < sliceConfig.minLength) return
    
    const startPos = content.indexOf(trimmed, currentPos)
    const endPos = startPos + trimmed.length
    
    // 计算重要性（模拟）
    let importance = 0.3
    if (trimmed.includes('要求') || trimmed.includes('标准')) importance += 0.3
    if (trimmed.includes('技术') || trimmed.includes('功能')) importance += 0.2
    if (trimmed.includes('目标') || trimmed.includes('关键')) importance += 0.2
    if (trimmed.length > 200) importance += 0.1
    
    // 确定标签
    const tags: string[] = []
    if (importance >= 0.7) tags.push('important')
    if (trimmed.includes('技术') || trimmed.includes('功能') || trimmed.includes('性能')) tags.push('technical')
    if (trimmed.includes('标准') || trimmed.includes('规范') || trimmed.includes('要求')) tags.push('legal')
    if (trimmed.includes('风险') || trimmed.includes('问题')) tags.push('risk')
    
    // 提取关键词（简单实现）
    const keywords = extractKeywords(trimmed)
    
    sliceResults.value.push({
      content: trimmed,
      importance: Math.min(importance, 1),
      tags,
      keywords: keywords.join(', '),
      startPos,
      endPos
    })
    
    currentPos = endPos
  })
}

const extractKeywords = (text: string): string[] => {
  const keywords: string[] = []
  const patterns = [
    /系统|平台|功能|技术|管理|服务|支持|提供|实现/g,
    /要求|标准|规范|质量|性能|安全|可靠/g,
    /文档|数据|信息|内容|格式|版本/g
  ]
  
  patterns.forEach(pattern => {
    const matches = text.match(pattern)
    if (matches) {
      keywords.push(...matches)
    }
  })
  
  return [...new Set(keywords)].slice(0, 5)
}

const getImportanceType = (importance: number) => {
  if (importance >= 0.8) return 'danger'
  if (importance >= 0.6) return 'warning'
  return 'info'
}

const getTagLabel = (tag: string) => {
  const labels: Record<string, string> = {
    important: '重要',
    technical: '技术',
    legal: '法规',
    risk: '风险'
  }
  return labels[tag] || tag
}

const editSlice = (index: number) => {
  editingIndex.value = index
  editingSlice.value = { ...sliceResults.value[index] }
  editDialogVisible.value = true
}

const saveSliceEdit = () => {
  if (editingSlice.value && editingIndex.value >= 0) {
    sliceResults.value[editingIndex.value] = { ...editingSlice.value }
    editDialogVisible.value = false
    ElMessage.success('切片已更新')
  }
}

const deleteSlice = async (index: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个切片吗？', '确认删除', {
      type: 'warning'
    })
    
    sliceResults.value.splice(index, 1)
    ElMessage.success('切片已删除')
  } catch {
    // 用户取消
  }
}

const exportSlices = () => {
  const data = {
    config: sliceConfig,
    slices: sliceResults.value,
    stats: {
      totalSlices: sliceResults.value.length,
      importantSlices: importantSlices.value.length,
      averageLength: averageLength.value,
      coverageRate: coverageRate.value
    },
    timestamp: new Date().toISOString()
  }
  
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `slice-analysis-${Date.now()}.json`
  a.click()
  URL.revokeObjectURL(url)
  
  ElMessage.success('切片结果已导出')
}

const togglePreview = () => {
  showPreview.value = !showPreview.value
}

const generatePreview = () => {
  let html = documentContent.value
  
  // 为重要切片添加高亮
  sliceResults.value.forEach((slice, index) => {
    if (slice.importance >= sliceConfig.importanceThreshold) {
      const className = slice.importance >= 0.8 ? 'highlight-high' : 'highlight-medium'
      html = html.replace(
        slice.content,
        `<span class="${className}" title="片段${index + 1} - 重要性: ${(slice.importance * 100).toFixed(0)}%">${slice.content}</span>`
      )
    }
  })
  
  return html.replace(/\n/g, '<br>')
}
</script>

<style scoped>
.slice-tagging-page {
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

.result-actions {
  display: flex;
  gap: 8px;
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

.slice-stats {
  margin-bottom: 24px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #409EFF;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.slice-list {
  margin-bottom: 24px;
}

.slice-item {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  margin-bottom: 16px;
  background-color: #fff;
  transition: all 0.3s ease;
}

.slice-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.slice-item.important {
  border-left: 4px solid #f56c6c;
}

.slice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #ebeef5;
}

.slice-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.slice-index {
  font-weight: 600;
  color: #303133;
}

.importance-tag,
.slice-tag {
  margin: 0;
}

.slice-actions {
  display: flex;
  gap: 8px;
}

.slice-content {
  padding: 16px;
}

.slice-text {
  line-height: 1.6;
  color: #303133;
  margin-bottom: 12px;
}

.slice-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;
}

.preview-section {
  margin-top: 24px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.preview-section h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.preview-content {
  line-height: 1.8;
  color: #303133;
}

.preview-content :deep(.highlight-high) {
  background-color: #fef0f0;
  border: 1px solid #f56c6c;
  padding: 2px 4px;
  border-radius: 3px;
  cursor: help;
}

.preview-content :deep(.highlight-medium) {
  background-color: #fdf6ec;
  border: 1px solid #e6a23c;
  padding: 2px 4px;
  border-radius: 3px;
  cursor: help;
}

@media (max-width: 768px) {
  .slice-tagging-page {
    padding: 16px;
  }
  
  .slice-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .slice-meta {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
