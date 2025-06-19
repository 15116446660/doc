import { get } from './request'
import type { SubCommand, QuickCommand } from '@/types/chat'
import { getPresetSubCommands } from '@/config/presetCommands'

/**
 * 根据父命令ID获取子命令列表
 * @param parentCommandId 父命令ID
 */
export function getSubCommands(parentCommandId: string): Promise<SubCommand[]> {
  // 首先尝试从预设命令中获取子命令
  const presetSubCommands = getPresetSubCommands(parentCommandId);
  if (presetSubCommands.length > 0) {
    // 如果有预设子命令，则直接返回
    return Promise.resolve(presetSubCommands);
  }

  // 否则从API获取
  // 注意：根据项目规范，ID应该作为查询参数传递
  return get<SubCommand[]>(`/api/commands/sub-commands?commandId=${parentCommandId}`);
}

/**
 * 获取快捷命令列表
 */
export function getQuickCommands(): Promise<QuickCommand[]> {
  return get<QuickCommand[]>('/api/quick-commands');
} 
 
 
 
 
 
 
 