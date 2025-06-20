import type { MockMethod } from 'vite-plugin-mock'
import type { AIModel, Command, KnowledgeBase, Message, SubCommand, QuickCommand, RAGChatRequest, RAGChatResponse, NormalChatRequest, NormalChatResponse, Reference, DocumentAggregation } from '@/types/chat'
import { v4 as uuidv4 } from 'uuid'

interface RequestParams {
    body?: any;
    query?: any;
}

// 模拟AI模型列表
const mockModels: AIModel[] = [
  {
    id: 'gpt-4',
    name: 'GPT-4',
    description: '最强大的AI模型，适合复杂任务',
    provider: 'openai',
    logo: 'https://cdn-icons-png.flaticon.com/512/2111/2111615.png',
    level: 'super',
    isDefault: true,
    apiKey: '',
    maxTokens: 4096,
    temperature: 0.7,
    modelVersion: 'gpt-4'
  },
  {
    id: 'gpt-3.5-turbo',
    name: 'GPT-3.5 Turbo',
    description: '快速高效的AI模型，适合一般任务',
    provider: 'openai',
    logo: 'https://cdn-icons-png.flaticon.com/512/2111/2111432.png',
    level: 'basic',
    apiKey: '',
    maxTokens: 2048,
    temperature: 0.7,
    modelVersion: 'gpt-3.5-turbo',
    isDefault: false
  },
  {
    id: 'qwen-2.5',
    name: '通义千问 2.5',
    description: '阿里云最新一代基础大模型，性能全面升级',
    provider: 'alibaba',
    logo: '/ai-models/qwen.png',
    level: 'super',
    apiKey: '',
    maxTokens: 10240,
    temperature: 0.7,
    modelVersion: 'qwen-2.5',
    isDefault: false
  },
  {
    id: 'deepseek-v3',
    name: 'DeepSeek V3',
    description: 'DeepSeek最新一代通用大模型，能力全面增强',
    provider: 'deepseek',
    logo: '/ai-models/deepseek.png',
    level: 'super',
    apiKey: '',
    maxTokens: 16384,
    temperature: 0.7,
    modelVersion: 'deepseek-v3',
    isDefault: false
  },
  {
    id: 'claude-3',
    name: 'Claude 3',
    description: '理解能力强的AI模型，适合文本分析',
    provider: 'anthropic',
    logo: 'https://cdn-icons-png.flaticon.com/512/2111/2111795.png',
    level: 'advanced',
    apiKey: '',
    maxTokens: 4096,
    temperature: 0.7,
    modelVersion: 'claude-3',
    isDefault: false
  }
]

// 模拟命令列表
const mockCommands: Command[] = [
  { id: 'document', name: '文档', icon: 'Document', description: '处理当前文档', hasSubCommands: true, createdAt: Date.now(), updatedAt: Date.now() },
  { id: 'rewrite', name: '重写', icon: 'MagicStick', description: '重写选中的文本', prompt: '请重写以下文本：\n{selectedText}', createdAt: Date.now(), updatedAt: Date.now() },
  { id: 'summary', name: '总结', icon: 'Notebook', description: '总结选中的文本', prompt: '请总结以下文本：\n{selectedText}', createdAt: Date.now(), updatedAt: Date.now() },
  { id: 'translate', name: '翻译', icon: 'Switch', description: '将选中的文本翻译成中文', prompt: '请将以下文本翻译成中文：\n{selectedText}', createdAt: Date.now(), updatedAt: Date.now() },
]

// 模拟知识库列表
const mockKnowledgeBases: KnowledgeBase[] = [
  { id: 'kb-1', name: '产品设计规范文档', description: '包含所有产品线的设计原则和组件规范。', icon: 'Document' },
  { id: 'kb-2', name: '研发项目管理知识库', description: '覆盖项目流程、代码规范和常见问题解答。', icon: 'FolderOpened' },
  { id: 'kb-3', name: '市场与竞品分析报告', description: '最新的市场趋势和竞争对手动态分析。', icon: 'DataAnalysis' },
]

// 模拟知识库引用
const mockReference: Reference = {
  total: 1,
  chunks: [
    {
      id: "cebaea2b7221cc02",
      content: "这是一段来自知识库的引用内容",
      document_id: "2982d73c3b9511f0af9e0242ac120003",
      document_name: "示例文档.docx",
      dataset_id: "d34e94b236b911f082200242ac120003",
      positions: [1, 2, 3]
    }
  ]
};

