package com.darshan.agent.context;

public class ConversationEntry {

    private final String role; // USER or AI
    private final String message;

    public ConversationEntry(String role, String message) {
        this.role = role;
        this.message = message;
    }

    public String getRole() {
        return role;
    }

    public String getMessage() {
        return message;
    }
}