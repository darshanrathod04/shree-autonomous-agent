 package com.darshan.agent.context;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a conversation session with its own context and message history.
 * Each session is isolated, enabling multi-user support.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConversationSession {
    
    private String sessionId;
    private String userId;
    private ConversationContext context;
    private LessonState lessonState;
    private String sessionUserName;
    private Instant createdAt;
    private Instant lastAccessedAt;
    private List<SessionMessage> messageHistory;
    
    // Default session timeout: 24 hours
    private static final Duration DEFAULT_TIMEOUT = Duration.ofHours(24);
    
    // Required for JSON deserialization
    public ConversationSession() {
        this.sessionId = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
        this.lastAccessedAt = Instant.now();
        this.context = new ConversationContext();
        this.lessonState = new LessonState();
        this.messageHistory = new ArrayList<>();
    }
    
    public ConversationSession(String userId) {
        this();
        this.userId = userId;
    }
    
    // Lifecycle methods
    public boolean isExpired() {
        return isExpired(DEFAULT_TIMEOUT);
    }
    
    public boolean isExpired(Duration timeout) {
        return Duration.between(lastAccessedAt, Instant.now()).compareTo(timeout) > 0;
    }
    
    public void touch() {
        this.lastAccessedAt = Instant.now();
    }
    
    public void addMessage(String role, String content) {
        messageHistory.add(new SessionMessage(role, content));
        touch();
        // Session owns messageHistory as the single source of truth for conversation history.
        // context.history is deprecated in favor of session messageHistory.
    }
    
    public void addMessage(String role, String content, String intent) {
        messageHistory.add(new SessionMessage(role, content, intent));
        touch();
        // Session owns messageHistory as the single source of truth for conversation history.
        // context.history is deprecated in favor of session messageHistory.
    }
    
    public String getSummary() {
        if (messageHistory.isEmpty()) {
            return "Empty session";
        }
        
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(10, messageHistory.size());
        
        for (int i = 0; i < limit; i++) {
            SessionMessage msg = messageHistory.get(i);
            sb.append(msg.getRole())
              .append(": ")
              .append(truncate(msg.getContent(), 50))
              .append("\n");
        }
        
        if (messageHistory.size() > 10) {
            sb.append("... (").append(messageHistory.size() - 10).append(" more messages)\n");
        }
        
        return sb.toString();
    }
    
    public String getFirstMessage() {
        if (messageHistory.isEmpty()) {
            return "Untitled";
        }
        return truncate(messageHistory.get(0).getContent(), 30);
    }
    
    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public ConversationContext getContext() {
        return context;
    }
    
    public void setContext(ConversationContext context) {
        this.context = context;
    }
    
    /**
     * Get the per-session lesson state.
     * Each session maintains its own lesson progress independently.
     */
    public LessonState getLessonState() {
        if (lessonState == null) {
            lessonState = new LessonState();
        }
        return lessonState;
    }
    
    public void setLessonState(LessonState lessonState) {
        this.lessonState = lessonState;
    }

    public String getSessionUserName() {
        return sessionUserName;
    }

    public void setSessionUserName(String sessionUserName) {
        this.sessionUserName = sessionUserName;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Instant getLastAccessedAt() {
        return lastAccessedAt;
    }
    
    public void setLastAccessedAt(Instant lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }
    
    public List<SessionMessage> getMessageHistory() {
        return messageHistory;
    }
    
    public void setMessageHistory(List<SessionMessage> messageHistory) {
        this.messageHistory = messageHistory;
    }
    
    public int getMessageCount() {
        return messageHistory != null ? messageHistory.size() : 0;
    }
    
    // Helper method
    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}