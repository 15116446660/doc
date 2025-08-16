<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="dashboard-card">
          <template #header>
            <div class="card-header">
              <span>进行中的项目</span>
            </div>
          </template>
          <div class="card-body">
            <div class="number">12</div>
            <div class="text">较上月增长 20%</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="dashboard-card">
          <template #header>
            <div class="card-header">
              <span>本月完成项目</span>
            </div>
          </template>
          <div class="card-body">
            <div class="number">8</div>
            <div class="text">较上月增长 15%</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="dashboard-card">
          <template #header>
            <div class="card-header">
              <span>待审批文档</span>
            </div>
          </template>
          <div class="card-body">
            <div class="number">5</div>
            <div class="text">较上月减少 10%</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="dashboard-card">
          <template #header>
            <div class="card-header">
              <span>中标率</span>
            </div>
          </template>
          <div class="card-body">
            <div class="number">75%</div>
            <div class="text">较上月增长 5%</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>API 连接测试</span>
            </div>
          </template>
          <div class="api-test-container">
            <div class="test-buttons">
              <el-button type="primary" @click="testHealthCheck" :loading="healthLoading">
                健康检查
              </el-button>
              <el-button type="success" @click="testSystemInfo" :loading="systemLoading">
                系统信息
              </el-button>
            </div>
            <div class="test-result" v-if="testResult">
              <el-alert
                :title="testResult.title"
                :type="testResult.type"
                :description="testResult.message"
                show-icon
                :closable="false"
              />
              <div class="result-data" v-if="testResult.data">
                <pre>{{ JSON.stringify(testResult.data, null, 2) }}</pre>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>待办事项</span>
            </div>
          </template>
          <el-table :data="todoList" style="width: 100%">
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="deadline" label="截止日期" width="180" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === '紧急' ? 'danger' : 'warning'">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { get } from '@/api/request'
import { ElMessage } from 'element-plus'

const todoList = ref([
  {
    title: '项目A标书审核',
    deadline: '2024-03-20',
    status: '紧急'
  },
  {
    title: '项目B技术方案编写',
    deadline: '2024-03-25',
    status: '进行中'
  },
  {
    title: '项目C投标文件准备',
    deadline: '2024-03-30',
    status: '进行中'
  }
])

// API测试相关
const healthLoading = ref(false)
const systemLoading = ref(false)
const testResult = ref<{
  title: string
  type: 'success' | 'error' | 'warning' | 'info'
  message: string
  data?: any
} | null>(null)

// 健康检查测试
const testHealthCheck = async () => {
  healthLoading.value = true
  testResult.value = null

  try {
    const response = await get('/test/health', {}, { rawResponse: true })
    testResult.value = {
      title: '健康检查成功',
      type: 'success',
      message: '后端服务运行正常',
      data: response.data
    }
    ElMessage.success('健康检查通过')
  } catch (error: any) {
    testResult.value = {
      title: '健康检查失败',
      type: 'error',
      message: error.message || '连接后端服务失败'
    }
    ElMessage.error('健康检查失败')
  } finally {
    healthLoading.value = false
  }
}

// 系统信息测试
const testSystemInfo = async () => {
  systemLoading.value = true
  testResult.value = null

  try {
    const response = await get('/test/system-info', {}, { rawResponse: true })
    testResult.value = {
      title: '获取系统信息成功',
      type: 'success',
      message: '成功获取系统信息',
      data: response.data
    }
    ElMessage.success('获取系统信息成功')
  } catch (error: any) {
    testResult.value = {
      title: '获取系统信息失败',
      type: 'error',
      message: error.message || '获取系统信息失败'
    }
    ElMessage.error('获取系统信息失败')
  } finally {
    systemLoading.value = false
  }
}
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.dashboard-card {
  height: 180px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-body {
  text-align: center;
  padding: 20px 0;
}

.number {
  font-size: 36px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
}

.text {
  font-size: 14px;
  color: #909399;
}

.mt-20 {
  margin-top: 20px;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder {
  color: #909399;
  font-size: 14px;
}

.api-test-container {
  padding: 20px;
}

.test-buttons {
  margin-bottom: 20px;
}

.test-buttons .el-button {
  margin-right: 10px;
}

.test-result {
  margin-top: 20px;
}

.result-data {
  margin-top: 15px;
  background: #f5f7fa;
  border-radius: 4px;
  padding: 15px;
  max-height: 300px;
  overflow-y: auto;
}

.result-data pre {
  margin: 0;
  font-size: 12px;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-all;
}
</style> 