<template>
  <div class="quality-check-page">
    <div class="page-header">
      <h1>文档质量检查</h1>
      <p>遵循规范，铸就品质。我们通过多维度、深层次的智能分析，助您在投标中稳操胜券。</p>
    </div>

    <el-card class="main-card">
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

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.page-header p {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.main-card {
  border-radius: 8px;
  flex: 1;
  display: flex;
  flex-direction: column;
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
</style>
