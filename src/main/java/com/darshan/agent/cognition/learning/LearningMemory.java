package com.darshan.agent.cognition.learning;

public class LearningMemory {

    private String lesson;
    private String category;
    private double importance;

    public LearningMemory() {}

    public LearningMemory(String lesson,
                          String category,
                          double importance) {
        this.lesson = lesson;
        this.category = category;
        this.importance = importance;
    }

    public String getLesson() { return lesson; }
    public String getCategory() { return category; }
    public double getImportance() { return importance; }

    public void setLesson(String lesson) {
        this.lesson = lesson;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setImportance(double importance) {
        this.importance = importance;
    }
}
