import { useState } from 'react';
import ReactMarkdown from 'react-markdown';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { oneDark } from 'react-syntax-highlighter/dist/esm/styles/prism';
import { Copy, Check, RefreshCw, Trash2, User, Bot, Clock } from 'lucide-react';
import { useChatStore } from '@/shared/stores/chatStore';
import { useUIStore } from '@/shared/stores/uiStore';
import type { Message } from '@/shared/types';

const MODE_AVATARS: Record<string, { gradient: string; icon: string }> = {
  teacher: { gradient: 'from-blue-500 to-blue-400', icon: '📚' },
  coach: { gradient: 'from-amber-500 to-amber-400', icon: '🏆' },
  assistant: { gradient: 'from-[#7c6cf0] to-[#a78bfa]', icon: '🤖' },
  friend: { gradient: 'from-emerald-500 to-emerald-400', icon: '💚' },
};

interface MessageBubbleProps {
  message: Message;
  isStreaming?: boolean;
}

export function MessageBubble({ message, isStreaming }: MessageBubbleProps) {
  const { removeMessage, regenerateLastMessage, startStreaming, updateStreaming, finishStreaming } = useChatStore();
  const { personalityMode } = useUIStore();
  const [copied, setCopied] = useState<string | boolean>(false);

  const isUser = message.role === 'user';
  const isThinking = message.isThinking;

  const handleCopy = async (content: string) => {
    try {
      await navigator.clipboard.writeText(content);
      setCopied(content);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      const textarea = document.createElement('textarea');
      textarea.value = content;
      document.body.appendChild(textarea);
      textarea.select();
      document.execCommand('copy');
      document.body.removeChild(textarea);
      setCopied(content);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const handleRegenerate = () => {
    const lastUserMsg = regenerateLastMessage();
    if (lastUserMsg) {
      startStreaming();
      const response = `Here's a regenerated response to "${lastUserMsg.slice(0, 50)}..."\n\nI've thought about this differently and here's what I came up with:\n\nThis is a fresh perspective on your question. Let me know if you'd like me to elaborate further!`;
      let idx = 0;
      const interval = setInterval(() => {
        idx += 4;
        if (idx >= response.length) {
          clearInterval(interval);
          updateStreaming(response);
          setTimeout(() => finishStreaming(), 100);
        } else {
          updateStreaming(response.slice(0, idx));
        }
      }, 25);
    }
  };

  const handleDelete = () => {
    removeMessage(message.id);
  };

  const avatarConfig = !isUser ? MODE_AVATARS[personalityMode] || MODE_AVATARS.assistant : null;

  return (
    <div className={`flex gap-3 md:gap-4 px-4 md:px-0 message-enter ${isUser ? 'flex-row-reverse' : ''}`}>
      {/* Avatar */}
      <div className={`shrink-0 mt-0.5 ${isUser ? 'order-2' : ''}`}>
        {isUser ? (
          <div className="w-8 h-8 rounded-xl bg-gradient-to-br from-[#7c6cf0]/30 to-[#a78bfa]/20 border border-[#7c6cf0]/20 flex items-center justify-center">
            <User size={14} className="text-[#a78bfa]" />
          </div>
        ) : (
          <div className={`w-8 h-8 rounded-xl bg-gradient-to-br ${avatarConfig?.gradient || 'from-[#7c6cf0] to-[#a78bfa]'} flex items-center justify-center shadow-sm`}>
            <span className="text-xs">{avatarConfig?.icon || '🤖'}</span>
          </div>
        )}
      </div>

      {/* Message Content */}
      <div className={`flex flex-col min-w-0 max-w-[85%] md:max-w-[70%] ${isUser ? 'items-end order-1' : 'items-start'}`}>
        {/* Label */}
        {!isUser && !isStreaming && !isThinking && (
          <div className="flex items-center gap-2 mb-1 ml-1">
            <span className="text-[11px] font-medium text-white/30">{personalityMode.charAt(0).toUpperCase() + personalityMode.slice(1)}</span>
          </div>
        )}

        {/* Thinking state */}
        {isThinking && !isUser && (
          <div className="relative w-full rounded-2xl px-4 py-3 bg-white/[0.03] border border-white/[0.06]">
            <div className="flex items-center gap-2.5">
              <div className="flex items-center gap-1">
                <span className="thinking-dot" />
                <span className="thinking-dot" />
                <span className="thinking-dot" />
              </div>
              <span className="text-[12px] text-white/30 font-medium animate-pulse">Thinking</span>
            </div>
          </div>
        )}

        {/* Bubble */}
        <div
          className={`relative w-full rounded-2xl px-4 py-3 ${
            isUser
              ? 'bg-gradient-to-br from-[#7c6cf0]/15 to-[#a78bfa]/10 border border-[#7c6cf0]/15'
              : isStreaming
                ? 'bg-white/[0.02] border border-white/[0.06]'
                : 'bg-transparent'
          }`}
        >
          {isUser ? (
            <p className="text-[14px] text-white/85 leading-relaxed whitespace-pre-wrap">
              {message.content}
            </p>
          ) : (
            <div className="markdown-body text-[14px] text-white/85 leading-relaxed">
              <ReactMarkdown
                components={{
                  code({ className, children, ...props }) {
                    const match = /language-(\w+)/.exec(className || '');
                    const codeString = String(children).replace(/\n$/, '');
                    
                    if (match) {
                      return (
                        <div className="relative group my-3">
                          <div className="flex items-center justify-between px-4 py-1.5 bg-black/40 rounded-t-lg border-b border-white/[0.06]">
                            <span className="text-[11px] text-white/30 font-mono">{match[1]}</span>
                            <button
                              onClick={() => handleCopy(codeString)}
                              className="flex items-center gap-1 text-[11px] text-white/30 hover:text-white/60 transition-colors"
                            >
                              {copied === codeString ? (
                                <Check size={12} className="text-emerald-400" />
                              ) : (
                                <Copy size={12} />
                              )}
                              <span>{copied === codeString ? 'Copied!' : 'Copy'}</span>
                            </button>
                          </div>
                          <SyntaxHighlighter
                            style={oneDark}
                            language={match[1]}
                            PreTag="div"
                            customStyle={{
                              margin: 0,
                              borderRadius: '0 0 0.75rem 0.75rem',
                              padding: '1rem',
                              background: 'rgba(0,0,0,0.3)',
                              fontSize: '13px',
                            }}
                          >
                            {codeString}
                          </SyntaxHighlighter>
                        </div>
                      );
                    }

                    return (
                      <code className="bg-white/[0.08] text-[#a78bfa] px-1.5 py-0.5 rounded text-[13px]" {...props}>
                        {children}
                      </code>
                    );
                  },
                  p({ children }) {
                    return <p className="mb-2 last:mb-0">{children}</p>;
                  },
                }}
              >
                {message.content}
              </ReactMarkdown>
            </div>
          )}
        </div>

        {/* Streaming indicator */}
        {isStreaming && (
          <div className="flex items-center gap-1.5 mt-2 ml-1">
            <span className="typing-dot" />
            <span className="typing-dot" />
            <span className="typing-dot" />
            <span className="text-[11px] text-white/20 ml-1 font-medium">Generating...</span>
          </div>
        )}

        {/* Message Actions */}
        {!isUser && !isStreaming && !isThinking && (
          <div className="flex items-center gap-1 mt-1.5 ml-1">
            <button
              onClick={() => handleCopy(message.content)}
              className="p-1.5 rounded-lg hover:bg-white/[0.06] text-white/20 hover:text-white/50 transition-all duration-200"
              title="Copy"
            >
              {copied === message.content ? <Check size={13} className="text-emerald-400" /> : <Copy size={13} />}
            </button>
            <button
              onClick={handleRegenerate}
              className="p-1.5 rounded-lg hover:bg-white/[0.06] text-white/20 hover:text-white/50 transition-all duration-200"
              title="Regenerate"
            >
              <RefreshCw size={13} />
            </button>
            <button
              onClick={handleDelete}
              className="p-1.5 rounded-lg hover:bg-white/[0.06] text-white/20 hover:text-red-400/60 transition-all duration-200"
              title="Delete"
            >
              <Trash2 size={13} />
            </button>
          </div>
        )}
      </div>
    </div>
  );
}