package com.darshan.agent.dto;

public class AgentRequest {

    private String message;
    private String sessionId;  // Optional: for conversation continuity

    // ✅ REQUIRED by Spring
    public AgentRequest() {}

    public AgentRequest(String message) {
        this.message = message;
    }

    public AgentRequest(String message, String sessionId) {
        this.message = message;
        this.sessionId = sessionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
