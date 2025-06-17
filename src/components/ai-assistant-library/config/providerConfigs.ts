// src/components/ai-assistant-library/config/providerConfigs.ts

interface ModelOption {
  value: string;
  label: string;
  description?: string;
}

interface ProviderConfig {
  defaults: {
    baseUrl: string;
    temperature: number;
    maxTokens: number;
    topP?: number;
    topK?: number;
  };
  ranges: {
    temperature: [number, number];
    maxTokens: [number, number];
    topP?: [number, number];
    topK?: [number, number];
  };
  basicParams: {
    modelName: boolean;
  };
  advancedParams: {
    topP: boolean;
    topK: boolean;
    frequencyPenalty: boolean;
    presencePenalty: boolean;
    stream: boolean;
  };
  specialParams?: {
    randomSeed?: boolean;
  };
  modelOptions?: ModelOption[];
}

const providerConfigs: Record<string, ProviderConfig> = {
  openai: {
    defaults: {
      baseUrl: 'https://api.openai.com/v1',
      temperature: 0.7,
      maxTokens: 4096,
      topP: 1.0,
    },
    ranges: {
      temperature: [0, 2],
      maxTokens: [1, 32000],
      topP: [0, 1],
    },
    basicParams: { modelName: true },
    advancedParams: {
      topP: true,
      topK: false,
      frequencyPenalty: true,
      presencePenalty: true,
      stream: true,
    },
    modelOptions: [
      { value: 'gpt-4-turbo', label: 'GPT-4 Turbo', description: '最新的GPT-4 Turbo模型，具有视觉能力。' },
      { value: 'gpt-4', label: 'GPT-4', description: '功能强大的模型，但在某些方面不如Turbo。' },
      { value: 'gpt-3.5-turbo', label: 'GPT-3.5 Turbo', description: '速度快，成本效益高的模型。' },
    ],
  },
  azure: {
    defaults: {
      baseUrl: '',
      temperature: 0.7,
      maxTokens: 4096,
    },
    ranges: {
      temperature: [0, 1],
      maxTokens: [1, 32000],
    },
    basicParams: { modelName: false },
    advancedParams: {
      topP: true,
      topK: false,
      frequencyPenalty: true,
      presencePenalty: true,
      stream: true,
    },
  },
  anthropic: {
    defaults: {
      baseUrl: 'https://api.anthropic.com/v1',
      temperature: 0.7,
      maxTokens: 4096,
      topK: 50
    },
    ranges: {
      temperature: [0, 1],
      maxTokens: [1, 200000],
      topK: [1, 100],
    },
    basicParams: { modelName: true },
    advancedParams: {
      topP: true,
      topK: true,
      frequencyPenalty: false,
      presencePenalty: false,
      stream: true,
    },
    modelOptions: [
        { value: "claude-3-opus-20240229", label: "Claude 3 Opus", description: "最强大的模型，适用于复杂任务。" },
        { value: "claude-3-sonnet-20240229", label: "Claude 3 Sonnet", description: "在智能和速度之间取得理想平衡。" },
        { value: "claude-3-haiku-20240307", label: "Claude 3 Haiku", description: "最快、最紧凑的模型，响应迅速。" },
    ],
  },
  google: {
     defaults: {
      baseUrl: 'https://generativelanguage.googleapis.com/v1beta',
      temperature: 0.9,
      maxTokens: 8192,
      topP: 1.0,
      topK: 1
    },
    ranges: {
      temperature: [0, 1],
      maxTokens: [1, 8192],
      topP: [0, 1],
      topK: [1, 100],
    },
    basicParams: { modelName: true },
    advancedParams: {
      topP: true,
      topK: true,
      frequencyPenalty: false,
      presencePenalty: false,
      stream: true,
    },
    modelOptions: [
      { value: 'gemini-1.5-pro-latest', label: 'Gemini 1.5 Pro', description: '最新、最强大的多模态模型。' },
      { value: 'gemini-pro', label: 'Gemini Pro', description: '为处理各种文本和代码任务而优化。' },
      { value: 'gemini-pro-vision', label: 'Gemini Pro Vision', description: '专为处理视觉相关任务而设计。' },
    ],
  },
  mistral: {
    defaults: {
      baseUrl: 'https://api.mistral.ai/v1',
      temperature: 0.7,
      maxTokens: 32000,
      topP: 1.0
    },
    ranges: {
      temperature: [0, 1],
      maxTokens: [1, 32000],
      topP: [0, 1],
    },
    basicParams: { modelName: true },
    advancedParams: {
      topP: true,
      topK: false,
      frequencyPenalty: false,
      presencePenalty: false,
      stream: true,
    },
    specialParams: {
      randomSeed: true,
    },
    modelOptions: [
      { value: 'mistral-large-latest', label: 'Mistral Large', description: '顶级推理能力的旗舰模型。' },
      { value: 'mistral-small-latest', label: 'Mistral Small', description: '低延迟、高效率的模型。' },
    ],
  },
  deepseek: {
    defaults: {
      baseUrl: 'https://api.deepseek.com/v1',
      temperature: 0.7,
      maxTokens: 4096
    },
    ranges: {
      temperature: [0, 2],
      maxTokens: [1, 32000]
    },
    basicParams: { modelName: true },
    advancedParams: {
      topP: true,
      topK: false,
      frequencyPenalty: true,
      presencePenalty: true,
      stream: true
    },
    modelOptions: [
      { value: 'deepseek-chat', label: 'DeepSeek Chat', description: '擅长对话的通用模型。' },
      { value: 'deepseek-coder', label: 'DeepSeek Coder', description: '专注于代码生成和理解。' },
    ]
  },
  other: {
    defaults: {
      baseUrl: '',
      temperature: 0.7,
      maxTokens: 2048
    },
    ranges: {
      temperature: [0, 2],
      maxTokens: [1, 32000]
    },
    basicParams: { modelName: true },
    advancedParams: {
      topP: true,
      topK: true,
      frequencyPenalty: true,
      presencePenalty: true,
      stream: true
    }
  }
};

export function getProviderConfig(type: string): ProviderConfig {
  return providerConfigs[type] || providerConfigs.other;
}

export function getModelOptions(type: string): ModelOption[] {
  const config = providerConfigs[type];
  return config?.modelOptions || [];
}

export function getDefaultBaseUrl(type: string): string {
  const config = providerConfigs[type];
  return config?.defaults.baseUrl || '';
} 