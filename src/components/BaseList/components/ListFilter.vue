<template>
  <div class="list-filter">
    <el-form
      ref="formRef"
      :model="formData"
      :label-width="labelWidth"
      class="filter-form"
    >
      <el-row :gutter="16">
        <!-- 常规筛选项 -->
        <el-col
          v-for="item in normalFilterItems"
          :key="item.field"
          :span="item.colSpan || 6"
        >
          <el-form-item
            :label="item.label"
            :prop="item.field"
            :rules="item.rules"
            :label-width="item.labelWidth"
          >
            <div class="form-item-content" :style="getItemStyle(item)">
              <component
                :is="getFormItemComponent(item.type)"
                v-model="formData[item.field]"
                v-bind="getComponentProps(item)"
                @change="handleItemChange"
              >
                <template v-if="item.type === 'select'" #default>
                  <el-option
                    v-for="option in getOptions(item)"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  />
                </template>
              </component>
            </div>
          </el-form-item>
        </el-col>

        <!-- 操作按钮 -->
        <el-col :span="6">
          <el-form-item :label-width="0">
            <div class="filter-actions">
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
              <el-button
                v-if="enableAdvancedFilter && hasAdvancedItems"
                type="text"
                @click="toggleAdvanced"
              >
                {{ showAdvanced ? '收起' : '展开' }}
                <el-icon class="advanced-icon" :class="{ 'is-active': showAdvanced }">
                  <arrow-up />
                </el-icon>
              </el-button>
            </div>
          </el-form-item>
        </el-col>

        <!-- 高级筛选项 -->
        <template v-if="enableAdvancedFilter && hasAdvancedItems">
          <el-col :span="24" v-show="showAdvanced">
            <div class="advanced-filters">
              <el-row :gutter="16">
                <el-col
                  v-for="item in advancedFilterItems"
                  :key="item.field"
                  :span="item.colSpan || 6"
                >
                  <el-form-item
                    :label="item.label"
                    :prop="item.field"
                    :rules="item.rules"
                    :label-width="item.labelWidth"
                  >
                    <div class="form-item-content" :style="getItemStyle(item)">
                      <component
                        :is="getFormItemComponent(item.type)"
                        v-model="formData[item.field]"
                        v-bind="getComponentProps(item)"
                        @change="handleItemChange"
                      >
                        <template v-if="item.type === 'select'" #default>
                          <el-option
                            v-for="option in getOptions(item)"
                            :key="option.value"
                            :label="option.label"
                            :value="option.value"
                          />
                        </template>
                      </component>
                    </div>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </el-col>
        </template>
      </el-row>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ArrowUp } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import type { FilterFormItem } from '../types'

interface ListFilterProps {
  filterConfig: FilterFormItem[]
  enableAdvancedFilter?: boolean
  labelWidth?: string | number
  itemWidth?: string | number
}

interface ListFilterEmits {
  (e: 'filter-change', event: { values: Record<string, any>, isAdvanced: boolean }): void
}

const props = withDefaults(defineProps<ListFilterProps>(), {
  filterConfig: () => [],
  enableAdvancedFilter: false,
  labelWidth: '80px',
  itemWidth: '200px'
})

const emit = defineEmits<ListFilterEmits>()

// 表单实例
const formRef = ref<FormInstance>()
// 表单数据
const formData = ref<Record<string, any>>({})
// 是否显示高级筛选
const showAdvanced = ref(false)

// 常规筛选项
const normalFilterItems = computed(() => {
  return props.filterConfig.filter(item => !item.advanced)
})

// 高级筛选项
const advancedFilterItems = computed(() => {
  return props.filterConfig.filter(item => item.advanced)
})

// 是否有高级筛选项
const hasAdvancedItems = computed(() => {
  return advancedFilterItems.value.length > 0
})

// 获取表单项组件
const getFormItemComponent = (type: string): string => {
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
  return componentMap[type] || 'el-input'
}

// 获取组件属性
const getComponentProps = (item: FilterFormItem): Record<string, any> => {
  const baseProps = {
    placeholder: item.placeholder || `请输入${item.label}`,
    clearable: true,
    ...item.props
  }

  if (item.type === 'daterange') {
    return {
      ...baseProps,
      type: 'daterange',
      startPlaceholder: '开始日期',
      endPlaceholder: '结束日期',
      valueFormat: 'YYYY-MM-DD'
    }
  }

  if (item.type === 'date') {
    return {
      ...baseProps,
      type: 'date',
      valueFormat: 'YYYY-MM-DD'
    }
  }

  return baseProps
}

// 获取表单项样式
const getItemStyle = (item: FilterFormItem): Record<string, string> => {
  return {
    width: `${item.itemWidth || props.itemWidth}`.replace(/^(\d+)$/, '$1px')
  }
}

// 获取选项数据
const getOptions = (item: FilterFormItem): { label: string; value: any }[] => {
  if (typeof item.options === 'function') {
    // 如果是函数，则调用获取数据
    item.options().then(options => {
      // 更新选项数据
      if (Array.isArray(options)) {
        item.options = options;
      }
    });
    return [];
  }
  return Array.isArray(item.options) ? item.options : [];
}

// 处理表单项变化
const handleItemChange = (): void => {
  emit('filter-change', {
    values: formData.value,
    isAdvanced: showAdvanced.value
  })
}

// 处理查询
const handleSearch = async (): Promise<void> => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    emit('filter-change', {
      values: formData.value,
      isAdvanced: showAdvanced.value
    })
  } catch (error) {
    console.error('Form validation failed:', error)
  }
}

// 处理重置
const handleReset = (): void => {
  if (!formRef.value) return
  
  formRef.value.resetFields()
  emit('filter-change', {
    values: formData.value,
    isAdvanced: showAdvanced.value
  })
}

// 切换高级筛选
const toggleAdvanced = (): void => {
  showAdvanced.value = !showAdvanced.value
}

// 初始化表单数据
onMounted(() => {
  // 初始化表单数据
  props.filterConfig.forEach(item => {
    formData.value[item.field] = item.defaultValue !== undefined ? item.defaultValue : null
  })
})
</script>

<style scoped>
.list-filter {
  margin-bottom: 16px;
}

.filter-form {
  background-color: var(--el-bg-color);
  padding: 16px;
  border-radius: 4px;
}

.form-item-content {
  width: 100%;
}

.filter-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 32px; /* 与表单项保持一致的高度 */
}

.advanced-filters {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed var(--el-border-color);
}

.advanced-icon {
  transition: transform 0.3s;
  margin-left: 4px;
}

.advanced-icon.is-active {
  transform: rotate(180deg);
}

:deep(.el-form-item) {
  margin-bottom: 16px;
}

:deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

/* 确保按钮表单项没有底部边距 */
:deep(.filter-actions .el-form-item) {
  margin-bottom: 0;
}

/* 调整按钮组的垂直对齐 */
:deep(.filter-actions .el-button) {
  margin-top: 1px;
}
</style> 