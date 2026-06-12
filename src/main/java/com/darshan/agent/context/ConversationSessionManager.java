package com.darshan.agent.context;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages conversation sessions lifecycle.
 * Provides session creation, retrieval, and cleanup.
 */
@Component
public class ConversationSessionManager {
    
    private final SessionRepository repository;
    
    // Fallback to original ContextStore for backward compatibility
    private final ContextStore fallbackContextStore;
    
    public ConversationSessionManager(
            SessionRepository repository,
            ContextStore fallbackContextStore) {
        this.repository = repository;
        this.fallbackContextStore = fallbackContextStore;
    }
    
    /**
     * Create a new conversation session.
     * @return The created session with a new UUID
     */
    public ConversationSession createSession() {
        return createSession(null);
    }
    
    /**
     * Create a new conversation session for a specific user.
     * @param userId Optional user identifier
     * @return The created session with a new UUID
     */
    public ConversationSession createSession(String userId) {
        ConversationSession session = new ConversationSession(userId);
        repository.save(session);
        System.out.println("🆕 Session created: " + session.getSessionId());
        return session;
    }
    
    /**
     * Get or create a session by ID.
     * If sessionId is null or not found, creates a new session.
     * @param sessionId Optional session ID
     * @return An existing or new session
     */
    public ConversationSession getOrCreateSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return createSession();
        }
        
        Optional<ConversationSession> existing = repository.findById(sessionId);
        if (existing.isPresent()) {
            System.out.println("📂 Session loaded: " + sessionId);
            return existing.get();
        }
        
        System.out.println("⚠️ Session not found, creating new: " + sessionId);
        return createSession();
    }
    
    /**
     * Get a session by ID.
     * @param sessionId The session ID
     * @return Optional containing the session if found
     */
    public Optional<ConversationSession> getSession(String sessionId) {
        return repository.findById(sessionId);
    }
    
    /**
     * Save changes to a session.
     * Should be called after modifying a session's context or message history.
     * @param session The session to save
     */
    public void saveSession(ConversationSession session) {
        if (session != null) {
            session.touch();
            repository.save(session);
        }
    }
    
    /**
     * End a session (delete it).
     * @param sessionId The session ID to end
     * @return true if session was deleted
     */
    public boolean endSession(String sessionId) {
        System.out.println("🔚 Session ended: " + sessionId);
        return repository.delete(sessionId);
    }
    
    /**
     * List all active sessions.
     * @return List of active sessions, sorted by most recent
     */
    public List<ConversationSession> listActiveSessions() {
        return repository.listActiveSessions();
    }
    
    /**
     * List sessions for a specific user.
     * @param userId The user ID
     * @return List of sessions for that user
     */
    public List<ConversationSession> listSessionsByUser(String userId) {
        return repository.listByUser(userId);
    }
    
    /**
     * Get the number of active sessions.
     * @return Count of active sessions
     */
    public int getActiveSessionCount() {
        return repository.getActiveSessionCount();
    }
    
    /**
     * Get the original fallback context (for backward compatibility).
     * This allows gradual migration without breaking existing code.
     * @deprecated Use session-based methods instead
     */
    @Deprecated
    public ConversationContext getFallbackContext() {
        return fallbackContextStore.getContext();
    }
    
    /**
     * Clean up expired sessions.
     * Runs periodically to remove stale sessions.
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    public int cleanupExpiredSessions() {
        int count = repository.cleanupExpiredSessions();
        if (count > 0) {
            System.out.println("🧹 Cleaned up " + count + " expired sessions");
        }
        return count;
    }
    
    /**
     * Add a message to a session and persist it.
     * @param session The session
     * @param role "USER" or "AI"
     * @param content The message content
     */
    public void addMessage(ConversationSession session, String role, String content) {
        session.addMessage(role, content);
        repository.save(session);
    }
    
    /**
     * Add a message with intent to a session and persist it.
     * @param session The session
     * @param role "USER" or "AI"
     * @param content The message content
     * @param intent The detected intent
     */
    public void addMessage(ConversationSession session, String role, String content, String intent) {
        session.addMessage(role, content, intent);
        repository.save(session);
    }
}