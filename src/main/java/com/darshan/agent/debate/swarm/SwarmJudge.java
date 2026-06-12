package com.darshan.agent.debate.swarm;

import com.darshan.agent.llm.OllamaClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SwarmJudge {

    private final OllamaClient llm;

    public SwarmJudge(OllamaClient llm) {
        this.llm = llm;
    }

    public String chooseBest(List<SwarmResult> results) {

        StringBuilder combined = new StringBuilder();

        for (SwarmResult r : results) {
            combined.append("""
                    Role: %s
                    Answer: %s

                    """.formatted(r.role(), r.answer()));
        }

        String prompt = """
                Evaluate all answers and choose the best reasoning.

                %s

                Return the best final answer only.
                """.formatted(combined);

        return llm.generate(prompt);
    }
}
