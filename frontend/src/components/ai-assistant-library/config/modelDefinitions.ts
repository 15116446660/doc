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
    logo: 'openai',
    description: 'OpenAI最先进的大语言模型',
    level: 'super',
    defaults: {
      maxTokens: 8192,
      temperature: 0.7,
      modelVersion: 'gpt-4'
    }
  },
  'gpt-3.5-turbo': {
    id: 'gpt-3.5-turbo',
    label: 'GPT-3.5',
    provider: 'openai',
    type: 'openai',
    logo: 'openai',
    description: '强大而经济的AI模型',
    level: 'basic',
    defaults: {
      maxTokens: 4096,
      temperature: 0.7,
      modelVersion: 'gpt-3.5-turbo'
    }
  },
  'claude-3': {
    id: 'claude-3',
    label: 'Claude 3',
    provider: 'anthropic',
    type: 'anthropic',
    logo: 'anthropic',
    description: 'Anthropic最先进的AI模型',
    level: 'super',
    defaults: {
      maxTokens: 8192,
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
    logo: 'alibaba',
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
    logo: 'alibaba',
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
    logo: 'alibaba',
    description: '阿里云最强大的通义千问模型，能力全面',
    level: 'super',
    defaults: {
      maxTokens: 8192,
      temperature: 0.7,
      modelVersion: 'qwen-max'
    }
  },
  'qwen-2.5': {
    id: 'qwen-2.5',
    label: '通义千问 2.5',
    provider: 'alibaba',
    type: 'alibaba',
    logo: 'alibaba',
    description: '通义千问最新一代基础大模型，性能大幅提升',
    level: 'super',
    defaults: {
      maxTokens: 10240,
      temperature: 0.7,
      modelVersion: 'qwen-2.5'
    }
  },
  'qwen-14b': {
    id: 'qwen-14b',
    label: '通义千问 14B',
    provider: 'alibaba',
    type: 'alibaba',
    logo: 'alibaba',
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
    logo: 'alibaba',
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
    logo: 'baidu',
    description: '百度最强的中文大模型',
    level: 'super',
    defaults: {
      maxTokens: 4096,
      temperature: 0.7,
      modelVersion: 'ernie-4.0-8k'
    }
  },
  'ernie-3.5': {
    id: 'ernie-3.5',
    label: '文心一言 3.5',
    provider: 'baidu',
    type: 'baidu',
    logo: 'baidu',
    description: '百度文心一言通用大模型',
    level: 'advanced',
    defaults: {
      maxTokens: 2048,
      temperature: 0.7,
      modelVersion: 'ernie-3.5-8k'
    }
  },
  'gemini-pro': {
    id: 'gemini-pro',
    label: 'Gemini Pro',
    provider: 'google',
    type: 'google',
    logo: 'google',
    description: 'Google最强大的多模态AI系统',
    level: 'super',
    defaults: {
      maxTokens: 8192,
      temperature: 0.7,
      modelVersion: 'gemini-pro'
    }
  },
  'mistral-large': {
    id: 'mistral-large',
    label: 'Mistral Large',
    provider: 'mistral',
    type: 'mistral',
    logo: 'mistral',
    description: 'Mistral AI的高级大模型',
    level: 'advanced',
    defaults: {
      maxTokens: 4096,
      temperature: 0.7,
      modelVersion: 'mistral-large-latest'
    }
  },
  'deepseek-coder': {
    id: 'deepseek-coder',
    label: 'DeepSeek Coder',
    provider: 'deepseek',
    type: 'deepseek',
    logo: 'deepseek',
    description: '专注于代码生成和理解的大模型',
    level: 'advanced',
    defaults: {
      maxTokens: 8192,
      temperature: 0.7,
      modelVersion: 'deepseek-coder-33b'
    }
  },
  'deepseek-v3': {
    id: 'deepseek-v3',
    label: 'DeepSeek V3',
    provider: 'deepseek',
    type: 'deepseek',
    logo: 'deepseek',
    description: 'DeepSeek最新一代通用大模型，能力全面增强',
    level: 'super',
    defaults: {
      maxTokens: 16384,
      temperature: 0.7,
      modelVersion: 'deepseek-v3'
    }
  }
} 