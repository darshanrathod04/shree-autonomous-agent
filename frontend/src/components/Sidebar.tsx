import { useEffect } from 'react';
import { useSessionStore, type Session } from '@/stores/sessionStore';
import { useUIStore } from '@/stores/uiStore';
import { fetchSessions } from '@/services/dashboard';
import { MessageSquare, Plus, Trash2, Menu, Brain, Target, Activity } from 'lucide-react';

interface SidebarProps {
  onNewChat: () => void;
  onSelectSession: (id: string) => void;
}

export function Sidebar({ onNewChat, onSelectSession }: SidebarProps) {
  const { sessions, activeSessionId, setSessions, setActiveSession, isLoading, setLoading } = useSessionStore();
  const { sidebarOpen, toggleSidebar, activeTab, setActiveTab } = useUIStore();

  useEffect(() => {
    loadSessions();
  }, []);

  const loadSessions = async () => {
    setLoading(true);
    try {
      const data = await fetchSessions();
      setSessions(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Failed to load sessions:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleNewChat = () => {
    setActiveSession(null);
    onNewChat();
  };

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const hours = Math.floor(diff / (1000 * 60 * 60));
    if (hours < 24) return `${hours}h ago`;
    const days = Math.floor(hours / 24);
    if (days < 7) return `${days}d ago`;
    return date.toLocaleDateString();
  };

  const truncate = (str: string, n: number) =>
    str.length > n ? str.slice(0, n) + '...' : str;

  const navItems = [
    { id: 'chat' as const, label: 'Chat', icon: MessageSquare },
    { id: 'goals' as const, label: 'Goals', icon: Target },
    { id: 'memory' as const, label: 'Memory', icon: Brain },
    { id: 'activity' as const, label: 'Activity', icon: Activity },
  ];

  return (
    <>
      {/* Mobile overlay */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-black/50 z-20 lg:hidden"
          onClick={toggleSidebar}
        />
      )}

      <aside
        className={`fixed lg:static inset-y-0 left-0 z-30 w-72 bg-[var(--bg-secondary)] border-r border-[var(--border-color)] flex flex-col transition-transform duration-300 ${
          sidebarOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0 lg:w-0 lg:overflow-hidden'
        }`}
      >
        {/* Header */}
        <div className="p-4 border-b border-[var(--border-color)]">
          <div className="flex items-center justify-between mb-4">
            <h1 className="text-lg font-bold text-[var(--accent)]">AI Agent</h1>
            <button
              onClick={toggleSidebar}
              className="p-1.5 rounded-lg hover:bg-[var(--bg-tertiary)] transition-colors"
            >
              <Menu size={20} />
            </button>
          </div>
          <button
            onClick={handleNewChat}
            className="w-full flex items-center gap-2 px-4 py-2.5 rounded-lg bg-[var(--accent)] text-white hover:bg-[var(--accent-hover)] transition-colors font-medium"
          >
            <Plus size={18} />
            New Chat
          </button>
        </div>

        {/* Navigation Tabs */}
        <div className="px-3 py-2 border-b border-[var(--border-color)]">
          <nav className="flex gap-1">
            {navItems.map((item) => (
              <button
                key={item.id}
                onClick={() => setActiveTab(item.id)}
                className={`flex items-center gap-1.5 px-3 py-2 rounded-lg text-xs font-medium transition-colors ${
                  activeTab === item.id
                    ? 'bg-[var(--accent)] text-white'
                    : 'text-[var(--text-secondary)] hover:bg-[var(--bg-tertiary)]'
                }`}
              >
                <item.icon size={14} />
                {item.label}
              </button>
            ))}
          </nav>
        </div>

        {/* Sessions List */}
        <div className="flex-1 overflow-y-auto p-2">
          {isLoading ? (
            <div className="flex items-center justify-center py-8">
              <div className="w-5 h-5 border-2 border-[var(--accent)] border-t-transparent rounded-full animate-spin" />
            </div>
          ) : sessions.length === 0 ? (
            <p className="text-center text-sm text-[var(--text-secondary)] py-8">
              No conversations yet
            </p>
          ) : (
            <div className="space-y-1">
              {sessions.map((session: Session) => (
                <button
                  key={session.sessionId}
                  onClick={() => {
                    setActiveSession(session.sessionId);
                    onSelectSession(session.sessionId);
                    setActiveTab('chat');
                  }}
                  className={`w-full flex items-start gap-3 px-3 py-2.5 rounded-lg text-left transition-colors group ${
                    activeSessionId === session.sessionId
                      ? 'bg-[var(--accent)]/10 text-[var(--accent)]'
                      : 'hover:bg-[var(--bg-tertiary)]'
                  }`}
                >
                  <MessageSquare size={16} className="mt-0.5 shrink-0" />
                  <div className="flex-1 min-w-0">
                    <p className="text-sm truncate">
                      {session.firstMessage
                        ? truncate(session.firstMessage, 35)
                        : 'New conversation'}
                    </p>
                    <p className="text-xs text-[var(--text-secondary)] mt-0.5">
                      {session.messageCount} msgs · {formatDate(session.lastAccessedAt || session.createdAt)}
                    </p>
                  </div>
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="p-3 border-t border-[var(--border-color)]">
          <button
            onClick={loadSessions}
            className="w-full text-xs text-[var(--text-secondary)] hover:text-[var(--text-primary)] transition-colors"
          >
            Refresh sessions
          </button>
        </div>
      </aside>
    </>
  );
}