import { useState, useRef, useEffect } from 'react';
import { useChatStore } from '@/stores/chatStore';
import { useSessionStore } from '@/stores/sessionStore';
import { sendMessage, fetchSessionMessages } from '@/services/dashboard';
import { Send, Menu, Loader2 } from 'lucide-react';
import { useUIStore } from '@/stores/uiStore';
import { MessageBubble } from './MessageBubble';

interface ChatAreaProps {
  sessionId: string | null;
}

export function ChatArea({ sessionId }: ChatAreaProps) {
  const [input, setInput] = useState('');
  const { messages, isLoading, error, addMessage, setLoading, setError, clearMessages } = useChatStore();
  const { setActiveSession } = useSessionStore();
  const { toggleSidebar } = useUIStore();
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLTextAreaElement>(null);

  // Load messages when session changes
  useEffect(() => {
    if (sessionId) {
      loadMessages(sessionId);
    } else {
      clearMessages();
    }
  }, [sessionId]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const loadMessages = async (id: string) => {
    setLoading(true);
    try {
      const data = await fetchSessionMessages(id);
      if (data?.messages) {
        clearMessages();
        data.messages.forEach((msg: any) => {
          addMessage({
            id: msg.id || crypto.randomUUID(),
            role: msg.role || 'assistant',
            content: msg.content || msg.text || '',
            timestamp: new Date(msg.timestamp || Date.now()),
          });
        });
      }
    } catch (err) {
      console.error('Failed to load messages:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const text = input.trim();
    if (!text || isLoading) return;

    setInput('');
    setError(null);

    // Add user message
    addMessage({
      id: crypto.randomUUID(),
      role: 'user',
      content: text,
      timestamp: new Date(),
    });

    setLoading(true);

    try {
      const response = await sendMessage(text, sessionId);
      if (response.sessionId) {
        setActiveSession(response.sessionId);
      }
      addMessage({
        id: crypto.randomUUID(),
        role: 'assistant',
        content: response.suggestion,
        timestamp: new Date(),
      });
    } catch (err: any) {
      const msg = err.response?.data?.error || err.message || 'Failed to send message';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit(e);
    }
  };

  return (
    <div className="flex-1 flex flex-col h-full">
      {/* Header */}
      <header className="flex items-center gap-3 px-4 py-3 border-b border-[var(--border-color)] bg-[var(--bg-primary)]">
        <button
          onClick={toggleSidebar}
          className="p-1.5 rounded-lg hover:bg-[var(--bg-tertiary)] transition-colors lg:hidden"
        >
          <Menu size={20} />
        </button>
        <div>
          <h2 className="text-sm font-semibold">AI Agent Chat</h2>
          <p className="text-xs text-[var(--text-secondary)]">
            {sessionId ? 'Active session' : 'New conversation'}
          </p>
        </div>
      </header>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto px-4 py-6">
        {messages.length === 0 && !isLoading && (
          <div className="flex flex-col items-center justify-center h-full text-center">
            <div className="w-16 h-16 rounded-2xl bg-[var(--accent)]/10 flex items-center justify-center mb-4">
              <svg className="w-8 h-8 text-[var(--accent)]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
              </svg>
            </div>
            <h3 className="text-lg font-semibold mb-2">Start a conversation</h3>
            <p className="text-sm text-[var(--text-secondary)] max-w-md">
              Ask me anything! I can help with learning, problem-solving, and more.
            </p>
          </div>
        )}

        {messages.map((msg) => (
          <MessageBubble key={msg.id} message={msg} />
        ))}

        {isLoading && (
          <div className="flex items-start gap-3 mb-4 animate-fade-in">
            <div className="w-8 h-8 rounded-lg bg-[var(--accent)]/10 flex items-center justify-center shrink-0">
              <Loader2 size={16} className="text-[var(--accent)] animate-spin" />
            </div>
            <div className="flex items-center gap-1.5 py-2">
              <span className="typing-dot" />
              <span className="typing-dot" />
              <span className="typing-dot" />
            </div>
          </div>
        )}

        {error && (
          <div className="flex items-center gap-2 p-3 rounded-lg bg-[var(--error)]/10 text-[var(--error)] text-sm mb-4">
            <span>{error}</span>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* Input */}
      <div className="px-4 py-3 border-t border-[var(--border-color)] bg-[var(--bg-primary)]">
        <form onSubmit={handleSubmit} className="flex items-end gap-2">
          <div className="flex-1 relative">
            <textarea
              ref={inputRef}
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Type your message..."
              rows={1}
              className="w-full px-4 py-3 rounded-xl bg-[var(--bg-secondary)] border border-[var(--border-color)] text-sm resize-none focus:outline-none focus:border-[var(--accent)] transition-colors"
              style={{ minHeight: '44px', maxHeight: '120px' }}
              disabled={isLoading}
            />
          </div>
          <button
            type="submit"
            disabled={!input.trim() || isLoading}
            className="p-3 rounded-xl bg-[var(--accent)] text-white hover:bg-[var(--accent-hover)] disabled:opacity-50 disabled:cursor-not-allowed transition-colors shrink-0"
          >
            <Send size={18} />
          </button>
        </form>
      </div>
    </div>
  );
}