package com.darshan.agent.cognition.learning;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SelfLearningEngine {

    private final LearningStore store;

    public SelfLearningEngine(LearningStore store) {
        this.store = store;
    }

    // Extract learning from conversation
    public LearningMemory extract(String userInput,
                                  String response) {

        if (userInput == null) return null;

        String lower = userInput.toLowerCase();

        if (lower.contains("i like")) {
            return new LearningMemory(
                    userInput,
                    "PREFERENCE",
                    0.9
            );
        }

        if (lower.contains("i am")) {
            return new LearningMemory(
                    userInput,
                    "USER_FACT",
                    0.8
            );
        }

        return null;
    }

    // Store learning experience
    public void learn(String intent,
                      String strategy,
                      boolean success) {

        store.add(
                new LearningRecord(intent,
                        strategy,
                        success)
        );
    }
    public String suggestStrategy(String intent) {

        var records = store.load();

        int success = 0;
        int fail = 0;

        for (var r : records) {
            if (intent.equals(r.getIntent())) {
                if (r.isSuccess()) success++;
                else fail++;
            }
        }

        if (fail > success) {
            return "CHANGE_STRATEGY";
        }

        return "DEFAULT";
    }

}
