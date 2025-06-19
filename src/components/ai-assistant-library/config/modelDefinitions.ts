import type { AIModelDefinition } from '@/types/chat'

export const modelDefinitions: Record<string, AIModelDefinition> = {
  'gpt-4-turbo': {
    id: 'gpt-4-turbo',
    label: 'GPT-4 Turbo',
    provider: 'openai',
    type: 'openai',
    logo: '/ai-models/gpt4.png',
    description: '最新的GPT-4 Turbo模型，支持更长上下文和更新的知识库',
    level: 'super',
    defaults: {
      maxTokens: 128000,
      temperature: 0.7,
      modelVersion: 'gpt-4-turbo-preview'
    }
  },
  'gpt-4': {
    id: 'gpt-4',
    label: 'GPT-4',
    provider: 'openai',
    type: 'openai',
    logo: '/ai-models/gpt4.png',
    description: '功能强大的GPT-4模型，适合复杂任务',
    level: 'advanced',
    defaults: {
      maxTokens: 8192,
      temperature: 0.7,
      modelVersion: 'gpt-4'
    }
  },
  'gpt-3.5-turbo': {
    id: 'gpt-3.5-turbo',
    label: 'GPT-3.5 Turbo',
    provider: 'openai',
    type: 'openai',
    logo: '/ai-models/gpt3.png',
    description: '高效的GPT-3.5模型，适合日常任务',
    level: 'basic',
    defaults: {
      maxTokens: 4096,
      temperature: 0.7,
      modelVersion: 'gpt-3.5-turbo'
    }
  },
  'claude-3-opus': {
    id: 'claude-3-opus',
    label: 'Claude 3 Opus',
    provider: 'anthropic',
    type: 'anthropic',
    logo: '/ai-models/claude.png',
    description: 'Anthropic最强大的Claude 3模型',
    level: 'super',
    defaults: {
      maxTokens: 200000,
      temperature: 0.7,
      modelVersion: 'claude-3-opus-20240229'
    }
  },
  'gemini-ultra': {
    id: 'gemini-ultra',
    label: 'Gemini Ultra',
    provider: 'google',
    type: 'google',
    logo: '/ai-models/gemini.png',
    description: 'Google最强大的Gemini模型',
    level: 'super',
    defaults: {
      maxTokens: 32768,
      temperature: 0.7,
      modelVersion: 'gemini-ultra'
    }
  },
  'qwen-turbo': {
    id: 'qwen-turbo',
    label: '通义千问 Turbo',
    provider: 'alibaba',
    type: 'alibaba',
    logo: '/ai-models/qwen.png',
    description: '阿里云通义千问高性能模型',
    level: 'advanced',
    defaults: {
      maxTokens: 6144,
      temperature: 0.7,
      modelVersion: 'qwen-turbo'
    }
  },
  'qwen-plus': {
    id: 'qwen-plus',
    label: '通义千问 Plus',
    provider: 'alibaba',
    type: 'alibaba',
    logo: '/ai-models/qwen.png',
    description: '阿里云通义千问增强版模型，性能更强',
    level: 'advanced',
    defaults: {
      maxTokens: 8192,
      temperature: 0.7,
      modelVersion: 'qwen-plus'
    }
  },
  'qwen-max': {
    id: 'qwen-max',
    label: '通义千问 Max',
    provider: 'alibaba',
    type: 'alibaba',
    logo: '/ai-models/qwen.png',
    description: '阿里云最强大的通义千问模型，能力全面',
    level: 'super',
    defaults: {
      maxTokens: 8192,
      temperature: 0.7,
      modelVersion: 'qwen-max'
    }
  },
  'qwen-14b': {
    id: 'qwen-14b',
    label: '通义千问 14B',
    provider: 'alibaba',
    type: 'alibaba',
    logo: '/ai-models/qwen.png',
    description: '通义千问开源14B模型，基础能力强',
    level: 'basic',
    defaults: {
      maxTokens: 4096,
      temperature: 0.7,
      modelVersion: 'qwen-14b'
    }
  },
  'qwen-7b': {
    id: 'qwen-7b',
    label: '通义千问 7B',
    provider: 'alibaba',
    type: 'alibaba',
    logo: '/ai-models/qwen.png',
    description: '通义千问开源7B模型，轻量高效',
    level: 'basic',
    defaults: {
      maxTokens: 2048,
      temperature: 0.7,
      modelVersion: 'qwen-7b'
    }
  },
  'ernie-4': {
    id: 'ernie-4',
    label: '文心一言 4.0',
    provider: 'baidu',
    type: 'baidu',
    logo: '/ai-models/ernie.png',
    description: '百度文心一言最新模型',
    level: 'advanced',
    defaults: {
      maxTokens: 4096,
      temperature: 0.7,
      modelVersion: 'ernie-4.0'
    }
  },
  'deepseek-coder': {
    id: 'deepseek-coder',
    label: 'DeepSeek Coder',
    provider: 'deepseek',
    type: 'deepseek',
    logo: '/ai-models/deepseek.png',
    description: '专注于代码生成和理解的大模型',
    level: 'advanced',
    defaults: {
      maxTokens: 8192,
      temperature: 0.7,
      modelVersion: 'deepseek-coder-33b'
    }
  }
} 