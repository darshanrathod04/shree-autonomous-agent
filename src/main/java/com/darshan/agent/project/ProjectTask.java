package com.darshan.agent.project;

import java.time.Instant;
import java.util.UUID;

public class ProjectTask {
    public enum Status { TODO, IN_PROGRESS, DONE }
    public enum Priority { LOW, MEDIUM, HIGH }

    private String id;
    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private Instant createdAt;
    private Instant completedAt;

    public ProjectTask() {
        this.id = UUID.randomUUID().toString();
        this.status = Status.TODO;
        this.priority = Priority.MEDIUM;
        this.createdAt = Instant.now();
    }

    public ProjectTask(String title) {
        this();
        this.title = title;
    }

    public ProjectTask(String title, String description, Priority priority) {
        this(title);
        this.description = description;
        this.priority = priority;
    }

    public void complete() {
        this.status = Status.DONE;
        this.completedAt = Instant.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}