<script setup lang="ts">
import StreamingChat from '@/components/StreamingChat.vue';
</script>

<template>
  <div class="app-container">
    <header class="app-header">
      <h1>AI Streaming Chat Demo</h1>
      <p class="subtitle">Experience real-time AI responses with Markdown support</p>
    </header>
    
    <main class="app-content">
      <StreamingChat />
    </main>
    
    <footer class="app-footer">
      <p>
        Try asking about <strong>Markdown</strong> to see formatting examples or about 
        <strong>streaming SSE issues</strong> to learn more about the implementation.
      </p>
    </footer>
  </div>
</template>

<style>
body {
  margin: 0;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  background-color: #f5f5f5;
  color: #333;
  line-height: 1.6;
}

.app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  max-width: 1200px;
  margin: 0 auto;
  padding: 1rem;
}

.app-header {
  text-align: center;
  margin-bottom: 2rem;
  padding: 1rem;
}

.app-header h1 {
  margin: 0;
  font-size: 2.5rem;
  color: #2c3e50;
}

.subtitle {
  color: #666;
  font-size: 1.2rem;
  margin-top: 0.5rem;
}

.app-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.app-footer {
  margin-top: 2rem;
  text-align: center;
  padding: 1rem;
  border-top: 1px solid #eee;
  font-size: 0.9rem;
  color: #666;
}
</style>
