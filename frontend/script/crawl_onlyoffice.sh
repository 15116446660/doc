#!/bin/bash

# ======================
# Configuration
# ======================

# 起始 URL，默认是 OnlyOffice API 文档主页
START_URL=${1:-"https://api.onlyoffice.com/"}

# Firecrawl 本地服务地址（你部署的 Docker 容器）
FIRECRAWL_API_URL="http://localhost:3002/v1/crawl"

# 输出文件
JSON_OUTPUT="rag-data.json"
MD_OUTPUT="rag-data.md"

# ======================
# 执行爬虫请求
# ======================

echo "🚀 正在向 Firecrawl 请求爬取：$START_URL"

RESPONSE=$(curl -s -X POST "$FIRECRAWL_API_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "url": "'"$START_URL"'"
  }')

# ======================
# 处理结果并提取字段
# ======================

# 提取所需字段，并生成 JSON 输出
echo "$RESPONSE" | jq '[.pages[] | {title: .title, content: .text, url: .url}]' > "$JSON_OUTPUT"

# 生成 Markdown 文件
echo "$RESPONSE" | jq -r '.pages[] | "# \(.title)\n\n\(.text)\n\n[原始链接](\(.url))\n\n---\n"' > "$MD_OUTPUT"

# ======================
# 完成提示
# ======================

echo "✅ JSON 输出已保存到: $JSON_OUTPUT"
echo "✅ Markdown 输出已保存到: $MD_OUTPUT"
