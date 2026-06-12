package com.darshan.agent.autonomy;

import java.util.ArrayList;
import java.util.List;

public class AgentGoal {

    private final String description;
    private final List<SubGoal> subGoals = new ArrayList<>();
    private boolean completed = false;

    public AgentGoal(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    // ✅ completion state
    public boolean isCompleted() {
        return completed;
    }

    public void complete() {
        this.completed = true;
    }

    public List<SubGoal> getSubGoals() {
        return subGoals;
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
}
