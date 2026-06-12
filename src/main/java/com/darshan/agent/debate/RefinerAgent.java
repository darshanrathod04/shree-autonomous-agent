package com.darshan.agent.debate;

import org.springframework.stereotype.Component;

@Component
public class RefinerAgent {

    public String refine(String problem, String critique) {

        return """
        Improve the solution using this critique:

        Problem:
        %s

        Critique:
        %s

        Produce a refined and improved answer.
        """.formatted(problem, critique);
    }
}
