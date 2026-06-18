package com.darshan.agent.chief;

import java.time.Instant;
import java.util.UUID;

public class ChiefInsight {
    public enum Type {
        PROJECT_RISK,
        PROJECT_STAGNATION,
        LEARNING_GAP,
        GOAL_DELAY,
        MILESTONE_DUE,
        TASK_OVERLOAD,
        POSITIVE_PROGRESS,
        DECISION_WARNING
    }

    public enum Severity { LOW, MEDIUM, HIGH, CRITICAL }

    private String id;
    private Type type;
    private Severity severity;
    private int priorityScore;
    private String message;
    private String recommendation;
    private boolean resolved;
    private Instant createdAt;

    public ChiefInsight() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
        this.resolved = false;
    }

    public ChiefInsight(Type type, Severity severity, int priorityScore, String message, String recommendation) {
        this();
        this.type = type;
        this.severity = severity;
        this.priorityScore = priorityScore;
        this.message = message;
        this.recommendation = recommendation;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public int getPriorityScore() { return priorityScore; }
    public void setPriorityScore(int priorityScore) { this.priorityScore = priorityScore; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}