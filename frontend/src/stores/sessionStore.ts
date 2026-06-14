import { create } from 'zustand';

export interface Session {
  sessionId: string;
  userId?: string;
  createdAt: string;
  lastAccessedAt: string;
  messageCount: number;
  firstMessage: string;
}

interface SessionState {
  sessions: Session[];
  activeSessionId: string | null;
  isLoading: boolean;
  setSessions: (sessions: Session[]) => void;
  setActiveSession: (id: string | null) => void;
  setLoading: (loading: boolean) => void;
}

export const useSessionStore = create<SessionState>((set) => ({
  sessions: [],
  activeSessionId: null,
  isLoading: false,
  setSessions: (sessions) => set({ sessions }),
  setActiveSession: (id) => set({ activeSessionId: id }),
  setLoading: (loading) => set({ isLoading: loading }),
}));