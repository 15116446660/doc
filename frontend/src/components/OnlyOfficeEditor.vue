<template>
  <div :id="editorId" class="onlyoffice-editor-container"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue';
import { v4 as uuidv4 } from 'uuid';
import request from '@/utils/request'; // Using a generic request for now

interface EditorProps {
  documentId: number;
}

const props = defineProps<EditorProps>();
const editorId = ref(`onlyoffice-editor-${uuidv4()}`);
let docEditor: any = null;

const createEditor = async (docId: number) => {
  if (docEditor) {
    docEditor.destroyEditor();
    docEditor = null;
  }

  try {
    // Fetch the config from our backend
    const config = await request({
      url: `/api/onlyoffice/config/${docId}`,
      method: 'get',
    });

    // The 'DocsAPI' is loaded from the script in index.html
    if ((window as any).DocsAPI) {
      docEditor = new (window as any).DocsAPI.DocEditor(editorId.value, config);
    } else {
      console.error('OnlyOffice DocsAPI not found. Check if the API script is loaded.');
    }
  } catch (error) {
    console.error('Failed to load OnlyOffice editor config:', error);
  }
};

onMounted(() => {
  if (props.documentId) {
    createEditor(props.documentId);
  }
});

onUnmounted(() => {
  if (docEditor) {
    docEditor.destroyEditor();
  }
});

watch(() => props.documentId, (newId) => {
  if (newId) {
    createEditor(newId);
  }
});
</script>

<style scoped>
.onlyoffice-editor-container {
  width: 100%;
  height: 80vh; /* Default height, can be overridden by parent */
  border: 1px solid #e5e7eb;
}
</style>
