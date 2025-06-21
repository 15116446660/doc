<template>
  <div class="check-progress">
    <div v-if="isLoading" class="loading-container">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <p>正在获取任务状态...</p>
    </div>
    
    <div v-else-if="error" class="error-container">
      <el-icon :size="40"><CircleClose /></el-icon>
      <p>{{ error }}</p>
      <el-button @click="fetchTaskStatus" type="primary">重试</el-button>
    </div>

    <div v-else class="progress-list-container">
      <el-timeline>
        <el-timeline-item
          v-for="item in task.progress"
          :key="item.checkId"
          :timestamp="getStatusText(item.status)"
          :type="getTimelineType(item.status)"
          :icon="getTimelineIcon(item.status)"
          size="large"
        >
          <h4>{{ item.name }}</h4>
          <el-progress 
            :percentage="item.progress" 
            :status="getProgressStatus(item.status)"
            :stroke-width="10"
            striped
            striped-flow
          />
        </el-timeline-item>
      </el-timeline>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, shallowRef } from 'vue';
import { ElMessage } from 'element-plus';
import { getQualityCheckTask } from '@/api/qualityCheck';
import type { QualityCheckTask, CheckStatus } from '@/types/qualityCheck';
import { Loading, CircleCheck, CircleClose, More } from '@element-plus/icons-vue';

const props = defineProps({
  taskId: {
    type: String,
    required: true,
  },
});

const emit = defineEmits(['check-completed']);

const isLoading = ref(true);
const error = ref<string | null>(null);
const task = ref<QualityCheckTask>({} as QualityCheckTask);
let pollInterval: number | null = null;

const fetchTaskStatus = async () => {
  if (!props.taskId) {
    error.value = '无效的任务ID';
    isLoading.value = false;
    return;
  }
  try {
    const data = await getQualityCheckTask(props.taskId);
    task.value = data;
    if (data.status === 'finished') {
      ElMessage.success('所有检查项已完成！');
      if (pollInterval) clearInterval(pollInterval);
      setTimeout(() => {
        emit('check-completed');
      }, 500);
    }
  } catch (e) {
    error.value = '获取任务状态失败';
    if (pollInterval) clearInterval(pollInterval);
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchTaskStatus();
  pollInterval = window.setInterval(fetchTaskStatus, 2000);
});

onUnmounted(() => {
  if (pollInterval) {
    clearInterval(pollInterval);
  }
});

const getStatusText = (status: CheckStatus) => {
  const map = {
    waiting: '等待中',
    checking: '检查中',
    completed: '已完成',
    failed: '失败',
    cancelled: '已取消',
  };
  return map[status];
};

const getTimelineType = (status: CheckStatus) => {
  const map = {
    waiting: 'info',
    checking: 'primary',
    completed: 'success',
    failed: 'danger',
    cancelled: 'warning',
  };
  return map[status] as 'primary' | 'success' | 'info' | 'danger' | 'warning';
};

const getTimelineIcon = (status: CheckStatus) => {
  const map = {
    waiting: shallowRef(More),
    checking: shallowRef(Loading),
    completed: shallowRef(CircleCheck),
    failed: shallowRef(CircleClose),
    cancelled: shallowRef(CircleClose),
  };
  return map[status];
};

const getProgressStatus = (status: CheckStatus) => {
  if (status === 'completed') return 'success';
  if (status === 'failed') return 'exception';
  return undefined;
};
</script>

<style scoped>
.check-progress {
  padding: 20px 50px;
}
.loading-container, .error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  gap: 20px;
  color: #909399;
}
.progress-list-container {
  max-width: 800px;
  margin: 0 auto;
}
.el-timeline-item h4 {
  font-weight: 500;
  margin-bottom: 10px;
}
</style>
