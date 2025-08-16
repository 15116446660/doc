**智能文档复用平台需求说明书**
**（Vue3 + Spring Boot单体应用版）**

**版本**：2.0
**日期**：2025-07-14
**编制**：王鹏飞
**技术架构**：Vue3 + Spring Boot + MySQL + Milvus

---

## 架构变更说明

本版本将原始的微服务架构调整为Vue3 + Spring Boot单体应用架构，主要变更如下：

### 原架构 vs 新架构对比

| 组件 | 原架构 | 新架构 | 变更原因 |
|------|--------|--------|----------|
| 文档切分服务 | Python + python-docx | Spring Boot + Apache POI | 统一技术栈，减少维护成本 |
| Embedding服务 | FastAPI + LLM API | Spring Boot Service + HTTP Client | 集成到主应用，简化部署 |
| 召回服务 | Go/Python微服务 | Spring Boot Service | 减少服务间通信开销 |
| 内容源管理 | 独立微服务 | Spring Boot Service | 简化架构复杂度 |
| 文档合成服务 | .NET Core + OpenXML | Spring Boot + Apache POI | 统一技术栈 |
| 消息调度 | RabbitMQ + Celery | Spring @Async + Redis | 简化消息处理 |

### 优势
- **部署简单**：单一应用，减少运维复杂度
- **开发效率**：统一技术栈，团队协作更高效
- **调试方便**：单体应用更容易定位问题
- **事务一致性**：避免分布式事务复杂性

### 权衡
- **扩展性**：相比微服务架构扩展性有所降低
- **技术选择**：受限于Java生态，但Java生态已足够丰富
- **团队协作**：需要团队成员熟悉统一的技术栈

---

## 1. 引言

### 1.1 目的

本文档旨在定义企业级智能文档复用平台的功能需求与非功能需求，确保平台能够在多层级多用途场景中，自动识别、检索、复用已定稿段落，并支持动态内容源更新。系统将结合 Zero‑/Few‑Shot LLM 实现段落级“用途”标签自动标注，基于“多层级召回 + 用途优先”策略，精准匹配和提取 Word 段落，同时支持“单一内容源”机制，实现内容动态刷新。

### 1.2 范围

* 支持 6 个 BU → 多品类 → 多子品类 → 多项目的层级结构
* 支持对文档级用途（DocType）与段落级用途（ParagraphPurpose）双维度标签化
* 支持 Zero‑/Few‑Shot LLM 自动抽取标签
* 基于向量检索和 metadata 过滤，实现多层级召回 + 用途优先
* 支持“单一内容源”模式（Content Control / Custom XML），实现动态更新
* 保留原始 Word 段落样式、图片、表格、公式等

## 2. 背景与业务挑战

* 历史定版文档量不断增长，需高效检索与复用
* 同一项目内多文档、多用途场景，对“用途”区分和优先级要求高
* 平台需在内网环境部署，不依赖外部服务
* 需降低人工干预，实现端到端自动化生成

## 3. 功能需求

### 3.1 段落级用途自动标注

* **目标**：对每个切分段落自动抽取 `para_purpose_id`
* **技术**：Zero‑/Few‑Shot LLM（如 GPT-4o、Qwen-2.5）
* **流程**：通过 Prompt 设计在输入文本时，实时返回对应 `purpose_id`
* **要求**：

  * 支持批量与增量标注
  * 99%+ 高准确度，误差可由人工反馈循环优化

### 3.2 文档级用途自动标注

* **目标**：根据文档标题、元信息自动抽取 `doc_purpose_id`
* **技术**：同上，或规则+LLM 结合
* **要求**：确保新模板选择和检索过滤的准确性

### 3.2.1 Zero-Shot/Few-Shot LLM技术详解

#### 3.2.1.1 Zero-Shot LLM（零样本学习）

**定义**：Zero-Shot是指大语言模型在**没有任何特定任务训练样本**的情况下，仅通过自然语言指令就能完成任务的能力。

**工作原理**：
```
用户输入: "请判断以下段落的用途类型：本项目旨在构建智能文档复用平台..."
模型输出: "PROJECT_BACKGROUND"
```

**技术实现**：
```java
@Service
public class ZeroShotAnnotationService {

    private static final String ZERO_SHOT_PROMPT = """
        你是一个专业的文档分析助手。请分析以下段落内容，判断其在技术文档中的用途类型。

        可选的用途类型包括：
        - PROJECT_BACKGROUND: 项目背景描述
        - TECHNICAL_SOLUTION: 技术方案说明
        - REQUIREMENT_ANALYSIS: 需求分析
        - SYSTEM_ARCHITECTURE: 系统架构设计
        - IMPLEMENTATION_DETAIL: 实现细节
        - TESTING_PLAN: 测试计划
        - RISK_ASSESSMENT: 风险评估

        请仅返回最匹配的用途类型ID，不要包含其他解释。

        段落内容：{content}

        用途类型：
        """;

    public String annotateWithZeroShot(String content) {
        String prompt = ZERO_SHOT_PROMPT.replace("{content}", content);

        LLMRequest request = LLMRequest.builder()
            .prompt(prompt)
            .maxTokens(50)
            .temperature(0.1) // 低温度确保一致性
            .build();

        LLMResponse response = llmClient.chat(request);
        return response.getContent().trim();
    }
}
```

**特点**：
- 不需要提供任何示例
- 完全依赖模型的预训练知识
- 通过清晰的指令描述任务要求
- 实施简单，Token消耗较少

#### 3.2.1.2 Few-Shot LLM（少样本学习）

**定义**：Few-Shot是指在提示中提供**少量示例**（通常1-10个），让模型通过这些示例学习任务模式，然后应用到新的输入上。

**工作原理**：
```
示例1:
输入: "本系统采用微服务架构，包含用户服务、订单服务..."
输出: "SYSTEM_ARCHITECTURE"

示例2:
输入: "项目背景：随着业务快速发展，现有系统面临..."
输出: "PROJECT_BACKGROUND"

现在请分析:
输入: "技术选型方面，我们选择Spring Boot作为后端框架..."
输出: ?
```

**技术实现**：
```java
@Service
public class FewShotAnnotationService {

    private static final String FEW_SHOT_PROMPT = """
        你是一个专业的文档分析助手。请根据以下示例，学习如何判断段落的用途类型：

        示例1：
        段落内容：本项目旨在解决企业文档管理中的重复编写问题，通过智能化手段提升文档复用效率。
        用途类型：PROJECT_BACKGROUND

        示例2：
        段落内容：系统采用Vue3 + Spring Boot架构，前后端分离部署，支持高并发访问。
        用途类型：TECHNICAL_SOLUTION

        示例3：
        段落内容：用户需要能够上传Word文档，系统自动解析并提取可复用段落。
        用途类型：REQUIREMENT_ANALYSIS

        示例4：
        段落内容：整体架构分为表示层、业务层、数据层三个层次，各层职责清晰。
        用途类型：SYSTEM_ARCHITECTURE

        现在请分析以下段落：
        段落内容：{content}
        用途类型：
        """;

    public String annotateWithFewShot(String content) {
        String prompt = FEW_SHOT_PROMPT.replace("{content}", content);

        LLMRequest request = LLMRequest.builder()
            .prompt(prompt)
            .maxTokens(50)
            .temperature(0.1)
            .build();

        LLMResponse response = llmClient.chat(request);
        return response.getContent().trim();
    }
}
```

**特点**：
- 需要少量高质量示例
- 准确性和一致性更高
- 适用于特定领域的自定义分类
- Token消耗较多但效果更好

#### 3.2.1.3 两种方法对比

| 特性 | Zero-Shot | Few-Shot |
|------|-----------|----------|
| **样本需求** | 无需示例 | 需要少量示例 |
| **准确性** | 中等，依赖模型理解 | 较高，有示例指导 |
| **一致性** | 可能不稳定 | 更加稳定 |
| **实施难度** | 简单，只需设计指令 | 中等，需要准备示例 |
| **适用场景** | 通用任务，标准分类 | 特定领域，自定义分类 |
| **Token消耗** | 较少 | 较多（包含示例） |
| **响应速度** | 较快 | 稍慢 |
| **成本** | 较低 | 较高 |

#### 3.2.1.4 智能标注服务实现

