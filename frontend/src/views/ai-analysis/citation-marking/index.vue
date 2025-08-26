<template>
  <div class="citation-marking-page">
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><Link /></el-icon>
        引用来源标记
      </h1>
      <p class="page-description">智能识别和标记文档中的引用来源，确保引用规范性和可信度</p>
    </div>

    <el-card class="operation-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>引用分析</span>
          <el-button type="primary" @click="runAnalysis" :loading="analyzing">
            <el-icon><MagicStick /></el-icon>
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
                placeholder="请输入包含引用的文档内容..."
                show-word-limit
                maxlength="5000"
              />
            </div>
          </el-col>
          
          <el-col :span="8">
            <div class="config-section">
              <h3>分析配置</h3>
              <el-form :model="analysisConfig" label-width="100px">
                <el-form-item label="引用数据库">
                  <el-select v-model="analysisConfig.sourceDatabase">
                    <el-option label="学术数据库" value="academic" />
                    <el-option label="法规数据库" value="legal" />
                    <el-option label="标准数据库" value="standard" />
                    <el-option label="综合数据库" value="comprehensive" />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="检查深度">
                  <el-radio-group v-model="analysisConfig.checkDepth">
                    <el-radio label="basic">基础检查</el-radio>
                    <el-radio label="detailed">详细检查</el-radio>
                    <el-radio label="comprehensive">全面检查</el-radio>
                  </el-radio-group>
                </el-form-item>
                
                <el-form-item label="引用格式">
                  <el-select v-model="analysisConfig.citationFormat">
                    <el-option label="GB/T 7714" value="gbt7714" />
                    <el-option label="APA" value="apa" />
                    <el-option label="MLA" value="mla" />
                    <el-option label="Chicago" value="chicago" />
                  </el-select>
                </el-form-item>
              </el-form>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <el-card v-if="analysisResult" class="result-card" shadow="hover">
      <template #header>
        <span>引用分析结果</span>
      </template>
      
      <div class="result-content">
        <el-tabs v-model="activeTab" type="border-card">
          <el-tab-pane label="已识别引用" name="identified">
            <div class="citations-list">
              <div v-for="(citation, index) in mockCitations" :key="index" class="citation-item">
                <div class="citation-header">
                  <el-tag :type="getCitationStatusType(citation.status)">
                    {{ getCitationStatusLabel(citation.status) }}
                  </el-tag>
                  <span class="citation-type">{{ citation.type }}</span>
                </div>
                <div class="citation-content">{{ citation.content }}</div>
                <div class="citation-meta">
                  <span>位置: {{ citation.position }}</span>
                  <span>可信度: {{ citation.reliability }}%</span>
                </div>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="格式检查" name="format">
            <div class="format-check-content">
              <div class="analysis-text" v-html="formatAnalysisContent(analysisResult)"></div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="建议优化" name="suggestions">
            <div class="suggestions-content">
              <div class="suggestion-item" v-for="(suggestion, index) in mockSuggestions" :key="index">
                <el-icon class="suggestion-icon"><Warning /></el-icon>
                <div class="suggestion-text">{{ suggestion }}</div>
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
import { Link, MagicStick, Warning } from '@element-plus/icons-vue'
import { aiTestApi } from '@/api/ai-analysis'

const analyzing = ref(false)
const activeTab = ref('identified')
const analysisResult = ref('')

const documentContent = ref(`根据GB/T 1.1-2020《标准化工作导则 第1部分：标准化文件的结构和起草规则》的要求，本文档应包含以下内容。

参考ISO 9001:2015质量管理体系要求，系统应建立完善的质量管理流程。

依据《网络安全法》第二十一条规定，网络运营者应当按照网络安全等级保护制度的要求。

如Smith等人(2020)在其研究中指出，人工智能技术在文档处理领域具有广阔的应用前景。`)

const analysisConfig = reactive({
  sourceDatabase: 'comprehensive',
  checkDepth: 'detailed',
  citationFormat: 'gbt7714'
})

const mockCitations = [
  {
    content: 'GB/T 1.1-2020《标准化工作导则 第1部分：标准化文件的结构和起草规则》',
    type: '国家标准',
    status: 'valid',
    position: '第1段',
    reliability: 95
  },
  {
    content: 'ISO 9001:2015质量管理体系要求',
    type: '国际标准',
    status: 'valid',
    position: '第2段',
    reliability: 98
  },
  {
    content: '《网络安全法》第二十一条',
    type: '法律法规',
    status: 'valid',
    position: '第3段',
    reliability: 100
  },
  {
    content: 'Smith等人(2020)',
    type: '学术文献',
    status: 'incomplete',
    position: '第4段',
    reliability: 60
  }
]

const mockSuggestions = [
  '建议为"Smith等人(2020)"补充完整的文献信息，包括文章标题、期刊名称等',
  '建议统一引用格式，按照GB/T 7714标准进行规范化',
  '建议添加参考文献列表，方便读者查阅原始资料'
]

const runAnalysis = async () => {
  if (!documentContent.value.trim()) {
    ElMessage.warning('请输入文档内容')
    return
  }

  analyzing.value = true
  
  try {
    const result = await aiTestApi.testCitationMarking()
    analysisResult.value = result
    ElMessage.success('引用分析完成')
  } catch (error) {
    ElMessage.error('分析失败，请重试')
  } finally {
    analyzing.value = false
  }
}

const getCitationStatusType = (status: string) => {
  const types: Record<string, string> = {
    valid: 'success',
    incomplete: 'warning',
    invalid: 'danger'
  }
  return types[status] || 'info'
}

const getCitationStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    valid: '有效',
    incomplete: '不完整',
    invalid: '无效'
  }
  return labels[status] || status
}

const formatAnalysisContent = (content: string) => {
  return content.replace(/\n/g, '<br>')
}
</script>

<style scoped>
.citation-marking-page {
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

.citations-list {
  padding: 16px;
}

.citation-item {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 12px;
  background-color: #fff;
}

.citation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.citation-type {
  font-size: 12px;
  color: #909399;
}

.citation-content {
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
  line-height: 1.5;
}

.citation-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;
}

.format-check-content,
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

.suggestion-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  margin-bottom: 8px;
  background-color: #fff3cd;
  border-radius: 6px;
  border-left: 4px solid #e6a23c;
}

.suggestion-icon {
  color: #e6a23c;
  margin-top: 2px;
}

.suggestion-text {
  flex: 1;
  line-height: 1.5;
  color: #856404;
}
</style>
