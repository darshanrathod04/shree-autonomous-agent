// ===== SHREE AI - Core Types =====

export type PersonalityMode = 'teacher' | 'coach' | 'assistant' | 'friend';
export type NavigationTab = 'chat' | 'memory' | 'goals' | 'learning';
export type ConnectionStatusType = 'connected' | 'connecting' | 'disconnected' | 'error';

export interface Message {
  id: string;
  role: 'user' | 'assistant' | 'system';
  content: string;
  timestamp: number;
  isStreaming?: boolean;
  isThinking?: boolean;
  metadata?: MessageMetadata;
}

export interface MessageMetadata {
  model?: string;
  tokens?: number;
  latency?: number;
  goals?: string[];
  memories?: string[];
  mode?: PersonalityMode;
}

export interface Session {
  id: string;
  title: string;
  messages: Message[];
  createdAt: number;
  updatedAt: number;
  personalityMode?: PersonalityMode;
  goalId?: string;
  metadata?: SessionMetadata;
}

export interface SessionMetadata {
  messageCount: number;
  tokensUsed: number;
  duration: number;
}

export interface SuggestedPrompt {
  id: string;
  text: string;
  icon: string;
  category: string;
}

export interface PersonalityModeConfig {
  id: PersonalityMode;
  name: string;
  description: string;
  icon: string;
  color: string;
  gradient: string;
}

export interface Goal {
  id: string;
  title: string;
  description: string;
  status: 'active' | 'completed' | 'paused' | 'failed';
  progress: number;
  createdAt: number;
}

export interface Memory {
  id: string;
  content: string;
  type: 'episodic' | 'semantic' | 'procedural';
  timestamp: number;
  relevance: number;
}

export interface AIStatus {
  memoryActive: boolean;
  goalActive: boolean;
  learningMode: boolean;
  autonomous: boolean;
}

export interface UIState {
  sidebarOpen: boolean;
  sidebarCollapsed: boolean;
  personalityMode: PersonalityMode;
  activeGoalId: string | null;
  connectionStatus: ConnectionStatusType;
  isStreaming: boolean;
  showMobileSidebar: boolean;
  activeNavigation: NavigationTab;
  aiStatus: AIStatus;
}

export interface ConnectionStatus {
  status: ConnectionStatusType;
  message?: string;
  latency?: number;
}

export interface AppSettings {
  personalityMode: PersonalityMode;
  theme: 'dark';
  fontSize: 'sm' | 'base' | 'lg';
  enterToSend: boolean;
  showTimestamps: boolean;
  codeSyntaxHighlight: boolean;
}