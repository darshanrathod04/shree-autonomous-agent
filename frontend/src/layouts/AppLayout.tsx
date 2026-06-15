import { useEffect } from 'react';
import { useUIStore } from '@/shared/stores/uiStore';
import { useSessionStore } from '@/shared/stores/sessionStore';
import { useChatStore } from '@/shared/stores/chatStore';
import { chatApi } from '@/shared/services/chatApi';
import { Sidebar } from './Sidebar';
import { Header } from './Header';
import { ChatArea } from '@/features/chat/ChatArea';
import { MemoryPage } from '@/features/memory/MemoryPage';
import { GoalsPage } from '@/features/goals/GoalsPage';
import { LearningPage } from '@/features/learning/LearningPage';
import { ActivityPage } from '@/features/activity/ActivityPage';
import type { Session } from '@/shared/types';

export function AppLayout() {
  const { activeNavigation } = useUIStore();
  const { activeSessionId, setActiveSession, setSessions } = useSessionStore();
  const { clearMessages } = useChatStore();

  // Load sessions from backend on mount
  useEffect(() => {
    const loadSessions = async () => {
      try {
        const backendSessions = await chatApi.listSessions();
        const sessions: Session[] = backendSessions.map((s) => ({
          id: s.sessionId,
          title: s.firstMessage || 'New conversation',
          messages: [],
          createdAt: new Date(s.createdAt).getTime(),
          updatedAt: new Date(s.lastAccessedAt).getTime(),
          metadata: {
            messageCount: s.messageCount,
            tokensUsed: 0,
            duration: 0,
          },
        }));
        setSessions(sessions);
      } catch (err) {
        console.error('[AppLayout] Failed to load sessions:', err);
      }
    };
    loadSessions();
  }, [setSessions]);

  const handleNewChat = () => {
    setActiveSession(null);
    clearMessages();
  };

  const handleSelectSession = (id: string) => {
    setActiveSession(id);
  };

  const renderContent = () => {
    switch (activeNavigation) {
      case 'memory':
        return <MemoryPage />;
      case 'goals':
        return <GoalsPage />;
      case 'learning':
        return <LearningPage />;
      case 'chat':
      default:
        return <ChatArea sessionId={activeSessionId} />;
    }
  };

  return (
    <div className="h-full flex bg-[#0a0a0f] text-white overflow-hidden relative">
      {/* Animated Mesh Gradient Background */}
      <div className="mesh-bg">
        <div className="mesh-blob mesh-blob-1" />
        <div className="mesh-blob mesh-blob-2" />
        <div className="mesh-blob mesh-blob-3" />
        <div className="mesh-blob mesh-blob-4" />
      </div>

      {/* Sidebar */}
      <Sidebar onNewChat={handleNewChat} onSelectSession={handleSelectSession} />

      {/* Main Content */}
      <div className="flex-1 flex flex-col min-w-0 relative z-[1]">
        <Header />
        {renderContent()}
      </div>
    </div>
  );
}