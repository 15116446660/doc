<template>
  <div>
    <h1>问题详情 (Issue Detail)</h1>
    <p>问题ID: {{ $route.params.id }}</p>
    <IssueEditor />
  </div>
</template>

<script setup lang="ts">
import IssueEditor from './components/IssueEditor.vue';
</script>
