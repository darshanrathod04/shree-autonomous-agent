import { create } from 'zustand';
import type { Message } from '@/shared/types';

interface SessionChatState {
  messages: Message[];
  isStreaming: boolean;
  streamingContent: string;
}

interface ChatState {
  // Per-session chat state: sessionId -> { messages, isStreaming, streamingContent }
  sessions: Record<string, SessionChatState>;
  // Active session ID (kept in sync with sessionStore)
  activeSessionId: string | null;

  // Actions
  setActiveSession: (id: string | null) => void;
  addMessage: (message: Message) => void;
  setMessages: (messages: Message[]) => void;
  clearMessages: () => void;
  startStreaming: () => void;
  updateStreaming: (content: string) => void;
  finishStreaming: () => void;
  removeMessage: (id: string) => void;
  updateMessage: (id: string, updates: Partial<Message>) => void;
  regenerateLastMessage: () => string | null;

  // Getters for active session
  getMessages: () => Message[];
  getIsStreaming: () => boolean;
  getStreamingContent: () => string;
}

function getSessionState(sessions: Record<string, SessionChatState>, sessionId: string | null): SessionChatState {
  if (!sessionId) {
    // Fallback for no-session state
    const key = '__no_session__';
    if (!sessions[key]) {
      sessions[key] = { messages: [], isStreaming: false, streamingContent: '' };
    }
    return sessions[key];
  }
  if (!sessions[sessionId]) {
    sessions[sessionId] = { messages: [], isStreaming: false, streamingContent: '' };
  }
  return sessions[sessionId];
}

export const useChatStore = create<ChatState>((set, get) => ({
  sessions: {},
  activeSessionId: null,

  setActiveSession: (id) => set({ activeSessionId: id }),

  addMessage: (message) =>
    set((state) => {
      const key = state.activeSessionId || '__no_session__';
      const current = getSessionState(state.sessions, key);
      return {
        sessions: {
          ...state.sessions,
          [key]: {
            ...current,
            messages: [...current.messages, message],
          },
        },
      };
    }),

  setMessages: (messages) =>
    set((state) => {
      const key = state.activeSessionId || '__no_session__';
      const current = getSessionState(state.sessions, key);
      return {
        sessions: {
          ...state.sessions,
          [key]: {
            ...current,
            messages,
          },
        },
      };
    }),

  clearMessages: () =>
    set((state) => {
      const key = state.activeSessionId || '__no_session__';
      const current = getSessionState(state.sessions, key);
      return {
        sessions: {
          ...state.sessions,
          [key]: {
            ...current,
            messages: [],
            streamingContent: '',
          },
        },
      };
    }),

  startStreaming: () =>
    set((state) => {
      const key = state.activeSessionId || '__no_session__';
      const current = getSessionState(state.sessions, key);
      return {
        sessions: {
          ...state.sessions,
          [key]: {
            ...current,
            isStreaming: true,
            streamingContent: '',
          },
        },
      };
    }),

  updateStreaming: (content) =>
    set((state) => {
      const key = state.activeSessionId || '__no_session__';
      const current = getSessionState(state.sessions, key);
      return {
        sessions: {
          ...state.sessions,
          [key]: {
            ...current,
            streamingContent: content,
          },
        },
      };
    }),

  finishStreaming: () => {
    const state = get();
    const key = state.activeSessionId || '__no_session__';
    const current = getSessionState(state.sessions, key);
    if (current.streamingContent) {
      // Check if the last message is already this assistant response (prevent duplicates)
      const messages = current.messages;
      const isDuplicate = messages.length > 0
        && messages[messages.length - 1].role === 'assistant'
        && messages[messages.length - 1].content === current.streamingContent;

      if (!isDuplicate) {
        const message: Message = {
          id: `msg-${Date.now()}`,
          role: 'assistant',
          content: current.streamingContent,
          timestamp: Date.now(),
        };
        set({
          sessions: {
            ...state.sessions,
            [key]: {
              messages: [...current.messages, message],
              isStreaming: false,
              streamingContent: '',
            },
          },
        });
      } else {
        // Just clear streaming state, message already exists
        set({
          sessions: {
            ...state.sessions,
            [key]: {
              ...current,
              isStreaming: false,
              streamingContent: '',
            },
          },
        });
      }
    } else {
      set({
        sessions: {
          ...state.sessions,
          [key]: {
            ...current,
            isStreaming: false,
          },
        },
      });
    }
  },

  removeMessage: (id) =>
    set((state) => {
      const key = state.activeSessionId || '__no_session__';
      const current = getSessionState(state.sessions, key);
      return {
        sessions: {
          ...state.sessions,
          [key]: {
            ...current,
            messages: current.messages.filter((m) => m.id !== id),
          },
        },
      };
    }),

  updateMessage: (id, updates) =>
    set((state) => {
      const key = state.activeSessionId || '__no_session__';
      const current = getSessionState(state.sessions, key);
      return {
        sessions: {
          ...state.sessions,
          [key]: {
            ...current,
            messages: current.messages.map((m) =>
              m.id === id ? { ...m, ...updates } : m
            ),
          },
        },
      };
    }),

  regenerateLastMessage: () => {
    const state = get();
    const key = state.activeSessionId || '__no_session__';
    const current = getSessionState(state.sessions, key);
    if (current.messages.length >= 2) {
      const lastAssistant = current.messages
        .slice()
        .reverse()
        .find((m) => m.role === 'assistant');
      if (lastAssistant) {
        set({
          sessions: {
            ...state.sessions,
            [key]: {
              ...current,
              messages: current.messages.filter((m) => m.id !== lastAssistant.id),
            },
          },
        });
        const userMsg = current.messages
          .slice()
          .reverse()
          .find((m) => m.role === 'user');
        return userMsg?.content || null;
      }
    }
    return null;
  },

  getMessages: () => {
    const state = get();
    const key = state.activeSessionId || '__no_session__';
    return getSessionState(state.sessions, key).messages;
  },

  getIsStreaming: () => {
    const state = get();
    const key = state.activeSessionId || '__no_session__';
    return getSessionState(state.sessions, key).isStreaming;
  },

  getStreamingContent: () => {
    const state = get();
    const key = state.activeSessionId || '__no_session__';
    return getSessionState(state.sessions, key).streamingContent;
  },
}));