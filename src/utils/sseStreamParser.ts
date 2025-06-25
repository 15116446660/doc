/**
 * Server-Sent Events Stream Parser
 * 
 * This utility handles SSE streams while properly preserving Markdown content
 * and addressing common issues with streaming AI model responses.
 */

// Type definitions for SSE events
type SSEEventCallback = (data: string, id?: string) => void;

interface SSEParserOptions {
  onMessage?: SSEEventCallback;
  onError?: (error: Error) => void;
  onComplete?: (fullContent: string) => void;
}

/**
 * Parse a stream from a fetch response into SSE events
 */
export async function parseSSEStream(
  response: Response, 
  options: SSEParserOptions = {}
): Promise<string> {
  if (!response.ok) {
    const error = new Error(`HTTP error! Status: ${response.status}`);
    options.onError?.(error);
    throw error;
  }

  const reader = response.body?.getReader();
  if (!reader) {
    const error = new Error('Response body is null');
    options.onError?.(error);
    throw error;
  }

  const decoder = new TextDecoder();
  
  // Accumulated state
  let buffer = '';
  let fullContent = '';

  try {
    while (true) {
      const { done, value } = await reader.read();
      
      if (done) break;
      
      // Decode and add to buffer
      const chunk = decoder.decode(value, { stream: true });
      buffer += chunk;
      
      // Process any complete events in the buffer
      const result = processBuffer(buffer);
      buffer = result.remainingBuffer;
      
      // Add processed content to the full content
      if (result.content) {
        fullContent += result.content;
        options.onMessage?.(result.content);
      }
    }
    
    // Process any remaining buffer content
    if (buffer.trim().length > 0) {
      const result = processBuffer(buffer + '\n\n'); // Force processing of remaining buffer
      if (result.content) {
        fullContent += result.content;
        options.onMessage?.(result.content);
      }
    }
    
    options.onComplete?.(fullContent);
    return fullContent;
  } catch (error) {
    const typedError = error as Error;
    options.onError?.(typedError);
    throw typedError;
  }
}

/**
 * Process the buffer to extract complete SSE messages
 */
function processBuffer(buffer: string): { content: string; remainingBuffer: string } {
  let content = '';
  let currentIndex = 0;
  let eventStart = 0;
  
  // Find event boundaries (double newlines)
  while (true) {
    const eventEnd = buffer.indexOf('\n\n', currentIndex);
    if (eventEnd === -1) break;
    
    // Extract the event
    const event = buffer.substring(eventStart, eventEnd);
    
    // Process the event data
    const eventData = extractEventData(event);
    if (eventData.data) {
      content += eventData.data;
    }
    
    // Move to the next event
    currentIndex = eventEnd + 2;
    eventStart = currentIndex;
  }
  
  // Return remaining buffer for next iteration
  return {
    content,
    remainingBuffer: buffer.substring(eventStart)
  };
}

/**
 * Extract data from an SSE event
 */
function extractEventData(event: string): { data: string | null; id: string | null } {
  // Split the event into lines
  const lines = event.split('\n');
  
  // Extract all data lines
  let dataContent = '';
  let idContent: string | null = null;
  
  for (const line of lines) {
    if (line.startsWith('data:')) {
      // Extract content after "data:" prefix
      const data = line.substring(5).trim();
      dataContent += data;
    } else if (line.startsWith('id:')) {
      idContent = line.substring(3).trim();
    }
  }
  
  return { data: dataContent || null, id: idContent };
}

/**
 * Create an SSE stream parser that updates content incrementally
 */
export function createSSEParser(options: SSEParserOptions = {}) {
  let content = '';
  
  const parser = {
    parseResponse: async (response: Response): Promise<string> => {
      content = '';
      return parseSSEStream(response, {
        ...options,
        onMessage: (data) => {
          content += data;
          options.onMessage?.(data);
        },
        onComplete: (fullContent) => {
          options.onComplete?.(fullContent);
        },
      });
    },
    getContent: () => content
  };
  
  return parser;
}

/**
 * Utility function to send a request and parse the SSE stream
 */
export async function fetchSSE(
  url: string, 
  options: RequestInit = {}, 
  parserOptions: SSEParserOptions = {}
): Promise<string> {
  const response = await fetch(url, {
    ...options,
    headers: {
      ...options.headers,
      'Accept': 'text/event-stream',
    },
  });
  
  return parseSSEStream(response, parserOptions);
} 