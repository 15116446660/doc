import { get } from './request'
import type { SubCommand, QuickCommand } from '@/types/chat'

/**
 * 根据父命令ID获取子命令列表
 * @param parentCommandId 父命令ID
 */
export function getSubCommands(parentCommandId: string): Promise<SubCommand[]> {
  // 注意：根据项目规范，ID应该作为查询参数传递
  return get<SubCommand[]>(`/api/commands/sub-commands?commandId=${parentCommandId}`);
}

/**
 * 获取快捷命令列表
 */
export function getQuickCommands(): Promise<QuickCommand[]> {
  return get<QuickCommand[]>('/api/quick-commands');
} 
 
 
 
 
 
 
 