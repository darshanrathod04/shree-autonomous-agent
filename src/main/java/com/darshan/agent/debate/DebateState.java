package com.darshan.agent.debate;

public class DebateState {

    private String proposal;
    private String critique;
    private int round;

    public DebateState(String proposal) {
        this.proposal = proposal;
        this.round = 1;
    }

    public String getProposal() { return proposal; }
    public void setProposal(String proposal) { this.proposal = proposal; }

    public String getCritique() { return critique; }
    public void setCritique(String critique) { this.critique = critique; }

    public int nextRound() { return ++round; }
}
