<template>
  <el-tag :type="type">{{ status }}</el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  status: string;
}>();

const type = computed(() => {
  switch (props.status) {
    case '进行中':
      return 'primary';
    case '已完成':
      return 'success';
    case '开放':
      return 'danger';
    default:
      return 'info';
  }
});
</script>
