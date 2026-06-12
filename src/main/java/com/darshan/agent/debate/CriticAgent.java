package com.darshan.agent.debate;

import com.darshan.agent.llm.OllamaClient;
import org.springframework.stereotype.Component;

@Component
public class CriticAgent {

    private final OllamaClient llm;

    public CriticAgent(OllamaClient llm) {
        this.llm = llm;
    }

    public String critique(String answer) {

        String prompt = """
        Critically analyze this answer.
        Find logical errors, missing steps,
        or improvements.

        Answer:
        %s
        """.formatted(answer);

        return llm.generate(prompt);
    }
}

