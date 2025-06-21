/**
 * 检查项定义
 */
export interface CheckItem {
  id: string;          // 唯一标识
  name: string;        // 检查项名称
  description: string; // 检查项描述
  category: 'routine' | 'special' | 'advanced'; // 类别
}

/**
 * 检查配置
 */
export interface CheckOptions {
  routine: CheckItem[];
  special: CheckItem[];
  advanced: CheckItem[];
}

/**
 * 检查任务状态
 */
export type CheckStatus = 'waiting' | 'checking' | 'completed' | 'failed' | 'cancelled';

/**
 * 检查进度项
 */
export interface ProgressItem {
  checkId: string;
  name: string;
  status: CheckStatus;
  progress: number; // 0-100
}

/**
 * 检查结果详情
 */
export interface IssueDetail {
  id: string;
  type: 'suggestion' | 'warning' | 'error';
  original: string;    // 原始文本
  corrected: string;   // 修正建议
  explanation: string; // 问题解释
  location: string;    // 问题位置 (e.g., "第3章 第2节 第5段")
}

/**
 * 检查结果
 */
export interface CheckResult {
  checkId: string;
  name: string;
  issueCount: number;
  issues: IssueDetail[];
}

/**
 * 质量检查任务
 */
export interface QualityCheckTask {
  taskId: string;
  status: 'selecting' | 'progressing' | 'finished';
  selectedChecks: string[];
  progress: ProgressItem[];
  results: CheckResult[];
}
