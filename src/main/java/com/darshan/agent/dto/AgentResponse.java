package com.darshan.agent.dto;

public class AgentResponse {

    private String suggestion;
    private boolean approvalRequired;

    public AgentResponse(String suggestion, boolean approvalRequired) {
        this.suggestion = suggestion;
        this.approvalRequired = approvalRequired;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public boolean isApprovalRequired() {
        return approvalRequired;
    }
}
