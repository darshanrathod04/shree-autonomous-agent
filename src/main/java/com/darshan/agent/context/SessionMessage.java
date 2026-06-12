package com.darshan.agent.context;

import java.time.Instant;

/**
 * Represents a single message in a conversation session.
 * Used for persistent storage and session history.
 */
public class SessionMessage {
    
    private String role;        // "USER" or "AI"
    private String content;
    private Instant timestamp;
    private String intent;      // Optional: detected intent
    
    // Required for JSON deserialization
    public SessionMessage() {
        this.timestamp = Instant.now();
    }
    
    public SessionMessage(String role, String content) {
        this.role = role;
        this.content = content;
        this.timestamp = Instant.now();
    }
    
    public SessionMessage(String role, String content, String intent) {
        this.role = role;
        this.content = content;
        this.intent = intent;
        this.timestamp = Instant.now();
    }
    
    // Getters and Setters
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getIntent() {
        return intent;
    }
    
    public void setIntent(String intent) {
        this.intent = intent;
    }
}