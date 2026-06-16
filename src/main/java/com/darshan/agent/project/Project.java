package com.darshan.agent.project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {
    private String id;
    private String name;
    private String description;
    private ProjectStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private List<ProjectTask> tasks;
    private List<ProjectMilestone> milestones;
    private List<ProjectDecision> decisions;
    private List<ProjectRisk> risks;

    public Project() {
        this.id = UUID.randomUUID().toString();
        this.status = ProjectStatus.PLANNING;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.tasks = new ArrayList<>();
        this.milestones = new ArrayList<>();
        this.decisions = new ArrayList<>();
        this.risks = new ArrayList<>();
    }

    public Project(String name) {
        this();
        this.name = name;
    }

    public Project(String name, String description) {
        this(name);
        this.description = description;
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }

    public double getProgressPercentage() {
        if (tasks.isEmpty()) return 0;
        double total = 0;
        for (ProjectTask task : tasks) {
            switch (task.getStatus()) {
                case DONE -> total += 100;
                case IN_PROGRESS -> total += 50;
                case TODO -> total += 0;
            }
        }
        return Math.round(total / tasks.size());
    }

    public long getDoneTaskCount() {
        return tasks.stream().filter(t -> t.getStatus() == ProjectTask.Status.DONE).count();
    }

    public long getInProgressTaskCount() {
        return tasks.stream().filter(t -> t.getStatus() == ProjectTask.Status.IN_PROGRESS).count();
    }

    public long getTodoTaskCount() {
        return tasks.stream().filter(t -> t.getStatus() == ProjectTask.Status.TODO).count();
    }

    public long getUnresolvedRiskCount() {
        return risks.stream().filter(r -> !r.isResolved()).count();
    }

    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(" (").append((int) getProgressPercentage()).append("%)");
        sb.append(" [").append(status.name()).append("]");
        sb.append(" | Tasks: ").append(getDoneTaskCount()).append("/").append(tasks.size()).append(" done");
        if (getUnresolvedRiskCount() > 0) {
            sb.append(" | Risks: ").append(getUnresolvedRiskCount()).append(" unresolved");
        }
        return sb.toString();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; touch(); }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public List<ProjectTask> getTasks() { return tasks; }
    public void setTasks(List<ProjectTask> tasks) { this.tasks = tasks; }
    public List<ProjectMilestone> getMilestones() { return milestones; }
    public void setMilestones(List<ProjectMilestone> milestones) { this.milestones = milestones; }
    public List<ProjectDecision> getDecisions() { return decisions; }
    public void setDecisions(List<ProjectDecision> decisions) { this.decisions = decisions; }
    public List<ProjectRisk> getRisks() { return risks; }
    public void setRisks(List<ProjectRisk> risks) { this.risks = risks; }
}