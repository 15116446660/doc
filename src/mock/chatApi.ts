import type { MockMethod } from 'vite-plugin-mock';

// Mock data for streaming responses
const mockStreamResponses = [
  `Hello! I'm an AI assistant. How can I help you today?`,
  
  `# Markdown Support Demo

Here are some examples of Markdown formatting:

## Headers

### Level 3 header

## Lists
- Item 1
- Item 2
- Item 3

## Code Blocks
\`\`\`javascript
function hello() {
  console.log('Hello, world!');
}
\`\`\`

## Tables
| Name | Age | Occupation |
|------|-----|------------|
| John | 30  | Developer  |
| Jane | 25  | Designer   |

## Math Support
The formula for the area of a circle is: $A = \pi r^2$

## Blockquotes
> This is a blockquote.
> It can span multiple lines.`,

  `Here's an explanation of the streaming SSE issues:

1. **Partial messages**: When receiving chunks from the SSE stream, a chunk might end in the middle of a message. This is why we need to accumulate the buffer and only process complete messages.

2. **Non-standard formats**: Sometimes the buffer might contain partial data prefixes like "da" when the full prefix should be "data:". By waiting for complete messages (ending with \\n\\n), we avoid processing these partial formats.

3. **Escaped newlines**: The server might escape newlines in Markdown content as "\\n". Our parser handles this by processing complete events rather than splitting on every newline character.

4. **Preserving content integrity**: By accumulating the complete content and only rendering when we have full messages, we ensure proper Markdown rendering.

\`\`\`typescript
// Example of how the SSE parser works
function processBuffer(buffer: string) {
  // Find complete events (double newlines)
  const events = buffer.split('\\n\\n');
  
  // The last part might be incomplete
  const remainingBuffer = events.pop() || '';
  
  // Process complete events
  for (const event of events) {
    // Extract data content
    const dataContent = extractData(event);
    // Add to the accumulated content
    fullContent += dataContent;
  }
  
  return { content: fullContent, remainingBuffer };
}
\`\`\``,
];

// Helper function to simulate streaming responses
function createStreamResponse(content: string) {
  return new Response(
    new ReadableStream({
      async start(controller) {
        // Break content into smaller chunks to simulate streaming
        const chunks: string[] = [];
        let currentChunk = '';
        
        // Split into words
        const words = content.split(' ');
        
        // Group words into chunks of 3-8 words
        for (const word of words) {
          currentChunk += (currentChunk ? ' ' : '') + word;
          
          // Randomly decide if we should send the current chunk
          if (currentChunk.length > 20 && Math.random() > 0.7) {
            chunks.push(currentChunk);
            currentChunk = '';
          }
        }
        
        // Add any remaining words
        if (currentChunk) {
          chunks.push(currentChunk);
        }
        
        // Send chunks with delays to simulate streaming
        for (let i = 0; i < chunks.length; i++) {
          // Add proper SSE format
          const sseData = `data: ${chunks[i]}\n\n`;
          
          // Encode as Uint8Array
          const encoded = new TextEncoder().encode(sseData);
          
          // Add delay between chunks
          await new Promise(resolve => setTimeout(resolve, 50 + Math.random() * 150));
          
          // Send chunk
          controller.enqueue(encoded);
        }
        
        controller.close();
      }
    }),
    {
      headers: {
        'Content-Type': 'text/event-stream',
        'Cache-Control': 'no-cache',
        'Connection': 'keep-alive',
      },
    }
  );
}

// Define message interface
interface ChatMessage {
  role: string;
  content: string;
}

// Define request body interface
interface ChatRequestBody {
  messages: ChatMessage[];
}

export default [
  {
    url: '/api/chat',
    method: 'post',
    response: ({ body }: { body: ChatRequestBody }) => {
      // Get the messages from the request body
      const { messages } = body;
      
      // Choose a response based on the last message
      let responseIndex = 0;
      
      if (messages && messages.length > 0) {
        const lastMessage = messages[messages.length - 1];
        
        // Simple keyword matching for demo purposes
        if (lastMessage.content.toLowerCase().includes('markdown')) {
          responseIndex = 1;
        } else if (lastMessage.content.toLowerCase().includes('stream') || 
                  lastMessage.content.toLowerCase().includes('sse') ||
                  lastMessage.content.toLowerCase().includes('issue')) {
          responseIndex = 2;
        }
      }
      
      // Return a streaming response
      return createStreamResponse(mockStreamResponses[responseIndex]);
    }
  }
] as MockMethod[]; 