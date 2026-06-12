package com.darshan.agent.autonomy;

import com.darshan.agent.llm.OllamaClient;
import org.springframework.stereotype.Component;

@Component
public class SubGoalPlanner {

    private final OllamaClient llm;

    public SubGoalPlanner(OllamaClient llm) {
        this.llm = llm;
    }

    public void generateSubGoals(AgentGoal goal, String memoryContext) {

        String prompt = """
        Break this goal into 3 small actionable steps.
        Return each step on new line.

        Goal:
        """ + goal.getDescription();

        String result = llm.generate(prompt);

        String[] steps = result.split("\n");

        for (String s : steps) {
            if (!s.isBlank()) {
                goal.addSubGoal(s.trim());
            }
        }
    }
}
