package com.darshan.agent.project;

import java.time.Instant;
import java.util.UUID;

public class ProjectDecision {
    private String id;
    private String decision;
    private String reason;
    private Instant timestamp;

    public ProjectDecision() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
    }

    public ProjectDecision(String decision, String reason) {
        this();
        this.decision = decision;
        this.reason = reason;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}