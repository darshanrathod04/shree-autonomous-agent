import { useState, useEffect } from 'react';
import { learningApi, type LessonData } from '@/shared/services/learningApi';
import { BookOpen, GraduationCap, Play, ClipboardList, RefreshCw, AlertCircle, ChevronRight } from 'lucide-react';

export function LearningPage() {
  const [lesson, setLesson] = useState<LessonData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchLesson = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await learningApi.getLesson();
      setLesson(data);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to load lesson data');
      if (err && typeof err === 'object' && 'code' in err && (err as { code?: string }).code === 'ERR_NETWORK') {
        setError('Backend offline. Start the Shree AI backend server.');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLesson();
    const interval = setInterval(fetchLesson, 15000);
    return () => clearInterval(interval);
  }, []);

  if (loading && !lesson) {
    return (
      <div className="flex-1 flex items-center justify-center">
        <div className="flex items-center gap-3">
          <RefreshCw size={18} className="text-[#a78bfa] animate-spin" />
          <span className="text-sm text-white/40">Loading lesson...</span>
        </div>
      </div>
    );
  }

  if (error && !lesson) {
    return (
      <div className="flex-1 flex items-center justify-center px-4">
        <div className="text-center max-w-sm">
          <AlertCircle size={32} className="text-red-400/60 mx-auto mb-3" />
          <p className="text-sm text-white/50 mb-3">{error}</p>
          <button onClick={fetchLesson} className="px-4 py-2 rounded-lg bg-white/[0.06] hover:bg-white/[0.10] text-sm text-white/60 transition-all">
            Retry
          </button>
        </div>
      </div>
    );
  }

  const hasLesson = lesson?.hasActiveLesson;

  return (
    <div className="flex-1 overflow-y-auto px-6 py-8 scroll-smooth">
      <div className="max-w-2xl mx-auto">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-xl font-semibold text-white/90">Learning</h1>
            <p className="text-sm text-white/30 mt-0.5">Active lesson and chapter progress</p>
          </div>
          <button
            onClick={fetchLesson}
            className="w-8 h-8 rounded-lg hover:bg-white/[0.06] flex items-center justify-center text-white/30 hover:text-white/60 transition-all"
            title="Refresh"
          >
            <RefreshCw size={15} />
          </button>
        </div>

        {!hasLesson ? (
          <div className="p-8 rounded-xl bg-white/[0.03] border border-white/[0.06] text-center">
            <GraduationCap size={32} className="text-white/10 mx-auto mb-3" />
            <p className="text-sm text-white/30">No active lesson</p>
            <p className="text-xs text-white/20 mt-1">Start a conversation to begin learning</p>
          </div>
        ) : (
          <>
            {/* Active Lesson Card */}
            <div className="p-5 rounded-xl bg-gradient-to-br from-blue-500/10 to-blue-400/5 border border-blue-500/15 mb-4">
              <div className="flex items-start gap-3">
                <div className="w-10 h-10 rounded-xl bg-blue-500/15 flex items-center justify-center shrink-0">
                  <BookOpen size={18} className="text-blue-400" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-[11px] text-blue-300/60 uppercase tracking-wider mb-1">Current Lesson</p>
                  <p className="text-base font-semibold text-white">{lesson?.lessonName || 'Untitled Lesson'}</p>
                  {lesson?.activeTopic && (
                    <p className="text-sm text-white/50 mt-1">{lesson.activeTopic}</p>
                  )}
                </div>
              </div>
            </div>

            {/* Chapter & Objective */}
            <div className="grid grid-cols-2 gap-3 mb-4">
              <div className="p-4 rounded-xl bg-white/[0.03] border border-white/[0.06]">
                <p className="text-[11px] text-white/30 uppercase tracking-wider mb-1">Chapter</p>
                <p className="text-lg font-semibold text-white">{lesson?.chapterNumber ?? 0}</p>
              </div>
              <div className="p-4 rounded-xl bg-white/[0.03] border border-white/[0.06]">
                <p className="text-[11px] text-white/30 uppercase tracking-wider mb-1">Chapters Complete</p>
                <p className="text-lg font-semibold text-white">{Array.isArray(lesson?.completedChapters) ? lesson.completedChapters.length : 0}</p>
              </div>
            </div>

            {/* Objective */}
            {lesson?.currentObjective && (
              <div className="p-4 rounded-xl bg-gradient-to-br from-amber-500/8 to-amber-400/5 border border-amber-500/15 mb-4">
                <div className="flex items-start gap-2.5">
                  <ChevronRight size={15} className="text-amber-400 shrink-0 mt-0.5" />
                  <div>
                    <p className="text-[11px] text-amber-300/60 uppercase tracking-wider mb-1">Current Objective</p>
                    <p className="text-sm text-white/70">{lesson.currentObjective}</p>
                  </div>
                </div>
              </div>
            )}

            {/* Action Buttons */}
            <div className="grid grid-cols-2 gap-3">
              <button className="flex items-center gap-3 px-4 py-3 rounded-xl bg-gradient-to-r from-emerald-500/15 to-emerald-400/10 border border-emerald-500/20 hover:from-emerald-500/25 hover:to-emerald-400/20 transition-all duration-200">
                <Play size={16} className="text-emerald-400" />
                <span className="text-sm font-medium text-white/80">Continue</span>
              </button>
              <button className="flex items-center gap-3 px-4 py-3 rounded-xl bg-gradient-to-r from-purple-500/15 to-purple-400/10 border border-purple-500/20 hover:from-purple-500/25 hover:to-purple-400/20 transition-all duration-200">
                <ClipboardList size={16} className="text-purple-400" />
                <span className="text-sm font-medium text-white/80">Quiz</span>
              </button>
            </div>

            {/* Progress Summary */}
            {lesson?.progressSummary && (
              <div className="mt-4 p-4 rounded-xl bg-white/[0.03] border border-white/[0.06]">
                <p className="text-[11px] text-white/30 uppercase tracking-wider mb-2">Progress Summary</p>
                <p className="text-sm text-white/60 leading-relaxed">{lesson.progressSummary}</p>
              </div>
            )}

            {/* Pending Follow-ups */}
            {Array.isArray(lesson?.pendingFollowups) && lesson.pendingFollowups.length > 0 && (
              <div className="mt-4 p-4 rounded-xl bg-white/[0.03] border border-white/[0.06]">
                <p className="text-[11px] text-white/30 uppercase tracking-wider mb-3">Pending Follow-ups</p>
                <div className="space-y-2">
                  {lesson.pendingFollowups.map((item: unknown, i: number) => (
                    <div key={i} className="flex items-center gap-2 text-sm text-white/50">
                      <ChevronRight size={12} className="text-white/20" />
                      <span>{String(item)}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}