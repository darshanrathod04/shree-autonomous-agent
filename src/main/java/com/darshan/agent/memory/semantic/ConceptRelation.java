package com.darshan.agent.memory.semantic;

public class ConceptRelation {

    private final String source;
    private final String relation;
    private final String target;
    private int strength = 1;

    public ConceptRelation(String source,
                           String relation,
                           String target) {
        this.source = source.toLowerCase();
        this.relation = relation;
        this.target = target.toLowerCase();
    }

    public void reinforce() {
        strength++;
    }

    public String getSource() { return source; }
    public String getRelation() { return relation; }
    public String getTarget() { return target; }
    public int getStrength() { return strength; }
}