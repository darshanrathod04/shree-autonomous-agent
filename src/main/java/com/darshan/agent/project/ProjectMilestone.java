package com.darshan.agent.project;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class ProjectMilestone {
    private String id;
    private String title;
    private String description;
    private LocalDate targetDate;
    private boolean completed;
    private Instant completedAt;

    public ProjectMilestone() {
        this.id = UUID.randomUUID().toString();
    }

    public ProjectMilestone(String title) {
        this();
        this.title = title;
    }

    public ProjectMilestone(String title, LocalDate targetDate) {
        this(title);
        this.targetDate = targetDate;
    }

    public void complete() {
        this.completed = true;
        this.completedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}