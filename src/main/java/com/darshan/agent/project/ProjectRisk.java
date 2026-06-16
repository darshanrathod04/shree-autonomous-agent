package com.darshan.agent.project;

import java.time.Instant;
import java.util.UUID;

public class ProjectRisk {
    public enum Severity { LOW, MEDIUM, HIGH }

    private String id;
    private String title;
    private String description;
    private Severity severity;
    private boolean resolved;
    private Instant createdAt;

    public ProjectRisk() {
        this.id = UUID.randomUUID().toString();
        this.severity = Severity.MEDIUM;
        this.createdAt = Instant.now();
    }

    public ProjectRisk(String title, Severity severity) {
        this();
        this.title = title;
        this.severity = severity;
    }

    public void resolve() {
        this.resolved = true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}