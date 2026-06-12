package com.darshan.agent.cognition;

public class ReflectionResult {

    private boolean needsImprovement;
    private String feedback;
    private final String summary;
    private final double score;

    public ReflectionResult(boolean needsImprovement,
                            String feedback,
                            String summary, double score) {
        this.needsImprovement = needsImprovement;
        this.feedback = feedback;
        this.summary = summary;
        this.score = score;
    }

    public boolean needsImprovement() {
        return needsImprovement;
    }

    public String getFeedback() {
        return feedback;
    }

    public String getSummary() {
        return summary;
    }

    public double getScore() {
        return score;
    }
}
