<template>
  <div class="markdown-message" :class="{ 'thinking-mode': showThinking }">
    <!-- 思考内容区域 -->
    <div v-if="thinking" class="thinking-container">
      <div class="thinking-header" @click="toggleThinking">
        <el-icon :class="{ 'is-active': showThinking }">
          <component :is="showThinking ? 'ArrowDown' : 'ArrowRight'" />
        </el-icon>
        <span>思考过程</span>
      </div>
      <div v-show="showThinking" class="thinking-content">
        <div v-html="renderedThinking"></div>
      </div>
    </div>
    
    <!-- 主要内容区域 -->
    <div class="markdown-content" v-html="renderedContent"></div>
    
    <!-- 复制按钮 -->
    <div class="copy-button" v-if="content && content.trim().length > 0">
      <el-button
        size="small"
        type="text"
        @click="copyContent"
        :title="copied ? '已复制' : '复制内容'"
      >
        <el-icon>
          <component :is="copied ? 'Check' : 'DocumentCopy'" />
        </el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

// 定义组件属性
const props = defineProps<{
  content: string
  thinking?: string
}>()

// 状态
const copied = ref(false)
const showThinking = ref(false)

// 创建Markdown渲染器
const md: MarkdownIt = new MarkdownIt({
  html: false,
  breaks: true,
  linkify: true,
  typographer: true,
  highlight: function (str: string, lang: string): string {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return `<pre class="hljs"><code>${hljs.highlight(str, { language: lang, ignoreIllegals: true }).value}</code></pre>`
      } catch (__) {
        // 如果高亮失败，使用普通渲染
      }
    }
    return `<pre class="hljs"><code>${md.utils.escapeHtml(str)}</code></pre>`
  }
})

// 渲染Markdown内容
const renderedContent = computed(() => {
  if (!props.content) return ''
  return md.render(props.content)
})

// 渲染思考内容
const renderedThinking = computed(() => {
  if (!props.thinking) return ''
  return md.render(props.thinking)
})

// 复制内容到剪贴板
const copyContent = () => {
  navigator.clipboard.writeText(props.content).then(() => {
    copied.value = true
    ElMessage.success('内容已复制到剪贴板')
    
    // 3秒后重置状态
    setTimeout(() => {
      copied.value = false
    }, 3000)
  }).catch(err => {
    console.error('复制失败:', err)
    ElMessage.error('复制失败')
  })
}

// 切换思考内容显示状态
const toggleThinking = () => {
  showThinking.value = !showThinking.value
}

// 监听内容变化，重置复制状态
watch(() => props.content, () => {
  copied.value = false
  // 异步更新DOM后执行
  setTimeout(() => {
    addCopyButtonToCodeBlocks()
    addCopyButtonToTables()
  }, 100)
})

const addCopyButtonToCodeBlocks = () => {
  const codeBlocks = document.querySelectorAll('.markdown-content pre.hljs')
  codeBlocks.forEach(block => {
    if (block.querySelector('.code-copy-button')) return
      
    const copyBtn = document.createElement('button')
    copyBtn.className = 'code-copy-button'
    copyBtn.innerHTML = '复制'
    copyBtn.addEventListener('click', (e) => {
      e.stopPropagation()
      const code = block.querySelector('code')
      if (code) {
        navigator.clipboard.writeText(code.textContent || '')
          .then(() => {
            copyBtn.innerHTML = '已复制'
            ElMessage.success('代码已复制')
            setTimeout(() => {
              copyBtn.innerHTML = '复制'
            }, 2000)
          })
          .catch(err => {
            console.error('复制代码失败:', err)
            ElMessage.error('复制代码失败')
          })
      }
    })
    
    block.appendChild(copyBtn)
  })
}

const addCopyButtonToTables = () => {
  const tables = document.querySelectorAll('.markdown-content table')
  tables.forEach(table => {
    // 如果表格已经在容器里，说明已经处理过
    if (table.parentElement?.classList.contains('table-container')) return

    const container = document.createElement('div')
    container.className = 'table-container'
    
    // 将表格移动到新容器中
    table.parentNode?.insertBefore(container, table)
    container.appendChild(table)
    
    const copyBtn = document.createElement('button')
    copyBtn.className = 'table-copy-button'
    copyBtn.innerHTML = '复制表格'
    copyBtn.addEventListener('click', (e) => {
      e.stopPropagation()
      const rows = table.querySelectorAll('tr')
      const tsv = Array.from(rows).map(row => {
        const cells = row.querySelectorAll('th, td')
        return Array.from(cells).map(cell => cell.textContent?.trim() || '').join('\t')
      }).join('\n')
      
      navigator.clipboard.writeText(tsv)
        .then(() => {
          copyBtn.innerHTML = '已复制'
          ElMessage.success('表格内容已复制 (TSV格式)')
          setTimeout(() => {
            copyBtn.innerHTML = '复制表格'
          }, 2000)
        })
        .catch(err => {
          console.error('复制表格失败:', err)
          ElMessage.error('复制表格失败')
        })
    })
    
    container.appendChild(copyBtn)
  })
}

// 组件挂载后处理
onMounted(() => {
  setTimeout(() => {
    addCopyButtonToCodeBlocks()
    addCopyButtonToTables()
  }, 100)
})
</script>

<style scoped>
.markdown-message {
  position: relative;
  width: 100%;
}

