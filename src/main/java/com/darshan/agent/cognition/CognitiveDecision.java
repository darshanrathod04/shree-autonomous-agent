package com.darshan.agent.cognition;

public class CognitiveDecision {

    public enum Action {
        RESPOND,
        RECALL_AND_RESPOND,
        USE_TOOL,
        ASK_CLARIFICATION,
        PAUSE_AGENT,
        REFUSE
    }

    private final Action action;
    private final String reason;

    public CognitiveDecision(Action action, String reason) {
        this.action = action;
        this.reason = reason;
    }

    public Action getAction() {
        return action;
    }

    public String getReason() {
        return reason;
    }
}