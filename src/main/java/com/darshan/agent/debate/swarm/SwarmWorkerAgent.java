package com.darshan.agent.debate.swarm;

import com.darshan.agent.llm.OllamaClient;
import org.springframework.stereotype.Component;

@Component
public class SwarmWorkerAgent {

    private final OllamaClient llm;
    private final String name;

    public SwarmWorkerAgent(OllamaClient llm) {
        this.llm = llm;
        this.name = "worker-" + hashCode();
    }

    public String name() {
        return name;
    }

    public String solve(String problem) {
        // Use generateDirect to avoid duplicate memory recall
        return llm.generateDirect("""
        Solve logically and clearly:

        %s
        """.formatted(problem));
    }
}