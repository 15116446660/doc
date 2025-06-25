<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue';
import { fetchSSE } from '@/utils/sseStreamParser';
import MarkdownIt from 'markdown-it';
import hljs from 'highlight.js';
import 'highlight.js/styles/github.css';

// Initialize markdown parser with code highlighting
const md = new MarkdownIt({
  html: false,
  linkify: true,
  typographer: true,
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(str, { language: lang }).value;
      } catch (__) {}
    }
    return ''; // use external default escaping
  }
});

// State
const prompt = ref('');
const responseContent = ref('');
const isLoading = ref(false);
const error = ref<string | null>(null);
const chatHistory = ref<Array<{role: 'user' | 'assistant', content: string}>>([]);

// Computed properties
const renderedMarkdown = computed(() => {
  return md.render(responseContent.value || '');
});

// Methods
async function handleSubmit() {
  if (!prompt.value.trim()) return;
  
  error.value = null;
  isLoading.value = true;
  
  // Add user message to chat history
  chatHistory.value.push({
    role: 'user',
    content: prompt.value
  });
  
  // Reset response content for new streaming
  responseContent.value = '';
  
  // Prepare the messages for API
  const messages = chatHistory.value.map(msg => ({
    role: msg.role,
    content: msg.content
  }));
  
  try {
    // Send the request and process the stream
    await fetchSSE('/api/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ messages }),
    }, {
      onMessage: (chunk) => {
        // Update response content incrementally
        responseContent.value += chunk;
        // Update UI immediately with each chunk
        nextTick(() => {
          scrollToBottom();
        });
      },
      onComplete: () => {
        isLoading.value = false;
        // Add completed response to chat history
        if (responseContent.value.trim()) {
          chatHistory.value.push({
            role: 'assistant',
            content: responseContent.value
          });
        }
        scrollToBottom();
      },
      onError: (err) => {
        error.value = err.message;
        isLoading.value = false;
      }
    });
    
    // Clear prompt after successful submission
    prompt.value = '';
  } catch (err) {
    console.error('Error fetching response:', err);
    error.value = err instanceof Error ? err.message : 'An error occurred';
  } finally {
    isLoading.value = false;
  }
}

function scrollToBottom() {
  const chatContainer = document.querySelector('.chat-container');
  if (chatContainer) {
    chatContainer.scrollTop = chatContainer.scrollHeight;
  }
}

onMounted(() => {
  // Focus the input field when component is mounted
  const inputEl = document.querySelector('.prompt-input') as HTMLElement;
  if (inputEl) inputEl.focus();
});
</script>

<template>
  <div class="streaming-chat">
    <div class="chat-container">
      <!-- Chat history -->
      <div class="message-list">
        <div 
          v-for="(message, index) in chatHistory" 
          :key="index"
          :class="['message', message.role]"
        >
          <div class="avatar">
            {{ message.role === 'user' ? '👤' : '🤖' }}
          </div>
          <div class="content">
            <div v-if="message.role === 'user'" class="message-text">
              {{ message.content }}
            </div>
            <div 
              v-else 
              class="message-text markdown-body"
              v-html="md.render(message.content)"
            ></div>
          </div>
        </div>
        
        <!-- Streaming response (if active) -->
        <div v-if="isLoading && responseContent" class="message assistant">
          <div class="avatar">🤖</div>
          <div class="content">
            <div class="message-text markdown-body" v-html="renderedMarkdown"></div>
            <div class="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Error message -->
    <div v-if="error" class="error-message">
      {{ error }}
    </div>
    
    <!-- Input area -->
    <div class="input-container">
      <textarea
        v-model="prompt"
        class="prompt-input"
        placeholder="Type your message..."
        @keydown.enter.prevent="handleSubmit"
        :disabled="isLoading"
      ></textarea>
      <button 
        class="send-button" 
        @click="handleSubmit"
        :disabled="isLoading || !prompt.trim()"
      >
        {{ isLoading ? 'Generating...' : 'Send' }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.streaming-chat {
  display: flex;
  flex-direction: column;
  height: 100%;
  max-width: 800px;
  margin: 0 auto;
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
  background-color: #f5f5f5;
  border-radius: 8px;
  margin-bottom: 1rem;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.message {
  display: flex;
  gap: 0.5rem;
}

.message.user {
  justify-content: flex-end;
}

.avatar {
  width: 32px;
  height: 32px;
  background-color: #e0e0e0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.message.user .avatar {
  order: 2;
  background-color: #dcf8c6;
}

.content {
  max-width: 70%;
  padding: 0.75rem;
  border-radius: 8px;
  background-color: white;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.message.user .content {
  background-color: #dcf8c6;
}

.message-text {
  white-space: pre-wrap;
  word-break: break-word;
}

.input-container {
  display: flex;
  gap: 0.5rem;
  padding: 0.5rem;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 -1px 5px rgba(0, 0, 0, 0.05);
}

.prompt-input {
  flex: 1;
  padding: 0.75rem;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  resize: none;
  min-height: 40px;
  max-height: 120px;
  font-family: inherit;
  font-size: inherit;
}

.send-button {
  padding: 0 1.5rem;
  background-color: #4caf50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
}

.send-button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.error-message {
  color: #d32f2f;
  margin-bottom: 0.5rem;
  padding: 0.5rem;
  background-color: #ffebee;
  border-radius: 4px;
  text-align: center;
}

.typing-indicator {
  display: flex;
  align-items: center;
  column-gap: 4px;
  margin-top: 8px;
}

.typing-indicator span {
  height: 8px;
  width: 8px;
  background-color: #606060;
  border-radius: 50%;
  display: block;
  opacity: 0.4;
  animation: typing 1s infinite alternate;
}

.typing-indicator span:nth-child(1) {
  animation-delay: 0s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.3s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.6s;
}

@keyframes typing {
  0% {
    opacity: 0.4;
    transform: translateY(0);
  }
  100% {
    opacity: 1;
    transform: translateY(-5px);
  }
}
</style>

<style>
/* Markdown styles */
.markdown-body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial, sans-serif;
  font-size: 16px;
  line-height: 1.6;
  word-wrap: break-word;
}

.markdown-body pre {
  padding: 16px;
  overflow: auto;
  font-size: 85%;
  line-height: 1.45;
  background-color: #f6f8fa;
  border-radius: 6px;
  margin-top: 0;
  margin-bottom: 16px;
}

.markdown-body code {
  padding: 0.2em 0.4em;
  margin: 0;
  font-size: 85%;
  background-color: rgba(175, 184, 193, 0.2);
  border-radius: 6px;
}

.markdown-body pre code {
  padding: 0;
  background-color: transparent;
}

.markdown-body table {
  border-spacing: 0;
  border-collapse: collapse;
  margin-top: 0;
  margin-bottom: 16px;
  width: 100%;
  overflow: auto;
}

.markdown-body table th,
.markdown-body table td {
  padding: 6px 13px;
  border: 1px solid #d0d7de;
}

.markdown-body table tr {
  background-color: #ffffff;
  border-top: 1px solid #d0d7de;
}

.markdown-body table tr:nth-child(2n) {
  background-color: #f6f8fa;
}

.markdown-body blockquote {
  padding: 0 1em;
  color: #57606a;
  border-left: 0.25em solid #d0d7de;
  margin: 0 0 16px 0;
}
</style> 