package com.darshan.agent.debate;

import com.darshan.agent.llm.OllamaClient;
import org.springframework.stereotype.Component;

@Component
public class JudgeAgent {

    private final OllamaClient llm;

    public JudgeAgent(OllamaClient llm) {
        this.llm = llm;
    }

    public boolean isGoodEnough(String answer) {

        String prompt = """
        Decide if this answer is complete and correct.
        Reply ONLY YES or NO.

        Answer:
        %s
        """.formatted(answer);

        String result = llm.generate(prompt);

        return result.toLowerCase().contains("yes");
    }
}
