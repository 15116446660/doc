<template>
  <div class="check-selector">
    <el-alert
      title="请选择您需要对文档进行的质量检查项，我们建议您至少选择所有常规检查。"
      type="info"
      show-icon
      :closable="false"
      class="info-alert"
    />

    <div class="preset-buttons">
      <el-button @click="selectPreset('routine')">仅常规检查</el-button>
      <el-button type="primary" @click="selectPreset('recommended')">推荐检查 (常规+专项)</el-button>
      <el-button type="success" @click="selectPreset('full')">全面检查 (全部)</el-button>
    </div>

    <div v-for="(category, key) in checkCategories" :key="key" class="category-section">
      <div class="category-header">
        <h3 class="category-title">{{ category.title }}</h3>
        <div class="category-actions">
          <el-button 
            type="primary" 
            link 
            @click="toggleCategoryAll(key)"
            :disabled="options[key].length === 0"
          >
            {{ isCategoryAllSelected(key) ? '取消全选' : '全选' }}
          </el-button>
          <span class="category-count">
            ({{ getCategorySelectedCount(key) }}/{{ options[key].length }})
          </span>
        </div>
      </div>
      <el-checkbox-group v-model="selectedChecks">
        <el-row :gutter="20">
          <el-col :span="12" v-for="item in options[key]" :key="item.id">
            <div class="check-item-wrapper">
              <el-checkbox :label="item.id" size="large">
                <div class="card-content">
                  <div class="item-header">
                    <span class="item-name">{{ item.name }}</span>
                    <el-button 
                      v-if="item.description.length > 80" 
                      type="primary" 
                      link 
                      size="small"
                      @click.stop="showDescriptionDialog(item)"
                      class="detail-btn"
                    >
                      查看详情
                    </el-button>
                  </div>
                  <p class="item-description truncated">
                    {{ item.description }}
                  </p>
                </div>
              </el-checkbox>
            </div>
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
        round
      >
        开始检查 (已选 {{ selectedChecks.length }} 项)
      </el-button>
    </div>

    <!-- 描述详情弹窗 -->
    <el-dialog
      v-model="descriptionDialogVisible"
      :title="currentItem ? currentItem.name : '检查项详情'"
      width="500px"
      append-to-body
      destroy-on-close
    >
      <div class="description-dialog-content">
        <p>{{ currentItem ? currentItem.description : '' }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getCheckOptions, startQualityCheck } from '@/api/qualityCheck';
import type { CheckOptions, CheckItem } from '@/types/qualityCheck';

// 扩展CheckItem类型以包含展开状态
interface ExtendedCheckItem extends CheckItem {
  expanded: boolean;
}

interface ExtendedCheckOptions {
  routine: ExtendedCheckItem[];
  special: ExtendedCheckItem[];
  advanced: ExtendedCheckItem[];
}

const emit = defineEmits(['start-check']);

const options = ref<ExtendedCheckOptions>({ routine: [], special: [], advanced: [] });
const selectedChecks = ref<string[]>([]);
const isLoading = ref(false);
const descriptionDialogVisible = ref(false);
const currentItem = ref<ExtendedCheckItem | null>(null);

const checkCategories = {
  routine: { title: '常规检查' },
  special: { title: '专项检查' },
  advanced: { title: '高级检查' }
};

onMounted(async () => {
  try {
    const data = await getCheckOptions();
    // 为每个检查项添加展开状态
    Object.keys(data).forEach(key => {
      const categoryKey = key as keyof CheckOptions;
      data[categoryKey] = data[categoryKey].map(item => ({ ...item, expanded: false }));
    });
    options.value = data as ExtendedCheckOptions;
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

const toggleCategoryAll = (categoryKey: keyof ExtendedCheckOptions) => {
  const categoryItems = options.value[categoryKey];
  const categoryIds = categoryItems.map(item => item.id);
  
  if (isCategoryAllSelected(categoryKey)) {
    // 取消全选：从已选中移除该分类的所有项
    selectedChecks.value = selectedChecks.value.filter(id => !categoryIds.includes(id));
  } else {
    // 全选：添加该分类的所有项（不重复）
    const newSelected = [...selectedChecks.value];
    categoryIds.forEach(id => {
      if (!newSelected.includes(id)) {
        newSelected.push(id);
      }
    });
    selectedChecks.value = newSelected;
  }
};

const isCategoryAllSelected = (categoryKey: keyof ExtendedCheckOptions) => {
  const categoryItems = options.value[categoryKey];
  const categoryIds = categoryItems.map(item => item.id);
  return categoryIds.every(id => selectedChecks.value.includes(id));
};

const getCategorySelectedCount = (categoryKey: keyof ExtendedCheckOptions) => {
  const categoryItems = options.value[categoryKey];
  const categoryIds = categoryItems.map(item => item.id);
  return categoryIds.filter(id => selectedChecks.value.includes(id)).length;
};

const showDescriptionDialog = (item: ExtendedCheckItem) => {
  currentItem.value = item;
  descriptionDialogVisible.value = true;
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
  padding: 0 10px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.info-alert {
  margin-bottom: 20px;
  flex-shrink: 0;
}

.preset-buttons {
  margin-bottom: 30px;
  display: flex;
  gap: 15px;
  flex-shrink: 0;
}

.category-section {
  margin-bottom: 30px;
  flex-shrink: 0;
}

.category-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.category-title {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
  margin: 0;
}

.category-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.category-count {
  font-size: 14px;
  color: #909399;
}

.check-item-wrapper {
  background-color: #fcfcfc;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 20px;
  transition: all 0.2s ease-in-out;
}

.check-item-wrapper:hover {
  border-color: var(--el-color-primary);
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.check-item-wrapper .el-checkbox {
  width: 100%;
  height: 100%;
  padding: 20px;
  align-items: flex-start;
}

.card-content {
  display: flex;
  flex-direction: column;
  margin-left: 12px;
  white-space: normal;
  width: 100%;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.item-name {
  font-weight: 500;
  font-size: 16px;
  color: var(--el-text-color-primary);
}

.detail-btn {
  padding: 2px 8px;
  margin: 0;
  height: auto;
  line-height: 1;
  font-size: 12px;
}

.item-description {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
  margin: 0;
  word-break: break-word;
}

.item-description.truncated {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.start-button-container {
  text-align: center;
  margin-top: 30px;
  flex-shrink: 0;
}

/* 滚动区域 */
.category-section .el-row {
  max-height: 400px;
  overflow-y: auto;
  padding-right: 10px;
}

.category-section .el-row::-webkit-scrollbar {
  width: 6px;
}

.category-section .el-row::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.category-section .el-row::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.category-section .el-row::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 弹窗样式 */
.description-dialog-content {
  max-height: 60vh;
  overflow-y: auto;
  padding: 10px;
  line-height: 1.6;
  white-space: pre-wrap;
  font-size: 14px;
}

.description-dialog-content p {
  margin: 0;
}
</style>
