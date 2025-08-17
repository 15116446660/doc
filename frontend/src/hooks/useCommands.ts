import { ref, computed } from 'vue';
import type { BaseCommand, SubCommand, CommandContext, CommandResult } from '@/types/command';
import { CommandManager } from '@/services/command/manager';
import { useAuth } from './useAuth';

export function useCommands() {
  const commandManager = CommandManager.getInstance();
  const { state: authState } = useAuth();
  const commands = ref<BaseCommand[]>(commandManager.getCommands());
  const activeCommand = ref<BaseCommand | null>(null);
  const subCommands = ref<SubCommand[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);

  // 计算属性：是否有活动的命令
  const hasActiveCommand = computed(() => activeCommand.value !== null);

  // 获取子命令
  const fetchSubCommands = async (commandId: string, context: CommandContext) => {
    loading.value = true;
    error.value = null;
    
    try {
      const command = commands.value.find(cmd => cmd.id === commandId);
      if (!command) {
        throw new Error(`Command not found: ${commandId}`);
      }
      
      activeCommand.value = command;
      subCommands.value = await commandManager.getSubCommands(commandId, {
        ...context,
        userId: authState.user?.id || ''
      });
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch sub commands';
      subCommands.value = [];
    } finally {
      loading.value = false;
    }
  };

  // 执行命令
  const executeCommand = async (commandId: string, context: CommandContext): Promise<CommandResult> => {
    loading.value = true;
    error.value = null;
    
    try {
      const result = await commandManager.executeCommand(commandId, {
        ...context,
        userId: authState.user?.id || ''
      });
      return result;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to execute command';
      return {
        success: false,
        message: error.value
      };
    } finally {
      loading.value = false;
    }
  };

  // 清除活动命令
  const clearActiveCommand = () => {
    activeCommand.value = null;
    subCommands.value = [];
  };

  // 清除子命令缓存
  const clearSubCommandCache = (commandId?: string) => {
    commandManager.clearSubCommandCache(commandId);
    if (!commandId || (activeCommand.value && activeCommand.value.id === commandId)) {
      subCommands.value = [];
    }
  };

  // 注册新命令
  const registerCommand = (command: BaseCommand) => {
    commandManager.registerCommand(command);
    commands.value = commandManager.getCommands();
  };

  // 注册子命令
  const registerSubCommands = (commandId: string, newSubCommands: SubCommand[]) => {
    commandManager.registerSubCommands(commandId, newSubCommands);
    if (activeCommand.value && activeCommand.value.id === commandId) {
      subCommands.value = newSubCommands;
    }
  };

  return {
    commands,
    activeCommand,
    subCommands,
    loading,
    error,
    hasActiveCommand,
    fetchSubCommands,
    executeCommand,
    clearActiveCommand,
    clearSubCommandCache,
    registerCommand,
    registerSubCommands
  };
} 