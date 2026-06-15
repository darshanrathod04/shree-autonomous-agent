import apiClient from './apiClient';

export interface LessonData {
  hasActiveLesson: boolean;
  lessonName: string | null;
  activeTopic: string | null;
  chapterNumber: number;
  currentObjective: string | null;
  completedChapters: unknown[];
  pendingFollowups: unknown[];
  progressSummary: string | null;
}

export const learningApi = {
  async getLesson(): Promise<LessonData> {
    const { data } = await apiClient.get<LessonData>('/lesson');
    return data;
  },
};