package com.darshan.agent.brain.executive;

public class ExecutiveDecision {

    public enum Action {
        RESPOND,
        ASK_CLARIFICATION,
        START_TEACHING,
        CREATE_GOAL,
        WAIT
    }

    private final Action action;
    private final String reason;

    public ExecutiveDecision(Action action, String reason) {
        this.action = action;
        this.reason = reason;
    }

    public Action getAction() { return action; }
    public String getReason() { return reason; }
}