**统一标注服务**：
```java
@Service
public class ParagraphAnnotationService {

    @Autowired
    private LLMClient llmClient;

    @Value("${annotation.mode:few-shot}") // 可配置使用模式
    private String annotationMode;

    /**
     * 智能标注段落用途
     */
    public AnnotationResult annotateParagraphPurpose(String content, String context) {
        try {
            String purposeId;
            double confidence;

            if ("zero-shot".equals(annotationMode)) {
                purposeId = annotateWithZeroShot(content);
                confidence = calculateZeroShotConfidence(content, purposeId);
            } else {
                purposeId = annotateWithFewShot(content, context);
                confidence = calculateFewShotConfidence(content, purposeId);
            }

            return AnnotationResult.builder()
                .purposeId(purposeId)
                .confidence(confidence)
                .method(annotationMode)
                .processingTime(System.currentTimeMillis() - startTime)
                .build();

        } catch (Exception e) {
            log.error("段落标注失败: {}", content.substring(0, Math.min(100, content.length())), e);
            return AnnotationResult.builder()
                .purposeId("UNKNOWN")
                .confidence(0.0)
                .method(annotationMode)
                .error(e.getMessage())
                .build();
        }
    }

    /**
     * 批量标注（提升效率）
     */
    public List<AnnotationResult> batchAnnotate(List<ParagraphChunk> paragraphs) {
        if ("few-shot".equals(annotationMode)) {
            return batchAnnotateWithFewShot(paragraphs);
        } else {
            return paragraphs.stream()
                .map(p -> annotateParagraphPurpose(p.getPlainText(), p.getContext()))
                .collect(Collectors.toList());
        }
    }

    private List<AnnotationResult> batchAnnotateWithFewShot(List<ParagraphChunk> paragraphs) {
        // 构建批量处理的Few-Shot prompt
        StringBuilder batchPrompt = new StringBuilder();
        batchPrompt.append(getFewShotExamples());
        batchPrompt.append("\n现在请分析以下多个段落：\n");

        for (int i = 0; i < paragraphs.size(); i++) {
            batchPrompt.append(String.format("段落%d：%s\n", i + 1, paragraphs.get(i).getPlainText()));
        }

        batchPrompt.append("\n请按顺序返回每个段落的用途类型，格式：段落1:PURPOSE_ID,段落2:PURPOSE_ID...\n");

        LLMResponse response = llmClient.chat(LLMRequest.builder()
            .prompt(batchPrompt.toString())
            .maxTokens(200)
            .temperature(0.1)
            .build());

        return parseBatchResponse(response.getContent(), paragraphs.size());
    }
}

@Data
@Builder
public class AnnotationResult {
    private String purposeId;
    private double confidence;
    private String method; // zero-shot, few-shot
    private String error;
    private long processingTime;
}
```

**文档级用途标注**：
```java
@Service
public class DocumentAnnotationService {

    /**
     * 文档级用途标注（Zero-Shot）
     */
    public String annotateDocumentPurpose(String title, String summary) {
        String prompt = String.format("""
            请分析以下文档信息，判断文档的主要用途类型：

            文档标题：%s
            文档摘要：%s

            可选用途类型：
            - REQUIREMENT_DOC: 需求文档
            - DESIGN_DOC: 设计文档
            - TECHNICAL_DOC: 技术文档
            - TEST_DOC: 测试文档
            - USER_MANUAL: 用户手册
            - PROJECT_PLAN: 项目计划

            请返回最匹配的用途类型ID：
            """, title, summary);

        LLMResponse response = llmClient.chat(LLMRequest.builder()
            .prompt(prompt)
            .maxTokens(30)
            .temperature(0.0)
            .build());

        return response.getContent().trim();
    }
}
```

#### 3.2.1.5 优化策略

**Prompt工程优化**：
```java
@Service
public class PromptOptimizer {

    /**
     * 动态调整Prompt策略
     */
    public String optimizePrompt(String basePrompt, AnnotationHistory history) {
        // 根据历史准确率调整策略
        if (history.getAccuracy() < 0.8) {
            // 准确率低，增加更多示例和约束
            return enhancePromptWithMoreExamples(basePrompt);
        } else if (history.getAccuracy() > 0.95) {
            // 准确率高，可以简化Prompt节省Token
            return simplifyPrompt(basePrompt);
        }
        return basePrompt;
    }

    /**
     * 上下文感知的Prompt生成
     */
    public String generateContextAwarePrompt(String content, DocumentContext context) {
        StringBuilder prompt = new StringBuilder();

        // 添加文档上下文信息
        if (context.getDocumentType() != null) {
            prompt.append("文档类型：").append(context.getDocumentType()).append("\n");
        }

        if (context.getProjectDomain() != null) {
            prompt.append("项目领域：").append(context.getProjectDomain()).append("\n");
        }

        // 根据上下文选择相关示例
        List<String> relevantExamples = selectRelevantExamples(context);
        for (String example : relevantExamples) {
            prompt.append(example).append("\n");
        }

        prompt.append("请分析以下段落：").append(content);

        return prompt.toString();
    }
}
```

**结果验证与反馈循环**：
```java
@Service
public class AnnotationValidationService {

    /**
     * 验证标注结果的合理性
     */
    public ValidationResult validateAnnotation(String content, String purposeId) {
        // 1. 关键词匹配验证
        boolean keywordMatch = validateByKeywords(content, purposeId);

        // 2. 上下文一致性验证
        boolean contextConsistent = validateContextConsistency(content, purposeId);

        // 3. 历史模式验证
        boolean patternMatch = validateByHistoricalPattern(content, purposeId);

        double confidence = calculateValidationConfidence(keywordMatch, contextConsistent, patternMatch);

        return ValidationResult.builder()
            .isValid(confidence > 0.7)
            .confidence(confidence)
            .keywordMatch(keywordMatch)
            .contextConsistent(contextConsistent)
            .patternMatch(patternMatch)
            .build();
    }

    /**
     * 人工反馈学习
     */
    public void learnFromFeedback(String content, String predictedPurpose, String actualPurpose) {
        if (!predictedPurpose.equals(actualPurpose)) {
            // 记录错误案例
            AnnotationError error = new AnnotationError();
            error.setContent(content);
            error.setPredictedPurpose(predictedPurpose);
            error.setActualPurpose(actualPurpose);
            error.setTimestamp(LocalDateTime.now());

            annotationErrorRepository.save(error);

            // 更新Few-Shot示例库
            updateFewShotExamples(content, actualPurpose);
        }
    }
}
```

#### 3.2.1.6 性能优化

**缓存策略**：
```java
@Service
public class CachedAnnotationService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 带缓存的标注服务
     */
    @Cacheable(value = "annotations", key = "#content.hashCode()")
    public String annotateWithCache(String content) {
        // 先检查缓存
        String cacheKey = "annotation:" + DigestUtils.md5Hex(content);
        String cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            return cached;
        }

        // 执行标注
        String result = annotateParagraphPurpose(content, null).getPurposeId();

        // 缓存结果（24小时过期）
        redisTemplate.opsForValue().set(cacheKey, result, Duration.ofHours(24));

        return result;
    }
}
```

**异步批处理**：
```java
@Service
public class AsyncAnnotationService {

    @Async("annotationExecutor")
    public CompletableFuture<List<AnnotationResult>> asyncBatchAnnotate(List<ParagraphChunk> paragraphs) {
        try {
            List<AnnotationResult> results = batchAnnotate(paragraphs);
            return CompletableFuture.completedFuture(results);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
```

#### 3.2.1.7 配置与部署

**应用配置**：
```yaml
# application.yml
llm:
  annotation:
    mode: few-shot  # zero-shot, few-shot
    batch-size: 10
    cache-enabled: true
    cache-ttl: 24h

  providers:
    openai:
      api-key: ${OPENAI_API_KEY}
      model: gpt-4o
      max-tokens: 100
      temperature: 0.1

    qwen:
      api-key: ${QWEN_API_KEY}
      model: qwen-2.5-72b
      max-tokens: 100
      temperature: 0.1

annotation:
  validation:
    enabled: true
    confidence-threshold: 0.7

  feedback:
    enabled: true
    auto-update-examples: true
```

**线程池配置**：
```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "annotationExecutor")
    public Executor annotationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Annotation-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
```

#### 3.2.1.8 技术优势与挑战

**技术优势**：
1. **自动化程度高**：减少人工标注工作量，提升文档处理效率
2. **适应性强**：可以处理各种类型的文档内容和领域知识
3. **准确性可控**：通过Prompt优化和反馈学习持续提升准确性
4. **扩展性好**：容易添加新的用途类型和适应新的业务场景
5. **成本效益**：相比人工标注，长期成本更低且一致性更好

**技术挑战**：
1. **一致性保证**：需要通过技术手段确保标注结果的一致性
2. **成本控制**：LLM API调用成本需要合理控制和优化
3. **延迟优化**：批处理和缓存策略降低响应延迟
4. **准确性提升**：需要持续的反馈学习和模型优化
5. **多模型适配**：支持不同LLM提供商的API差异

**实施建议**：
1. **渐进式部署**：从Zero-Shot开始，逐步优化到Few-Shot
2. **质量监控**：建立完善的标注质量监控和告警机制
3. **人工校验**：保留人工校验环节，形成闭环反馈
4. **成本优化**：合理使用缓存、批处理等技术降低成本
5. **多模型备份**：配置多个LLM提供商，确保服务可用性

这种基于Zero-Shot/Few-Shot LLM的智能标注方案为文档复用平台提供了强大的自动化能力，是实现"端到端自动化生成"的关键技术组件。

### 3.3 多层级召回 + 用途优先策略

* **层级**：项目 → 兄弟项目 → 跨子品类 → 跨品类
* **优先级**：

  1. 同项目内 && doc\_purpose && para\_purpose
  2. 同项目内 && para\_purpose
  3. 同项目内（不限用途） 4–6. 依次降级到兄弟→跨子品类→跨品类，流程同上
* **召回接口**：RESTful，支持多重 filter、top-K 查询
* **置信度阈值**：可配置，默认 0.85

