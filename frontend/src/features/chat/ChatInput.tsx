import { useState, useRef, useEffect } from 'react';
import { ArrowUp, Square, Sparkles } from 'lucide-react';
import { useChatStore } from '@/shared/stores/chatStore';
import { useSessionStore } from '@/shared/stores/sessionStore';
import { chatApi } from '@/shared/services/chatApi';

export function ChatInput() {
  const [input, setInput] = useState('');
  const [sending, setSending] = useState(false);
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const { addMessage, isStreaming, startStreaming, updateStreaming, finishStreaming } = useChatStore();
  const { activeSessionId, setActiveSession } = useSessionStore();

  useEffect(() => {
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
      const newHeight = Math.min(textareaRef.current.scrollHeight, 200);
      textareaRef.current.style.height = `${newHeight}px`;
    }
  }, [input]);

  const handleSubmit = async () => {
    const trimmed = input.trim();
    if (!trimmed || isStreaming || sending) return;

    const userMsg = {
      id: `msg-${Date.now()}`,
      role: 'user' as const,
      content: trimmed,
      timestamp: Date.now(),
    };
    addMessage(userMsg);
    setInput('');
    setSending(true);

    // Start streaming
    startStreaming();

    try {
      // Use fetch directly for streaming-like experience
      const response = await chatApi.send({
        message: trimmed,
        sessionId: activeSessionId,
      });

      // Update session ID for continuity
      if (response.sessionId && response.sessionId !== activeSessionId) {
        setActiveSession(response.sessionId);

        // Add new session to sidebar if it's a new session
        if (!activeSessionId) {
          const { addSession } = useSessionStore.getState();
          addSession({
            id: response.sessionId,
            title: trimmed.slice(0, 30) + (trimmed.length > 30 ? '...' : ''),
            messages: [],
            createdAt: Date.now(),
            updatedAt: Date.now(),
          });
        }
      }

      // Simulate character-by-character streaming of the real response
      const realResponse = response.suggestion || 'I processed your request.';
      let idx = 0;
      const charInterval = setInterval(() => {
        idx += 2;
        if (idx >= realResponse.length) {
          clearInterval(charInterval);
          updateStreaming(realResponse);
          setTimeout(() => finishStreaming(), 50);
        } else {
          updateStreaming(realResponse.slice(0, idx));
        }
      }, 15);
    } catch (err) {
      console.error('[Chat] API error:', err);
      finishStreaming();
      
      // Add error message
      addMessage({
        id: `msg-${Date.now()}`,
        role: 'assistant',
        content: "⚠️ I couldn't reach the backend. Please ensure the Shree AI server is running on port 8080.",
        timestamp: Date.now(),
      });
    } finally {
      setSending(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit();
    }
  };

  const handleStopStreaming = () => {
    finishStreaming();
    setSending(false);
  };

  return (
    <div className="px-4 pb-4 pt-2">
      <div className="max-w-3xl mx-auto">
        <div className="relative group">
          <div className="relative rounded-2xl border border-white/[0.08] bg-[#121218] hover:border-white/[0.15] focus-within:border-[#7c6cf0]/30 focus-within:ring-1 focus-within:ring-[#7c6cf0]/20 transition-all duration-200 shadow-lg shadow-black/20">
            <textarea
              ref={textareaRef}
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Message Shree AI..."
              rows={1}
              className="w-full bg-transparent text-white/85 text-[14px] placeholder:text-white/20 resize-none outline-none py-3.5 px-4 pr-14 max-h-[200px] leading-relaxed"
              disabled={isStreaming}
            />
            <div className="absolute bottom-2 right-2">
              {isStreaming ? (
                <button
                  onClick={handleStopStreaming}
                  className="w-9 h-9 rounded-xl bg-white/10 hover:bg-white/15 flex items-center justify-center text-white/60 hover:text-white transition-all duration-200"
                  title="Stop generating"
                >
                  <Square size={14} />
                </button>
              ) : (
                <button
                  onClick={handleSubmit}
                  disabled={!input.trim() || sending}
                  className={`w-9 h-9 rounded-xl flex items-center justify-center transition-all duration-200 ${
                    input.trim() && !sending
                      ? 'bg-gradient-to-r from-[#7c6cf0] to-[#a78bfa] text-white shadow-sm shadow-[#7c6cf0]/30 hover:shadow-md hover:shadow-[#7c6cf0]/40 hover:scale-105'
                      : 'bg-white/5 text-white/20 cursor-not-allowed'
                  }`}
                  title="Send message"
                >
                  <ArrowUp size={16} />
                </button>
              )}
            </div>
          </div>
          <div className="flex items-center justify-center gap-1.5 mt-2">
            <Sparkles size={10} className="text-white/15" />
            <p className="text-[11px] text-white/15 text-center">
              Shree AI can make mistakes. Verify important information.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}