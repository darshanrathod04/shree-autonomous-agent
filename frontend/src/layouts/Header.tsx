import { useUIStore } from '@/shared/stores/uiStore';
import {
  Goal,
  Menu,
  Brain,
  BookOpen,
  Cpu,
  GraduationCap,
  MessageSquare,
} from 'lucide-react';
import type { PersonalityMode, NavigationTab } from '@/shared/types';

const PERSONALITY_MODES: { id: PersonalityMode; name: string; description: string; icon: string; color: string; gradient: string }[] = [
  { id: 'teacher', name: 'Teacher', description: 'Explains concepts clearly', icon: '📚', color: '#60a5fa', gradient: 'from-blue-500/20 to-blue-400/10' },
  { id: 'coach', name: 'Coach', description: 'Guides and motivates', icon: '🏆', color: '#f59e0b', gradient: 'from-amber-500/20 to-amber-400/10' },
  { id: 'assistant', name: 'Assistant', description: 'Helpful and efficient', icon: '🤖', color: '#a78bfa', gradient: 'from-purple-500/20 to-purple-400/10' },
  { id: 'friend', name: 'Friend', description: 'Casual and empathetic', icon: '💚', color: '#34d399', gradient: 'from-emerald-500/20 to-emerald-400/10' },
];

const MODE_ICONS: Record<PersonalityMode, string> = {
  teacher: '📚',
  coach: '🏆',
  assistant: '🤖',
  friend: '💚',
};

const NAV_ITEMS: { id: NavigationTab; label: string; icon: typeof Brain }[] = [
  { id: 'chat', label: 'Chat', icon: MessageSquare },
  { id: 'memory', label: 'Memory', icon: Brain },
  { id: 'goals', label: 'Goals', icon: Goal },
  { id: 'learning', label: 'Learning', icon: GraduationCap },
];

interface HeaderProps {
  onToggleSidebar?: () => void;
}

