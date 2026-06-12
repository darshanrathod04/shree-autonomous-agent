package com.darshan.agent.cognition;

public class MotivationState {

    private double motivation = 0.7; // 0–1
    private double confidence = 0.5;
    private double fatigue = 0.1;

    public double getMotivation() { return motivation; }
    public double getConfidence() { return confidence; }
    public double getFatigue() { return fatigue; }

    public void increaseConfidence() {
        confidence = Math.min(1.0, confidence + 0.05);
        motivation = Math.min(1.0, motivation + 0.03);
    }

    public void decreaseConfidence() {
        confidence = Math.max(0.0, confidence - 0.05);
        motivation = Math.max(0.0, motivation - 0.04);
    }

    public void addFatigue() {
        fatigue = Math.min(1.0, fatigue + 0.02);
    }

    public void recover() {
        fatigue = Math.max(0.0, fatigue - 0.05);
    }
}