#### 3.3.1 Milvus集成与向量存储

**Milvus集成架构**：

```java
@Configuration
public class MilvusConfig {

    @Value("${milvus.host:localhost}")
    private String milvusHost;

    @Value("${milvus.port:19530}")
    private int milvusPort;

    @Value("${milvus.database:doc_reuse}")
    private String database;

    @Bean
    public MilvusServiceClient milvusClient() {
        ConnectParam connectParam = ConnectParam.newBuilder()
            .withHost(milvusHost)
            .withPort(milvusPort)
            .withDatabaseName(database)
            .build();

        return new MilvusServiceClient(connectParam);
    }
}
```

**向量存储服务实现**：

```java
@Service
@Slf4j
public class VectorStorageService {

    @Autowired
    private MilvusServiceClient milvusClient;

    @Autowired
    private EmbeddingService embeddingService;

    private static final String COLLECTION_NAME = "paragraph_vectors";
    private static final int VECTOR_DIMENSION = 1536; // OpenAI embedding维度

    @PostConstruct
    public void initializeCollection() {
        if (!collectionExists()) {
            createCollection();
            createIndex();
        }
    }

    /**
     * 创建向量集合
     */
    private void createCollection() {
        // 定义字段
        List<FieldType> fields = Arrays.asList(
            FieldType.newBuilder()
                .withName("id")
                .withDataType(DataType.VarChar)
                .withMaxLength(64)
                .withPrimaryKey(true)
                .build(),
            FieldType.newBuilder()
                .withName("project_id")
                .withDataType(DataType.VarChar)
                .withMaxLength(64)
                .build(),
            FieldType.newBuilder()
                .withName("bu")
                .withDataType(DataType.VarChar)
                .withMaxLength(32)
                .build(),
            FieldType.newBuilder()
                .withName("category")
                .withDataType(DataType.VarChar)
                .withMaxLength(64)
                .build(),
            FieldType.newBuilder()
                .withName("subcategory")
                .withDataType(DataType.VarChar)
                .withMaxLength(64)
                .build(),
            FieldType.newBuilder()
                .withName("doc_purpose_id")
                .withDataType(DataType.VarChar)
                .withMaxLength(32)
                .build(),
            FieldType.newBuilder()
                .withName("para_purpose_id")
                .withDataType(DataType.VarChar)
                .withMaxLength(32)
                .build(),
            FieldType.newBuilder()
                .withName("content_vector")
                .withDataType(DataType.FloatVector)
                .withDimension(VECTOR_DIMENSION)
                .build(),
            FieldType.newBuilder()
                .withName("plain_text")
                .withDataType(DataType.VarChar)
                .withMaxLength(8192)
                .build(),
            FieldType.newBuilder()
                .withName("created_at")
                .withDataType(DataType.Int64)
                .build()
        );

        // 创建集合
        CreateCollectionParam createParam = CreateCollectionParam.newBuilder()
            .withCollectionName(COLLECTION_NAME)
            .withDescription("文档段落向量存储")
            .withFieldTypes(fields)
            .build();

        R<RpcStatus> response = milvusClient.createCollection(createParam);
        if (response.getStatus() != R.Status.Success.getCode()) {
            throw new VectorStorageException("创建Milvus集合失败: " + response.getMessage());
        }

        log.info("Milvus集合创建成功: {}", COLLECTION_NAME);
    }

    /**
     * 创建向量索引
     */
    private void createIndex() {
        IndexParam indexParam = IndexParam.newBuilder()
            .withCollectionName(COLLECTION_NAME)
            .withFieldName("content_vector")
            .withIndexType(IndexType.IVF_FLAT)
            .withMetricType(MetricType.COSINE)
            .withExtraParam("{\"nlist\":1024}")
            .build();

        R<RpcStatus> response = milvusClient.createIndex(indexParam);
        if (response.getStatus() != R.Status.Success.getCode()) {
            throw new VectorStorageException("创建向量索引失败: " + response.getMessage());
        }

        log.info("向量索引创建成功");
    }

    /**
     * 存储段落向量
     */
    public void storeParagraphVector(ParagraphChunk paragraph) {
        try {
            // 生成向量
            List<Float> vector = embeddingService.generateEmbedding(paragraph.getPlainText());

            // 准备数据
            List<InsertParam.Field> fields = Arrays.asList(
                new InsertParam.Field("id", Collections.singletonList(paragraph.getId())),
                new InsertParam.Field("project_id", Collections.singletonList(paragraph.getProjectId())),
                new InsertParam.Field("bu", Collections.singletonList(paragraph.getBu())),
                new InsertParam.Field("category", Collections.singletonList(paragraph.getCategory())),
                new InsertParam.Field("subcategory", Collections.singletonList(paragraph.getSubcategory())),
                new InsertParam.Field("doc_purpose_id", Collections.singletonList(paragraph.getDocPurposeId())),
                new InsertParam.Field("para_purpose_id", Collections.singletonList(paragraph.getParaPurposeId())),
                new InsertParam.Field("content_vector", Collections.singletonList(vector)),
                new InsertParam.Field("plain_text", Collections.singletonList(paragraph.getPlainText())),
                new InsertParam.Field("created_at", Collections.singletonList(System.currentTimeMillis()))
            );

            // 插入数据
            InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withFields(fields)
                .build();

            R<MutationResult> response = milvusClient.insert(insertParam);
            if (response.getStatus() != R.Status.Success.getCode()) {
                throw new VectorStorageException("向量存储失败: " + response.getMessage());
            }

            // 更新段落的向量ID
            paragraph.setVectorId(paragraph.getId());

            log.debug("段落向量存储成功: {}", paragraph.getId());

        } catch (Exception e) {
            log.error("存储段落向量失败: {}", paragraph.getId(), e);
            throw new VectorStorageException("向量存储失败", e);
        }
    }

    /**
     * 批量存储段落向量
     */
    @Async("vectorProcessingExecutor")
    public CompletableFuture<Void> batchStoreParagraphVectors(List<ParagraphChunk> paragraphs) {
        try {
            // 批量生成向量
            List<String> texts = paragraphs.stream()
                .map(ParagraphChunk::getPlainText)
                .collect(Collectors.toList());

            List<List<Float>> vectors = embeddingService.batchGenerateEmbeddings(texts);

            // 准备批量数据
            List<String> ids = new ArrayList<>();
            List<String> projectIds = new ArrayList<>();
            List<String> bus = new ArrayList<>();
            List<String> categories = new ArrayList<>();
            List<String> subcategories = new ArrayList<>();
            List<String> docPurposeIds = new ArrayList<>();
            List<String> paraPurposeIds = new ArrayList<>();
            List<String> plainTexts = new ArrayList<>();
            List<Long> createdAts = new ArrayList<>();

            for (int i = 0; i < paragraphs.size(); i++) {
                ParagraphChunk paragraph = paragraphs.get(i);
                ids.add(paragraph.getId());
                projectIds.add(paragraph.getProjectId());
                bus.add(paragraph.getBu());
                categories.add(paragraph.getCategory());
                subcategories.add(paragraph.getSubcategory());
                docPurposeIds.add(paragraph.getDocPurposeId());
                paraPurposeIds.add(paragraph.getParaPurposeId());
                plainTexts.add(paragraph.getPlainText());
                createdAts.add(System.currentTimeMillis());
            }

            List<InsertParam.Field> fields = Arrays.asList(
                new InsertParam.Field("id", ids),
                new InsertParam.Field("project_id", projectIds),
                new InsertParam.Field("bu", bus),
                new InsertParam.Field("category", categories),
                new InsertParam.Field("subcategory", subcategories),
                new InsertParam.Field("doc_purpose_id", docPurposeIds),
                new InsertParam.Field("para_purpose_id", paraPurposeIds),
                new InsertParam.Field("content_vector", vectors),
                new InsertParam.Field("plain_text", plainTexts),
                new InsertParam.Field("created_at", createdAts)
            );

            InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withFields(fields)
                .build();

            R<MutationResult> response = milvusClient.insert(insertParam);
            if (response.getStatus() != R.Status.Success.getCode()) {
                throw new VectorStorageException("批量向量存储失败: " + response.getMessage());
            }

            log.info("批量向量存储成功，数量: {}", paragraphs.size());
            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("批量向量存储失败", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    private boolean collectionExists() {
        HasCollectionParam param = HasCollectionParam.newBuilder()
            .withCollectionName(COLLECTION_NAME)
            .build();

        R<Boolean> response = milvusClient.hasCollection(param);
        return response.getStatus() == R.Status.Success.getCode() && response.getData();
    }
}
```

#### 3.3.2 多层级召回算法实现

**召回策略枚举**：

