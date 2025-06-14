import type { MockMethod } from 'vite-plugin-mock'
import type { AIModel, Command, KnowledgeBase, Message } from '@/types/chat'
import { v4 as uuidv4 } from 'uuid'

// 模拟AI模型列表
const mockModels: AIModel[] = [
  {
    id: 'gpt-4',
    name: 'GPT-4',
    description: '最强大的AI模型，适合复杂任务',
    icon: 'https://cdn-icons-png.flaticon.com/512/2111/2111615.png',
    systemPrompt: '你是一个智能AI助手，乐于解答各种问题。',
    parameters: {
      temperature: 0.7,
      maxTokens: 4096,
      topP: 1,
      frequencyPenalty: 0,
      presencePenalty: 0
    },
    isDefault: true
  },
  {
    id: 'gpt-3.5-turbo',
    name: 'GPT-3.5 Turbo',
    description: '快速高效的AI模型，适合一般任务',
    icon: 'https://cdn-icons-png.flaticon.com/512/2111/2111432.png',
    systemPrompt: '你是一个智能AI助手，乐于解答各种问题。',
    parameters: {
      temperature: 0.7,
      maxTokens: 2048,
      topP: 1,
      frequencyPenalty: 0,
      presencePenalty: 0
    }
  },
  {
    id: 'claude-3',
    name: 'Claude 3',
    description: '理解能力强的AI模型，适合文本分析',
    icon: 'https://cdn-icons-png.flaticon.com/512/2111/2111795.png',
    systemPrompt: '你是Claude，一个由Anthropic开发的AI助手。',
    parameters: {
      temperature: 0.7,
      maxTokens: 4096,
      topP: 1,
      frequencyPenalty: 0,
      presencePenalty: 0
    }
  }
]

// 模拟命令列表
const mockCommands: Command[] = [
  {
    id: 'cmd-expand',
    name: '扩展内容',
    icon: 'expand',
    description: '扩展和丰富给定的内容',
    prompt: '请扩展以下内容，使其更加详细和丰富：\n\n{input}',
    category: '内容创作',
    createdAt: Date.now(),
    updatedAt: Date.now(),
    isSystem: true
  },
  {
    id: 'cmd-summarize',
    name: '内容摘要',
    icon: 'summarize',
    description: '总结长文本的要点',
    prompt: '请总结以下内容的要点：\n\n{input}',
    category: '内容分析',
    createdAt: Date.now(),
    updatedAt: Date.now(),
    isSystem: true
  },
  {
    id: 'cmd-rewrite',
    name: '重写内容',
    icon: 'edit',
    description: '以不同风格重写内容',
    prompt: '请重写以下内容，保持意思不变但使用不同的表达方式：\n\n{input}',
    category: '内容创作',
    createdAt: Date.now(),
    updatedAt: Date.now(),
    isSystem: true
  },
  {
    id: 'cmd-check',
    name: '内容检查',
    icon: 'check',
    description: '检查文本中的错误和问题',
    prompt: '请检查以下内容中的语法错误、拼写错误和表达不清的地方：\n\n{input}',
    category: '内容分析',
    createdAt: Date.now(),
    updatedAt: Date.now(),
    isSystem: true
  }
]

// 模拟知识库列表
const mockKnowledgeBases: KnowledgeBase[] = [
  {
    id: 'kb-docs',
    name: '文档知识库',
    description: '包含公司文档和产品手册',
    icon: 'document',
    apiEndpoint: '/api/knowledge/docs',
    parameters: {
      relevanceThreshold: 0.7,
      maxResults: 5
    }
  },
  {
    id: 'kb-wiki',
    name: '内部维基',
    description: '包含公司内部维基百科内容',
    icon: 'wiki',
    apiEndpoint: '/api/knowledge/wiki',
    parameters: {
      relevanceThreshold: 0.7,
      maxResults: 5
    }
  }
]

