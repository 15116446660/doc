<template>
  <el-dialog
    :model-value="modelValue"
    title="创建评审任务"
    width="50%"
    @update:model-value="$emit('update:modelValue', $event)"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="任务名称" prop="taskName">
        <el-input v-model="form.taskName" placeholder="请输入任务名称"></el-input>
      </el-form-item>
      <el-form-item label="任务描述" prop="taskDescription">
        <el-input
          v-model="form.taskDescription"
          type="textarea"
          placeholder="请输入任务描述"
        ></el-input>
      </el-form-item>
      <el-form-item label="优先级" prop="priority">
        <el-select v-model="form.priority" placeholder="请选择优先级">
          <el-option label="高" value="HIGH"></el-option>
          <el-option label="中" value="MEDIUM"></el-option>
          <el-option label="低" value="LOW"></el-option>
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="$emit('update:modelValue', false)">取消</el-button>
        <el-button type="primary" @click="handleSubmit">
          创建
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';

const props = defineProps<{
  modelValue: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
  (e: 'task-created'): void;
}>();

const formRef = ref<FormInstance>();
const form = reactive({
  taskName: '',
  taskDescription: '',
  priority: 'MEDIUM',
});

const rules = reactive<FormRules>({
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
});

const handleClose = () => {
  formRef.value?.resetFields();
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  await formRef.value.validate((valid) => {
    if (valid) {
      // Here we would call the store action
      console.log('Form submitted:', form);
      emit('task-created', { ...form });
      // ElMessage.success('任务创建成功 (Mock)');
      // emit('update:modelValue', false);
    } else {
      ElMessage.error('请填写必要的字段');
    }
  });
};
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}
</style>
