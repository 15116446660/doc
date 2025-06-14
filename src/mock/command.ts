import { v4 as uuidv4 } from 'uuid'
import type { MockMethod } from 'vite-plugin-mock'
import type { Command } from '@/types/chat'
import type { BaseCommand, SubCommand } from '@/types/command'

// 模拟的私有命令数据
const mockPrivateCommands: Command[] = [
  {
    id: 'sys-1',
    name: '总结文档',
    icon: 'Document',
    description: '总结文档的主要内容',
    prompt: '请总结以下文档的主要内容，提取关键信息，并以简洁的方式呈现：\n\n{input}',
    category: '文档处理',
    createdAt: Date.now() - 86400000 * 5,
    updatedAt: Date.now() - 86400000 * 5,
    isSystem: true,
    shareType: 'private'
  },
  {
    id: 'sys-2',
    name: '代码解释',
    icon: 'Edit',
    description: '解释代码的功能和逻辑',
    prompt: '请解释以下代码的功能、逻辑和实现细节：\n\n```\n{input}\n```',
    category: '编程',
    createdAt: Date.now() - 86400000 * 4,
    updatedAt: Date.now() - 86400000 * 4,
    isSystem: true,
    shareType: 'private'
  },
  {
    id: 'sys-3',
    name: '翻译助手',
    icon: 'Operation',
    description: '将文本翻译成指定语言',
    prompt: '请将以下文本翻译成{target_language}：\n\n{input}',
    category: '翻译',
    createdAt: Date.now() - 86400000 * 3,
    updatedAt: Date.now() - 86400000 * 3,
    isSystem: true,
    shareType: 'private',
    parameters: [
      {
        name: 'target_language',
        description: '目标语言',
        required: true,
        defaultValue: '英语',
        type: 'string'
      }
    ]
  },
  {
    id: 'sys-4',
    name: '写作助手',
    icon: 'Edit',
    description: '根据主题生成文章',
    prompt: '请根据以下主题，生成一篇{style}风格的文章，字数约{word_count}字：\n\n{input}',
    category: '写作',
    createdAt: Date.now() - 86400000 * 2,
    updatedAt: Date.now() - 86400000 * 2,
    isSystem: true,
    shareType: 'private',
    parameters: [
      {
        name: 'style',
        description: '写作风格',
        required: true,
        defaultValue: '正式',
        type: 'string'
      },
      {
        name: 'word_count',
        description: '目标字数',
        required: true,
        defaultValue: '500',
        type: 'number'
      }
    ]
  }
];

// 模拟的共享命令数据
const mockSharedCommands: Command[] = [
  {
    id: 'shared-1',
    name: '需求分析',
    icon: 'Operation',
    description: '分析产品需求并提供建议',
    prompt: '请分析以下产品需求，指出潜在问题，并提供改进建议：\n\n{input}',
    category: '产品',
    createdAt: Date.now() - 86400000 * 3,
    updatedAt: Date.now() - 86400000 * 3,
    isSystem: false,
    shareType: 'shared',
    creator: '张三'
  },
  {
    id: 'shared-2',
    name: '会议纪要',
    icon: 'List',
    description: '根据会议内容生成会议纪要',
    prompt: '请根据以下会议内容，生成一份结构化的会议纪要，包括：\n1. 会议主题\n2. 讨论要点\n3. 决策事项\n4. 行动计划\n\n会议内容：\n{input}',
    category: '办公',
    createdAt: Date.now() - 86400000 * 2,
    updatedAt: Date.now() - 86400000 * 2,
    isSystem: false,
    shareType: 'shared',
    creator: '李四'
  }
];

// 存储用户创建的命令
const userCommands: Record<string, Command[]> = {};

