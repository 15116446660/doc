<template>
  <div class="check-results" :class="{ 'compact-mode': props.isCompact }">
    <div v-if="isLoading" class="loading-container">
      <el-icon class="is-loading"><Loading /></el-icon>
      <p>正在获取检查结果...</p>
    </div>
    
    <div v-else-if="error" class="error-container">
      <el-icon color="red"><CircleClose /></el-icon>
      <p>{{ error }}</p>
      <el-button @click="fetchTask" :size="props.isCompact ? 'small' : 'default'">重试</el-button>
    </div>
    
    <div v-else class="results-content">
      <!-- 统计面板 -->
      <el-row :gutter="props.isCompact ? 15 : 20" class="stats-panel">
        <el-col :span="12">
          <el-statistic title="总问题数" :value="filteredStats.total" />
        </el-col>
        <el-col :span="12">
          <el-statistic title="错误" :value="filteredStats.errors">
            <template #prefix><el-icon color="red"><CircleClose /></el-icon></template>
          </el-statistic>
        </el-col>
        <el-col :span="12">
          <el-statistic title="警告" :value="filteredStats.warnings">
            <template #prefix><el-icon color="orange"><Warning /></el-icon></template>
          </el-statistic>
        </el-col>
        <el-col :span="12">
          <el-statistic title="建议" :value="filteredStats.suggestions">
            <template #prefix><el-icon color="blue"><InfoFilled /></el-icon></template>
          </el-statistic>
        </el-col>
      </el-row>

      <!-- 过滤和操作栏 -->
      <div class="filter-bar">
        <div class="filter-controls">
          <el-select v-model="selectedCategory" placeholder="选择检查类型" clearable :style="{ width: props.isCompact ? '180px' : '200px' }">
            <el-option label="全部类型" value="" />
            <el-option 
              v-for="result in task?.results || []" 
              :key="result.checkId" 
              :label="result.name" 
              :value="result.checkId"
            />
          </el-select>
          
          <el-select v-model="selectedIssueType" placeholder="选择问题类型" clearable :style="{ width: props.isCompact ? '130px' : '150px' }">
            <el-option label="全部问题" value="" />
            <el-option label="错误" value="error" />
            <el-option label="警告" value="warning" />
            <el-option label="建议" value="suggestion" />
          </el-select>
        </div>
        
        <div class="action-controls">
          <el-button @click="expandAll" type="primary" plain :size="props.isCompact ? 'small' : 'small'">
            全部展开
          </el-button>
          <el-button @click="collapseAll" type="primary" plain :size="props.isCompact ? 'small' : 'small'">
            全部折叠
          </el-button>
        </div>
      </div>

      <!-- 结果列表 -->
      <div class="results-list-container">
        <el-collapse v-model="activeCollapse" class="results-collapse" accordion>
          <el-collapse-item 
            v-for="result in filteredResults" 
            :key="result.checkId" 
            :name="result.checkId"
          >
            <template #title>
              <span class="collapse-title">{{ result.name }}</span>
              <el-badge :value="result.issueCount" :type="result.issueCount > 0 ? 'danger' : 'success'" class="collapse-badge" />
            </template>
            
            <div v-if="result.issues.length === 0" class="no-issues">
              🎉 未发现任何问题，做得很好！
            </div>
            
            <div v-else class="issue-list">
              <div v-for="issue in result.issues" :key="issue.id" class="issue-item">
                <div class="issue-header">
                  <el-tag :type="issue.type === 'error' ? 'danger' : issue.type === 'warning' ? 'warning' : 'info'" effect="dark" round>
                    {{ issue.location }}
                  </el-tag>
                  <span class="issue-type-label">{{ getIssueTypeLabel(issue.type) }}</span>
                </div>
                
                <p class="explanation">{{ issue.explanation }}</p>
                
                <div class="diff-container">
                  <div class="diff-header">
                    <div class="diff-tab">
                      <span class="diff-tab-label">原文</span>
                      <span class="diff-tab-count">{{ issue.original.length }}字符</span>
                    </div>
                    <div class="diff-tab">
                      <span class="diff-tab-label">修改建议</span>
                      <span class="diff-tab-count">{{ issue.corrected.length }}字符</span>
                    </div>
                  </div>
                  <div class="diff-content" v-html="createDiffHtml(issue.original, issue.corrected)"></div>
                </div>
                
                <div class="issue-actions">
                  <el-button type="primary" plain :size="props.isCompact ? 'small' : 'small'">采纳建议</el-button>
                  <el-button 
                    :size="props.isCompact ? 'small' : 'small'"
                    @click="handleIgnoreIssue(result.checkId, issue.id)"
                  >忽略</el-button>
                </div>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>

      <!-- 底部操作 -->
      <div class="footer-actions">
        <el-button @click="$emit('re-check')" :size="props.isCompact ? 'default' : 'default'">重新检查</el-button>
        <el-button type="primary" icon="Download" :size="props.isCompact ? 'default' : 'default'">导出报告</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getQualityCheckTask } from '@/api/qualityCheck';