```java
public enum RecallLevel {
    SAME_PROJECT_FULL_MATCH(1, "同项目内 && doc_purpose && para_purpose"),
    SAME_PROJECT_PARA_MATCH(2, "同项目内 && para_purpose"),
    SAME_PROJECT_ANY(3, "同项目内（不限用途）"),
    SIBLING_PROJECT_FULL_MATCH(4, "兄弟项目 && doc_purpose && para_purpose"),
    SIBLING_PROJECT_PARA_MATCH(5, "兄弟项目 && para_purpose"),
    SIBLING_PROJECT_ANY(6, "兄弟项目（不限用途）"),
    CROSS_SUBCATEGORY_FULL_MATCH(7, "跨子品类 && doc_purpose && para_purpose"),
    CROSS_SUBCATEGORY_PARA_MATCH(8, "跨子品类 && para_purpose"),
    CROSS_SUBCATEGORY_ANY(9, "跨子品类（不限用途）"),
    CROSS_CATEGORY_FULL_MATCH(10, "跨品类 && doc_purpose && para_purpose"),
    CROSS_CATEGORY_PARA_MATCH(11, "跨品类 && para_purpose"),
    CROSS_CATEGORY_ANY(12, "跨品类（不限用途）");

    private final int priority;
    private final String description;

    RecallLevel(int priority, String description) {
        this.priority = priority;
        this.description = description;
    }

    public int getPriority() { return priority; }
    public String getDescription() { return description; }
}
```

**多层级召回服务**：

```java
@Service
@Slf4j
public class MultiLevelRecallService {

    @Autowired
    private MilvusServiceClient milvusClient;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private ParagraphChunkRepository paragraphRepository;

    @Value("${recall.confidence.threshold:0.85}")
    private double confidenceThreshold;

    @Value("${recall.max.results.per.level:10}")
    private int maxResultsPerLevel;

    private static final String COLLECTION_NAME = "paragraph_vectors";

    /**
     * 执行多层级召回
     */
    public RecallResponse performMultiLevelRecall(RecallRequest request) {
        RecallResponse response = new RecallResponse();
        Map<String, SectionResult> results = new HashMap<>();

        for (SectionQuery section : request.getSections()) {
            SectionResult sectionResult = recallForSection(section, request);
            if (sectionResult != null) {
                results.put(section.getSectionId(), sectionResult);
            }
        }

        response.setResults(results);
        response.setTotalSections(request.getSections().size());
        response.setSuccessfulSections(results.size());
        response.setProcessingTime(System.currentTimeMillis() - request.getStartTime());

        return response;
    }

    /**
     * 为单个章节执行召回
     */
    private SectionResult recallForSection(SectionQuery section, RecallRequest request) {
        try {
            // 生成查询向量
            List<Float> queryVector = embeddingService.generateEmbedding(section.getContext());

            // 按优先级逐级召回
            for (RecallLevel level : RecallLevel.values()) {
                List<VectorSearchResult> candidates = searchByLevel(
                    queryVector, section, request, level
                );

                if (!candidates.isEmpty()) {
                    // 找到结果，选择最佳匹配
                    VectorSearchResult bestMatch = selectBestMatch(candidates, section);

                    if (bestMatch.getScore() >= confidenceThreshold) {
                        return buildSectionResult(bestMatch, level);
                    }
                }
            }

            log.warn("未找到满足阈值的召回结果: sectionId={}, threshold={}",
                    section.getSectionId(), confidenceThreshold);
            return null;

        } catch (Exception e) {
            log.error("章节召回失败: {}", section.getSectionId(), e);
            return null;
        }
    }

    /**
     * 按层级搜索
     */
    private List<VectorSearchResult> searchByLevel(List<Float> queryVector,
                                                  SectionQuery section,
                                                  RecallRequest request,
                                                  RecallLevel level) {

        // 构建过滤条件
        String filterExpression = buildFilterExpression(request, section, level);

        // 执行向量搜索
        SearchParam searchParam = SearchParam.newBuilder()
            .withCollectionName(COLLECTION_NAME)
            .withVectorFieldName("content_vector")
            .withVectors(Collections.singletonList(queryVector))
            .withTopK(maxResultsPerLevel)
            .withMetricType(MetricType.COSINE)
            .withParams("{\"nprobe\":10}")
            .withExpr(filterExpression)
            .withOutFields(Arrays.asList("id", "project_id", "bu", "category",
                                       "subcategory", "doc_purpose_id", "para_purpose_id",
                                       "plain_text"))
            .build();

        R<SearchResults> response = milvusClient.search(searchParam);

        if (response.getStatus() != R.Status.Success.getCode()) {
            log.error("向量搜索失败: level={}, error={}", level, response.getMessage());
            return Collections.emptyList();
        }

        return parseSearchResults(response.getData(), level);
    }

    /**
     * 构建过滤表达式
     */
    private String buildFilterExpression(RecallRequest request, SectionQuery section, RecallLevel level) {
        StringBuilder filter = new StringBuilder();

        switch (level) {
            case SAME_PROJECT_FULL_MATCH:
                filter.append(String.format("project_id == '%s'", request.getProjectId()));
                filter.append(String.format(" && doc_purpose_id == '%s'", request.getDocPurposeId()));
                filter.append(String.format(" && para_purpose_id == '%s'", section.getParaPurposeId()));
                break;

            case SAME_PROJECT_PARA_MATCH:
                filter.append(String.format("project_id == '%s'", request.getProjectId()));
                filter.append(String.format(" && para_purpose_id == '%s'", section.getParaPurposeId()));
                break;

            case SAME_PROJECT_ANY:
                filter.append(String.format("project_id == '%s'", request.getProjectId()));
                break;

            case SIBLING_PROJECT_FULL_MATCH:
                filter.append(String.format("bu == '%s' && category == '%s' && subcategory == '%s'",
                            request.getBu(), request.getCategory(), request.getSubcategory()));
                filter.append(String.format(" && project_id != '%s'", request.getProjectId()));
                filter.append(String.format(" && doc_purpose_id == '%s'", request.getDocPurposeId()));
                filter.append(String.format(" && para_purpose_id == '%s'", section.getParaPurposeId()));
                break;

            case SIBLING_PROJECT_PARA_MATCH:
                filter.append(String.format("bu == '%s' && category == '%s' && subcategory == '%s'",
                            request.getBu(), request.getCategory(), request.getSubcategory()));
                filter.append(String.format(" && project_id != '%s'", request.getProjectId()));
                filter.append(String.format(" && para_purpose_id == '%s'", section.getParaPurposeId()));
                break;

            case SIBLING_PROJECT_ANY:
                filter.append(String.format("bu == '%s' && category == '%s' && subcategory == '%s'",
                            request.getBu(), request.getCategory(), request.getSubcategory()));
                filter.append(String.format(" && project_id != '%s'", request.getProjectId()));
                break;

            case CROSS_SUBCATEGORY_FULL_MATCH:
                filter.append(String.format("bu == '%s' && category == '%s'",
                            request.getBu(), request.getCategory()));
                filter.append(String.format(" && subcategory != '%s'", request.getSubcategory()));
                filter.append(String.format(" && doc_purpose_id == '%s'", request.getDocPurposeId()));
                filter.append(String.format(" && para_purpose_id == '%s'", section.getParaPurposeId()));
                break;

            case CROSS_SUBCATEGORY_PARA_MATCH:
                filter.append(String.format("bu == '%s' && category == '%s'",
                            request.getBu(), request.getCategory()));
                filter.append(String.format(" && subcategory != '%s'", request.getSubcategory()));
                filter.append(String.format(" && para_purpose_id == '%s'", section.getParaPurposeId()));
                break;

            case CROSS_SUBCATEGORY_ANY:
                filter.append(String.format("bu == '%s' && category == '%s'",
                            request.getBu(), request.getCategory()));
                filter.append(String.format(" && subcategory != '%s'", request.getSubcategory()));
                break;

            case CROSS_CATEGORY_FULL_MATCH:
                filter.append(String.format("bu == '%s'", request.getBu()));
                filter.append(String.format(" && category != '%s'", request.getCategory()));
                filter.append(String.format(" && doc_purpose_id == '%s'", request.getDocPurposeId()));
                filter.append(String.format(" && para_purpose_id == '%s'", section.getParaPurposeId()));
                break;

            case CROSS_CATEGORY_PARA_MATCH:
                filter.append(String.format("bu == '%s'", request.getBu()));
                filter.append(String.format(" && category != '%s'", request.getCategory()));
                filter.append(String.format(" && para_purpose_id == '%s'", section.getParaPurposeId()));
                break;

            case CROSS_CATEGORY_ANY:
                filter.append(String.format("bu == '%s'", request.getBu()));
                filter.append(String.format(" && category != '%s'", request.getCategory()));
                break;
        }

        return filter.toString();
    }

    /**
     * 解析搜索结果
     */
    private List<VectorSearchResult> parseSearchResults(SearchResults searchResults, RecallLevel level) {
        List<VectorSearchResult> results = new ArrayList<>();

        for (SearchResults.QueryResult queryResult : searchResults.getQueryResults()) {
            for (int i = 0; i < queryResult.getIds().size(); i++) {
                VectorSearchResult result = new VectorSearchResult();
                result.setId(queryResult.getIds().get(i).toString());
                result.setScore(queryResult.getScores().get(i));
                result.setLevel(level);

                // 提取字段值
                Map<String, Object> fields = new HashMap<>();
                for (String fieldName : queryResult.getFieldsMap().keySet()) {
                    fields.put(fieldName, queryResult.getFieldsMap().get(fieldName).get(i));
                }
                result.setFields(fields);

                results.add(result);
            }
        }

        return results;
    }

    /**
     * 选择最佳匹配
     */
    private VectorSearchResult selectBestMatch(List<VectorSearchResult> candidates, SectionQuery section) {
        // 按相似度分数排序
        candidates.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        // 可以在这里添加更复杂的选择逻辑，比如：
        // 1. 考虑文本长度匹配度
        // 2. 考虑关键词匹配度
        // 3. 考虑历史使用频率

        return candidates.get(0);
    }

    /**
     * 构建章节结果
     */
    private SectionResult buildSectionResult(VectorSearchResult searchResult, RecallLevel level) {
        // 从数据库获取完整的段落信息
        String paragraphId = searchResult.getId();
        ParagraphChunk paragraph = paragraphRepository.findById(paragraphId)
            .orElseThrow(() -> new ParagraphNotFoundException(paragraphId));

        SectionResult result = new SectionResult();
        result.setContentId(paragraph.getContentId());
        result.setRawXml(paragraph.getRawXml());
        result.setConfidence(searchResult.getScore());
        result.setSource(level.getDescription());
        result.setSourceLevel(level.getPriority());
        result.setPlainText(paragraph.getPlainText());
        result.setParagraphId(paragraphId);
        result.setProjectId(paragraph.getProjectId());
        result.setBu(paragraph.getBu());
        result.setCategory(paragraph.getCategory());
        result.setSubcategory(paragraph.getSubcategory());

        return result;
    }
}
```