export function Header({ onToggleSidebar }: HeaderProps) {
  const {
    personalityMode,
    setPersonalityMode,
    connectionStatus,
    showMobileSidebar,
    setShowMobileSidebar,
    activeNavigation,
    setActiveNavigation,
    aiStatus,
  } = useUIStore();

  const currentMode = PERSONALITY_MODES.find((m) => m.id === personalityMode) || PERSONALITY_MODES[0];

  const statusConfig = {
    connected: { dotColor: 'bg-emerald-400', color: 'text-emerald-400', label: 'Connected' },
    connecting: { dotColor: 'bg-amber-400', color: 'text-amber-400', label: 'Connecting...' },
    disconnected: { dotColor: 'bg-gray-500', color: 'text-gray-500', label: 'Disconnected' },
    error: { dotColor: 'bg-red-400', color: 'text-red-400', label: 'Error' },
  };

  return (
    <header className="h-12 flex items-center justify-between px-3 border-b border-white/[0.06] bg-[#0a0a0f]/70 backdrop-blur-xl shrink-0 z-[var(--z-header)] relative">
      {/* Left section */}
      <div className="flex items-center gap-2 min-w-0">
        {/* Mobile menu */}
        <button
          onClick={() => setShowMobileSidebar(!showMobileSidebar)}
          className="md:hidden w-7 h-7 rounded-lg hover:bg-white/[0.06] flex items-center justify-center text-white/50 hover:text-white/80 transition-all shrink-0"
        >
          <Menu size={16} />
        </button>

        {/* Connection Status - dot style */}
        <div className="flex items-center gap-2 shrink-0">
          <span className={`w-2 h-2 rounded-full ${statusConfig[connectionStatus].dotColor} ${connectionStatus === 'connecting' ? 'animate-pulse' : ''}`} />
          <span className="text-[11px] text-white/30 font-medium hidden sm:inline">{statusConfig[connectionStatus].label}</span>
        </div>

        <span className="text-white/[0.08] text-[10px] hidden sm:inline">|</span>

        {/* Personality Mode Badge with icon - using div instead of button to avoid nested <button> */}
        <div className="relative group">
          <div
            onClick={() => {
              const modes: PersonalityMode[] = ['teacher', 'coach', 'assistant', 'friend'];
              const currentIdx = modes.indexOf(personalityMode);
              setPersonalityMode(modes[(currentIdx + 1) % modes.length]);
            }}
            role="button"
            tabIndex={0}
            onKeyDown={(e) => { if (e.key === 'Enter') { const modes: PersonalityMode[] = ['teacher', 'coach', 'assistant', 'friend']; const currentIdx = modes.indexOf(personalityMode); setPersonalityMode(modes[(currentIdx + 1) % modes.length]); } }}
            className="flex items-center gap-1.5 px-2 py-1 rounded-lg hover:bg-white/[0.06] border border-transparent hover:border-white/[0.08] transition-all duration-200 shrink-0 cursor-pointer"
          >
            <span className="text-sm">{MODE_ICONS[personalityMode]}</span>
            <span className="text-[11px] font-medium text-white/60 group-hover:text-white/80 transition-colors">{currentMode.name}</span>
          </div>
          
          {/* Dropdown */}
          <div className="absolute top-full left-0 mt-1.5 w-48 p-1.5 rounded-xl bg-[#121218] border border-white/[0.08] shadow-xl opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 pointer-events-none z-50">
            {PERSONALITY_MODES.map((mode) => (
              <button
                key={mode.id}
                onClick={() => setPersonalityMode(mode.id)}
                className={`w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-left transition-all ${
                  mode.id === personalityMode
                    ? 'bg-white/[0.08] text-white'
                    : 'text-white/50 hover:text-white/80 hover:bg-white/[0.04]'
                }`}
              >
                <span className="text-base">{mode.icon}</span>
                <div className="min-w-0">
                  <p className="text-[12px] font-medium">{mode.name}</p>
                  <p className="text-[10px] text-white/30 truncate">{mode.description}</p>
                </div>
                {mode.id === personalityMode && (
                  <div className="w-1.5 h-1.5 rounded-full bg-current ml-auto shrink-0 text-[#a78bfa]" />
                )}
              </button>
            ))}
          </div>
        </div>

        {/* AI Status Badges */}
        {aiStatus.memoryActive && (
          <div className="hidden md:flex items-center gap-1 px-2 py-1 rounded-lg bg-indigo-500/8 border border-indigo-500/15">
            <Brain size={10} className="text-indigo-400" />
            <span className="text-[10px] font-medium text-indigo-300 whitespace-nowrap">Memory Active</span>
          </div>
        )}
        {aiStatus.goalActive && (
          <div className="hidden md:flex items-center gap-1 px-2 py-1 rounded-lg bg-emerald-500/8 border border-emerald-500/15">
            <Goal size={10} className="text-emerald-400" />
            <span className="text-[10px] font-medium text-emerald-300 whitespace-nowrap">Goal Active</span>
          </div>
        )}
        {aiStatus.learningMode && (
          <div className="hidden lg:flex items-center gap-1 px-2 py-1 rounded-lg bg-amber-500/8 border border-amber-500/15">
            <BookOpen size={10} className="text-amber-400" />
            <span className="text-[10px] font-medium text-amber-300 whitespace-nowrap">Learning Mode</span>
          </div>
        )}
        {aiStatus.autonomous && (
          <div className="hidden lg:flex items-center gap-1 px-2 py-1 rounded-lg bg-cyan-500/8 border border-cyan-500/15">
            <Cpu size={10} className="text-cyan-400" />
            <span className="text-[10px] font-medium text-cyan-300 whitespace-nowrap">Autonomous</span>
          </div>
        )}
      </div>

      {/* Center - Title */}
      <div className="absolute left-1/2 -translate-x-1/2 hidden md:flex items-center gap-2">
        <div className="w-5 h-5 rounded-md bg-gradient-to-br from-[#7c6cf0] to-[#a78bfa] flex items-center justify-center shadow-sm shadow-[#7c6cf0]/20">
          <span className="text-[9px] text-white font-bold">S</span>
        </div>
        <span className="text-[13px] font-semibold text-white/90 tracking-tight">Shree AI</span>
      </div>

      {/* Right - Navigation Tabs */}
      <div className="flex items-center gap-0.5">
        {NAV_ITEMS.map((item) => {
          const Icon = item.icon;
          const isActive = activeNavigation === item.id;
          return (
            <button
              key={item.id}
              onClick={() => setActiveNavigation(item.id)}
              className={`flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg text-[11px] font-medium transition-all duration-200 ${
                isActive
                  ? 'text-white bg-white/[0.08]'
                  : 'text-white/30 hover:text-white/60 hover:bg-white/[0.04]'
              }`}
            >
              <Icon size={13} />
              <span className="hidden sm:inline">{item.label}</span>
            </button>
          );
        })}
      </div>
    </header>
  );
}