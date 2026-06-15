import { create } from 'zustand';
import type { Session, SuggestedPrompt } from '@/shared/types';

interface SessionState {
  sessions: Session[];
  activeSessionId: string | null;
  setActiveSession: (id: string | null) => void;
  setSessions: (sessions: Session[]) => void;
  addSession: (session: Session) => void;
  removeSession: (id: string) => void;
  updateSession: (id: string, updates: Partial<Session>) => void;
  searchQuery: string;
  setSearchQuery: (query: string) => void;
  filteredSessions: () => Session[];
  suggestedPrompts: SuggestedPrompt[];
}

const DEFAULT_PROMPTS: SuggestedPrompt[] = [
  {
    id: 'prompt-1',
    text: 'What can Shree AI do for me?',
    icon: '✨',
    category: 'general',
  },
  {
    id: 'prompt-2',
    text: 'Help me plan a software architecture',
    icon: '🏗️',
    category: 'coding',
  },
  {
    id: 'prompt-3',
    text: 'Explain quantum computing simply',
    icon: '🔬',
    category: 'learning',
  },
  {
    id: 'prompt-4',
    text: 'Write a creative story about AI',
    icon: '📝',
    category: 'creative',
  },
  {
    id: 'prompt-5',
    text: 'Analyze this data and give insights',
    icon: '📊',
    category: 'analysis',
  },
  {
    id: 'prompt-6',
    text: 'Debug my code and fix issues',
    icon: '🔧',
    category: 'coding',
  },
  {
    id: 'prompt-7',
    text: 'Generate a business strategy',
    icon: '💼',
    category: 'business',
  },
  {
    id: 'prompt-8',
    text: 'Teach me a new skill step by step',
    icon: '🎓',
    category: 'learning',
  },
];

export const useSessionStore = create<SessionState>((set, get) => ({
  sessions: [],
  activeSessionId: null,
  searchQuery: '',
  suggestedPrompts: DEFAULT_PROMPTS,

  setActiveSession: (id) => set({ activeSessionId: id }),

  setSessions: (sessions) => set({ sessions }),

  addSession: (session) =>
    set((state) => ({
      sessions: [session, ...state.sessions],
    })),

  removeSession: (id) =>
    set((state) => ({
      sessions: state.sessions.filter((s) => s.id !== id),
      activeSessionId:
        state.activeSessionId === id ? null : state.activeSessionId,
    })),

  updateSession: (id, updates) =>
    set((state) => ({
      sessions: state.sessions.map((s) =>
        s.id === id ? { ...s, ...updates } : s
      ),
    })),

  setSearchQuery: (query) => set({ searchQuery: query }),

  filteredSessions: () => {
    const { sessions, searchQuery } = get();
    if (!searchQuery.trim()) return sessions;
    const q = searchQuery.toLowerCase();
    return sessions.filter(
      (s) =>
        s.title.toLowerCase().includes(q) ||
        s.messages.some((m) => m.content.toLowerCase().includes(q))
    );
  },
}));