// src/components/ai-assistant-library/config/providerConfigs.ts

import type { Provider, ModelOption } from '@/types/chat'

export const providerConfigs: Record<string, Provider> = {
  openai: {
    name: 'OpenAI',
    logo: '/ai-models/openai.png',
    description: 'OpenAI提供的GPT系列模型',
    configFields: ['apiKey', 'baseUrl', 'temperature', 'maxTokens'],
    ranges: {
      temperature: [0, 2],
      maxTokens: [1, 128000],
      topP: [0, 1]
    },
    basicParams: {
      modelName: true
    },
    advancedParams: {
      topP: true,
      topK: false,
      frequencyPenalty: true,
      presencePenalty: true,
      stream: true
    }
  },
  anthropic: {
    name: 'Anthropic',
    logo: '/ai-models/anthropic.png',
    description: 'Anthropic提供的Claude系列模型',
    configFields: ['apiKey', 'temperature', 'maxTokens'],
    ranges: {
      temperature: [0, 1],
      maxTokens: [1, 200000],
      topK: [1, 100]
    },
    basicParams: {
      modelName: true
    },
    advancedParams: {
      topP: false,
      topK: true,
      frequencyPenalty: false,
      presencePenalty: false,
      stream: true
    }
  },
  google: {
    name: 'Google',
    logo: '/ai-models/google.png',
    description: 'Google提供的Gemini系列模型',
    configFields: ['apiKey', 'temperature', 'maxTokens'],
    ranges: {
      temperature: [0, 1],
      maxTokens: [1, 32768],
      topP: [0, 1],
      topK: [1, 100]
    },
    basicParams: {
      modelName: true
    },
    advancedParams: {
      topP: true,
      topK: true,
      frequencyPenalty: false,
      presencePenalty: false,
      stream: true
    }
  },
  alibaba: {
    name: '阿里云',
    logo: '/ai-models/alibaba.png',
    description: '阿里云提供的通义千问系列模型',
    configFields: ['apiKey', 'temperature', 'maxTokens'],
    ranges: {
      temperature: [0, 1],
      maxTokens: [1, 6144]
    },
    basicParams: {
      modelName: true
    },
    advancedParams: {
      topP: true,
      topK: false,
      frequencyPenalty: false,
      presencePenalty: false,
      stream: true
    },
    modelOptions: [
      {
        id: 'qwen-turbo',
        name: '通义千问 Turbo',
        description: '快速高效的通义千问标准模型',
        maxTokens: 6144,
        contextWindow: 32768
      },
      {
        id: 'qwen-plus',
        name: '通义千问 Plus',
        description: '增强版千问模型，性能更好',
        maxTokens: 8192,
        contextWindow: 32768
      },
      {
        id: 'qwen-max',
        name: '通义千问 Max',
        description: '阿里云的旗舰级大模型，能力全面',
        maxTokens: 8192,
        contextWindow: 65536
      },
      {
        id: 'qwen-2.5',
        name: '通义千问 2.5',
        description: '最新一代基础大模型，性能全面升级',
        maxTokens: 10240,
        contextWindow: 128000
      },
      {
        id: 'qwen-14b',
        name: '通义千问 14B',
        description: '开源的14B参数通义千问模型',
        maxTokens: 4096,
        contextWindow: 16384
      },
      {
        id: 'qwen-7b',
        name: '通义千问 7B',
        description: '轻量级开源通义千问模型',
        maxTokens: 2048,
        contextWindow: 8192
      }
    ],
    defaults: {
      baseUrl: 'https://dashscope.aliyuncs.com/api/v1'
    }
  },
  baidu: {
    name: '百度智能云',
    logo: '/ai-models/baidu.png',
    description: '百度智能云提供的文心一言系列模型',
    configFields: ['apiKey', 'temperature', 'maxTokens'],
    ranges: {
      temperature: [0, 1],
      maxTokens: [1, 4096]
    },
    basicParams: {
      modelName: true
    },
    advancedParams: {
      topP: true,
      topK: false,
      frequencyPenalty: false,
      presencePenalty: false,
      stream: true
    }
  },
  xunfei: {
    name: '讯飞开放平台',
    logo: '/ai-models/xunfei.png',
    description: '讯飞开放平台提供的星火系列模型',
    configFields: ['apiKey', 'temperature', 'maxTokens'],
    ranges: {
      temperature: [0, 1],
      maxTokens: [1, 4096]
    },
    basicParams: {
      modelName: true
    },
    advancedParams: {
      topP: true,
      topK: false,
      frequencyPenalty: false,
      presencePenalty: false,
      stream: true
    }
  },
  deepseek: {
    name: 'DeepSeek',
    logo: '/ai-models/deepseek.png',
    description: 'DeepSeek提供的开源大模型',
    configFields: ['apiKey', 'temperature', 'maxTokens'],
    ranges: {
      temperature: [0, 2],
      maxTokens: [1, 8192]
    },
    basicParams: {
      modelName: true
    },
    advancedParams: {
      topP: true,
      topK: false,
      frequencyPenalty: true,
      presencePenalty: true,
      stream: true
    },
    modelOptions: [
      {
        id: 'deepseek-coder',
        name: 'DeepSeek Coder',
        description: '专注于代码生成和理解的大模型',
        maxTokens: 8192,
        contextWindow: 32768
      },
      {
        id: 'deepseek-v3',
        name: 'DeepSeek V3',
        description: '最新一代通用大模型，能力全面增强',
        maxTokens: 16384,
        contextWindow: 131072
      }
    ],
    defaults: {
      baseUrl: 'https://api.deepseek.com/v1'
    }
  }
}

export function getProviderConfig(type: string): Provider {
  return providerConfigs[type] || providerConfigs.other;
}

export function getModelOptions(type: string): ModelOption[] {
  const config = providerConfigs[type];
  return config?.modelOptions || [];
}

export function getDefaultBaseUrl(type: string): string {
  const config = providerConfigs[type];
  return config?.defaults?.baseUrl || '';
} 