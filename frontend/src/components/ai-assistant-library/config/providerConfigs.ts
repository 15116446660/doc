// src/components/ai-assistant-library/config/providerConfigs.ts

import type { Provider } from '@/types/chat'

export const providerConfigs: Record<string, Provider> = {
  openai: {
    name: 'OpenAI',
    icon: 'openai',
    description: 'OpenAI提供的GPT系列模型',
    configFields: ['apiKey', 'baseUrl', 'temperature', 'maxTokens'],
    defaultModel: 'gpt-3.5-turbo',
    defaultSystemPrompt: '你是一个有用的AI助手。',
    tokenName: 'API Key',
    apiBaseUrl: 'https://api.openai.com',
    apiPath: '/v1/chat/completions',
    streamPath: '/v1/chat/completions',
    modelParam: 'model',
    passwordLabel: 'API Key',
    models: ['gpt-4', 'gpt-3.5-turbo'],
    supportsStreaming: true,
    inputParams: {
      minTemperature: 0,
      maxTemperature: 2,
      minTopP: 0,
      maxTopP: 1
    }
  },
  anthropic: {
    name: 'Anthropic',
    icon: 'anthropic',
    description: 'Anthropic提供的Claude系列模型',
    configFields: ['apiKey', 'temperature', 'maxTokens'],
    defaultModel: 'claude-3',
    defaultSystemPrompt: '你是Claude，一个有用的AI助手。',
    tokenName: 'API Key',
    apiBaseUrl: 'https://api.anthropic.com',
    apiPath: '/v1/messages',
    streamPath: '/v1/messages',
    modelParam: 'model',
    passwordLabel: 'API Key',
    models: ['claude-3'],
    supportsStreaming: true,
    inputParams: {
      minTemperature: 0,
      maxTemperature: 1,
      minTopP: 0,
      maxTopP: 1
    }
  },
  alibaba: {
    name: '通义千问',
    icon: 'alibaba',
    description: '阿里云提供的通义千问系列模型',
    configFields: ['apiKey', 'temperature', 'maxTokens'],
    defaultModel: 'qwen-turbo',
    defaultSystemPrompt: '你是通义千问，一个有用的AI助手。',
    tokenName: 'API Key',
    apiBaseUrl: 'https://dashscope.aliyuncs.com',
    apiPath: '/api/v1/services/aigc/text-generation/generation',
    streamPath: '/api/v1/services/aigc/text-generation/generation',
    modelParam: 'model',
    passwordLabel: 'API Key',
    models: ['qwen-turbo', 'qwen-plus', 'qwen-max', 'qwen-2.5', 'qwen-14b', 'qwen-7b'],
    supportsStreaming: true,
    inputParams: {
      minTemperature: 0,
      maxTemperature: 2,
      minTopP: 0,
      maxTopP: 1
    }
  },
  baidu: {
    name: '文心一言',
    icon: 'baidu',
    description: '百度智能云提供的文心一言系列模型',
    configFields: ['apiKey', 'temperature', 'maxTokens'],
    defaultModel: 'ernie-4',
    defaultSystemPrompt: '你是文心一言，一个有用的AI助手。',
    tokenName: 'API Key',
    apiBaseUrl: 'https://aip.baidubce.com',
    apiPath: '/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions',
    streamPath: '/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions_pro',
    modelParam: 'model',
    passwordLabel: 'API Key',
    models: ['ernie-4', 'ernie-3.5'],
    supportsStreaming: true,
    inputParams: {
      minTemperature: 0,
      maxTemperature: 1,
      minTopP: 0,
      maxTopP: 1
    }
  },
  google: {
    name: 'Google AI',
    icon: 'google',
    description: 'Google提供的Gemini系列模型',
    configFields: ['apiKey', 'temperature', 'maxTokens'],
    defaultModel: 'gemini-pro',
    defaultSystemPrompt: '你是Gemini，一个有用的AI助手。',
    tokenName: 'API Key',
    apiBaseUrl: 'https://generativelanguage.googleapis.com',
    apiPath: '/v1beta/models/gemini-pro:generateContent',
    streamPath: '/v1beta/models/gemini-pro:streamGenerateContent',
    modelParam: 'model',
    passwordLabel: 'API Key',
    models: ['gemini-pro'],
    supportsStreaming: true,
    inputParams: {
      minTemperature: 0,
      maxTemperature: 1,
      minTopP: 0,
      maxTopP: 1
    }
  },
  mistral: {
    name: 'Mistral AI',
    icon: 'mistral',
    description: 'Mistral AI提供的大语言模型',
    configFields: ['apiKey', 'temperature', 'maxTokens'],
    defaultModel: 'mistral-large',
    defaultSystemPrompt: '你是Mistral，一个有用的AI助手。',
    tokenName: 'API Key',
    apiBaseUrl: 'https://api.mistral.ai',
    apiPath: '/v1/chat/completions',
    streamPath: '/v1/chat/completions',
    modelParam: 'model',
    passwordLabel: 'API Key',
    models: ['mistral-large'],
    supportsStreaming: true,
    inputParams: {
      minTemperature: 0,
      maxTemperature: 1,
      minTopP: 0,
      maxTopP: 1
    }
  },
  deepseek: {
    name: 'DeepSeek AI',
    icon: 'deepseek',
    description: 'DeepSeek提供的开源大模型',
    configFields: ['apiKey', 'temperature', 'maxTokens'],
    defaultModel: 'deepseek-v3',
    defaultSystemPrompt: '你是DeepSeek，一个有用的AI助手。',
    tokenName: 'API Key',
    apiBaseUrl: 'https://api.deepseek.com',
    apiPath: '/v1/chat/completions',
    streamPath: '/v1/chat/completions',
    modelParam: 'model',
    passwordLabel: 'API Key',
    models: ['deepseek-coder', 'deepseek-v3'],
    supportsStreaming: true,
    inputParams: {
      minTemperature: 0,
      maxTemperature: 2,
      minTopP: 0,
      maxTopP: 1
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