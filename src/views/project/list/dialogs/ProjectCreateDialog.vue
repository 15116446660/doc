<template>
  <el-dialog
    v-model="dialogVisible"
    title="新建投标项目"
    width="60%"
    destroy-on-close
    class="create-project-dialog"
    @close="handleClose"
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
                  <el-option
                    v-for="category in formOptions.projectCategories"
                    :key="category.value"
                    :label="category.label"
                    :value="category.value"
                  />
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
                      <el-option
                        v-for="status in formOptions.statusOptions"
                        :key="status.value"
                        :label="status.label"
                        :value="status.value"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="风险等级" prop="risk">
                    <el-select v-model="createForm.risk" placeholder="请选择风险等级">
                      <el-option
                        v-for="risk in formOptions.riskLevels"
                        :key="risk.value"
                        :label="risk.label"
                        :value="risk.value"
                      />
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
                  <el-option
                    v-for="manager in formOptions.managers"
                    :key="manager.value"
                    :label="manager.label"
                    :value="manager.value"
                  />
                </el-select>
              </el-form-item>
              
              <div class="team-section">
                <div class="team-label">技术团队</div>
                <div class="team-content">
                  <el-form-item label="技术负责人" prop="technicalLead">
                    <el-select v-model="createForm.technicalLead" placeholder="请选择技术负责人">
                      <el-option
                        v-for="lead in formOptions.technicalTeam.leads"
                        :key="lead.value"
                        :label="lead.label"
                        :value="lead.value"
                      />
                    </el-select>
                  </el-form-item>
                  
                  <el-form-item label="技术团队成员" prop="technicalTeam">
                    <el-select
                      v-model="createForm.technicalTeam"
                      multiple
                      collapse-tags
                      placeholder="请选择技术团队成员"
                    >
                      <el-option
                        v-for="member in formOptions.technicalTeam.members"
                        :key="member.value"
                        :label="member.label"
                        :value="member.value"
                      />
                    </el-select>
                  </el-form-item>
                </div>
              </div>
              
              <div class="team-section">
                <div class="team-label">商务团队</div>
                <div class="team-content">
                  <el-form-item label="商务负责人" prop="businessLead">
                    <el-select v-model="createForm.businessLead" placeholder="请选择商务负责人">
                      <el-option
                        v-for="lead in formOptions.businessTeam.leads"
                        :key="lead.value"
                        :label="lead.label"
                        :value="lead.value"
                      />
                    </el-select>
                  </el-form-item>
                  
                  <el-form-item label="商务团队成员" prop="businessTeam">
                    <el-select
                      v-model="createForm.businessTeam"
                      multiple
                      collapse-tags
                      placeholder="请选择商务团队成员"
                    >
                      <el-option
                        v-for="member in formOptions.businessTeam.members"
                        :key="member.value"
                        :label="member.label"
                        :value="member.value"
                      />
                    </el-select>
                  </el-form-item>
                </div>
              </div>
              
              <div class="team-section">
                <div class="team-label">法务支持</div>
                <div class="team-content">
                  <el-form-item label="法务负责人" prop="legalLead">
                    <el-select v-model="createForm.legalLead" placeholder="请选择法务负责人">
                      <el-option
                        v-for="lead in formOptions.legalTeam.leads"
                        :key="lead.value"
                        :label="lead.label"
                        :value="lead.value"
                      />
                    </el-select>
                  </el-form-item>
                  
                  <el-form-item label="法务团队成员" prop="legalTeam">
                    <el-select
                      v-model="createForm.legalTeam"
                      multiple
                      collapse-tags
                      placeholder="请选择法务团队成员"
                    >
                      <el-option
                        v-for="member in formOptions.legalTeam.members"
                        :key="member.value"
                        :label="member.label"
                        :value="member.value"
                      />
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
                      <el-option
                        v-for="expert in formOptions.externalExperts"
                        :key="expert.value"
                        :label="expert.label"
                        :value="expert.value"
                      />
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
                  <el-option
                    v-for="qual in formOptions.qualifications"
                    :key="qual.value"
                    :label="qual.label"
                    :value="qual.value"
                  />
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
                    v-for="tag in formOptions.projectTags"
                    :key="tag.value"
                    :label="tag.label"
                    :value="tag.value"
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
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确认</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import dialogInstance from '@/hooks/useDialog'
import {
  getProjectCategoryOptions,
  getProjectStatusOptions,
  getProjectRiskOptions,
  getTeamMemberOptions,
  getTechnicalTeamOptions,
  getBusinessTeamOptions,
  getLegalTeamOptions,
  getExternalExpertOptions,
  getQualificationOptions,
  getProjectTagOptions,
  createProject
} from '@/api/project'

