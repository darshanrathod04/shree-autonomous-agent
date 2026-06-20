package com.darshan.agent.planning;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecutionPlan {
    private String id;
    private String goalId;
    private String goalName;
    private PlanStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private List<PlanMilestone> milestones;

    public ExecutionPlan() {
        this.id = UUID.randomUUID().toString();
        this.status = PlanStatus.NOT_STARTED;
        this.milestones = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public ExecutionPlan(String goalId, String goalName) {
        this();
        this.goalId = goalId;
        this.goalName = goalName;
    }

    public double getOverallProgress() {
        if (milestones.isEmpty()) return 0;
        double total = milestones.stream().mapToDouble(PlanMilestone::getProgress).sum();
        return total / milestones.size();
    }

    public long getTotalTasks() {
        return milestones.stream()
                .flatMap(m -> m.getTasks().stream())
                .count();
    }

    public long getCompletedTasks() {
        return milestones.stream()
                .flatMap(m -> m.getTasks().stream())
                .filter(ExecutionTask::isCompleted)
                .count();
    }

    public List<ExecutionTask> getAllTasks() {
        List<ExecutionTask> all = new ArrayList<>();
        for (PlanMilestone m : milestones) {
            all.addAll(m.getTasks());
        }
        return all;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getGoalId() { return goalId; }
    public void setGoalId(String goalId) { this.goalId = goalId; }
    public String getGoalName() { return goalName; }
    public void setGoalName(String goalName) { this.goalName = goalName; }
    public PlanStatus getStatus() { return status; }
    public void setStatus(PlanStatus status) { this.status = status; this.updatedAt = Instant.now(); }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public List<PlanMilestone> getMilestones() { return milestones; }
    public void setMilestones(List<PlanMilestone> milestones) { this.milestones = milestones; }
}