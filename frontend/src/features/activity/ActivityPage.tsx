import { useState, useEffect } from 'react';
import { chatApi } from '@/shared/services/chatApi';
import { Activity, RefreshCw, AlertCircle, Zap, BookOpen, Target, Cpu } from 'lucide-react';

type ActivityItem = string;

function getActivityIcon(text: string) {
  const lower = text.toLowerCase();
  if (lower.includes('memory') || lower.includes('remember') || lower.includes('recall')) return Zap;
  if (lower.includes('learn') || lower.includes('lesson') || lower.includes('chapter')) return BookOpen;
  if (lower.includes('goal') || lower.includes('subgoal') || lower.includes('objective')) return Target;
  return Cpu;
}

function getActivityColor(text: string) {
  const lower = text.toLowerCase();
  if (lower.includes('memory') || lower.includes('remember') || lower.includes('recall')) return 'text-indigo-400 bg-indigo-500/15';
  if (lower.includes('learn') || lower.includes('lesson') || lower.includes('chapter')) return 'text-blue-400 bg-blue-500/15';
  if (lower.includes('goal') || lower.includes('subgoal') || lower.includes('objective')) return 'text-emerald-400 bg-emerald-500/15';
  return 'text-cyan-400 bg-cyan-500/15';
}

export function ActivityPage() {
  const [activities, setActivities] = useState<ActivityItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchActivity = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await chatApi.getActivity();
      setActivities(data);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to load activity');
      if (err && typeof err === 'object' && 'code' in err && (err as { code?: string }).code === 'ERR_NETWORK') {
        setError('Backend offline. Start the Shree AI backend server.');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchActivity();
    const interval = setInterval(fetchActivity, 10000);
    return () => clearInterval(interval);
  }, []);

  if (loading && activities.length === 0) {
    return (
      <div className="flex-1 flex items-center justify-center">
        <div className="flex items-center gap-3">
          <RefreshCw size={18} className="text-[#a78bfa] animate-spin" />
          <span className="text-sm text-white/40">Loading activity feed...</span>
        </div>
      </div>
    );
  }

  if (error && activities.length === 0) {
    return (
      <div className="flex-1 flex items-center justify-center px-4">
        <div className="text-center max-w-sm">
          <AlertCircle size={32} className="text-red-400/60 mx-auto mb-3" />
          <p className="text-sm text-white/50 mb-3">{error}</p>
          <button onClick={fetchActivity} className="px-4 py-2 rounded-lg bg-white/[0.06] hover:bg-white/[0.10] text-sm text-white/60 transition-all">
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="flex-1 overflow-y-auto px-6 py-8 scroll-smooth">
      <div className="max-w-2xl mx-auto">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-xl font-semibold text-white/90">Activity</h1>
            <p className="text-sm text-white/30 mt-0.5">Autonomous events, memory updates, and learning progress</p>
          </div>
          <button
            onClick={fetchActivity}
            className="w-8 h-8 rounded-lg hover:bg-white/[0.06] flex items-center justify-center text-white/30 hover:text-white/60 transition-all"
            title="Refresh"
          >
            <RefreshCw size={15} />
          </button>
        </div>

        {activities.length === 0 ? (
          <div className="p-8 rounded-xl bg-white/[0.03] border border-white/[0.06] text-center">
            <Activity size={32} className="text-white/10 mx-auto mb-3" />
            <p className="text-sm text-white/30">No recent activity</p>
            <p className="text-xs text-white/20 mt-1">Events will appear here as the agent works</p>
          </div>
        ) : (
          <div className="space-y-2">
            {activities.map((item, i) => {
              const Icon = getActivityIcon(item);
              const colorClass = getActivityColor(item);
              return (
                <div
                  key={i}
                  className="flex items-start gap-3 px-4 py-3 rounded-xl bg-white/[0.03] border border-white/[0.06] hover:bg-white/[0.06] transition-all stagger-item"
                >
                  <div className={`w-8 h-8 rounded-lg ${colorClass} flex items-center justify-center shrink-0`}>
                    <Icon size={14} />
                  </div>
                  <p className="text-sm text-white/60 leading-relaxed pt-1">{item}</p>
                </div>
              );
            })}
          </div>
        )}

        {activities.length > 0 && (
          <div className="mt-4 text-center">
            <p className="text-[11px] text-white/15">Auto-refreshes every 10 seconds</p>
          </div>
        )}
      </div>
    </div>
  );
}