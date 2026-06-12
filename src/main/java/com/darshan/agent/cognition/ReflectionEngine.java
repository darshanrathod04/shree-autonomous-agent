package com.darshan.agent.cognition;

import com.darshan.agent.memory.semantic.SemanticMemoryEngine;
import org.springframework.stereotype.Component;

@Component
public class ReflectionEngine {

    private final SemanticMemoryEngine semantic;

    public ReflectionEngine(SemanticMemoryEngine semantic) {
        this.semantic = semantic;
    }

    public ReflectionResult reflect(
            String input,
            String response,
            Thought thought) {

        boolean success = true;
        String summary = "Response looks reasonable.";
        String improvement = "Maintain current reasoning.";

        if (response.contains("Java")) {
            semantic.learn(
                    "java",
                    "Java is an object-oriented programming language used for backend and applications."
            );
        }

        // --- evaluation rules ---

        if (response == null || response.length() < 15) {
            success = false;
            summary = "Response too short.";
            improvement = "Provide more detailed explanations.";
        }

        else if (response.contains("I am still learning")) {
            success = false;
            summary = "Low confidence detected.";
            improvement = "Answer more confidently.";
        }

        else if (!response.toLowerCase()
                .contains(thought.getIntent().toLowerCase())) {

            success = false;
            summary = "Response not aligned with intent.";
            improvement = "Stay focused on user intent.";
        }

        double score = evaluateQuality(response);

        return new ReflectionResult(
                success,
                summary,
                improvement,
                score
        );
    }

    private double evaluateQuality(String text) {

        if (text == null || text.isBlank()) {
            return 0.2;
        }

        int length = text.length();

        if (length > 300) return 0.9;
        if (length > 150) return 0.7;
        if (length > 60)  return 0.5;

        return 0.3;
    }
}