// 模拟文档聚合
const mockDocAggs: DocumentAggregation[] = [
  {
    doc_name: "示例文档.docx",
    doc_id: "2982d73c3b9511f0af9e0242ac120003",
    count: 1
  }
];

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

// // 模拟的私有命令数据
// const mockPrivateCommands: Command[] = [
//   {
//     id: 'cmd-1',
//     name: 'summarize',
//     prompt: '请总结以下内容：',
//     description: '对提供的文本进行总结',
//     icon: 'Document',
//     isSystem: true,
//     shareType: 'private',
//     createdAt: Date.now(),
//     updatedAt: Date.now(),
//   },
//   {
//     id: 'cmd-2',
//     name: 'translate',
//     prompt: '请将以下内容翻译成中文：',
//     description: '将文本翻译成中文',
//     icon: 'Switch',
//     isSystem: true,
//     shareType: 'private',
//     createdAt: Date.now(),
//     updatedAt: Date.now(),
//   },
// ]

// 模拟的共享命令数据
const mockSharedCommands: Command[] = [
  {
    id: 'shared-cmd-1',
    name: 'code_review',
    prompt: '请审查以下代码，并提供改进建议：',
    description: '由 @张三 分享的代码审查模板',
    icon: 'Monitor',
    isSystem: false,
    shareType: 'shared',
    creator: '张三',
    createdAt: Date.now(),
    updatedAt: Date.now(),
  },
]

// 扩展mockSubCommands，为更多父命令添加子命令
const mockSubCommands: Record<string, SubCommand[]> = {
  document: [
    { id: 'doc_summary', name: '生成摘要', description: '为当前文档生成摘要内容', icon: 'DocumentText', template: '为当前文档生成一份摘要' },
    { id: 'doc_outline', name: '提取大纲', description: '提取文档的章节结构', icon: 'List', template: '为当前文档提取大纲结构' },
    { id: 'doc_qa', name: '问答', description: '根据文档内容回答问题', icon: 'Help', template: '根据文档内容回答我的问题：{input}' },
    { id: 'doc_format', name: '格式转换', description: '将文档转换为不同格式', icon: 'Switch', template: '将当前文档转换为 Markdown 格式' },
  ],
  rewrite: [
    { id: 'rewrite_formal', name: '正式语气', description: '使用正式语气重写', icon: 'Briefcase', template: '请使用正式语气重写以下文本：\n{selectedText}' },
    { id: 'rewrite_casual', name: '轻松语气', description: '使用轻松语气重写', icon: 'ChatRound', template: '请使用轻松友好的语气重写以下文本：\n{selectedText}' },
    { id: 'rewrite_professional', name: '专业化', description: '使用专业术语重写', icon: 'Medal', template: '请使用更专业的术语重写以下文本：\n{selectedText}' },
  ],
  summary: [
    { id: 'summary_brief', name: '简要总结', description: '生成简短的总结', icon: 'Paperclip', template: '请简要总结以下文本（100字以内）：\n{selectedText}' },
    { id: 'summary_detailed', name: '详细总结', description: '生成详细的总结', icon: 'Document', template: '请详细总结以下文本，包括关键点和重要数据：\n{selectedText}' },
    { id: 'summary_executive', name: '执行摘要', description: '生成执行层面的摘要', icon: 'Management', template: '请为以下文本生成一份适合管理层阅读的执行摘要：\n{selectedText}' },
  ],
};

