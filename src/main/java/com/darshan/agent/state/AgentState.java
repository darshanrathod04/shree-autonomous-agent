package com.darshan.agent.state;

import org.springframework.stereotype.Component;

@Component
public class AgentState {

    private double motivation = 0.7;
    private double confidence = 0.6;
    private double fatigue = 0.1;

    public double getMotivation() { return motivation; }
    public double getConfidence() { return confidence; }
    public double getFatigue() { return fatigue; }

    public void success() {
        motivation = Math.min(1, motivation + 0.05);
        confidence = Math.min(1, confidence + 0.05);
        fatigue = Math.max(0, fatigue - 0.03);
    }

    public void failure() {
        motivation = Math.max(0, motivation - 0.05);
        confidence = Math.max(0, confidence - 0.05);
        fatigue = Math.min(1, fatigue + 0.05);
    }
}