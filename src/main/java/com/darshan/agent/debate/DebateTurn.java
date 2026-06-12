package com.darshan.agent.debate;

public class DebateTurn {

    private final String agent;
    private final String message;

    public DebateTurn(String agent, String message) {
        this.agent = agent;
        this.message = message;
    }

    public String getAgent() { return agent; }
    public String getMessage() { return message; }
}
