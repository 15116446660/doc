<template>
  <div class="workflow-designer">
    <div class="toolbar">
      <!-- Toolbar buttons for adding nodes, etc. -->
      <el-button>添加步骤</el-button>
    </div>
    <div class="canvas-container">
        <FlowChart />
    </div>
    <div class="inspector">
        <NodeEditor />
    </div>
  </div>
</template>

<script setup lang="ts">
import FlowChart from './FlowChart.vue';
import NodeEditor from './NodeEditor.vue';
</script>

<style scoped>
.workflow-designer {
  display: flex;
  height: 70vh;
}
.toolbar {
  padding: 10px;
  border-right: 1px solid #dcdfe6;
}
.canvas-container {
  flex-grow: 1;
  padding: 20px;
  background-color: #f5f7fa;
}
.inspector {
  width: 300px;
  padding: 10px;
  border-left: 1px solid #dcdfe6;
}
</style>