const fullMarkdownExample = `
# 全场景Markdown响应示例

这是一个模拟的AI回复，用于测试Markdown渲染的各种场景。

## 1. 文本格式

- **粗体文本**: **这是一个加粗的词语**
- *斜体文本*: *这是一个倾斜的词语*
- ***粗斜体文本***: ***这是一个既加粗又倾斜的词语***
- ~~删除线文本~~: ~~这是一个带删除线的词语~~

## 2. 列表

### 无序列表
- 列表项 A
  - 嵌套列表项 A1
  - 嵌套列表项 A2
- 列表项 B
- 列表项 C

### 有序列表
1. 第一项
2. 第二项
   1. 嵌套第一项
   2. 嵌套第二项
3. 第三项

## 3. 代码块

这里是一个JavaScript代码块的示例，带有语法高亮：

\`\`\`javascript
function greet(name) {
  // 返回一个问候字符串
  return \`Hello, \${name}! This is a test of code block rendering.\`;
}

console.log(greet('Developer'));
\`\`\`

## 4. 表格

| 表头 1 | 表头 2 | 表头 3 |
| :--- | :---: | ---: |
| 左对齐 | 居中对齐 | 右对齐 |
| 单元格 | 单元格 | 单元格 |
| 单元格 | 单元格 | 单元格 |

## 5. 引用块

> "这是一个引用块。它通常用于引用他人的话语或突出显示特定段落。"
> > 这是一个嵌套的引用块。

## 6. 其他元素

---

这是一个水平分割线，用于分隔内容。

链接示例: [访问 Element Plus 官网](https://element-plus.org)
`;

// 生成思考内容
function generateThinking(question: string): string {
  return `让我思考一下这个问题...\n\n问题分析：\n${question}\n\n这个问题涉及到几个关键点：\n1. 背景信息\n2. 核心概念\n3. 实际应用\n\n开始组织回答...`;
}