#### 3.3.3 检索接口与前端界面开发

**检索控制器实现**：

```java
@RestController
@RequestMapping("/api/v1/recall")
@Slf4j
@Validated
public class RecallController {

    @Autowired
    private MultiLevelRecallService recallService;

    @Autowired
    private RecallHistoryService historyService;

    @Autowired
    private RecallAnalyticsService analyticsService;

    /**
     * 多层级召回检索
     */
    @PostMapping("/search")
    public ResponseEntity<RecallResponse> recall(@Valid @RequestBody RecallRequest request) {
        long startTime = System.currentTimeMillis();
        request.setStartTime(startTime);

        try {
            // 参数验证
            validateRecallRequest(request);

            // 执行召回
            RecallResponse response = recallService.performMultiLevelRecall(request);

            // 记录历史
            historyService.recordRecallHistory(request, response);

            // 更新分析数据
            analyticsService.updateRecallAnalytics(request, response);

            log.info("召回请求完成: sections={}, successful={}, time={}ms",
                    response.getTotalSections(), response.getSuccessfulSections(),
                    response.getProcessingTime());

            return ResponseEntity.ok(response);

        } catch (ValidationException e) {
            log.warn("召回请求参数验证失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(RecallResponse.error("参数验证失败: " + e.getMessage()));
        } catch (Exception e) {
            log.error("召回请求处理失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RecallResponse.error("召回处理失败: " + e.getMessage()));
        }
    }

    /**
     * 相似段落推荐
     */
    @PostMapping("/recommend")
    public ResponseEntity<RecommendationResponse> recommend(@Valid @RequestBody RecommendationRequest request) {
        try {
            RecommendationResponse response = recallService.recommendSimilarParagraphs(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("推荐请求处理失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RecommendationResponse.error("推荐处理失败: " + e.getMessage()));
        }
    }

    /**
     * 获取召回历史
     */
    @GetMapping("/history")
    public ResponseEntity<PageResult<RecallHistory>> getRecallHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String userId) {

        try {
            PageResult<RecallHistory> result = historyService.getRecallHistory(
                page, size, projectId, userId
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取召回历史失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取召回分析数据
     */
    @GetMapping("/analytics")
    public ResponseEntity<RecallAnalytics> getRecallAnalytics(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String timeRange) {

        try {
            RecallAnalytics analytics = analyticsService.getRecallAnalytics(projectId, timeRange);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("获取召回分析数据失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 实时搜索建议
     */
    @GetMapping("/suggestions")
    public ResponseEntity<List<SearchSuggestion>> getSearchSuggestions(
            @RequestParam String query,
            @RequestParam(required = false) String projectId,
            @RequestParam(defaultValue = "10") int limit) {

        try {
            List<SearchSuggestion> suggestions = recallService.getSearchSuggestions(
                query, projectId, limit
            );
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            log.error("获取搜索建议失败", e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    private void validateRecallRequest(RecallRequest request) {
        if (request.getSections() == null || request.getSections().isEmpty()) {
            throw new ValidationException("sections不能为空");
        }

        if (request.getProjectId() == null || request.getProjectId().trim().isEmpty()) {
            throw new ValidationException("projectId不能为空");
        }

        for (SectionQuery section : request.getSections()) {
            if (section.getSectionId() == null || section.getContext() == null) {
                throw new ValidationException("section的sectionId和context不能为空");
            }
        }
    }
}
```

