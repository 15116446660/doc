<template>
  <div class="project-list-container">
    <base-list
      ref="listRef"
      title="项目列表"
      :filter-config="filterConfig"
      :columns="columns"
      :enable-advanced-filter="true"
      :enable-view-switch="true"
      :request-api="getProjectList"
      :table-props="tableProps"
      :pagination-config="paginationConfig"
      @filter-change="handleFilterChange"
      @view-change="handleViewChange"
      @selection-change="handleSelectionChange"
    >
      <!-- 顶部工具栏插槽 -->
      <template #toolbar>
        <el-button type="primary" @click="handleCreateProject">
          <el-icon><plus /></el-icon>新建项目
        </el-button>
      </template>
      
      <!-- 项目名称自定义插槽 -->
      <template #project-name="{ row }">
        <div class="project-name">
          <el-avatar :size="32" :src="row.logo">
            {{ row?.title?.charAt(0) || 'P' }}
          </el-avatar>
          <span>{{ row.title || '未命名项目' }}</span>
        </div>
      </template>

      <!-- 状态自定义插槽 -->
      <template #status="{ row }">
        <el-tag :type="getStatusType(row.status)">{{ row.status || '未设置' }}</el-tag>
      </template>

      <!-- 风险自定义插槽 -->
      <template #risk="{ row }">
        <el-tag :type="getRiskType(row.risk)">{{ row.risk || '未设置' }}</el-tag>
      </template>

      <!-- 进度自定义插槽 -->
      <template #progress="{ row }">
        <el-progress :percentage="row.progress || 0" />
      </template>

      <!-- 负责人自定义插槽 -->
      <template #leader="{ row }">
        <div class="user-info">
          <el-avatar :size="24" :src="row.leaderAvatar">
            {{ row?.team?.charAt(0) || 'U' }}
          </el-avatar>
          <span>{{ row.team || '未分配' }}</span>
        </div>
      </template>

      <!-- 操作自定义插槽 -->
      <template #actions="{ row }">
        <el-button type="primary" text @click="handleEdit(row)">编辑</el-button>
        <el-button type="danger" text @click="handleDelete(row)">删除</el-button>
      </template>

      <!-- 卡片视图插槽 -->
      <template #card="{ item }">
        <project-card
          :project="item"
          @edit="handleEdit"
          @delete="handleDelete"
          @view="handleViewProject"
        />
      </template>
    </base-list>
    
    <!-- 创建项目对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="新建投标项目"
      width="60%"
      destroy-on-close
      class="create-project-dialog"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="120px"
        label-position="right"
        class="create-form"
      >
        <div class="tabs-container">
          <el-tabs v-model="activeTab" class="fixed-tabs">
            <!-- 基本信息 -->
            <el-tab-pane label="基本信息" name="basic">
              <div class="tab-content">
                <el-form-item label="项目名称" prop="title">
                  <el-input v-model="createForm.title" placeholder="请输入项目名称" />
                </el-form-item>
                
                <el-form-item label="项目分类" prop="categoryName">
                  <el-select v-model="createForm.categoryName" placeholder="请选择项目分类">
                    <el-option label="政府项目" value="政府项目" />
                    <el-option label="企业项目" value="企业项目" />
                    <el-option label="事业单位项目" value="事业单位项目" />
                    <el-option label="国际项目" value="国际项目" />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="项目描述" prop="description">
                  <el-input
                    v-model="createForm.description"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入项目描述"
                  />
                </el-form-item>
                
                <el-form-item label="招标方信息" prop="client">
                  <el-input v-model="createForm.client" placeholder="请输入招标方名称" />
                </el-form-item>
                
                <el-form-item label="招标方联系人">
                  <div class="contact-info-container">
                    <el-row :gutter="24">
                      <el-col :span="12">
                        <el-form-item class="nested-form-item" label="姓名" prop="clientContactName" label-width="60px">
                          <el-input v-model="createForm.clientContactName" placeholder="请输入联系人姓名" />
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item class="nested-form-item" label="职位" prop="clientContactTitle" label-width="60px">
                          <el-input v-model="createForm.clientContactTitle" placeholder="请输入联系人职位" />
                        </el-form-item>
                      </el-col>
                    </el-row>
                    <el-row :gutter="24">
                      <el-col :span="12">
                        <el-form-item class="nested-form-item" label="电话" prop="clientContactPhone" label-width="60px">
                          <el-input v-model="createForm.clientContactPhone" placeholder="请输入联系电话" />
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item class="nested-form-item" label="邮箱" prop="clientContactEmail" label-width="60px">
                          <el-input v-model="createForm.clientContactEmail" placeholder="请输入电子邮箱" />
                        </el-form-item>
                      </el-col>
                    </el-row>
                  </div>
                </el-form-item>
                
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="开始时间" prop="date">
                      <el-date-picker
                        v-model="createForm.date"
                        type="date"
                        placeholder="选择开始时间"
                        style="width: 100%"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="截止时间" prop="dueDate">
                      <el-date-picker
                        v-model="createForm.dueDate"
                        type="date"
                        placeholder="选择截止时间"
                        style="width: 100%"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
                
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="项目状态" prop="status">
                      <el-select v-model="createForm.status" placeholder="请选择项目状态">
                        <el-option label="进行中" value="进行中" />
                        <el-option label="待审核" value="待审核" />
                        <el-option label="已暂停" value="已暂停" />
                        <el-option label="已完成" value="已完成" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="风险等级" prop="risk">
                      <el-select v-model="createForm.risk" placeholder="请选择风险等级">
                        <el-option label="低" value="低" />
                        <el-option label="中" value="中" />
                        <el-option label="高" value="高" />
                        <el-option label="严重" value="严重" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                
                <el-form-item label="项目进度" prop="progress">
                  <el-slider v-model="createForm.progress" :step="5" show-input />
                </el-form-item>
              </div>
            </el-tab-pane>
            
            <!-- 团队信息 -->
            <el-tab-pane label="团队信息" name="team">
              <div class="tab-content">
                <el-form-item label="项目经理" prop="manager">
                  <el-select v-model="createForm.manager" placeholder="请选择项目经理">
                    <el-option label="张三" value="张三" />
                    <el-option label="李四" value="李四" />
                    <el-option label="王五" value="王五" />
                  </el-select>
                </el-form-item>
                
                <div class="team-section">
                  <div class="team-label">技术团队</div>
                  <div class="team-content">
                    <el-form-item label="技术负责人" prop="technicalLead">
                      <el-select v-model="createForm.technicalLead" placeholder="请选择技术负责人">
                        <el-option label="赵六" value="赵六" />
                        <el-option label="钱七" value="钱七" />
                        <el-option label="孙八" value="孙八" />
                      </el-select>
                    </el-form-item>
                    
                    <el-form-item label="技术团队成员" prop="technicalTeam">
                      <el-select
                        v-model="createForm.technicalTeam"
                        multiple
                        collapse-tags
                        placeholder="请选择技术团队成员"
                      >
                        <el-option label="赵六" value="赵六" />
                        <el-option label="钱七" value="钱七" />
                        <el-option label="孙八" value="孙八" />
                        <el-option label="周九" value="周九" />
                        <el-option label="吴十" value="吴十" />
                      </el-select>
                    </el-form-item>
                  </div>
                </div>
                
                <div class="team-section">
                  <div class="team-label">商务团队</div>
                  <div class="team-content">
                    <el-form-item label="商务负责人" prop="businessLead">
                      <el-select v-model="createForm.businessLead" placeholder="请选择商务负责人">
                        <el-option label="李一" value="李一" />
                        <el-option label="王二" value="王二" />
                        <el-option label="张三" value="张三" />
                      </el-select>
                    </el-form-item>
                    
                    <el-form-item label="商务团队成员" prop="businessTeam">
                      <el-select
                        v-model="createForm.businessTeam"
                        multiple
                        collapse-tags
                        placeholder="请选择商务团队成员"
                      >
                        <el-option label="李一" value="李一" />
                        <el-option label="王二" value="王二" />
                        <el-option label="张三" value="张三" />
                        <el-option label="赵四" value="赵四" />
                        <el-option label="钱五" value="钱五" />
                      </el-select>
                    </el-form-item>
                  </div>
                </div>
                
                <div class="team-section">
                  <div class="team-label">法务支持</div>
                  <div class="team-content">
                    <el-form-item label="法务负责人" prop="legalLead">
                      <el-select v-model="createForm.legalLead" placeholder="请选择法务负责人">
                        <el-option label="陈一" value="陈一" />
                        <el-option label="林二" value="林二" />
                      </el-select>
                    </el-form-item>
                    
                    <el-form-item label="法务团队成员" prop="legalTeam">
                      <el-select
                        v-model="createForm.legalTeam"
                        multiple
                        collapse-tags
                        placeholder="请选择法务团队成员"
                      >
                        <el-option label="陈一" value="陈一" />
                        <el-option label="林二" value="林二" />
                        <el-option label="黄三" value="黄三" />
                      </el-select>
                    </el-form-item>
                  </div>
                </div>
                
                <div class="team-section">
                  <div class="team-label">外部专家</div>
                  <div class="team-content">
                    <el-form-item label="外部专家" prop="externalExperts">
                      <el-select
                        v-model="createForm.externalExperts"
                        multiple
                        filterable
                        allow-create
                        default-first-option
                        placeholder="请输入或选择外部专家"
                      >
                        <el-option label="刘教授" value="刘教授" />
                        <el-option label="张工程师" value="张工程师" />
                        <el-option label="王顾问" value="王顾问" />
                      </el-select>
                    </el-form-item>
                  </div>
                </div>
              </div>
            </el-tab-pane>
            
            <!-- 商务信息 -->
            <el-tab-pane label="商务信息" name="business">
              <div class="tab-content">
                <el-form-item label="项目预算" prop="budget">
                  <el-input-number
                    v-model="createForm.budget"
                    :min="0"
                    :step="10000"
                    :precision="2"
                    style="width: 100%"
                  />
                </el-form-item>
                
                <el-form-item label="投标保证金" prop="deposit">
                  <el-input-number
                    v-model="createForm.deposit"
                    :min="0"
                    :step="1000"
                    :precision="2"
                    style="width: 100%"
                  />
                </el-form-item>
                
                <el-form-item label="资质要求" prop="qualifications">
                  <el-select
                    v-model="createForm.qualifications"
                    multiple
                    collapse-tags
                    placeholder="请选择所需资质"
                  >
                    <el-option label="营业执照" value="营业执照" />
                    <el-option label="资质证书" value="资质证书" />
                    <el-option label="纳税证明" value="纳税证明" />
                    <el-option label="社保缴纳证明" value="社保缴纳证明" />
                    <el-option label="银行资信证明" value="银行资信证明" />
                    <el-option label="无重大违法记录证明" value="无重大违法记录证明" />
                  </el-select>
                </el-form-item>
              </div>
            </el-tab-pane>
            
            <!-- 技术信息 -->
            <el-tab-pane label="技术信息" name="technical">
              <div class="tab-content">
                <el-form-item label="技术方案" prop="technicalPlan">
                  <el-input
                    v-model="createForm.technicalPlan"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入技术方案概述"
                  />
                </el-form-item>
                
                <el-form-item label="技术难点" prop="technicalChallenges">
                  <el-input
                    v-model="createForm.technicalChallenges"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入技术难点"
                  />
                </el-form-item>
                
                <el-form-item label="项目标签" prop="tags">
                  <el-select
                    v-model="createForm.tags"
                    multiple
                    filterable
                    allow-create
                    default-first-option
                    placeholder="请输入或选择标签"
                  >
                    <el-option
                      v-for="tag in tagOptions"
                      :key="tag"
                      :label="tag"
                      :value="tag"
                    />
                  </el-select>
                </el-form-item>
              </div>
            </el-tab-pane>
            
            <!-- 时间节点 -->
            <el-tab-pane label="时间节点" name="timeline">
              <div class="tab-content">
                <el-form-item label="标书购买截止" prop="bidPurchaseDeadline">
                  <el-date-picker
                    v-model="createForm.bidPurchaseDeadline"
                    type="datetime"
                    placeholder="选择标书购买截止时间"
                    style="width: 100%"
                  />
                </el-form-item>
                
                <el-form-item label="现场踏勘时间" prop="siteVisitTime">
                  <el-date-picker
                    v-model="createForm.siteVisitTime"
                    type="datetime"
                    placeholder="选择现场踏勘时间"
                    style="width: 100%"
                  />
                </el-form-item>
                
                <el-form-item label="答疑截止时间" prop="inquiryDeadline">
                  <el-date-picker
                    v-model="createForm.inquiryDeadline"
                    type="datetime"
                    placeholder="选择答疑截止时间"
                    style="width: 100%"
                  />
                </el-form-item>
                
                <el-form-item label="投标文件递交截止" prop="submissionDeadline">
                  <el-date-picker
                    v-model="createForm.submissionDeadline"
                    type="datetime"
                    placeholder="选择投标文件递交截止时间"
                    style="width: 100%"
                  />
                </el-form-item>
                
                <el-form-item label="开标时间" prop="openingTime">
                  <el-date-picker
                    v-model="createForm.openingTime"
                    type="datetime"
                    placeholder="选择开标时间"
                    style="width: 100%"
                  />
                </el-form-item>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-form>
      
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreateForm">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import BaseList from '@/components/BaseList/index.vue'
import ProjectCard from '@/components/ProjectCard.vue'
import type { FilterFormItem, TableColumn } from '@/components/BaseList/types'
import { getProjectList } from '@/api/project'

