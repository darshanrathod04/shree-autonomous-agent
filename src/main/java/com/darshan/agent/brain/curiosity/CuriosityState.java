package com.darshan.agent.brain.curiosity;

public class CuriosityState {

    private double curiosityLevel = 0.3;
    private String lastQuestion;

    public void increase(double value) {
        curiosityLevel = Math.min(1.0, curiosityLevel + value);
    }

    public void decrease(double value) {
        curiosityLevel = Math.max(0.0, curiosityLevel - value);
    }

    public double level() {
        return curiosityLevel;
    }

    public void rememberQuestion(String q) {
        this.lastQuestion = q;
    }

    public String getLastQuestion() {
        return lastQuestion;
    }
}