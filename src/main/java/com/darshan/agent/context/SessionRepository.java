package com.darshan.agent.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository for persistent storage of conversation sessions.
 * Uses JSON file storage in the sessions/ directory.
 */
@Component
public class SessionRepository {
    
    private static final String SESSIONS_DIR = "sessions";
    private static final String EXTENSION = ".json";
    
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, ConversationSession> sessionCache;
    
    public SessionRepository() {
        this.objectMapper = new ObjectMapper();
        // Register JSR310 module for Java 8 date/time serialization support
        this.objectMapper.registerModule(new JavaTimeModule());
        this.sessionCache = new ConcurrentHashMap<>();
        
        // Ensure sessions directory exists
        File dir = new File(SESSIONS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    /**
     * Save a session to persistent storage.
     */
    public synchronized void save(ConversationSession session) {
        if (session == null || session.getSessionId() == null) {
            throw new IllegalArgumentException("Session and sessionId cannot be null");
        }
        
        try {
            File file = new File(SESSIONS_DIR, session.getSessionId() + EXTENSION);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, session);
            
            // Update cache
            sessionCache.put(session.getSessionId(), session);
            
        } catch (IOException e) {
            System.err.println("Failed to save session: " + session.getSessionId());
            e.printStackTrace();
        }
    }
    
    /**
     * Load a session from persistent storage.
     */
    public Optional<ConversationSession> findById(String sessionId) {
        if (sessionId == null) {
            return Optional.empty();
        }
        
        // Check cache first
        if (sessionCache.containsKey(sessionId)) {
            ConversationSession session = sessionCache.get(sessionId);
            session.touch();
            return Optional.of(session);
        }
        
        // Load from file
        try {
            File file = new File(SESSIONS_DIR, sessionId + EXTENSION);
            if (file.exists()) {
                ConversationSession session = objectMapper.readValue(file, ConversationSession.class);
                session.touch();
                
                // Update cache
                sessionCache.put(sessionId, session);
                
                return Optional.of(session);
            }
        } catch (IOException e) {
            System.err.println("Failed to load session: " + sessionId);
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    /**
     * Delete a session from persistent storage.
     */
    public synchronized boolean delete(String sessionId) {
        if (sessionId == null) {
            return false;
        }
        
        // Remove from cache
        sessionCache.remove(sessionId);
        
        // Delete file
        File file = new File(SESSIONS_DIR, sessionId + EXTENSION);
        if (file.exists()) {
            return file.delete();
        }
        
        return true;
    }
    
    /**
     * List all active (non-expired) sessions.
     */
    public List<ConversationSession> listActiveSessions() {
        List<ConversationSession> active = new ArrayList<>();
        
        for (ConversationSession session : sessionCache.values()) {
            if (!session.isExpired()) {
                active.add(session);
            }
        }
        
        // Also check file system for sessions not in cache
        File dir = new File(SESSIONS_DIR);
        File[] files = dir.listFiles((d, name) -> name.endsWith(EXTENSION));
        
        if (files != null) {
            for (File file : files) {
                String sessionId = file.getName().replace(EXTENSION, "");
                if (!sessionCache.containsKey(sessionId)) {
                    findById(sessionId).ifPresent(session -> {
                        if (!session.isExpired()) {
                            active.add(session);
                        } else {
                            // Clean up expired session
                            delete(sessionId);
                        }
                    });
                }
            }
        }
        
        // Sort by last accessed (most recent first)
        active.sort((a, b) -> b.getLastAccessedAt().compareTo(a.getLastAccessedAt()));
        
        return active;
    }
    
    /**
     * List sessions for a specific user.
     */
    public List<ConversationSession> listByUser(String userId) {
        List<ConversationSession> userSessions = new ArrayList<>();
        
        for (ConversationSession session : sessionCache.values()) {
            if (userId.equals(session.getUserId()) && !session.isExpired()) {
                userSessions.add(session);
            }
        }
        
        userSessions.sort((a, b) -> b.getLastAccessedAt().compareTo(a.getLastAccessedAt()));
        
        return userSessions;
    }
    
    /**
     * Clean up expired sessions.
     */
    public int cleanupExpiredSessions() {
        int count = 0;
        List<String> toRemove = new ArrayList<>();
        
        for (ConversationSession session : sessionCache.values()) {
            if (session.isExpired()) {
                toRemove.add(session.getSessionId());
            }
        }
        
        for (String sessionId : toRemove) {
            delete(sessionId);
            count++;
        }
        
        return count;
    }
    
    /**
     * Get the number of active sessions.
     */
    public int getActiveSessionCount() {
        return (int) sessionCache.values().stream()
                .filter(s -> !s.isExpired())
                .count();
    }
    
    /**
     * Clear all sessions (use with caution).
     */
    public synchronized void clearAll() {
        sessionCache.clear();
        
        File dir = new File(SESSIONS_DIR);
        if (dir.exists()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(EXTENSION));
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        }
    }
}