package com.darshan.agent.cognition;

public class MetaThought {

    private final boolean successful;
    private final String evaluation;
    private final String improvement;

    public MetaThought(boolean successful,
                       String evaluation,
                       String improvement) {
        this.successful = successful;
        this.evaluation = evaluation;
        this.improvement = improvement;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getEvaluation() {
        return evaluation;
    }

    public String getImprovement() {
        return improvement;
    }
}