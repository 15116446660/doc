<template>
  <div class="project-hierarchy-page">
    <div class="page-header">
      <div class="header-left">
        <h2>项目层级管理</h2>
        <p class="header-desc">管理部门、品类、子品类和项目的四级层级结构</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
        <el-button @click="handleImport">
          <el-icon><Upload /></el-icon>
          导入
        </el-button>
        <el-button @click="showStatistics = true">
          <el-icon><DataAnalysis /></el-icon>
          统计
        </el-button>
      </div>
    </div>

    <div class="page-content">
      <div class="content-left">
        <!-- 项目层级树 -->
        <div class="tree-panel">
          <project-hierarchy-tree
            ref="treeRef"
            :draggable="true"
            :show-actions="true"
            @node-click="handleNodeClick"
            @node-select="handleNodeSelect"
            @refresh="handleTreeRefresh"
          />
        </div>
      </div>

      <div class="content-right" v-if="selectedNode">
        <!-- 节点详情面板 -->
        <div class="detail-panel">
          <div class="detail-header">
            <div class="detail-title">
              <el-icon class="title-icon" :style="{ color: selectedNode.color }">
                <component :is="getNodeIcon(selectedNode)" />
              </el-icon>
              <span class="title-text">{{ selectedNode.name }}</span>
              <el-tag :type="getNodeTypeTagType(selectedNode.nodeType)" size="small">
                {{ getNodeTypeText(selectedNode.nodeType) }}
              </el-tag>
            </div>
            <div class="detail-actions">
              <el-button size="small" @click="handleEditNode">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button size="small" @click="handleAddChild" v-if="canAddChild(selectedNode)">
                <el-icon><Plus /></el-icon>
                新建子项
              </el-button>
              <el-dropdown @command="handleMoreAction">
                <el-button size="small">
                  更多
                  <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="copy">
                      <el-icon><CopyDocument /></el-icon>
                      复制
                    </el-dropdown-item>
                    <el-dropdown-item command="move">
                      <el-icon><Rank /></el-icon>
                      移动
                    </el-dropdown-item>
                    <el-dropdown-item command="export">
                      <el-icon><Download /></el-icon>
                      导出
                    </el-dropdown-item>
                    <el-dropdown-item command="delete" divided v-if="canDelete(selectedNode)">
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <div class="detail-content">
            <!-- 基础信息 -->
            <div class="info-section">
              <h4>基础信息</h4>
              <div class="info-grid">
                <div class="info-item">
                  <label>名称：</label>
                  <span>{{ selectedNode.name }}</span>
                </div>
                <div class="info-item" v-if="selectedNode.code">
                  <label>编码：</label>
                  <span>{{ selectedNode.code }}</span>
                </div>
                <div class="info-item" v-if="selectedNode.description">
                  <label>描述：</label>
                  <span>{{ selectedNode.description }}</span>
                </div>
                <div class="info-item" v-if="selectedNode.managerName">
                  <label>负责人：</label>
                  <span>{{ selectedNode.managerName }}</span>
                </div>
                <div class="info-item">
                  <label>状态：</label>
                  <el-tag :type="selectedNode.enabled ? 'success' : 'danger'" size="small">
                    {{ selectedNode.enabled ? '启用' : '禁用' }}
                  </el-tag>
                </div>
                <div class="info-item">
                  <label>排序：</label>
                  <span>{{ selectedNode.sortOrder }}</span>
                </div>
              </div>
            </div>

            <!-- 项目特有信息 -->
            <div class="info-section" v-if="selectedNode.nodeType === 'project'">
              <h4>项目信息</h4>
              <div class="info-grid">
                <div class="info-item" v-if="selectedNode.projectStatus">
                  <label>状态：</label>
                  <el-tag :type="getProjectStatusType(selectedNode.projectStatus)" size="small">
                    {{ getProjectStatusText(selectedNode.projectStatus) }}
                  </el-tag>
                </div>
                <div class="info-item" v-if="selectedNode.projectPriority">
                  <label>优先级：</label>
                  <el-tag :type="getProjectPriorityType(selectedNode.projectPriority)" size="small">
                    {{ getProjectPriorityText(selectedNode.projectPriority) }}
                  </el-tag>
                </div>
                <div class="info-item" v-if="selectedNode.projectProgress !== undefined">
                  <label>进度：</label>
                  <el-progress :percentage="selectedNode.projectProgress" :width="100" />
                </div>
                <div class="info-item" v-if="selectedNode.projectBudget">
                  <label>预算：</label>
                  <span>¥{{ formatNumber(selectedNode.projectBudget) }}</span>
                </div>
                <div class="info-item" v-if="selectedNode.plannedStartTime">
                  <label>计划开始：</label>
                  <span>{{ formatDate(selectedNode.plannedStartTime) }}</span>
                </div>
                <div class="info-item" v-if="selectedNode.plannedEndTime">
                  <label>计划结束：</label>
                  <span>{{ formatDate(selectedNode.plannedEndTime) }}</span>
                </div>
                <div class="info-item" v-if="selectedNode.tags">
                  <label>标签：</label>
                  <div class="tags-container">
                    <el-tag
                      v-for="tag in selectedNode.tags.split(',')"
                      :key="tag"
                      size="small"
                      style="margin-right: 4px;"
                    >
                      {{ tag.trim() }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </div>

            <!-- 统计信息 -->
            <div class="info-section">
              <h4>统计信息</h4>
              <div class="stats-grid">
                <div class="stat-item">
                  <div class="stat-value">{{ nodeStats.childrenCount || 0 }}</div>
                  <div class="stat-label">子节点数量</div>
                </div>
                <div class="stat-item" v-if="nodeStats.projectCount !== undefined">
                  <div class="stat-value">{{ nodeStats.projectCount }}</div>
                  <div class="stat-label">项目数量</div>
                </div>
                <div class="stat-item" v-if="nodeStats.completedProjects !== undefined">
                  <div class="stat-value">{{ nodeStats.completedProjects }}</div>
                  <div class="stat-label">已完成项目</div>
                </div>
                <div class="stat-item" v-if="nodeStats.totalBudget !== undefined">
                  <div class="stat-value">¥{{ formatNumber(nodeStats.totalBudget) }}</div>
                  <div class="stat-label">总预算</div>
                </div>
              </div>
            </div>

            <!-- 时间信息 -->
            <div class="info-section">
              <h4>时间信息</h4>
              <div class="info-grid">
                <div class="info-item">
                  <label>创建时间：</label>
                  <span>{{ formatDate(selectedNode.createdAt) }}</span>
                </div>
                <div class="info-item">
                  <label>更新时间：</label>
                  <span>{{ formatDate(selectedNode.updatedAt) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div class="empty-state" v-else>
        <el-empty description="请选择一个节点查看详情" />
      </div>
    </div>

    <!-- 统计对话框 -->
    <el-dialog
      v-model="showStatistics"
      title="层级统计"
      width="800px"
    >
      <div class="statistics-content" v-loading="statisticsLoading">
        <div class="stats-overview">
          <div class="overview-item">
            <div class="overview-value">{{ statistics.totalDepartments || 0 }}</div>
            <div class="overview-label">部门总数</div>
          </div>
          <div class="overview-item">
            <div class="overview-value">{{ statistics.totalCategories || 0 }}</div>
            <div class="overview-label">品类总数</div>
          </div>
          <div class="overview-item">
            <div class="overview-value">{{ statistics.totalSubcategories || 0 }}</div>
            <div class="overview-label">子品类总数</div>
          </div>
          <div class="overview-item">
            <div class="overview-value">{{ statistics.totalProjects || 0 }}</div>
            <div class="overview-label">项目总数</div>
          </div>
        </div>

        <!-- 这里可以添加更多统计图表 -->
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Download,
  Upload,
  DataAnalysis,
  Edit,
  Plus,
  ArrowDown,
  CopyDocument,
  Rank,
  Delete,
  OfficeBuilding,
  Collection,
  Files,
  Document
} from '@element-plus/icons-vue'
import type { ProjectHierarchyTreeNode } from '@/api/project'
import { getHierarchyStatistics, getDepartmentStatistics } from '@/api/project'
import ProjectHierarchyTree from '@/components/ProjectHierarchyTree.vue'

// Refs
const treeRef = ref<InstanceType<typeof ProjectHierarchyTree>>()

// Reactive data
const selectedNode = ref<ProjectHierarchyTreeNode | null>(null)
const showStatistics = ref(false)
const statisticsLoading = ref(false)
const statistics = reactive({
  totalDepartments: 0,
  totalCategories: 0,
  totalSubcategories: 0,
  totalProjects: 0
})
const nodeStats = reactive({
  childrenCount: 0,
  projectCount: 0,
  completedProjects: 0,
  totalBudget: 0
})

// 生命周期
onMounted(() => {
  loadStatistics()
})

// 方法
const handleNodeClick = (node: ProjectHierarchyTreeNode) => {
  selectedNode.value = node
  loadNodeStats(node)
}

const handleNodeSelect = (node: ProjectHierarchyTreeNode) => {
  selectedNode.value = node
  loadNodeStats(node)
}

const handleTreeRefresh = () => {
  loadStatistics()
}

const handleEditNode = () => {
  if (!selectedNode.value) return
  // 触发编辑操作
  // 这里可以通过事件或者直接调用树组件的方法
}

const handleAddChild = () => {
  if (!selectedNode.value) return
  // 触发添加子节点操作
}

const handleMoreAction = (command: string) => {
  if (!selectedNode.value) return

  switch (command) {
    case 'copy':
      handleCopyNode()
      break
    case 'move':
      handleMoveNode()
      break
    case 'export':
      handleExportNode()
      break
    case 'delete':
      handleDeleteNode()
      break
  }
}

const handleCopyNode = () => {
  if (!selectedNode.value) return
  
  const copyData = {
    name: selectedNode.value.name,
    code: selectedNode.value.code,
    description: selectedNode.value.description,
    nodeType: selectedNode.value.nodeType
  }
  
  navigator.clipboard.writeText(JSON.stringify(copyData, null, 2))
    .then(() => {
      ElMessage.success('节点信息已复制到剪贴板')
    })
    .catch(() => {
      ElMessage.error('复制失败')
    })
}

const handleMoveNode = () => {
  ElMessage.info('移动功能开发中...')
}

const handleExportNode = () => {
  ElMessage.info('导出功能开发中...')
}

const handleDeleteNode = async () => {
  if (!selectedNode.value) return

  try {
    await ElMessageBox.confirm(
      `确定要删除${getNodeTypeText(selectedNode.value.nodeType)}"${selectedNode.value.name}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 调用删除API
    ElMessage.success('删除成功')
    selectedNode.value = null
    treeRef.value?.refresh()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

const handleImport = () => {
  ElMessage.info('导入功能开发中...')
}

const loadStatistics = async () => {
  try {
    statisticsLoading.value = true
    const response = await getHierarchyStatistics()
    Object.assign(statistics, response.data)
  } catch (error) {
    console.error('加载统计信息失败:', error)
  } finally {
    statisticsLoading.value = false
  }
}

const loadNodeStats = async (node: ProjectHierarchyTreeNode) => {
  try {
    // 根据节点类型加载相应的统计信息
    if (node.nodeType === 'department') {
      const response = await getDepartmentStatistics(node.id)
      Object.assign(nodeStats, response.data)
    } else {
      // 其他类型的统计信息
      Object.assign(nodeStats, {
        childrenCount: node.childrenCount || 0,
        projectCount: 0,
        completedProjects: 0,
        totalBudget: 0
      })
    }
  } catch (error) {
    console.error('加载节点统计信息失败:', error)
  }
}

// 工具方法
const getNodeIcon = (node: ProjectHierarchyTreeNode) => {
  switch (node.nodeType) {
    case 'department':
      return OfficeBuilding
    case 'category':
      return Collection
    case 'subcategory':
      return Files
    case 'project':
      return Document
    default:
      return Document
  }
}

const getNodeTypeText = (nodeType: string) => {
  switch (nodeType) {
    case 'department':
      return '部门'
    case 'category':
      return '品类'
    case 'subcategory':
      return '子品类'
    case 'project':
      return '项目'
    default:
      return '节点'
  }
}

const getNodeTypeTagType = (nodeType: string) => {
  switch (nodeType) {
    case 'department':
      return 'primary'
    case 'category':
      return 'success'
    case 'subcategory':
      return 'warning'
    case 'project':
      return 'info'
    default:
      return 'info'
  }
}

const getProjectStatusType = (status: string) => {
  switch (status) {
    case 'DRAFT':
      return 'info'
    case 'IN_PROGRESS':
      return 'warning'
    case 'COMPLETED':
      return 'success'
    case 'ARCHIVED':
      return 'danger'
    default:
      return 'info'
  }
}

const getProjectStatusText = (status: string) => {
  switch (status) {
    case 'DRAFT':
      return '草稿'
    case 'IN_PROGRESS':
      return '进行中'
    case 'COMPLETED':
      return '已完成'
    case 'ARCHIVED':
      return '已归档'
    default:
      return status
  }
}

const getProjectPriorityType = (priority: string) => {
  switch (priority) {
    case 'LOW':
      return 'info'
    case 'MEDIUM':
      return 'warning'
    case 'HIGH':
      return 'danger'
    case 'URGENT':
      return 'danger'
    default:
      return 'info'
  }
}

const getProjectPriorityText = (priority: string) => {
  switch (priority) {
    case 'LOW':
      return '低'
    case 'MEDIUM':
      return '中'
    case 'HIGH':
      return '高'
    case 'URGENT':
      return '紧急'
    default:
      return priority
  }
}

const canAddChild = (node: ProjectHierarchyTreeNode) => {
  return node.nodeType !== 'project'
}

const canDelete = (node: ProjectHierarchyTreeNode) => {
  return node.childrenCount === 0 || !node.childrenCount
}

const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}

const formatNumber = (num?: number) => {
  if (num === undefined || num === null) return '0'
  return num.toLocaleString()
}
</script>

<style scoped>
.project-hierarchy-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid var(--el-border-color);
}

.header-left h2 {
  margin: 0 0 4px 0;
  font-size: 20px;
  font-weight: 600;
}

.header-desc {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.header-right {
  display: flex;
  gap: 8px;
}

.page-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.content-left {
  width: 400px;
  border-right: 1px solid var(--el-border-color);
  display: flex;
  flex-direction: column;
}

.tree-panel {
  flex: 1;
  padding: 16px;
  overflow: hidden;
}

.content-right {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.detail-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid var(--el-border-color);
}

.detail-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  font-size: 20px;
}

.title-text {
  font-size: 18px;
  font-weight: 600;
}

.detail-actions {
  display: flex;
  gap: 8px;
}

.detail-content {
  flex: 1;
  padding: 24px;
  overflow: auto;
}

.info-section {
  margin-bottom: 32px;
}

.info-section h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-item label {
  min-width: 80px;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.info-item span {
  color: var(--el-text-color-primary);
  font-size: 14px;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 16px;
}

.stat-item {
  text-align: center;
  padding: 16px;
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--el-color-primary);
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.statistics-content {
  min-height: 300px;
}

.stats-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.overview-item {
  text-align: center;
  padding: 24px;
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
}

.overview-value {
  font-size: 32px;
  font-weight: 600;
  color: var(--el-color-primary);
  margin-bottom: 8px;
}

.overview-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}
</style>
