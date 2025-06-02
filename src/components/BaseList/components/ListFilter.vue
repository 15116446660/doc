<template>
  <div class="list-filter">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      inline
      @submit.prevent="handleSubmit"
    >
      <!-- 简单过滤区域 -->
      <div class="simple-filter">
        <template v-for="item in simpleFilterItems" :key="item.field">
          <el-form-item
            :label="item.label"
            :prop="item.field"
            :rules="item.rules"
          >
            <!-- 输入框 -->
            <el-input
              v-if="item.type === 'input'"
              v-model="formData[item.field]"
              v-bind="item.props"
              :placeholder="item.placeholder || `请输入${item.label}`"
              clearable
              @change="handleFilterChange"
            />

            <!-- 选择框 -->
            <el-select
              v-else-if="item.type === 'select'"
              v-model="formData[item.field]"
              v-bind="item.props"
              :placeholder="item.placeholder || `请选择${item.label}`"
              clearable
              @change="handleFilterChange"
            >
              <el-option
                v-for="option in getOptions(item)"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>

            <!-- 日期选择器 -->
            <el-date-picker
              v-else-if="item.type === 'date'"
              v-model="formData[item.field]"
              v-bind="item.props"
              :type="item.dateType || 'date'"
              :placeholder="item.placeholder || `请选择${item.label}`"
              clearable
              @change="handleFilterChange"
            />

            <!-- 日期范围选择器 -->
            <el-date-picker
              v-else-if="item.type === 'daterange'"
              v-model="formData[item.field]"
              v-bind="item.props"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              clearable
              @change="handleFilterChange"
            />

            <!-- 数字输入框 -->
            <el-input-number
              v-else-if="item.type === 'number'"
              v-model="formData[item.field]"
              v-bind="item.props"
              :placeholder="item.placeholder || `请输入${item.label}`"
              clearable
              @change="handleFilterChange"
            />

            <!-- 复选框组 -->
            <el-checkbox-group
              v-else-if="item.type === 'checkbox'"
              v-model="formData[item.field]"
              v-bind="item.props"
              @change="handleFilterChange"
            >
              <el-checkbox
                v-for="option in getOptions(item)"
                :key="option.value"
                :label="option.value"
              >
                {{ option.label }}
              </el-checkbox>
            </el-checkbox-group>

            <!-- 单选框组 -->
            <el-radio-group
              v-else-if="item.type === 'radio'"
              v-model="formData[item.field]"
              v-bind="item.props"
              @change="handleFilterChange"
            >
              <el-radio
                v-for="option in getOptions(item)"
                :key="option.value"
                :label="option.value"
              >
                {{ option.label }}
              </el-radio>
            </el-radio-group>

            <!-- 级联选择器 -->
            <el-cascader
              v-else-if="item.type === 'cascader'"
              v-model="formData[item.field]"
              v-bind="item.props"
              :options="getOptions(item)"
              :placeholder="item.placeholder || `请选择${item.label}`"
              clearable
              @change="handleFilterChange"
            />
          </el-form-item>
        </template>

        <!-- 操作按钮 -->
        <div class="filter-actions">
          <el-button type="primary" @click="handleSubmit">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button 
            v-if="enableAdvancedFilter && hasAdvancedFields" 
            type="primary" 
            text
            @click="toggleAdvanced"
          >
            {{ isAdvanced ? '收起' : '展开' }}
            <el-icon>
              <ArrowUpIcon v-if="isAdvanced" />
              <ArrowDownIcon v-else />
            </el-icon>
          </el-button>
        </div>
      </div>

      <!-- 高级过滤区域 -->
      <div v-if="enableAdvancedFilter && hasAdvancedFields" class="advanced-filter" :class="{ active: isAdvanced }">
        <el-row :gutter="20">
          <el-col
            v-for="item in advancedFilterItems"
            :key="item.field"
            :span="item.colSpan || 8"
          >
            <el-form-item
              :label="item.label"
              :prop="item.field"
              :rules="item.rules"
            >
              <!-- 复用上面的表单项模板 -->
              <component
                :is="getFormComponent(item.type)"
                v-model="formData[item.field]"
                v-bind="getComponentProps(item)"
                @change="handleFilterChange"
              >
                <template v-if="hasOptions(item.type)">
                  <component
                    :is="getOptionComponent(item.type)"
                    v-for="option in getOptions(item)"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  />
                </template>
              </component>
            </el-form-item>
          </el-col>
        </el-row>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ArrowUp as ArrowUpIcon, ArrowDown as ArrowDownIcon } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import type { FilterFormItem, FilterChangeEvent } from '../types'