// 模拟的子命令数据
const mockSubCommands: Record<string, SubCommand[]> = {
  'document': [
    {
      id: 'doc-summary',
      parentId: 'document',
      name: '生成摘要',
      description: '为当前文档生成摘要内容',
      icon: 'Document',
      template: '请为以下文档生成一份摘要：\n{documentContent}',
      hasSubCommands: false
    },
    {
      id: 'doc-outline',
      parentId: 'document',
      name: '提取大纲',
      description: '提取文档的章节结构',
      icon: 'Menu',
      template: '请为以下文档提取大纲结构：\n{documentContent}',
      hasSubCommands: false
    },
    {
      id: 'doc-format',
      parentId: 'document',
      name: '格式转换',
      description: '转换文档格式',
      icon: 'Document',
      template: '请将以下{source_format}格式的内容转换为{target_format}格式：\n{documentContent}',
      hasSubCommands: false,
      params: [
        {
          name: 'source_format',
          description: '源格式',
          required: true,
          defaultValue: 'Markdown',
          type: 'string' as const
        },
        {
          name: 'target_format',
          description: '目标格式',
          required: true,
          defaultValue: 'HTML',
          type: 'string' as const
        }
      ]
    }
  ],
  'project': [
    {
      id: 'project-members',
      parentId: 'project',
      name: '查看成员',
      description: '查看项目成员列表',
      icon: 'User',
      template: '请列出当前项目的所有成员及其角色',
      hasSubCommands: false
    },
    {
      id: 'project-progress',
      parentId: 'project',
      name: '查看进度',
      description: '查看项目进度状态',
      icon: 'Loading',
      template: '请显示当前项目的进度状态和关键里程碑',
      hasSubCommands: false
    }
  ],
  'assist': [
    {
      id: 'assist-code',
      parentId: 'assist',
      name: '编写代码',
      description: '根据需求编写代码',
      icon: 'Edit',
      template: '请根据以下需求编写{language}代码：\n\n{input}',
      hasSubCommands: false,
      params: [
        {
          name: 'language',
          description: '编程语言',
          required: true,
          defaultValue: 'JavaScript',
          type: 'string' as const
        }
      ]
    },
    {
      id: 'assist-optimize',
      parentId: 'assist',
      name: '优化文案',
      description: '优化文字内容',
      icon: 'Edit',
      template: '请优化以下文案，使其更{style}：\n\n{input}',
      hasSubCommands: false,
      params: [
        {
          name: 'style',
          description: '目标风格',
          required: true,
          defaultValue: '专业',
          type: 'string' as const
        }
      ]
    },
    {
      id: 'assist-translate',
      parentId: 'assist',
      name: '翻译内容',
      description: '将内容翻译为指定语言',
      icon: 'Document',
      template: '请将以下内容翻译为{target_language}：\n\n{input}',
      hasSubCommands: false,
      params: [
        {
          name: 'target_language',
          description: '目标语言',
          required: true,
          defaultValue: '英语',
          type: 'string' as const
        }
      ]
    }
  ],
  'system': [
    {
      id: 'system-clear',
      parentId: 'system',
      name: '清理缓存',
      description: '清理系统缓存',
      icon: 'Delete',
      template: '请清理系统缓存',
      hasSubCommands: false
    },
    {
      id: 'system-export',
      parentId: 'system',
      name: '导出设置',
      description: '导出系统设置',
      icon: 'Download',
      template: '请导出系统设置',
      hasSubCommands: false
    }
  ],
  'custom': [
    // 将私有命令转换为子命令
    ...mockPrivateCommands.map(cmd => ({
      id: cmd.id,
      parentId: 'custom',
      name: cmd.name,
      description: cmd.description,
      icon: cmd.icon,
      template: cmd.prompt,
      hasSubCommands: false,
      params: cmd.parameters?.map(param => ({
        name: param.name,
        description: param.description,
        required: param.required,
        defaultValue: param.defaultValue,
        type: 'string'
      }))
    })),
    // 将共享命令转换为子命令
    ...mockSharedCommands.map(cmd => ({
      id: cmd.id,
      parentId: 'custom',
      name: cmd.name,
      description: `${cmd.description} (由 ${cmd.creator} 分享)`,
      icon: cmd.icon,
      template: cmd.prompt,
      hasSubCommands: false,
      params: cmd.parameters?.map(param => ({
        name: param.name,
        description: param.description,
        required: param.required,
        defaultValue: param.defaultValue,
        type: 'string'
      }))
    }))
  ]
};

