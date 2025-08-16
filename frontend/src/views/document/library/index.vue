<template>
  <div class="document-library">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">文档库</h1>
        <p class="page-description">管理和查看所有文档资料</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="showUploadDialog = true">
          <el-icon><Upload /></el-icon>
          上传文档
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-section">
      <el-card>
        <el-form :model="searchForm" inline>
          <el-form-item label="关键词">
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索文档标题、摘要"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="文档类型">
            <el-select v-model="searchForm.documentType" placeholder="请选择" clearable style="width: 150px">
              <el-option label="合同" value="CONTRACT" />
              <el-option label="技术文档" value="TECHNICAL" />
              <el-option label="业务文档" value="BUSINESS" />
              <el-option label="法律文档" value="LEGAL" />
              <el-option label="财务文档" value="FINANCIAL" />
              <el-option label="人事文档" value="HR" />
              <el-option label="项目文档" value="PROJECT" />
              <el-option label="政策文档" value="POLICY" />
              <el-option label="培训文档" value="TRAINING" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px">
              <el-option label="草稿" value="DRAFT" />
              <el-option label="审核中" value="REVIEWING" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已拒绝" value="REJECTED" />
              <el-option label="已发布" value="PUBLISHED" />
              <el-option label="已撤回" value="WITHDRAWN" />
              <el-option label="已归档" value="ARCHIVED" />
              <el-option label="已过期" value="EXPIRED" />
            </el-select>
          </el-form-item>
          <el-form-item label="创建时间">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 240px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 文档列表 -->
    <div class="table-section">
      <el-card>
        <el-table
          v-loading="loading"
          :data="documentList"
          stripe
          style="width: 100%"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="title" label="文档标题" min-width="200">
            <template #default="{ row }">
              <div class="document-title">
                <el-link type="primary" @click="handleView(row)">
                  {{ row.title }}
                </el-link>
                <div class="document-meta">
                  <el-tag size="small" type="info">{{ row.documentNumber }}</el-tag>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="documentType" label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getDocumentTypeColor(row.documentType)" size="small">
                {{ getDocumentTypeLabel(row.documentType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusColor(row.status)" size="small">
                {{ getStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="authorName" label="作者" width="120" />
          <el-table-column prop="fileSize" label="文件大小" width="100">
            <template #default="{ row }">
              {{ formatFileSize(row.fileSize) }}
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="160" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="handleView(row)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button size="small" @click="handleDownload(row)" v-if="row.canDownload">
                <el-icon><Download /></el-icon>
                下载
              </el-button>
              <el-button size="small" @click="handleEdit(row)" v-if="row.canEdit">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)" v-if="row.canDelete">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 上传文档对话框 -->
    <el-dialog
      v-model="showUploadDialog"
      title="上传文档"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="uploadFormRef"
        :model="uploadForm"
        :rules="uploadRules"
        label-width="100px"
      >
        <el-form-item label="文档文件" prop="file" required>
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            drag
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持 doc/docx/pdf/txt 等格式，文件大小不超过 100MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="文档标题" prop="title">
          <el-input v-model="uploadForm.title" placeholder="请输入文档标题" />
        </el-form-item>
        <el-form-item label="文档类型" prop="documentType">
          <el-select v-model="uploadForm.documentType" placeholder="请选择文档类型" style="width: 100%">
            <el-option label="合同" value="CONTRACT" />
            <el-option label="技术文档" value="TECHNICAL" />
            <el-option label="业务文档" value="BUSINESS" />
            <el-option label="法律文档" value="LEGAL" />
            <el-option label="财务文档" value="FINANCIAL" />
            <el-option label="人事文档" value="HR" />
            <el-option label="项目文档" value="PROJECT" />
            <el-option label="政策文档" value="POLICY" />
            <el-option label="培训文档" value="TRAINING" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="文档摘要">
          <el-input
            v-model="uploadForm.summary"
            type="textarea"
            :rows="3"
            placeholder="请输入文档摘要"
          />
        </el-form-item>
        <el-form-item label="安全级别">
          <el-select v-model="uploadForm.securityLevel" placeholder="请选择安全级别" style="width: 100%">
            <el-option label="公开" value="PUBLIC" />
            <el-option label="内部" value="INTERNAL" />
            <el-option label="机密" value="CONFIDENTIAL" />
            <el-option label="秘密" value="SECRET" />
            <el-option label="限制" value="RESTRICTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="uploadForm.tags" placeholder="请输入标签，多个标签用逗号分隔" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showUploadDialog = false">取消</el-button>
          <el-button type="primary" @click="handleUpload" :loading="uploading">
            上传
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 文档详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="文档详情"
      width="800px"
    >
      <div v-if="currentDocument" class="document-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="文档标题">{{ currentDocument.title }}</el-descriptions-item>
          <el-descriptions-item label="文档编号">{{ currentDocument.documentNumber }}</el-descriptions-item>
          <el-descriptions-item label="文档类型">
            <el-tag :type="getDocumentTypeColor(currentDocument.documentType)" size="small">
              {{ getDocumentTypeLabel(currentDocument.documentType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusColor(currentDocument.status)" size="small">
              {{ getStatusLabel(currentDocument.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="作者">{{ currentDocument.authorName }}</el-descriptions-item>
          <el-descriptions-item label="文件大小">{{ formatFileSize(currentDocument.fileSize) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentDocument.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ currentDocument.updatedAt }}</el-descriptions-item>
          <el-descriptions-item label="安全级别" span="2">
            <el-tag size="small">{{ getSecurityLevelLabel(currentDocument.securityLevel) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="标签" span="2">
            <span v-if="currentDocument.tags">{{ currentDocument.tags }}</span>
            <span v-else class="text-muted">无</span>
          </el-descriptions-item>
          <el-descriptions-item label="文档摘要" span="2">
            <div class="document-summary">
              {{ currentDocument.summary || '无摘要' }}
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showDetailDialog = false">关闭</el-button>
          <el-button type="primary" @click="handleDownload(currentDocument)" v-if="currentDocument?.canDownload">
            <el-icon><Download /></el-icon>
            下载文档
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, UploadInstance, UploadFile } from 'element-plus'
import {
  Upload,
  Search,
  Refresh,
  View,
  Download,
  Edit,
  Delete,
  UploadFilled
} from '@element-plus/icons-vue'
import { documentApi } from '@/api/document'
import type { Document, DocumentQuery } from '@/types/document-library'

// 响应式数据
const loading = ref(false)
const uploading = ref(false)
const showUploadDialog = ref(false)
const showDetailDialog = ref(false)
const documentList = ref<Document[]>([])
const currentDocument = ref<Document | null>(null)
const selectedDocuments = ref<Document[]>([])

// 表单引用
const uploadFormRef = ref<FormInstance>()
const uploadRef = ref<UploadInstance>()

// 搜索表单
const searchForm = reactive<DocumentQuery>({
  keyword: '',
  documentType: '',
  status: '',
  dateRange: []
})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 上传表单
const uploadForm = reactive({
  file: null as File | null,
  title: '',
  documentType: '',
  summary: '',
  securityLevel: 'PUBLIC',
  tags: ''
})

// 上传表单验证规则
const uploadRules = {
  title: [
    { required: true, message: '请输入文档标题', trigger: 'blur' },
    { max: 200, message: '标题长度不能超过200个字符', trigger: 'blur' }
  ],
  documentType: [
    { required: true, message: '请选择文档类型', trigger: 'change' }
  ]
}

// 生命周期
onMounted(() => {
  loadDocuments()
})

// 方法
const loadDocuments = async () => {
  try {
    loading.value = true
    const params = {
      ...searchForm,
      page: pagination.page - 1, // 后端从0开始
      size: pagination.size
    }

    // 处理日期范围
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.createdAtStart = searchForm.dateRange[0] + ' 00:00:00'
      params.createdAtEnd = searchForm.dateRange[1] + ' 23:59:59'
    }

    const response = await documentApi.getDocuments(params)
    documentList.value = response.content
    pagination.total = response.totalElements
  } catch (error) {
    console.error('加载文档列表失败:', error)
    ElMessage.error('加载文档列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadDocuments()
}

const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    documentType: '',
    status: '',
    dateRange: []
  })
  pagination.page = 1
  loadDocuments()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadDocuments()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadDocuments()
}

const handleSelectionChange = (selection: Document[]) => {
  selectedDocuments.value = selection
}

const handleView = (document: Document) => {
  currentDocument.value = document
  showDetailDialog.value = true
}

const handleDownload = async (document: Document) => {
  try {
    await documentApi.downloadDocument(document.id)
    ElMessage.success('下载成功')
  } catch (error) {
    console.error('下载文档失败:', error)
    ElMessage.error('下载文档失败')
  }
}

const handleEdit = (document: Document) => {
  // TODO: 实现编辑功能
  ElMessage.info('编辑功能开发中...')
}

const handleDelete = async (document: Document) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文档"${document.title}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await documentApi.deleteDocument(document.id)
    ElMessage.success('删除成功')
    loadDocuments()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除文档失败:', error)
      ElMessage.error('删除文档失败')
    }
  }
}

const handleFileChange = (file: UploadFile) => {
  uploadForm.file = file.raw as File
  if (!uploadForm.title && file.name) {
    // 自动填充标题（去掉扩展名）
    const nameWithoutExt = file.name.replace(/\.[^/.]+$/, '')
    uploadForm.title = nameWithoutExt
  }
}

const handleFileRemove = () => {
  uploadForm.file = null
}

const handleUpload = async () => {
  if (!uploadForm.file) {
    ElMessage.error('请选择要上传的文件')
    return
  }

  try {
    await uploadFormRef.value?.validate()

    uploading.value = true
    const formData = new FormData()
    formData.append('file', uploadForm.file)
    formData.append('title', uploadForm.title)
    formData.append('documentType', uploadForm.documentType)
    formData.append('summary', uploadForm.summary)
    formData.append('securityLevel', uploadForm.securityLevel)
    formData.append('tags', uploadForm.tags)

    await documentApi.uploadDocument(formData)

    ElMessage.success('上传成功')
    showUploadDialog.value = false
    resetUploadForm()
    loadDocuments()
  } catch (error) {
    console.error('上传文档失败:', error)
    ElMessage.error('上传文档失败')
  } finally {
    uploading.value = false
  }
}

const resetUploadForm = () => {
  Object.assign(uploadForm, {
    file: null,
    title: '',
    documentType: '',
    summary: '',
    securityLevel: 'PUBLIC',
    tags: ''
  })
  uploadRef.value?.clearFiles()
  uploadFormRef.value?.resetFields()
}

// 工具方法
const getDocumentTypeLabel = (type: string) => {
  const typeMap: Record<string, string> = {
    CONTRACT: '合同',
    TECHNICAL: '技术文档',
    BUSINESS: '业务文档',
    LEGAL: '法律文档',
    FINANCIAL: '财务文档',
    HR: '人事文档',
    PROJECT: '项目文档',
    POLICY: '政策文档',
    TRAINING: '培训文档',
    OTHER: '其他'
  }
  return typeMap[type] || type
}

const getDocumentTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    CONTRACT: 'danger',
    TECHNICAL: 'primary',
    BUSINESS: 'success',
    LEGAL: 'warning',
    FINANCIAL: 'info',
    HR: '',
    PROJECT: 'primary',
    POLICY: 'warning',
    TRAINING: 'success',
    OTHER: ''
  }
  return colorMap[type] || ''
}

const getStatusLabel = (status: string) => {
  const statusMap: Record<string, string> = {
    DRAFT: '草稿',
    REVIEWING: '审核中',
    APPROVED: '已通过',
    REJECTED: '已拒绝',
    PUBLISHED: '已发布',
    WITHDRAWN: '已撤回',
    ARCHIVED: '已归档',
    EXPIRED: '已过期'
  }
  return statusMap[status] || status
}

const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    DRAFT: 'info',
    REVIEWING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    PUBLISHED: 'primary',
    WITHDRAWN: '',
    ARCHIVED: 'info',
    EXPIRED: 'danger'
  }
  return colorMap[status] || ''
}

const getSecurityLevelLabel = (level: string) => {
  const levelMap: Record<string, string> = {
    PUBLIC: '公开',
    INTERNAL: '内部',
    CONFIDENTIAL: '机密',
    SECRET: '秘密',
    RESTRICTED: '限制'
  }
  return levelMap[level] || level
}

const formatFileSize = (size: number | null) => {
  if (!size) return '-'

  const units = ['B', 'KB', 'MB', 'GB']
  let index = 0
  let fileSize = size

  while (fileSize >= 1024 && index < units.length - 1) {
    fileSize /= 1024
    index++
  }

  return `${fileSize.toFixed(1)} ${units[index]}`
}
</script>

<style scoped>
.document-library {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  flex: 1;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.page-description {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.header-right {
  display: flex;
  gap: 12px;
}

.search-section {
  margin-bottom: 20px;
}

.table-section {
  margin-bottom: 20px;
}

.document-title {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.document-meta {
  display: flex;
  gap: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.document-detail {
  margin-bottom: 20px;
}

.document-summary {
  line-height: 1.6;
  color: #606266;
}

.text-muted {
  color: #909399;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .header-right {
    width: 100%;
    justify-content: flex-start;
  }

  .el-form--inline .el-form-item {
    display: block;
    margin-bottom: 16px;
  }

  .el-table {
    font-size: 12px;
  }
}
</style>
