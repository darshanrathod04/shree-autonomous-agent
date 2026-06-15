import { create } from 'zustand';
import type { PersonalityMode, NavigationTab, AIStatus } from '@/shared/types';

interface UIState {
  sidebarOpen: boolean;
  sidebarCollapsed: boolean;
  personalityMode: PersonalityMode;
  activeGoalId: string | null;
  connectionStatus: 'connected' | 'connecting' | 'disconnected' | 'error';
  isStreaming: boolean;
  showMobileSidebar: boolean;
  activeNavigation: NavigationTab;
  aiStatus: AIStatus;

  toggleSidebar: () => void;
  setSidebarCollapsed: (collapsed: boolean) => void;
  setPersonalityMode: (mode: PersonalityMode) => void;
  setActiveGoalId: (id: string | null) => void;
  setConnectionStatus: (status: UIState['connectionStatus']) => void;
  setIsStreaming: (streaming: boolean) => void;
  setShowMobileSidebar: (show: boolean) => void;
  setActiveNavigation: (tab: NavigationTab) => void;
  setAIStatus: (status: Partial<AIStatus>) => void;
}

export const useUIStore = create<UIState>((set) => ({
  sidebarOpen: true,
  sidebarCollapsed: false,
  personalityMode: 'assistant',
  activeGoalId: 'goal-1',
  connectionStatus: 'connected',
  isStreaming: false,
  showMobileSidebar: false,
  activeNavigation: 'chat',
  aiStatus: {
    memoryActive: true,
    goalActive: true,
    learningMode: true,
    autonomous: true,
  },

  toggleSidebar: () =>
    set((state) => ({ sidebarOpen: !state.sidebarOpen })),

  setSidebarCollapsed: (collapsed) =>
    set({ sidebarCollapsed: collapsed }),

  setPersonalityMode: (mode) =>
    set({ personalityMode: mode }),

  setActiveGoalId: (id) =>
    set({ activeGoalId: id }),

  setConnectionStatus: (status) =>
    set({ connectionStatus: status }),

  setIsStreaming: (streaming) =>
    set({ isStreaming: streaming }),

  setShowMobileSidebar: (show) =>
    set({ showMobileSidebar: show }),

  setActiveNavigation: (tab) =>
    set({ activeNavigation: tab }),

  setAIStatus: (status) =>
    set((state) => ({ aiStatus: { ...state.aiStatus, ...status } })),
}));