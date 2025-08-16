<template>
  <el-avatar 
    :size="size" 
    :src="picture" 
    :class="avatarClass"
  >
    {{ userName ? userName.substring(0, 1).toUpperCase() : '?' }}
  </el-avatar>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  picture?: string
  userName?: string
  size?: number
  class?: string
}>()

const avatarClass = computed(() => props.class || '')
</script>

<style scoped>
.el-avatar {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: #fff;
  font-weight: 500;
}
</style> 