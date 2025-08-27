<template>
  <div class="tracking-board">
    <div class="column">
      <h3>待处理</h3>
      <IssueCard :issue="{ title: '问题1', status: '开放', severity: '高', category: '内容' }" />
    </div>
    <div class="column">
      <h3>进行中</h3>
       <IssueCard :issue="{ title: '问题3', status: '进行中', severity: '低', category: '格式' }" />
    </div>
    <div class="column">
      <h3>已解决</h3>
       <IssueCard :issue="{ title: '问题2', status: '已解决', severity: '中', category: '逻辑' }" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import IssueCard from './IssueCard.vue';
</script>

<style scoped>
.tracking-board {
  display: flex;
  justify-content: space-around;
}
.column {
  width: 30%;
  background-color: #f4f4f5;
  padding: 10px;
  border-radius: 4px;
}
</style>
