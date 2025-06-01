
Ran tool

Ran tool
<no message>

Ran tool
基于对OnlyOffice Document Builder API和Editor API的研究，我将为您提供一个实现标题框架、表格、段落等Word文档内容辅助生成和格式排版的最佳方案。

## OnlyOffice实现文档内容辅助生成的最佳方案

OnlyOffice提供了两种主要的API方式来实现文档内容的辅助生成和格式排版：Document Builder API和Editor API。根据您的需求场景，我推荐以下实现方案：

### 方案一：基于Document Builder API的服务端实现

Document Builder API是一个强大的服务端文档生成工具，适合批量生成文档、模板填充和自动化文档处理场景。

#### 优势：
1. 完全服务端实现，无需用户交互
2. 支持批量生成和处理文档
3. 精确控制文档结构和格式
4. 适合模板化文档生成和填充

#### 实现步骤：

1. **创建文档生成服务**：
```javascript
// 使用Document Builder API创建文档生成服务
const express = require('express');
const app = express();
const axios = require('axios');

app.post('/generate-document', async (req, res) => {
  try {
    // 构建docbuilder脚本
    const script = generateDocBuilderScript(req.body);
    
    // 调用OnlyOffice Document Builder服务
    const response = await axios.post('http://your-onlyoffice-server/docbuilder', {
      async: false,
      url: script
    });
    
    // 返回生成的文档URL
    res.json({ url: response.data.urls });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

function generateDocBuilderScript(data) {
  // 根据请求数据生成脚本
  return `
    builder.CreateFile("docx");
    var oDocument = Api.GetDocument();
    
    // 创建标题样式
    var oHeadingStyle = oDocument.CreateStyle("Heading", "paragraph");
    var oParaPr = oHeadingStyle.GetParaPr();
    oParaPr.SetKeepNext(true);
    oParaPr.SetSpacingAfter(240);
    var oTextPr = oHeadingStyle.GetTextPr();
    oTextPr.SetFontSize(28);
    oTextPr.SetBold(true);
    
    // 添加标题
    var oParagraph = oDocument.GetElement(0);
    oParagraph.SetStyle(oHeadingStyle);
    oParagraph.AddText("${data.title}");
    
    // 添加表格
    var oTable = Api.CreateTable(${data.table.rows}, ${data.table.cols});
    // 填充表格内容
    ${generateTableContent(data.table.content)}
    
    oDocument.Push(oTable);
    
    // 添加段落内容
    ${data.paragraphs.map(p => `
      var oParagraph = Api.CreateParagraph();
      oParagraph.AddText("${p}");
      oDocument.Push(oParagraph);
    `).join('')}
    
    builder.SaveFile("docx", "result.docx");
    builder.CloseFile();
  `;
}

function generateTableContent(content) {
  // 生成表格内容填充代码
  let code = '';
  content.forEach((row, i) => {
    row.forEach((cell, j) => {
      code += `oTable.GetCell(${i}, ${j}).GetContent().GetElement(0).AddText("${cell}");\n`;
    });
  });
  return code;
}

app.listen(3000, () => {
  console.log('Document generation service running on port 3000');
});
```

2. **素材库智能填充**：
```javascript
function fillFromMaterialLibrary(docContent, materialLibrary) {
  let script = '';
  
  // 检查文档内容中需要填充的位置标记
  if (docContent.needsImage) {
    script += `
      var oDrawing = Api.CreateImage("${materialLibrary.getImage(docContent.imageType)}", 
                                    ${docContent.imageWidth}, 
                                    ${docContent.imageHeight});
      oParagraph.AddDrawing(oDrawing);
    `;
  }
  
  if (docContent.needsTable) {
    script += `
      var oTable = Api.CreateTable(${docContent.tableRows}, ${docContent.tableCols});
      ${generateTableFromTemplate(materialLibrary.getTableTemplate(docContent.tableType))}
      oDocument.Push(oTable);
    `;
  }
  
  return script;
}
```

### 方案二：基于Editor API的客户端交互实现

