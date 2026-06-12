package com.darshan.agent.cognition;

public class MetaState {

    private int retryCount;
    private String lastStrategy;

    public int getRetryCount() {
        return retryCount;
    }

    public void incrementRetry() {
        retryCount++;
    }

    public void reset() {
        retryCount = 0;
        lastStrategy = null;
    }

    public String getLastStrategy() {
        return lastStrategy;
    }

    public void setLastStrategy(String strategy) {
        this.lastStrategy = strategy;
    }
}
