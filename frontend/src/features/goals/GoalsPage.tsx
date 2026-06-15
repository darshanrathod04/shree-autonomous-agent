import { useState, useEffect } from 'react';
import { goalApi, type GoalData } from '@/shared/services/goalApi';
import { Target, CheckCircle2, Circle, RefreshCw, AlertCircle, TrendingUp } from 'lucide-react';

export function GoalsPage() {
  const [goal, setGoal] = useState<GoalData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchGoals = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await goalApi.getGoals();
      setGoal(data);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to load goals');
      if (err && typeof err === 'object' && 'code' in err && (err as { code?: string }).code === 'ERR_NETWORK') {
        setError('Backend offline. Start the Shree AI backend server.');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchGoals();
    const interval = setInterval(fetchGoals, 15000);
    return () => clearInterval(interval);
  }, []);

  if (loading && !goal) {
    return (
      <div className="flex-1 flex items-center justify-center">
        <div className="flex items-center gap-3">
          <RefreshCw size={18} className="text-[#a78bfa] animate-spin" />
          <span className="text-sm text-white/40">Loading goals...</span>
        </div>
      </div>
    );
  }

  if (error && !goal) {
    return (
      <div className="flex-1 flex items-center justify-center px-4">
        <div className="text-center max-w-sm">
          <AlertCircle size={32} className="text-red-400/60 mx-auto mb-3" />
          <p className="text-sm text-white/50 mb-3">{error}</p>
          <button onClick={fetchGoals} className="px-4 py-2 rounded-lg bg-white/[0.06] hover:bg-white/[0.10] text-sm text-white/60 transition-all">
            Retry
          </button>
        </div>
      </div>
    );
  }

  const progress = goal?.progressPercent ?? 0;
  const total = goal?.totalSubGoals ?? 0;
  const completed = goal?.completedSubGoals ?? 0;

  return (
    <div className="flex-1 overflow-y-auto px-6 py-8 scroll-smooth">
      <div className="max-w-2xl mx-auto">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-xl font-semibold text-white/90">Goals</h1>
            <p className="text-sm text-white/30 mt-0.5">Active goal and progress tracking</p>
          </div>
          <button
            onClick={fetchGoals}
            className="w-8 h-8 rounded-lg hover:bg-white/[0.06] flex items-center justify-center text-white/30 hover:text-white/60 transition-all"
            title="Refresh"
          >
            <RefreshCw size={15} />
          </button>
        </div>

        {!goal?.hasGoal ? (
          <div className="p-8 rounded-xl bg-white/[0.03] border border-white/[0.06] text-center">
            <Target size={32} className="text-white/10 mx-auto mb-3" />
            <p className="text-sm text-white/30">No active goal</p>
            <p className="text-xs text-white/20 mt-1">The agent will create goals autonomously</p>
          </div>
        ) : (
          <>
            {/* Progress Ring */}
            <div className="p-5 rounded-xl bg-gradient-to-br from-emerald-500/10 to-emerald-400/5 border border-emerald-500/15 mb-4">
              <div className="flex items-center gap-4">
                <div className="relative w-20 h-20">
                  <svg className="w-20 h-20 -rotate-90" viewBox="0 0 80 80">
                    <circle cx="40" cy="40" r="34" fill="none" stroke="rgba(255,255,255,0.06)" strokeWidth="6" />
                    <circle
                      cx="40" cy="40" r="34"
                      fill="none"
                      stroke="#34d399"
                      strokeWidth="6"
                      strokeLinecap="round"
                      strokeDasharray={`${(progress / 100) * 213.6} 213.6`}
                      className="transition-all duration-500"
                    />
                  </svg>
                  <div className="absolute inset-0 flex items-center justify-center">
                    <span className="text-lg font-semibold text-white">{progress}%</span>
                  </div>
                </div>
                <div className="flex-1">
                  <p className="text-sm font-medium text-white/80">{goal?.description || 'Active Goal'}</p>
                  <p className="text-xs text-white/40 mt-1">
                    {completed} of {total} subgoals completed
                  </p>
                  <div className="flex items-center gap-1.5 mt-2">
                    <TrendingUp size={12} className="text-emerald-400" />
                    <span className="text-[11px] text-emerald-400/60">
                      {goal?.completed ? 'Goal completed!' : 'In progress'}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            {/* Subgoals */}
            <div className="p-4 rounded-xl bg-white/[0.03] border border-white/[0.06]">
              <h2 className="text-[13px] font-medium text-white/50 mb-3">Subgoals</h2>
              <div className="space-y-2">
                {goal?.subGoals?.map((sg, i) => (
                  <div key={i} className="flex items-center gap-3 px-3 py-2.5 rounded-lg bg-white/[0.03] border border-white/[0.06]">
                    {sg.completed ? (
                      <CheckCircle2 size={16} className="text-emerald-400 shrink-0" />
                    ) : (
                      <Circle size={16} className="text-white/20 shrink-0" />
                    )}
                    <span className={`text-[13px] ${sg.completed ? 'text-white/40 line-through' : 'text-white/70'}`}>
                      {sg.description}
                    </span>
                  </div>
                ))}
                {(!goal?.subGoals || goal.subGoals.length === 0) && (
                  <p className="text-xs text-white/20 text-center py-4">No subgoals defined</p>
                )}
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
}