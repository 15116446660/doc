<template>
  <div class="template-detail-container">
    <div class="header">
      <div class="title-section">
        <h2>{{ template?.title || '未命名模板' }}</h2>
        <el-tag :type="getStatusType(template?.status)">{{ template?.status || '未设置' }}</el-tag>
      </div>
      <div class="actions">
        <el-button type="primary" @click="handleEdit">编辑</el-button>
        <el-button type="success" @click="handleCopy">复制</el-button>
        <el-button @click="handleBack">返回</el-button>
      </div>
    </div>

    <el-card class="info-card">
      <template #header>
        <div class="card-header">
          <span>基本信息</span>
        </div>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="模板分类">
          {{ template?.categoryName || '未分类' }}
        </el-descriptions-item>
        <el-descriptions-item label="版本号">
          {{ template?.version || '1.0.0' }}
        </el-descriptions-item>
        <el-descriptions-item label="负责人">
          <div class="user-info" v-if="template?.owner">
            <el-avatar :size="24" :src="template.ownerAvatar">
              {{ template.owner.charAt(0) }}
            </el-avatar>
            <span>{{ template.owner }}</span>
          </div>
          <span v-else>未分配</span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ template?.createTime || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ template?.updateTime || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">
          {{ template?.description || '暂无描述' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="content-card">
      <template #header>
        <div class="card-header">
          <span>模板内容</span>
        </div>
      </template>
      
      <!-- 这里可以根据实际需求展示模板的具体内容 -->
      <div class="template-content">
        <el-empty v-if="!template?.content" description="暂无内容" />
        <div v-else v-html="template.content"></div>
      </div>
    </el-card>

    <!-- 模板编辑对话框 -->
    <template-create-dialog
      ref="templateCreateDialogRef"
      :initial-data="template"
      @submit="handleUpdateSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import type { Template } from '@/api/template'
import { getTemplate, copyTemplate } from '@/api/template'
import TemplateCreateDialog from '../list/dialogs/TemplateCreateDialog.vue'

const route = useRoute()
const router = useRouter()
const template = ref<Template>()
const templateCreateDialogRef = ref()

// 获取模板详情
const fetchTemplateDetail = async () => {
  try {
    const id = route.params.id as string
    const response = await getTemplate(id)
    template.value = response
  } catch (error) {
    ElMessage.error('获取模板详情失败')
    console.error('Failed to fetch template detail:', error)
  }
}

// 获取状态类型
const getStatusType = (status?: string) => {
  const typeMap: Record<string, string> = {
    '已发布': 'success',
    '草稿': 'info',
    '审核中': 'warning',
    '已废弃': 'danger'
  }
  return typeMap[status || ''] || 'info'
}

// 处理编辑
const handleEdit = () => {
  templateCreateDialogRef.value?.open()
}

// 处理复制
const handleCopy = async () => {
  if (!template.value?.id) return
  
  try {
    await ElMessageBox.confirm(
      `确定要复制模板"${template.value.title}"吗？`,
      '复制确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    await copyTemplate(template.value.id)
    ElMessage.success('复制成功')
    router.push('/document/template/list')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('复制失败')
      console.error('Failed to copy template:', error)
    }
  }
}

// 处理返回
const handleBack = () => {
  router.back()
}

// 处理更新成功
const handleUpdateSuccess = () => {
  fetchTemplateDetail()
  ElMessage.success('更新成功')
}

// 组件挂载时获取模板详情
onMounted(() => {
  fetchTemplateDetail()
})
</script>

<style lang="scss" scoped>
.template-detail-container {
  padding: 20px;
  
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .title-section {
      display: flex;
      align-items: center;
      gap: 12px;
      
      h2 {
        margin: 0;
      }
    }
    
    .actions {
      display: flex;
      gap: 8px;
    }
  }
  
  .info-card {
    margin-bottom: 20px;
  }
  
  .content-card {
    min-height: 300px;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  
  .template-content {
    min-height: 200px;
    padding: 16px;
  }
}
</style> 