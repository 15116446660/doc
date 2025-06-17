import { get } from './request';
import type { KnowledgeBase } from '@/types/chat';

export function getKnowledgeBases() {
  return get<KnowledgeBase[]>('/api/knowledge-bases');
}

