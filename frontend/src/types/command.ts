// 基础命令定义
export interface BaseCommand {
  id: string;
  name: string;
  description?: string;
  icon?: string;
  hasSubCommands: boolean;
}

// 命令参数定义
export interface CommandParam {
  name: string;
  description?: string;
  required?: boolean;
  defaultValue?: any;
  type?: string;
}

// 命令执行上下文
export interface CommandContext {
  userId: string;
  projectId?: string;
  documentId?: string;
  input?: string;
  selectedText?: string;
  [key: string]: any;
}

// 命令执行结果
export interface CommandResult {
  success: boolean;
  message?: string;
  data?: any;
}

// 子命令定义
export interface SubCommand extends BaseCommand {
  parentId: string;
  template?: string;
  params?: CommandParam[];
  execute?: (context: CommandContext) => Promise<CommandResult>;
}

// 命令适配器接口
export interface CommandAdapter {
  adaptRequest(command: BaseCommand, context: CommandContext): any;
  adaptResponse(response: any): SubCommand[];
}

// 命令服务接口
export interface CommandService {
  getSubCommands(command: BaseCommand, context: CommandContext): Promise<SubCommand[]>;
  executeCommand(command: SubCommand, context: CommandContext): Promise<CommandResult>;
} 