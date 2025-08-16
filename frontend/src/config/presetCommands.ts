import type { Command, SubCommand } from '@/types/chat'
import { v4 as uuidv4 } from 'uuid'

/**
 * 预设命令列表
 * 
 * 这些命令会在前端进行配置，不依赖后端API
 * 包含5个一级命令和部分二级命令
 */
export const presetCommands: Command[] = [
  // 内容扩写命令
  {
    id: 'expand',
    name: '内容扩写',
    icon: 'Expand',
    description: '将简短的内容扩展为更详细的文档',
    prompt: '请将以下内容进行扩展，丰富其细节和深度，保持原文的主旨和风格，但使其更加详尽和专业：\n\n{selectedText}',
    createdAt: Date.now(),
    updatedAt: Date.now(),
    isSystem: true,
    hasSubCommands: false
  },
  
  // 内容缩写命令
  {
    id: 'condense',
    name: '内容缩写',
    icon: 'Fold',
    description: '将冗长的内容精简为简洁的摘要',
    prompt: '请将以下内容进行精简，保留核心信息和关键点，但使表达更加简洁明了，减少冗余：\n\n{selectedText}',
    createdAt: Date.now(),
    updatedAt: Date.now(),
    isSystem: true,
    hasSubCommands: false
  },
  
  // 内容续写命令
  {
    id: 'continue',
    name: '内容续写',
    icon: 'Right',
    description: '根据已有内容继续编写后续段落',
    prompt: '请根据以下文本内容，以一致的风格、语调和逻辑，续写后续内容。续写内容应当自然衔接，保持连贯性和一致性：\n\n{selectedText}',
    createdAt: Date.now(),
    updatedAt: Date.now(),
    isSystem: true,
    hasSubCommands: false
  },
  
  // 内容重写命令
  {
    id: 'rewrite',
    name: '内容重写',
    icon: 'Refresh',
    description: '以不同的风格或角度重写现有内容',
    prompt: '请重写以下内容，保持相同的核心信息和要点，但改进其表达方式、结构和清晰度：\n\n{selectedText}',
    createdAt: Date.now(),
    updatedAt: Date.now(),
    isSystem: true,
    hasSubCommands: false
  },
  
  // 复杂表单命令
  {
    id: 'complex-form',
    name: '复杂表单',
    icon: 'Grid',
    description: '生成企业文档中的各类复杂表格',
    prompt: '请为我生成一个企业文档表格。请选择具体的表格类型（如达标表、偏离表等）：',
    createdAt: Date.now(),
    updatedAt: Date.now(),
    isSystem: true,
    hasSubCommands: true,
    subCommands: [
      {
        id: 'standard-form',
        name: '达标表',
        description: '生成用于评估达标情况的表格',
        icon: 'Check',
        template: `请根据以下要求生成一个标准达标评估表格：

## 表格要求
- 表格应包含评估指标、目标值、实际值、达标状态和备注等列
- 使用Markdown格式，确保表格布局整齐
- 包含评估总结

## 上下文信息
{selectedText}

## 表格用途
该表格将用于企业内部评估项目/任务的达标情况，帮助管理层直观了解绩效表现。

请生成一个专业、清晰的达标评估表格，并根据提供的上下文确定合适的评估指标。`
      },
      {
        id: 'deviation-form',
        name: '偏离表',
        description: '生成用于分析偏离原因与程度的表格',
        icon: 'Warning',
        template: `请根据以下要求生成一个偏离分析表格：

## 表格要求
- 表格应包含指标名称、计划值、实际值、偏离值、偏离率、偏离原因分析和改进措施等列
- 使用Markdown格式，确保表格布局整齐
- 为重大偏离项提供详细的原因分析

## 上下文信息
{selectedText}

## 表格用途
该表格将用于企业项目管理中识别和分析与计划的偏差，帮助团队理解偏离原因并制定改进措施。

请生成一个专业、详细的偏离分析表格，并根据提供的上下文合理设计表格内容。`
      }
    ]
  }
];

/**
 * 获取所有预设命令
 * 
 * @returns 命令列表
 */
export function getPresetCommands(): Command[] {
  return presetCommands
}

/**
 * 获取预设子命令
 * 
 * @param commandId 父命令ID
 * @returns 子命令列表
 */
export function getPresetSubCommands(commandId: string): SubCommand[] {
  const command = presetCommands.find(cmd => cmd.id === commandId);
  return command?.subCommands || [];
} 