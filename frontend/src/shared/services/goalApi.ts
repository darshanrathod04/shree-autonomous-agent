import apiClient from './apiClient';

export interface SubGoalData {
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
  createdAt?: string;
  subGoals?: SubGoalData[];
}

export const goalApi = {
  async getGoals(): Promise<GoalData> {
    const { data } = await apiClient.get<GoalData>('/goals');
    return data;
  },
};