// 列表实例
const listRef = ref()

// 创建项目对话框
const createDialogVisible = ref(false)
const createFormRef = ref()
const activeTab = ref('basic')

// 创建表单数据
const createForm = reactive({
  title: '',
  categoryName: '',
  description: '',
  client: '',
  clientContactName: '',
  clientContactTitle: '',
  clientContactPhone: '',
  clientContactEmail: '',
  date: '',
  dueDate: '',
  status: '进行中',
  risk: '低',
  progress: 0,
  team: '',
  manager: '',
  teamMembers: [],
  technicalLead: '',
  technicalTeam: [],
  businessLead: '',
  businessTeam: [],
  legalLead: '',
  legalTeam: [],
  externalExperts: [],
  budget: 0,
  deposit: 0,
  qualifications: [],
  technicalPlan: '',
  technicalChallenges: '',
  tags: [],
  bidPurchaseDeadline: '',
  siteVisitTime: '',
  inquiryDeadline: '',
  submissionDeadline: '',
  openingTime: ''
})

// 表单验证规则
const createRules = {
  title: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  categoryName: [{ required: true, message: '请选择项目分类', trigger: 'change' }],
  date: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  dueDate: [{ required: true, message: '请选择截止时间', trigger: 'change' }],
  status: [{ required: true, message: '请选择项目状态', trigger: 'change' }],
  risk: [{ required: true, message: '请选择风险等级', trigger: 'change' }],
  manager: [{ required: true, message: '请选择项目经理', trigger: 'change' }],
  technicalLead: [{ required: true, message: '请选择技术负责人', trigger: 'change' }],
  businessLead: [{ required: true, message: '请选择商务负责人', trigger: 'change' }],
  clientContactPhone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  clientContactEmail: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 标签选项
const tagOptions = [
  '政府采购',
  '信息化建设',
  '软件开发',
  '系统集成',
  '硬件采购',
  '咨询服务',
  '工程建设',
  '运维服务'
]

// 表格列配置
const columns = ref<TableColumn[]>([
  {
    type: 'selection',
    width: 55,
    fixed: 'left'
  },
  {
    prop: 'title',
    label: '项目名称',
    minWidth: 200,
    fixed: 'left',
    slot: 'project-name'
  },
  {
    prop: 'categoryName',
    label: '分类',
    width: 120
  },
  {
    prop: 'status',
    label: '状态',
    width: 100,
    slot: 'status'
  },
  {
    prop: 'risk',
    label: '风险',
    width: 100,
    slot: 'risk'
  },
  {
    prop: 'progress',
    label: '进度',
    width: 200,
    slot: 'progress'
  },
  {
    prop: 'team',
    label: '负责人',
    width: 120,
    slot: 'leader'
  },
  {
    prop: 'date',
    label: '开始时间',
    width: 120
  },
  {
    prop: 'dueDate',
    label: '结束时间',
    width: 120
  },
  {
    prop: 'createTime',
    label: '创建时间',
    width: 180
  },
  {
    label: '操作',
    width: 150,
    fixed: 'right',
    slot: 'actions'
  }
])

// 过滤条件配置
const filterConfig = ref<FilterFormItem[]>([
  {
    type: 'input',
    field: 'keyword',
    label: '关键词',
    placeholder: '项目名称/负责人'
  },
  {
    type: 'select',
    field: 'status',
    label: '状态',
    options: [
      { label: '进行中', value: '进行中' },
      { label: '已完成', value: '已完成' },
      { label: '待审核', value: '待审核' },
      { label: '已暂停', value: '已暂停' }
    ]
  },
  {
    type: 'select',
    field: 'risk',
    label: '风险',
    options: [
      { label: '低', value: '低' },
      { label: '中', value: '中' },
      { label: '高', value: '高' },
      { label: '严重', value: '严重' }
    ]
  },
  {
    type: 'daterange',
    field: 'dateRange',
    label: '创建时间',
    advanced: true
  },
  {
    type: 'select',
    field: 'leader',
    label: '负责人',
    advanced: true,
    options: async () => {
      // 模拟异步获取负责人列表
      return [
        { label: '张三', value: '张三' },
        { label: '李四', value: '李四' },
        { label: '王五', value: '王五' }
      ]
    }
  }
])

// 表格属性
const tableProps = {
  border: true,
  stripe: true,
  'row-key': 'id',
  'header-cell-style': {
    background: 'var(--el-fill-color-light)',
    color: 'var(--el-text-color-primary)'
  }
}

// 分页配置
const paginationConfig = {
  pageSize: 10,
  pageSizes: [10, 20, 50, 100],
  layout: 'total, sizes, prev, pager, next, jumper'
}

// 获取状态类型
const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    '进行中': 'primary',
    '已完成': 'success',
    '待审核': 'warning',
    '已暂停': 'info'
  }
  return typeMap[status] || 'info'
}

