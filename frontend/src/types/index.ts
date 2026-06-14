// Chat Types
export interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
}

export interface ChatState {
  messages: ChatMessage[];
  isLoading: boolean;
  error: string | null;
  addMessage: (msg: ChatMessage) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  clearMessages: () => void;
}

// Session Types
export interface Session {
  sessionId: string;
  userId?: string;
  createdAt: string;
  lastAccessedAt: string;
  messageCount: number;
  firstMessage: string;
}

export interface SessionState {
  sessions: Session[];
  activeSessionId: string | null;
  isLoading: boolean;
  setSessions: (sessions: Session[]) => void;
  setActiveSession: (id: string | null) => void;
  setLoading: (loading: boolean) => void;
}

// API Response Types
export interface AgentRequest {
  message: string;
  sessionId?: string | null;
}

export interface AgentResponse {
  suggestion: string;
  approvalRequired: boolean;
  sessionId: string;
}

// Goal Types
export interface SubGoal {
  description: string;
  completed: boolean;
}

export interface GoalData {
  hasGoal: boolean;
  description?: string;
  completed?: boolean;
  totalSubGoals?: number;
  completedSubGoals?: number;
  progressPercent?: number;
  subGoals?: SubGoal[];
  createdAt?: string;
}

// Memory Types
export interface MemoryData {
  semanticCount: number;
  episodicCount: number;
  activeTopic: string | null;
  lessonName: string | null;
  chapterNumber: number;
  currentObjective: string | null;
  completedChapters: number;
  hasActiveLesson: boolean;
}

// Profile Types
export interface ProfileData {
  name: string | null;
  teachingStyle: string;
  preferredTone: string;
  preferences: Record<string, unknown>;
  personalityMode: string;
}

// Lesson Types
export interface LessonData {
  hasActiveLesson: boolean;
  lessonName: string | null;
  activeTopic: string | null;
  chapterNumber: number;
  currentObjective: string | null;
  completedChapters: string[];
  pendingFollowups: string[];
  progressSummary: string;
}

// UI Types
export interface UIState {
  sidebarOpen: boolean;
  theme: 'light' | 'dark' | 'system';
  toggleSidebar: () => void;
  setTheme: (theme: 'light' | 'dark' | 'system') => void;
}