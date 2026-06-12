package com.darshan.agent.cognition.learning;

public class LearningRecord {

    private String intent;
    private String strategy;
    private boolean success;

    public LearningRecord() {}

    public LearningRecord(String intent,
                          String strategy,
                          boolean success) {
        this.intent = intent;
        this.strategy = strategy;
        this.success = success;
    }

    public String getIntent() {
        return intent;
    }

    public String getStrategy() {
        return strategy;
    }

    public boolean isSuccess() {
        return success;
    }
}