// 获取风险类型
const getRiskType = (risk: string) => {
  const typeMap: Record<string, string> = {
    '低': 'info',
    '中': 'warning',
    '高': 'danger',
    '严重': 'danger'
  }
  return typeMap[risk] || 'info'
}

// 处理过滤条件变化
const handleFilterChange = (event: any) => {
  console.log('Filter changed:', event)
}

// 处理视图切换
const handleViewChange = (type: string) => {
  console.log('View changed:', type)
}

// 处理选择变化
const handleSelectionChange = (selection: any[]) => {
  console.log('Selection changed:', selection)
}

// 处理创建项目
const handleCreateProject = () => {
  createDialogVisible.value = true
}

// 提交创建表单
const submitCreateForm = async () => {
  try {
    await createFormRef.value.validate()
    
    // 这里可以添加创建项目的API调用
    console.log('创建项目:', createForm)
    
    // 模拟创建成功
    ElMessage.success('项目创建成功')
    createDialogVisible.value = false
    
    // 刷新列表
    listRef.value?.refresh()
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

// 处理编辑
const handleEdit = (row: any) => {
  console.log('Edit:', row)
}

// 处理删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除项目"${row.title}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    console.log('Delete:', row)
  } catch {
    // 用户取消删除
  }
}

// 处理查看项目
const handleViewProject = (project: any) => {
  console.log('View project:', project)
  // 这里可以实现跳转到项目详情页的逻辑
}
</script>

