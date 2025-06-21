<template>
  <div class="check-selector">
    <el-alert
      title="请选择您需要对文档进行的质量检查项，我们建议您至少选择所有常规检查。"
      type="info"
      show-icon
      :closable="false"
    />

    <div class="preset-buttons">
      <el-button @click="selectPreset('routine')">仅常规检查</el-button>
      <el-button type="primary" @click="selectPreset('recommended')">推荐检查 (常规+专项)</el-button>
      <el-button type="success" @click="selectPreset('full')">全面检查 (全部)</el-button>
    </div>

    <div v-for="(category, key) in checkCategories" :key="key" class="category-section">
      <h3>{{ category.title }}</h3>
      <el-checkbox-group v-model="selectedChecks">
        <el-row :gutter="20">
          <el-col :span="8" v-for="item in options[key]" :key="item.id">
            <el-card shadow="hover" class="check-item-card">
              <el-checkbox :label="item.id" size="large" border>
                <div class="card-content">
                  <span class="item-name">{{ item.name }}</span>
                  <p class="item-description">{{ item.description }}</p>
                </div>
              </el-checkbox>
            </el-card>
          </el-col>
        </el-row>
      </el-checkbox-group>
    </div>

    <div class="start-button-container">
      <el-button 
        type="primary" 
        size="large" 
        @click="handleStartCheck" 
        :disabled="selectedChecks.length === 0 || isLoading"
        :loading="isLoading"
      >
        开始检查 (已选 {{ selectedChecks.length }} 项)
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getCheckOptions, startQualityCheck } from '@/api/qualityCheck';
import type { CheckOptions } from '@/types/qualityCheck';

const emit = defineEmits(['start-check']);

const options = ref<CheckOptions>({ routine: [], special: [], advanced: [] });
const selectedChecks = ref<string[]>([]);
const isLoading = ref(false);

const checkCategories = {
  routine: { title: '常规检查' },
  special: { title: '专项检查' },
  advanced: { title: '高级检查' }
};

onMounted(async () => {
  try {
    const data = await getCheckOptions();
    options.value = data;
    // 默认选中所有常规检查
    selectPreset('routine');
  } catch (error) {
    ElMessage.error('获取检查项失败');
  }
});

const selectPreset = (type: 'routine' | 'recommended' | 'full') => {
  const routineIds = options.value.routine.map(c => c.id);
  const specialIds = options.value.special.map(c => c.id);
  const advancedIds = options.value.advanced.map(c => c.id);

  if (type === 'routine') {
    selectedChecks.value = [...routineIds];
  } else if (type === 'recommended') {
    selectedChecks.value = [...routineIds, ...specialIds];
  } else if (type === 'full') {
    selectedChecks.value = [...routineIds, ...specialIds, ...advancedIds];
  }
};

const handleStartCheck = async () => {
  if (selectedChecks.value.length === 0) {
    ElMessage.warning('请至少选择一个检查项');
    return;
  }
  isLoading.value = true;
  try {
    const res = await startQualityCheck(selectedChecks.value);
    ElMessage.success('质量检查任务已启动');
    emit('start-check', res.taskId);
  } catch (error) {
    ElMessage.error('启动检查任务失败');
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
.check-selector {
  padding: 0 20px;
}
.preset-buttons {
  margin: 20px 0;
  display: flex;
  gap: 10px;
}
.category-section {
  margin-bottom: 30px;
}
.category-section h3 {
  margin-bottom: 15px;
  font-size: 16px;
  border-left: 4px solid var(--el-color-primary);
  padding-left: 10px;
}
.check-item-card {
  margin-bottom: 20px;
}
.check-item-card .el-checkbox {
  width: 100%;
  height: 100%;
  padding: 15px;
  align-items: flex-start;
}
.card-content {
  display: flex;
  flex-direction: column;
  margin-left: 10px;
  white-space: normal;
}
.item-name {
  font-weight: 500;
  color: var(--el-text-color-primary);
}
.item-description {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-top: 5px;
  line-height: 1.4;
}
.start-button-container {
  text-align: center;
  margin-top: 20px;
}
</style>
