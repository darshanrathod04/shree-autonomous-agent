import { useState, useEffect } from 'react';
import { useSessionStore } from '@/shared/stores/sessionStore';
import { useChatStore } from '@/shared/stores/chatStore';
import { useUIStore } from '@/shared/stores/uiStore';
import type { SuggestedPrompt } from '@/shared/types';
import {
  Sparkles,
  BookOpen,
  Brain,
  Zap,
  Code,
  Palette,
  ArrowRight,
  GraduationCap,
  Target,
} from 'lucide-react';

const CATEGORY_ICONS: Record<string, string> = {
  general: '💬',
  coding: '💻',
  learning: '📚',
  creative: '🎨',
  analysis: '📊',
  business: '💼',
};

const COMMAND_CARDS = [
  { icon: BookOpen, label: 'Continue Lesson', desc: 'Resume your last learning session', color: 'from-blue-500/20 to-blue-400/10', border: 'border-blue-500/20', iconColor: 'text-blue-400' },
  { icon: Target, label: 'Resume Goal', desc: 'Pick up where you left off', color: 'from-emerald-500/20 to-emerald-400/10', border: 'border-emerald-500/20', iconColor: 'text-emerald-400' },
  { icon: Brain, label: 'Review Memory', desc: 'See what I remember about you', color: 'from-purple-500/20 to-purple-400/10', border: 'border-purple-500/20', iconColor: 'text-purple-400' },
  { icon: GraduationCap, label: 'Start Learning', desc: 'Begin a new learning journey', color: 'from-amber-500/20 to-amber-400/10', border: 'border-amber-500/20', iconColor: 'text-amber-400' },
];