import type { QualityCheckTask } from '@/types/qualityCheck';
import { Loading, CircleClose, Warning, InfoFilled } from '@element-plus/icons-vue';
import * as Diff from 'diff';

const props = withDefaults(defineProps<{
  taskId: string | null;
  isCompact?: boolean;
}>(), {
  isCompact: false
});

const emit = defineEmits(['re-check', 'check-completed']);

const task = ref<QualityCheckTask | null>(null);
const isLoading = ref(true);
const error = ref<string | null>(null);
const activeCollapse = ref<string[]>([]);
const selectedCategory = ref<string>('');
const selectedIssueType = ref<string>('');

onMounted(() => {
  if (props.taskId) {
    fetchTask();
  }
});

const fetchTask = async () => {
  if (!props.taskId) return;
  
  try {
    isLoading.value = true;
    error.value = null;
    const data = await getQualityCheckTask(props.taskId);
    task.value = data;
  } catch (err) {
    error.value = '获取检查结果失败';
    ElMessage.error('获取检查结果失败');
  } finally {
    isLoading.value = false;
  }
};

// 过滤后的结果
const filteredResults = computed(() => {
  if (!task.value) return [];
  
  return task.value.results.filter(result => {
    // 按检查类型过滤
    if (selectedCategory.value && result.checkId !== selectedCategory.value) {
      return false;
    }
    
    // 按问题类型过滤
    if (selectedIssueType.value) {
      const hasMatchingIssues = result.issues.some(issue => issue.type === selectedIssueType.value);
      if (!hasMatchingIssues) {
        return false;
      }
    }
    
    return true;
  }).map(result => {
    // 如果按问题类型过滤，需要过滤issues
    if (selectedIssueType.value) {
      return {
        ...result,
        issues: result.issues.filter(issue => issue.type === selectedIssueType.value),
        issueCount: result.issues.filter(issue => issue.type === selectedIssueType.value).length
      };
    }
    return result;
  });
});

// 过滤后的统计
const filteredStats = computed(() => {
  if (!filteredResults.value.length) return { total: 0, errors: 0, warnings: 0, suggestions: 0 };
  
  let errors = 0;
  let warnings = 0;
  let suggestions = 0;
  
  filteredResults.value.forEach(result => {
    result.issues.forEach(issue => {
      if (issue.type === 'error') errors++;
      if (issue.type === 'warning') warnings++;
      if (issue.type === 'suggestion') suggestions++;
    });
  });
  
  return {
    total: errors + warnings + suggestions,
    errors,
    warnings,
    suggestions
  };
});

const expandAll = () => {
  if (!task.value) return;
  activeCollapse.value = task.value.results.map(r => r.checkId);
};

const collapseAll = () => {
  activeCollapse.value = [];
};

const getIssueTypeLabel = (type: string) => {
  switch (type) {
    case 'error': return '错误';
    case 'warning': return '警告';
    case 'suggestion': return '建议';
    default: return '未知';
  }
};

const createDiffHtml = (original: string, corrected: string) => {
  const diff = Diff.diffChars(original, corrected);
  let html = '';
  diff.forEach(part => {
    const color = part.added ? '#d4edda' : part.removed ? '#f8d7da' : 'transparent';
    const textColor = part.added ? '#155724' : part.removed ? '#721c24' : 'inherit';
    const textDecoration = part.removed ? 'line-through' : 'none';
    const tag = part.added ? 'ins' : part.removed ? 'del' : 'span';
    html += `<${tag} style="background-color: ${color}; color: ${textColor}; text-decoration: ${textDecoration}; border-radius: 3px; padding: 1px 3px;">${part.value}</${tag}>`;
  });
  return html;
};

const handleIgnoreIssue = (checkId: string, issueId: string) => {
  if (!task.value) return;
  
  // 找到对应的检查结果
  const result = task.value.results.find(r => r.checkId === checkId);
  if (result) {
    // 移除指定的问题
    result.issues = result.issues.filter(i => i.id !== issueId);
    // 更新问题计数
    result.issueCount = result.issues.length;
    
    ElMessage.success('已忽略该问题');
  }
};
</script>

