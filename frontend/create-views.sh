#!/bin/bash

# 创建基本的Vue组件模板
create_vue_component() {
    local path=$1
    local title=$2
    mkdir -p "src/views/$path"
    cat > "src/views/$path/index.vue" << EOF
<template>
  <div class="container">
    <h1>${title}</h1>
    <div class="content">
      <!-- 内容区域 -->
    </div>
  </div>
</template>

<script setup lang="ts">
// 组件逻辑
</script>

<style scoped>
.container {
  padding: 20px;
}

.content {
  margin-top: 20px;
}
</style>
EOF
}

# 项目管理
create_vue_component "project/overview" "项目概览"
create_vue_component "project/create" "项目创建"
create_vue_component "project/list" "项目列表"
create_vue_component "project/board" "项目看板"

# 标书管理
create_vue_component "document/edit" "标书编辑"
create_vue_component "document/template" "模板中心"
create_vue_component "document/content" "内容库"
create_vue_component "document/approval" "审批中心"

# 智能工具
create_vue_component "tools/analysis" "智能分析"
create_vue_component "tools/compliance" "合规检查"
create_vue_component "tools/risk" "风险预警"
create_vue_component "tools/failure" "废标分析"

# 陪标管理
create_vue_component "companion/space" "多标书空间"
create_vue_component "companion/duplicate" "查重中心"
create_vue_component "companion/difference" "差异化工具"

# 知识中心
create_vue_component "knowledge/regulation" "法规政策"
create_vue_component "knowledge/cases" "案例库"
create_vue_component "knowledge/industry" "行业知识"
create_vue_component "knowledge/qa" "智能问答"

# 数据分析
create_vue_component "analysis/bid" "投标数据"
create_vue_component "analysis/market" "市场分析"
create_vue_component "analysis/report" "自定义报表"

# 系统管理
create_vue_component "system/user" "用户权限"
create_vue_component "system/organization" "组织架构"
create_vue_component "system/workflow" "工作流配置"
create_vue_component "system/integration" "系统集成"
create_vue_component "system/ai" "AI配置" 