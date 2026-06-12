package com.darshan.agent.dto;

public class AgentRequest {

    private String message;

    // ✅ REQUIRED by Spring
    public AgentRequest() {}

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
