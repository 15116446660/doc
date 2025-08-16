<template>
  <el-drawer
    v-model="visible"
    :title="templateData?.name ? `${templateData.name} (v${templateData.version})` : '模板内容'"
    direction="btt"
    size="100%"
    :destroy-on-close="true"
  >
    <div class="template-content-viewer">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated />
      </div>
      <div v-else-if="content" class="template-content">
        <div class="content-header">
          <div class="meta-info">
            <div class="info-item">
              <span class="label">版本号：</span>
              <span class="value">{{ templateData?.version || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">{{ isVersion ? '修改人' : '创建人' }}：</span>
              <span class="value">{{ isVersion ? templateData?.updater : (templateData?.creator || templateData?.owner) || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">{{ isVersion ? '修改时间' : '创建时间' }}：</span>
              <span class="value">{{ isVersion ? templateData?.updateTime : templateData?.createTime || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">状态：</span>
              <el-tag size="small" :type="getStatusType(templateData?.status || '')">
                {{ templateData?.status || '-' }}
              </el-tag>
            </div>
          </div>
          <div class="actions">
            <el-button type="primary" size="small" @click="handleDownload">
              <el-icon><download /></el-icon>
              下载文档
            </el-button>
          </div>
        </div>
        
        <el-divider />
        
        <div class="content-body">
          <!-- 这里可以使用iframe或其他方式展示Word内容 -->
          <div v-if="previewUrl" class="document-preview">
            <iframe :src="previewUrl" frameborder="0"></iframe>
          </div>
          <div v-else class="document-content">
            <!-- 模拟Word文档内容 -->
            <div class="document-header">
              <h1>{{ templateData?.name }}</h1>
            </div>
            <div v-if="isVersion" class="document-section">
              <h2>修订内容</h2>
              <p>{{ templateData?.content }}</p>
            </div>
            <div v-else class="document-section">
              <h2>模板描述</h2>
              <p>{{ templateData?.description || '暂无描述' }}</p>
            </div>
            <div class="document-section">
              <h2>模板内容</h2>
              <div v-html="content"></div>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="no-content">
        <el-empty description="暂无内容可预览" />
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'

// 定义组件属性
const props = defineProps<{
  modelValue: boolean
  templateData?: any
  isVersion?: boolean
}>()

// 定义事件
const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

// 控制抽屉显示
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 内部状态
const loading = ref(false)
const content = ref<string | null>(null)
const previewUrl = ref<string | null>(null)

// 获取状态类型
const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    '审核中': 'warning',
    '通过': 'success',
    '驳回': 'danger',
    '草稿': 'info',
    '已发布': 'success',
    '已废弃': 'danger'
  }
  return typeMap[status] || 'info'
}

// 加载内容
const loadContent = async () => {
  if (!props.templateData) return
  
  loading.value = true
  
  try {
    // 模拟加载内容
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    if (props.isVersion) {
      // 版本历史内容
      content.value = `
        <p>这是版本 "${props.templateData.name}" 的内容预览，版本号：${props.templateData.version}。</p>
        <div style="margin-top: 20px; padding: 15px; background-color: #f5f7fa; border-radius: 4px;">
          <h3 style="margin-top: 0;">版本内容示例</h3>
          <p>这里是版本的具体内容，可以包含格式化的文本、表格、图片等。</p>
          <p>在实际应用中，您可以集成文档预览组件来展示真实的Word文档内容。</p>
        </div>
      `
    } else {
      // 模板内容
      content.value = `
        <p>这是模板 "${props.templateData.name}" 的内容预览。在实际应用中，您可以通过API获取真实的模板内容或使用文档预览服务。</p>
        <p>模板类型: ${props.templateData.typeName || props.templateData.type || '未指定'}</p>
        <p>适用范围: ${props.templateData.scope || props.templateData.applicableScope || '全部'}</p>
        <p>标准类型: ${props.templateData.standardType || '未指定'}</p>
        <div style="margin-top: 20px; padding: 15px; background-color: #f5f7fa; border-radius: 4px;">
          <h3 style="margin-top: 0;">模板内容示例</h3>
          <p>这里是模板的具体内容，可以包含格式化的文本、表格、图片等。</p>
          <p>在实际应用中，您可以集成文档预览组件来展示真实的Word文档内容。</p>
        </div>
      `
    }
    
    // 如果有预览URL，可以设置
    if (props.templateData.url) {
      // 如果是Office文档，可以使用Office Online预览
      // previewUrl.value = `https://view.officeapps.live.com/op/view.aspx?src=${encodeURIComponent(props.templateData.url)}`
      
      // 这里只是模拟，实际项目中可以使用真实的预览服务
      previewUrl.value = null
    }
    
    loading.value = false
  } catch (error) {
    console.error('加载模板内容失败:', error)
    ElMessage.error('加载模板内容失败')
    loading.value = false
    content.value = null
  }
}

// 下载文档
const handleDownload = () => {
  if (!props.templateData) return
  
  // 如果有URL，可以直接下载
  if (props.templateData.url) {
    const link = document.createElement('a')
    link.href = props.templateData.url
    link.download = `${props.templateData.name}_v${props.templateData.version}.${props.templateData.suffix || 'docx'}`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } else {
    ElMessage.warning('暂无可下载的文件')
  }
}

// 监听抽屉打开状态变化
watch(() => visible.value, (newVal) => {
  if (newVal && props.templateData) {
    loadContent()
  } else {
    // 关闭抽屉时清空内容
    content.value = null
    previewUrl.value = null
  }
})
</script>

<style lang="scss" scoped>
.template-content-viewer {
  height: 100%;
  padding: 20px;
  
  .loading-container {
    padding: 40px;
  }
  
  .content-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .meta-info {
      display: flex;
      flex-wrap: wrap;
      gap: 20px;
      
      .info-item {
        display: flex;
        align-items: center;
        
        .label {
          color: var(--el-text-color-secondary);
          margin-right: 8px;
        }
        
        .value {
          font-weight: 500;
        }
      }
    }
  }
  
  .content-body {
    height: calc(100% - 100px);
    overflow: auto;
    
    .document-preview {
      height: 100%;
      
      iframe {
        width: 100%;
        height: 100%;
        border: 1px solid var(--el-border-color-light);
        border-radius: 4px;
      }
    }
    
    .document-content {
      padding: 20px;
      background-color: white;
      border: 1px solid var(--el-border-color-light);
      border-radius: 4px;
      
      .document-header {
        text-align: center;
        margin-bottom: 30px;
        
        h1 {
          font-size: 24px;
          font-weight: bold;
        }
      }
      
      .document-section {
        margin-bottom: 20px;
        
        h2 {
          font-size: 18px;
          font-weight: bold;
          margin-bottom: 10px;
          color: var(--el-color-primary);
        }
        
        p {
          line-height: 1.6;
          margin-bottom: 10px;
        }
      }
    }
  }
  
  .no-content {
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
  }
}
</style> 