export default [
  // 获取私有命令
  {
    url: '/api/commands',
    method: 'get',
    response: () => {
      return {
        code: 200,
        msg: '获取成功',
        data: mockPrivateCommands
      }
    }
  },
  
  // 获取共享命令
  {
    url: '/api/commands/shared',
    method: 'get',
    response: () => {
      return {
        code: 200,
        msg: '获取成功',
        data: mockSharedCommands
      }
    }
  },
  
  // 创建命令
  {
    url: '/api/commands',
    method: 'post',
    response: ({ body }: { body: any }) => {
      const { userId, command } = body;
      
      if (!userId || !command) {
        return {
          code: 400,
          msg: '参数错误',
          data: null
        };
      }
      
      // 初始化用户的命令列表
      if (!userCommands[userId]) {
        userCommands[userId] = [];
      }
      
      // 创建新命令
      const newCommand: Command = {
        ...command,
        id: uuidv4(),
        createdAt: Date.now(),
        updatedAt: Date.now(),
        isSystem: false,
        shareType: 'private'
      };
      
      // 添加到用户的命令列表
      userCommands[userId].push(newCommand);
      
      return {
        code: 200,
        msg: '创建成功',
        data: newCommand
      };
    }
  },
  
  // 获取用户的命令列表
  {
    url: '/api/commands/user',
    method: 'get',
    response: ({ query }: { query: any }) => {
      const { userId } = query;
      
      if (!userId) {
        return {
          code: 400,
          msg: '参数错误',
          data: null
        };
      }
      
      return {
        code: 200,
        msg: '获取成功',
        data: userCommands[userId] || []
      };
    }
  },
  
  // 更新命令
  {
    url: '/api/commands/:id',
    method: 'put',
    response: ({ body, query }: { body: any; query: any }) => {
      const { userId } = query;
      const { command } = body;
      
      if (!userId || !command) {
        return {
          code: 400,
          msg: '参数错误',
          data: null
        };
      }
      
      // 查找并更新命令
      const userCommandList = userCommands[userId] || [];
      const commandIndex = userCommandList.findIndex(cmd => cmd.id === command.id);
      
      if (commandIndex === -1) {
        return {
          code: 404,
          msg: '命令不存在',
          data: null
        };
      }
      
      // 更新命令
      const updatedCommand: Command = {
        ...userCommandList[commandIndex],
        ...command,
        updatedAt: Date.now()
      };
      
      userCommandList[commandIndex] = updatedCommand;
      
      return {
        code: 200,
        msg: '更新成功',
        data: updatedCommand
      };
    }
  },
  
  // 删除命令
  {
    url: '/api/commands/:id',
    method: 'delete',
    response: ({ query }: { query: any }) => {
      const { userId, id } = query;
      
      if (!userId || !id) {
        return {
          code: 400,
          msg: '参数错误',
          data: null
        };
      }
      
      // 查找并删除命令
      const userCommandList = userCommands[userId] || [];
      const commandIndex = userCommandList.findIndex(cmd => cmd.id === id);
      
      if (commandIndex === -1) {
        return {
          code: 404,
          msg: '命令不存在',
          data: null
        };
      }
      
      // 删除命令
      userCommandList.splice(commandIndex, 1);
      
      return {
        code: 200,
        msg: '删除成功',
        data: null
      };
    }
  },
  
  // 获取子命令
  {
    url: '/api/commands/sub-commands',
    method: 'post',
    response: ({ body }: { body: any }) => {
      const { commandId, userId } = body;
      
      if (commandId === 'custom') {
        // 返回自定义命令
        const customCommands = [
          // 将私有命令转换为子命令
          ...mockPrivateCommands.map(cmd => ({
            id: cmd.id,
            parentId: 'custom',
            name: cmd.name,
            description: cmd.description || '',
            icon: cmd.icon || 'Star',
            template: cmd.prompt,
            hasSubCommands: false,
            params: cmd.parameters?.map(param => ({
              name: param.name,
              description: param.description || '无描述',
              required: true,
              defaultValue: param.defaultValue || '',
              type: param.type || 'string'
            }))
          })),
          // 将共享命令转换为子命令
          ...mockSharedCommands.map(cmd => ({
            id: cmd.id,
            parentId: 'custom',
            name: cmd.name,
            description: `${cmd.description || ''} (由 ${cmd.creator} 分享)`,
            icon: cmd.icon || 'Star',
            template: cmd.prompt,
            hasSubCommands: false,
            params: cmd.parameters?.map(param => ({
              name: param.name,
              description: param.description || '无描述',
              required: true,
              defaultValue: param.defaultValue || '',
              type: param.type || 'string'
            }))
          })),
          // 添加用户自定义命令
          ...(userCommands[userId] || []).map(cmd => ({
            id: cmd.id,
            parentId: 'custom',
            name: cmd.name,
            description: cmd.description || '',
            icon: cmd.icon || 'Star',
            template: cmd.prompt,
            hasSubCommands: false,
            params: cmd.parameters?.map(param => ({
              name: param.name,
              description: param.description || '无描述',
              required: true,
              defaultValue: param.defaultValue || '',
              type: param.type || 'string'
            }))
          }))
        ] as SubCommand[];
        
        return {
          code: 200,
          msg: '获取成功',
          data: customCommands
        };
      }
      
      if (!commandId || !mockSubCommands[commandId]) {
        return {
          code: 400,
          msg: '无效的命令ID',
          data: []
        };
      }
      
      return {
        code: 200,
        msg: '获取成功',
        data: mockSubCommands[commandId]
      };
    }
  },
  
  // 执行命令
  {
    url: '/api/commands/execute',
    method: 'post',
    response: ({ body }: { body: any }) => {
      const { commandId, context } = body;
      
      // 在实际应用中，这里应该根据commandId和context执行相应的操作
      
      return {
        code: 200,
        msg: '执行成功',
        data: {
          success: true,
          message: `命令 ${commandId} 执行成功`,
          result: `这是命令 ${commandId} 的执行结果，参数：${JSON.stringify(context)}`
        }
      }
    }
  }
] as MockMethod[]; 