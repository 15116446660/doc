import type { AIModel } from '@/types/chat'

type RawAIModel = {
  id: string
  apiKey: string
  apiUrl: string
  model: string
  modelName: string
  isDefault: 'Y' | 'N'
  cueWord: string
  temperature?: number
  maxTokens?: number
  topP?: number
  frequencyPenalty?: number
  presencePenalty?: number
}

const models: RawAIModel[] = [
  {
    id: '1',
    modelName: 'GPT-4 Turbo',
    model: 'gpt-4-turbo',
    apiKey: 'sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
    apiUrl: 'https://api.openai.com/v1/chat/completions',
    isDefault: 'Y',
    cueWord: 'You are a helpful assistant.',
    temperature: 0.7,
    maxTokens: 4096,
    topP: 1,
    frequencyPenalty: 0,
    presencePenalty: 0,
  },
  {
    id: '2',
    modelName: 'Claude 3 Opus',
    model: 'claude-3-opus-20240229',
    apiKey: 'sk-ant-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
    apiUrl: 'https://api.anthropic.com/v1/messages',
    isDefault: 'N',
    cueWord: 'You are a helpful assistant.',
    temperature: 0.5,
    maxTokens: 200000,
    topP: 1,
    frequencyPenalty: 0,
    presencePenalty: 0,
  },
  {
    id: '3',
    modelName: 'DeepSeek Coder',
    model: 'deepseek-coder',
    apiKey: 'sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
    apiUrl: 'https://api.deepseek.com/chat/completions',
    isDefault: 'N',
    cueWord: 'You are an expert programming assistant.',
    temperature: 0.7,
    maxTokens: 16384,
    topP: 0.9,
    frequencyPenalty: 0,
    presencePenalty: 0,
  }
]

export default [
  {
    url: '/aiModel/selectList',
    method: 'get',
    response: (req: any) => {
      console.log('[Mock] get /aiModel/selectList', req.query);
      return {
        code: 200,
        msg: '操作成功',
        data: models
      }
    }
  },
  {
    url: '/aiModel/addAiModel',
    method: 'post',
    response: (req: any) => {
        console.log('[Mock] post /aiModel/addAiModel', req.body);
        const newModel = {
            id: String(models.length + 1 + Math.random()),
            ...req.body
        };
        // @ts-ignore
        models.push(newModel);
        return { code: 200, msg: '添加成功' };
    }
  },
  {
      url: '/aiModel/updateAiModel',
      method: 'post',
      response: (req: any) => {
          console.log('[Mock] post /aiModel/updateAiModel', req.body);
          const index = models.findIndex(m => m.id === req.body.id);
          if (index !== -1) {
              // @ts-ignore
              models[index] = { ...models[index], ...req.body };
          }
          return { code: 200, msg: '更新成功' };
      }
  },
  {
      url: '/aiModel/delAiModel/:id',
      method: 'delete',
      response: (req: any) => {
          console.log('[Mock] delete /aiModel/delAiModel', req.params);
          const id = req.params.id;
          const index = models.findIndex(m => m.id === id);
          if (index !== -1) {
              models.splice(index, 1);
          }
          return { code: 200, msg: '删除成功' };
      }
  }
] 