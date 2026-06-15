package com.darshan.agent.autonomy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SubGoal {

    private String description;
    private boolean completed;

    // Default constructor for JSON deserialization
    public SubGoal() {
    }

    @JsonCreator
    public SubGoal(@JsonProperty("description") String description) {
        this.description = description;
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
    }

    public void complete() {
        this.completed = true;
    }
}