.markdown-content {
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.thinking-container {
  margin-bottom: 12px;
  border-radius: 4px;
  background-color: #f9f9f9;
  overflow: hidden;
}

.thinking-header {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background-color: #f2f2f2;
  cursor: pointer;
  user-select: none;
}

.thinking-header .el-icon {
  margin-right: 8px;
  transition: transform 0.3s;
}

.thinking-header .is-active {
  transform: rotate(0deg);
}

.thinking-content {
  padding: 12px;
  font-size: 13px;
  color: #666;
  border-top: 1px solid #eee;
}

.copy-button {
  position: absolute;
  top: 0;
  right: 0;
  opacity: 0;
  transition: opacity 0.2s;
}

.markdown-message:hover .copy-button {
  opacity: 1;
}

/* 深度选择器，修改Markdown渲染内容的样式 */
:deep(.markdown-content) {
  /* 标题样式 */
  h1, h2, h3, h4, h5, h6 {
    margin-top: 1.5em;
    margin-bottom: 1em;
    font-weight: 600;
  }
  
  h1 {
    font-size: 1.8em;
    border-bottom: 1px solid #eee;
    padding-bottom: 0.3em;
  }
  
  h2 {
    font-size: 1.5em;
    border-bottom: 1px solid #eee;
    padding-bottom: 0.3em;
  }
  
  h3 {
    font-size: 1.3em;
  }
  
  /* 段落样式 */
  p {
    margin: 1em 0;
  }
  
  /* 列表样式 */
  ul, ol {
    padding-left: 2em;
    margin: 1em 0;
  }
  
  /* 代码块样式 */
  pre {
    position: relative;
    margin: 1em 0;
    padding: 1em;
    border-radius: 4px;
    background-color: #f6f8fa;
    overflow: auto;
  }
  
  /* 行内代码样式 */
  code:not(pre code) {
    padding: 0.2em 0.4em;
    margin: 0;
    font-size: 0.85em;
    background-color: rgba(175, 184, 193, 0.2);
    border-radius: 3px;
  }
  
  /* 表格样式 */
  table {
    border-collapse: collapse;
    width: 100%;
    margin: 1em 0;
  }
  
  table th, table td {
    border: 1px solid #dfe2e5;
    padding: 6px 13px;
  }
  
  table th {
    background-color: #f6f8fa;
    font-weight: 600;
  }
  
  /* 引用样式 */
  blockquote {
    margin: 1em 0;
    padding: 0 1em;
    color: #6a737d;
    border-left: 0.25em solid #dfe2e5;
  }
  
  /* 水平线样式 */
  hr {
    height: 0.25em;
    padding: 0;
    margin: 24px 0;
    background-color: #e1e4e8;
    border: 0;
  }
  
  /* 链接样式 */
  a {
    color: #0366d6;
    text-decoration: none;
  }
  
  a:hover {
    text-decoration: underline;
  }
}

/* 代码块复制按钮 */
:deep(.code-copy-button) {
  position: absolute;
  top: 5px;
  right: 5px;
  padding: 4px 8px;
  font-size: 12px;
  color: #666;
  background-color: #f6f8fa;
  border: 1px solid #ddd;
  border-radius: 3px;
  opacity: 0;
  transition: opacity 0.2s;
  cursor: pointer;
}

:deep(pre:hover .code-copy-button) {
  opacity: 1;
}

:deep(.code-copy-button:hover) {
  background-color: #e6e6e6;
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .thinking-container {
    background-color: #2d2d2d;
  }
  
  .thinking-header {
    background-color: #3a3a3a;
  }
  
  .thinking-content {
    color: #bbb;
    border-top: 1px solid #444;
  }
  
  :deep(.markdown-content) {
    h1, h2 {
      border-bottom: 1px solid #444;
    }
    
    pre {
      background-color: #2d2d2d;
    }
    
    code:not(pre code) {
      background-color: rgba(110, 118, 129, 0.4);
    }
    
    table th, table td {
      border: 1px solid #444;
    }
    
    table th {
      background-color: #3a3a3a;
    }
    
    blockquote {
      color: #9e9e9e;
      border-left: 0.25em solid #444;
    }
    
    hr {
      background-color: #444;
    }
    
    a {
      color: #58a6ff;
    }
  }
  
  :deep(.code-copy-button) {
    background-color: #3a3a3a;
    border: 1px solid #555;
    color: #ddd;
  }
  
  :deep(.code-copy-button:hover) {
    background-color: #4a4a4a;
  }
}
</style>

<style>
/* 全局样式，用于动态添加的元素 */
.table-container {
  position: relative;
  margin: 1em 0;
}

.table-container:hover .table-copy-button {
  opacity: 1;
}

.table-copy-button {
  position: absolute;
  top: 8px;
  right: 8px;
  opacity: 0;
  transition: opacity 0.2s;
  background-color: #f0f0f0;
  border: 1px solid #ccc;
  border-radius: 4px;
  padding: 2px 8px;
  font-size: 12px;
  cursor: pointer;
  z-index: 10;
}

.table-copy-button:hover {
  background-color: #e0e0e0;
}

.code-copy-button {
  position: absolute;
  top: 8px;
  right: 8px;
  opacity: 0;
  transition: opacity 0.2s;
  background-color: #f0f0f0;
  border: 1px solid #ccc;
  border-radius: 4px;
  padding: 2px 8px;
  font-size: 12px;
  cursor: pointer;
  z-index: 10;
}

.markdown-content pre.hljs {
  position: relative;
}

.markdown-content pre.hljs:hover .code-copy-button {
  opacity: 1;
}
</style> 