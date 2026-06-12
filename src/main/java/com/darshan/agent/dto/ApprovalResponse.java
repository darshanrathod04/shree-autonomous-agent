package com.darshan.agent.dto;

public class ApprovalResponse {

    private String status;
    private String message;

    public ApprovalResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
