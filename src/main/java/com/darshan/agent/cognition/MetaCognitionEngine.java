 package com.darshan.agent.cognition;

import org.springframework.stereotype.Component;

@Component
public class MetaCognitionEngine {

    private MetaThought lastThought;

    // ⭐ Used by ChatSkill
    public MetaThought evaluate(String userInput,
                                String agentResponse) {

        // Short responses are not necessarily failures - greetings, confirmations,
        // and quick answers are valid. Only flag truly empty or error responses.
        if (agentResponse == null || agentResponse.isBlank()) {
            lastThought = new MetaThought(
                    false,
                    "Empty response",
                    "Provide a meaningful answer"
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