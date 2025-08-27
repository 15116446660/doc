<template>
  <el-badge :value="priority" :type="type">
    <div class="badge-content"></div>
  </el-badge>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  priority: string;
}>();

const type = computed(() => {
  switch (props.priority) {
    case '高':
      return 'danger';
    case '中':
      return 'warning';
    case '低':
      return 'info';
    default:
      return 'primary';
  }
});
</script>

<style scoped>
.badge-content {
    width: 10px;
    height: 10px;
}
</style>
