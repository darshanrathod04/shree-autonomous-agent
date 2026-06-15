import apiClient from './apiClient';

export interface ChatRequest {
  message: string;
  sessionId?: string | null;
}

export interface ChatResponse {
  suggestion: string;
  approvalRequired: boolean;
  sessionId: string | null;
}

export interface SessionInfo {
  sessionId: string;
  userId?: string | null;
  createdAt: string;
  lastAccessedAt: string;
  messageCount: number;
  firstMessage?: string | null;
  summary?: string | null;
}

export interface SessionMessage {
  role: string;
  content: string;
  timestamp: string;
}

export const chatApi = {
  async send(request: ChatRequest): Promise<ChatResponse> {
    const { data } = await apiClient.post<ChatResponse>('/ask', {
      message: request.message,
      sessionId: request.sessionId || null,
    });
    return data;
  },

  async listSessions(): Promise<SessionInfo[]> {
    const { data } = await apiClient.get<SessionInfo[]>('/sessions');
    return data;
  },

  async getSession(sessionId: string): Promise<SessionInfo> {
    const { data } = await apiClient.get<SessionInfo>(`/session/${sessionId}`);
    return data;
  },

  async getSessionMessages(sessionId: string): Promise<SessionMessage[]> {
    const { data } = await apiClient.get<SessionMessage[]>(`/session/${sessionId}/messages`);
    return data;
  },

  async createSession(userId?: string): Promise<{ sessionId: string; createdAt: string; message: string }> {
    const { data } = await apiClient.post('/session', userId ? { userId } : {});
    return data;
  },

  async deleteSession(sessionId: string): Promise<void> {
    await apiClient.delete(`/session/${sessionId}`);
  },

  async getSessionCount(): Promise<{ activeSessions: number }> {
    const { data } = await apiClient.get('/sessions/count');
    return data;
  },

  async getActivity(): Promise<string[]> {
    const { data } = await apiClient.get<string[]>('/activity');
    return data;
  },
};