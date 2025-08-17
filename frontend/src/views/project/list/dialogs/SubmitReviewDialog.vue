<template>
  <el-dialog v-model="dialogVisible" title="Submit for Review" width="500px" @close="handleClose">
    <el-form ref="formRef" :model="formData" label-width="120px">
      <el-form-item label="Document">
        <span>{{ documentName }}</span>
      </el-form-item>
      <el-form-item label="Review Template" prop="templateId">
        <el-select v-model="formData.templateId" placeholder="Select a review template">
          <!-- TODO: Fetch templates from API -->
          <el-option label="Standard 3-Step Review" :value="1" />
          <el-option label="Quick Review" :value="2" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="handleSubmit">Submit</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import request from '@/utils/request'; // Generic request

const dialogVisible = ref(false);
const documentId = ref<number | null>(null);
const documentName = ref('');

const formData = reactive({
  templateId: null as number | null,
});

const open = (doc: { id: number, name: string }) => {
  documentId.value = doc.id;
  documentName.value = doc.name;
  dialogVisible.value = true;
};

const handleClose = () => {
  formData.templateId = null;
};

const handleSubmit = async () => {
  if (!formData.templateId) {
    ElMessage.warning('Please select a review template.');
    return;
  }

  try {
    await request({
      url: '/api/reviews/submit',
      method: 'post',
      data: {
        documentId: documentId.value,
        templateId: formData.templateId,
      },
    });
    ElMessage.success('Successfully submitted for review.');
    dialogVisible.value = false;
  } catch (error) {
    ElMessage.error('Submission failed.');
  }
};

defineExpose({
  open,
});
</script>
