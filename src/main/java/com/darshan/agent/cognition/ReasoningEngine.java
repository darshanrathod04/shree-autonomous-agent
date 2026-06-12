package com.darshan.agent.cognition;

import org.springframework.stereotype.Component;

@Component
public class ReasoningEngine {

    public Thought think(String input, String goal) {

        switch (goal) {

            case "PLANNING":
                return new Thought(
                        "PLANNING",
                        "PLAN_DAY",
                        "CREATE_PLAN",
                        "User wants structured planning."
                );

            case "REMINDER":
                return new Thought(
                        "REMINDER",
                        "REMINDER",
                        "START_REMINDER_FLOW",
                        "User wants notification."
                );

            default:
                return new Thought(
                        "GENERAL",
                        "CHAT",
                        "USE_SKILL",
                        "General conversation."
                );
        }

    }
}