Editor API更适合需要用户交互的场景，可以在用户编辑过程中提供辅助功能。

#### 优势：
1. 实时交互体验
2. 用户可以在编辑过程中获得辅助
3. 适合协作编辑场景
4. 可以集成到现有的编辑流程中

#### 实现步骤：

1. **初始化编辑器并添加辅助功能**：
```javascript
// 初始化OnlyOffice编辑器
const docEditor = new DocsAPI.DocEditor("placeholder", {
  document: {
    fileType: "docx",
    key: "unique_document_key",
    title: "Document.docx",
    url: "https://example.com/url-to-document.docx"
  },
  documentType: "word",
  editorConfig: {
    mode: "edit",
    callbackUrl: "https://example.com/callback"
  }
});

// 创建连接器以与编辑器交互
const connector = docEditor.createConnector();

// 添加辅助功能按钮
function addAssistantButtons() {
  // 添加标题框架生成按钮
  connector.executeMethod("AddButton", {
    guid: "asc.{E6978D28-0441-4BD7-8346-82FAD68BCA3C}",
    data: {
      "icon": "https://example.com/icons/heading-icon.svg",
      "text": "插入标题框架",
      "sort": 1
    }
  }, function(result) {
    if (result.success) {
      // 按钮添加成功
      connector.attachEvent("onButtonClick", function(e) {
        if (e.guid === "asc.{E6978D28-0441-4BD7-8346-82FAD68BCA3C}") {
          insertHeadingFramework();
        }
      });
    }
  });
  
  // 添加表格模板按钮
  connector.executeMethod("AddButton", {
    guid: "asc.{F5978D28-0441-4BD7-8346-82FAD68BCA3D}",
    data: {
      "icon": "https://example.com/icons/table-icon.svg",
      "text": "插入表格模板",
      "sort": 2
    }
  }, function(result) {
    if (result.success) {
      connector.attachEvent("onButtonClick", function(e) {
        if (e.guid === "asc.{F5978D28-0441-4BD7-8346-82FAD68BCA3D}") {
          insertTableTemplate();
        }
      });
    }
  });
}

// 插入标题框架
function insertHeadingFramework() {
  connector.executeMethod("GetDocumentContent", null, function(content) {
    // 创建标题框架
    connector.executeMethod("AddContentControl", {
      type: "block",
      content: [
        { type: "paragraph", text: "文档标题", fontSize: 24, bold: true, alignment: "center" },
        { type: "paragraph", text: "副标题", fontSize: 16, italic: true, alignment: "center" },
        { type: "paragraph", text: "", fontSize: 12 }
      ]
    });
  });
}

// 插入表格模板
function insertTableTemplate() {
  // 显示表格模板选择对话框
  showTableTemplateDialog(function(template) {
    connector.executeMethod("InsertTable", {
      rows: template.rows,
      cols: template.cols,
      data: template.data
    });
  });
}

// 格式质量检查
function checkDocumentFormatting() {
  connector.executeMethod("GetDocumentContent", null, function(content) {
    const issues = analyzeFormatting(content);
    showFormattingIssues(issues);
  });
}
```

2. **错别字和格式检查功能**：
```javascript
// 检查文档格式和错别字
function checkDocumentQuality() {
  connector.executeMethod("GetDocumentContent", null, function(content) {
    // 格式检查
    const formattingIssues = checkFormatting(content);
    
    // 错别字检查 (可以调用外部API)
    checkSpelling(content.text).then(spellingIssues => {
      // 显示问题
      showDocumentIssues([...formattingIssues, ...spellingIssues]);
    });
  });
}

function checkFormatting(content) {
  const issues = [];
  
  // 检查标题格式
  content.paragraphs.forEach((paragraph, index) => {
    if (paragraph.isHeading) {
      if (paragraph.fontSize < 14) {
        issues.push({
          type: "formatting",
          message: `标题 "${paragraph.text}" 字体过小`,
          position: index,
          severity: "warning"
        });
      }
    }
  });
  
  // 检查表格格式
  content.tables.forEach((table, index) => {
    if (table.rows.length > 0 && !table.hasHeader) {
      issues.push({
        type: "formatting",
        message: "表格缺少表头",
        position: `table-${index}`,
        severity: "warning"
      });
    }
  });
  
  return issues;
}

// 显示文档问题
function showDocumentIssues(issues) {
  // 创建问题面板
  const panel = document.createElement("div");
  panel.className = "document-issues-panel";
  
  issues.forEach(issue => {
    const issueElement = document.createElement("div");
    issueElement.className = `issue ${issue.severity}`;
    issueElement.textContent = issue.message;
    
    // 点击定位到问题位置
    issueElement.addEventListener("click", () => {
      connector.executeMethod("GoToPosition", { position: issue.position });
    });
    
    panel.appendChild(issueElement);
  });
  
  document.body.appendChild(panel);
}
```

