import { get, post } from './request';
import type { CheckOptions, QualityCheckTask } from '@/types/qualityCheck';

/**
 * 获取所有可用的质量检查项
 * @returns Promise<CheckOptions>
 */
export function getCheckOptions(): Promise<CheckOptions> {
  return get<CheckOptions>('/api/quality-checks/options');
}

/**
 * 启动一个新的质量检查任务
 * @param checkIds - 选中的检查项ID数组
 * @returns Promise<{ taskId: string }>
 */
export function startQualityCheck(checkIds: string[]): Promise<{ taskId: string }> {
  return post('/api/quality-checks/start', { checkIds });
}

/**
 * 获取指定任务的实时状态和结果
 * @param taskId - 任务ID
 * @returns Promise<QualityCheckTask>
 */
export function getQualityCheckTask(taskId: string): Promise<QualityCheckTask> {
  return get<QualityCheckTask>(`/api/quality-checks/task?taskId=${taskId}`);
}
