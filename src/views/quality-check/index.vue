<template>
  <div class="quality-check-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>文档质量检查</span>
          <el-steps :active="activeStep" finish-status="success" simple>
            <el-step title="选择检查项" />
            <el-step title="检查进度" />
            <el-step title="检查结果" />
          </el-steps>
        </div>
      </template>
      
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
.quality-check-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-header span {
  font-size: 18px;
  font-weight: 500;
}
.el-steps {
  width: 50%;
}
.content-container {
  min-height: 600px;
}
.fade-transform-enter-active, .fade-transform-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}
.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style>
