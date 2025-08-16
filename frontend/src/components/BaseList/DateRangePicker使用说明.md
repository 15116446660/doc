# BaseList DateRangePicker 使用说明

## 概述

`BaseList` 组件中的 `DateRangePicker`（`daterange` 类型）是一个功能完整的日期范围选择器，支持起止时间的初始化和 `filter-change` 事件处理。

## 核心特性

### 1. 初始化支持

支持多种默认值配置方式：

```typescript
// 方式1：数组格式
{
  type: 'daterange',
  field: 'createTime',
  label: '创建时间',
  defaultValue: ['2024-01-01', '2024-12-31']
}

// 方式2：对象格式
{
  type: 'daterange',
  field: 'createTime',
  label: '创建时间',
  defaultValue: { 
    start: '2024-01-01', 
    end: '2024-12-31' 
  }
}
```

### 2. 事件处理

`filter-change` 事件会返回处理后的数据，包含：

- **原始数组值**：`createTime: ['2024-01-01', '2024-12-31']`
- **分离的起止时间**：`createTimeStart: '2024-01-01'`, `createTimeEnd: '2024-12-31'`

### 3. 自定义配置

支持丰富的自定义配置：

```typescript
{
  type: 'daterange',
  field: 'createTime',
  label: '创建时间',
  props: {
    // 自定义占位符
    startPlaceholder: '开始创建时间',
    endPlaceholder: '结束创建时间',
    
    // 自定义日期格式
    valueFormat: 'YYYY-MM-DD',
    format: 'YYYY-MM-DD',
    
    // 自定义快捷选项
    shortcuts: [
      {
        text: '今天',
        value: () => {
          const today = new Date()
          return [today, today]
        }
      },
      {
        text: '最近7天',
        value: () => {
          const end = new Date()
          const start = new Date()
          start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
          return [start, end]
        }
      }
    ]
  }
}
```

## 完整配置示例

```typescript
const filterConfig: FilterFormItem[] = [
  {
    type: 'daterange',
    field: 'createTime',
    label: '创建时间',
    placeholder: '请选择创建时间范围',
    defaultValue: ['2024-01-01', '2024-12-31'],
    props: {
      startPlaceholder: '开始创建时间',
      endPlaceholder: '结束创建时间',
      valueFormat: 'YYYY-MM-DD',
      format: 'YYYY-MM-DD',
      shortcuts: [
        {
          text: '今天',
          value: () => {
            const today = new Date()
            return [today, today]
          }
        },
        {
          text: '昨天',
          value: () => {
            const yesterday = new Date()
            yesterday.setTime(yesterday.getTime() - 3600 * 1000 * 24)
            return [yesterday, yesterday]
          }
        },
        {
          text: '最近7天',
          value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
            return [start, end]
          }
        },
        {
          text: '最近30天',
          value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
            return [start, end]
          }
        }
      ]
    }
  }
]
```

## 事件处理示例

```typescript
const handleFilterChange = (event: any) => {
  console.log('过滤条件变化:', event)
  
  // event.values 包含处理后的数据
  const { 
    createTime,           // 原始数组值
    createTimeStart,      // 开始时间
    createTimeEnd,        // 结束时间
    keyword, 
    status 
  } = event.values
  
  // 构建API请求参数
  const apiParams = {
    startDate: createTimeStart,
    endDate: createTimeEnd,
    keyword,
    status,
    page: 1,
    pageSize: 10
  }
  
  // 调用API
  fetchData(apiParams)
}
```

## 实现原理

### 1. 组件映射

`daterange` 类型映射到 `el-date-picker` 组件，并设置 `type: 'daterange'`。

### 2. 数据处理

通过 `processFilterValues` 函数处理日期范围数据：

```typescript
const processFilterValues = (values: Record<string, any>): Record<string, any> => {
  const processed: Record<string, any> = {}
  
  Object.keys(values).forEach(key => {
    const value = values[key]
    const filterItem = props.filterConfig.find(item => item.field === key)
    
    if (filterItem?.type === 'daterange' && Array.isArray(value) && value.length === 2) {
      // 日期范围类型，分离起止时间
      const [startDate, endDate] = value
      processed[`${key}Start`] = startDate
      processed[`${key}End`] = endDate
      // 保留原始数组值
      processed[key] = value
    } else {
      // 其他类型直接赋值
      processed[key] = value
    }
  })
  
  return processed
}
```

### 3. 初始化处理

在 `onMounted` 中特殊处理日期范围的初始化：

```typescript
onMounted(() => {
  props.filterConfig.forEach(item => {
    if (item.type === 'daterange') {
      if (item.defaultValue !== undefined) {
        if (Array.isArray(item.defaultValue)) {
          // 数组格式直接使用
          formData.value[item.field] = item.defaultValue
        } else if (typeof item.defaultValue === 'object' && item.defaultValue.start && item.defaultValue.end) {
          // 对象格式转换为数组
          formData.value[item.field] = [item.defaultValue.start, item.defaultValue.end]
        } else {
          formData.value[item.field] = []
        }
      } else {
        formData.value[item.field] = []
      }
    } else {
      formData.value[item.field] = item.defaultValue !== undefined ? item.defaultValue : null
    }
  })
})
```

## 样式特性

- 自动设置双倍宽度以适应日期范围选择器
- 支持栅格布局的列宽配置
- 响应式设计，适配不同屏幕尺寸

## 注意事项

1. **数据格式**：确保后端API能够正确处理分离的起止时间参数
2. **时区处理**：日期格式统一使用 `YYYY-MM-DD`，避免时区问题
3. **空值处理**：当用户清空日期范围时，会传递空数组或 `null` 值
4. **验证规则**：可以添加自定义验证规则确保日期范围的合理性

## 最佳实践

1. **提供快捷选项**：为用户提供常用的日期范围快捷选择
2. **合理的默认值**：设置合理的默认日期范围，提升用户体验
3. **清晰的标签**：使用清晰的标签和占位符文本
4. **错误处理**：在API调用时正确处理日期参数的空值情况 