import { Sidebar } from './components/Sidebar';
import { ChatArea } from './components/ChatArea';
import { GoalPanel } from './components/GoalPanel';
import { MemoryPanel } from './components/MemoryPanel';
import { ActivityPanel } from './components/ActivityPanel';
import { useUIStore } from './stores/uiStore';
import { useSessionStore } from './stores/sessionStore';
import { useChatStore } from './stores/chatStore';

function App() {
  const { activeTab } = useUIStore();
  const { activeSessionId, setActiveSession } = useSessionStore();
  const { clearMessages } = useChatStore();

  const handleNewChat = () => {
    setActiveSession(null);
    clearMessages();
  };

  const handleSelectSession = (id: string) => {
    setActiveSession(id);
  };

  return (
    <div className="h-full flex dark">
      {/* Sidebar */}
      <Sidebar onNewChat={handleNewChat} onSelectSession={handleSelectSession} />

      {/* Main Content */}
      <main className="flex-1 flex flex-col min-w-0">
        {activeTab === 'chat' && <ChatArea sessionId={activeSessionId} />}
        {activeTab === 'goals' && <GoalPanel />}
        {activeTab === 'memory' && <MemoryPanel />}
        {activeTab === 'activity' && <ActivityPanel />}
      </main>
    </div>
  );
}

export default App;