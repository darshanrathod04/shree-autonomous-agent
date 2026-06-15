import { useState, useEffect } from 'react';
import { memoryApi, type MemoryData } from '@/shared/services/memoryApi';
import { Brain, Database, BookOpen, Target, RefreshCw, AlertCircle } from 'lucide-react';

export function MemoryPage() {
  const [memory, setMemory] = useState<MemoryData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchMemory = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await memoryApi.getMemory();
      setMemory(data);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to load memory data');
      if (err && typeof err === 'object' && 'code' in err && (err as { code?: string }).code === 'ERR_NETWORK') {
        setError('Backend offline. Start the Shree AI backend server.');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMemory();
    const interval = setInterval(fetchMemory, 15000);
    return () => clearInterval(interval);
  }, []);

  if (loading && !memory) {
    return (
      <div className="flex-1 flex items-center justify-center">
        <div className="flex items-center gap-3">
          <RefreshCw size={18} className="text-[#a78bfa] animate-spin" />
          <span className="text-sm text-white/40">Loading memory data...</span>
        </div>
      </div>
    );
  }

  if (error && !memory) {
    return (
      <div className="flex-1 flex items-center justify-center px-4">
        <div className="text-center max-w-sm">
          <AlertCircle size={32} className="text-red-400/60 mx-auto mb-3" />
          <p className="text-sm text-white/50 mb-3">{error}</p>
          <button onClick={fetchMemory} className="px-4 py-2 rounded-lg bg-white/[0.06] hover:bg-white/[0.10] text-sm text-white/60 transition-all">
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
            <h1 className="text-xl font-semibold text-white/90">Memory</h1>
            <p className="text-sm text-white/30 mt-0.5">Semantic and episodic memory overview</p>
          </div>
          <button
            onClick={fetchMemory}
            className="w-8 h-8 rounded-lg hover:bg-white/[0.06] flex items-center justify-center text-white/30 hover:text-white/60 transition-all"
            title="Refresh"
          >
            <RefreshCw size={15} />
          </button>
        </div>

        <div className="grid grid-cols-2 gap-3 mb-6">
          <div className="p-4 rounded-xl bg-gradient-to-br from-indigo-500/10 to-indigo-400/5 border border-indigo-500/15">
            <div className="flex items-center gap-2 mb-3">
              <div className="w-8 h-8 rounded-lg bg-indigo-500/15 flex items-center justify-center">
                <Database size={15} className="text-indigo-400" />
              </div>
              <div>
                <p className="text-[11px] text-indigo-300/60 uppercase tracking-wider">Semantic</p>
              </div>
            </div>
            <p className="text-2xl font-semibold text-white">{memory?.semanticCount ?? 0}</p>
            <p className="text-xs text-white/30 mt-1">concepts stored</p>
          </div>

          <div className="p-4 rounded-xl bg-gradient-to-br from-purple-500/10 to-purple-400/5 border border-purple-500/15">
            <div className="flex items-center gap-2 mb-3">
              <div className="w-8 h-8 rounded-lg bg-purple-500/15 flex items-center justify-center">
                <Brain size={15} className="text-purple-400" />
              </div>
              <div>
                <p className="text-[11px] text-purple-300/60 uppercase tracking-wider">Episodic</p>
              </div>
            </div>
            <p className="text-2xl font-semibold text-white">{memory?.episodicCount ?? 0}</p>
            <p className="text-xs text-white/30 mt-1">episodes recorded</p>
          </div>
        </div>

        {/* Active Context */}
        <div className="p-4 rounded-xl bg-white/[0.03] border border-white/[0.06] mb-4">
          <h2 className="text-[13px] font-medium text-white/50 mb-3">Active Context</h2>
          <div className="space-y-3">
            <div className="flex items-center justify-between py-1.5 border-b border-white/[0.04]">
              <span className="text-xs text-white/40">Active Topic</span>
              <span className="text-xs text-white/70">{memory?.activeTopic || 'None'}</span>
            </div>
            <div className="flex items-center justify-between py-1.5 border-b border-white/[0.04]">
              <span className="text-xs text-white/40">Lesson</span>
              <span className="text-xs text-white/70">{memory?.lessonName || 'None'}</span>
            </div>
            <div className="flex items-center justify-between py-1.5 border-b border-white/[0.04]">
              <span className="text-xs text-white/40">Chapter</span>
              <span className="text-xs text-white/70">{memory?.chapterNumber ?? 0}</span>
            </div>
            <div className="flex items-center justify-between py-1.5 border-b border-white/[0.04]">
              <span className="text-xs text-white/40">Completed Chapters</span>
              <span className="text-xs text-white/70">{memory?.completedChapters ?? 0}</span>
            </div>
            <div className="flex items-center justify-between py-1.5">
              <span className="text-xs text-white/40">Active Lesson</span>
              <span className={`text-xs ${memory?.hasActiveLesson ? 'text-emerald-400' : 'text-white/30'}`}>
                {memory?.hasActiveLesson ? 'Yes' : 'No'}
              </span>
            </div>
          </div>
        </div>

        {memory?.currentObjective && (
          <div className="p-4 rounded-xl bg-gradient-to-br from-amber-500/8 to-amber-400/5 border border-amber-500/15">
            <div className="flex items-start gap-2.5">
              <Target size={15} className="text-amber-400 shrink-0 mt-0.5" />
              <div>
                <p className="text-[11px] text-amber-300/60 uppercase tracking-wider mb-1">Current Objective</p>
                <p className="text-sm text-white/70">{memory.currentObjective}</p>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}