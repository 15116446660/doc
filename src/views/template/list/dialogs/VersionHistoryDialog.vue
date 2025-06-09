<template>
  <el-dialog
    v-model="visible"
    :title="`版本历史 [${template?.name || ''}]`"
    width="75%"
    destroy-on-close
  >
    <div class="version-history">
      <base-list
        :columns="columns"
        :request-api="loadVersionHistory"
        :enable-view-switch="false"
        :table-props="{
          border: true,
          stripe: true,
          'header-cell-style': {
            'text-align': 'center',
            'background-color': 'var(--el-fill-color-light)',
            color: 'var(--el-text-color-regular)'
          }
        }"
        :pagination-config="{
          pageSize: 10,
          pageSizes: [10, 20, 50, 100],
          layout: 'total, sizes, prev, pager, next, jumper'
        }"
      >
        <!-- 状态列自定义渲染 -->
        <template #status="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
        </template>
        
        <!-- 操作列自定义渲染 -->
        <template #action="{ row }">
          <el-button 
            type="primary" 
            link
            @click="handleView(row)"
          >
            查看
          </el-button>
        </template>
      </base-list>
    </div>
  </el-dialog>

  <!-- 模板内容查看抽屉 -->
  <el-drawer
    v-model="drawerVisible"
    :title="currentVersion?.name ? `${currentVersion.name} (v${currentVersion.version})` : '模板内容'"
    direction="btt"
    size="100%"
    :destroy-on-close="true"
  >
    <div class="template-content-viewer">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated />
      </div>
      <div v-else-if="templateContent" class="template-content">
        <div class="content-header">
          <div class="meta-info">
            <div class="info-item">
              <span class="label">版本号：</span>
              <span class="value">{{ currentVersion?.version || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">修改人：</span>
              <span class="value">{{ currentVersion?.updater || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">修改时间：</span>
              <span class="value">{{ currentVersion?.updateTime || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">审核状态：</span>
              <el-tag size="small" :type="getStatusType(currentVersion?.status || '')">
                {{ currentVersion?.status || '-' }}
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
              <h1>{{ currentVersion?.name }}</h1>
            </div>
            <div class="document-section">
              <h2>修订内容</h2>
              <p>{{ currentVersion?.content }}</p>
            </div>
            <div class="document-section">
              <h2>模板内容示例</h2>
              <p>这里是模板内容的预览，实际项目中可以集成文档预览组件或使用Office Online等服务。</p>
              <p>当前展示的是模拟内容，您可以根据实际需求替换为真实的文档预览实现。</p>
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
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import type { Template } from '@/api/template'
import { getTemplateVersionHistory } from '@/api/template'
import type { VersionHistoryItem } from '@/api/template'
import BaseList from '@/components/BaseList/index.vue'
import type { TableColumn } from '@/components/BaseList/types'

const props = defineProps<{
  modelValue: boolean
  template?: Template
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

// 控制对话框显示
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 抽屉控制
const drawerVisible = ref(false)
const currentVersion = ref<VersionHistoryItem | null>(null)
const loading = ref(false)
const templateContent = ref<string | null>(null)
const previewUrl = ref<string | null>(null)

// 表格列配置
const columns = ref<TableColumn[]>([
  {
    type: 'index',
    label: '序号',
    width: 60,
    align: 'center',
    headerAlign: 'center'
  },
  {
    prop: 'name',
    label: '名称',
    minWidth: 150,
    headerAlign: 'center'
  },
  {
    prop: 'version',
    label: '版本号',
    width: 80,
    align: 'center',
    headerAlign: 'center'
  },
  {
    prop: 'content',
    label: '制作内容',
    minWidth: 300,
    showOverflowTooltip: true,
    headerAlign: 'center'
  },
  {
    prop: 'status',
    label: '审核状态',
    width: 100,
    align: 'center',
    slot: 'status',
    headerAlign: 'center'
  },
  {
    prop: 'reviewReason',
    label: '驳回原因',
    width: 120,
    align: 'center',
    showOverflowTooltip: true,
    headerAlign: 'center'
  },
  {
    prop: 'updater',
    label: '修改人',
    width: 100,
    align: 'center',
    headerAlign: 'center'
  },
  {
    prop: 'updateTime',
    label: '修改时间',
    width: 220,
    align: 'center',
    headerAlign: 'center'
  },
  {
    label: '操作',
    width: 80,
    align: 'center',
    fixed: 'right',
    slot: 'action',
    headerAlign: 'center'
  }
])

// 获取状态类型
const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    '审核中': 'warning',
    '通过': 'success',
    '驳回': 'danger',
    '草稿': 'info'
  }
  return typeMap[status] || 'info'
}

// 加载版本历史数据
const loadVersionHistory = async (params: any) => {
  if (!props.template?.id) {
    return {
      list: [],
      total: 0
    }
  }

  try {
    // 调用API获取版本历史
    const { pageNum, pageSize } = params
    const response = await getTemplateVersionHistory(props.template.id, { pageNum, pageSize })
    
    return {
      list: response.list,
      total: response.total
    }
  } catch (error) {
    console.error('加载版本历史失败:', error)
    ElMessage.error('加载版本历史失败')
    return {
      list: [],
      total: 0
    }
  }
}

// 查看版本
const handleView = async (version: VersionHistoryItem) => {
  currentVersion.value = version
  drawerVisible.value = true
  loading.value = true
  
  try {
    // 这里应该调用API获取模板内容
    // 模拟API请求延迟
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟获取模板内容
    templateContent.value = `这是模板 "${version.name}" 的内容，版本号：${version.version}`
    
    // 如果有预览URL，可以设置
    if (props.template?.url) {
      // 如果是Office文档，可以使用Office Online预览
      // previewUrl.value = `https://view.officeapps.live.com/op/view.aspx?src=${encodeURIComponent(props.template.url)}`
      
      // 这里只是模拟，实际项目中可以使用真实的预览服务
      previewUrl.value = null
    }
    
    loading.value = false
  } catch (error) {
    console.error('加载模板内容失败:', error)
    ElMessage.error('加载模板内容失败')
    loading.value = false
    templateContent.value = null
  }
}

// 下载文档
const handleDownload = () => {
  if (!currentVersion.value) return
  
  // 如果有URL，可以直接下载
  if (props.template?.url) {
    const link = document.createElement('a')
    link.href = props.template.url
    link.download = `${currentVersion.value.name}_v${currentVersion.value.version}.${props.template.suffix || 'docx'}`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } else {
    ElMessage.warning('暂无可下载的文件')
  }
}
</script>

<style lang="scss" scoped>
.version-history {
  margin: -20px;
  
  :deep(.el-table) {
    width: 100%;
  }
}

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