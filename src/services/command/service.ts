import type { BaseCommand, CommandContext, CommandResult, SubCommand, CommandService } from '@/types/command';
import { CommandAdapterFactory } from './adapters';
import request from '@/utils/request';

export class DefaultCommandService implements CommandService {
  private static readonly API_ENDPOINTS = {
    SUB_COMMANDS: '/api/commands/sub-commands',
    EXECUTE: '/api/commands/execute'
  };

  async getSubCommands(command: BaseCommand, context: CommandContext): Promise<SubCommand[]> {
    try {
      // 发送请求到统一的子命令API
      const response = await request.post(DefaultCommandService.API_ENDPOINTS.SUB_COMMANDS, {
        commandId: command.id,
        userId: context.userId,
        context
      });
      
      // 检查响应状态
      if (response.code !== 200) {
        throw new Error(response.msg || '获取子命令失败');
      }
      
      return response.data || [];
    } catch (error) {
      console.error('Failed to get sub commands:', error);
      return [];
    }
  }

  async executeCommand(command: SubCommand, context: CommandContext): Promise<CommandResult> {
    try {
      // 如果命令有自定义执行函数，直接执行
      if (command.execute) {
        return await command.execute(context);
      }

      // 发送请求到统一的命令执行API
      const response = await request.post(DefaultCommandService.API_ENDPOINTS.EXECUTE, {
        commandId: command.id,
        userId: context.userId,
        context
      });
      
      // 检查响应状态
      if (response.code !== 200) {
        throw new Error(response.msg || '执行命令失败');
      }
      
      return response.data || { success: false, message: '执行结果为空' };
    } catch (error) {
      console.error('Failed to execute command:', error);
      return {
        success: false,
        message: error instanceof Error ? error.message : 'Unknown error occurred'
      };
    }
  }
} 