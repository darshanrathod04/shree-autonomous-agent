import { create } from 'zustand';
import type { Message } from '@/shared/types';

interface ChatState {
  messages: Message[];
  isStreaming: boolean;
  streamingContent: string;
  addMessage: (message: Message) => void;
  setMessages: (messages: Message[]) => void;
  clearMessages: () => void;
  startStreaming: () => void;
  updateStreaming: (content: string) => void;
  finishStreaming: () => void;
  removeMessage: (id: string) => void;
  updateMessage: (id: string, updates: Partial<Message>) => void;
  regenerateLastMessage: () => string | null;
}

export const useChatStore = create<ChatState>((set, get) => ({
  messages: [],
  isStreaming: false,
  streamingContent: '',

  addMessage: (message) =>
    set((state) => ({
      messages: [...state.messages, message],
    })),

  setMessages: (messages) => set({ messages }),

  clearMessages: () => set({ messages: [], streamingContent: '' }),

  startStreaming: () => set({ isStreaming: true, streamingContent: '' }),

  updateStreaming: (content) => set({ streamingContent: content }),

  finishStreaming: () => {
    const { streamingContent } = get();
    if (streamingContent) {
      const message: Message = {
        id: `msg-${Date.now()}`,
        role: 'assistant',
        content: streamingContent,
        timestamp: Date.now(),
      };
      set((state) => ({
        messages: [...state.messages, message],
        isStreaming: false,
        streamingContent: '',
      }));
    } else {
      set({ isStreaming: false });
    }
  },

  removeMessage: (id) =>
    set((state) => ({
      messages: state.messages.filter((m) => m.id !== id),
    })),

  updateMessage: (id, updates) =>
    set((state) => ({
      messages: state.messages.map((m) =>
        m.id === id ? { ...m, ...updates } : m
      ),
    })),

  regenerateLastMessage: () => {
    const { messages } = get();
    if (messages.length >= 2) {
      const lastAssistant = messages
        .slice()
        .reverse()
        .find((m) => m.role === 'assistant');
      if (lastAssistant) {
        set((state) => ({
          messages: state.messages.filter((m) => m.id !== lastAssistant.id),
        }));
        const userMsg = messages
          .slice()
          .reverse()
          .find((m) => m.role === 'user');
        return userMsg?.content || null;
      }
    }
    return null;
  },
}));