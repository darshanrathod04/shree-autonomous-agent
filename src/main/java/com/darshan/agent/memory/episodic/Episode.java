package com.darshan.agent.memory.episodic;

import java.time.LocalDateTime;

public class Episode {

    private final EpisodeType type;
    private final String summary;
    private final String userInput;
    private final String agentResponse;
    private final double successScore;
    private final LocalDateTime timestamp;

    public Episode(
            EpisodeType type,
            String summary,
            String userInput,
            String agentResponse,
            double successScore
    ) {
        this.type = type;
        this.summary = summary;
        this.userInput = userInput;
        this.agentResponse = agentResponse;
        this.successScore = successScore;
        this.timestamp = LocalDateTime.now();
    }

    public EpisodeType getType() { return type; }
    public String getSummary() { return summary; }
    public String getUserInput() { return userInput; }
    public String getAgentResponse() { return agentResponse; }
    public double getSuccessScore() { return successScore; }
    public LocalDateTime getTimestamp() { return timestamp; }
}