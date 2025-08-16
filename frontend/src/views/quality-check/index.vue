<template>
  <div class="quality-check-page">
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1>文档质量检查</h1>
          <p>遵循规范，铸就品质。我们通过多维度、深层次的智能分析，助您在投标中稳操胜券。</p>
        </div>
        <div class="header-right">
          <el-button 
            :type="isCompactMode ? 'primary' : 'default'"
            size="small"
            @click="toggleCompactMode"
            :icon="isCompactMode ? 'Expand' : 'Fold'"
          >
            {{ isCompactMode ? '标准模式' : '紧凑模式' }}
          </el-button>
        </div>
      </div>
    </div>

    <el-card class="main-card" :class="{ 'compact-mode': isCompactMode }">
      <div class="main-content-wrapper">
        <el-steps :active="activeStep" finish-status="success" align-center class="steps-bar">
          <el-step title="选择检查项" />
          <el-step title="智能分析中" />
          <el-step title="查看检查报告" />
        </el-steps>
        
        <div class="content-container">
          <transition name="fade-transform" mode="out-in">
            <component 
              :is="steps[activeStep].component"
              @start-check="handleStartCheck"
              @re-check="handleReCheck"
              @check-completed="handleCheckCompleted"
              :task-id="currentTaskId"
              :is-compact="isCompactMode"
              :key="activeStep"
            />
          </transition>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, shallowRef } from 'vue';
import CheckSelector from './components/CheckSelector.vue';
import CheckProgress from './components/CheckProgress.vue';
import CheckResults from './components/CheckResults.vue';

const activeStep = ref(0);
const currentTaskId = ref<string | null>(null);
const isCompactMode = ref(false);

const steps = shallowRef([
  { component: CheckSelector },
  { component: CheckProgress },
  { component: CheckResults }
]);

const handleStartCheck = (taskId: string) => {
  currentTaskId.value = taskId;
  activeStep.value = 1;
};

const handleReCheck = () => {
  currentTaskId.value = null;
  activeStep.value = 0;
}

const handleCheckCompleted = () => {
  activeStep.value = 2;
}

const toggleCompactMode = () => {
  isCompactMode.value = !isCompactMode.value;
}
</script>

<style scoped>
.quality-check-page {
  padding: 20px 30px;
  background-color: #f7f8fa;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.page-header {
  margin-bottom: 20px;
  flex-shrink: 0;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.header-left {
  flex: 1;
}

.header-right {
  flex-shrink: 0;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.page-header p {
  font-size: 14px;
  color: #909399;
  margin: 0;
  line-height: 1.5;
}

.main-card {
  border-radius: 8px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.main-card.compact-mode {
  border-radius: 4px;
}

.main-content-wrapper {
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.steps-bar {
  margin: 20px 0 30px 0;
  flex-shrink: 0;
}

.compact-mode .steps-bar {
  margin: 15px 0 20px 0;
}

.content-container {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s ease-out;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* 紧凑模式下的全局样式调整 */
.compact-mode :deep(.el-card__body) {
  padding: 15px;
}

.compact-mode :deep(.el-steps) {
  --el-step-title-font-size: 14px;
}

.compact-mode :deep(.el-step__title) {
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .quality-check-page {
    padding: 15px 20px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 15px;
  }
  
  .header-right {
    align-self: flex-end;
  }
  
  .page-header h1 {
    font-size: 20px;
  }
}
</style>
