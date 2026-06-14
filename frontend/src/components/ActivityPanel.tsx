import { useEffect, useState } from 'react';
import { fetchSessions } from '@/services/dashboard';
import type { Session } from '@/stores/sessionStore';
import { Activity, MessageSquare, Clock, Calendar, TrendingUp } from 'lucide-react';

export function ActivityPanel() {
  const [sessions, setSessions] = useState<Session[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadSessions();
  }, []);

  const loadSessions = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchSessions();
      const arr = Array.isArray(data) ? data : [];
      arr.sort((a, b) => new Date(b.lastAccessedAt || b.createdAt).getTime() - new Date(a.lastAccessedAt || a.createdAt).getTime());
      setSessions(arr);
    } catch (err: any) {
      setError(err.response?.data?.error || 'Failed to load activity');
    } finally {
      setLoading(false);
    }
  };

  const totalMessages = sessions.reduce((sum, s) => sum + (s.messageCount || 0), 0);
  const today = new Date().toDateString();
  const todaySessions = sessions.filter(
    (s) => new Date(s.lastAccessedAt || s.createdAt).toDateString() === today
  );
  const activeDays = new Set(
    sessions.map((s) => new Date(s.lastAccessedAt || s.createdAt).toDateString())
  ).size;

  if (loading) {
    return (
      <div className="flex-1 flex items-center justify-center">
        <div className="w-6 h-6 border-2 border-[var(--accent)] border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex-1 flex items-center justify-center">
        <div className="text-center">
          <p className="text-[var(--error)] text-sm mb-2">{error}</p>
          <button onClick={loadSessions} className="text-xs text-[var(--accent)] hover:underline">
            Try again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="flex-1 overflow-y-auto p-6">
      <div className="max-w-2xl mx-auto space-y-6">
        {/* Header */}
        <div>
          <h2 className="text-xl font-bold mb-1">Activity</h2>
          <p className="text-sm text-[var(--text-secondary)]">
            Your interaction history with the agent
          </p>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-3 gap-3">
          <div className="p-4 rounded-xl bg-[var(--bg-secondary)] border border-[var(--border-color)]">
            <div className="flex items-center gap-2 mb-2">
              <MessageSquare size={16} className="text-[var(--accent)]" />
              <span className="text-xs text-[var(--text-secondary)]">Total Msgs</span>
            </div>
            <p className="text-xl font-bold">{totalMessages}</p>
          </div>
          <div className="p-4 rounded-xl bg-[var(--bg-secondary)] border border-[var(--border-color)]">
            <div className="flex items-center gap-2 mb-2">
              <Calendar size={16} className="text-[var(--warning)]" />
              <span className="text-xs text-[var(--text-secondary)]">Today</span>
            </div>
            <p className="text-xl font-bold">{todaySessions.length}</p>
          </div>
          <div className="p-4 rounded-xl bg-[var(--bg-secondary)] border border-[var(--border-color)]">
            <div className="flex items-center gap-2 mb-2">
              <TrendingUp size={16} className="text-[var(--success)]" />
              <span className="text-xs text-[var(--text-secondary)]">Active Days</span>
            </div>
            <p className="text-xl font-bold">{activeDays}</p>
          </div>
        </div>

        {/* Recent Sessions */}
        <div>
          <h3 className="text-sm font-semibold mb-3">Session History</h3>
          {sessions.length === 0 ? (
            <p className="text-sm text-[var(--text-secondary)] text-center py-8">
              No sessions yet
            </p>
          ) : (
            <div className="space-y-2">
              {sessions.map((session) => (
                <div
                  key={session.sessionId}
                  className="p-4 rounded-xl bg-[var(--bg-secondary)] border border-[var(--border-color)]"
                >
                  <div className="flex items-start gap-3">
                    <div className="p-2 rounded-lg bg-[var(--accent)]/10">
                      <MessageSquare size={16} className="text-[var(--accent)]" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium truncate">
                        {session.firstMessage || 'New conversation'}
                      </p>
                      <div className="flex items-center gap-3 mt-1.5 text-xs text-[var(--text-secondary)]">
                        <span className="flex items-center gap-1">
                          <MessageSquare size={12} />
                          {session.messageCount || 0} messages
                        </span>
                        <span className="flex items-center gap-1">
                          <Clock size={12} />
                          {new Date(session.lastAccessedAt || session.createdAt).toLocaleDateString()}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Weekly Activity Chart */}
        <div className="p-5 rounded-xl bg-[var(--bg-secondary)] border border-[var(--border-color)]">
          <h3 className="text-sm font-semibold mb-4">Weekly Activity</h3>
          <div className="flex items-end justify-between h-24 gap-2">
            {Array.from({ length: 7 }, (_, i) => {
              const date = new Date();
              date.setDate(date.getDate() - (6 - i));
              const dateStr = date.toDateString();
              const daySessions = sessions.filter(
                (s) => new Date(s.lastAccessedAt || s.createdAt).toDateString() === dateStr
              );
              const count = daySessions.reduce((sum, s) => sum + (s.messageCount || 0), 0);
              const max = Math.max(
                ...Array.from({ length: 7 }, (_, j) => {
                  const d = new Date();
                  d.setDate(d.getDate() - (6 - j));
                  return sessions
                    .filter((s) => new Date(s.lastAccessedAt || s.createdAt).toDateString() === d.toDateString())
                    .reduce((sum, s) => sum + (s.messageCount || 0), 0);
                }),
                1
              );
              const height = Math.max((count / max) * 100, 4);
              const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
              return (
                <div key={i} className="flex-1 flex flex-col items-center gap-1">
                  <span className="text-xs text-[var(--text-secondary)]">{count}</span>
                  <div className="w-full flex-1 flex items-end">
                    <div
                      className="w-full rounded-t-md bg-[var(--accent)] transition-all duration-500"
                      style={{ height: `${height}%`, opacity: 0.3 + (count / max) * 0.7 }}
                    />
                  </div>
                  <span className="text-xs text-[var(--text-secondary)]">{days[date.getDay()]}</span>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
}