export function WelcomeScreen() {
  const { suggestedPrompts } = useSessionStore();
  const { personalityMode } = useUIStore();
  const [greeting, setGreeting] = useState('');

  useEffect(() => {
    const greetings = [
      "What would you like to explore today?",
      "How can I help you?",
      "Ready to learn something new?",
      "What's on your mind?",
    ];
    setGreeting(greetings[Math.floor(Math.random() * greetings.length)]);
  }, []);

  const modeGreetings: Record<string, string> = {
    teacher: "Ready to learn something new today?",
    coach: "Let's achieve something great together!",
    assistant: "How can I help you today?",
    friend: "Hey! What's on your mind?",
  };

  const displayGreeting = modeGreetings[personalityMode] || greeting;

  return (
    <div className="flex-1 flex flex-col items-center justify-center px-4 overflow-y-auto relative">
      {/* Hidden hero orb behind the scene */}
      <div className="hero-orb" />

      <div className="w-full max-w-2xl mx-auto py-8 relative z-[1]">
        {/* Welcome Header */}
        <div className="text-center mb-8 stagger-item">
          {/* Logo with glow */}
          <div className="relative inline-flex mb-5">
            <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-[#7c6cf0] to-[#a78bfa] flex items-center justify-center shadow-lg shadow-[#7c6cf0]/25 animate-breathe">
              <span className="text-xl font-bold text-white">S</span>
            </div>
            <div className="absolute -inset-2 rounded-2xl bg-gradient-to-br from-[#7c6cf0]/15 to-[#a78bfa]/8 blur-2xl -z-10" />
          </div>

          <h1 className="text-[28px] md:text-[32px] font-bold text-white mb-1.5 tracking-tight">
            <span className="gradient-text">Shree AI</span>
          </h1>
          <p className="text-sm md:text-[15px] text-white/40 max-w-lg mx-auto leading-relaxed">
            {displayGreeting}
          </p>
        </div>

        {/* Command Cards - Premium quick actions */}
        <div className="grid grid-cols-2 gap-2.5 mb-6 stagger-item">
          {COMMAND_CARDS.map((card, i) => {
            const Icon = card.icon;
            return (
              <button
                key={i}
                className={`flex items-start gap-3 px-3.5 py-3 rounded-xl bg-gradient-to-br ${card.color} border ${card.border} hover:bg-white/[0.06] hover:border-white/[0.12] text-left transition-all duration-200 group`}
              >
                <div className={`w-8 h-8 rounded-lg bg-white/[0.06] flex items-center justify-center shrink-0 ${card.iconColor} group-hover:scale-110 transition-transform duration-200`}>
                  <Icon size={15} />
                </div>
                <div className="min-w-0">
                  <p className="text-[13px] font-medium text-white/80 group-hover:text-white transition-colors">{card.label}</p>
                  <p className="text-[11px] text-white/30 mt-0.5">{card.desc}</p>
                </div>
              </button>
            );
          })}
        </div>

        {/* Quick Features */}
        <div className="grid grid-cols-4 gap-2 mb-6 stagger-item">
          {[
            { icon: Zap, label: 'Fast', desc: 'Real-time streaming' },
            { icon: Brain, label: 'Memory', desc: 'Remembers context' },
            { icon: Code, label: 'Code', desc: 'Syntax highlighting' },
            { icon: Palette, label: 'Adaptive', desc: 'Personality modes' },
          ].map((feature, i) => (
            <div
              key={i}
              className="flex flex-col items-center gap-1.5 px-2 py-2.5 rounded-xl bg-white/[0.03] border border-white/[0.06] hover:bg-white/[0.06] transition-all duration-200"
            >
              <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-[#7c6cf0]/20 to-[#a78bfa]/10 flex items-center justify-center">
                <feature.icon size={13} className="text-[#a78bfa]" />
              </div>
              <p className="text-[11px] font-medium text-white/60">{feature.label}</p>
            </div>
          ))}
        </div>

        {/* Suggested Prompts */}
        <div className="stagger-item">
          <div className="flex items-center justify-center gap-2 mb-3">
            <Sparkles size={12} className="text-white/20" />
            <p className="text-[11px] font-medium text-white/20 uppercase tracking-wider">
              Suggested Prompts
            </p>
            <Sparkles size={12} className="text-white/20" />
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
            {suggestedPrompts.slice(0, 6).map((prompt) => (
              <SuggestedPromptCard key={prompt.id} prompt={prompt} />
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

function SuggestedPromptCard({ prompt }: { prompt: SuggestedPrompt }) {
  const { addMessage, startStreaming, updateStreaming, finishStreaming } = useChatStore();

  const handleClick = () => {
    const userMsg = {
      id: `msg-${Date.now()}`,
      role: 'user' as const,
      content: prompt.text,
      timestamp: Date.now(),
    };
    addMessage(userMsg);

    // Simulate streaming response
    startStreaming();
    const response = `That's a great question! Here's what I can tell you about "${prompt.text}"...\n\nI'm Shree AI, your autonomous agent designed to help you with a wide range of tasks. I can assist with:\n\n- **Research** and information gathering\n- **Code generation** and debugging\n- **Creative writing** and brainstorming\n- **Data analysis** and insights\n- **Problem solving** and strategic planning\n\nFeel free to ask me anything! I'm here to help.`;
    
    let idx = 0;
    const interval = setInterval(() => {
      idx += 3;
      if (idx >= response.length) {
        clearInterval(interval);
        updateStreaming(response);
        setTimeout(() => finishStreaming(), 100);
      } else {
        updateStreaming(response.slice(0, idx));
      }
    }, 30);
  };

  return (
    <button
      onClick={handleClick}
      className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl bg-white/[0.03] border border-white/[0.06] hover:bg-white/[0.06] hover:border-[#7c6cf0]/20 hover:shadow-sm hover:shadow-[#7c6cf0]/5 text-left transition-all duration-200 group"
    >
      <span className="text-base shrink-0">{CATEGORY_ICONS[prompt.category] || '💬'}</span>
      <span className="text-[13px] text-white/50 group-hover:text-white/70 leading-relaxed transition-colors flex-1">
        {prompt.text}
      </span>
      <ArrowRight size={14} className="text-white/10 group-hover:text-[#a78bfa] transition-all duration-200 shrink-0 opacity-0 group-hover:opacity-100 -translate-x-1 group-hover:translate-x-0" />
    </button>
  );
}