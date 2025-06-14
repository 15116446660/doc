import type { BaseCommand, CommandContext, CommandAdapter, SubCommand } from '@/types/command';

// 文档命令适配器
export class DocumentCommandAdapter implements CommandAdapter {
  adaptRequest(command: BaseCommand, context: CommandContext) {
    return {
      commandId: command.id,
      documentId: context.documentId,
      projectId: context.projectId,
      userId: context.userId,
      selectedText: context.selectedText
    };
  }

  adaptResponse(response: any): SubCommand[] {
    if (!response?.data) return [];
    
    return response.data.map((item: any) => ({
      id: item.id,
      parentId: item.commandId,
      name: item.name,
      description: item.description,
      icon: item.icon,
      hasSubCommands: false,
      template: item.template,
      params: item.params
    }));
  }
}

// 项目命令适配器
export class ProjectCommandAdapter implements CommandAdapter {
  adaptRequest(command: BaseCommand, context: CommandContext) {
    return {
      command: command.id,
      project: context.projectId,
      user: context.userId
    };
  }

  adaptResponse(response: any): SubCommand[] {
    if (!response?.commands) return [];
    
    return response.commands.map((cmd: any) => ({
      id: cmd.commandId,
      parentId: cmd.parentCommand,
      name: cmd.commandName,
      description: cmd.commandDesc,
      icon: cmd.iconName,
      hasSubCommands: false,
      template: cmd.promptTemplate,
      params: cmd.parameters?.map((p: any) => ({
        name: p.name,
        type: p.dataType,
        description: p.desc,
        required: p.isRequired,
        defaultValue: p.default
      }))
    }));
  }
}

// AI助手命令适配器
export class AIAssistantCommandAdapter implements CommandAdapter {
  adaptRequest(command: BaseCommand, context: CommandContext) {
    return {
      type: command.id,
      context: {
        userId: context.userId,
        input: context.input
      }
    };
  }

  adaptResponse(response: any): SubCommand[] {
    if (!response?.suggestions) return [];
    
    return response.suggestions.map((sug: any) => ({
      id: sug.id,
      parentId: sug.type,
      name: sug.title,
      description: sug.description,
      icon: sug.icon,
      hasSubCommands: false,
      template: sug.prompt,
      params: sug.parameters
    }));
  }
}

// 系统命令适配器
export class SystemCommandAdapter implements CommandAdapter {
  adaptRequest(command: BaseCommand, context: CommandContext) {
    return {
      action: command.id,
      userId: context.userId
    };
  }

  adaptResponse(response: any): SubCommand[] {
    if (!response?.actions) return [];
    
    return response.actions.map((action: any) => ({
      id: action.id,
      parentId: action.parentAction,
      name: action.name,
      description: action.description,
      icon: action.icon,
      hasSubCommands: false,
      template: action.template
    }));
  }
}

// 适配器工厂
export class CommandAdapterFactory {
  private static adapters = new Map<string, CommandAdapter>([
    ['document', new DocumentCommandAdapter()],
    ['project', new ProjectCommandAdapter()],
    ['assist', new AIAssistantCommandAdapter()],
    ['system', new SystemCommandAdapter()]
  ]);

  static getAdapter(commandType: string): CommandAdapter {
    const adapter = this.adapters.get(commandType);
    if (!adapter) {
      throw new Error(`No adapter found for command type: ${commandType}`);
    }
    return adapter;
  }

  static registerAdapter(commandType: string, adapter: CommandAdapter) {
    this.adapters.set(commandType, adapter);
  }
} 