<style lang="scss" scoped>
.project-list-container {
  height: 100%;
  padding: 20px;
  background-color: var(--el-bg-color);
}

.project-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 创建表单样式 */
.create-form {
  height: 100%;
  
  .tabs-container {
    height: 100%;
    display: flex;
    flex-direction: column;
    
    .fixed-tabs {
      position: sticky;
      top: 0;
      z-index: 1;
      background-color: var(--el-bg-color);
      border-bottom: 1px solid var(--el-border-color-light);
      
      :deep(.el-tabs__header) {
        margin: 0;
        padding: 16px 20px 0;
      }
    }
    
    :deep(.el-tabs__content) {
      flex: 1;
      overflow-y: auto;
      padding: 20px;
    }
  }
}

.tab-content {
  padding-bottom: 20px;
}

.contact-info-container {
  background-color: var(--el-fill-color-light);
  border-radius: 4px;
  padding: 24px 20px;
  margin-bottom: 8px;
  
  .el-row {
    margin-bottom: 24px;
    
    &:last-child {
      margin-bottom: 0;
    }
  }
  
  .nested-form-item {
    margin-bottom: 0;
    
    :deep(.el-form-item__label) {
      font-weight: normal;
      color: var(--el-text-color-secondary);
    }
  }
}

/* 卡片样式 */
.project-card {
  height: 100%;
}

/* 团队样式 */
.team-section {
  position: relative;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  padding: 24px 16px 16px;
  margin-bottom: 24px;
}

.team-label {
  position: absolute;
  top: -12px;
  left: 16px;
  background-color: var(--el-bg-color);
  padding: 0 10px;
  font-size: 14px;
  font-weight: 600;
  color: var(--el-color-primary);
}

.team-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.header-left {
  display: flex;
  gap: 12px;
}

.project-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.project-info h3 {
  margin: 0;
  font-size: 16px;
  line-height: 24px;
}

.project-info p {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.card-content {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.content-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.content-row .label {
  color: var(--el-text-color-secondary);
  width: 60px;
}

.card-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--el-border-color-light);
  background-color: var(--el-fill-color-light);
}

.date-info {
  display: flex;
  justify-content: space-between;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.date-info p {
  margin: 0;
}

/* 创建项目对话框样式 */
.create-project-dialog {
  :deep(.el-dialog__body) {
    padding: 0;
    height: 65vh;
  }
}
</style>