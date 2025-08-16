import type { CheckOptions, QualityCheckTask, ProgressItem, CheckResult, IssueDetail } from '@/types/qualityCheck';

const checkOptions: CheckOptions = {
  routine: [
    { id: 'c1', name: '章节标题检查', description: '检查章节标题层级、格式是否符合规范。检查章节标题层级、格式是否符合规范。检查章节标题层级、格式是否符合规范。检查章节标题层级、格式是否符合规范。检查章节标题层级、格式是否符合规范。检查章节标题层级、格式是否符合规范。', category: 'routine' },
    { id: 'c2', name: '页眉页脚一致性', description: '确保文档所有页面的页眉页脚格式统一。', category: 'routine' },
    { id: 'c3', name: '标点符号规范', description: '检查标点符号是否使用正确，如中英文标点混用。', category: 'routine' },
    { id: 'c4', name: '编号连续性', description: '检查各级标题、图表、表格的编号是否连续无误。', category: 'routine' },
    { id: 'c5', name: '敏感信息检查', description: '扫描文档中是否包含公司或个人敏感信息。', category: 'routine' },
    { id: 'c6', name: '单位格式检查', description: '检查计量单位的写法是否标准，如 "KM" 应为 "km"。', category: 'routine' },
  ],
  special: [
    { id: 's1', name: '合同/协议类文档审查', description: '针对合同的关键条款、法律术语进行审查。', category: 'special' },
    { id: 's2', name: '技术方案审查', description: '审查技术方案中的参数、指标是否满足招标文件要求。', category: 'special' },
    { id: 's3', name: '投标函格式审查', description: '确保投标函的格式、签署、盖章等符合要求。', category: 'special' },
    { id: 's4', name: '项目管理类文档审查', description: '检查项目计划、进度报告等文档的完整性和一致性。', category: 'special' },
  ],
  advanced: [
    { id: 'a1', name: '错别字与语法模型', description: '使用高级语言模型检查文档中的错别字和语法错误。', category: 'advanced' },
    { id: 'a2', name: '表述一致性分析', description: '检查同一概念或术语在全文中的表述是否一致。', category: 'advanced' },
    { id: 'a3', name: '逻辑与呼应关系', description: '分析前后文是否存在逻辑矛盾或未呼应的承诺。', category: 'advanced' },
    { id: 'a4', name: '引用文件有效性', description: '检查引用的法规、标准、专利是否为最新有效版本。', category: 'advanced' },
  ]
};

const allChecks = [...checkOptions.routine, ...checkOptions.special, ...checkOptions.advanced];

// 模拟任务数据库
const tasks = new Map<string, QualityCheckTask>();

function createMockIssues(checkId: string, name: string): IssueDetail[] {
  if (Math.random() > 0.8) return []; // 20%的概率没有问题
  const count = Math.floor(Math.random() * 5) + 1;
  const issues: IssueDetail[] = [];
  for (let i = 0; i < count; i++) {
    issues.push({
      id: `issue_${checkId}_${i}`,
      type: ['suggestion', 'warning', 'error'][Math.floor(Math.random() * 3)] as 'suggestion' | 'warning' | 'error',
      original: `这是存在问题的原始文本片段，位于${name}。`,
      corrected: `这是修正后的文本建议，位于${name}。`,
      explanation: `这里是关于"${name}"问题的详细解释，说明了为什么需要修改以及修改的依据。`,
      location: `第 ${i + 2} 章 第 ${i + 1} 节`
    });
  }
  return issues;
}

export default [
  {
    url: '/api/quality-checks/options',
    method: 'get',
    response: () => {
      return {
        code: 200,
        msg: '成功',
        data: checkOptions,
      };
    },
  },
  {
    url: '/api/quality-checks/start',
    method: 'post',
    response: ({ body }: { body: { checkIds: string[] } }) => {
      const { checkIds } = body;
      const taskId = `task_${Date.now()}`;
      const initialProgress: ProgressItem[] = checkIds.map((id: string) => {
        const check = allChecks.find(c => c.id === id);
        return {
          checkId: id,
          name: check?.name || '未知检查',
          status: 'waiting',
          progress: 0,
        };
      });

      const task: QualityCheckTask = {
        taskId,
        status: 'progressing',
        selectedChecks: checkIds,
        progress: initialProgress,
        results: [],
      };

      tasks.set(taskId, task);

      // 模拟后台处理 - 改为顺序处理，避免竞态条件
      const processCheckQueue = async () => {
        for (const check of task.progress) {
          // 设置当前项为检查中
          check.status = 'checking';
          
          // 模拟进度增长
          await new Promise(resolve => {
            const progressInterval = setInterval(() => {
              check.progress += Math.floor(Math.random() * 30) + 20;
              if (check.progress >= 100) {
                check.progress = 100;
                clearInterval(progressInterval);
                resolve(null);
              }
            }, 400);
          });
          
          // 设置为已完成并生成结果
          check.status = 'completed';
          const issues = createMockIssues(check.checkId, check.name);
          const result: CheckResult = {
            checkId: check.checkId,
            name: check.name,
            issueCount: issues.length,
            issues: issues,
          };
          task.results.push(result);
        }
        
        // 所有项处理完毕
        task.status = 'finished';
      };
      
      processCheckQueue();

      return {
        code: 200,
        msg: '任务已启动',
        data: { taskId },
      };
    },
  },
  {
    url: '/api/quality-checks/task',
    method: 'get',
    response: ({ query }: { query: { taskId: string } }) => {
      const { taskId } = query;
      const task = tasks.get(taskId);

      if (!task) {
        return {
          code: 404,
          msg: '任务未找到',
          data: null,
        };
      }

      return {
        code: 200,
        msg: '成功',
        data: task,
      };
    },
  },
];
