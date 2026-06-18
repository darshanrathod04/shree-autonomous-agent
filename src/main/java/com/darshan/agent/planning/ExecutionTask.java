package com.darshan.agent.planning;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExecutionTask {
    public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }

    private String id;
    private String title;
    private String description;
    private Priority priority;
    private double estimatedHours;
    private PlanStatus status;
    private List<String> dependencies;
    private Instant createdAt;
    private Instant completedAt;

    public ExecutionTask() {
        this.id = UUID.randomUUID().toString();
        this.status = PlanStatus.NOT_STARTED;
        this.priority = Priority.MEDIUM;
        this.dependencies = new ArrayList<>();
        this.createdAt = Instant.now();
    }

    public ExecutionTask(String title, String description, Priority priority, double estimatedHours) {
        this();
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.estimatedHours = estimatedHours;
    }

    public boolean isBlocked() {
        return status == PlanStatus.BLOCKED;
    }

    public boolean isCompleted() {
        return status == PlanStatus.COMPLETED;
    }

    public void complete() {
        this.status = PlanStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public double getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(double estimatedHours) { this.estimatedHours = estimatedHours; }
    public PlanStatus getStatus() { return status; }
    public void setStatus(PlanStatus status) { this.status = status; }
    public List<String> getDependencies() { return dependencies; }
    public void setDependencies(List<String> dependencies) { this.dependencies = dependencies; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}