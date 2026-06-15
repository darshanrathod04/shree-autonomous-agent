import apiClient from './apiClient';

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
  name: string;
  teachingStyle: string;
  preferredTone: string;
  preferences: Record<string, unknown>;
  personalityMode: string;
}

export const memoryApi = {
  async getMemory(): Promise<MemoryData> {
    const { data } = await apiClient.get<MemoryData>('/memory');
    return data;
  },

  async getProfile(): Promise<ProfileData> {
    const { data } = await apiClient.get<ProfileData>('/profile');
    return data;
  },
};