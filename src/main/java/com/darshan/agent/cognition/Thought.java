package com.darshan.agent.cognition;

public class Thought {

    private String goal;
    private String intent;
    private String action;
    private String reasoning;

    public Thought(String goal, String intent,
                   String action, String reasoning) {
        this.goal = goal;
        this.intent = intent;
        this.action = action;
        this.reasoning = reasoning;
    }

    public String getGoal() { return goal; }
    public String getIntent() { return intent; }
    public String getAction() { return action; }
    public String getReasoning() { return reasoning; }
}
