import { useState } from 'react';
import { useUIStore } from '@/shared/stores/uiStore';
import { useSessionStore } from '@/shared/stores/sessionStore';
import { useChatStore } from '@/shared/stores/chatStore';
import {
  Plus,
  Search,
  MessageSquare,
  Trash2,
  Settings,
  Sparkles,
  PanelLeftClose,
  PanelLeft,
  Brain,
  Goal,
  GraduationCap,
} from 'lucide-react';
import type { NavigationTab } from '@/shared/types';

interface SidebarProps {
  onNewChat: () => void;
  onSelectSession: (id: string) => void;
}

const NAV_ITEMS: { id: NavigationTab; label: string; icon: typeof Brain }[] = [
  { id: 'chat', label: 'Chat', icon: MessageSquare },
  { id: 'memory', label: 'Memory', icon: Brain },
  { id: 'goals', label: 'Goals', icon: Goal },
  { id: 'learning', label: 'Learning', icon: GraduationCap },
];

function formatDate(timestamp: number) {
  const date = new Date(timestamp);
  const now = new Date();
  const diffDays = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60 * 24));
  if (diffDays === 0) return 'Today';
  if (diffDays === 1) return 'Yesterday';
  if (diffDays < 7) return `${diffDays}d ago`;
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

export function Sidebar({ onNewChat, onSelectSession }: SidebarProps) {
  const {
    sidebarCollapsed,
    setSidebarCollapsed,
    showMobileSidebar,
    setShowMobileSidebar,
    activeNavigation,
    setActiveNavigation,
  } = useUIStore();
  const {
    sessions,
    searchQuery,
    setSearchQuery,
    filteredSessions,
    removeSession,
    activeSessionId,
    setActiveSession,
  } = useSessionStore();
  const { clearMessages } = useChatStore();
  const [searchFocused, setSearchFocused] = useState(false);

  const handleNewChat = () => {
    setActiveSession(null);
    clearMessages();
    onNewChat();
    setShowMobileSidebar(false);
  };

  const handleSelectSession = (id: string) => {
    setActiveSession(id);
    onSelectSession(id);
    if (window.innerWidth < 768) setShowMobileSidebar(false);
  };

  const handleDeleteSession = (e: React.MouseEvent, id: string) => {
    e.stopPropagation();
    removeSession(id);
  };

  // Collapsed sidebar
  if (sidebarCollapsed) {
    return (
      <>
        <aside className="flex flex-col items-center py-4 gap-3 w-[60px] bg-[#0c0c12] border-r border-white/[0.06] shrink-0 z-[var(--z-sidebar)]">
          <div
            onClick={() => setSidebarCollapsed(false)}
            role="button"
            tabIndex={0}
            onKeyDown={(e) => { if (e.key === 'Enter') setSidebarCollapsed(false); }}
            className="w-9 h-9 rounded-xl bg-gradient-to-br from-[#7c6cf0] to-[#a78bfa] flex items-center justify-center text-white font-bold text-sm hover:shadow-lg hover:shadow-[#7c6cf0]/25 transition-all duration-300 cursor-pointer"
          >
            <span className="text-[15px]">S</span>
          </div>
          <div className="w-8 h-px bg-white/[0.06]" />
          <button
            onClick={handleNewChat}
            className="w-9 h-9 rounded-xl bg-white/[0.06] hover:bg-white/[0.12] flex items-center justify-center text-white/70 hover:text-white transition-all duration-200"
            title="New Chat"
          >
            <Plus size={18} />
          </button>
          {/* Nav items */}
          {NAV_ITEMS.map((item) => {
            const Icon = item.icon;
            return (
              <button
                key={item.id}
                onClick={() => setActiveNavigation(item.id)}
                className={`w-9 h-9 rounded-xl flex items-center justify-center transition-all duration-200 ${
                  activeNavigation === item.id
                    ? 'bg-[#7c6cf0]/15 text-[#a78bfa]'
                    : 'text-white/30 hover:text-white/60 hover:bg-white/[0.06]'
                }`}
                title={item.label}
              >
                <Icon size={16} />
              </button>
            );
          })}
          <div className="flex-1" />
          <button
            onClick={() => setSidebarCollapsed(false)}
            className="w-9 h-9 rounded-xl hover:bg-white/[0.06] flex items-center justify-center text-white/40 hover:text-white/70 transition-all duration-200"
            title="Expand sidebar"
          >
            <PanelLeft size={16} />
          </button>
        </aside>
        {showMobileSidebar && <MobileSidebar onClose={() => setShowMobileSidebar(false)} onNewChat={handleNewChat} onSelectSession={handleSelectSession} />}
      </>
    );
  }

  // Group sessions
  const filtered = filteredSessions();
  const groups: Record<string, typeof sessions> = {};
  filtered.forEach((session) => {
    const label = formatDate(session.createdAt);
    if (!groups[label]) groups[label] = [];
    groups[label].push(session);
  });

  return (
    <>
      <aside className="flex flex-col h-full w-[280px] bg-[#0c0c12] border-r border-white/[0.06] shrink-0 z-[var(--z-sidebar)] animate-slide-in-left">
        {/* Header */}
        <div className="flex items-center justify-between px-4 h-14 border-b border-white/[0.06] shrink-0">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-[#7c6cf0] to-[#a78bfa] flex items-center justify-center text-white font-bold text-xs shadow-sm shadow-[#7c6cf0]/20">
              <span className="text-[13px]">S</span>
            </div>
            <span className="font-semibold text-[15px] text-white/90 tracking-tight">Shree AI</span>
          </div>
          <button
            onClick={() => setSidebarCollapsed(true)}
            className="w-7 h-7 rounded-lg hover:bg-white/[0.06] flex items-center justify-center text-white/30 hover:text-white/60 transition-all duration-200"
            title="Collapse sidebar"
          >
            <PanelLeftClose size={15} />
          </button>
        </div>

        {/* New Chat Button */}
        <div className="px-3 pt-3 pb-2">
          <button
            onClick={handleNewChat}
            className="w-full flex items-center gap-2.5 px-4 py-2.5 rounded-xl bg-gradient-to-r from-[#7c6cf0]/20 to-[#a78bfa]/10 hover:from-[#7c6cf0]/30 hover:to-[#a78bfa]/20 border border-white/[0.08] hover:border-white/[0.15] text-white/80 hover:text-white transition-all duration-200 group"
          >
            <Plus size={16} className="text-[#a78bfa] group-hover:rotate-90 transition-transform duration-300" />
            <span className="text-sm font-medium">New Chat</span>
          </button>
        </div>

        {/* Navigation Tabs */}
        <div className="px-3 pb-2">
          <div className="flex gap-1 p-1 rounded-xl bg-white/[0.04] border border-white/[0.06]">
            {NAV_ITEMS.map((item) => {
              const Icon = item.icon;
              const isActive = activeNavigation === item.id;
              return (
                <button
                  key={item.id}
                  onClick={() => setActiveNavigation(item.id)}
                  className={`flex items-center justify-center gap-1.5 flex-1 px-2 py-1.5 rounded-lg text-[11px] font-medium transition-all duration-200 ${
                    isActive
                      ? 'text-white bg-[#7c6cf0]/15'
                      : 'text-white/30 hover:text-white/60'
                  }`}
                >
                  <Icon size={13} />
                </button>
              );
            })}
          </div>
        </div>

        {/* Search */}
        <div className="px-3 pb-2">
          <div className={`relative flex items-center transition-all duration-200 ${searchFocused ? 'ring-1 ring-[#7c6cf0]/40' : ''}`}>
            <Search size={14} className="absolute left-3 text-white/30 pointer-events-none" />
            <input
              type="text"
              placeholder="Search conversations..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onFocus={() => setSearchFocused(true)}
              onBlur={() => setSearchFocused(false)}
              className="w-full bg-white/[0.04] hover:bg-white/[0.08] focus:bg-white/[0.08] text-white/80 placeholder:text-white/25 text-sm rounded-lg pl-9 pr-3 py-2 outline-none transition-all duration-200"
            />
          </div>
        </div>

        {/* Sessions List */}
        <div className="flex-1 overflow-y-auto px-3 pb-2 space-y-1 scroll-smooth">
          {Object.keys(groups).length === 0 ? (
            <div className="flex flex-col items-center justify-center h-full text-center px-4">
              <Sparkles size={24} className="text-white/10 mb-3" />
              <p className="text-xs text-white/20">No conversations yet</p>
              <p className="text-xs text-white/10 mt-1">Start a new chat to begin</p>
            </div>
          ) : (
            Object.entries(groups).map(([label, groupSessions]) => (
              <div key={label} className="mb-2">
                <p className="text-[11px] font-medium text-white/20 uppercase tracking-wider px-3 py-1.5">
                  {label}
                </p>
                {groupSessions.map((session) => (
                  <div
                    key={session.id}
                    onClick={() => handleSelectSession(session.id)}
                    role="button"
                    tabIndex={0}
                    onKeyDown={(e) => { if (e.key === 'Enter') handleSelectSession(session.id); }}
                    className={`w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-left transition-all duration-200 group cursor-pointer ${
                      activeSessionId === session.id
                        ? 'bg-[#7c6cf0]/12 text-white border border-[#7c6cf0]/20'
                        : 'text-white/60 hover:text-white/85 hover:bg-white/[0.04] border border-transparent'
                    }`}
                  >
                    <MessageSquare
                      size={14}
                      className={`shrink-0 ${
                        activeSessionId === session.id ? 'text-[#a78bfa]' : 'text-white/20 group-hover:text-white/30'
                      }`}
                    />
                    <span className="text-[13px] truncate flex-1">{session.title || 'New conversation'}</span>
                    <button
                      onClick={(e) => handleDeleteSession(e, session.id)}
                      className="opacity-0 group-hover:opacity-100 text-white/20 hover:text-red-400 transition-all duration-200 shrink-0"
                    >
                      <Trash2 size={12} />
                    </button>
                  </div>
                ))}
              </div>
            ))
          )}
        </div>

        {/* Bottom Settings */}
        <div className="px-3 py-3 border-t border-white/[0.06] shrink-0">
          <button className="w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-white/50 hover:text-white/80 hover:bg-white/[0.04] transition-all duration-200 text-sm">
            <Settings size={15} />
            <span>Settings</span>
          </button>
        </div>
      </aside>

      {showMobileSidebar && <MobileSidebar onClose={() => setShowMobileSidebar(false)} onNewChat={handleNewChat} onSelectSession={handleSelectSession} />}
    </>
  );
}

function MobileSidebar({ onClose, onNewChat, onSelectSession }: {
  onClose: () => void;
  onNewChat: () => void;
  onSelectSession: (id: string) => void;
}) {
  const { sessions, activeSessionId, setActiveSession } = useSessionStore();
  const { clearMessages } = useChatStore();
  const { activeNavigation, setActiveNavigation } = useUIStore();

  const handleNewChat = () => {
    setActiveSession(null);
    clearMessages();
    onNewChat();
  };

  const NAV_ITEMS: { id: NavigationTab; label: string; icon: typeof Brain }[] = [
    { id: 'chat', label: 'Chat', icon: MessageSquare },
    { id: 'memory', label: 'Memory', icon: Brain },
    { id: 'goals', label: 'Goals', icon: Goal },
    { id: 'learning', label: 'Learning', icon: GraduationCap },
  ];

  return (
    <div className="fixed inset-0 z-[var(--z-overlay)] md:hidden">
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={onClose} />
      <aside className="absolute left-0 top-0 bottom-0 w-[280px] bg-[#0c0c12] border-r border-white/[0.06] animate-slide-in-left">
        <div className="flex items-center justify-between px-4 h-14 border-b border-white/[0.06]">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-[#7c6cf0] to-[#a78bfa] flex items-center justify-center text-white font-bold text-xs">
              <span className="text-[13px]">S</span>
            </div>
            <span className="font-semibold text-[15px] text-white/90">Shree AI</span>
          </div>
        </div>
        {/* Mobile Nav */}
        <div className="flex gap-1 p-2 border-b border-white/[0.06]">
          {NAV_ITEMS.map((item) => {
            const Icon = item.icon;
            return (
              <button
                key={item.id}
                onClick={() => { setActiveNavigation(item.id); onClose(); }}
                className={`flex items-center gap-1 px-2.5 py-1.5 rounded-lg text-xs ${
                  activeNavigation === item.id ? 'text-white bg-[#7c6cf0]/15' : 'text-white/40'
                }`}
              >
                <Icon size={13} />
                {item.label}
              </button>
            );
          })}
        </div>
        <div className="px-3 pt-3 pb-2">
          <button
            onClick={handleNewChat}
            className="w-full flex items-center gap-2.5 px-4 py-2.5 rounded-xl bg-gradient-to-r from-[#7c6cf0]/20 to-[#a78bfa]/10 border border-white/[0.08] text-white/80 text-sm font-medium"
          >
            <Plus size={16} className="text-[#a78bfa]" />
            New Chat
          </button>
        </div>
        <div className="flex-1 overflow-y-auto px-3">
          {sessions.map((session) => (
            <div
              key={session.id}
              onClick={() => onSelectSession(session.id)}
              role="button"
              tabIndex={0}
              onKeyDown={(e) => { if (e.key === 'Enter') onSelectSession(session.id); }}
              className={`w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-left text-sm cursor-pointer ${
                activeSessionId === session.id ? 'bg-[#7c6cf0]/12 text-white' : 'text-white/60 hover:bg-white/[0.04]'
              }`}
            >
              <MessageSquare size={14} className="shrink-0" />
              <span className="truncate">{session.title || 'New conversation'}</span>
            </div>
          ))}
        </div>
      </aside>
    </div>
  );
}