<template>
  <el-dialog title="分派专家" :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)">
    <el-select multiple placeholder="请选择要分派的专家">
      <el-option label="张专家" value="expert1"></el-option>
      <el-option label="李专家" value="expert2"></el-option>
    </el-select>
    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" @click="$emit('update:modelValue', false)">确认分派</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
defineProps<{
  modelValue: boolean;
}>();
defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
}>();
</script>
