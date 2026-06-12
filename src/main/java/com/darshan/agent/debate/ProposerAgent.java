package com.darshan.agent.debate;

import com.darshan.agent.llm.OllamaClient;
import org.springframework.stereotype.Component;

@Component
public class ProposerAgent {

    private final OllamaClient llm;

    public ProposerAgent(OllamaClient llm) {
        this.llm = llm;
    }

    public String propose(String problem, String feedback) {

        String prompt = """
        You are a reasoning agent.
        Improve the answer using feedback.

        Problem:
        %s

        Feedback:
        %s

        Provide improved solution.
        """.formatted(problem, feedback);

        return llm.generate(prompt);
    }
}
