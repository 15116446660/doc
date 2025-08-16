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
              <span>项目进度</span>
            </div>
          </template>
          <div class="chart-container">
            <!-- TODO: 添加项目进度图表 -->
            <div class="placeholder">项目进度图表</div>
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
</style> 