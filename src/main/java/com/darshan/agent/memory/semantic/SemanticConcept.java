package com.darshan.agent.memory.semantic;

public class SemanticConcept {

    private String concept;
    private String meaning;
    private int reinforcement;

    public SemanticConcept(String concept, String meaning) {
        this.concept = concept;
        this.meaning = meaning;
        this.reinforcement = 1;
    }

    public String getConcept() { return concept; }
    public String getMeaning() { return meaning; }

    public void reinforce() {
        reinforcement++;
    }

    public int getReinforcement() {
        return reinforcement;
    }
}