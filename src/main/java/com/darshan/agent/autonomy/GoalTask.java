package com.darshan.agent.autonomy;

/**
 * Structured task data for roadmap/next-step responses.
 * Returned directly without LLM invocation.
 */
public class GoalTask {

    private String title;
    private String description;
    private double estimatedHours;
    private String status;

    public GoalTask() {
    }

    public GoalTask(String title, String description, double estimatedHours, String status) {
        this.title = title;
        this.description = description;
        this.estimatedHours = estimatedHours;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(double estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}