import { useEffect, useRef } from 'react';
import { useChatStore } from '@/shared/stores/chatStore';
import { useSessionStore } from '@/shared/stores/sessionStore';
import { chatApi } from '@/shared/services/chatApi';
import { MessageBubble } from './MessageBubble';
import { ChatInput } from './ChatInput';
import { WelcomeScreen } from './WelcomeScreen';
import type { Message } from '@/shared/types';

interface ChatAreaProps {
  sessionId: string | null;
}

export function ChatArea({ sessionId }: ChatAreaProps) {
  const { messages, isStreaming, streamingContent, setMessages } = useChatStore();
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const loadedSessionRef = useRef<string | null>(null);

  // Load messages when session changes
  useEffect(() => {
    if (!sessionId) {
      setMessages([]);
      loadedSessionRef.current = null;
      return;
    }

    // Skip reload if we already loaded this session's messages
    // This prevents duplication when ChatInput adds a message locally
    // and then this effect tries to load the same messages from the server
    if (loadedSessionRef.current === sessionId) {
      return;
    }

    const loadMessages = async () => {
      try {
        const sessionMessages = await chatApi.getSessionMessages(sessionId);
        const mapped: Message[] = sessionMessages.map((msg, idx) => ({
          id: `msg-${sessionId}-${idx}`,
          role: msg.role === 'USER' ? 'user' : 'assistant',
          content: msg.content,
          timestamp: new Date(msg.timestamp).getTime(),
        }));
        loadedSessionRef.current = sessionId;
        setMessages(mapped);
      } catch (err) {
        console.error('[ChatArea] Failed to load session messages:', err);
        setMessages([]);
      }
    };

    loadMessages();
  }, [sessionId, setMessages]);

  // Auto-scroll to bottom when new messages arrive or during streaming
  useEffect(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: 'smooth', block: 'end' });
    }
  }, [messages, streamingContent]);

  const hasMessages = messages.length > 0;

  return (
    <div className="flex-1 flex flex-col min-h-0">
      {/* Messages Area */}
      <div className="flex-1 overflow-y-auto scroll-smooth">
        {!hasMessages ? (
          <WelcomeScreen />
        ) : (
          <div className="max-w-3xl mx-auto py-6 md:py-8 space-y-6">
            {messages.map((message) => (
              <MessageBubble key={message.id} message={message} />
            ))}

            {/* Streaming message */}
            {isStreaming && streamingContent && (
              <MessageBubble
                message={{
                  id: 'streaming',
                  role: 'assistant',
                  content: streamingContent,
                  timestamp: Date.now(),
                  isStreaming: true,
                }}
                isStreaming={true}
              />
            )}

            <div ref={messagesEndRef} />
          </div>
        )}
      </div>

      {/* Input Area */}
      <ChatInput />
    </div>
  );
}