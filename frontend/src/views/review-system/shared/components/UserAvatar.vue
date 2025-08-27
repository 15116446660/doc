<template>
  <el-avatar :src="src">{{ initials }}</el-avatar>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  src?: string;
  name: string;
}>();

const initials = computed(() => {
  if (!props.name) return '';
  return props.name.substring(0, 1).toUpperCase();
});
</script>