// 标签页活动标签
const activeTab = ref('basic')

// 对话框可见性
const dialogVisible = ref(false)
const createFormRef = ref()

// 表单选项数据
const formOptions = ref({
  projectCategories: [],
  statusOptions: [],
  riskLevels: [],
  managers: [],
  technicalTeam: {
    leads: [],
    members: []
  },
  businessTeam: {
    leads: [],
    members: []
  },
  legalTeam: {
    leads: [],
    members: []
  },
  externalExperts: [],
  qualifications: [],
  projectTags: []
})

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

// 获取表单选项数据
const fetchFormOptions = async () => {
  try {
    // 并行请求所有选项数据
    const [
      categoryRes,
      statusRes,
      riskRes,
      teamRes,
      technicalRes,
      businessRes,
      legalRes,
      expertRes,
      qualificationRes,
      tagRes
    ] = await Promise.all([
      getProjectCategoryOptions(),
      getProjectStatusOptions(),
      getProjectRiskOptions(),
      getTeamMemberOptions(),
      getTechnicalTeamOptions(),
      getBusinessTeamOptions(),
      getLegalTeamOptions(),
      getExternalExpertOptions(),
      getQualificationOptions(),
      getProjectTagOptions()
    ])

    // 更新表单选项数据
    formOptions.value = {
      projectCategories: categoryRes,
      statusOptions: statusRes,
      riskLevels: riskRes,
      managers: teamRes,
      technicalTeam: technicalRes,
      businessTeam: businessRes,
      legalTeam: legalRes,
      externalExperts: expertRes,
      qualifications: qualificationRes,
      projectTags: tagRes
    }
  } catch (error) {
    console.error('获取表单选项失败:', error)
    ElMessage.error('获取表单选项失败')
  }
}

// 组件挂载时获取表单选项
onMounted(() => {
  fetchFormOptions()
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

// 监听对话框状态
watch(() => dialogInstance.isVisible('projectCreate'), (visible) => {
  dialogVisible.value = visible
  if (visible) {
    // 如果有传递的属性，可以在这里处理
    const props = dialogInstance.getProps('projectCreate')
    if (props.initialData) {
      Object.assign(createForm, props.initialData)
    }
  }
})

// 处理取消
const handleCancel = () => {
  dialogInstance.close('projectCreate')
}

// 处理关闭
const handleClose = () => {
  dialogInstance.close('projectCreate')
}

// 处理提交
const handleSubmit = async () => {
  try {
    await createFormRef.value.validate()
    
    // 调用创建项目API
    const response = await createProject(createForm)
    
    if (response.data.code === 200) {
      ElMessage.success(response.data.message || '项目创建成功')
      
      // 触发回调事件
      const result = dialogInstance.emit('projectCreate', 'submit', response.data.data)
      
      // 关闭对话框
      dialogInstance.close('projectCreate')
      
      // 触发刷新事件
      window.dispatchEvent(new CustomEvent('project-list-refresh'))
      
      return result
    } else {
      throw new Error(response.data.message || '创建失败')
    }
  } catch (error: any) {
    console.error('创建项目失败:', error)
    ElMessage.error(error.message || '创建失败')
    // 触发错误回调
    dialogInstance.emit('projectCreate', 'error', error)
  }
}

// 暴露方法
defineExpose({
  open: () => dialogInstance.open('projectCreate')
})
</script>

<style lang="scss" scoped>
/* 创建项目对话框样式 */
.create-project-dialog {
  :deep(.el-dialog__body) {
    padding: 0;
    height: 65vh;
  }
}

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
</style> 