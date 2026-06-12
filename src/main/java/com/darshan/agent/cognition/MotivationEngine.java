package com.darshan.agent.cognition;

import org.springframework.stereotype.Component;

@Component
public class MotivationEngine {

    private final MotivationState state = new MotivationState();

    public MotivationState getState() {
        return state;
    }

    public void evaluate(String result) {

        if (result == null) return;

        String lower = result.toLowerCase();

        if (lower.contains("done") || lower.contains("completed")) {
            state.increaseConfidence();
        } else {
            state.decreaseConfidence();
        }

        state.addFatigue();

        logState();
    }

    private void logState() {
        System.out.println(
                "🧠 Motivation=" + state.getMotivation()
                        + " Confidence=" + state.getConfidence()
                        + " Fatigue=" + state.getFatigue()
        );
    }
}
