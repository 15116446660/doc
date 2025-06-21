<template>
  <div class="check-results">
    <div v-if="isLoading" class="loading-container">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <p>正在加载检查结果...</p>
    </div>

    <div v-else-if="error" class="error-container">
       <el-icon :size="40"><CircleClose /></el-icon>
      <p>{{ error }}</p>
      <el-button @click="fetchResults" type="primary">重试</el-button>
    </div>
    
    <div v-else>
      <el-row :gutter="20" class="stats-panel">
        <el-col :span="6">
          <el-statistic title="总问题数" :value="stats.total" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="错误" :value="stats.errors">
             <template #prefix><el-icon color="red"><CircleClose /></el-icon></template>
          </el-statistic>
        </el-col>
        <el-col :span="6">
          <el-statistic title="警告" :value="stats.warnings">
            <template #prefix><el-icon color="orange"><Warning /></el-icon></template>
          </el-statistic>
        </el-col>
        <el-col :span="6">
          <el-statistic title="建议" :value="stats.suggestions">
            <template #prefix><el-icon color="blue"><InfoFilled /></el-icon></template>
          </el-statistic>
        </el-col>
      </el-row>

      <el-collapse v-model="activeCollapse" class="results-collapse">
        <el-collapse-item v-for="result in task.results" :key="result.checkId" :name="result.checkId">
          <template #title>
            <span class="collapse-title">{{ result.name }}</span>
            <el-badge :value="result.issueCount" :type="result.issueCount > 0 ? 'danger' : 'success'" />
          </template>
          <div v-if="result.issues.length === 0" class="no-issues">
            未发现问题
          </div>
          <div v-else class="issue-list">
            <div v-for="issue in result.issues" :key="issue.id" class="issue-item">
              <el-tag :type="issue.type === 'error' ? 'danger' : issue.type === 'warning' ? 'warning' : 'info'" effect="light" round>
                {{ issue.location }}
              </el-tag>
              <p class="explanation">{{ issue.explanation }}</p>
              <div class="diff-container" v-html="createDiffHtml(issue.original, issue.corrected)"></div>
              <div class="issue-actions">
                <el-button type="primary" plain size="small">采纳建议</el-button>
                <el-button size="small">忽略</el-button>
              </div>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
       <div class="footer-actions">
        <el-button @click="$emit('re-check')">重新检查</el-button>
        <el-button type="primary">导出报告</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { getQualityCheckTask } from '@/api/qualityCheck';
import type { QualityCheckTask, IssueDetail } from '@/types/qualityCheck';
import { Loading, CircleClose, Warning, InfoFilled } from '@element-plus/icons-vue';
import * as Diff from 'diff';

const props = defineProps({
  taskId: { type: String, required: true },
});

defineEmits(['re-check']);

const isLoading = ref(true);
const error = ref<string | null>(null);
const task = ref<QualityCheckTask>({} as QualityCheckTask);
const activeCollapse = ref<string[]>([]);

const stats = computed(() => {
  if (!task.value.results) return { total: 0, errors: 0, warnings: 0, suggestions: 0 };
  let errors = 0, warnings = 0, suggestions = 0;
  task.value.results.forEach(r => {
    r.issues.forEach(i => {
      if (i.type === 'error') errors++;
      else if (i.type === 'warning') warnings++;
      else suggestions++;
    });
  });
  return { total: errors + warnings + suggestions, errors, warnings, suggestions };
});

const fetchResults = async () => {
  isLoading.value = true;
  error.value = null;
  try {
    const data = await getQualityCheckTask(props.taskId);
    if (data.status !== 'finished') {
       // 如果任务没完成，可能需要一个等待或轮询机制，但这里我们简化处理
       console.warn("Task is not finished yet, polling might be needed.");
    }
    task.value = data;
    // 默认展开所有有问题结果
    activeCollapse.value = data.results.filter(r => r.issueCount > 0).map(r => r.checkId);
  } catch (e) {
    error.value = '获取检查结果失败';
    ElMessage.error(error.value);
  } finally {
    isLoading.value = false;
  }
};

onMounted(fetchResults);

const createDiffHtml = (original: string, corrected: string) => {
  const diff = Diff.diffChars(original, corrected);
  let html = '';
  diff.forEach(part => {
    const color = part.added ? 'green' : part.removed ? 'red' : 'grey';
    const textDecoration = part.removed ? 'line-through' : 'none';
    const tag = part.added ? 'ins' : part.removed ? 'del' : 'span';
    html += `<${tag} style="color: ${color}; text-decoration: ${textDecoration}; background-color: ${color === 'grey' ? 'transparent' : (color + '20')};">${part.value}</${tag}>`;
  });
  return html;
};
</script>

<style scoped>
.loading-container, .error-container { text-align: center; padding: 40px; }
.stats-panel { background-color: #fafafa; padding: 20px; border-radius: 8px; margin-bottom: 20px; }
.results-collapse { border-top: none; }
.collapse-title { font-weight: 500; margin-right: 10px; }
.no-issues { color: #909399; padding: 20px; text-align: center; }
.issue-list { display: flex; flex-direction: column; gap: 20px; }
.issue-item { border: 1px solid #e4e7ed; border-radius: 4px; padding: 15px; }
.issue-item .el-tag { margin-bottom: 10px; }
.explanation { color: #606266; font-size: 14px; margin-bottom: 15px; }
.diff-container { background-color: #fcfcfc; padding: 10px; border-radius: 4px; font-family: monospace; line-height: 1.5; }
.issue-actions { margin-top: 15px; text-align: right; }
.footer-actions { text-align: center; margin-top: 30px; }
</style>
