package com.darshan.agent.society;

import org.springframework.stereotype.Component;

@Component
public class EvolutionEngine {

    private final AgentSociety society;

    public EvolutionEngine(AgentSociety society) {
        this.society = society;
    }

    public void evolve(String problem) {

        if (problem.length() > 80) {
            society.spawn("RESEARCHER");
        }

        if (problem.contains("plan")) {
            society.spawn("PLANNER");
        }

        if (problem.contains("improve")) {
            society.spawn("CRITIC");
        }
    }
}
