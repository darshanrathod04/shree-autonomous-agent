package com.darshan.agent.cognition;

import org.springframework.stereotype.Component;

@Component
public class MetaCognitionEngine {

    private MetaThought lastThought;

    // ⭐ Used by ChatSkill
    public MetaThought evaluate(String userInput,
                                String agentResponse) {

        if (agentResponse.length() < 25) {
            lastThought = new MetaThought(
                    false,
                    "Answer too short",
                    "Provide deeper explanation"
            );
        } else {
            lastThought = new MetaThought(
                    true,
                    "Response acceptable",
                    "Continue strategy"
            );
        }

        return lastThought;
    }

    // ⭐ Used by CognitiveLoop
    public MetaThought observe(
            Thought thought,
            ReflectionResult reflection) {

        boolean success =
                reflection.getScore() > 0.6;

        String evaluation =
                success
                        ? "Good reasoning"
                        : "Needs improvement";

        String improvement =
                success
                        ? "Maintain strategy"
                        : "Explain more clearly";

        return new MetaThought(
                success,
                evaluation,
                improvement
        );
    }

    // ⭐ Strategy adjustment hook
    public String adjustStrategy() {

        if (lastThought == null)
            return "No adjustment";

        if (!lastThought.isSuccessful()) {
            return "Switch to detailed reasoning mode";
        }

        return "Maintain strategy";
    }
}