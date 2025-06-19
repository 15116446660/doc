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