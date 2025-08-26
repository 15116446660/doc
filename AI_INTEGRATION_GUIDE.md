# AI集成指南 - 阿里云千问3 (QWQ3)

## 🎯 概述

本系统已成功集成阿里云千问3 AI模型，支持7个核心AI功能：

1. **AI内容反向插入** - 智能内容建议和插入
2. **智能切片标记** - 文档结构化分析和重要片段标记
3. **提示词测试工具** - AI提示词效果测试和优化
4. **引用来源标记** - 智能识别和标记文档引用
5. **AI智能格式化** - 文档格式优化和标准化
6. **长文本交互** - 长文档智能摘要和交互分析
7. **文档差异对比** - 版本差异智能分析

## 🔧 配置说明

### 1. 获取阿里云千问API密钥

1. 访问 [阿里云百炼平台](https://bailian.console.aliyun.com/)
2. 注册并登录阿里云账号
3. 开通千问模型服务
4. 获取API密钥 (API Key)

### 2. 配置API密钥

#### 方法一：环境变量配置（推荐）
```bash
# 设置环境变量
export QWEN_API_KEY="your-api-key-here"

# 启动服务
./start.sh --backend-only
```

#### 方法二：配置文件配置
编辑 `backend/src/main/resources/application-dev.yml`：
```yaml
ai:
  enabled: true
  qwen:
    api-key: "your-api-key-here"  # 替换为实际的API密钥
    model: qwen-max
    max-tokens: 2000
    temperature: 0.7
```

### 3. 验证配置

启动服务后，访问以下API验证配置：

```bash
# 检查AI服务状态
curl -X GET "http://localhost:8080/api/ai/status"

# 测试AI服务连接
curl -X POST "http://localhost:8080/api/ai/status/test"

# 获取AI服务配置
curl -X GET "http://localhost:8080/api/ai/status/config"
```

## 🚀 AI功能测试

### 1. AI内容反向插入
```bash
curl -X POST "http://localhost:8080/test/ai/content-insertion" \
  -H "Content-Type: application/json"
```

### 2. 智能切片标记
```bash
curl -X POST "http://localhost:8080/test/ai/slice-tagging" \
  -H "Content-Type: application/json"
```

### 3. 提示词测试工具
```bash
curl -X POST "http://localhost:8080/test/ai/prompt-testing" \
  -H "Content-Type: application/json"
```

### 4. 引用来源标记
```bash
curl -X POST "http://localhost:8080/test/ai/citation-marking" \
  -H "Content-Type: application/json"
```

### 5. AI智能格式化
```bash
curl -X POST "http://localhost:8080/test/ai/smart-formatting" \
  -H "Content-Type: application/json"
```

### 6. 长文本交互
```bash
curl -X POST "http://localhost:8080/test/ai/long-text-interaction" \
  -H "Content-Type: application/json"
```

### 7. 文档差异对比
```bash
curl -X POST "http://localhost:8080/test/ai/difference-comparison" \
  -H "Content-Type: application/json"
```

### 8. 完整AI分析（所有7个功能）
```bash
curl -X POST "http://localhost:8080/test/ai/full-analysis" \
  -H "Content-Type: application/json"
```

## 📊 AI服务状态监控

### 服务状态API
- `GET /api/ai/status` - 获取AI服务状态
- `POST /api/ai/status/test` - 测试AI服务连接
- `GET /api/ai/status/config` - 获取AI服务配置

### 状态说明
- **available: true** - AI服务已启用，使用真实千问API
- **available: false** - AI服务未启用，使用模拟模式

## 🔄 模式切换

### 模拟模式（当前状态）
- 无需API密钥
- 返回预设的模拟分析结果
- 适用于开发和测试

### 真实AI模式
- 需要配置有效的API密钥
- 调用真实的千问AI模型
- 返回AI生成的分析结果

## 💡 使用建议

1. **开发阶段**：使用模拟模式进行功能开发和测试
2. **测试阶段**：配置API密钥，使用真实AI模式验证效果
3. **生产环境**：确保API密钥安全配置，监控API调用量和成本

## 🛠️ 技术架构

### AI服务层次
```
Controller Layer (AI测试接口)
    ↓
Service Layer (AI分析服务)
    ↓
AI Integration Layer (千问AI服务)
    ↓
Alibaba DashScope SDK
    ↓
阿里云千问API
```

### 核心组件
- `AIConfig` - AI配置管理
- `QwenAIService` - 千问AI服务封装
- `AIAnalysisService` - AI分析业务逻辑
- `AITestController` - AI功能测试接口
- `AIStatusController` - AI状态监控接口

## 📈 性能优化

1. **异步处理**：所有AI调用都使用CompletableFuture异步执行
2. **错误处理**：完善的异常处理和降级机制
3. **缓存机制**：可扩展Redis缓存减少重复调用
4. **批处理**：支持批量文档分析

## 🔒 安全考虑

1. **API密钥安全**：使用环境变量存储，避免硬编码
2. **访问控制**：生产环境需要配置适当的访问权限
3. **日志脱敏**：确保敏感信息不出现在日志中
4. **限流控制**：防止API调用过于频繁

## 📝 下一步计划

1. **前端界面开发**：Vue3界面展示AI分析结果
2. **缓存优化**：Redis缓存常用分析结果
3. **批处理优化**：支持大批量文档处理
4. **监控告警**：AI服务状态监控和告警
5. **成本控制**：API调用量统计和成本控制

---

🎉 **AI集成完成！** 系统现在具备了完整的AI文档处理能力，可以根据实际需求配置真实的千问API或使用模拟模式进行开发测试。
