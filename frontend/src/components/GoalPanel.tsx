import { useEffect, useState } from 'react';
import { fetchGoalData } from '@/services/dashboard';
import type { GoalData } from '@/services/dashboard';
import { Target, CheckCircle2, Circle, TrendingUp, Calendar } from 'lucide-react';

export function GoalPanel() {
  const [data, setData] = useState<GoalData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await fetchGoalData();
      setData(result);
    } catch (err: any) {
      setError(err.response?.data?.error || 'Failed to load goals');
    } finally {
      setLoading(false);
    }
  };

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
          <button onClick={loadData} className="text-xs text-[var(--accent)] hover:underline">
            Try again
          </button>
        </div>
      </div>
    );
  }

  if (!data || !data.hasGoal) {
    return (
      <div className="flex-1 flex items-center justify-center">
        <div className="text-center">
          <Target size={40} className="mx-auto mb-3 text-[var(--text-secondary)]" />
          <h3 className="text-lg font-semibold mb-1">No active goal</h3>
          <p className="text-sm text-[var(--text-secondary)]">
            Ask the agent to help you set a learning goal
          </p>
        </div>
      </div>
    );
  }

  const progress = data.progressPercent ?? 0;
  const completed = data.completedSubGoals ?? 0;
  const total = data.totalSubGoals ?? 0;

  return (
    <div className="flex-1 overflow-y-auto p-6">
      <div className="max-w-2xl mx-auto space-y-6">
        {/* Header */}
        <div>
          <h2 className="text-xl font-bold mb-1">Current Goal</h2>
          <p className="text-sm text-[var(--text-secondary)]">
            {data.createdAt
              ? `Created ${new Date(data.createdAt).toLocaleDateString()}`
              : 'Learning objective'}
          </p>
        </div>

        {/* Goal Card */}
        <div className="p-5 rounded-xl bg-[var(--bg-secondary)] border border-[var(--border-color)]">
          <div className="flex items-start gap-3 mb-4">
            <div className={`p-2 rounded-lg ${data.completed ? 'bg-[var(--success)]/10' : 'bg-[var(--accent)]/10'}`}>
              {data.completed ? (
                <CheckCircle2 size={20} className="text-[var(--success)]" />
              ) : (
                <Target size={20} className="text-[var(--accent)]" />
              )}
            </div>
            <div className="flex-1">
              <h3 className="font-semibold mb-1">{data.description}</h3>
              <span
                className={`inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded-full ${
                  data.completed
                    ? 'bg-[var(--success)]/10 text-[var(--success)]'
                    : 'bg-[var(--accent)]/10 text-[var(--accent)]'
                }`}
              >
                {data.completed ? 'Completed' : 'In Progress'}
              </span>
            </div>
          </div>

          {/* Progress Bar */}
          <div className="space-y-2">
            <div className="flex items-center justify-between text-sm">
              <span className="text-[var(--text-secondary)]">Progress</span>
              <span className="font-medium">{progress}%</span>
            </div>
            <div className="h-2 rounded-full bg-[var(--bg-tertiary)] overflow-hidden">
              <div
                className="h-full rounded-full bg-[var(--accent)] transition-all duration-500"
                style={{ width: `${progress}%` }}
              />
            </div>
          </div>
        </div>

        {/* Sub-goals */}
        {data.subGoals && data.subGoals.length > 0 && (
          <div>
            <h3 className="text-sm font-semibold mb-3 flex items-center gap-2">
              <CheckCircle2 size={16} />
              Sub-goals ({completed}/{total})
            </h3>
            <div className="space-y-2">
              {data.subGoals.map((sg, i) => (
                <div
                  key={i}
                  className={`flex items-start gap-3 p-3 rounded-lg border ${
                    sg.completed
                      ? 'border-[var(--success)]/20 bg-[var(--success)]/5'
                      : 'border-[var(--border-color)] bg-[var(--bg-secondary)]'
                  }`}
                >
                  {sg.completed ? (
                    <CheckCircle2 size={18} className="text-[var(--success)] mt-0.5 shrink-0" />
                  ) : (
                    <Circle size={18} className="text-[var(--text-secondary)] mt-0.5 shrink-0" />
                  )}
                  <span className={`text-sm ${sg.completed ? 'line-through text-[var(--text-secondary)]' : ''}`}>
                    {sg.description}
                  </span>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Stats */}
        <div className="grid grid-cols-2 gap-3">
          <div className="p-4 rounded-xl bg-[var(--bg-secondary)] border border-[var(--border-color)]">
            <div className="flex items-center gap-2 mb-1">
              <TrendingUp size={16} className="text-[var(--accent)]" />
              <span className="text-xs text-[var(--text-secondary)]">Progress</span>
            </div>
            <p className="text-2xl font-bold">{progress}%</p>
          </div>
          <div className="p-4 rounded-xl bg-[var(--bg-secondary)] border border-[var(--border-color)]">
            <div className="flex items-center gap-2 mb-1">
              <CheckCircle2 size={16} className="text-[var(--success)]" />
              <span className="text-xs text-[var(--text-secondary)]">Completed</span>
            </div>
            <p className="text-2xl font-bold">
              {completed}/{total}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}