### 方案三：混合方案（推荐）

结合Document Builder API和Editor API的优势，实现更全面的解决方案。

#### 优势：
1. 兼顾自动化和交互式体验
2. 可以在服务端预处理文档，然后在客户端进行微调
3. 适合复杂的文档处理流程

#### 实现步骤：

1. **服务端预处理和模板生成**：
```javascript
// 服务端API
app.post('/prepare-document', async (req, res) => {
  try {
    // 根据请求生成文档模板
    const templateScript = generateTemplateScript(req.body);
    
    // 调用Document Builder生成模板
    const response = await axios.post('http://your-onlyoffice-server/docbuilder', {
      async: false,
      url: templateScript
    });
    
    // 返回生成的模板URL，供编辑器打开
    res.json({ 
      templateUrl: response.data.urls[Object.keys(response.data.urls)[0]],
      editorConfig: {
        // 编辑器配置
        document: {
          fileType: "docx",
          key: generateUniqueKey(),
          title: req.body.title || "Document.docx",
          url: response.data.urls[Object.keys(response.data.urls)[0]]
        },
        editorConfig: {
          mode: "edit",
          callbackUrl: "https://example.com/callback",
          customization: {
            plugins: {
              autostart: ["asc.{E6978D28-0441-4BD7-8346-82FAD68BCA3C}"],
              pluginsData: ["https://example.com/plugins/document-assistant/config.json"]
            }
          }
        }
      }
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});
```

2. **客户端编辑器增强插件**：

创建一个OnlyOffice插件，提供额外的辅助功能：

```javascript
// 插件配置 (config.json)
{
  "name": "Document Assistant",
  "guid": "asc.{E6978D28-0441-4BD7-8346-82FAD68BCA3C}",
  "variations": [
    {
      "description": "Document formatting assistant",
      "url": "index.html",
      "icons": ["icon.png", "icon@2x.png"],
      "isViewer": false,
      "EditorsSupport": ["word"],
      "isVisual": true,
      "isModal": false,
      "isInsideMode": true,
      "initDataType": "text",
      "initData": "",
      "buttons": [
        {
          "text": "Insert heading framework",
          "primary": true
        },
        {
          "text": "Insert table template",
          "primary": false
        },
        {
          "text": "Check formatting",
          "primary": false
        },
        {
          "text": "Check spelling",
          "primary": false
        },
        {
          "text": "Insert from material library",
          "primary": false
        }
      ]
    }
  ]
}
```

