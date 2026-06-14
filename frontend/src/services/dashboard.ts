import api from './api';
import type { Session } from '@/stores/sessionStore';

export interface GoalData {
  hasGoal: boolean;
  description?: string;
  completed?: boolean;
  totalSubGoals?: number;
  completedSubGoals?: number;
  progressPercent?: number;
  subGoals?: { description: string; completed: boolean }[];
  createdAt?: string;
}

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

export interface ProfileData {
  name: string | null;
  teachingStyle: string;
  preferredTone: string;
  preferences: Record<string, unknown>;
  personalityMode: string;
}

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

export interface ChatResponse {
  suggestion: string;
  approvalRequired: boolean;
  sessionId: string;
}

// Session APIs
export const fetchSessions = () =>
  api.get<Session[]>('/sessions').then((r) => r.data);

export const fetchSessionMessages = (sessionId: string) =>
  api.get(`/sessions/${sessionId}/messages`).then((r) => r.data);

// Chat API
export const sendMessage = (message: string, sessionId?: string | null) =>
  api
    .post<ChatResponse>('/chat', { message, sessionId: sessionId || null })
    .then((r) => r.data);

// Dashboard APIs
export const fetchGoalData = () =>
  api.get<GoalData>('/dashboard/goals').then((r) => r.data);

export const fetchMemoryData = () =>
  api.get<MemoryData>('/dashboard/memory').then((r) => r.data);

export const fetchProfileData = () =>
  api.get<ProfileData>('/dashboard/profile').then((r) => r.data);

export const fetchLessonData = () =>
  api.get<LessonData>('/dashboard/lesson').then((r) => r.data);