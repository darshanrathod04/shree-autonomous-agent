package com.darshan.agent.self;

public class SelfState {

    private double confidence = 0.5;
    private String currentFocus = "Learning";
    private int autonomyLevel = 1;

    public double getConfidence() {
        return confidence;
    }

    public void increaseConfidence() {
        confidence = Math.min(1.0, confidence + 0.05);
    }

    public void decreaseConfidence() {
        confidence = Math.max(0.0, confidence - 0.05);
    }

    public String getCurrentFocus() {
        return currentFocus;
    }

    public void setCurrentFocus(String focus) {
        this.currentFocus = focus;
    }

    public int getAutonomyLevel() {
        return autonomyLevel;
    }

    public void increaseAutonomy() {
        autonomyLevel++;
    }
}