<style scoped>
.check-results {
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

.compact-mode .loading-container,
.compact-mode .error-container {
  padding: 30px;
  gap: 15px;
}

.loading-container .el-icon {
  font-size: 48px;
  color: var(--el-color-primary);
}

.compact-mode .loading-container .el-icon {
  font-size: 36px;
}

.error-container .el-icon {
  font-size: 48px;
}

.compact-mode .error-container .el-icon {
  font-size: 36px;
}

.results-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.stats-panel {
  background-color: #f7f8fa;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  text-align: center;
  flex-shrink: 0;
}

.compact-mode .stats-panel {
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 15px;
}

.el-col {
  margin-bottom: 10px;
}

.compact-mode .el-col {
  margin-bottom: 8px;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #fafafa;
  border-radius: 8px;
  flex-shrink: 0;
}

.compact-mode .filter-bar {
  margin-bottom: 15px;
  padding: 12px;
  border-radius: 6px;
}

.filter-controls {
  display: flex;
  gap: 15px;
}

.compact-mode .filter-controls {
  gap: 12px;
}

.action-controls {
  display: flex;
  gap: 10px;
}

.compact-mode .action-controls {
  gap: 8px;
}

.results-list-container {
  flex: 1;
  overflow-y: auto;
  padding-right: 10px;
}

.compact-mode .results-list-container {
  padding-right: 8px;
}

.results-collapse {
  border: none;
}

.el-collapse-item {
  margin-bottom: 15px;
}

.compact-mode .el-collapse-item {
  margin-bottom: 12px;
}

:deep(.el-collapse-item__header) {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 0 20px;
  background-color: #fff;
  transition: all 0.2s ease;
}

.compact-mode :deep(.el-collapse-item__header) {
  border-radius: 6px;
  padding: 0 15px;
}

:deep(.el-collapse-item__header.is-active) {
  border-bottom-left-radius: 0;
  border-bottom-right-radius: 0;
  border-color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
}

:deep(.el-collapse-item__wrap) {
  border: 1px solid #e4e7ed;
  border-top: none;
  border-bottom-left-radius: 8px;
  border-bottom-right-radius: 8px;
}

.compact-mode :deep(.el-collapse-item__wrap) {
  border-bottom-left-radius: 6px;
  border-bottom-right-radius: 6px;
}

:deep(.el-collapse-item__content) {
  padding: 20px;
}

.compact-mode :deep(.el-collapse-item__content) {
  padding: 15px;
}

.collapse-title {
  font-weight: 500;
  font-size: 16px;
  color: #303133;
}

.compact-mode .collapse-title {
  font-size: 14px;
}

.collapse-badge {
  margin-left: 15px;
}

.compact-mode .collapse-badge {
  margin-left: 12px;
}

.no-issues {
  color: #67c23a;
  padding: 20px;
  text-align: center;
  font-size: 16px;
}

.compact-mode .no-issues {
  padding: 15px;
  font-size: 14px;
}

.issue-list {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.compact-mode .issue-list {
  gap: 20px;
}

.issue-item {
  border: 1px solid #f0f2f5;
  border-radius: 8px;
  padding: 20px;
  background-color: #fcfcfc;
}

.compact-mode .issue-item {
  border-radius: 6px;
  padding: 15px;
}

.issue-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.compact-mode .issue-header {
  gap: 8px;
  margin-bottom: 12px;
}

.issue-type-label {
  font-size: 12px;
  color: #909399;
  background-color: #f5f5f5;
  padding: 2px 8px;
  border-radius: 10px;
}

.compact-mode .issue-type-label {
  font-size: 11px;
  padding: 1px 6px;
}

.explanation {
  color: #606266;
  font-size: 14px;
  margin-bottom: 15px;
  line-height: 1.6;
}

.compact-mode .explanation {
  font-size: 13px;
  margin-bottom: 12px;
  line-height: 1.5;
}

.diff-container {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 15px;
}

.compact-mode .diff-container {
  border-radius: 4px;
  margin-bottom: 12px;
}

.diff-header {
  display: flex;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.diff-tab {
  flex: 1;
  padding: 12px 15px;
  text-align: center;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.compact-mode .diff-tab {
  padding: 8px 12px;
  gap: 2px;
}

.diff-tab:last-child {
  border-right: none;
}

.diff-tab-label {
  font-weight: 500;
  font-size: 14px;
  color: #303133;
}

.compact-mode .diff-tab-label {
  font-size: 13px;
}

.diff-tab-count {
  font-size: 12px;
  color: #909399;
}

.compact-mode .diff-tab-count {
  font-size: 11px;
}

.diff-content {
  padding: 15px;
  font-family: 'Courier New', Courier, monospace;
  line-height: 1.6;
  font-size: 14px;
  background-color: #fafafa;
  min-height: 60px;
}

.compact-mode .diff-content {
  padding: 12px;
  font-size: 13px;
  line-height: 1.5;
  min-height: 50px;
}

.issue-actions {
  margin-top: 20px;
  text-align: right;
}

.compact-mode .issue-actions {
  margin-top: 15px;
}

.footer-actions {
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
  flex-shrink: 0;
}

.compact-mode .footer-actions {
  margin-top: 15px;
  padding-top: 15px;
}

/* 滚动条样式 */
.results-list-container::-webkit-scrollbar {
  width: 6px;
}

.results-list-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.results-list-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.results-list-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