const props = withDefaults(defineProps<{
  filterConfig?: FilterFormItem[]
  enableAdvancedFilter?: boolean
}>(), {
  filterConfig: () => [],
  enableAdvancedFilter: false
})

const emit = defineEmits<{
  (e: 'filter-change', event: FilterChangeEvent): void
}>()

// 表单实例
const formRef = ref<FormInstance>()
// 表单数据
const formData = ref<Record<string, any>>({})
// 是否显示高级搜索
const isAdvanced = ref(false)

// 表单验证规则
const formRules = computed(() => {
  const rules: Record<string, any> = {}
  props.filterConfig.forEach(item => {
    if (item.rules) {
      rules[item.field] = item.rules
    }
  })
  return rules
})

// 简单过滤项
const simpleFilterItems = computed(() => {
  return props.filterConfig.filter(item => !item.advanced)
})

// 高级过滤项
const advancedFilterItems = computed(() => {
  return props.filterConfig.filter(item => item.advanced)
})

// 是否有高级字段
const hasAdvancedFields = computed(() => {
  return advancedFilterItems.value.length > 0
})

// 获取选项数据
const getOptions = (item: FilterFormItem) => {
  if (typeof item.options === 'function') {
    // 如果是函数，则调用获取数据
    item.options().then(options => {
      item.options = options
    })
    return []
  }
  return item.options || []
}

// 获取表单组件
const getFormComponent = (type: string) => {
  const componentMap: Record<string, string> = {
    input: 'el-input',
    select: 'el-select',
    date: 'el-date-picker',
    daterange: 'el-date-picker',
    number: 'el-input-number',
    checkbox: 'el-checkbox-group',
    radio: 'el-radio-group',
    cascader: 'el-cascader'
  }
  return componentMap[type]
}

// 获取选项组件
const getOptionComponent = (type: string) => {
  const componentMap: Record<string, string> = {
    select: 'el-option',
    checkbox: 'el-checkbox',
    radio: 'el-radio'
  }
  return componentMap[type]
}

// 判断是否有选项
const hasOptions = (type: string) => {
  return ['select', 'checkbox', 'radio'].includes(type)
}

// 获取组件属性
const getComponentProps = (item: FilterFormItem) => {
  const baseProps = {
    placeholder: item.placeholder || `请输入${item.label}`,
    clearable: true,
    ...item.props
  }

  const typeProps: Record<string, any> = {
    daterange: {
      type: 'daterange',
      rangeSeparator: '至',
      startPlaceholder: '开始日期',
      endPlaceholder: '结束日期'
    }
  }

  return {
    ...baseProps,
    ...(typeProps[item.type] || {})
  }
}

// 切换高级搜索
const toggleAdvanced = () => {
  isAdvanced.value = !isAdvanced.value
}

// 处理过滤条件变化
const handleFilterChange = () => {
  emit('filter-change', {
    values: formData.value,
    isAdvanced: isAdvanced.value
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    handleFilterChange()
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

// 重置表单
const handleReset = () => {
  if (!formRef.value) return
  
  formRef.value.resetFields()
  handleFilterChange()
}

// 初始化表单数据
onMounted(() => {
  const initialData: Record<string, any> = {}
  props.filterConfig.forEach(item => {
    if (item.defaultValue !== undefined) {
      initialData[item.field] = item.defaultValue
    }
  })
  formData.value = initialData
})
</script>

<style scoped>
.list-filter {
  margin-bottom: 16px;
}

.simple-filter {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: flex-start;
}

.filter-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.advanced-filter {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-light);
  display: none;
}

.advanced-filter.active {
  display: block;
}

:deep(.el-form-item) {
  margin-bottom: 0;
}

:deep(.el-form-item__content) {
  min-width: 200px;
}
</style> 