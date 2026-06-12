package com.darshan.agent.dto;

public class AgentResponse {

    private String suggestion;
    private boolean approvalRequired;
    private String sessionId;  // For conversation continuity

    public AgentResponse(String suggestion, boolean approvalRequired) {
        this.suggestion = suggestion;
        this.approvalRequired = approvalRequired;
    }

    public AgentResponse(String suggestion, boolean approvalRequired, String sessionId) {
        this.suggestion = suggestion;
        this.approvalRequired = approvalRequired;
        this.sessionId = sessionId;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public boolean isApprovalRequired() {
        return approvalRequired;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
