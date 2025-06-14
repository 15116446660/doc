import type { BaseCommand, CommandContext, CommandResult, SubCommand } from '@/types/command';
import { DefaultCommandService } from './service';

export class CommandManager {
  private static instance: CommandManager;
  private commandService: DefaultCommandService;
  private commands: Map<string, BaseCommand>;
  private subCommands: Map<string, SubCommand[]>;

  private constructor() {
    this.commandService = new DefaultCommandService();
    this.commands = new Map();
    this.subCommands = new Map();
    this.initializeCommands();
  }

  static getInstance(): CommandManager {
    if (!CommandManager.instance) {
      CommandManager.instance = new CommandManager();
    }
    return CommandManager.instance;
  }

  private initializeCommands() {
    // 初始化基础命令
    const baseCommands: BaseCommand[] = [
      {
        id: 'document',
        name: '文档操作',
        description: '文档相关的操作命令',
        icon: 'Document',
        hasSubCommands: true
      },
      {
        id: 'project',
        name: '项目管理',
        description: '项目相关的管理命令',
        icon: 'Folder',
        hasSubCommands: true
      },
      {
        id: 'assist',
        name: 'AI助手',
        description: 'AI辅助功能',
        icon: 'ChatLineRound',
        hasSubCommands: true
      },
      {
        id: 'system',
        name: '系统功能',
        description: '系统相关的功能命令',
        icon: 'Setting',
        hasSubCommands: true
      },
      {
        id: 'custom',
        name: '自定义命令',
        description: '用户自定义的命令集合',
        icon: 'Star',
        hasSubCommands: true
      }
    ];

    baseCommands.forEach(command => {
      this.commands.set(command.id, command);
    });
  }

  // 获取所有基础命令
  getCommands(): BaseCommand[] {
    return Array.from(this.commands.values());
  }

  // 获取指定命令的子命令
  async getSubCommands(commandId: string, context: CommandContext): Promise<SubCommand[]> {
    // 检查缓存
    const cachedSubCommands = this.subCommands.get(commandId);
    if (cachedSubCommands) {
      return cachedSubCommands;
    }

    // 获取命令
    const command = this.commands.get(commandId);
    if (!command) {
      throw new Error(`Command not found: ${commandId}`);
    }

    // 获取子命令
    const subCommands = await this.commandService.getSubCommands(command, context);
    
    // 缓存子命令
    this.subCommands.set(commandId, subCommands);
    
    return subCommands;
  }

  // 执行命令
  async executeCommand(commandId: string, context: CommandContext): Promise<CommandResult> {
    // 查找子命令
    const subCommands = await this.getSubCommands(context.parentCommandId!, context);
    const command = subCommands.find(cmd => cmd.id === commandId);
    
    if (!command) {
      throw new Error(`Sub command not found: ${commandId}`);
    }

    return this.commandService.executeCommand(command, context);
  }

  // 清除子命令缓存
  clearSubCommandCache(commandId?: string) {
    if (commandId) {
      this.subCommands.delete(commandId);
    } else {
      this.subCommands.clear();
    }
  }

  // 注册新命令
  registerCommand(command: BaseCommand) {
    this.commands.set(command.id, command);
  }

  // 注册子命令
  registerSubCommands(commandId: string, subCommands: SubCommand[]) {
    this.subCommands.set(commandId, subCommands);
  }
} 