package com.darshan.agent.planning;

import java.time.LocalDate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlanMilestone {
    private String id;
    private String title;
    private String description;
    private int priority;
    private PlanStatus status;
    private LocalDate targetDate;
    private List<ExecutionTask> tasks;
    private Instant createdAt;

    public PlanMilestone() {
        this.id = UUID.randomUUID().toString();
        this.status = PlanStatus.NOT_STARTED;
        this.priority = 5;
        this.tasks = new ArrayList<>();
        this.createdAt = Instant.now();
    }

    public PlanMilestone(String title, String description, int priority, LocalDate targetDate) {
        this();
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.targetDate = targetDate;
    }

    public double getProgress() {
        if (tasks.isEmpty()) return 0;
        long done = tasks.stream().filter(ExecutionTask::isCompleted).count();
        return (done * 100.0) / tasks.size();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public PlanStatus getStatus() { return status; }
    public void setStatus(PlanStatus status) { this.status = status; }
    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
    public List<ExecutionTask> getTasks() { return tasks; }
    public void setTasks(List<ExecutionTask> tasks) { this.tasks = tasks; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}