// 生成回答内容
function generateAnswer(question: string, withReference = false): string {
  const baseAnswer = `关于"${question}"，我的回答是：\n\n这是一个很好的问题。根据我所了解的信息，这个问题可以从多个角度来分析...\n\n首先，我们需要考虑...\n\n其次，重要的一点是...\n\n最后，不要忘记...\n\n希望这个回答对你有所帮助！`;
  
  if (withReference) {
    return `根据知识库内容，${baseAnswer}`;
  }
  
  return baseAnswer;
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
        data: mockCommands.filter(c => !c.hasSubCommands)
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
  
  // 流式发送消息
  {
    url: '/api/chat/completions/stream',
    method: 'post',
    response: (req: RequestParams) => {
      // 从body或query中获取commandId，而不是从params中获取
      const { body, query } = req;
      const commandId = body?.commandId || query?.commandId;
      
      console.log('流式请求参数:', { body, query, commandId });
      
      // 根据commandId生成不同的回复内容（如果需要）
      let content = fullMarkdownExample;
      if (commandId) {
        console.log(`处理命令ID: ${commandId} 的流式请求`);
        // 这里可以根据commandId定制不同的回复内容
      }
      
      // 创建一个简单的流式响应
      const encoder = new TextEncoder();
      let chunks: Uint8Array[] = [];
      
      // 发送内容（分成多个小块）
      const contentChunks = content.split(' ');
      for (let i = 0; i < contentChunks.length; i++) {
        chunks.push(encoder.encode(JSON.stringify({ content: contentChunks[i] + ' ', id: uuidv4() }) + '\n'));
      }
      
      // 创建可读流
      const stream = new ReadableStream({
        start(controller) {
          let index = 0;
          
          function push() {
            if (index < chunks.length) {
              controller.enqueue(chunks[index]);
              index++;
              setTimeout(push, 50);  // 模拟流式传输
            } else {
              controller.close();
            }
          }
          
          push();
        }
      });
      
      return {
        code: 200,
        body: stream,
        headers: {
          'Content-Type': 'text/event-stream',
        },
      };
    },
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
  },
  
  // 获取私有命令
  {
    url: '/api/commands',
    method: 'get',
    response: () => ({
      code: 200,
      msg: '获取成功',
      data: mockCommands,
    }),
  },
  // 获取共享命令
  {
    url: '/api/commands/shared',
    method: 'get',
    response: () => ({
      code: 200,
      msg: '获取成功',
      data: mockSharedCommands,
    }),
  },
  // 创建私有命令
  {
    url: '/api/commands',
    method: 'post',
    response: ({ body }: { body: any }) => {
      const newCommand = {
        ...body,
        id: `cmd-${Date.now()}`,
        isSystem: false,
        shareType: 'private',
      } as Command
      mockCommands.push(newCommand)
      return {
        code: 200,
        msg: '创建成功',
        data: newCommand,
      }
    },
  },
  // 创建共享命令
  {
    url: '/api/commands/shared',
    method: 'post',
    response: ({ body }: { body: any }) => {
      const newCommand = {
        ...body,
        id: `shared-cmd-${Date.now()}`,
        isSystem: false,
        shareType: 'shared',
      } as Command
      mockSharedCommands.push(newCommand)
      return {
        code: 200,
        msg: '创建成功',
        data: newCommand,
      }
    },
  },
  // 模拟对话历史
  {
    url: '/api/chat/history',
    method: 'get',
    response: () => ({
      code: 200,
      msg: '获取成功',
      data: [], // 初始历史为空
    }),
  },
  // 模拟发送消息
  {
    url: '/api/chat/send',
    method: 'post',
    response: ({ body }: { body: any }) => {
      const { message } = body
      const reply: Message = {
        id: uuidv4(),
        role: 'assistant',
        content: `这是对"${message.substring(0, 20)}..."的模拟回复。`,
        timestamp: Date.now(),
      }
      return {
        code: 200,
        msg: '发送成功',
        data: reply,
      }
    },
  },
  // 获取子命令
  {
    url: '/api/commands/:commandId/sub-commands',
    method: 'post',
    response: (req: any) => {
      const { commandId } = req.params
      const { body } = req
      
      // 从mock数据中获取子命令
      const subCommands = mockSubCommands[commandId] || []
      
      // 如果提供了documentId，可以返回适合该文档的特定子命令
      if (body?.documentId) {
        console.log(`获取文档ID: ${body.documentId} 相关的子命令`)
        // 这里可以基于documentId做一些过滤或增强，示例中我们不做特殊处理
      }
      
      return {
        code: 200,
        msg: '获取成功',
        data: subCommands
      }
    }
  },
  {
    url: '/api/knowledge-bases',
    method: 'get',
    response: () => {
      return {
        code: 200,
        msg: '操作成功',
        data: mockKnowledgeBases,
      };
    },
  },
  {
    url: '/api/quick-commands',
    method: 'get',
    response: () => {
      return {
        code: 200,
        msg: '操作成功',
        data: mockCommands,
      };
    },
  },
  // RAG 流式对话
  {
    url: '/api/document-ai/rag/chat',
    method: 'post',
    response: ({ body }: { body: RAGChatRequest }) => {
      // 生成内容，如果启用了深度思考则包含思考内容
      const content = generateAnswer(body.question, true);
      const thinking = body.deepthinking ? generateThinking(body.question) : '';
      
      // 创建数据流响应
      const encoder = new TextEncoder();
      let chunks: Uint8Array[] = [];
      
      // 如果有思考内容，先发送思考内容
      if (thinking) {
        chunks.push(encoder.encode(`data: <think>${thinking}</think>\n\n`));
      }
      
      // 发送主要内容（分成多个小块）
      const contentChunks = content.split(' ');
      for (let i = 0; i < contentChunks.length; i++) {
        chunks.push(encoder.encode(`data: ${contentChunks[i]} \n\n`));
      }
      
      // 发送结束标记
      chunks.push(encoder.encode('data: ["DONE"]\n\n'));
      
      // 创建可读流
      return new ReadableStream({
        start(controller) {
          let index = 0;
          
          function push() {
            if (index < chunks.length) {
              controller.enqueue(chunks[index]);
              index++;
              setTimeout(push, 50);  // 模拟流式传输
            } else {
              controller.close();
            }
          }
          
          push();
        }
      });
    }
  },
  // RAG 非流式对话
  {
    url: '/api/document-ai/ai/rag/chat',
    method: 'post',
    response: ({ body }: { body: RAGChatRequest }): RAGChatResponse => {
      return {
        code: 200,
        data: {
          answer: generateAnswer(body.question, true),
          reference: mockReference,
          doc_aggs: mockDocAggs,
          prompt: body.deepthinking ? generateThinking(body.question) : undefined
        }
      };
    }
  },
  // 普通流式对话
  {
    url: '/api/document-ai/ai/poststreamPolish',
    method: 'post',
    response: ({ body }: { body: NormalChatRequest }) => {
      // 生成内容，如果启用了深度思考则包含思考内容
      const content = generateAnswer(body.prompt);
      const thinking = body.deepthinking ? generateThinking(body.prompt) : '';
      
      // 创建数据流响应
      const encoder = new TextEncoder();
      let chunks: Uint8Array[] = [];
      
      // 如果有思考内容，先发送思考内容
      if (thinking) {
        chunks.push(encoder.encode(`data: <think>${thinking}</think>\n\n`));
      }
      
      // 发送主要内容（分成多个小块）
      const contentChunks = content.split(' ');
      for (let i = 0; i < contentChunks.length; i++) {
        chunks.push(encoder.encode(`data: ${contentChunks[i]} \n\n`));
      }
      
      // 发送结束标记
      chunks.push(encoder.encode('data: ["DONE"]\n\n'));
      
      // 创建可读流
      return new ReadableStream({
        start(controller) {
          let index = 0;
          
          function push() {
            if (index < chunks.length) {
              controller.enqueue(chunks[index]);
              index++;
              setTimeout(push, 50);  // 模拟流式传输
            } else {
              controller.close();
            }
          }
          
          push();
        }
      });
    }
  },
  // 普通非流式对话
  {
    url: '/api/document-ai/ai/postPolish',
    method: 'post',
    response: ({ body }: { body: NormalChatRequest }): NormalChatResponse => {
      return {
        code: 200,
        data: {
          content: generateAnswer(body.prompt),
          thinking: body.deepthinking ? generateThinking(body.prompt) : undefined
        }
      };
    }
  },
  // 创建子命令
  {
    url: '/api/commands/:commandId/sub-commands',
    method: 'post',
    response: (req: any) => {
      const { commandId } = req.params
      const { body } = req
      
      if (!mockSubCommands[commandId]) {
        mockSubCommands[commandId] = []
      }
      
      // 创建新子命令
      const newSubCommand: SubCommand = {
        id: `${commandId}_sub_${uuidv4().substring(0, 8)}`,
        name: body.name || '新建子命令',
        description: body.description || '',
        icon: body.icon || 'Document',
        template: body.template || '{selectedText}',
      }
      
      mockSubCommands[commandId].push(newSubCommand)
      
      return {
        code: 200,
        msg: '创建成功',
        data: newSubCommand
      }
    }
  },
  // 更新子命令
  {
    url: '/api/commands/:commandId/sub-commands/:subCommandId',
    method: 'put',
    response: (req: any) => {
      const { commandId, subCommandId } = req.params
      const { body } = req
      
      // 检查父命令和子命令是否存在
      if (!mockSubCommands[commandId]) {
        return {
          code: 404,
          msg: '父命令不存在',
          data: null
        }
      }
      
      const subCommandIndex = mockSubCommands[commandId].findIndex(cmd => cmd.id === subCommandId)
      if (subCommandIndex === -1) {
        return {
          code: 404,
          msg: '子命令不存在',
          data: null
        }
      }
      
      // 更新子命令
      const updatedSubCommand = {
        ...mockSubCommands[commandId][subCommandIndex],
        ...body,
        id: subCommandId // 保留原ID
      }
      
      mockSubCommands[commandId][subCommandIndex] = updatedSubCommand
      
      return {
        code: 200,
        msg: '更新成功',
        data: updatedSubCommand
      }
    }
  },
  // 删除子命令
  {
    url: '/api/commands/:commandId/sub-commands/:subCommandId',
    method: 'delete',
    response: (req: any) => {
      const { commandId, subCommandId } = req.params
      
      // 检查父命令和子命令是否存在
      if (!mockSubCommands[commandId]) {
        return {
          code: 404,
          msg: '父命令不存在',
          data: null
        }
      }
      
      const subCommandIndex = mockSubCommands[commandId].findIndex(cmd => cmd.id === subCommandId)
      if (subCommandIndex === -1) {
        return {
          code: 404,
          msg: '子命令不存在',
          data: null
        }
      }
      
      // 删除子命令
      mockSubCommands[commandId].splice(subCommandIndex, 1)
      
      return {
        code: 200,
        msg: '删除成功',
        data: null
      }
    }
  },
  // Add specific handler for '/api/commands/document/sub-commands'
  {
    url: '/api/commands/document/sub-commands',
    method: 'post',
    response: ({ body }: RequestParams) => {
      // Return document sub-commands from our mock data
      const subCommands = mockSubCommands['document'] || []
      
      return {
        code: 200,
        msg: '获取成功',
        data: subCommands
      }
    }
  },
]

export default mockApi 