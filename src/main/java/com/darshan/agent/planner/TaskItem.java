package com.darshan.agent.planner;

public class TaskItem {

    private String title;
    private String description;
    private int day;

    public TaskItem() {}

    public TaskItem(String title, String description, int day) {
        this.title = title;
        this.description = description;
        this.day = day;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getDay() {
        return day;
    }
}