// 生成AI回复的函数
function generateAIResponse(messages: Message[], options?: any): Message {
  const lastMessage = messages[messages.length - 1]
  let responseContent = ''
  
  // 根据最后一条消息生成回复
  if (lastMessage.content.includes('你好') || lastMessage.content.includes('嗨') || lastMessage.content.includes('hi')) {
    responseContent = '你好！我是景智文档助手，有什么我可以帮助你的吗？'
  } else if (lastMessage.content.includes('功能') || lastMessage.content.includes('能做什么')) {
    responseContent = '作为景智文档助手，我可以：\n\n1. 回答关于文档的问题\n2. 帮助分析和总结文档内容\n3. 协助编写和修改文档\n4. 提供各类知识查询服务\n\n你有什么具体需要帮助的吗？'
  } else if (lastMessage.content.includes('谢谢') || lastMessage.content.includes('感谢')) {
    responseContent = '不客气！如果还有其他问题，随时可以问我。'
  } else if (lastMessage.commandId === 'cmd-expand') {
    responseContent = `我来扩展一下这个内容：\n\n${lastMessage.content}\n\n这个主题可以从以下几个方面进行深入探讨：\n\n1. 历史背景和发展脉络\n2. 核心概念和原理解析\n3. 实际应用场景和案例\n4. 未来发展趋势和挑战\n\n让我们逐一展开...`
  } else if (lastMessage.commandId === 'cmd-summarize') {
    responseContent = `以下是对内容的摘要：\n\n${lastMessage.content}\n\n核心要点：\n\n- 第一个关键点...\n- 第二个关键点...\n- 第三个关键点...\n\n总结：这段内容主要讨论了...，并强调了...的重要性。`
  } else {
    responseContent = `感谢你的提问！关于"${lastMessage.content.substring(0, 20)}..."，我的回答是：\n\n这是一个很好的问题。根据我所了解的信息，这个问题可以从多个角度来分析...\n\n首先，我们需要考虑...\n\n其次，重要的一点是...\n\n最后，不要忘记...\n\n希望这个回答对你有所帮助！如果你有更多问题，请随时提问。`
  }
  
  // 如果启用了深度思考模式
  let thinking = undefined
  if (options?.deepThinking) {
    thinking = `让我思考一下这个问题...\n\n问题分析：\n${lastMessage.content}\n\n这个问题涉及到几个关键点：\n1. ...\n2. ...\n3. ...\n\n我需要从以下角度来回答：\n- 背景信息\n- 核心概念\n- 实际应用\n- 最佳实践\n\n开始组织回答...`
  }
  
  return {
    id: uuidv4(),
    role: 'assistant',
    content: responseContent,
    timestamp: Date.now(),
    status: 'completed',
    thinking
  }
}

interface RequestParams {
  body?: any;
  query?: any;
}

const mockApi: MockMethod[] = [
  // 获取AI模型列表
  {
    url: '/api/chat/models',
    method: 'get',
    response: () => {
      return {
        code: 200,
        msg: '获取成功',
        data: mockModels
      }
    }
  },
  
  // 获取命令列表
  {
    url: '/api/chat/commands',
    method: 'get',
    response: () => {
      return {
        code: 200,
        msg: '获取成功',
        data: mockCommands
      }
    }
  },
  
  // 创建自定义命令
  {
    url: '/api/chat/commands',
    method: 'post',
    response: ({ body }: RequestParams) => {
      const newCommand: Command = {
        ...body,
        id: `cmd-${uuidv4().substring(0, 8)}`,
        createdAt: Date.now(),
        updatedAt: Date.now(),
        isSystem: false
      }
      
      mockCommands.push(newCommand)
      
      return {
        code: 200,
        msg: '创建成功',
        data: newCommand
      }
    }
  },
  
  // 更新自定义命令
  {
    url: '/api/chat/commands/:id',
    method: 'post',
    response: ({ body, query }: RequestParams) => {
      const { id } = query || {}
      const commandIndex = mockCommands.findIndex(cmd => cmd.id === id)
      
      if (commandIndex === -1) {
        return {
          code: 404,
          msg: '命令不存在',
          data: null
        }
      }
      
      mockCommands[commandIndex] = {
        ...mockCommands[commandIndex],
        ...body,
        updatedAt: Date.now()
      }
      
      return {
        code: 200,
        msg: '更新成功',
        data: mockCommands[commandIndex]
      }
    }
  },
  
  // 删除自定义命令
  {
    url: '/api/chat/commands/:id/delete',
    method: 'post',
    response: ({ query }: RequestParams) => {
      const { id } = query || {}
      const commandIndex = mockCommands.findIndex(cmd => cmd.id === id)
      
      if (commandIndex === -1) {
        return {
          code: 404,
          msg: '命令不存在',
          data: null
        }
      }
      
      if (mockCommands[commandIndex].isSystem) {
        return {
          code: 403,
          msg: '系统命令不能删除',
          data: null
        }
      }
      
      mockCommands.splice(commandIndex, 1)
      
      return {
        code: 200,
        msg: '删除成功',
        data: null
      }
    }
  },
  
  // 获取知识库列表
  {
    url: '/api/chat/knowledge-bases',
    method: 'get',
    response: () => {
      return {
        code: 200,
        msg: '获取成功',
        data: mockKnowledgeBases
      }
    }
  },
  
  // 发送消息获取AI回复
  {
    url: '/api/chat/completions',
    method: 'post',
    response: ({ body }: RequestParams) => {
      const { messages, deepThinking } = body || {}
      
      // 模拟网络延迟
      const response = generateAIResponse(messages as Message[], { deepThinking })
      
      return {
        code: 200,
        msg: '操作成功',
        data: response
      }
    }
  },
  
  // 上传文件附件
  {
    url: '/api/chat/attachments',
    method: 'post',
    response: ({ body }: RequestParams) => {
      // 模拟文件上传
      return {
        code: 200,
        msg: '上传成功',
        data: {
          id: uuidv4(),
          url: 'https://example.com/attachments/sample.pdf',
          thumbnail: body?.file?.name?.endsWith('.jpg') || body?.file?.name?.endsWith('.png') 
            ? 'https://example.com/attachments/thumbnail.jpg' 
            : undefined
        }
      }
    }
  }
]

export default mockApi 