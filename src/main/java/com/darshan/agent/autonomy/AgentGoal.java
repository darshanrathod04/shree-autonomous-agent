package com.darshan.agent.autonomy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AgentGoal {

    private String description;
    private List<SubGoal> subGoals = new ArrayList<>();
    private boolean completed = false;
    private Instant createdAt;
    private Instant completedAt;

    // Default constructor for JSON deserialization
    public AgentGoal() {
        this.createdAt = Instant.now();
    }

    public AgentGoal(String description) {
        this.description = description;
        this.createdAt = Instant.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed) {
            this.completedAt = Instant.now();
        }
    }

    public void complete() {
        this.completed = true;
        this.completedAt = Instant.now();
    }

    public List<SubGoal> getSubGoals() {
        return subGoals;
    }

    public void setSubGoals(List<SubGoal> subGoals) {
        this.subGoals = subGoals;
    }

    public void addSubGoal(String text) {
        subGoals.add(new SubGoal(text));
    }

    public SubGoal nextPending() {
        return subGoals.stream()
                .filter(s -> !s.isCompleted())
                .findFirst()
                .orElse(null);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}