package com.darshan.agent.autonomy;

public class SubGoal {

    private String description;
    private boolean completed;

    public SubGoal(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void complete() {
        this.completed = true;
    }
}
