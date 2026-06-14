import { useEffect, useState } from 'react';
import { fetchMemoryData, fetchLessonData, fetchProfileData } from '@/services/dashboard';
import type { MemoryData, LessonData, ProfileData } from '@/services/dashboard';
import { Brain, BookOpen, User, Database, Layers, Bookmark } from 'lucide-react';

export function MemoryPanel() {
  const [memory, setMemory] = useState<MemoryData | null>(null);
  const [lesson, setLesson] = useState<LessonData | null>(null);
  const [profile, setProfile] = useState<ProfileData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadAll();
  }, []);

  const loadAll = async () => {
    setLoading(true);
    setError(null);
    try {
      const [mem, les, prof] = await Promise.all([
        fetchMemoryData(),
        fetchLessonData(),
        fetchProfileData(),
      ]);
      setMemory(mem);
      setLesson(les);
      setProfile(prof);
    } catch (err: any) {
      setError(err.response?.data?.error || 'Failed to load memory data');
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
          <button onClick={loadAll} className="text-xs text-[var(--accent)] hover:underline">
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
          <h2 className="text-xl font-bold mb-1">Memory & Knowledge</h2>
          <p className="text-sm text-[var(--text-secondary)]">
            Agent's stored knowledge and current learning context
          </p>
        </div>

        {/* Memory Stats */}
        <div className="grid grid-cols-2 gap-3">
          <div className="p-4 rounded-xl bg-[var(--bg-secondary)] border border-[var(--border-color)]">
            <div className="flex items-center gap-2 mb-2">
              <Database size={16} className="text-[var(--accent)]" />
              <span className="text-xs text-[var(--text-secondary)]">Semantic</span>
            </div>
            <p className="text-2xl font-bold">{memory?.semanticCount ?? 0}</p>
            <p className="text-xs text-[var(--text-secondary)] mt-1">concepts stored</p>
          </div>
          <div className="p-4 rounded-xl bg-[var(--bg-secondary)] border border-[var(--border-color)]">
            <div className="flex items-center gap-2 mb-2">
              <Layers size={16} className="text-[var(--warning)]" />
              <span className="text-xs text-[var(--text-secondary)]">Episodic</span>
            </div>
            <p className="text-2xl font-bold">{memory?.episodicCount ?? 0}</p>
            <p className="text-xs text-[var(--text-secondary)] mt-1">experiences</p>
          </div>
        </div>

        {/* Active Learning */}
        {lesson?.hasActiveLesson && (
          <div className="p-5 rounded-xl bg-[var(--bg-secondary)] border border-[var(--border-color)]">
            <div className="flex items-center gap-2 mb-4">
              <div className="p-2 rounded-lg bg-[var(--accent)]/10">
                <BookOpen size={18} className="text-[var(--accent)]" />
              </div>
              <div>
                <h3 className="font-semibold">Active Lesson</h3>
                <p className="text-xs text-[var(--text-secondary)]">{lesson.lessonName}</p>
              </div>
            </div>

            <div className="space-y-3">
              <div className="flex items-center justify-between text-sm">
                <span className="text-[var(--text-secondary)]">Topic</span>
                <span className="font-medium">{lesson.activeTopic || 'N/A'}</span>
              </div>
              <div className="flex items-center justify-between text-sm">
                <span className="text-[var(--text-secondary)]">Chapter</span>
                <span className="font-medium">{lesson.chapterNumber || 1}</span>
              </div>
              {lesson.currentObjective && (
                <div className="flex items-center justify-between text-sm">
                  <span className="text-[var(--text-secondary)]">Objective</span>
                  <span className="font-medium text-right max-w-[60%]">{lesson.currentObjective}</span>
                </div>
              )}
              {lesson.completedChapters && lesson.completedChapters.length > 0 && (
                <div>
                  <span className="text-xs text-[var(--text-secondary)]">Completed chapters:</span>
                  <div className="flex flex-wrap gap-1 mt-1">
                    {lesson.completedChapters.map((ch, i) => (
                      <span
                        key={i}
                        className="text-xs px-2 py-0.5 rounded-full bg-[var(--success)]/10 text-[var(--success)]"
                      >
                        {ch}
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>
        )}

        {/* Profile */}
        {profile && (
          <div className="p-5 rounded-xl bg-[var(--bg-secondary)] border border-[var(--border-color)]">
            <div className="flex items-center gap-2 mb-4">
              <div className="p-2 rounded-lg bg-[var(--accent)]/10">
                <User size={18} className="text-[var(--accent)]" />
              </div>
              <div>
                <h3 className="font-semibold">User Profile</h3>
                <p className="text-xs text-[var(--text-secondary)]">{profile.name || 'Anonymous'}</p>
              </div>
            </div>

            <div className="space-y-3">
              <div className="flex items-center justify-between text-sm">
                <span className="text-[var(--text-secondary)]">Teaching Style</span>
                <span className="font-medium capitalize">{profile.teachingStyle}</span>
              </div>
              <div className="flex items-center justify-between text-sm">
                <span className="text-[var(--text-secondary)]">Preferred Tone</span>
                <span className="font-medium capitalize">{profile.preferredTone}</span>
              </div>
              <div className="flex items-center justify-between text-sm">
                <span className="text-[var(--text-secondary)]">Personality Mode</span>
                <span className="font-medium capitalize">{profile.personalityMode}</span>
              </div>
            </div>
          </div>
        )}

        {/* Active Topic */}
        {memory?.activeTopic && (
          <div className="p-4 rounded-xl bg-[var(--bg-secondary)] border border-[var(--border-color)]">
            <div className="flex items-center gap-2 mb-2">
              <Bookmark size={16} className="text-[var(--accent)]" />
              <span className="text-sm font-medium">Active Topic</span>
            </div>
            <p className="text-sm text-[var(--text-secondary)]">{memory.activeTopic}</p>
          </div>
        )}
      </div>
    </div>
  );
}