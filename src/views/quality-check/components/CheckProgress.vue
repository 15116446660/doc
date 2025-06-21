<template>
  <div class="check-progress">
    <div v-if="isLoading" class="loading-container">
      <el-icon class="is-loading"><Loading /></el-icon>
      <p>正在获取检查进度...</p>
    </div>
    
    <div v-else-if="error" class="error-container">
      <el-icon color="red"><CircleClose /></el-icon>
      <p>{{ error }}</p>
      <el-button @click="fetchTask">重试</el-button>
    </div>
    
    <div v-else class="progress-content">
      <div class="progress-header">
        <h3>检查进度</h3>
        <p>正在对文档进行智能分析，请稍候...</p>
      </div>
      
      <div class="progress-list-container">
        <el-timeline v-if="task">
          <el-timeline-item
            v-for="item in task.progress"
            :key="item.checkId"
            :timestamp="getStatusText(item.status)"
            :type="getTimelineType(item.status)"
            :icon="getTimelineIcon(item.status)"
            size="large"
            class="timeline-item"
          >
            <div class="item-content">
              <h4>{{ item.name }}</h4>
              <el-progress 
                :percentage="item.progress" 
                :status="getProgressStatus(item.status)"
                :stroke-width="10"
                striped
                striped-flow
              />
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getQualityCheckTask } from '@/api/qualityCheck';
import type { QualityCheckTask, CheckStatus } from '@/types/qualityCheck';
import { Loading, CircleClose, Check, Clock, Warning } from '@element-plus/icons-vue';

const props = defineProps<{
  taskId: string | null;
}>();

const emit = defineEmits(['check-completed']);

const task = ref<QualityCheckTask | null>(null);
const isLoading = ref(true);
const error = ref<string | null>(null);
let pollInterval: NodeJS.Timeout | null = null;

onMounted(() => {
  if (props.taskId) {
    fetchTask();
    startPolling();
  }
});

onUnmounted(() => {
  if (pollInterval) {
    clearInterval(pollInterval);
  }
});

const fetchTask = async () => {
  if (!props.taskId) return;
  
  try {
    isLoading.value = true;
    error.value = null;
    const data = await getQualityCheckTask(props.taskId);
    task.value = data;
    
    // 检查是否完成
    if (data.status === 'finished') {
      if (pollInterval) {
        clearInterval(pollInterval);
      }
      emit('check-completed');
    }
  } catch (err) {
    error.value = '获取检查进度失败';
    ElMessage.error('获取检查进度失败');
  } finally {
    isLoading.value = false;
  }
};

const startPolling = () => {
  pollInterval = setInterval(() => {
    if (task.value?.status !== 'finished') {
      fetchTask();
    }
  }, 2000);
};

const getStatusText = (status: CheckStatus) => {
  switch (status) {
    case 'waiting': return '等待中';
    case 'checking': return '检查中';
    case 'completed': return '已完成';
    case 'failed': return '失败';
    case 'cancelled': return '已取消';
    default: return '未知';
  }
};

const getTimelineType = (status: CheckStatus) => {
  switch (status) {
    case 'completed': return 'success';
    case 'failed': return 'danger';
    case 'checking': return 'primary';
    default: return 'info';
  }
};

const getTimelineIcon = (status: CheckStatus) => {
  switch (status) {
    case 'completed': return Check;
    case 'failed': return Warning;
    case 'checking': return Loading;
    default: return Clock;
  }
};

const getProgressStatus = (status: CheckStatus) => {
  switch (status) {
    case 'completed': return 'success';
    case 'failed': return 'exception';
    case 'checking': return undefined;
    default: return undefined;
  }
};
</script>

<style scoped>
.check-progress {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.loading-container, .error-container {
  text-align: center;
  padding: 40px;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.loading-container .el-icon {
  font-size: 48px;
  color: var(--el-color-primary);
}

.error-container .el-icon {
  font-size: 48px;
}

.progress-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.progress-header {
  text-align: center;
  margin-bottom: 30px;
  flex-shrink: 0;
}

.progress-header h3 {
  font-size: 20px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 10px 0;
}

.progress-header p {
  color: #909399;
  margin: 0;
}

.progress-list-container {
  flex: 1;
  overflow-y: auto;
  padding: 0 20px;
  max-height: 500px;
}

.timeline-item h4 {
  font-weight: 500;
  margin-bottom: 10px;
  color: #303133;
}

.item-content {
  padding-bottom: 20px;
}

/* 滚动条样式 */
.progress-list-container::-webkit-scrollbar {
  width: 6px;
}

.progress-list-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.progress-list-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.progress-list-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