**请求响应DTO定义**：

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecallRequest {
    @NotBlank(message = "项目ID不能为空")
    private String projectId;

    @NotBlank(message = "BU不能为空")
    private String bu;

    @NotBlank(message = "品类不能为空")
    private String category;

    @NotBlank(message = "子品类不能为空")
    private String subcategory;

    private String docPurposeId;

    @NotEmpty(message = "章节列表不能为空")
    @Valid
    private List<SectionQuery> sections;

    @DecimalMin(value = "0.0", message = "置信度阈值不能小于0")
    @DecimalMax(value = "1.0", message = "置信度阈值不能大于1")
    private Double threshold = 0.85;

    private String userId; // 用于记录和分析
    private Long startTime; // 内部使用
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionQuery {
    @NotBlank(message = "章节ID不能为空")
    private String sectionId;

    @NotBlank(message = "上下文内容不能为空")
    private String context;

    private String paraPurposeId;

    private Map<String, Object> metadata; // 扩展元数据
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecallResponse {
    private Map<String, SectionResult> results;
    private int totalSections;
    private int successfulSections;
    private long processingTime;
    private String status = "SUCCESS";
    private String message;
    private RecallStatistics statistics;

    public static RecallResponse error(String message) {
        return RecallResponse.builder()
            .status("ERROR")
            .message(message)
            .results(Collections.emptyMap())
            .build();
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionResult {
    private String contentId;
    private String rawXml;
    private Double confidence;
    private String source; // 召回层级描述
    private Integer sourceLevel; // 召回层级优先级
    private String plainText;
    private String paragraphId;
    private String projectId;
    private String bu;
    private String category;
    private String subcategory;
    private Map<String, Object> metadata;
}

@Data
@Builder
public class RecallStatistics {
    private Map<String, Integer> levelDistribution; // 各层级召回数量分布
    private Double averageConfidence; // 平均置信度
    private Long averageResponseTime; // 平均响应时间
    private Map<String, Double> purposeMatchRate; // 用途匹配率
}
```

**Vue3前端检索界面实现**：

```vue
<template>
  <div class="recall-search-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>智能召回检索</span>
          <el-button type="primary" @click="showAdvancedSearch = !showAdvancedSearch">
            <el-icon><Setting /></el-icon>
            高级设置
          </el-button>
        </div>
      </template>

      <!-- 基础搜索表单 -->
      <el-form :model="searchForm" :rules="searchRules" ref="searchFormRef" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="项目" prop="projectId">
              <el-select v-model="searchForm.projectId" placeholder="选择项目" filterable>
                <el-option v-for="project in projects" :key="project.id"
                          :label="project.name" :value="project.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="BU" prop="bu">
              <el-select v-model="searchForm.bu" placeholder="选择BU">
                <el-option v-for="bu in buList" :key="bu.code"
                          :label="bu.name" :value="bu.code" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="品类" prop="category">
              <el-select v-model="searchForm.category" placeholder="选择品类">
                <el-option v-for="category in categories" :key="category.code"
                          :label="category.name" :value="category.code" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="子品类" prop="subcategory">
              <el-select v-model="searchForm.subcategory" placeholder="选择子品类">
                <el-option v-for="subcategory in subcategories" :key="subcategory.code"
                          :label="subcategory.name" :value="subcategory.code" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="文档用途">
              <el-select v-model="searchForm.docPurposeId" placeholder="选择文档用途" clearable>
                <el-option v-for="purpose in docPurposes" :key="purpose.id"
                          :label="purpose.name" :value="purpose.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="置信度阈值">
              <el-slider v-model="searchForm.threshold" :min="0.5" :max="1.0" :step="0.05"
                        show-input :format-tooltip="formatThreshold" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <!-- 高级设置 -->
      <el-collapse v-model="activeAdvanced" v-show="showAdvancedSearch">
        <el-collapse-item title="高级搜索设置" name="advanced">
          <el-form :model="advancedForm" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="最大结果数">
                  <el-input-number v-model="advancedForm.maxResults" :min="1" :max="100" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="搜索模式">
                  <el-radio-group v-model="advancedForm.searchMode">
                    <el-radio label="strict">严格模式</el-radio>
                    <el-radio label="fuzzy">模糊模式</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="排除项目">
              <el-select v-model="advancedForm.excludeProjects" multiple placeholder="选择要排除的项目">
                <el-option v-for="project in projects" :key="project.id"
                          :label="project.name" :value="project.id" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-collapse-item>
      </el-collapse>

      <!-- 章节配置 -->
      <div class="sections-config">
        <div class="section-header">
          <h3>章节配置</h3>
          <el-button type="primary" size="small" @click="addSection">
            <el-icon><Plus /></el-icon>
            添加章节
          </el-button>
        </div>

        <div v-for="(section, index) in sections" :key="section.id" class="section-item">
          <el-card>
            <div class="section-controls">
              <span class="section-title">章节 {{ index + 1 }}</span>
              <el-button size="small" type="danger" @click="removeSection(index)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>

            <el-form :model="section" label-width="100px">
              <el-form-item label="章节ID">
                <el-input v-model="section.sectionId" placeholder="输入章节标识" />
              </el-form-item>
              <el-form-item label="段落用途">
                <el-select v-model="section.paraPurposeId" placeholder="选择段落用途" clearable>
                  <el-option v-for="purpose in paraPurposes" :key="purpose.id"
                            :label="purpose.name" :value="purpose.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="上下文内容">
                <el-input v-model="section.context" type="textarea" :rows="3"
                         placeholder="输入章节的上下文内容，用于语义匹配" />
              </el-form-item>
            </el-form>
          </el-card>
        </div>
      </div>

      <!-- 搜索按钮 -->
      <div class="search-actions">
        <el-button type="primary" size="large" @click="performSearch" :loading="searching">
          <el-icon><Search /></el-icon>
          开始召回检索
        </el-button>
        <el-button size="large" @click="resetForm">重置</el-button>
        <el-button size="large" @click="saveAsTemplate">保存为模板</el-button>
      </div>
    </el-card>

    <!-- 搜索结果 -->
    <el-card v-if="searchResults" class="results-card">
      <template #header>
        <div class="results-header">
          <span>召回结果</span>
          <div class="results-stats">
            <el-tag type="success">成功: {{ searchResults.successfulSections }}</el-tag>
            <el-tag type="info">总计: {{ searchResults.totalSections }}</el-tag>
            <el-tag type="warning">耗时: {{ searchResults.processingTime }}ms</el-tag>
          </div>
        </div>
      </template>

      <!-- 结果统计 -->
      <div v-if="searchResults.statistics" class="statistics-section">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-statistic title="平均置信度" :value="searchResults.statistics.averageConfidence"
                         :precision="3" suffix="%" />
          </el-col>
          <el-col :span="8">
            <el-statistic title="平均响应时间" :value="searchResults.statistics.averageResponseTime"
                         suffix="ms" />
          </el-col>
          <el-col :span="8">
            <div class="level-distribution">
              <h4>层级分布</h4>
              <el-progress v-for="(count, level) in searchResults.statistics.levelDistribution"
                          :key="level" :text-inside="true" :stroke-width="20"
                          :percentage="(count / searchResults.totalSections) * 100"
                          :format="() => `${level}: ${count}`" />
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 结果列表 -->
      <div class="results-list">
        <div v-for="(result, sectionId) in searchResults.results" :key="sectionId"
             class="result-item">
          <el-card>
            <div class="result-header">
              <div class="result-title">
                <h4>{{ sectionId }}</h4>
                <el-tag :type="getConfidenceTagType(result.confidence)">
                  置信度: {{ (result.confidence * 100).toFixed(1) }}%
                </el-tag>
              </div>
              <div class="result-actions">
                <el-button size="small" @click="previewContent(result)">预览</el-button>
                <el-button size="small" type="primary" @click="useContent(result)">使用</el-button>
                <el-button size="small" @click="viewSource(result)">查看来源</el-button>
              </div>
            </div>

            <div class="result-meta">
              <el-descriptions :column="4" size="small">
                <el-descriptions-item label="来源层级">{{ result.source }}</el-descriptions-item>
                <el-descriptions-item label="项目">{{ result.projectId }}</el-descriptions-item>
                <el-descriptions-item label="品类">{{ result.category }}</el-descriptions-item>
                <el-descriptions-item label="子品类">{{ result.subcategory }}</el-descriptions-item>
              </el-descriptions>
            </div>

            <div class="result-content">
              <el-text class="content-preview" truncated>
                {{ result.plainText }}
              </el-text>
            </div>
          </el-card>
        </div>
      </div>
    </el-card>

    <!-- 内容预览对话框 -->
    <ContentPreviewDialog
      v-model="showPreviewDialog"
      :content="previewContent"
      @use="useContent"
    />

    <!-- 来源详情对话框 -->
    <SourceDetailDialog
      v-model="showSourceDialog"
      :source-info="sourceInfo"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Setting, Plus, Delete } from '@element-plus/icons-vue'
import ContentPreviewDialog from './ContentPreviewDialog.vue'
import SourceDetailDialog from './SourceDetailDialog.vue'
import { recallApi } from '@/api/recall'
import type { RecallRequest, RecallResponse, SectionQuery } from '@/types/recall'

// 响应式数据
const searching = ref(false)
const showAdvancedSearch = ref(false)
const activeAdvanced = ref(['advanced'])
const showPreviewDialog = ref(false)
const showSourceDialog = ref(false)

const searchFormRef = ref()
const searchResults = ref<RecallResponse | null>(null)
const previewContent = ref(null)
const sourceInfo = ref(null)

// 基础选项数据
const projects = ref([])
const buList = ref([])
const categories = ref([])
const subcategories = ref([])
const docPurposes = ref([])
const paraPurposes = ref([])

// 搜索表单
const searchForm = reactive({
  projectId: '',
  bu: '',
  category: '',
  subcategory: '',
  docPurposeId: '',
  threshold: 0.85
})

// 高级设置
const advancedForm = reactive({
  maxResults: 10,
  searchMode: 'strict',
  excludeProjects: []
})

// 章节配置
const sections = ref<SectionQuery[]>([
  {
    id: Date.now(),
    sectionId: 'section_1',
    context: '',
    paraPurposeId: ''
  }
])

// 表单验证规则
const searchRules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  bu: [{ required: true, message: '请选择BU', trigger: 'change' }],
  category: [{ required: true, message: '请选择品类', trigger: 'change' }],
  subcategory: [{ required: true, message: '请选择子品类', trigger: 'change' }]
}

// 方法
const performSearch = async () => {
  try {
    await searchFormRef.value.validate()

    // 验证章节配置
    const validSections = sections.value.filter(s => s.sectionId && s.context)
    if (validSections.length === 0) {
      ElMessage.warning('请至少配置一个有效的章节')
      return
    }

    searching.value = true

    const request: RecallRequest = {
      ...searchForm,
      sections: validSections.map(s => ({
        sectionId: s.sectionId,
        context: s.context,
        paraPurposeId: s.paraPurposeId
      })),
      userId: getCurrentUserId()
    }

    const response = await recallApi.recall(request)
    searchResults.value = response.data

    ElMessage.success(`召回完成，成功匹配 ${response.data.successfulSections} 个章节`)

  } catch (error) {
    ElMessage.error('召回检索失败: ' + error.message)
  } finally {
    searching.value = false
  }
}

const addSection = () => {
  sections.value.push({
    id: Date.now(),
    sectionId: `section_${sections.value.length + 1}`,
    context: '',
    paraPurposeId: ''
  })
}

const removeSection = (index: number) => {
  if (sections.value.length > 1) {
    sections.value.splice(index, 1)
  } else {
    ElMessage.warning('至少需要保留一个章节')
  }
}

const resetForm = () => {
  searchFormRef.value.resetFields()
  sections.value = [{
    id: Date.now(),
    sectionId: 'section_1',
    context: '',
    paraPurposeId: ''
  }]
  searchResults.value = null
}

const previewContent = (result) => {
  previewContent.value = result
  showPreviewDialog.value = true
}

const useContent = (result) => {
  // 实现内容使用逻辑
  ElMessage.success('内容已添加到文档中')
}

const viewSource = (result) => {
  sourceInfo.value = result
  showSourceDialog.value = true
}

const getConfidenceTagType = (confidence: number) => {
  if (confidence >= 0.9) return 'success'
  if (confidence >= 0.8) return 'warning'
  return 'danger'
}

const formatThreshold = (value: number) => {
  return `${(value * 100).toFixed(0)}%`
}

const saveAsTemplate = () => {
  // 实现保存模板逻辑
  ElMessage.success('模板保存成功')
}

const getCurrentUserId = () => {
  // 获取当前用户ID
  return 'current_user_id'
}

// 生命周期
onMounted(() => {
  // 加载基础数据
  loadProjects()
  loadBuList()
  loadCategories()
  loadPurposes()
})

const loadProjects = async () => {
  // 实现项目数据加载
}

const loadBuList = async () => {
  // 实现BU数据加载
}

const loadCategories = async () => {
  // 实现品类数据加载
}

const loadPurposes = async () => {
  // 实现用途数据加载
}
</script>

<style scoped>
.recall-search-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sections-config {
  margin: 20px 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-item {
  margin-bottom: 15px;
}

.section-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-title {
  font-weight: bold;
  color: #409eff;
}

.search-actions {
  text-align: center;
  margin: 30px 0;
}

.results-card {
  margin-top: 20px;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.results-stats {
  display: flex;
  gap: 10px;
}

.statistics-section {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.level-distribution h4 {
  margin-bottom: 10px;
  color: #606266;
}

.results-list {
  margin-top: 20px;
}

.result-item {
  margin-bottom: 15px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.result-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.result-title h4 {
  margin: 0;
  color: #303133;
}

.result-actions {
  display: flex;
  gap: 8px;
}

.result-meta {
  margin-bottom: 15px;
}

.result-content {
  padding: 10px;
  background-color: #fafafa;
  border-radius: 4px;
}

.content-preview {
  color: #606266;
  line-height: 1.6;
}
</style>
```

#### 3.3.4 配置与优化

**应用配置**：

```yaml
# application.yml
milvus:
  host: localhost
  port: 19530
  database: doc_reuse
  collection:
    name: paragraph_vectors
    dimension: 1536
  index:
    type: IVF_FLAT
    metric: COSINE
    params: '{"nlist":1024}'

recall:
  confidence:
    threshold: 0.85
    min-threshold: 0.5
    max-threshold: 1.0

  search:
    max-results-per-level: 10
    max-total-results: 100
    timeout: 30s

  cache:
    enabled: true
    ttl: 1h
    max-size: 10000

  analytics:
    enabled: true
    retention-days: 90

embedding:
  provider: openai  # openai, qwen, local
  model: text-embedding-ada-002
  batch-size: 100
  timeout: 30s

  openai:
    api-key: ${OPENAI_API_KEY}
    base-url: https://api.openai.com/v1

  qwen:
    api-key: ${QWEN_API_KEY}
    base-url: https://dashscope.aliyuncs.com/api/v1

thread-pool:
  vector-processing:
    core-size: 5
    max-size: 20
    queue-capacity: 100
    keep-alive: 60s
```

**性能监控配置**：

```java
@Configuration
public class RecallMonitoringConfig {

    @Bean
    public MeterRegistry meterRegistry() {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Component
    public static class RecallMetrics {
        private final Counter recallRequestCounter;
        private final Timer recallTimer;
        private final Gauge confidenceGauge;

        public RecallMetrics(MeterRegistry registry) {
            this.recallRequestCounter = Counter.builder("recall.requests.total")
                .description("Total recall requests")
                .register(registry);

            this.recallTimer = Timer.builder("recall.processing.time")
                .description("Recall processing time")
                .register(registry);

            this.confidenceGauge = Gauge.builder("recall.confidence.average")
                .description("Average recall confidence")
                .register(registry, this, RecallMetrics::getAverageConfidence);
        }

        public void incrementRequest() {
            recallRequestCounter.increment();
        }

        public Timer.Sample startTimer() {
            return Timer.start(recallTimer);
        }

        private double getAverageConfidence() {
            // 实现平均置信度计算
            return 0.85;
        }
    }
}
```

#### 3.3.5 技术优势与实施建议

**技术优势**：

1. **高精度召回**：多层级策略确保在不同场景下都能找到合适的内容
2. **语义理解**：基于向量相似度的语义匹配，超越关键词匹配
3. **可扩展性**：Milvus向量数据库支持大规模数据和高并发查询
4. **用户友好**：直观的前端界面，支持实时搜索和结果预览
5. **性能优化**：缓存、批处理、异步处理等多种优化策略

**实施建议**：

1. **分阶段部署**：
   - 第一阶段：基础向量存储和简单召回
   - 第二阶段：多层级召回策略
   - 第三阶段：高级功能和优化

2. **数据准备**：
   - 建立高质量的训练数据集
   - 定期更新和维护向量索引
   - 监控数据质量和召回效果

3. **性能调优**：
   - 根据实际使用情况调整置信度阈值
   - 优化向量维度和索引参数
   - 实施有效的缓存策略

4. **用户培训**：
   - 提供详细的使用文档
   - 组织用户培训和反馈收集
   - 持续改进用户体验

这套完整的多层级召回系统为智能文档复用平台提供了强大的内容检索能力，能够在复杂的企业文档环境中实现高效、准确的内容复用。

### 3.4 单一内容源管理

* **内容源库**：存储所有可复用段落的 raw\_xml 于 Custom XML Part
* **绑定机制**：Word Content Control 标签 `content_id`
* **动态更新**：更新库中 raw\_xml 后，所有引用文档下次打开/更新域时自动刷新
* **API**：增、删、改、查 内容源段落

### 3.5 文档合成

* **动态绑定模式**：插入 Content Control
* **静态深拷贝模式**：深度复制 raw\_xml 节点
* **输出**：生成符合模版样式的 `.docx`
* **下载**：支持 Web、API 调用下载链接

## 4. 数据模型（JPA Entity设计）

### 4.1 核心实体类

```java
@Entity
@Table(name = "paragraph_chunks")
public class ParagraphChunk {
    @Id
    private String id; // UUID

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "bu", nullable = false)
    private String bu;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "subcategory", nullable = false)
    private String subcategory;

    @Column(name = "doc_purpose_id")
    private String docPurposeId;

    @Column(name = "para_purpose_id")
    private String paraPurposeId;

    @Column(name = "content_id")
    private String contentId; // 内容源ID，可为null

    @ElementCollection
    @CollectionTable(name = "paragraph_tags")
    private Set<String> tags = new HashSet<>();

    @Column(name = "approved")
    private Boolean approved = false;

    @Column(name = "raw_xml", columnDefinition = "TEXT")
    private String rawXml;

    @Column(name = "plain_text", columnDefinition = "TEXT")
    private String plainText; // 用于检索的纯文本

    @Column(name = "vector_id")
    private String vectorId; // Milvus中的向量ID

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 关联文档信息
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;
}

@Entity
@Table(name = "documents")
public class Document {
    @Id
    private String id;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "original_path")
    private String originalPath;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "bu", nullable = false)
    private String bu;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "subcategory", nullable = false)
    private String subcategory;

    @Column(name = "doc_purpose_id")
    private String docPurposeId;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private DocumentStatus status; // UPLOADED, PROCESSING, COMPLETED, FAILED

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL)
    private List<ParagraphChunk> paragraphs = new ArrayList<>();
}

@Entity
@Table(name = "content_sources")
public class ContentSource {
    @Id
    private String id; // content_id

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "raw_xml", columnDefinition = "TEXT")
    private String rawXml;

    @Column(name = "plain_text", columnDefinition = "TEXT")
    private String plainText;

    @Column(name = "project_id")
    private String projectId;

    @Column(name = "bu")
    private String bu;

    @Column(name = "category")
    private String category;

    @Column(name = "subcategory")
    private String subcategory;

    @Column(name = "para_purpose_id")
    private String paraPurposeId;

    @ElementCollection
    @CollectionTable(name = "content_source_tags")
    private Set<String> tags = new HashSet<>();

    @Column(name = "version")
    private Integer version = 1;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

## 5. 系统架构（单体应用）

### 5.1 整体架构
采用Vue3 + Spring Boot单体应用架构，前后端分离部署：

**前端层（Vue3 + TypeScript）**
- 文档上传与管理界面
- 检索与预览功能
- 文档合成与下载
- 用户权限管理

**后端层（Spring Boot）**
- **Controller层**：RESTful API接口
- **Service层**：业务逻辑处理
  - DocumentProcessingService：文档切分与解析
  - EmbeddingService：LLM标注与向量化
  - RecallService：多层级召回逻辑
  - ContentSourceService：内容源管理
  - DocumentCompositionService：文档合成
- **Repository层**：数据访问
- **Integration层**：外部服务集成

### 5.2 技术栈选择

**前端技术栈**
- Vue3 + TypeScript + Vite
- Element Plus UI组件库
- Axios HTTP客户端
- Pinia状态管理

**后端技术栈**
- Spring Boot 3.x + Java 17
- Spring Data JPA + MySQL/PostgreSQL
- Spring Security + JWT认证
- Apache POI：Word文档处理
- Milvus Java SDK：向量检索
- OkHttp3：LLM API调用
- Redis：缓存与会话管理

**基础设施**
- Milvus：向量数据库
- Redis：缓存
- MySQL/PostgreSQL：关系型数据库
- Nginx：反向代理
- Docker：容器化部署

## 6. 接口设计（Spring Boot RESTful API）

### 6.1 文档管理 API

```java
@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    // 上传文档并自动切分标注
    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
        @RequestParam("file") MultipartFile file,
        @RequestParam("projectId") String projectId,
        @RequestParam("bu") String bu,
        @RequestParam("category") String category,
        @RequestParam("subcategory") String subcategory
    );

    // 获取文档列表
    @GetMapping
    public ResponseEntity<PageResult<DocumentInfo>> getDocuments(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String projectId
    );
}
```

### 6.2 标注 API

```java
@RestController
@RequestMapping("/api/v1/annotation")
public class AnnotationController {

    // 自动标注段落用途
    @PostMapping("/annotate")
    public ResponseEntity<AnnotationResponse> annotate(
        @RequestBody AnnotationRequest request
    );

    // 批量标注
    @PostMapping("/batch-annotate")
    public ResponseEntity<BatchAnnotationResponse> batchAnnotate(
        @RequestBody BatchAnnotationRequest request
    );
}

// Request/Response DTOs
public class AnnotationRequest {
    private String text;
    private String context; // 可选上下文
}

public class AnnotationResponse {
    private String docPurposeId;
    private String paraPurposeId;
    private Double confidence;
}
```

### 6.3 召回检索 API

```java
@RestController
@RequestMapping("/api/v1/recall")
public class RecallController {

    // 多层级召回检索
    @PostMapping("/search")
    public ResponseEntity<RecallResponse> recall(
        @RequestBody RecallRequest request
    );

    // 相似段落推荐
    @PostMapping("/recommend")
    public ResponseEntity<RecommendationResponse> recommend(
        @RequestBody RecommendationRequest request
    );
}

public class RecallRequest {
    private String projectId;
    private String bu;
    private String category;
    private String subcategory;
    private String docPurposeId;
    private List<SectionQuery> sections;
    private Double threshold = 0.85;
}

public class SectionQuery {
    private String sectionId;
    private String context;
    private String paraPurposeId;
}

public class RecallResponse {
    private Map<String, SectionResult> results;
}

public class SectionResult {
    private String contentId;
    private String rawXml;
    private Double confidence;
    private String source; // 来源层级标识
}
```

### 6.4 内容源管理 API

```java
@RestController
@RequestMapping("/api/v1/content-sources")
public class ContentSourceController {

    @PostMapping
    public ResponseEntity<ContentSource> createContentSource(
        @RequestBody CreateContentSourceRequest request
    );

    @PutMapping("/{contentId}")
    public ResponseEntity<ContentSource> updateContentSource(
        @PathVariable String contentId,
        @RequestBody UpdateContentSourceRequest request
    );

    @DeleteMapping("/{contentId}")
    public ResponseEntity<Void> deleteContentSource(
        @PathVariable String contentId
    );

    @GetMapping("/{contentId}")
    public ResponseEntity<ContentSource> getContentSource(
        @PathVariable String contentId
    );

    @GetMapping
    public ResponseEntity<PageResult<ContentSource>> getContentSources(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String projectId
    );
}
```

### 6.5 文档合成 API

```java
@RestController
@RequestMapping("/api/v1/composition")
public class DocumentCompositionController {

    // 文档合成
    @PostMapping("/compose")
    public ResponseEntity<CompositionResponse> composeDocument(
        @RequestBody CompositionRequest request
    );

    // 下载合成文档
    @GetMapping("/download/{taskId}")
    public ResponseEntity<Resource> downloadDocument(
        @PathVariable String taskId
    );

    // 获取合成任务状态
    @GetMapping("/status/{taskId}")
    public ResponseEntity<CompositionStatus> getCompositionStatus(
        @PathVariable String taskId
    );
}

public class CompositionRequest {
    private String templatePath;
    private Map<String, SectionResult> recallResults;
    private Boolean useDynamicBinding = true;
    private String outputFormat = "docx";
}

public class CompositionResponse {
    private String taskId;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private String downloadUrl;
}
```

## 7. 非功能需求（Spring Boot实现）

### 7.1 性能需求
| 指标 | 目标值 | 实现方案 |
|------|--------|----------|
| 召回延迟 | < 200ms | Redis缓存 + Milvus索引优化 + 连接池 |
| 文档上传处理 | < 5s (10MB文档) | 异步处理 + 进度反馈 |
| 并发用户数 | 500 TPS | Spring Boot + Tomcat调优 + 数据库连接池 |
| 向量检索QPS | > 1000 | Milvus集群 + 查询优化 |

### 7.2 可用性需求
| 指标 | 目标值 | 实现方案 |
|------|--------|----------|
| 系统可用性 | 99.9% SLA | 健康检查 + 自动重启 + 负载均衡 |
| 数据备份 | 每日备份 | MySQL主从复制 + 定时备份脚本 |
| 故障恢复 | < 5分钟 | Docker容器化 + 快速重启机制 |

### 7.3 安全需求
```java
// Spring Security配置示例
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer().jwt()
            .and()
            .build();
    }
}
```

**安全措施**：
- JWT Token认证 + RBAC权限控制
- HTTPS/TLS加密传输
- 敏感数据加密存储
- API访问频率限制
- 审计日志记录

### 7.4 扩展性需求
```java
// 用途标签扩展示例
@Entity
@Table(name = "purpose_definitions")
public class PurposeDefinition {
    @Id
    private String purposeId;

    @Column(name = "purpose_name")
    private String purposeName;

    @Column(name = "description")
    private String description;

    @Column(name = "category") // DOC_PURPOSE, PARA_PURPOSE
    @Enumerated(EnumType.STRING)
    private PurposeCategory category;

    @Column(name = "active")
    private Boolean active = true;
}
```

**扩展能力**：
- 动态用途标签管理
- BU/品类/子品类配置化
- 插件化LLM模型支持
- 模块化架构设计

### 7.5 可维护性需求
```java
// 日志配置示例
@Slf4j
@Service
public class DocumentProcessingService {

    @Async
    public CompletableFuture<ProcessingResult> processDocument(String documentId) {
        log.info("开始处理文档: {}", documentId);

        try {
            // 处理逻辑
            ProcessingResult result = doProcess(documentId);
            log.info("文档处理完成: {}, 耗时: {}ms", documentId, result.getProcessingTime());
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("文档处理失败: {}", documentId, e);
            throw new DocumentProcessingException("处理失败", e);
        }
    }
}
```

**维护措施**：
- 结构化日志输出（JSON格式）
- Spring Boot Actuator健康检查
- Micrometer指标收集
- 完整的API文档（Swagger/OpenAPI）
- 单元测试覆盖率 > 80%

## 8. 实施方案与技术选型

### 8.1 开发阶段规划

**第一阶段：基础框架搭建（2-3周）**
- Spring Boot项目初始化，集成Spring Security + JWT
- Vue3前端项目搭建，集成Element Plus
- 数据库设计与JPA实体创建
- 基础的CRUD接口开发

**第二阶段：文档处理功能（3-4周）**
- Apache POI集成，实现Word文档解析
- 文档切分算法实现
- 文件上传与存储功能
- 基础的文档管理界面

**第三阶段：LLM集成与标注（2-3周）**
- LLM API客户端开发（支持多种模型）
- 自动标注服务实现
- 标注结果管理与人工校正功能

**第四阶段：向量检索功能（3-4周）**
- Milvus集成与向量存储
- 多层级召回算法实现
- 检索接口与前端界面开发

**第五阶段：文档合成与优化（2-3周）**
- 文档合成服务开发
- Content Control机制实现
- 性能优化与缓存策略

### 8.2 技术风险与应对

**风险1：Apache POI处理复杂Word文档的局限性**
- 应对：结合使用多个库（POI + docx4j），必要时调用外部服务
- 备选：集成LibreOffice API或OnlyOffice API

**风险2：向量检索性能瓶颈**
- 应对：使用Redis缓存热点数据，优化查询策略
- 备选：考虑使用PostgreSQL的pgvector扩展

**风险3：LLM API稳定性和成本**
- 应对：支持多个LLM提供商，实现降级策略
- 备选：集成本地部署的开源模型

**风险4：单体应用的扩展性限制**
- 应对：采用模块化设计，为后续微服务拆分做准备
- 备选：使用Spring Boot的多模块项目结构

### 8.3 部署与运维

**容器化部署**
```dockerfile
# 后端Dockerfile示例
FROM openjdk:17-jdk-slim
COPY target/doc-reuse-platform.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

**Docker Compose配置**
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
      - milvus

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: doc_reuse
      MYSQL_ROOT_PASSWORD: password

  redis:
    image: redis:7-alpine

  milvus:
    image: milvusdb/milvus:latest
```

**监控与日志**
- 使用Spring Boot Actuator暴露健康检查端点
- 集成Micrometer + Prometheus进行指标收集
- 使用Logback配置结构化日志输出
- 可选择集成ELK Stack进行日志分析

## 9. 可行性分析（更新）

### 9.1 技术可行性
* **Spring Boot生态成熟**：丰富的starter和第三方集成库
* **Apache POI文档处理**：Java生态最成熟的Office文档处理库
* **Milvus Java SDK**：官方支持，性能稳定
* **LLM API集成**：HTTP客户端调用，技术门槛低
* **Vue3前端开发**：现代化前端框架，开发效率高

### 9.2 性能可行性
* **召回延迟<200ms**：通过缓存和索引优化可以达到
* **500 TPS并发**：Spring Boot + 适当的硬件配置可以支持
* **向量检索性能**：Milvus专为高性能向量检索设计

### 9.3 维护可行性
* **单体应用部署简单**：减少运维复杂度
* **Java生态工具链完善**：IDE支持、调试工具、性能分析工具
* **模块化设计**：便于后续功能扩展和维护

### 9.4 成本可行性
* **开源技术栈**：大部分组件免费使用
* **内网部署**：无需外部服务依赖，数据安全可控
* **人员技能匹配**：Java + Vue技术栈人才储备充足

---
