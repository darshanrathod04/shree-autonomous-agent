package com.darshan.agent.debate;

import com.darshan.agent.llm.OllamaClient;
import org.springframework.stereotype.Component;

@Component
public class ResearchAgent {

    private final OllamaClient llm;
    private final DebateMemory memory;

    public ResearchAgent(OllamaClient llm,
                         DebateMemory memory) {
        this.llm = llm;
        this.memory = memory;
    }

    public String propose(String problem) {

        String prompt = """
        You are a research agent.
        Propose a solution to:

        """ + problem;

        String reply = llm.generate(prompt);

        memory.add("RESEARCHER", reply);
        return reply;
    }
}