```javascript
// 插件主代码 (index.js)
(function(window, undefined) {
  window.Asc.plugin.init = function() {
    // 初始化插件UI
    createPluginInterface();
    
    // 加载素材库
    loadMaterialLibrary();
  };
  
  function createPluginInterface() {
    // 创建插件界面
    const container = document.getElementById('plugin-container');
    
    // 添加标题框架按钮
    const headingBtn = document.createElement('button');
    headingBtn.textContent = '插入标题框架';
    headingBtn.onclick = insertHeadingFramework;
    container.appendChild(headingBtn);
    
    // 添加表格模板按钮
    const tableBtn = document.createElement('button');
    tableBtn.textContent = '插入表格模板';
    tableBtn.onclick = showTableTemplateSelector;
    container.appendChild(tableBtn);
    
    // 添加格式检查按钮
    const formatBtn = document.createElement('button');
    formatBtn.textContent = '检查文档格式';
    formatBtn.onclick = checkDocumentFormatting;
    container.appendChild(formatBtn);
    
    // 添加错别字检查按钮
    const spellingBtn = document.createElement('button');
    spellingBtn.textContent = '检查错别字';
    spellingBtn.onclick = checkSpelling;
    container.appendChild(spellingBtn);
    
    // 添加素材库按钮
    const materialBtn = document.createElement('button');
    materialBtn.textContent = '从素材库插入';
    materialBtn.onclick = showMaterialLibrary;
    container.appendChild(materialBtn);
  }
  
  // 插入标题框架
  function insertHeadingFramework() {
    window.Asc.plugin.executeMethod("AddContentControl", [
      {
        "type": "block",
        "content": [
          { "type": "paragraph", "text": "文档标题", "fontSize": 24, "bold": true, "alignment": "center" },
          { "type": "paragraph", "text": "副标题", "fontSize": 16, "italic": true, "alignment": "center" },
          { "type": "paragraph", "text": "", "fontSize": 12 }
        ]
      }
    ]);
  }
  
  // 显示表格模板选择器
  function showTableTemplateSelector() {
    // 创建表格模板选择界面
    const selector = document.createElement('div');
    selector.className = 'table-template-selector';
    
    // 添加常用表格模板
    const templates = [
      { name: "简单表格", rows: 3, cols: 3 },
      { name: "带表头表格", rows: 4, cols: 3, hasHeader: true },
      { name: "财务报表", rows: 5, cols: 4, template: "financial" },
      { name: "项目进度", rows: 6, cols: 5, template: "project" }
    ];
    
    templates.forEach(template => {
      const item = document.createElement('div');
      item.className = 'template-item';
      item.textContent = template.name;
      item.onclick = () => insertTableTemplate(template);
      selector.appendChild(item);
    });
    
    // 显示选择器
    document.getElementById('plugin-container').appendChild(selector);
  }
  
  // 插入表格模板
  function insertTableTemplate(template) {
    // 构建表格数据
    let tableData = [];
    
    if (template.template === "financial") {
      tableData = [
        ["项目", "预算", "实际", "差异"],
        ["收入", "", "", ""],
        ["支出", "", "", ""],
        ["利润", "", "", ""]
      ];
    } else if (template.template === "project") {
      tableData = [
        ["任务", "负责人", "开始日期", "结束日期", "状态"],
        ["需求分析", "", "", "", ""],
        ["设计", "", "", "", ""],
        ["开发", "", "", "", ""],
        ["测试", "", "", "", ""]
      ];
    } else if (template.hasHeader) {
      tableData = [["表头1", "表头2", "表头3"]];
      for (let i = 1; i < template.rows; i++) {
        const row = [];
        for (let j = 0; j < template.cols; j++) {
          row.push("");
        }
        tableData.push(row);
      }
    } else {
      for (let i = 0; i < template.rows; i++) {
        const row = [];
        for (let j = 0; j < template.cols; j++) {
          row.push("");
        }
        tableData.push(row);
      }
    }
    
    // 插入表格
    window.Asc.plugin.executeMethod("InsertTable", [
      template.rows,
      template.cols,
      tableData
    ]);
  }
  
  // 检查文档格式
  function checkDocumentFormatting() {
    window.Asc.plugin.executeMethod("GetDocumentContent", [], function(content) {
      if (content) {
        // 分析文档格式
        const issues = analyzeFormatting(JSON.parse(content));
        
        // 显示格式问题
        showFormattingIssues(issues);
      }
    });
  }
  
  // 分析文档格式
  function analyzeFormatting(content) {
    const issues = [];
    
    // 检查段落格式
    if (content.paragraphs) {
      content.paragraphs.forEach((paragraph, index) => {
        // 检查段落间距
        if (paragraph.spacing && paragraph.spacing.after < 6 && index < content.paragraphs.length - 1) {
          issues.push({
            type: "spacing",
            message: `第${index+1}段落后间距过小，建议增加间距`,
            position: index,
            severity: "warning"
          });
        }
        
        // 检查字体一致性
        if (paragraph.runs && paragraph.runs.length > 1) {
          const fonts = new Set();
          paragraph.runs.forEach(run => {
            if (run.font) fonts.add(run.font);
          });
          
          if (fonts.size > 2) {
            issues.push({
              type: "font",
              message: `第${index+1}段落使用了多种字体，建议保持一致`,
              position: index,
              severity: "warning"
            });
          }
        }
      });
    }
    
    // 检查表格格式
    if (content.tables) {
      content.tables.forEach((table, index) => {
        // 检查表格宽度
        if (table.width && table.width > 100) {
          issues.push({
            type: "table",
            message: `第${index+1}个表格宽度超出页面边距`,
            position: `table-${index}`,
            severity: "error"
          });
        }
        
        // 检查表格单元格内容
        if (table.rows) {
          table.rows.forEach((row, rowIndex) => {
            if (row.cells) {
              row.cells.forEach((cell, cellIndex) => {
                if (cell.content && cell.content.length > 500) {
                  issues.push({
                    type: "table",
                    message: `表格(${index+1})中单元格(${rowIndex+1},${cellIndex+1})内容过多`,
                    position: `table-${index}-cell-${rowIndex}-${cellIndex}`,
                    severity: "warning"
                  });
                }
              });
            }
          });
        }
      });
    }
    
    return issues;
  }
  
  // 显示格式问题
  function showFormattingIssues(issues) {
    // 创建问题显示面板
    const panel = document.createElement('div');
    panel.className = 'formatting-issues';
    
    if (issues.length === 0) {
      panel.innerHTML = '<p>未发现格式问题</p>';
    } else {
      const list = document.createElement('ul');
      
      issues.forEach(issue => {
        const item = document.createElement('li');
        item.className = issue.severity;
        item.textContent = issue.message;
        
        // 点击定位到问题位置
        item.onclick = () => {
          window.Asc.plugin.executeMethod("GoToPosition", [issue.position]);
        };
        
        list.appendChild(item);
      });
      
      panel.appendChild(list);
    }
    
    document.getElementById('plugin-container').appendChild(panel);
  }
  
  // 检查错别字
  function checkSpelling() {
    window.Asc.plugin.executeMethod("GetDocumentContent", [], function(content) {
      if (content) {
        // 提取文本内容
        const text = extractText(JSON.parse(content));
        
        // 调用拼写检查API (示例)
        checkSpellingAPI(text).then(spellingIssues => {
          showSpellingIssues(spellingIssues);
        });
      }
    });
  }
  
  // 提取文本内容
  function extractText(content) {
    let text = '';
    
    // 从段落提取
    if (content.paragraphs) {
      content.paragraphs.forEach(paragraph => {
        if (paragraph.text) {
          text += paragraph.text + ' ';
        }
      });
    }
    
    // 从表格提取
    if (content.tables) {
      content.tables.forEach(table => {
        if (table.rows) {
          table.rows.forEach(row => {
            if (row.cells) {
              row.cells.forEach(cell => {
                if (cell.content) {
                  text += cell.content + ' ';
                }
              });
            }
          });
        }
      });
    }
    
    return text;
  }
  
  // 调用拼写检查API (示例)
  function checkSpellingAPI(text) {
    // 这里应该调用实际的拼写检查API
    // 示例返回格式
    return new Promise(resolve => {
      setTimeout(() => {
        resolve([
          { word: "teh", suggestion: "the", position: 45 },
          { word: "documnet", suggestion: "document", position: 120 }
        ]);
      }, 500);
    });
  }
  
  // 显示拼写问题
  function showSpellingIssues(issues) {
    // 创建拼写问题面板
    const panel = document.createElement('div');
    panel.className = 'spelling-issues';
    
    if (issues.length === 0) {
      panel.innerHTML = '<p>未发现拼写错误</p>';
    } else {
      const list = document.createElement('ul');
      
      issues.forEach(issue => {
        const item = document.createElement('li');
        item.textContent = `"${issue.word}" 可能应为 "${issue.suggestion}"`;
        
        // 创建修复按钮
        const fixBtn = document.createElement('button');
        fixBtn.textContent = '修复';
        fixBtn.onclick = () => {
          window.Asc.plugin.executeMethod("ReplaceTextAtPosition", [
            issue.position,
            issue.word.length,
            issue.suggestion
          ]);
        };
        
        item.appendChild(fixBtn);
        list.appendChild(item);
      });
      
      panel.appendChild(list);
    }
    
    document.getElementById('plugin-container').appendChild(panel);
  }
  
  // 显示素材库
  function showMaterialLibrary() {
    // 创建素材库界面
    const libraryPanel = document.createElement('div');
    libraryPanel.className = 'material-library';
    
    // 添加素材分类
    const categories = [
      { name: "标题模板", type: "heading" },
      { name: "表格模板", type: "table" },
      { name: "图表模板", type: "chart" },
      { name: "常用段落", type: "paragraph" },
      { name: "图片素材", type: "image" }
    ];
    
    // 创建分类选择器
    const categorySelector = document.createElement('div');
    categorySelector.className = 'category-selector';
    
    categories.forEach(category => {
      const item = document.createElement('div');
      item.className = 'category-item';
      item.textContent = category.name;
      item.onclick = () => showMaterialsByCategory(category.type);
      categorySelector.appendChild(item);
    });
    
    libraryPanel.appendChild(categorySelector);
    
    // 添加素材显示区域
    const materialsContainer = document.createElement('div');
    materialsContainer.id = 'materials-container';
    materialsContainer.className = 'materials-container';
    libraryPanel.appendChild(materialsContainer);
    
    document.getElementById('plugin-container').appendChild(libraryPanel);
    
    // 默认显示第一个分类
    showMaterialsByCategory(categories[0].type);
  }
  
  // 显示指定分类的素材
  function showMaterialsByCategory(type) {
    // 获取素材容器
    const container = document.getElementById('materials-container');
    container.innerHTML = '';
    
    // 根据分类加载素材
    const materials = getMaterialsByCategory(type);
    
    materials.forEach(material => {
      const item = document.createElement('div');
      item.className = 'material-item';
      
      // 添加预览
      if (material.preview) {
        const preview = document.createElement('div');
        preview.className = 'material-preview';
        
        if (material.previewType === 'image') {
          const img = document.createElement('img');
          img.src = material.preview;
          preview.appendChild(img);
        } else {
          preview.innerHTML = material.preview;
        }
        
        item.appendChild(preview);
      }
      
      // 添加标题
      const title = document.createElement('div');
      title.className = 'material-title';
      title.textContent = material.name;
      item.appendChild(title);
      
      // 点击插入素材
      item.onclick = () => insertMaterial(material);
      
      container.appendChild(item);
    });
  }
  
  // 获取指定分类的素材
  function getMaterialsByCategory(type) {
    // 这里应该从服务器或本地缓存获取素材
    // 示例数据
    const materialsByType = {
      'heading': [
        { id: 'h1', name: '标准标题', content: { text: '标准标题', fontSize: 24, bold: true, alignment: 'center' } },
        { id: 'h2', name: '带副标题', content: [
          { text: '主标题', fontSize: 24, bold: true, alignment: 'center' },
          { text: '副标题', fontSize: 16, italic: true, alignment: 'center' }
        ]}
      ],
      'table': [
        { id: 't1', name: '简单表格', content: { rows: 3, cols: 3 } },
        { id: 't2', name: '财务报表', content: { template: 'financial' } }
      ],
      'paragraph': [
        { id: 'p1', name: '项目介绍段落', content: { text: '本项目旨在提供一个全面的解决方案，满足客户的
