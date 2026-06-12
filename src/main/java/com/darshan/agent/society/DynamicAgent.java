package com.darshan.agent.society;

public class DynamicAgent {

    private final String id;
    private final String role;
    private double score = 0;

    public DynamicAgent(String id, String role) {
        this.id = id;
        this.role = role;
    }

    public String id() { return id; }
    public String role() { return role; }

    public void reward() { score += 1; }
    public void punish() { score -= 0.5; }

    public double score() { return score; }
}
