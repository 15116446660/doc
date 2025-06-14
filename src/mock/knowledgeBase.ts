import { defineMock } from 'vite-plugin-mock/server';
import type { KnowledgeBase } from '@/types/knowledgeBase';

const mockKnowledgeBases: KnowledgeBase[] = [
  {
    id: 'kb-1',
    name: '产品设计规范文档',
    description: '包含所有产品线的设计原则和组件规范。',
    icon: 'Document',
  },
  {
    id: 'kb-2',
    name: '研发项目管理知识库',
    description: '覆盖项目流程、代码规范和常见问题解答。',
    icon: 'FolderOpened',
  },
  {
    id: 'kb-3',
    name: '市场与竞品分析报告',
    description: '最新的市场趋势和竞争对手动态分析。',
    icon: 'DataAnalysis',
  },
  {
    id: 'kb-4',
    name: '人力资源政策手册',
    description: '公司人事政策、福利和员工手册。',
    icon: 'User',
  },
];

export default defineMock([
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
]); 