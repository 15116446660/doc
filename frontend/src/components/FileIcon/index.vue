<template>
  <div class="file-icon" v-html="iconSvg" :title="format"></div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'

const props = withDefaults(defineProps<{
  format?: string
}>(), {
  format: 'unknown'
})

const iconSvg = ref<string>('')

const icons = import.meta.glob('@/assets/svg/file-types/*.svg', { as: 'raw' })

async function loadIcon() {
  const formatKey = props.format?.toLowerCase() || 'unknown'
  const iconPath = `/src/assets/svg/file-types/${formatKey}.svg`
  
  if (icons[iconPath]) {
    iconSvg.value = await icons[iconPath]()
  } else {
    // Fallback to unknown if specific icon not found
    iconSvg.value = await icons['/src/assets/svg/file-types/unknown.svg']()
  }
}

watch(() => props.format, loadIcon)

onMounted(loadIcon)
</script>

<style scoped>
.file-icon {
  width: 100%;
  height: 100%;
  display: inline-flex;
  justify-content: center;
  align-items: center;
}
.file-icon :deep(svg) {
  width: 100%;
  height: 100%;
}
</style> 